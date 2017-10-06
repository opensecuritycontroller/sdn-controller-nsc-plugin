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
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@Component(service = NetworkElementApis.class)
@Path("/networkElements")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class NetworkElementApis {
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
        props.setProperty(JDBC_URL,
                "jdbc:h2:./nscPlugin_" + id + ";MVCC\\=TRUE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;");
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

    @Path("/controller/{id}/netelem/{elementId}")
    @POST
    public String createNetworkElement(@PathParam("id") String id, @PathParam("elementId") String elementId,
            NetworkElementEntity entity)
                    throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create network element with no network entities");
        }
        LOG.info(String.format("Creating network element with elementId  %s", elementId));

        init(id);
        entity.setElementId(elementId);
        return this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(elementId);

            if (element != null) {
                String msg = String.format("Network element already exists id: %s\n", elementId);
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.em.merge(entity);
            return entity.getElementId();
        });
    }

    @Path("/controller/{id}/netelem/{elementId}")
    @PUT
    public NetworkElementEntity updateNetworkElement(@PathParam("id") String id,
            @PathParam("elementId") String elementId, NetworkElementEntity entity) throws Exception {
        LOG.info(String.format("Updating the network element id %s ", "" + elementId));
        init(id);
        entity.setElementId(elementId);
        return this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(elementId);

            if (element == null) {
                String msg = String.format("Cannot find the network element id: %s\n", elementId);
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.em.merge(entity);
            return entity;
        });
    }

    @Path("/controller/{id}/netelem/{elementId}")
    @DELETE
    public void deleteNetworkElement(@PathParam("id") String id, @PathParam("elementId") String elementId)
            throws Exception {
        LOG.info(String.format("Deleting the network element for id %s ", elementId));
        init(id);

        this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(elementId);

            if (element == null) {
                LOG.info(String.format(
                        "Attempt to delete network element for id %s and network element not found, no-op.",
                        elementId));
                return null;
            }

            this.em.remove(element);
            return null;
        });
    }

    @Path("/controller/{id}/netelem")
    @GET
    public List<String> getNetworkElementIds(@PathParam("id") String id) throws Exception {
        LOG.info("Listing network elements ids'");
        init(id);
        return this.txControl.supports(() -> {
            List<? extends NetworkElementEntity> elements = this.utils.txNetworkElementEntities();
            List<String> elementList = new ArrayList<String>();
            if (!elements.isEmpty()) {
                elementList = elements.stream().map(NetworkElementEntity::getElementId).collect(Collectors.toList());
            }
            return elementList;
        });
    }

    @Path("/controller/{id}/netelem/{elementId}")
    @GET
    public NetworkElementEntity getNetworkElement(@PathParam("id") String id,
            @PathParam("elementId") String elementId)
                    throws Exception {
        LOG.info(String.format("Getting the network for id %s ", elementId));
        init(id);
        return this.txControl.supports(() -> {
            return this.utils.txNetworkElementEntityByElementId(elementId);
        });
    }
}