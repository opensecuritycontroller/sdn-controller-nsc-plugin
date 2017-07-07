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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osc.controller.nsc.entities.MacAddressNSCEntity;
import org.osc.controller.nsc.entities.NetworkElementNSCEntity;
import org.osc.controller.nsc.entities.PortIpNSCEntity;
import org.osc.controller.nsc.model.InspectionHook;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;
import org.osgi.util.tracker.ServiceTracker;

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

    @Inject
    BundleContext context;
    
    @Inject
    SdnControllerApi api;

//    @Reference(target="(osgi.local.enabled=true)")
//    TransactionControl txControl;

//    @Reference(target="(osgi.unit.name=nsc-mgr)")
//    EntityManagerFactoryBuilder builder;

//    @Reference(target="(osgi.jdbc.driver.class=org.h2.Driver)")
//    DataSourceFactory jdbcFactory;

//    @Reference(target="(osgi.local.enabled=true)")
//    JPAEntityManagerProviderFactory resourceFactory;
    

//    private ServiceTracker<SdnControllerApi, SdnControllerApi> tracker;

    @org.ops4j.pax.exam.Configuration
    public Option[] config() {

        return options(
                // Load the current module from its built classes so we get the latest from Eclipse
                bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),

                // And some dependencies
                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),

                mavenBundle("org.osc.api", "security-mgr-api").versionAsInProject(),
                mavenBundle("javax.websocket", "javax.websocket-api").versionAsInProject(),
                mavenBundle("log4j", "log4j").versionAsInProject(),
                mavenBundle("org.apache.aries.jpa", "org.apache.aries.jpa.container").versionAsInProject(),
                mavenBundle("org.apache.aries.tx-control", "tx-control-service-local").versionAsInProject(),
                mavenBundle("org.apache.aries.tx-control", "tx-control-provider-jpa-local").versionAsInProject(),
                mavenBundle("com.h2database", "h2").versionAsInProject(),

                // Hibernate

                systemPackage("javax.xml.stream;version=1.0"),
                systemPackage("javax.xml.stream.events;version=1.0"),
                systemPackage("javax.xml.stream.util;version=1.0"),
                systemPackage("javax.transaction;version=1.1"),
                systemPackage("javax.transaction.xa;version=1.1"),
                bootClasspathLibrary(mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec", "1.1.1")).beforeFramework(),

                // Hibernate bundles and their dependencies (JPA API is available from the tx-control)
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.antlr", "2.7.7_5"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j", "1.6.1_5"),
                mavenBundle("org.javassist", "javassist", "3.18.1-GA"),
                mavenBundle("org.jboss.logging", "jboss-logging", "3.3.0.Final"),
                mavenBundle("org.jboss", "jandex", "2.0.0.Final"),
                mavenBundle("org.hibernate.common", "hibernate-commons-annotations", "5.0.1.Final"),
                mavenBundle("org.hibernate", "hibernate-core", "5.0.9.Final"),
                mavenBundle("org.hibernate", "hibernate-osgi", "5.0.9.Final"),
                mavenBundle("org.hibernate", "hibernate-entitymanager", "5.0.9.Final"),

                // Just needed for the test so we can configure the client to point at the local test server
                //                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.10"),

                // Uncomment this line to allow remote debugging
                // CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),

                systemTimeout(5000),
                junitBundles()

                );
    }

    @Before
    public void setup() {
//        this.tracker = new ServiceTracker<>(this.context, SdnControllerApi.class, null);
//        this.tracker.open();
    }


//    @Test
    public void testRegistered() throws InterruptedException {
//        SdnControllerApi service = this.tracker.waitForService(5000);
    	SdnControllerApi service = api;
        assertNotNull(service);

//        ServiceObjects<SdnControllerApi> so = this.context.getServiceObjects(this.tracker.getServiceReference());
//        SdnControllerApi objectA = so.getService();
//        SdnControllerApi objectB = so.getService();
//        assertSame(objectA, objectB);
    }

    /**
     * This test doesn't really validate much, it would be better if
     * we could start a simple local server to connect to...
     * @throws Exception
     */
//    @Test
    public void testConnect() throws Exception {
//        SdnControllerApi service = this.tracker.waitForService(5000);
    	SdnControllerApi service = api;
        assertNotNull(service);

//        ServiceObjects<SdnControllerApi> so = this.context.getServiceObjects(this.tracker.getServiceReference());

//        SdnControllerApi object = so.getService();
        SdnControllerApi object = api;

        object.getStatus(new VirtualizationConnectorElement() {

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
//        assertNotNull(service);
//        assertNotNull(jdbcFactory);
        
//        ServiceObjects<SdnControllerApi> so = this.context.getServiceObjects(this.tracker.getServiceReference());

//        new InspectionHook();
//        NetworkElementNSCEntity nscEntity = new NetworkElementNSCEntity();
//        MacAddrNSCEntity ma1 = new MacAddrNSCEntity();
//        ma1.setMacAddress("aaa8434878ffae834989");
//        MacAddrNSCEntity ma2 = new MacAddrNSCEntity();
//        ma1.setMacAddress("bbb8434878ffae834989");
//        PortIpNSCEntity pip1 = new PortIpNSCEntity();
//        pip1.setPortIp("10.2.3.4");
//        PortIpNSCEntity pip2 = new PortIpNSCEntity();
//        pip1.setPortIp("10.5.4.3");
        
        VirtualizationConnectorElement vce = new VirtualizationConnectorElement() {

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
        };
    }
}
