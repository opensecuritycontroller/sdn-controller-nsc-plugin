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
package org.osc.controller.nsc.model;

import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.osc.sdk.controller.element.VirtualizationConnectorElement;

public class VirtualizationConnectorElementImpl implements VirtualizationConnectorElement {

    private String name;

    private String providerIpAddress;

    VirtualizationConnectorElementImpl() {
    }

    public VirtualizationConnectorElementImpl(String name, String providerIpAddress) {
        this.name = name;
        this.providerIpAddress = providerIpAddress;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getControllerIpAddress() {
        return null;
    }

    @Override
    public String getControllerUsername() {
        return null;
    }

    @Override
    public String getControllerPassword() {
        return null;
    }

    @Override
    public boolean isControllerHttps() {
        return Boolean.TRUE;
    }

    @Override
    public String getProviderIpAddress() {
        return this.providerIpAddress;
    }

    @Override
    public String getProviderUsername() {
        return null;
    }

    @Override
    public String getProviderPassword() {
        return null;
    }

    @Override
    public String getProviderAdminTenantName() {
        return null;
    }

    @Override
    public String getProviderAdminDomainId() {
        return null;
    }

    @Override
    public boolean isProviderHttps() {
        return Boolean.TRUE;
    }

    @Override
    public Map<String, String> getProviderAttributes() {
        return null;
    }

    @Override
    public SSLContext getSslContext() {
        return null;
    }

    @Override
    public TrustManager[] getTruststoreManager() throws Exception {
        return null;
    }
}
