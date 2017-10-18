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

import javax.persistence.EntityManager;
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

    private static Logger LOG = LoggerFactory.getLogger(InspectionPortApis.class);

    private EntityManager em;

    @Reference(target = "(osgi.local.enabled=true)")
    private TransactionControl txControl;

    @Reference(target = "(osgi.unit.name=nsc-mgr)")
    private EntityManagerFactoryBuilder builder;

    @Reference(target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;

    @Reference(target = "(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;

    @POST
    public String createInspectionHook(@PathParam("id") String id, InspectionHookEntity entity) throws Exception {

        LOG.info(String.format("Creating inspection hook with inspection port id %s",
                entity.getInspectionPort().getElementId()));
        return getSdnRedirectionApi(id).installInspectionHook(entity.getInspectedPort(), entity.getInspectionPort(),
                entity.getTag(),
                entity.getEncType(), entity.getOrder(), entity.getFailurePolicyType());
    }

    @Path("/{inshookId}")
    @PUT
    public InspectionHookEntity updateInspectionHook(@PathParam("id") String id,
            @PathParam("inshookId") String inshookId, InspectionHookEntity entity) throws Exception {
        entity.setHookId(inshookId);
        getSdnRedirectionApi(id).updateInspectionHook(entity);
        return entity;
    }

    @Path("/{inshookId}")
    @DELETE
    public void deleteInspectionHook(@PathParam("id") String id, @PathParam("inshookId") String inshookId)
            throws Exception {
        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to delete null inspection hook port");
        }
        LOG.info(String.format("Deleting the inspection hook element for id %s ", inshookId));
        getSdnRedirectionApi(id).removeInspectionHook(inshookId);
    }

    @GET
    public List<String> getInspectionHookIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing inspection hook ids'");

        return getSdnRedirectionApi(id).getInspectionHooks();
    }

    @Path("/{inshookId}")
    @GET
    public InspectionHookEntity getInspectionHook(@PathParam("id") String id, @PathParam("inshookId") String inshookId)
            throws Exception {
        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection hook");
        }
        LOG.info(String.format("Getting the inspection hook element for id %s ", inshookId));
        InspectionHookEntity inspectionHook = (InspectionHookEntity) getSdnRedirectionApi(id)
                .getInspectionHook(inshookId);
        inspectionHook.setInspectedPort(null);
        inspectionHook.setInspectionPort(null);
        return inspectionHook;
    }

    private SampleSdnRedirectionApi getSdnRedirectionApi(String id) throws Exception {

        if (id == null) {
            return null;
        }

        SampleSdnRedirectionApi sdnApi = RestServerApiUtils.getSdnApi(id);
        if (sdnApi != null) {
            return sdnApi;
        }

        this.em = RestServerApiUtils.createEntityHandle(this.resourceFactory, this.txControl, this.builder,
                this.jdbcFactory, id);
        sdnApi = new SampleSdnRedirectionApi(this.txControl, this.em);
        RestServerApiUtils.insertSdnApi(id, sdnApi);

        return sdnApi;
    }
}
