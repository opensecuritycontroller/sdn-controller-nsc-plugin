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
package org.osc.controller.nsc.api.jcloud;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.v2_0.KeystoneApiMetadata;
import org.jclouds.openstack.neutron.v2.NeutronApiMetadata;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class JCloudUtil {

    // TODO: Future. Openstack. Externalize the timeout values
    private static final Properties OVERRIDES = new Properties();

    private static final int DEFAULT_READ_TIMEOUT = 60 * 1000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5 * 1000;

    static {
        OVERRIDES.setProperty(Constants.PROPERTY_REQUEST_TIMEOUT, String.valueOf(DEFAULT_READ_TIMEOUT));
        OVERRIDES.setProperty(Constants.PROPERTY_CONNECTION_TIMEOUT, String.valueOf(DEFAULT_CONNECTION_TIMEOUT));
        OVERRIDES.setProperty(Constants.PROPERTY_LOGGER_WIRE_LOG_SENSITIVE_INFO, String.valueOf(false));
        OVERRIDES.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, String.valueOf(true));
        OVERRIDES.setProperty(Constants.PROPERTY_USER_THREADS, String.valueOf(10));
    }

    public static <A extends Closeable> A buildApi(Class<A> api, String serviceName, Endpoint endPoint) {

        String endpointURL;
        try {
            endpointURL = prepareEndpointURL(endPoint);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        ContextBuilder contextBuilder = null;
        if("openstack-neutron".equals(serviceName)) {
            contextBuilder = ContextBuilder.newBuilder(new NeutronApiMetadata())
                    .endpoint(endpointURL)
                    .credentials(endPoint.getTenant() + ":" + endPoint.getUser(), endPoint.getPassword())
                    .overrides(OVERRIDES);
        }
        if("openstack-keystone".equals(serviceName)) {
            contextBuilder = ContextBuilder.newBuilder(new KeystoneApiMetadata())
                    .endpoint(endpointURL)
                    .credentials(endPoint.getTenant() + ":" + endPoint.getUser(), endPoint.getPassword())
                    .overrides(OVERRIDES);
        }

        if (endPoint.isHttps()) {
            contextBuilder = configureSSLContext(contextBuilder, endPoint.getSslContext());
        }

        return contextBuilder.buildApi(api);
    }

    private static String prepareEndpointURL(Endpoint endPoint) throws URISyntaxException, MalformedURLException {
        String schema = endPoint.isHttps() ? "https" : "http";
        URI uri = new URI(schema, null, endPoint.getEndPointIP(), 5000, "/v2.0", null, null);
        return uri.toURL().toString();
    }

    private static ContextBuilder configureSSLContext(ContextBuilder contextBuilder,final SSLContext sslContext) {
        Module customSSLModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<Supplier<SSLContext>>() {
                }).toInstance(() -> sslContext);
            }
        };
        contextBuilder.modules(ImmutableSet.of(customSSLModule));
        return contextBuilder;
    }
}
