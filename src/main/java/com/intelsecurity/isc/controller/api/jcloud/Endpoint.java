package com.intelsecurity.isc.controller.api.jcloud;

import com.intelsecurity.isc.plugin.controller.element.VirtualizationConnectorElement;
import com.mcafee.vmidc.util.EncryptionUtil;

public class Endpoint {

    private String endPointIP;
    private String tenant;
    private String user;
    private String password;
    private boolean isHttps;

    public Endpoint(String endPointIP, String tenant, String user, String password, boolean isHttps) {
        this.endPointIP = endPointIP;
        this.tenant = tenant;
        this.user = user;
        this.password = password;
        this.isHttps = isHttps;
    }

    public Endpoint(VirtualizationConnectorElement vc) {
        this.endPointIP = vc.getControllerIpAddress();
        this.tenant = vc.getAdminTenantName();
        this.user = vc.getControllerUsername();
        this.password = EncryptionUtil.decrypt(vc.getControllerPassword());
        this.isHttps = vc.isHttps();
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

}
