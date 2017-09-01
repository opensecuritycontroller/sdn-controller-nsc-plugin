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

public class NSCUtils {

    private static final Logger LOG = Logger.getLogger(NSCUtils.class);

    private TransactionControl txControl;
    private EntityManager em;

    public NSCUtils(EntityManager em, TransactionControl txControl) {
        this.em = em;
        this.txControl = txControl;
    }

    public NetworkElementEntity makeNetworkElementEntity(NetworkElement networkElement) {

        if (networkElement == null) {
            return null;
        }

        NetworkElementEntity retVal = new NetworkElementEntity();

        retVal.setElementId(networkElement.getElementId());
        retVal.setMacAddresses(networkElement.getMacAddresses());
        retVal.setPortIPs(networkElement.getMacAddresses());

        return retVal;
    }

    public InspectionPortEntity makeInspectionPortEntity(InspectionPortElement inspectionPortElement) {

        if (inspectionPortElement == null) {
            return null;
        }

        NetworkElementEntity ingressEntity = null;
        NetworkElementEntity egressEntity = null;

        NetworkElement ingress = inspectionPortElement.getIngressPort();
        if (ingress != null) {
            ingressEntity = makeNetworkElementEntity(ingress);
        }

        NetworkElement egress = inspectionPortElement.getEgressPort();

        if (egress != null) {
            if (ingressEntity != null && ingressEntity.getElementId().equals(egress.getElementId())) {
                egressEntity = ingressEntity;
            } else {
                egressEntity = makeNetworkElementEntity(egress);
            }
        }

        return new InspectionPortEntity(inspectionPortElement.getElementId(), ingressEntity, egressEntity, null);
    }


    public InspectionHookEntity makeInspectionHookEntity(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort, Long tag, TagEncapsulationType encType, Long order,
            FailurePolicyType failurePolicyType) {
        InspectionPortEntity inspectionPortEntity = makeInspectionPortEntity(inspectionPort);
        final String elementId = inspectedPort.getElementId();

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

        inspectionPortEntity.setInspectionHook(retVal);
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
            LOG.error(String.format("Finding Network Element %s ", elementId), e); // TODO
            return null;
        }
    }

    public InspectionPortEntity findInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        return this.txControl.required(() -> txInspPortByNetworkElements(ingress, egress));
    }

    private InspectionPortEntity txInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        String ingressId = ingress != null ? ingress.getElementId() : null;
        String egressId = ingress != null ? egress.getElementId() : null;

        Query q = this.em.createQuery("FROM InspectionPortEntity WHERE ingressId = :ingId AND egressId = :egId ");
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
                    egress.getElementId()), e); // TODO
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
            InspectionPortEntity inspectionPortEntity = inspectionHookEntity.getInspectionPort();

            inspectionHookEntity.setInspectionPort(null);
            inspectionPortEntity.setInspectionHook(null);
            inspectionHookEntity.setInspectedPort(null);
            networkElementEntity.setInspectionHook(null);
            this.em.remove(inspectionHookEntity);
            return null;
        });
    }

    private InspectionHookEntity txInspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        // Paranoid
        NetworkElement ingress = element != null ? element.getIngressPort() : null;
        NetworkElement egress = element != null ? element.getEgressPort() : null;

        String inspectedId = inspected != null ? inspected.getElementId() : null;

        InspectionPortEntity inspPort = findInspPortByNetworkElements(ingress, egress);

        String portId = inspPort != null ? inspPort.getElementId() : null;

        Query q = this.em.createQuery(
                "FROM InspectionHookEntity WHERE inspectedPortId = :inspectedId AND inspectionPortId = :inspectionId ");
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

}
