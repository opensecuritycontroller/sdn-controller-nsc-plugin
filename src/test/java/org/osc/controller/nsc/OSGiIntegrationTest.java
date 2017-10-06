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
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;
import static org.osgi.service.jdbc.DataSourceFactory.*;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.Element;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiIntegrationTest {

    private static final String TEST_DB_URL_PREFIX = "jdbc:h2:";
    private static final String TEST_DB_FILENAME = "./nscPlugin_OSGiIntegrationTest";
    private static final String TEST_DB_URL_SUFFIX = ";MVCC\\=FALSE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;";
    private static final String TEST_DB_URL = TEST_DB_URL_PREFIX + TEST_DB_FILENAME + TEST_DB_URL_SUFFIX;

    private static final String EADDR2_STR = "192.168.0.12";

    private static final String EADDR1_STR = "192.168.0.11";

    private static final String IADDR2_STR = "10.4.3.2";

    private static final String IADDR1_STR = "10.4.3.1";

    private static final String EMAC2_STR = "ee:ff:aa:bb:cc:02";

    private static final String EMAC1_STR = "ee:ff:aa:bb:cc:01";

    private static final String IMAC1_STR = "ff:ff:aa:bb:cc:01";

    private static final String IMAC2_STR = "ff:ff:aa:bb:cc:02";

    private static final String INSPMAC1_STR = "aa:aa:aa:bb:cc:01";

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

    private SampleSdnRedirectionApi redirApi;

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

                    mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.antlr")
                            .versionAsInProject(),
                    mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j")
                            .versionAsInProject(),
                    mavenBundle("org.javassist", "javassist").versionAsInProject(),
                    mavenBundle("org.jboss.logging", "jboss-logging").versionAsInProject(),
                    mavenBundle("org.jboss", "jandex").versionAsInProject(),

                    mavenBundle("org.hibernate.common", "hibernate-commons-annotations").versionAsInProject(),
                    mavenBundle("org.hibernate", "hibernate-core").versionAsInProject(),
                    mavenBundle("org.hibernate", "hibernate-osgi").versionAsInProject(),
                    mavenBundle("com.fasterxml", "classmate").versionAsInProject(),
                    mavenBundle("org.javassist", "javassist").versionAsInProject(),

                    mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                    // Fragment bundles cannot be started
                    mavenBundle("org.slf4j", "slf4j-simple").versionAsInProject().noStart(),

                    mavenBundle("org.apache.directory.studio", "org.apache.commons.lang").versionAsInProject(),

                    // Uncomment this line to allow remote debugging
                    //                    CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),

                    bootClasspathLibrary(mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec", "1.1.1"))
                            .beforeFramework(),
                    junitBundles());
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

        this.ingress.setElementId(IMAC1_STR + IMAC1_STR);
        this.egress.setElementId(EMAC1_STR + EMAC1_STR);
        this.inspected.setElementId("iNsPeCtEdPoRt");

        this.ingress.setMacAddresses(asList(IMAC1_STR, IMAC2_STR));
        this.ingress.setPortIPs(asList(IADDR1_STR, IADDR2_STR));

        this.egress.setMacAddresses(asList(EMAC1_STR, EMAC2_STR));
        this.egress.setPortIPs(asList(EADDR1_STR, EADDR2_STR));

        this.inspected.setMacAddresses(asList(INSPMAC1_STR));

        this.inspected.setInspectionHook(this.inspectionHook);

        this.inspectionPort.setIngressPort(this.ingress);
        this.inspectionPort.setEgressPort(this.egress);
        this.inspectionHook.setInspectedPort(this.inspected);

        this.inspectionHook.setInspectionPort(this.inspectionPort);
    }

    @After
    public void tearDown() throws Exception {

        if (this.redirApi != null) {
            this.redirApi.close();
        }
        File dbfile = new File(TEST_DB_FILENAME + ".h2.db");

        if (!dbfile.delete()) {
            throw new IllegalStateException("Failed to delete database file : " + dbfile.getAbsolutePath());
        }

        File tracefile = new File(TEST_DB_FILENAME + ".trace.db");
        if (tracefile.exists() && !tracefile.delete()) {
            throw new IllegalStateException("Failed to delete trace file : " + tracefile.getAbsolutePath());
        }
    }

    @Test
    public void verifyCorrectNumberOfMacsAdPortIps() throws Exception {

        assertEquals(null, this.inspectionPort.getElementId());

        this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        assertNotNull(this.inspectionPort.getElementId());

        InspectionPortEntity tmp = this.txControl.requiresNew(() -> {
            return this.em.find(InspectionPortEntity.class, this.inspectionPort.getElementId());
        });

        assertEquals(2, tmp.getEgressPort().getMacAddresses().size());
        assertEquals(2, tmp.getEgressPort().getPortIPs().size());
        assertEquals(2, tmp.getIngressPort().getMacAddresses().size());
        assertEquals(2, tmp.getIngressPort().getPortIPs().size());
    }

    @Test
    public void verifyHookAndPortPersistedAfterSingleHookPersistenceWithObjectGraphSetUp() {

        this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        InspectionHookEntity persistedHook = this.txControl.required(() -> {
            InspectionHookEntity ph = this.em.find(InspectionHookEntity.class, this.inspectionHook.getHookId());
            InspectionPortEntity iprt = this.em.find(InspectionPortEntity.class, this.inspectionPort.getElementId());

            assertEquals(this.inspectionPort.getElementId(), iprt.getElementId());
            return ph;
        });

        assertNotNull(this.inspectionHook.getHookId());
        assertEquals(this.inspectionHook.getHookId(), persistedHook.getHookId());

        InspectionPortEntity persistedPort = persistedHook.getInspectionPort();

        assertNotNull(persistedPort);
        assertEquals(this.inspectionHook.getInspectionPort().getElementId(), persistedPort.getElementId());
    }

    @Test
    public void testUtilsInspPortByNetworkElements() throws Exception {

        this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        InspectionPortEntity foundPort = utils.findInspPortByNetworkElements(this.ingress, this.egress);

        assertNotNull(foundPort);
        assertEquals(this.inspectionPort.getElementId(), foundPort.getElementId());

    }

    @Test
    public void testUtilsNetworkElementEntityByElementId() throws Exception {

        this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        NetworkElementEntity foundNE = this.txControl.required(() -> {
            NetworkElementEntity e = utils.txNetworkElementEntityByElementId(this.inspected.getElementId());
            return e;
        });

        assertNotNull(foundNE);
        assertNotNull(foundNE.getMacAddresses());
        assertEquals(1, foundNE.getMacAddresses().size());

    }

    @Test
    public void testUtilsInspHookByInspectedAndPort() throws Exception {
        this.txControl.required(() -> {
            this.em.persist(this.inspectionHook);
            return this.inspectionHook;
        });

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        InspectionHookEntity foundIH = this.txControl.required(() -> {
            InspectionPortEntity ipe = this.em.find(this.inspectionPort.getClass(), this.inspectionPort.getElementId());
            assertNotNull(ipe);

            NetworkElement ne = this.inspected;
            InspectionPortElement prte = ipe;
            InspectionHookEntity ihe = utils.findInspHookByInspectedAndPort(ne, prte);

            assertNotNull(ihe);
            assertEquals(this.inspectionHook.getHookId(), ihe.getHookId());
            //            assertNotNull(ihe.getInspectionPort());
            //            assertEquals(ihe.getInspectionPort().getElementId(), ipe.getElementId());
            return ihe;
        });

        assertEquals(foundIH.getHookId(), this.inspectionHook.getHookId());
        assertNotNull(foundIH.getInspectionPort());
        assertEquals(foundIH.getInspectionPort().getElementId(), this.inspectionPort.getElementId());

    }

    @Test
    public void testUtilsRemoveSingleInspectionHook() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookEntity inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookEntity);
        assertEquals(hookId, inspectionHookEntity.getHookId());

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        utils.removeSingleInspectionHook(hookId);

        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApiRegisterInspectionPort() throws Exception {
        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        // Here we are mostly afraid of LazyInitializationException
        assertNotNull(inspectionPortElement.getElementId());
        inspectionPortElement.getParentId();
        assertNotNull(inspectionPortElement.getIngressPort());
        assertNotNull(inspectionPortElement.getEgressPort());
        assertNotNull(inspectionPortElement.getEgressPort().getMacAddresses());
        assertNotNull(inspectionPortElement.getEgressPort().getElementId());
        assertNotNull(inspectionPortElement.getIngressPort());
        assertNotNull(inspectionPortElement.getIngressPort().getMacAddresses());
        assertNotNull(inspectionPortElement.getIngressPort().getElementId());
        inspectionPortElement.getIngressPort().getParentId();
        inspectionPortElement.getEgressPort().getParentId();

        final InspectionPortElement inspectionPortElementTmp = inspectionPortElement;
        NetworkElementEntity foundIngress = this.txControl.required(() -> {
            NetworkElementEntity elementEntity = utils
                    .txNetworkElementEntityByElementId(inspectionPortElementTmp.getIngressPort().getElementId());
            return elementEntity;
        });

        assertNotNull(foundIngress);
        assertEquals(inspectionPortElement.getIngressPort().getElementId(), foundIngress.getElementId());

        // Here we are afraid of lazyInitializationException
        foundIngress.getMacAddresses();
        foundIngress.getPortIPs();
        foundIngress.getElementId();
        foundIngress.getParentId();

        InspectionPortElement foundInspPortElement = this.redirApi.getInspectionPort(inspectionPortElement);
        assertEquals(inspectionPortElement.getIngressPort().getElementId(),
                foundInspPortElement.getIngressPort().getElementId());
        assertEquals(inspectionPortElement.getEgressPort().getElementId(),
                foundInspPortElement.getEgressPort().getElementId());
        assertEquals(inspectionPortElement.getElementId(), foundInspPortElement.getElementId());

        assertEquals(null, inspectionPortElement.getParentId());
        assertEquals(null, foundInspPortElement.getParentId());
    }

    @Test
    public void testApiRegisterInspectionPortWithNetworkElementsAlreadyPersisted() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        this.txControl.required(() -> {
            this.em.persist(this.ingress);
            this.em.persist(this.egress);
            return null;
        });

        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        // ... and the test
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);
        assertNotNull(inspectionPortElement);
        assertNotNull(inspectionPortElement.getElementId());
        assertNotNull(inspectionPortElement.getIngressPort());
        assertNotNull(inspectionPortElement.getEgressPort());
        assertEquals(this.ingress.getElementId(), inspectionPortElement.getIngressPort().getElementId());
        assertEquals(this.egress.getElementId(), inspectionPortElement.getEgressPort().getElementId());
    }

    @Test
    public void testApiInstallInspectionHook() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);
        final String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookElement inspectionHookElement = this.txControl.required(() -> {
            InspectionHookElement tmp = this.em.find(InspectionHookEntity.class, hookId);
            assertNotNull(tmp);
            assertNotNull(tmp.getInspectionPort());
            return tmp;
        });

        // Here we are mostly afraid of LazyInitializationException
        assertNotNull(inspectionHookElement);
        assertNotNull(inspectionHookElement.getHookId());
        assertNotNull(inspectionHookElement.getInspectionPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort().getMacAddresses());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort().getPortIPs());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort().getMacAddresses());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort().getPortIPs());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort());
        assertNotNull(inspectionHookElement.getInspectedPort().getMacAddresses());
        assertNotNull(inspectionHookElement.getInspectedPort().getPortIPs());
    }

    @Test
    public void testApiGetInspectionHook() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        InspectionHookElement foundInspectionHook = this.redirApi.getInspectionHook(this.inspected,
                inspectionPortElement);
        assertEquals("Inspection Hook already in the database before installed!", null, foundInspectionHook);

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);

        foundInspectionHook = this.redirApi.getInspectionHook(this.inspected, inspectionPortElement);
        assertEquals("Inspection Hook already in the database before installed!", null, foundInspectionHook);

        final String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);
        foundInspectionHook = this.redirApi.getInspectionHook(this.inspected, inspectionPortElement);
        assertNotNull("Not found Inspection Hook by ports after install!", foundInspectionHook);
        foundInspectionHook = this.redirApi.getInspectionHook(hookId);
        assertNotNull("Not found Inspection Hook by id after install!", foundInspectionHook);
    }

    @Test
    public void testApiRemoveInspectionHookByPorts_InspectionHookDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookEntity inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookEntity);
        assertEquals(hookId, inspectionHookEntity.getHookId());

        this.redirApi.removeInspectionHook(inspectionHookEntity.getInspectedPort(),
                inspectionHookEntity.getInspectionPort());

        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApiRemoveInspectionHookById_InspectionHookDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookEntity inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookEntity);
        assertEquals(hookId, inspectionHookEntity.getHookId());

        this.redirApi.removeInspectionHook(inspectionHookEntity.getHookId());

        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApiRemoveAllInspectionHooks_InspectionHookDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookElement inspectionHookElement = this.txControl.required(() -> {
            InspectionHookElement tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookElement);
        assertEquals(hookId, inspectionHookElement.getHookId());

        assertNotNull(inspectionHookElement.getInspectionPort());
        assertEquals(registeredElement.getElementId(), inspectionHookElement.getInspectionPort().getElementId());

        this.redirApi.removeAllInspectionHooks(this.inspected);

        inspectionHookElement = this.txControl.required(() -> {
            InspectionHookElement tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookElement);

        int nInspectionHooks = this.txControl.required(() -> {
            List<InspectionHookEntity> list = this.em
                    .createQuery("FROM InspectionHookEntity", InspectionHookEntity.class).getResultList();
            return list.size();
        });

        assertEquals(0, nInspectionHooks);
    }

    @Test
    public void testApiRemoveAllInspectionHooks_InspectedPortDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        String inspectedId = this.inspected.getElementId();
        assertNotNull(inspectedId);

        this.txControl.required(() -> {
            NetworkElementEntity tmp = this.em.find(NetworkElementEntity.class, inspectedId);
            assertEquals(null, tmp);
            return null;
        });

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);
        String hookId = this.redirApi.installInspectionHook(this.inspected, inspectionPortElement, 0L, VLAN, 0L, NA);

        assertNotNull(hookId);

        NetworkElementEntity foundInspectedPort = this.txControl.required(() -> {
            return this.em.find(NetworkElementEntity.class, inspectedId);
        });

        assertNotNull(foundInspectedPort);
        assertEquals(inspectedId, foundInspectedPort.getElementId());
        assertNotNull(foundInspectedPort.getInspectionHook());
        assertEquals(hookId, foundInspectedPort.getInspectionHook().getHookId());

        this.redirApi.removeAllInspectionHooks(this.inspected);

        this.txControl.required(() -> {
            NetworkElementEntity tmpNE = this.em.find(NetworkElementEntity.class, inspectedId);
            assertEquals(null, tmpNE);
            return null;
        });
    }

    @Test
    public void testApiRemoveAllInspectionHooks_PortPairRemains() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        String inspectedId = this.inspected.getElementId();
        assertNotNull(inspectedId);

        this.txControl.required(() -> {
            NetworkElementEntity tmp = this.em.find(NetworkElementEntity.class, inspectedId);
            assertEquals(null, tmp);
            return null;
        });

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);
        String elementId = registeredElement.getElementId();

        InspectionPortEntity foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertNotNull(foundInspectionPort);
        assertEquals(elementId, foundInspectionPort.getElementId());

        this.redirApi.removeAllInspectionHooks(this.inspected);

        foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertNotNull(foundInspectionPort);
        assertEquals(elementId, foundInspectionPort.getElementId());
    }

    @Test
    public void testApiRemoveInspectionPort() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, this.ingress, this.egress);

        String inspectedId = this.inspected.getElementId();
        assertNotNull(inspectedId);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertTrue(registeredElement instanceof InspectionPortEntity);
        String elementId = registeredElement.getElementId();

        InspectionPortEntity foundInspectionPort = this.txControl.required(() -> {
            InspectionPortEntity tmpInspectionPort = this.em.find(InspectionPortEntity.class, elementId);
            assertNotNull(tmpInspectionPort);
            return tmpInspectionPort;
        });

        assertEquals(elementId, foundInspectionPort.getElementId());

        // The inspectionPortElement does not have an id. Should still work.
        this.redirApi.removeInspectionPort(inspectionPortElement);

        foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertEquals(null, foundInspectionPort);
    }
}
