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

import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = NetworkElementApis.class)
@Path("/controller/{id}/networkElements")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class NetworkElementApis {

    private static Logger LOG = LoggerFactory.getLogger(NetworkElementApis.class);

    @Reference(target = "(osgi.local.enabled=true)")
    private TransactionControl txControl;

    @Reference(target = "(osgi.unit.name=nsc-mgr)")
    private EntityManagerFactoryBuilder builder;

    @Reference(target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;

    @Reference(target = "(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;

    @Reference
    private SdnControllerApi api;

    @Path("/{elementId}")
    @POST
    public String createNetworkElement(@PathParam("id") String id, @PathParam("elementId") String elementId,
            NetworkElementEntity entity)
                    throws Exception {
        // TODO:SUDHIR - Rename Rest API's to Port instead of NetworkElement, for eg: createPort etc.
        // TODO:SUDHIR - Rename NetworkElementEntity to PortEntity.

        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create network element with no network entities");
        }

        LOG.info(String.format("Creating network element with elementId  %s", elementId));

        entity.setElementId(elementId);

        return RestServerApiUtils.getSdnRedirectionApi(id, this.api).registerPort(entity).getElementId();
    }

    @Path("/{elementId}")
    @PUT
    public NetworkElementEntity updateNetworkElement(@PathParam("id") String id,
            @PathParam("elementId") String elementId, NetworkElementEntity entity) throws Exception {
        LOG.info(String.format("Updating the network element id %s ", "" + elementId));

        entity.setElementId(elementId);

        return (NetworkElementEntity) RestServerApiUtils.getSdnRedirectionApi(id, this.api).updatePort(entity);
    }

    @Path("/{elementId}")
    @DELETE
    public void deleteNetworkElement(@PathParam("id") String id, @PathParam("elementId") String elementId)
            throws Exception {
        LOG.info(String.format("Deleting the network element for id %s ", elementId));

        RestServerApiUtils.getSdnRedirectionApi(id, this.api).deletePort(elementId);
    }

    @GET
    public List<String> getNetworkElementIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing network elements ids'");

        return RestServerApiUtils.getSdnRedirectionApi(id, this.api).getPortIds();
    }

    @Path("/{elementId}")
    @GET
    public NetworkElementEntity getNetworkElement(@PathParam("id") String id,
            @PathParam("elementId") String elementId)
                    throws Exception {
        LOG.info(String.format("Getting the network for id %s ", elementId));

        return (NetworkElementEntity) RestServerApiUtils.getSdnRedirectionApi(id, this.api).getPort(elementId);
    }
}