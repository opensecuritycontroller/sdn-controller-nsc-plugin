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
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGiIntegrationTest {

    @Inject
    BundleContext context;

    private ServiceTracker<SdnControllerApi, SdnControllerApi> tracker;

    @org.ops4j.pax.exam.Configuration
    public Option[] config() {

        return options(
                // Load the current module from its built classes so we get the latest from Eclipse
                bundle("reference:file:" + PathUtils.getBaseDir() + "/target/classes/"),

                // And some dependencies
                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),

                mavenBundle("org.osc.api", "sdn-controller-api").versionAsInProject(),
                mavenBundle("org.apache.jclouds", "jclouds-core").versionAsInProject(),
                mavenBundle("org.apache.jclouds.labs", "openstack-neutron").versionAsInProject(),
                mavenBundle("org.apache.jclouds.api", "openstack-keystone").versionAsInProject(),
                mavenBundle("com.google.code.gson", "gson").versionAsInProject(),
                mavenBundle("com.google.guava", "guava").versionAsInProject(),
                mavenBundle("com.google.inject", "guice").versionAsInProject(),
                mavenBundle("com.google.inject.extensions", "guice-multibindings").versionAsInProject().noStart(),
                mavenBundle("com.google.inject.extensions", "guice-assistedinject").versionAsInProject().noStart(),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.aopalliance").versionAsInProject(),
                mavenBundle("log4j", "log4j").versionAsInProject(),
                mavenBundle("org.apache.directory.studio", "org.apache.commons.lang").versionAsInProject(),
                mavenBundle("javax.ws.rs", "jsr311-api").versionAsInProject(),

                // Uncomment this line to allow remote debugging
                // CoreOptions.vmOption("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"),

                junitBundles()
                );
    }

    @Before
    public void setup() {
        this.tracker = new ServiceTracker<>(this.context, SdnControllerApi.class, null);
        this.tracker.open();
    }

    @Test
    public void testRegistered() throws InterruptedException {
        SdnControllerApi service = this.tracker.waitForService(5000);
        assertNotNull(service);

        ServiceObjects<SdnControllerApi> so = this.context.getServiceObjects(this.tracker.getServiceReference());
        SdnControllerApi objectA = so.getService();
        SdnControllerApi objectB = so.getService();
        assertSame(objectA, objectB);
    }

    /**
     * This test doesn't really validate much, it would be better if
     * we could start a simple local server to connect to...
     * @throws Exception
     */
    @Test(expected=org.jclouds.http.HttpResponseException.class)
    public void testConnect() throws Exception {
        SdnControllerApi service = this.tracker.waitForService(5000);
        assertNotNull(service);

        ServiceObjects<SdnControllerApi> so = this.context.getServiceObjects(this.tracker.getServiceReference());

        SdnControllerApi object = so.getService();

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
        }, "foo");
    }
}
