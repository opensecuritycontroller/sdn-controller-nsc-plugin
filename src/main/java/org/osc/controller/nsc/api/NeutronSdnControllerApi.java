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
package org.osc.controller.nsc.api;

import static org.osc.sdk.controller.Constants.*;

import java.util.HashMap;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.osc.controller.nsc.api.jcloud.Endpoint;
import org.osc.sdk.controller.FlowInfo;
import org.osc.sdk.controller.FlowPortInfo;
import org.osc.sdk.controller.Status;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.api.SdnRedirectionApi;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osgi.service.component.annotations.Component;

@Component(configurationPid = "com.intel.nsc.SdnController",
        property = {
                PLUGIN_NAME + "=NSC",
                SUPPORT_OFFBOX_REDIRECTION + ":Boolean=false",
                SUPPORT_SFC + ":Boolean=false",
                SUPPORT_FAILURE_POLICY + ":Boolean=false",
                USE_PROVIDER_CREDS + ":Boolean=true",
                QUERY_PORT_INFO + ":Boolean=false",
                SUPPORT_PORT_GROUP + ":Boolean=false" })
public class NeutronSdnControllerApi implements SdnControllerApi {

    Logger log = Logger.getLogger(NeutronSdnControllerApi.class);

    private final static String VERSION = "0.1";
    private final static String NAME = "NSC";

    public NeutronSdnControllerApi() {
    }

    @Override
    public Status getStatus(VirtualizationConnectorElement vc, String region) throws Exception {
        // TODO: Future. We should not rely on list ports instead we should send a valid status
        // based on is SDN controller ready to serve
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(vc))) {
            neutronApi.test();
        }
        return new Status(NAME, VERSION, true);
    }

    @Override
    public SdnRedirectionApi createRedirectionApi(VirtualizationConnectorElement vc, String region) {
        return new NeutronSdnRedirectionApi(vc, region);
    }

    @Override
    public HashMap<String, FlowPortInfo> queryPortInfo(VirtualizationConnectorElement vc, String region,
            HashMap<String, FlowInfo> portsQuery) throws Exception {
        throw new NotImplementedException("NSC SDN Controller does not support flow based query");
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }

}
