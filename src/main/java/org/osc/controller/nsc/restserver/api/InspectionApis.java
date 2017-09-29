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
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@Component(service = InspectionApis.class)
@Path("/nw")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionApis {
    private static Logger LOG = Logger.getLogger(InspectionApis.class);
    private EntityManager em;
    private RedirectionApiUtils utils;
    private SampleSdnRedirectionApi api;
    @Reference(target = "(osgi.local.enabled=true)")
    private TransactionControl txControl;
    @Reference(target = "(osgi.unit.name=nsc-mgr)")
    private EntityManagerFactoryBuilder builder;
    @Reference(target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;
    @Reference(target = "(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;
    private String ipAddr;

    public void init(String ipAddr) throws Exception {
        if (ipAddr != null) {
            if (ipAddr.equals(this.ipAddr)) {
                return;
            }
        } else {
            return;
        }

        Properties props = new Properties();
        props.setProperty(JDBC_URL,
                "jdbc:h2:./nscPlugin_" + ipAddr + ";MVCC\\=TRUE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;");
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
        this.api = new SampleSdnRedirectionApi(this.txControl, this.em);
        this.utils = new RedirectionApiUtils(this.em, this.txControl);
        this.ipAddr = ipAddr;

    }

    @Path("/ipAddr/{ipAddr}/netelem")
    @POST
    public String createNetworkElement(@PathParam("ipAddr") String ipAddr, NetworkElementEntity entity)
            throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create network element with no network entities");
        }
        LOG.info(String.format("Creating network element with elementId  id %s)", "" + entity.getElementId()));

        init(ipAddr);
        return this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(entity.getElementId());

            if (element != null) {
                String msg = String.format("Network element already exists id: %s\n", entity.getElementId());
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.em.merge(entity);
            return entity.getElementId();
        });
    }

    @Path("/ipAddr/{ipAddr}/netelem/{elementId}")
    @PUT
    public NetworkElementEntity updateNetworkElement(@PathParam("ipAddr") String ipAddr,
            @PathParam("elementId") String elementId, NetworkElementEntity entity) throws Exception {
        LOG.info(String.format("Updating the network element id %s ", "" + elementId));
        init(ipAddr);

        return this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(entity.getElementId());

            if (element == null) {
                String msg = String.format("Cannot find the network element id: %s\n", entity.getElementId());
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.em.merge(entity);
            return entity;
        });
    }

    @Path("/ipAddr/{ipAddr}/netelem/{elementId}")
    @DELETE
    public void deleteNetworkElement(@PathParam("ipAddr") String ipAddr, @PathParam("elementId") String elementId)
            throws Exception {
        LOG.info(String.format("Deleting the network element for id %s ", "" + elementId));
        init(ipAddr);

        this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(elementId);

            if (element == null) {
                LOG.info(String.format(
                        "Attempt to delete network element for id %s and network element not found, no-op.",
                        "" + elementId));
                return null;
            }

            this.em.remove(element);
            return null;
        });
    }

    @Path("/ipAddr/{ipAddr}/netelem")
    @GET
    public List<String> getNetworkElementIds(@PathParam("ipAddr") String ipAddr) throws Exception {
        LOG.info("Listing network elements ids'");
        init(ipAddr);
        return this.txControl.supports(() -> {
            List<? extends NetworkElementEntity> elements = this.utils.txNetworkElementEntities();
            List<String> elementList = new ArrayList<String>();
            if (elements.isEmpty()) {
                return elementList;
            }
            elementList = elements.stream().map(NetworkElementEntity::getElementId).collect(Collectors.toList());
            return elementList;
        });
    }

    @Path("/ipAddr/{ipAddr}/netelem/{elementId}")
    @GET
    public NetworkElementEntity getNetworkElement(@PathParam("ipAddr") String ipAddr,
            @PathParam("elementId") String elementId)
                    throws Exception {
        LOG.info(String.format("Getting the network for id %s ", "" + elementId));
        init(ipAddr);
        return this.txControl.supports(() -> {
            return this.utils.txNetworkElementEntityByElementId(elementId);
        });
    }

    @Path("/ipAddr/{ipAddr}/insport")
    @POST
    public String createInspectionPort(@PathParam("ipAddr") String ipAddr, InspectionPortEntity entity)
            throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("Attempt to create null InspectionPort");
        }
        LOG.info(String.format("Creating inspection port for (ingress id %s ; egress id %s)",
                "" + entity.getIngressPort().getElementId(), "" + entity.getEgressPort().getElementId()));
        init(ipAddr);
        return this.txControl.required(() -> {

            InspectionPortEntity inspectionPortEntity = (InspectionPortEntity) this.api.getInspectionPort(entity);

            if (inspectionPortEntity != null) {
                String msg = String.format("inspection port element already exists id: %s\n", entity.getElementId());
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            inspectionPortEntity = this.utils.makeInspectionPortEntity(entity);
            return this.em.merge(inspectionPortEntity).getElementId();
        });
    }

    @Path("/ipAddr/{ipAddr}/insport/{portId}")
    @PUT
    public InspectionPortEntity updateInspectionPort(@PathParam("ipAddr") String ipAddr,
            @PathParam("portId") String portId, InspectionPortEntity entity) throws Exception {

        LOG.info(String.format("Updating the inspection port element id %s ", "" + portId));
        init(ipAddr);
        return this.txControl.required(() -> {

            InspectionPortEntity inspectionPortEntity = (InspectionPortEntity) this.api.getInspectionPort(entity);

            if (inspectionPortEntity == null) {
                String msg = String.format("inspection port element does not exists id: %s\n", entity.getElementId());
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            inspectionPortEntity = this.utils.makeInspectionPortEntity(entity);
            return this.em.merge(inspectionPortEntity);
        });
    }

    @Path("/ipAddr/{ipAddr}/insport/{portId}")
    @DELETE
    public void deleteInspectionPort(@PathParam("ipAddr") String ipAddr, @PathParam("portId") String portId)
            throws Exception {
        if (portId == null) {
            throw new IllegalArgumentException("Attempt to update null inspection port");
        }
        LOG.info(String.format("Deleting the inspection port element for id %s ", "" + portId));
        init(ipAddr);
        InspectionPortEntity inspectionPort = new InspectionPortEntity(portId, null, null);
        this.api.removeInspectionPort(inspectionPort);
    }

    @Path("/ipAddr/{ipAddr}/insport")
    @GET
    public List<String> getInspectionPortIds(@PathParam("ipAddr") String ipAddr) throws Exception {
        LOG.info("Listing inspection port ids'");
        init(ipAddr);

        return this.txControl.supports(() -> {
            List<? extends InspectionPortEntity> elements = this.utils.txInspectionPortEntities();
            List<String> elementList = new ArrayList<String>();
            if (elements.isEmpty()) {
                return elementList;
            }
            elementList = elements.stream().map(InspectionPortEntity::getElementId).collect(Collectors.toList());
            return elementList;
        });
    }

    @Path("/ipAddr/{ipAddr}/insport/{insportId}")
    @GET
    public InspectionPortEntity getInspectionPortElement(@PathParam("ipAddr") String ipAddr,
            @PathParam("insportId") String insportId) throws Exception {
        if (insportId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection port");
        }
        LOG.info(String.format("Getting the inspection port element for id %s ", "" + insportId));
        init(ipAddr);
        InspectionPortEntity inspectionPort = new InspectionPortEntity(insportId, null, null);
        return (InspectionPortEntity) this.api.getInspectionPort(inspectionPort);
    }

    @Path("/ipAddr/{ipAddr}/insport/{insportId}/inshook")
    @POST
    public String createInspectionHook(@PathParam("ipAddr") String ipAddr, @PathParam("insportId") String insportId,
            InspectionHookEntity entity)
                    throws Exception {

        if (insportId == null) {
            throw new IllegalArgumentException("Attempt to create inspection hook with null inspection port");
        }
        LOG.info(String.format("Creating inspection hook with inspection port id %s", "" + insportId));
        init(ipAddr);
        InspectionPortEntity inspectionPort = new InspectionPortEntity(insportId, null, null);
        NetworkElementEntity inspectedPort = new NetworkElementEntity();
        inspectedPort.setElementId(entity.getInspectedPort().getElementId());

        InspectionHookEntity retValEntity = this.txControl.required(() -> {
            InspectionPortEntity dbInspectionPort = (InspectionPortEntity) this.api.getInspectionPort(inspectionPort);
            this.utils.throwExceptionIfNullEntity(dbInspectionPort, inspectionPort);

            InspectionHookEntity inspectionHookEntity = this.utils.findInspHookByInspectedAndPort(inspectedPort,
                    dbInspectionPort);
            if (inspectionHookEntity != null) {
                String msg = String.format("inspection hook element exists for inspection id: %s, inspected id: %s\n",
                        insportId, inspectedPort.getElementId());
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            inspectionHookEntity = this.utils.makeInspectionHookEntity(inspectedPort, dbInspectionPort, entity.getTag(),
                    entity.getEncType(), entity.getOrder(), entity.getFailurePolicyType());
            inspectionHookEntity = this.em.merge(inspectionHookEntity);
            return inspectionHookEntity;
        });
        return retValEntity.getHookId();
    }

    @Path("/ipAddr/{ipAddr}/insport/{insportId}/inshook/{inshookId}")
    @PUT
    public InspectionHookEntity updateInspectionHook(@PathParam("ipAddr") String ipAddr,
            @PathParam("insportId") String insportId, @PathParam("insportId") String inshookId,
            InspectionHookEntity entity) throws Exception {

        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to update inspection hook with null inspection hook port");
        }
        LOG.info(String.format("Updating the inspection hook element id %s ", "" + inshookId));
        init(ipAddr);
        InspectionPortEntity inspectionPort = new InspectionPortEntity(insportId, null, null);
        NetworkElementEntity inspectedPort = new NetworkElementEntity();
        inspectedPort.setElementId(entity.getInspectedPort().getElementId());

        InspectionHookEntity retValEntity = this.txControl.required(() -> {
            InspectionPortEntity dbInspectionPort = (InspectionPortEntity) this.api.getInspectionPort(inspectionPort);
            this.utils.throwExceptionIfNullEntity(dbInspectionPort, inspectionPort);

            InspectionHookEntity inspectionHookEntity = this.utils.findInspHookByInspectedAndPort(inspectedPort,
                    dbInspectionPort);
            if (inspectionHookEntity == null) {
                String msg = String.format(
                        "inspection hook element does not exists for inspection id: %s, inspected id: %s\n", insportId,
                        inspectedPort.getElementId());
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }

            inspectionHookEntity = this.utils.makeInspectionHookEntity(inspectedPort, dbInspectionPort, entity.getTag(),
                    entity.getEncType(), entity.getOrder(), entity.getFailurePolicyType());
            return this.em.merge(inspectionHookEntity);
        });
        return retValEntity;
    }

    @Path("/ipAddr/{ipAddr}/insport/{insportId}/inshook/{inshookId}")
    @DELETE
    public void deleteInspectionHook(@PathParam("ipAddr") String ipAddr, @PathParam("portId") String portId,
            @PathParam("inshookId") String inshookId) throws Exception {
        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to delete null inspection hook port");
        }
        LOG.info(String.format("Deleting the inspection hook element for id %s ", "" + inshookId));
        init(ipAddr);
        this.api.removeInspectionHook(inshookId);
    }

    @Path("/ipAddr/{ipAddr}/insport/{insportId}/inshook/")
    @GET
    public List<String> getInspectionHookIds(@PathParam("ipAddr") String ipAddr,
            @PathParam("insportId") String insportId) throws Exception {
        LOG.info("Listing inspection hook ids'");
        init(ipAddr);

        return this.txControl.supports(() -> {
            List<InspectionHookEntity> elements = this.utils.txInspectionHookEntities(insportId);
            List<String> elementList = new ArrayList<String>();
            if (elements.isEmpty()) {
                return elementList;
            }
            elementList = elements.stream().map(InspectionHookEntity::getHookId).collect(Collectors.toList());
            return elementList;
        });
    }

    @Path("/ipAddr/{ipAddr}/insport/{insportId}/inshook/{inshookId}")
    @GET
    public InspectionHookEntity getInspectionHookElement(@PathParam("ipAddr") String ipAddr,
            @PathParam("insportId") String insportId, @PathParam("inshookId") String inshookId) throws Exception {
        if (inshookId == null) {
            throw new IllegalArgumentException("Attempt to retrive null inspection hook");
        }
        LOG.info(String.format("Getting the inspection hook element for id %s ", "" + inshookId));
        init(ipAddr);
        InspectionHookEntity inspectionHook = (InspectionHookEntity) this.api.getInspectionHook(inshookId);
        inspectionHook.setInspectedPort(null);
        inspectionHook.setInspectionPort(null);
        return inspectionHook;
    }
}

