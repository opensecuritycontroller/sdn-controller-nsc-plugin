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
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.osgi.service.jdbc.DataSourceFactory.*;

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

					mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.antlr").versionAsInProject(),
					mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j").versionAsInProject(),
                    mavenBundle("org.javassist", "javassist").versionAsInProject(),
                    mavenBundle("org.jboss.logging", "jboss-logging").versionAsInProject(),
                    mavenBundle("org.jboss", "jandex").versionAsInProject(),

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

        ServiceReference<DataSourceFactory> dsRef = this.context.getServiceReference(DataSourceFactory.class);
        this.jdbcFactory = this.context.getService(dsRef);

        ServiceReference<EntityManagerFactoryBuilder> emRef = this.context
                .getServiceReference(EntityManagerFactoryBuilder.class);
        this.builder = this.context.getService(emRef);

        ServiceReference<TransactionControl> txcRef = this.context.getServiceReference(TransactionControl.class);
        this.txControl = this.context.getService(txcRef);

        ServiceReference<JPAEntityManagerProviderFactory> jpaRef = this.context
                .getServiceReference(JPAEntityManagerProviderFactory.class);
        this.resourceFactory = this.context.getService(jpaRef);

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

        assertNotNull(this.em);

        setupDataObjects();

    }

    private void setupDataObjects() {
        this.inspectionHook = new InspectionHookEntity();

        this.inspectionPort = new InspectionPortEntity();

        this.ingress = new NetworkElementEntity();
        this.egress = new NetworkElementEntity();
        this.inspected = new NetworkElementEntity();

        this.iMac1 = new MacAddressEntity();
        this.iMac2 = new MacAddressEntity();
        this.eMac1 = new MacAddressEntity();
        this.eMac2 = new MacAddressEntity();
        this.inspMac = new MacAddressEntity();

        this.iPort1 = new PortIpEntity();
        this.iPort2 = new PortIpEntity();
        this.ePort1 = new PortIpEntity();
        this.ePort2 = new PortIpEntity();

        this.ingress.setElementId(IMAC1_STR + IMAC1_STR);
        this.egress.setElementId(EMAC1_STR + EMAC1_STR);
        this.inspected.setElementId("iNsPeCtEdPoRt");

        this.iMac1.setMacAddress(IMAC1_STR);
        this.iMac2.setMacAddress(IMAC2_STR);
        this.eMac1.setMacAddress(EMAC1_STR);
        this.eMac2.setMacAddress(EMAC2_STR);

        this.inspMac.setMacAddress(INSPMAC1_STR);

        this.iPort1.setPortIp(IADDR1_STR);
        this.iPort2.setPortIp(IADDR2_STR);
        this.ePort1.setPortIp(EADDR1_STR);
        this.ePort2.setPortIp(EADDR2_STR);

        this.iPort1.setElement(this.ingress);
        this.iPort2.setElement(this.ingress);
        this.ePort1.setElement(this.egress);
        this.ePort2.setElement(this.egress);

        this.iMac1.setElement(this.ingress);
        this.iMac2.setElement(this.ingress);
        this.eMac1.setElement(this.egress);
        this.eMac2.setElement(this.egress);

        this.ingress.setMacAddressEntities(asList(this.iMac1, this.iMac2));
        this.ingress.setPortIpEntities(asList(this.iPort1, this.iPort2));

        this.egress.setMacAddressEntities(asList(this.eMac1, this.eMac2));
        this.egress.setPortIpEntities(asList(this.ePort1, this.ePort2));

        this.inspected.setMacAddressEntities(asList(this.inspMac));

        this.ingress.setIngressInspectionPort(this.inspectionPort);
        this.egress.setEgressInspectionPort(this.inspectionPort);
        this.inspected.setInspectionHook(this.inspectionHook);

        this.inspectionPort.setIngress(this.ingress);
        this.inspectionPort.setEgress(this.egress);
        this.inspectionHook.setInspectedPort(this.inspected);

        this.inspectionPort.setInspectionHook(this.inspectionHook);
        this.inspectionHook.setInspectionPort(this.inspectionPort);
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

        assertEquals(null, this.inspectionPort.getId());

        InspectionHookEntity inspHookEntity = this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        assertNotNull(this.inspectionPort.getId());

        List<MacAddressEntity> lsMacs;

        lsMacs = this.txControl.requiresNew(() -> {
            CriteriaBuilder cb = this.em.getCriteriaBuilder();

            CriteriaQuery<MacAddressEntity> query = cb.createQuery(MacAddressEntity.class);
            Root<MacAddressEntity> from = query.from(MacAddressEntity.class);
            query = query.select(from).distinct(true);
            return this.em.createQuery(query).getResultList();

        });

        assertEquals(5, lsMacs.size());

        List<PortIpEntity> lsPorts;

        lsPorts = this.txControl.requiresNew(() -> {
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

        InspectionHookEntity inspHookEntity = this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        InspectionHookEntity persistedHook = this.txControl.required(() -> {
            InspectionHookEntity ph = this.em.find(InspectionHookEntity.class, this.inspectionHook.getHookId());
            InspectionPortEntity iprt = this.em.find(InspectionPortEntity.class, this.inspectionPort.getId());

            assertNotNull(this.inspectionPort.getInspectionHook());
            assertEquals(this.inspectionPort.getId(), iprt.getId());
            return ph;
        });

        assertNotNull(this.inspectionHook.getHookId());
        assertEquals(this.inspectionHook.getHookId(), persistedHook.getHookId());

        InspectionPortEntity persistedPort = persistedHook.getInspectionPort();

        assertNotNull(persistedPort);
        assertEquals(this.inspectionHook.getInspectionPort().getId(), persistedPort.getId());
    }

    @Test
    public void testUtilsInspPortByNetworkElements() throws Exception {

        InspectionHookEntity inspHookEntity = this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        // TODO : Separate the tests!
        NSCUtils utils = new NSCUtils(this.em, this.txControl);

        NetworkElement ingressElement = NSCUtils.makeNetworkElement(this.ingress);
        NetworkElement egressElement = NSCUtils.makeNetworkElement(this.egress);
        InspectionPortEntity foundPort = utils.inspPortByNetworkElements(ingressElement, egressElement);

        assertNotNull(foundPort);
        assertEquals(this.inspectionPort.getId(), foundPort.getId());

    }

    @Test
    public void testUtilsNetworkElementEntityByElementId() throws Exception {

        InspectionHookEntity inspHookEntity = this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        // TODO : Separate the tests!
        NSCUtils utils = new NSCUtils(this.em, this.txControl);

        NetworkElementEntity foundNE = this.txControl.required(() -> {
            NetworkElementEntity e = utils.networkElementEntityByElementId(this.inspected.getElementId());
            e.getMacAddressEntities().size();
            return e;
        });

        assertNotNull(foundNE);
        assertNotNull(foundNE.getMacAddressEntities());
        assertEquals(1, foundNE.getMacAddressEntities().size());

    }

    @Test
    public void testUtilsInspHookByInspectedAndPort() throws Exception {
        InspectionHookEntity inspHookEntity = this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        // TODO : Separate the tests!
        NSCUtils utils = new NSCUtils(this.em, this.txControl);

        InspectionHookEntity foundIH = this.txControl.required(() -> {
            InspectionPortEntity ipe = this.em.find(this.inspectionPort.getClass(), this.inspectionPort.getId());
            assertNotNull(ipe);
            NetworkElement ne = NSCUtils.makeNetworkElement(this.inspected);
            InspectionPortElement prte = NSCUtils.makeInspectionPortElement(ipe);
            InspectionHookEntity ihe = utils.inspHookByInspectedAndPort(ne, prte);

            assertNotNull(ihe);
            assertEquals(this.inspectionHook.getHookId(), ihe.getHookId());
            assertNotNull(ihe.getInspectionPort());
            assertEquals(ihe.getInspectionPort().getId(), ipe.getId());
            return ihe;
        });

        assertEquals(foundIH.getHookId(), this.inspectionHook.getHookId());
        assertNotNull(foundIH.getInspectionPort());
        assertEquals(foundIH.getInspectionPort().getId(), this.inspectionPort.getId());

    }

    @Test
    public void testRegisterInspectionPort() throws Exception {
        NSCUtils utils = new NSCUtils(this.em, this.txControl);
        NeutronSdnRedirectionApi redirApi = new NeutronSdnRedirectionApi(null, "boogus", this.txControl, this.em);

        InspectionPortElement element = new InspectionPortElement() {

            private String parentId = OSGiIntegrationTest.this.inspectionHook.getHookId();
            private NetworkElement ingrElt = NSCUtils.makeNetworkElement(OSGiIntegrationTest.this.ingress);
            private NetworkElement egrElt = NSCUtils.makeNetworkElement(OSGiIntegrationTest.this.egress);

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
                return this.ingrElt;
            }

            @Override
            public NetworkElement getEgressPort() {

                return this.egrElt;
            }
        };

        InspectionPortElement ipe = (InspectionPortElement) redirApi.registerInspectionPort(element);

        assertNotNull(ipe.getIngressPort());

        NetworkElementEntity foundIngr = this.txControl
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
