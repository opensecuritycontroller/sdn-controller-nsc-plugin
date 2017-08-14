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
import static junit.framework.Assert.assertTrue;
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

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osc.controller.nsc.entities.InspectionHookNSCEntity;
import org.osc.controller.nsc.entities.InspectionPortNSCEntity;
import org.osc.controller.nsc.entities.MacAddressNSCEntity;
import org.osc.controller.nsc.entities.NetworkElementNSCEntity;
import org.osc.controller.nsc.entities.PortIpNSCEntity;
import org.osc.controller.nsc.utils.NSCUtils;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
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

    
    
    
    @org.ops4j.pax.exam.Configuration
    public Option[] config() {

    	try {
        return options(

                // Load the current module from its built classes so we get the latest from Eclipse
                bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),
                // And some dependencies
                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),

                mavenBundle("org.ow2.asm", "asm").versionAsInProject(),
                mavenBundle("org.ow2.asm", "asm-commons").versionAsInProject(),
                mavenBundle("org.ow2.asm", "asm-tree").versionAsInProject(),
                mavenBundle("org.apache.aries", "org.apache.aries.util").versionAsInProject(),
                mavenBundle("org.apache.aries.spifly", "org.apache.aries.spifly.dynamic.bundle").versionAsInProject(),
                mavenBundle("com.codahale.metrics", "metrics-core").versionAsInProject(),
                mavenBundle("com.codahale.metrics", "metrics-healthchecks").versionAsInProject(),
                mavenBundle("com.codahale.metrics", "metrics-jvm").versionAsInProject(),
                
                
                mavenBundle("org.osc.api", "sdn-controller-api").versionAsInProject(),
                
                mavenBundle("org.osgi", "org.osgi.core").versionAsInProject(),
//                mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime").versionAsInProject(),
//                mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell").versionAsInProject(),
//                mavenBundle("org.apache.felix", "org.apache.felix.gogo.command").versionAsInProject(),           
                
                mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.container").versionAsInProject(),
                mavenBundle("org.apache.aries.tx-control", "tx-control-service-local").versionAsInProject(),
//                mavenBundle("org.apache.aries.tx-control", "tx-control-api").versionAsInProject(),
                mavenBundle("org.apache.aries.tx-control", "tx-control-provider-jpa-local").versionAsInProject(),
                mavenBundle("com.h2database", "h2").versionAsInProject(),

                // Hibernate

                systemPackage("javax.xml.stream;version=1.0"),
                systemPackage("javax.xml.stream.events;version=1.0"),
                systemPackage("javax.xml.stream.util;version=1.0"),
                systemPackage("javax.transaction;version=1.1"),
                systemPackage("javax.transaction.xa;version=1.1"),
//                bootClasspathLibrary(mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec", "1.1.1")).beforeFramework(),

                // Hibernate bundles and their dependencies (JPA API is available from the tx-control)
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
//                CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),
                bootClasspathLibrary(mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec", "1.1.1")).beforeFramework(),
                junitBundles()
                
            );
    	} catch (Throwable t) {
    		
    		System.err.println(t.getClass().getName() + ":\n" +  t.getMessage());
    		t.printStackTrace(System.err);
    		throw t;
    	}
    }

    @Before
    public void setup() {
    	    	
    	ServiceReference<DataSourceFactory> dsRef = context.getServiceReference(DataSourceFactory.class);
    	this.jdbcFactory = this.context.getService(dsRef);
    	
    	ServiceReference<EntityManagerFactoryBuilder> emRef = context.getServiceReference(EntityManagerFactoryBuilder.class);
    	this.builder = this.context.getService(emRef);
    	
    	ServiceReference<TransactionControl> txcRef = context.getServiceReference(TransactionControl.class);
    	txControl = this.context.getService(txcRef);

    	ServiceReference<JPAEntityManagerProviderFactory> jpaRef = context.getServiceReference(JPAEntityManagerProviderFactory.class);
    	resourceFactory = this.context.getService(jpaRef);
    	
    	assertNotNull(this.jdbcFactory);
    	assertNotNull(this.builder);
    	assertNotNull(this.txControl);
    	assertNotNull(this.resourceFactory);
    	
    	Properties props = new Properties();

    	props.setProperty(JDBC_URL, "jdbc:h2:./nscPlugin_OSGiIntegrationTest");        
    	props.setProperty(JDBC_USER, "admin");
    	props.setProperty(JDBC_PASSWORD, "admin123");

    	DataSource ds = null;
    	try {
    		ds = this.jdbcFactory.createDataSource(props);
    	} catch (SQLException e) {
    		Assert.fail(e.getClass() + " : " + e.getMessage());
    	}

    	
    	this.em = this.resourceFactory.getProviderFor(this.builder,
    			singletonMap("javax.persistence.nonJtaDataSource", (Object)ds), null)
    			.getResource(this.txControl);

    	assertNotNull(em);

    }

//    @Test
    public void testRegistered() throws InterruptedException {
//        SdnControllerApi objectA = api.getService();
//        SdnControllerApi objectB = api.getService();
//        assertSame(objectA, objectB);
    }

    /**
     * This test doesn't really validate much, it would be better if
     * we could start a simple local server to connect to...
     * @throws Exception
     */
