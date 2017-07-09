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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;

import javax.net.ssl.SSLContext;

public class Endpoint {

    private String endPointIP;
    private String domainId;
    private String tenant;
    private String user;
    private String password;
    private boolean isHttps;
    private SSLContext sslContext;

    public Endpoint(String endPointIP, String domainId, String tenant, String user, String password, boolean isHttps, SSLContext sslContext) {
        this.endPointIP = endPointIP;
        this.tenant = tenant;
        this.domainId = domainId;
        this.user = user;
        this.password = password;
        this.isHttps = isHttps;
        this.sslContext = sslContext;
    }

    public Endpoint(VirtualizationConnectorElement vc) {
        this.endPointIP = vc.getProviderIpAddress();
        this.domainId = vc.getProviderAdminDomainId();
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

    public String getDomainId() {
        return this.domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Endpoint endpoint = (Endpoint) o;

        return new EqualsBuilder()
                .append(this.isHttps, endpoint.isHttps)
                .append(this.endPointIP, endpoint.endPointIP)
                .append(this.domainId, endpoint.domainId)
                .append(this.tenant, endpoint.tenant)
                .append(this.user, endpoint.user)
                .append(this.password, endpoint.password)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.endPointIP)
                .append(this.domainId)
                .append(this.tenant)
                .append(this.user)
                .append(this.password)
                .append(this.isHttps)
                .toHashCode();
    }
}
