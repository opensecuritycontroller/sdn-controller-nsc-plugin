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
package org.osc.controller.nsc.restserver.api;

import java.util.concurrent.ConcurrentHashMap;

import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.model.VirtualizationConnectorElementImpl;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;

public class RestServerApiUtils {

    private static ConcurrentHashMap<String, SampleSdnRedirectionApi> sdnApiHashMap = new ConcurrentHashMap<String, SampleSdnRedirectionApi>();

    static void insertSdnApi(String id, SampleSdnRedirectionApi value) {
        sdnApiHashMap.putIfAbsent(id, value);
    }

    static SampleSdnRedirectionApi getSdnApi(String id) {
        return sdnApiHashMap.get(id);
    }

    static SampleSdnRedirectionApi getSdnRedirectionApi(String id, SdnControllerApi api) throws Exception {

        if (id == null) {
            return null;
        }

        SampleSdnRedirectionApi sdnApi = RestServerApiUtils.getSdnApi(id);
        if (sdnApi != null) {
            return sdnApi;
        }

        VirtualizationConnectorElement vc = new VirtualizationConnectorElementImpl("Sample", id);
        sdnApi = (SampleSdnRedirectionApi) api.createRedirectionApi(vc, "TEST");

        RestServerApiUtils.insertSdnApi(id, sdnApi);

        return sdnApi;
    }
}
