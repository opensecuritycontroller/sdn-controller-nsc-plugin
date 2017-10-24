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
package org.osc.controller.nsc.utils;

import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.entities.PortGroupEntity;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.service.transaction.control.TransactionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectionApiUtils {


    public final static String SUPPORTS_PORT_GROUP_VALUE = ":Boolean=false";
    private static final Logger LOG = LoggerFactory.getLogger(RedirectionApiUtils.class);

    private TransactionControl txControl;
    private EntityManager em;

    public RedirectionApiUtils(EntityManager em, TransactionControl txControl) {
        this.em = em;
        this.txControl = txControl;
    }

    public InspectionPortEntity makeInspectionPortEntity(InspectionPortElement inspectionPortElement) {
        throwExceptionIfNullElement(inspectionPortElement);

        NetworkElement ingress = inspectionPortElement.getIngressPort();
        throwExceptionIfNullElement(ingress, "Null ingress element.");
        NetworkElementEntity ingressEntity = null;
        if (ingress.getElementId() != null) {
            ingressEntity = findNetworkElementEntityByElementId(ingress.getElementId());
        }

        ingressEntity = ingressEntity == null ? makeNetworkElementEntity(ingress) : ingressEntity;

        NetworkElement egress = inspectionPortElement.getEgressPort();
        NetworkElementEntity egressEntity = null;
        throwExceptionIfNullElement(egress, "Null egeress element.");

        if (ingressEntity != null && ingressEntity.getElementId().equals(egress.getElementId())) {
            egressEntity = ingressEntity;
        } else {
            if (egress.getElementId() != null) {
                egressEntity = findNetworkElementEntityByElementId(egress.getElementId());
            }

            egressEntity = egressEntity == null ? makeNetworkElementEntity(egress) : egressEntity;
        }

        return new InspectionPortEntity(inspectionPortElement.getElementId(), ingressEntity, egressEntity);
    }

    public PortGroupEntity makePortGroupEntity(List<NetworkElement> inspectedPorts) {
        // Using the same parent id as the inspected ports, all the parent ids of the inspected ports
        // are expected to be the same.
        PortGroupEntity portGroupEntity = new PortGroupEntity(null, inspectedPorts.get(0).getParentId(), null);
        Set<NetworkElementEntity> inspectedPortEntities = networkElementsToEntities(inspectedPorts);

        inspectedPortEntities.forEach(inspectedPortEntity -> inspectedPortEntity.setPortGroup(portGroupEntity));
        portGroupEntity.setVirtualPorts(inspectedPortEntities);

        return portGroupEntity;
    }

    public Set<NetworkElementEntity> networkElementsToEntities(List<NetworkElement> inspectedPorts) {
        throwExceptionIfNullInspectedPorts(inspectedPorts);
        Set<NetworkElementEntity> inspectedPortEntities = new HashSet<>();

        for (NetworkElement inspectedPort : inspectedPorts) {
            NetworkElementEntity inspectedPortEntity = null;

            if (inspectedPort.getElementId() != null) {
                inspectedPortEntity = findNetworkElementEntityByElementId(inspectedPort.getElementId());
            }

            inspectedPortEntities.add(inspectedPortEntity == null ?
                    makeNetworkElementEntity(inspectedPort) : inspectedPortEntity);
        }

        return inspectedPortEntities;
    }

    public InspectionHookEntity makeInspectionHookEntity(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort, Long tag, TagEncapsulationType encType, Long order,
            FailurePolicyType failurePolicyType) {

        throwExceptionIfNullElement(inspectedPort, "Null inspected port!");

        InspectionPortEntity inspectionPortEntity = makeInspectionPortEntity(inspectionPort);

        encType = (encType != null ? encType : VLAN);
        failurePolicyType = (failurePolicyType != null ? failurePolicyType : NA);

        NetworkElementEntity inspected = makeNetworkElementEntity(inspectedPort);
        InspectionHookEntity retVal = new InspectionHookEntity();

        retVal.setInspectedPort(inspected);
        retVal.setInspectionPort(inspectionPortEntity);
        retVal.setOrder(order);
        retVal.setTag(tag);
        retVal.setEncType(encType);
        retVal.setFailurePolicyType(failurePolicyType);

        inspectionPortEntity.getInspectionHooks().add(retVal);
        inspected.setInspectionHook(retVal);

        return retVal;
    }

    public NetworkElementEntity findNetworkElementEntityByElementId(String elementId) {
        return this.txControl.required(() -> {
            return txNetworkElementEntityByElementId(elementId);
        });
    }

    public NetworkElementEntity txNetworkElementEntityByElementId(String elementId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<NetworkElementEntity> q = cb.createQuery(NetworkElementEntity.class);
        Root<NetworkElementEntity> r = q.from(NetworkElementEntity.class);
        q.where(cb.equal(r.get("elementId"), elementId));

        try {
            return this.em.createQuery(q).getSingleResult();
        } catch (Exception e) {
            LOG.error(String.format("Finding Network Element %s ", elementId), e);
            return null;
        }
    }

    public PortGroupEntity txPortGroupEntity(String elementId, String parentId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<PortGroupEntity> q = cb.createQuery(PortGroupEntity.class);
        Root<PortGroupEntity> r = q.from(PortGroupEntity.class);
        q.where(cb.equal(r.get("elementId"), elementId), cb.equal(r.get("parentId"), parentId));

        try {
            return this.em.createQuery(q).getSingleResult();
        } catch (Exception e) {
            LOG.error(String.format("Finding port group entity id '%s' and parentId '%s' ", elementId, parentId), e);
            return null;
        }
    }

    public PortGroupEntity findPortGroupEntity(String elementId, String parentId) {
        return this.txControl.required(() -> {
            return txPortGroupEntity(elementId, parentId);
        });
    }

    public NetworkElementEntity findNetworkElementEntityByDeviceOwnerId(String deviceOwnerId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<NetworkElementEntity> q = cb.createQuery(NetworkElementEntity.class);
        Root<NetworkElementEntity> r = q.from(NetworkElementEntity.class);
        q.where(cb.equal(r.get("deviceOwnerId"), deviceOwnerId));
        return this.txControl.required(() -> {
            try {
                return this.em.createQuery(q).getSingleResult();
            } catch (Exception e) {
                LOG.warn(String.format("Finding Network Element %s ", deviceOwnerId), e);
                return null;
            }
        });
    }

    public List<NetworkElementEntity> findNetworkElementEntitiesByDeviceOwnerPrefixId(String deviceOwnerPrefixId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<NetworkElementEntity> q = cb.createQuery(NetworkElementEntity.class);
        Root<NetworkElementEntity> r = q.from(NetworkElementEntity.class);
        q.where(cb.like(r.get("deviceOwnerId"), deviceOwnerPrefixId + "%"));

        return this.txControl.required(() -> {
            try {
                return this.em.createQuery(q).getResultList();
            } catch (Exception e) {
                LOG.error(String.format("Finding Network Elements with Prefix Id %s ", deviceOwnerPrefixId), e);
                return new ArrayList<>();
            }
        });
    }

    public InspectionPortEntity findInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        return this.txControl.required(() -> txInspPortByNetworkElements(ingress, egress));
    }

    public InspectionHookEntity findInspHookByInspectedAndPort(NetworkElement inspected,
            InspectionPortElement element) {
        return this.txControl.required(() -> {
            return txInspHookByInspectedAndPort(inspected, element);
        });
    }

    public InspectionPortEntity txInspectionPortEntityById(String id) {
        return this.em.find(InspectionPortEntity.class, id);
    }

    public NetworkElementEntity txNetworkElementEntityById(Long id) {
        return this.em.find(NetworkElementEntity.class, id);
    }

    public void removeSingleInspectionHook(String hookId) {
        if (hookId == null) {
            LOG.warn("Attempt to remove Inspection Hook with null id");
            return;
        }

        String inspectedId  = this.txControl.required(() -> {
            InspectionHookEntity dbInspectionHook =
                    this.em.find(InspectionHookEntity.class, hookId);

            if (dbInspectionHook == null) {
                LOG.warn("Attempt to remove nonexistent Inspection Hook for id " + hookId);
                return null;
            }

            NetworkElementEntity dbInspectedPort = dbInspectionHook.getInspectedPort();

            dbInspectedPort.setInspectionHook(null);
            dbInspectionHook.setInspectedPort(null);
            return dbInspectedPort.getElementId();
        });

        if (inspectedId == null) {
            return;
        }

        this.txControl.required(() -> {
            NetworkElementEntity dbNetworkElement = this.em.find(NetworkElementEntity.class, inspectedId);

            this.em.remove(dbNetworkElement);

            Query q = this.em.createQuery("DELETE FROM InspectionHookEntity WHERE hook_id = :id");
            q.setParameter("id", hookId);
            q.executeUpdate();

            return null;
        });
    }

    public void removeSingleInspectionPort(String inspectionPortId) {
        this.txControl.required(() -> {

            Query q = this.em.createQuery("DELETE FROM InspectionPortEntity WHERE element_id = :id");
            q.setParameter("id", inspectionPortId);
            q.executeUpdate();
            return null;
        });
    }

    public List<InspectionHookEntity> txInspectionHookEntitiesByInspected(String inspectedId) {

        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InspectionHookEntity> criteria = cb.createQuery(InspectionHookEntity.class);
        Root<InspectionHookEntity> root = criteria.from(InspectionHookEntity.class);

        criteria.select(root).where(cb.equal(root.join("inspectedPort").get("elementId"), inspectedId));

        Query q= this.em.createQuery(criteria);

        @SuppressWarnings("unchecked")
        List<InspectionHookEntity> results = q.getResultList();

        return results;
    }

    public void throwExceptionIfNullEntity(InspectionPortEntity inspectionPortTmp, InspectionPortElement inspectionPort)
            throws IllegalArgumentException {
        if (inspectionPortTmp == null) {
            String msg = String.format(
                    "Cannot find inspection port for inspection hook " + "id: %s; ingress: %s; egress: %s\n",
                    inspectionPort.getElementId(),
                    "" + inspectionPort.getIngressPort(),
                    "" + inspectionPort.getEgressPort());
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public void throwExceptionIfNullElement(NetworkElement networkElement, String msg) {
        if (networkElement == null) {
            msg = (msg != null ? msg : "null passed for Network Element argument!");
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static boolean supportsPortGroup() {
        String supportsPortGroupValue = SUPPORTS_PORT_GROUP_VALUE.split("=")[1];
        return Boolean.parseBoolean(supportsPortGroupValue);
    }

    private InspectionPortEntity txInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        String ingressId = ingress != null ? ingress.getElementId() : null;
        String egressId = ingress != null ? egress.getElementId() : null;

        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InspectionPortEntity> criteria = cb.createQuery(InspectionPortEntity.class);
        Root<InspectionPortEntity> root = criteria.from(InspectionPortEntity.class);
        criteria.select(root).where(cb.and(
                cb.equal(root.join("ingressPort").get("elementId"), ingressId),
                cb.equal(root.join("egressPort").get("elementId"), egressId)));
        Query q= this.em.createQuery(criteria);

        try {
            @SuppressWarnings("unchecked")
            List<InspectionPortEntity> ports = q.getResultList();
            if (ports == null || ports.size() == 0) {
                LOG.warn(String.format("No Inspection Ports by ingress %s and egress %s", ingressId, egressId));
                return null;
            } else if (ports.size() > 1) {
                LOG.warn(String.format("Multiple results! Inspection Ports by ingress %s and egress %s", ingressId,
                        egressId));
            }
            return ports.get(0);

        } catch (Exception e) {
            LOG.error(String.format("Finding Inspection Ports by ingress %s and egress %s", ingressId, egressId), e);
            return null;
        }
    }

    private InspectionHookEntity txInspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
        // Paranoid
        NetworkElement ingress = element != null ? element.getIngressPort() : null;
        NetworkElement egress = element != null ? element.getEgressPort() : null;

        String inspectedId = inspected != null ? inspected.getElementId() : null;

        InspectionPortEntity inspPort = findInspPortByNetworkElements(ingress, egress);

        String portId = inspPort != null ? inspPort.getElementId() : null;

        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InspectionHookEntity> criteria = cb.createQuery(InspectionHookEntity.class);
        Root<InspectionHookEntity> root = criteria.from(InspectionHookEntity.class);
        criteria.select(root).where(cb.and(
                cb.equal(root.join("inspectedPort").get("elementId"), inspectedId),
                cb.equal(root.join("inspectionPort").get("elementId"), portId)));
        Query q= this.em.createQuery(criteria);

        try {
            @SuppressWarnings("unchecked")
            List<InspectionHookEntity> inspectionHooks = q.getResultList();
            if (inspectionHooks == null || inspectionHooks.size() == 0) {
                LOG.warn(String.format("No Inspection hooks by inspected %s and port %s", inspectedId, portId));
                return null;
            } else if (inspectionHooks.size() > 1) {
                LOG.warn(String.format("Multiple results! Inspection hooks by inspected %s and port %s", inspectedId,
                        portId));
            }
            return inspectionHooks.get(0);

        } catch (Exception e) {
            LOG.error(String.format("Finding Inspection hooks by inspected %s and port %s", inspectedId, portId), e);
            return null;
        }
    }

    private void throwExceptionIfNullElement(InspectionPortElement networkElement) {
        if (networkElement == null) {
            String msg = "null passed for Inspection Port argument!";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private void throwExceptionIfNullInspectedPorts(List<NetworkElement> inspectedPorts) {
        if (inspectedPorts == null) {
            String msg = "null passed for inspected ports argument!";
            throw new IllegalArgumentException(msg);
        }
    }

    private static NetworkElementEntity makeNetworkElementEntity(NetworkElement networkElement) {
        NetworkElementEntity retVal = new NetworkElementEntity();

        retVal.setElementId(networkElement.getElementId());
        retVal.setMacAddresses(networkElement.getMacAddresses());
        retVal.setPortIPs(networkElement.getPortIPs());

        return retVal;
    }
}
