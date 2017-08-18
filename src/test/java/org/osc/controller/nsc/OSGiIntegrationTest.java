/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bootClasspathLibrary;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_PASSWORD;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_URL;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_USER;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osc.controller.nsc.api.NeutronSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.MacAddressEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.entities.PortIpEntity;
import org.osc.controller.nsc.utils.NSCUtils;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

import junit.framework.Assert;

/*TODO: test is commented, because there is a problem with openstack4j dependecies injected directly into osgi (whicch is build during that test)
    Problem is related to importing some packages and test is failing because of:
org.osgi.framework.BundleException: Unable to resolve openstack4j-jersey2 [17](R 17.0): missing requirement [openstack4j-jersey2 [17](R 17.0)] osgi.wiring.package;
(&(osgi.wiring.package=org.openstack4j.core.transport.internal)(version>=3.0.0)(!(version>=4.0.0))) [caused by: Unable to resolve openstack4j-core [16](R 16.0): missing requirement
[openstack4j-core [16](R 16.0)] osgi.extender; (osgi.extender=osgi.serviceloader.processor)]
Unresolved requirements: [[openstack4j-jersey2 [17](R 17.0)] osgi.wiring.package; (&(osgi.wiring.package=org.openstack4j.core.transport.internal)(version>=3.0.0)(!(version>=4.0.0)))]
*/
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGiIntegrationTest {

	private static final String TEST_DB_URL_PREFIX = "jdbc:h2:";
	private static final String TEST_DB_FILENAME = "./nscPlugin_OSGiIntegrationTest";
	private static final String TEST_DB_URL = TEST_DB_URL_PREFIX + TEST_DB_FILENAME;

	private static final String EADDR2_STR = "192.168.0.12";

	private static final String EADDR1_STR = "192.168.0.11";

	private static final String IADDR2_STR = "10.4.3.2";

	private static final String IADDR1_STR = "10.4.3.1";

	private static final String EMAC2_STR = "ee:ff:aa:bb:cc:02";

	private static final String EMAC1_STR = "ee:ff:aa:bb:cc:01";

	private static final String IMAC1_STR = "ff:ff:aa:bb:cc:01";

	private static final String IMAC2_STR = "ff:ff:aa:bb:cc:02";

	private static final String INSPMAC1_STR = "aa:aa:aa:bb:cc:01";

	private static final String HOOK_ID = "TEST_INSP_HOOK";

	@Inject
	BundleContext context;

	@Inject
	SdnControllerApi api;

	private TransactionControl txControl;
	private EntityManagerFactoryBuilder builder;
	private DataSourceFactory jdbcFactory;
	private JPAEntityManagerProviderFactory resourceFactory;

	private EntityManager em;

	private InspectionHookEntity inspectionHook;
	private InspectionPortEntity inspectionPort;

	private NetworkElementEntity ingress;
	private NetworkElementEntity egress;
	private NetworkElementEntity inspected;

	private MacAddressEntity iMac1;
	private MacAddressEntity iMac2;
	private MacAddressEntity eMac1;
	private MacAddressEntity eMac2;
	private MacAddressEntity inspMac;

	private PortIpEntity iPort1;
	private PortIpEntity iPort2;
	private PortIpEntity ePort1;
	private PortIpEntity ePort2;

	@org.ops4j.pax.exam.Configuration
	public Option[] config() {

		try {
			return options(

					// Load the current module from its built classes so we get
					// the latest from Eclipse
					bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),
					// And some dependencies
					mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),

					mavenBundle("org.osc.api", "sdn-controller-api").versionAsInProject(),

					mavenBundle("org.osgi", "org.osgi.core").versionAsInProject(),

					mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.container").versionAsInProject(),
					mavenBundle("org.apache.aries.tx-control", "tx-control-service-local").versionAsInProject(),
					mavenBundle("org.apache.aries.tx-control", "tx-control-provider-jpa-local").versionAsInProject(),
					mavenBundle("com.h2database", "h2").versionAsInProject(),

					// Hibernate

					systemPackage("javax.xml.stream;version=1.0"), systemPackage("javax.xml.stream.events;version=1.0"),
					systemPackage("javax.xml.stream.util;version=1.0"), systemPackage("javax.transaction;version=1.1"),
					systemPackage("javax.transaction.xa;version=1.1"),
					mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.antlr", "2.7.7_5"),
					mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j", "1.6.1_5"),
					mavenBundle("org.javassist", "javassist", "3.18.1-GA"),
					mavenBundle("org.jboss.logging", "jboss-logging", "3.3.0.Final"),
					mavenBundle("org.jboss", "jandex", "2.0.0.Final"),
					mavenBundle("org.hibernate.common", "hibernate-commons-annotations").versionAsInProject(),
					mavenBundle("org.hibernate", "hibernate-core").versionAsInProject(),
					mavenBundle("org.hibernate", "hibernate-osgi").versionAsInProject(),
					mavenBundle("com.fasterxml", "classmate").versionAsInProject(),
					mavenBundle("org.javassist", "javassist").versionAsInProject(),

					mavenBundle("log4j", "log4j").versionAsInProject(),

					mavenBundle("org.apache.directory.studio", "org.apache.commons.lang").versionAsInProject(),

					// Uncomment this line to allow remote debugging
					// CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),
					bootClasspathLibrary(mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec", "1.1.1"))
							.beforeFramework(),
					junitBundles()

			);
		} catch (Throwable t) {

			System.err.println(t.getClass().getName() + ":\n" + t.getMessage());
			t.printStackTrace(System.err);
			throw t;
		}
	}

	@Before
	public void setup() {

		ServiceReference<DataSourceFactory> dsRef = context.getServiceReference(DataSourceFactory.class);
		this.jdbcFactory = this.context.getService(dsRef);

		ServiceReference<EntityManagerFactoryBuilder> emRef = context
				.getServiceReference(EntityManagerFactoryBuilder.class);
		this.builder = this.context.getService(emRef);

		ServiceReference<TransactionControl> txcRef = context.getServiceReference(TransactionControl.class);
		txControl = this.context.getService(txcRef);

		ServiceReference<JPAEntityManagerProviderFactory> jpaRef = context
				.getServiceReference(JPAEntityManagerProviderFactory.class);
		resourceFactory = this.context.getService(jpaRef);

		assertNotNull(this.jdbcFactory);
		assertNotNull(this.builder);
		assertNotNull(this.txControl);
		assertNotNull(this.resourceFactory);

		Properties props = new Properties();

		props.setProperty(JDBC_URL, TEST_DB_URL);
		props.setProperty(JDBC_USER, "admin");
		props.setProperty(JDBC_PASSWORD, "admin123");

		DataSource ds = null;
		try {
			ds = this.jdbcFactory.createDataSource(props);
		} catch (SQLException e) {
			Assert.fail(e.getClass() + " : " + e.getMessage());
		}

		this.em = this.resourceFactory
				.getProviderFor(this.builder, singletonMap("javax.persistence.nonJtaDataSource", (Object) ds), null)
				.getResource(this.txControl);

		assertNotNull(em);

		setupDataObjects();

	}

	private void setupDataObjects() {
		inspectionHook = new InspectionHookEntity();

		inspectionPort = new InspectionPortEntity();

		ingress = new NetworkElementEntity();
		egress = new NetworkElementEntity();
		inspected = new NetworkElementEntity();

		iMac1 = new MacAddressEntity();
		iMac2 = new MacAddressEntity();
		eMac1 = new MacAddressEntity();
		eMac2 = new MacAddressEntity();
		inspMac = new MacAddressEntity();

		iPort1 = new PortIpEntity();
		iPort2 = new PortIpEntity();
		ePort1 = new PortIpEntity();
		ePort2 = new PortIpEntity();

		iMac1.setMacAddress(IMAC1_STR);
		iMac2.setMacAddress(IMAC2_STR);
		eMac1.setMacAddress(EMAC1_STR);
		eMac2.setMacAddress(EMAC2_STR);

		inspMac.setMacAddress(INSPMAC1_STR);

		iPort1.setPortIp(IADDR1_STR);
		iPort2.setPortIp(IADDR2_STR);
		ePort1.setPortIp(EADDR1_STR);
		ePort2.setPortIp(EADDR2_STR);

		iPort1.setElement(ingress);
		iPort2.setElement(ingress);
		ePort1.setElement(egress);
		ePort2.setElement(egress);

		iMac1.setElement(ingress);
		iMac2.setElement(ingress);
		eMac1.setElement(egress);
		eMac2.setElement(egress);

		ingress.setMacAddressEntities(asList(iMac1, iMac2));
		ingress.setPortIpEntities(asList(iPort1, iPort2));

		egress.setMacAddressEntities(asList(eMac1, eMac2));
		egress.setPortIpEntities(asList(ePort1, ePort2));

		inspected.setMacAddressEntities(asList(inspMac));

		ingress.setIngressInspectionPort(inspectionPort);
		egress.setEgressInspectionPort(inspectionPort);
		inspected.setInspectionHook(inspectionHook);

		inspectionPort.setIngress(ingress);
		inspectionPort.setEgress(egress);
		inspectionHook.setInspectedPort(inspected);

		inspectionPort.setInspectionHook(inspectionHook);
		inspectionHook.setInspectionPort(inspectionPort);
	}

	@After
	public void tearDown() {
		File dbfile = new File(TEST_DB_FILENAME + ".h2.db");

		if (!dbfile.delete()) {
			throw new IllegalStateException("Failed to delete database file : " + dbfile.getAbsolutePath());
		}

		File tracefile = new File(TEST_DB_FILENAME + ".trace.db");

		if (!tracefile.delete()) {
			throw new IllegalStateException("Failed to delete trace file : " + tracefile.getAbsolutePath());
		}
	}

	@Test
	public void verifyCorrectNumberOfMacsAdPortIps() throws Exception {

		assertEquals(null, inspectionPort.getId());

		InspectionHookEntity inspHookEntity = txControl.required(() -> {
			em.persist(inspectionHook);
			return inspectionHook;
		});

		assertNotNull(inspectionPort.getId());

		List<MacAddressEntity> lsMacs;

		lsMacs = txControl.requiresNew(() -> {
			CriteriaBuilder cb = this.em.getCriteriaBuilder();

			CriteriaQuery<MacAddressEntity> query = cb.createQuery(MacAddressEntity.class);
			Root<MacAddressEntity> from = query.from(MacAddressEntity.class);
			query = query.select(from).distinct(true);
			return this.em.createQuery(query).getResultList();

		});

		assertEquals(5, lsMacs.size());

		List<PortIpEntity> lsPorts;

		lsPorts = txControl.requiresNew(() -> {
			CriteriaBuilder cb = this.em.getCriteriaBuilder();

			CriteriaQuery<PortIpEntity> query = cb.createQuery(PortIpEntity.class);
			Root<PortIpEntity> from = query.from(PortIpEntity.class);
			query = query.select(from).distinct(true);
			return this.em.createQuery(query).getResultList();

		});

		assertEquals(4, lsPorts.size());

	}

	@Test
	public void verifyHookAndPortPersistedAfterSingleHookPersistenceWithObjectGraphSetUp() {

		InspectionHookEntity inspHookEntity = txControl.required(() -> {
			em.persist(inspectionHook);
			return inspectionHook;
		});

		InspectionHookEntity persistedHook = txControl.required(() -> {
			InspectionHookEntity ph = em.find(InspectionHookEntity.class, inspectionHook.getHookId());
			InspectionPortEntity iprt = em.find(InspectionPortEntity.class, inspectionPort.getId());

			assertNotNull(inspectionPort.getInspectionHook());
			assertEquals(inspectionPort.getId(), iprt.getId());
			return ph;
		});

		assertNotNull(inspectionHook.getHookId());
		assertEquals(inspectionHook.getHookId(), persistedHook.getHookId());

		InspectionPortEntity persistedPort = persistedHook.getInspectionPort();

		assertNotNull(persistedPort);
		assertEquals(inspectionHook.getInspectionPort().getId(), persistedPort.getId());
	}

	@Test
	public void testUtilsInspPortByNetworkElements() throws Exception {

		InspectionHookEntity inspHookEntity = txControl.required(() -> {
			em.persist(inspectionHook);
			return inspectionHook;
		});

		// TODO : Separate the tests!
		NSCUtils utils = new NSCUtils(em, txControl);

		NetworkElement ingressElement = NSCUtils.makeNetworkElement(ingress);
		NetworkElement egressElement = NSCUtils.makeNetworkElement(egress);
		InspectionPortEntity foundPort = utils.inspPortByNetworkElements(ingressElement, egressElement);

		assertNotNull(foundPort);
		assertEquals(inspectionPort.getId(), foundPort.getId());

	}

	@Test
	public void testUtilsNetworkElementEntityByElementId() throws Exception {

		InspectionHookEntity inspHookEntity = txControl.required(() -> {
			em.persist(inspectionHook);
			return inspectionHook;
		});

		// TODO : Separate the tests!
		NSCUtils utils = new NSCUtils(em, txControl);

		NetworkElementEntity foundNE = txControl.required(() -> {
			NetworkElementEntity e = utils.networkElementEntityByElementId(inspected.getElementId());
			e.getMacAddressEntities().size();
			return e;
		});

		assertNotNull(foundNE);
		assertNotNull(foundNE.getMacAddressEntities());
		assertEquals(1, foundNE.getMacAddressEntities().size());

	}

	@Test
	public void testUtilsInspHookByInspectedAndPort() throws Exception {
		InspectionHookEntity inspHookEntity = txControl.required(() -> {
			em.persist(inspectionHook);
			return inspectionHook;
		});

		// TODO : Separate the tests!
		NSCUtils utils = new NSCUtils(em, txControl);

		InspectionHookEntity foundIH = txControl.required(() -> {
			InspectionPortEntity ipe = em.find(inspectionPort.getClass(), inspectionPort.getId());
			assertNotNull(ipe);
			NetworkElement ne = NSCUtils.makeNetworkElement(inspected);
			InspectionPortElement prte = NSCUtils.makeInspectionPortElement(ipe);
			InspectionHookEntity ihe = utils.inspHookByInspectedAndPort(ne, prte);

			assertNotNull(ihe);
			assertEquals(inspectionHook.getHookId(), ihe.getHookId());
			assertNotNull(ihe.getInspectionPort());
			assertEquals(ihe.getInspectionPort().getId(), ipe.getId());
			return ihe;
		});

		assertEquals(foundIH.getHookId(), inspectionHook.getHookId());
		assertNotNull(foundIH.getInspectionPort());
		assertEquals(foundIH.getInspectionPort().getId(), inspectionPort.getId());

	}

	@Test
	public void testRegisterInspectionPort() throws Exception {
		NSCUtils utils = new NSCUtils(em, txControl);
		NeutronSdnRedirectionApi redirApi = new NeutronSdnRedirectionApi(null, "boogus", txControl, em);

		InspectionPortElement element = new InspectionPortElement() {

			private String parentId = inspectionHook.getHookId();
			private NetworkElement ingrElt = NSCUtils.makeNetworkElement(ingress);
			private NetworkElement egrElt = NSCUtils.makeNetworkElement(egress);

			@Override
			public String getParentId() {
				return null;
			}

			@Override
			public String getElementId() {
				return null;
			}

			@Override
			public NetworkElement getIngressPort() {
				return ingrElt;
			}

			@Override
			public NetworkElement getEgressPort() {

				return egrElt;
			}
		};

		InspectionPortElement ipe = (InspectionPortElement) redirApi.registerInspectionPort(element);

		assertNotNull(ipe.getIngressPort());

		NetworkElementEntity foundIngr = txControl
				.required(() -> utils.networkElementEntityByElementId(ipe.getIngressPort().getElementId()));

		assertNotNull(foundIngr);
		assertEquals(ipe.getIngressPort().getElementId(), foundIngr.getElementId());
		assertNotNull(foundIngr.getIngressInspectionPort());
		assertEquals(ipe.getElementId(), foundIngr.getIngressInspectionPort().getId() + "");

		InspectionPortElement ipeFound = redirApi.getInspectionPort(ipe);
		assertEquals(ipe.getIngressPort().getElementId(), ipeFound.getIngressPort().getElementId());
		assertEquals(ipe.getEgressPort().getElementId(), ipeFound.getEgressPort().getElementId());
		assertEquals(ipe.getElementId(), ipeFound.getElementId());

		assertEquals(null, ipe.getParentId());
		assertEquals(null, ipeFound.getParentId());

	}

}
