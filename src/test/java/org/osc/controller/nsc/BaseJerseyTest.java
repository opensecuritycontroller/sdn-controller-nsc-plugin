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
package org.osc.controller.nsc;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.sdk.controller.api.SdnControllerApi;

public class BaseJerseyTest extends JerseyTest {

    protected final String authorizationHeader = "Authorization";

    protected final String authorizationCreds = "Basic YWRtaW46YWRtaW4xMjM=";

    public BaseJerseyTest() {
    }

    protected void baseTestConfiguration() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
    }

    protected ResourceConfig getBaseResourceConfiguration() {
        return new ResourceConfig()
                .register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class)
                .register(SdnControllerApi.class)
                .register(SampleSdnRedirectionApi.class);
    }

    protected void callRealMethods(SampleSdnRedirectionApi sdnApi) throws Exception {
        doCallRealMethod().when(sdnApi).throwExceptionIfNullId(any());
        doCallRealMethod().when(sdnApi).throwExceptionIfIdMismatch(any(), any(), any());
    }
}
