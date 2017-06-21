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
package org.osc.controller.nsc.api.openstack4j;

import org.openstack4j.model.identity.v3.Token;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;

import javax.net.ssl.SSLContext;

public class Endpoint {

    private String endPointIP;
    private String domain;
    private String tenant;
    private String user;
    private String password;
    private boolean isHttps;
    private SSLContext sslContext;
    private Token token;

    public Endpoint(String endPointIP, String domain, String tenant, String user, String password, boolean isHttps, SSLContext sslContext) {
        this.endPointIP = endPointIP;
        this.tenant = tenant;
        this.domain = domain;
        this.user = user;
        this.password = password;
        this.isHttps = isHttps;
        this.sslContext = sslContext;
    }

    public Endpoint(VirtualizationConnectorElement vc) {
        this.endPointIP = vc.getProviderIpAddress();
        this.domain = vc.getProviderAdminDomainId();
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

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Token getToken() {
        return this.token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
