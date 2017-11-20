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
import org.osc.controller.nsc.entities.PortEntity;
import org.osc.controller.nsc.model.VirtualizationConnectorElementImpl;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = PortApis.class)
@Path("/controller/{controllerId}/portElements")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class PortApis {

    private static Logger LOG = LoggerFactory.getLogger(PortApis.class);

    @Reference
    private SdnControllerApi api;

    @Path("/{elementId}")
    @POST
    public String createPort(@PathParam("controllerId") String controllerId,
            @PathParam("elementId") String elementId, PortEntity entity) throws Exception {

        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create port element with no port entities");
        }

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        LOG.info("Creating port element with elementId  {}", elementId);

        sdnApi.throwExceptionIfIdMismatch(entity.getElementId(), elementId, "Port");

        return sdnApi.registerPort(entity).getElementId();
    }

    @Path("/{elementId}")
    @PUT
    public PortEntity updatePort(@PathParam("controllerId") String controllerId,
            @PathParam("elementId") String elementId, PortEntity entity) throws Exception {
        LOG.info("Updating the port element id {} ", elementId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfIdMismatch(entity.getElementId(), elementId, "Port");

        return sdnApi.updatePort(entity);
    }

    @Path("/{elementId}")
    @DELETE
    public void deletePort(@PathParam("controllerId") String controllerId,
            @PathParam("elementId") String elementId)
                    throws Exception {
        LOG.info("Deleting the port element for id {} ", elementId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.deletePort(elementId);
    }

    @GET
    public List<String> getPortIds(@PathParam("controllerId") String controllerId) throws Exception {
        LOG.info("Listing port elements ids'");

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        return sdnApi.getPortIds();
    }

    @Path("/{elementId}")
    @GET
    public PortEntity getPort(@PathParam("controllerId") String controllerId,
            @PathParam("elementId") String elementId)
                    throws Exception {
        LOG.info("Getting the port for id {} ", elementId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        PortEntity portElement = sdnApi.getPort(elementId);
        portElement.setInspectionHook(null);
        return portElement;
    }
}
