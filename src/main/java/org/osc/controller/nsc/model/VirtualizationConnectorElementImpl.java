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

/**
 * This implements virtualization connector element to the extent that is required for
 * sdn-controller-nsc-plugin.
 */
public class VirtualizationConnectorElementImpl implements VirtualizationConnectorElement {

    private String name;

    private String providerIpAddress;

    VirtualizationConnectorElementImpl() {
    }

    public VirtualizationConnectorElementImpl(String name, String providerIpAddress) {
        this.name = name;
        this.providerIpAddress = providerIpAddress;
    }

    /**
     * @return the name of virtualization connector
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return the IP address of controller
     */
    @Override
    public String getControllerIpAddress() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public String getControllerUsername() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public String getControllerPassword() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public boolean isControllerHttps() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProviderIpAddress() {
        return this.providerIpAddress;
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public String getProviderUsername() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProviderPassword() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public String getProviderAdminTenantName() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public String getProviderAdminDomainId() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public boolean isProviderHttps() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public Map<String, String> getProviderAttributes() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public SSLContext getSslContext() {
        throw new UnsupportedOperationException();
    }

    /**
     * This is not support as it is not used in sdn-controller-nsc-plugin
     */
    @Override
    public TrustManager[] getTruststoreManager() throws Exception {
        throw new UnsupportedOperationException();
    }
}
