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
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.model.VirtualizationConnectorElementImpl;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = InspectionPortApis.class)
@Path("/controller/{controllerId}/inspectionPorts")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionPortApis {

    private static Logger LOG = LoggerFactory.getLogger(InspectionPortApis.class);

    @Reference
    private SdnControllerApi api;

    @POST
    public String createInspectionPort(@PathParam("controllerId") String controllerId, InspectionPortEntity entity)
            throws Exception {

        LOG.info("Creating inspection port for (ingress id {} ; egress id {})", entity.getIngressPort().getElementId(),
                entity.getEgressPort().getElementId());

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        InspectionPortEntity inspectionPort = (InspectionPortEntity) sdnApi.registerInspectionPort(entity);

        return inspectionPort == null ? null : inspectionPort.getElementId();
    }

    @Path("/{inspectionPortId}")
    @PUT
    public InspectionPortEntity updateInspectionPort(@PathParam("controllerId") String controllerId,
            @PathParam("inspectionPortId") String inspectionPortId, InspectionPortEntity entity) throws Exception {

        LOG.info("Updating the inspection port element id {} ", inspectionPortId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.throwExceptionIfIdMismatch(entity.getElementId(), inspectionPortId, "InspectionPort");

        return (InspectionPortEntity) sdnApi.updateInspectionPort(entity);
    }

    @Path("/{inspectionPortId}")
    @DELETE
    public void deleteInspectionPort(@PathParam("controllerId") String controllerId,
            @PathParam("inspectionPortId") String inspectionPortId)
                    throws Exception {

        LOG.info("Deleting the inspection port element for id {} ", inspectionPortId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        sdnApi.removeInspectionPort(new InspectionPortEntity(inspectionPortId, null, null));
    }

    @GET
    public List<String> getInspectionPortIds(@PathParam("controllerId") String controllerId) throws Exception {

        LOG.info("Listing inspection port ids'");

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        return sdnApi.getInspectionPortsIds();
    }

    @Path("/{inspectionPortId}")
    @GET
    public InspectionPortEntity getInspectionPort(@PathParam("controllerId") String controllerId,
            @PathParam("inspectionPortId") String inspectionPortId) throws Exception {

        LOG.info("Getting the inspection port element for id {} ", inspectionPortId);

        SampleSdnRedirectionApi sdnApi = ((SampleSdnRedirectionApi) this.api
                .createRedirectionApi(new VirtualizationConnectorElementImpl("Sample", controllerId), "TEST"));

        InspectionPortEntity inspectionPort = (InspectionPortEntity) sdnApi
                .getInspectionPort(new InspectionPortEntity(inspectionPortId, null, null));

        if (inspectionPort.getInspectionHooks() != null) {
            inspectionPort.getInspectionHooks().clear();
        }

        return inspectionPort;
    }
}
