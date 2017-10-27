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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServerApiUtils {

    private static Logger LOG = LoggerFactory.getLogger(RestServerApiUtils.class);

    private static ConcurrentHashMap<String, SampleSdnRedirectionApi> sdnApiHashMap = new ConcurrentHashMap<String, SampleSdnRedirectionApi>();

    static void insertSdnApi(String id, SampleSdnRedirectionApi value) {
        sdnApiHashMap.putIfAbsent(id, value);
    }

    static SampleSdnRedirectionApi getSdnApi(String id) {
        return sdnApiHashMap.get(id);
    }

    static void validateIdMatches(String entityId, String id, String objName) throws Exception {
        if (!id.equals(entityId)) {
            throw new IllegalArgumentException(
                    String.format("The ID %s specified in the '%s' data does not match the id specified in the URL",
                            entityId, objName));
        }
    }

    static void flushSdnApiCache() {
        sdnApiHashMap.clear();
    }

    static void throwExceptionIfNullId(String id) {
        if (id == null) {
            String msg = "null passed for controller id argument!";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
