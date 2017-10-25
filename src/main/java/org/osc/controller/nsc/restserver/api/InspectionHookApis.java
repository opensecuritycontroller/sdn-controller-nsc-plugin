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

import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = InspectionHookApis.class)
@Path("/controller/{id}/inspectionHooks")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionHookApis {

    private static Logger LOG = LoggerFactory.getLogger(InspectionHookApis.class);

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

    @POST
    public String createInspectionHook(@PathParam("id") String id, InspectionHookEntity entity) throws Exception {

        LOG.info(String.format("Creating inspection hook with inspection port id %s",
                entity.getInspectionPort().getElementId()));

        return RestServerApiUtils.getSdnRedirectionApi(id, this.api).installInspectionHook(entity.getInspectedPort(),
                entity.getInspectionPort(), entity.getTag(), entity.getEncType(), entity.getOrder(),
                entity.getFailurePolicyType());
    }

    @Path("/{InspectionHookId}")
    @PUT
    public InspectionHookEntity updateInspectionHook(@PathParam("id") String id,
            @PathParam("InspectionHookId") String InspectionHookId, InspectionHookEntity entity) throws Exception {
        entity.setHookId(InspectionHookId);

        RestServerApiUtils.getSdnRedirectionApi(id, this.api).updateInspectionHook(entity);

        return entity;
    }

    @Path("/{InspectionHookId}")
    @DELETE
    public void deleteInspectionHook(@PathParam("id") String id, @PathParam("InspectionHookId") String InspectionHookId)
            throws Exception {
        if (InspectionHookId == null) {
            throw new IllegalArgumentException("Attempt to delete null inspection hook port");
        }

        LOG.info(String.format("Deleting the inspection hook element for id %s ", InspectionHookId));

        RestServerApiUtils.getSdnRedirectionApi(id, this.api).removeInspectionHook(InspectionHookId);
    }

    @GET
    public List<String> getInspectionHookIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing inspection hook ids'");

        return RestServerApiUtils.getSdnRedirectionApi(id, this.api).getInspectionHooksIds();
    }

    @Path("/{InspectionHookId}")
    @GET
    public InspectionHookEntity getInspectionHook(@PathParam("id") String id,
            @PathParam("InspectionHookId") String InspectionHookId)
                    throws Exception {
        if (InspectionHookId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection hook");
        }

        LOG.info(String.format("Getting the inspection hook element for id %s ", InspectionHookId));
        InspectionHookEntity inspectionHook = (InspectionHookEntity) RestServerApiUtils
                .getSdnRedirectionApi(id, this.api).getInspectionHook(InspectionHookId);

        inspectionHook.setInspectedPort(null);
        inspectionHook.setInspectionPort(null);

        return inspectionHook;
    }
}
