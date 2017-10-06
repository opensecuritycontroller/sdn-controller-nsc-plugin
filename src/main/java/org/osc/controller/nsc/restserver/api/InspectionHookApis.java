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
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@Component(service = InspectionHookApis.class)
@Path("/inspectionHook")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionHookApis {
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

    @Path("/controller/{id}/inshook")
    @POST
    public String createInspectionHook(@PathParam("id") String id, InspectionHookEntity entity) throws Exception {

        LOG.info(String.format("Creating inspection hook with inspection port id %s",
                entity.getInspectionPort().getElementId()));
        init(id);
        return this.sdnApi.installInspectionHook(entity.getInspectedPort(), entity.getInspectionPort(), entity.getTag(),
                entity.getEncType(), entity.getOrder(), entity.getFailurePolicyType());
    }

    @Path("/controller/{id}/inshook/{inshookId}")
    @PUT
    public InspectionHookEntity updateInspectionHook(@PathParam("id") String id,
            @PathParam("insportId") String insportId, @PathParam("inshookId") String inshookId,
            InspectionHookEntity entity) throws Exception {
        entity.setHookId(inshookId);
        this.sdnApi.updateInspectionHook(entity);
        return entity;
    }

    @Path("/controller/{id}/inshook/{inshookId}")
    @DELETE
    public void deleteInspectionHook(@PathParam("id") String id, @PathParam("inshookId") String inshookId)
            throws Exception {
        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to delete null inspection hook port");
        }
        LOG.info(String.format("Deleting the inspection hook element for id %s ", inshookId));
        init(id);
        this.sdnApi.removeInspectionHook(inshookId);
    }

    @Path("/controller/{id}/inshook/")
    @GET
    public List<String> getInspectionHookIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing inspection hook ids'");
        init(id);

        return this.txControl.supports(() -> {
            List<InspectionHookEntity> elements = this.utils.txInspectionHookEntities();
            List<String> elementList = new ArrayList<String>();
            if (!elements.isEmpty()) {
                elementList = elements.stream().map(InspectionHookEntity::getHookId).collect(Collectors.toList());
            }
            return elementList;
        });
    }

    @Path("/controller/{id}/inshook/{inshookId}")
    @GET
    public InspectionHookEntity getInspectionHook(@PathParam("id") String id, @PathParam("inshookId") String inshookId)
            throws Exception {
        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection hook");
        }
        LOG.info(String.format("Getting the inspection hook element for id %s ", inshookId));
        init(id);
        InspectionHookEntity inspectionHook = (InspectionHookEntity) this.sdnApi.getInspectionHook(inshookId);
        inspectionHook.setInspectedPort(null);
        inspectionHook.setInspectionPort(null);
        return inspectionHook;
    }
}
