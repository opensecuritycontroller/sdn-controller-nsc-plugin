package com.intelsecurity.isc.controller.api.jcloud;

import java.io.Closeable;
import java.util.Properties;

import org.jclouds.Constants;
import org.jclouds.ContextBuilder;

public class JCloudUtil {

    // TODO: Future. Openstack. Externalize the timeout values
    private static final Properties OVERRIDES = new Properties();

    private static final int DEFAULT_READ_TIMEOUT = 60 * 1000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5 * 1000;

    static {
        OVERRIDES.setProperty(Constants.PROPERTY_REQUEST_TIMEOUT, DEFAULT_READ_TIMEOUT + "");
        OVERRIDES.setProperty(Constants.PROPERTY_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT + "");
        OVERRIDES.setProperty(Constants.PROPERTY_LOGGER_WIRE_LOG_SENSITIVE_INFO, false + "");
        OVERRIDES.setProperty(Constants.PROPERTY_USER_THREADS, 10 + "");
    }

    public static <A extends Closeable> A buildApi(Class<A> api, String serviceName, Endpoint endPoint) {
        String httpPrefix = (endPoint.isHttps() ? "https" : "http") + "://";
        return ContextBuilder.newBuilder(serviceName).endpoint(httpPrefix + endPoint.getEndPointIP() + ":5000/v2.0")
                .credentials(endPoint.getTenant() + ":" + endPoint.getUser(), endPoint.getPassword())
                .overrides(OVERRIDES).buildApi(api);
    }
}
