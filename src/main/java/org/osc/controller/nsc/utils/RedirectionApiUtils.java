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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.service.transaction.control.TransactionControl;

public class RedirectionApiUtils {

    private static final Logger LOG = Logger.getLogger(RedirectionApiUtils.class);

    private TransactionControl txControl;
    private EntityManager em;

    public RedirectionApiUtils(EntityManager em, TransactionControl txControl) {
        this.em = em;
        this.txControl = txControl;
    }

    public NetworkElementEntity makeNetworkElementEntity(NetworkElement networkElement) {
        NetworkElementEntity retVal = new NetworkElementEntity();

        retVal.setElementId(networkElement.getElementId());
        retVal.setMacAddresses(networkElement.getMacAddresses());
        retVal.setPortIPs(networkElement.getPortIPs());

        return retVal;
    }

    public InspectionPortEntity makeInspectionPortEntity(InspectionPortElement inspectionPortElement) {
        throwExceptionIfNullElement(inspectionPortElement);

        NetworkElement ingress = inspectionPortElement.getIngressPort();
        throwExceptionIfNullElement(ingress, "Null ingress element.");
        NetworkElementEntity ingressEntity = makeNetworkElementEntity(ingress);

        NetworkElement egress = inspectionPortElement.getEgressPort();
        NetworkElementEntity egressEntity = null;
        throwExceptionIfNullElement(egress, "Null egeress element.");

        if (ingressEntity != null && ingressEntity.getElementId().equals(egress.getElementId())) {
            egressEntity = ingressEntity;
        } else {
            egressEntity = makeNetworkElementEntity(egress);
        }

        return new InspectionPortEntity(inspectionPortElement.getElementId(), ingressEntity, egressEntity);
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

    public NetworkElementEntity networkElementEntityByElementId(String elementId) {
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

    public InspectionPortEntity findInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        return this.txControl.required(() -> txInspPortByNetworkElements(ingress, egress));
    }

    private InspectionPortEntity txInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        String ingressId = ingress != null ? ingress.getElementId() : null;
        String egressId = ingress != null ? egress.getElementId() : null;

        Query q = this.em.createQuery("FROM InspectionPortEntity WHERE ingress_fk = :ingId AND egress_fk = :egId ");
        q.setParameter("ingId", ingressId);
        q.setParameter("egId", egressId);

        try {
            @SuppressWarnings("unchecked")
            List<InspectionPortEntity> ports = q.getResultList();
            if (ports == null || ports.size() == 0) {
                LOG.warn(String.format("No Inspection ports by ingress %s and egress %s", ingressId, egressId));
                return null;
            } else if (ports.size() > 1) {
                LOG.warn(String.format("Multiple results! Inspection ports by ingress %s and egress %s", ingressId,
                        egressId));
            }
            return ports.get(0);

        } catch (Exception e) {
            LOG.error(String.format("Finding Inspection ports by ingress %s and egress %s", ingress.getElementId(),
                    egress.getElementId()), e);
            return null;
        }
    }

    public InspectionHookEntity findInspHookByInspectedAndPort(NetworkElement inspected,
            InspectionPortElement element) {
        return this.txControl.required(() -> {
            InspectionHookEntity e = txInspHookByInspectedAndPort(inspected, element);
            return e;
        });
    }

    public InspectionPortEntity txInspectionPortEntityById(String id) {
        return this.em.find(InspectionPortEntity.class, id);
    }

    public NetworkElementEntity txNetworkElementEntityById(Long id) {
        return this.em.find(NetworkElementEntity.class, id);
    }

    public void removeSingleInspectionHook(InspectionHookEntity inspectionHookEntity) {
        this.txControl.required(() -> {
            NetworkElementEntity networkElementEntity = inspectionHookEntity.getInspectedPort();

            inspectionHookEntity.setInspectionPort(null);
            inspectionHookEntity.setInspectedPort(null);
            networkElementEntity.setInspectionHook(null);
            this.em.remove(inspectionHookEntity);
            return null;
        });
    }

    private InspectionHookEntity txInspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
        // Paranoid
        NetworkElement ingress = element != null ? element.getIngressPort() : null;
        NetworkElement egress = element != null ? element.getEgressPort() : null;

        String inspectedId = inspected != null ? inspected.getElementId() : null;

        InspectionPortEntity inspPort = findInspPortByNetworkElements(ingress, egress);

        String portId = inspPort != null ? inspPort.getElementId() : null;

        Query q = this.em.createQuery(
                "FROM InspectionHookEntity WHERE inspected_port_fk = :inspectedId AND inspection_port_fk = :inspectionId ");
        q.setParameter("inspectedId", inspectedId);
        q.setParameter("inspectionId", portId);

        try {
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

    public void throwExceptionIfNullEntity(InspectionPortEntity inspectionPortTmp, InspectionPortElement inspectionPort)
            throws IllegalArgumentException {
        if (inspectionPortTmp == null) {
            String ingressId = inspectionPort.getIngressPort() != null ? inspectionPort.getIngressPort().getElementId()
                    : null;
            String egressId = inspectionPort.getEgressPort() != null ? inspectionPort.getEgressPort().getElementId()
                    : null;
            String msg = String.format(
                    "Cannot find inspection port for inspection hook " + "id: %s; ingressId: %s; egressId: %s\n",
                    inspectionPort.getElementId(), ingressId, egressId);
            throw new IllegalArgumentException(msg);
        }
    }

    private void throwExceptionIfNullElement(NetworkElement networkElement, String msg) {
        if (networkElement == null) {
            msg = (msg != null ? msg : "null passed for Network Element argument!");
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private void throwExceptionIfNullElement(InspectionPortElement networkElement) {
        if (networkElement == null) {
            String msg = "null passed for Inspection Port argument!";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
