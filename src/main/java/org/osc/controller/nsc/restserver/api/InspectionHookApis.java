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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.model.VirtualizationConnectorElementImpl;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = InspectionHookApis.class)
@Path("/controller/{controllerId}/inspectionHooks")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionHookApis {

    private static Logger LOG = LoggerFactory.getLogger(InspectionHookApis.class);

    @Reference
    private SdnControllerApi api;

    @POST
    public String createInspectionHook(@PathParam("controllerId") String controllerId, InspectionHookEntity entity)
            throws Exception {

        LOG.info("Creating inspection hook with inspection port id {}", entity.getInspectionPort().getElementId());

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfNullId(controllerId);

        return sdnApi.installInspectionHook(entity.getInspectedPort(), entity.getInspectionPort(), entity.getTag(),
                entity.getEncType(), entity.getOrder(), entity.getFailurePolicyType());
    }

    @Path("/{inspectionHookId}")
    @PUT
    public InspectionHookEntity updateInspectionHook(@PathParam("controllerId") String controllerId,
            @PathParam("inspectionHookId") String inspectionHookId, InspectionHookEntity entity) throws Exception {

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfNullId(controllerId);

        sdnApi.throwExceptionIfIdMismatch(entity.getHookId(), inspectionHookId, "InspectionHook");

        sdnApi.updateInspectionHook(entity);
        return entity;
    }

    @Path("/{inspectionHookId}")
    @DELETE
    public void deleteInspectionHook(@PathParam("controllerId") String controllerId,
            @PathParam("inspectionHookId") String inspectionHookId)
                    throws Exception {
        LOG.info("Deleting the inspection hook element for id {} ", inspectionHookId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfNullId(controllerId);

        sdnApi.removeInspectionHook(inspectionHookId);
    }

    @GET
    public List<String> getInspectionHookIds(@PathParam("controllerId") String controllerId) throws Exception {

        LOG.info("Listing inspection hook ids'");

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfNullId(controllerId);

        return sdnApi.getInspectionHooksIds();
    }

    @Path("/{inspectionHookId}")
    @GET
    public InspectionHookEntity getInspectionHook(@PathParam("controllerId") String controllerId,
            @PathParam("inspectionHookId") String inspectionHookId)
                    throws Exception {

        LOG.info("Getting the inspection hook element for id {} ", inspectionHookId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfNullId(controllerId);

        InspectionHookEntity inspectionHook = (InspectionHookEntity) sdnApi.getInspectionHook(inspectionHookId);

        inspectionHook.setInspectedPort(null);
        inspectionHook.setInspectionPort(null);

        return inspectionHook;
    }
}
