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

import static java.util.Collections.singletonMap;
import static org.osgi.service.jdbc.DataSourceFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@Component(service = InspectionPortApis.class)
@Path("/inspectionPort")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionPortApis {
    private static Logger LOG = Logger.getLogger(InspectionPortApis.class);
    private EntityManager em;
    private RedirectionApiUtils utils;
    private SampleSdnRedirectionApi sdnApi;
    @Reference(target = "(osgi.local.enabled=true)")
    private TransactionControl txControl;
    @Reference(target = "(osgi.unit.name=nsc-mgr)")
    private EntityManagerFactoryBuilder builder;
    @Reference(target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;
    @Reference(target = "(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;
    private String id;

    public void init(String id) throws Exception {
        if (id != null) {
            if (id.equals(this.id)) {
                return;
            }
        } else {
            return;
        }

        Properties props = new Properties();
        props.setProperty(JDBC_URL, "jdbc:h2:./nscPlugin_" + id + ";MVCC\\=TRUE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;");
        props.setProperty(JDBC_USER, "admin");
        props.setProperty(JDBC_PASSWORD, "admin123");
        DataSource ds = null;
        try {
            ds = this.jdbcFactory.createDataSource(props);
        } catch (SQLException error) {
            LOG.error(error);
            throw new IllegalStateException(error.getMessage(), error);
        }
        EntityManager em = this.resourceFactory
                .getProviderFor(this.builder, singletonMap("javax.persistence.nonJtaDataSource", (Object) ds), null)
                .getResource(this.txControl);
        this.em = em;
        this.sdnApi = new SampleSdnRedirectionApi(this.txControl, this.em);
        this.utils = new RedirectionApiUtils(this.em, this.txControl);
        this.id = id;

    }

    @Path("/controller/{id}/insport")
    @POST
    public String createInspectionPort(@PathParam("id") String id, InspectionPortEntity entity)
            throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create null InspectionPort");
        }
        LOG.info(String.format("Creating inspection port for (ingress id %s ; egress id %s)",
                entity.getIngressPort().getElementId(), entity.getEgressPort().getElementId()));
        init(id);
        InspectionPortEntity inspectionPort = (InspectionPortEntity) this.sdnApi.registerInspectionPort(entity);
        return inspectionPort == null ? null : inspectionPort.getElementId();
    }

    @Path("/controller/{id}/insport/{portId}")
    @PUT
    public InspectionPortEntity updateInspectionPort(@PathParam("id") String id,
            @PathParam("portId") String portId, InspectionPortEntity entity) throws Exception {

        LOG.info(String.format("Updating the inspection port element id %s ", portId));
        init(id);
        return this.txControl.required(() -> {
            entity.setId(portId);
            InspectionPortEntity inspectionPortEntity = (InspectionPortEntity) this.sdnApi.getInspectionPort(entity);

            if (inspectionPortEntity == null) {
                String msg = String.format("inspection port element does not exists id: %s\n", portId);
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            inspectionPortEntity.setEgressPort(entity.getEgressPort());
            inspectionPortEntity.setIngressPort(entity.getIngressPort());
            return this.em.merge(inspectionPortEntity);
        });
    }

    @Path("/controller/{id}/insport/{portId}")
    @DELETE
    public void deleteInspectionPort(@PathParam("id") String id, @PathParam("portId") String portId)
            throws Exception {
        if (portId == null) {
            throw new IllegalArgumentException("Attempt to update null inspection port");
        }
        LOG.info(String.format("Deleting the inspection port element for id %s ", portId));
        init(id);
        InspectionPortEntity inspectionPort = new InspectionPortEntity(portId, null, null);
        this.sdnApi.removeInspectionPort(inspectionPort);
    }

    @Path("/controller/{id}/insport")
    @GET
    public List<String> getInspectionPortIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing inspection port ids'");
        init(id);

        return this.txControl.supports(() -> {
            List<? extends InspectionPortEntity> elements = this.utils.txInspectionPortEntities();
            List<String> elementList = new ArrayList<String>();
            if (!elements.isEmpty()) {
                elementList = elements.stream().map(InspectionPortEntity::getElementId).collect(Collectors.toList());
            }
            return elementList;
        });
    }

    @Path("/controller/{id}/insport/{insportId}")
    @GET
    public InspectionPortEntity getInspectionPort(@PathParam("id") String id,
            @PathParam("insportId") String insportId) throws Exception {
        if (insportId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection port");
        }
        LOG.info(String.format("Getting the inspection port element for id %s ", insportId));
        init(id);
        InspectionPortEntity inspectionPort = new InspectionPortEntity(insportId, null, null);
        return (InspectionPortEntity) this.sdnApi.getInspectionPort(inspectionPort);
    }
}
