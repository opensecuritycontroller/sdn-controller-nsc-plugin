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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.PortEntity;
import org.osc.controller.nsc.restserver.api.InspectionHookApis;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.api.SdnRedirectionApi;
import org.osc.sdk.controller.element.InspectionHookElement;

public class InspectionHookApisTest extends BaseJerseyTest {

    private List<String> expectedResponseList;

    @Rule
    public OsgiContext context;

    @Override
    protected Application configure() {
        baseTestConfiguration();

        //configure services
        this.context = new OsgiContext();

        SdnControllerApi sdnApi = Mockito.mock(SdnControllerApi.class);
        this.context.registerService(SdnControllerApi.class, sdnApi);

        SampleSdnRedirectionApi sdnRedirApi = Mockito.mock(SampleSdnRedirectionApi.class);
        this.context.registerService(SampleSdnRedirectionApi.class, sdnRedirApi);

        InspectionHookApis service = new InspectionHookApis();
        this.context.registerInjectActivateService(service);

        ResourceConfig application = getBaseResourceConfiguration().register(service);

        //configure responses
        this.expectedResponseList = new ArrayList<String>();
        this.expectedResponseList.add("HookId");

        Mockito.<SdnRedirectionApi> when(sdnApi.createRedirectionApi(any(), any())).thenReturn(sdnRedirApi);

        try {
            Mockito.<List<String>> when(sdnRedirApi.getInspectionHooksIds()).thenReturn(this.expectedResponseList);
            Mockito.<InspectionHookElement> when(sdnRedirApi.getInspectionHook(any()))
                    .thenReturn(createInspectionHookEntity());
            Mockito.<String> when(sdnRedirApi.installInspectionHook(any(), any(), any(), any(), any(), any()))
            .thenReturn("HookId");
            Mockito.doNothing().when(sdnRedirApi).updateInspectionHook(any());
            Mockito.doNothing().when(sdnRedirApi).removeInspectionHook(any());
            super.callRealMethods(sdnRedirApi);
        } catch (Exception ex) {
            Assert.fail(ex.getClass() + " : " + ex.getMessage());
        }

        return application;
    }

    @Test
    public void testExecute_WithGetInspectionHook_ExpectStatusOk() {
        // Assume.
        Response response = null;
        try {

            // Act.
            response = target("controller/1.2.3.0/inspectionHooks/HookId")
                    .request()
                    .header(this.AUTHORIZATION_HEADER, this.AUTHORIZATION_CREDS)
                    .get();

            response.close();

            // Assert.
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void testExecute_WithGetInspectionHookIds_ExpectStatusOk() {
        // Assume.
        Response response = null;
        try {

            // Act.
            response = target("controller/1.2.3.0/inspectionHooks")
                    .request()
                    .header(this.AUTHORIZATION_HEADER, this.AUTHORIZATION_CREDS)
                    .get();

            response.close();

            // Assert.
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void testExecute_WithPostInspectionHook_ExpectStatusOk() {
        // Assume.
        Response response = null;
        try {

            InspectionHookEntity inspectionHook = createInspectionHookEntity();

            Entity<InspectionHookEntity> entity = Entity.entity(inspectionHook, MediaType.APPLICATION_JSON);

            // Act.
            response = target("controller/1.2.3.0/inspectionHooks")
                    .request()
                    .header(this.AUTHORIZATION_HEADER, this.AUTHORIZATION_CREDS)
                    .post(entity);

            response.close();

            // Assert.
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void testExecute_WithUpdateInspectionHook_ExpectStatusOk() {
        // Assume.
        Response response = null;
        try {

            InspectionHookEntity inspectionHook = createInspectionHookEntity();

            Entity<InspectionHookEntity> entity = Entity.entity(inspectionHook, MediaType.APPLICATION_JSON);
            // Act.
            response = target("controller/1.2.3.0/inspectionHooks/HookId")
                    .request()
                    .header(this.AUTHORIZATION_HEADER, this.AUTHORIZATION_CREDS)
                    .put(entity);

            response.close();

            // Assert.
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void testExecute_WithUpdateInspectionHook_ExpectErrorCode() {
        // Assume.
        Response response = null;
        try {

            InspectionHookEntity inspectionHook = createInspectionHookEntity();

            Entity<InspectionHookEntity> entity = Entity.entity(inspectionHook, MediaType.APPLICATION_JSON);

            String badParam = "IdNotMatching";
            // Act.
            response = target("controller/1.2.3.0/inspectionHooks/" + badParam)
                    .request()
                    .header(this.AUTHORIZATION_HEADER, this.AUTHORIZATION_CREDS)
                    .put(entity);

            response.close();

            // Assert.
            assertThat(response.getStatus()).isEqualTo(500);

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void testExecute_WithDeleteInspectionHook_ExpectNoContent() {
        // Assume.
        Response response = null;
        try {

            // Act.
            response = target("controller/1.2.3.0/inspectionHooks/HookId")
                    .request()
                    .header(this.AUTHORIZATION_HEADER, this.AUTHORIZATION_CREDS)
                    .delete();

            response.close();

            // Assert.
            assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private PortEntity createPortEntity() {
        PortEntity port = new PortEntity();
        port.setElementId("ElementId");
        port.setPortIPs(Arrays.asList("10.1.1.1", "10.1.1.2"));
        port.setMacAddresses(Arrays.asList("ff:ff:aa:bb:cc:01", "ff:ff:aa:bb:cc:02"));
        port.setParentId("ParentId");

        return port;
    }

    private InspectionHookEntity createInspectionHookEntity() {
        InspectionHookEntity inspectionHookEntity = new InspectionHookEntity();
        inspectionHookEntity.setHookId("HookId");
        inspectionHookEntity.setInspectionPort(new InspectionPortEntity("InspPortId", null, null));
        inspectionHookEntity.setInspectedPort(createPortEntity());

        return inspectionHookEntity;
    }
}
