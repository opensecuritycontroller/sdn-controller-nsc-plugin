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
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = InspectionPortApis.class)
@Path("/controller/{id}/inspectionPorts")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionPortApis {

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
    public String createInspectionPort(@PathParam("id") String id, InspectionPortEntity entity)
            throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create null InspectionPort");
        }
        LOG.info(String.format("Creating inspection port for (ingress id %s ; egress id %s)",
                entity.getIngressPort().getElementId(), entity.getEgressPort().getElementId()));
        InspectionPortEntity inspectionPort = (InspectionPortEntity) getSdnRedirectionApi(id)
                .registerInspectionPort(entity);
        return inspectionPort == null ? null : inspectionPort.getElementId();
    }

    @Path("/{portId}")
    @PUT
    public InspectionPortEntity updateInspectionPort(@PathParam("id") String id,
            @PathParam("portId") String portId, InspectionPortEntity entity) throws Exception {

        LOG.info(String.format("Updating the inspection port element id %s ", portId));
        entity.setId(portId);
        return (InspectionPortEntity) getSdnRedirectionApi(id).updateInspectionPort(entity);
    }

    @Path("/{portId}")
    @DELETE
    public void deleteInspectionPort(@PathParam("id") String id, @PathParam("portId") String portId)
            throws Exception {
        if (portId == null) {
            throw new IllegalArgumentException("Attempt to update null inspection port");
        }
        LOG.info(String.format("Deleting the inspection port element for id %s ", portId));
        InspectionPortEntity inspectionPort = new InspectionPortEntity(portId, null, null);
        getSdnRedirectionApi(id).removeInspectionPort(inspectionPort);
    }

    @GET
    public List<String> getInspectionPortIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing inspection port ids'");
        return getSdnRedirectionApi(id).getInspectionPorts();
    }

    @Path("/{portId}")
    @GET
    public InspectionPortEntity getInspectionPort(@PathParam("id") String id,
            @PathParam("portId") String portId) throws Exception {
        if (portId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection port");
        }
        LOG.info(String.format("Getting the inspection port element for id %s ", portId));
        InspectionPortEntity inspectionPort = new InspectionPortEntity(portId, null, null);
        return (InspectionPortEntity) getSdnRedirectionApi(id).getInspectionPort(inspectionPort);
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
