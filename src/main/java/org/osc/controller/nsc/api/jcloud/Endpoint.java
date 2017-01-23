package org.osc.controller.nsc.api.jcloud;

import org.osc.sdk.controller.element.VirtualizationConnectorElement;

import javax.net.ssl.SSLContext;

public class Endpoint {

    private String endPointIP;
    private String tenant;
    private String user;
    private String password;
    private boolean isHttps;
    private SSLContext sslContext;

    public Endpoint(String endPointIP, String tenant, String user, String password, boolean isHttps, SSLContext sslContext) {
        this.endPointIP = endPointIP;
        this.tenant = tenant;
        this.user = user;
        this.password = password;
        this.isHttps = isHttps;
        this.sslContext = sslContext;
    }

    public Endpoint(VirtualizationConnectorElement vc) {
        this.endPointIP = vc.getProviderIpAddress();
        this.tenant = vc.getProviderAdminTenantName();
        this.user = vc.getProviderUsername();
        this.password = vc.getProviderPassword();
        this.isHttps = vc.isProviderHttps();
        this.sslContext = vc.getSslContext();
    }

    public Endpoint(VirtualizationConnectorElement vc, String tenant) {
        this(vc);
        this.tenant = tenant;
    }

    public String getEndPointIP() {
        return this.endPointIP;
    }

    public void setEndPointIP(String endPointIP) {
        this.endPointIP = endPointIP;
    }

    public String getTenant() {
        return this.tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isHttps() {
        return this.isHttps;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }
}