//    @Test
    public void testConnect() throws Exception {

        api.getStatus(new VirtualizationConnectorElement() {

            @Override
            public boolean isProviderHttps() {
                return false;
            }

            @Override
            public boolean isControllerHttps() {
                return false;
            }

            @Override
            public String getProviderUsername() {
                return "user";
            }

            @Override
            public String getProviderPassword() {
                return "password";
            }

            @Override
            public String getProviderIpAddress() {
                return "127.0.0.1";
            }

            @Override
            public Map<String, String> getProviderAttributes() {
                return new HashMap<>();
            }

            @Override
            public String getProviderAdminTenantName() {
                return "bar";
            }

            @Override
            public String getProviderAdminDomainId() {
                return "default";
            }

            @Override
            public String getName() {
                return "baz";
            }

            @Override
            public String getControllerUsername() {
                return "fizz";
            }

            @Override
            public String getControllerPassword() {
                return "buzz";
            }

            @Override
            public String getControllerIpAddress() {
                return "127.0.0.1";
            }

            @Override
            public SSLContext getSslContext() {
                try {
                    return SSLContext.getDefault();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public TrustManager[] getTruststoreManager() throws Exception {
                return new TrustManager[0];
            }

			@Override
			public String getProviderAdminDomainId() {
				return "default";
			}
        }, "foo");
    }

    
    @Test
    public void testNetworkElementWithTwoPortsAndMacAddresses() throws Exception {
    	SdnControllerApi service = api;
    	
    	final InspectionHookNSCEntity inspectionHook = new InspectionHookNSCEntity();
    	
    	InspectionPortNSCEntity inspectionPort = new InspectionPortNSCEntity();
    	
    	NetworkElementNSCEntity ingress = new NetworkElementNSCEntity();
    	NetworkElementNSCEntity egress = new NetworkElementNSCEntity();
    	NetworkElementNSCEntity inspected = new NetworkElementNSCEntity();
    	
    	MacAddressNSCEntity iMac1 = new MacAddressNSCEntity();
    	MacAddressNSCEntity iMac2 = new MacAddressNSCEntity();
    	MacAddressNSCEntity eMac1 = new MacAddressNSCEntity();
    	MacAddressNSCEntity eMac2 = new MacAddressNSCEntity();
    	MacAddressNSCEntity inspMac = new MacAddressNSCEntity();
    	
    	
    	PortIpNSCEntity iPort1 = new PortIpNSCEntity();
    	PortIpNSCEntity iPort2 = new PortIpNSCEntity();
    	PortIpNSCEntity ePort1 = new PortIpNSCEntity();
    	PortIpNSCEntity ePort2 = new PortIpNSCEntity();
    	
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
    	
    	
    	
    	
    	InspectionHookNSCEntity inspHookEntity = txControl.required(() -> { 
    		
    		em.persist(inspectionHook);
    		
    		
    		return inspectionHook; 
		});
    	

    	assertNotNull(inspectionPort.getId());
    	
    	List<MacAddressNSCEntity> lsMacs;
    	
    	lsMacs = txControl.requiresNew(() -> { 
        	CriteriaBuilder cb = this.em.getCriteriaBuilder();
        	
            CriteriaQuery<MacAddressNSCEntity> query = cb.createQuery(MacAddressNSCEntity.class);
            Root<MacAddressNSCEntity> from = query.from(MacAddressNSCEntity.class);
            query = query.select(from).distinct(true);            
            return this.em.createQuery(query).getResultList();
    		
		});

    	assertEquals(5, lsMacs.size());

    	List<PortIpNSCEntity> lsPorts;
    	
    	lsPorts = txControl.requiresNew(() -> { 
        	CriteriaBuilder cb = this.em.getCriteriaBuilder();
        	
            CriteriaQuery<PortIpNSCEntity> query = cb.createQuery(PortIpNSCEntity.class);
            Root<PortIpNSCEntity> from = query.from(PortIpNSCEntity.class);
            query = query.select(from).distinct(true);            
            return this.em.createQuery(query).getResultList();
    		
		});

    	assertEquals(4, lsPorts.size());
    	 
    	InspectionHookNSCEntity persistedHook = txControl.required(() -> { 
			assertTrue("EM is closed!", em.isOpen());
			
			InspectionHookNSCEntity ph = em.find(InspectionHookNSCEntity.class, 
						   						 inspectionHook.getHookId());
			InspectionPortNSCEntity iprt = em.find(InspectionPortNSCEntity.class, inspectionPort.getId());
						
			assertNotNull(inspectionPort.getInspectionHook());
			assertEquals(inspectionPort.getId(), iprt.getId());
			return ph;
		});
    	
    	
    	assertNotNull(inspectionHook.getHookId());
    	assertEquals(inspectionHook.getHookId(), persistedHook.getHookId());
    	
    	InspectionPortNSCEntity persistedPort = persistedHook.getInspectionPort();
    	
    	assertNotNull(persistedPort);
    	assertEquals(inspectionHook.getInspectionPort().getId(), persistedPort.getId());
    	
    	
    	// TODO : Separate!
    	NSCUtils utils = new NSCUtils(em, txControl);

    	NetworkElement ingressElement = NSCUtils.makeNetworkElement(ingress);
    	NetworkElement egressElement = NSCUtils.makeNetworkElement(egress);
    	InspectionPortNSCEntity foundPort = utils.inspPortByNetworkElements(ingressElement, egressElement);
    	
    	assertNotNull(foundPort);
    	assertEquals(inspectionPort.getId(), foundPort.getId());

    }
}
