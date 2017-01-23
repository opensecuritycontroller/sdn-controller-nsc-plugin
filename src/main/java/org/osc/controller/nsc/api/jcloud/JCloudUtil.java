package org.osc.controller.nsc.api.jcloud;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;

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

        ContextBuilder contextBuilder = ContextBuilder.newBuilder(serviceName)
                .endpoint(endpointURL)
                .credentials(endPoint.getTenant() + ":" + endPoint.getUser(), endPoint.getPassword())
                .overrides(OVERRIDES);

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
