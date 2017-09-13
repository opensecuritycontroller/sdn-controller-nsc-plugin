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
package org.osc.controller.nsc.api;

import static java.util.Arrays.asList;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.api.SdnRedirectionApi;
import org.osc.sdk.controller.element.Element;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.exception.NetworkPortNotFoundException;
import org.osgi.service.transaction.control.TransactionControl;

public class SampleSdnRedirectionApi implements SdnRedirectionApi {

    private static final Logger LOG = Logger.getLogger(SampleSdnRedirectionApi.class);

    private TransactionControl txControl;
    private EntityManager em;
    private RedirectionApiUtils utils;

    public SampleSdnRedirectionApi() {
    }

    public SampleSdnRedirectionApi(TransactionControl txControl,
            EntityManager em) {
        this.txControl = txControl;
        this.em = em;
        this.utils = new RedirectionApiUtils(em, txControl);
    }

    @Override
    public InspectionHookElement getInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
        try {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            return inspectionHook;
        } catch (Exception e) {
            LOG.error(String.format("Exception finding network Element (inspected %s ; inspectionPort %s) :", "" + inspectedPort,
                    "" + inspectionPort), e);
            return null;
        }
    }

    @Override
    public String installInspectionHook(List<NetworkElement> inspectedPorts, InspectionPortElement inspectionPort,
            Long tag, TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
            throws NetworkPortNotFoundException, Exception {

        if (inspectedPorts == null || inspectedPorts.size() == 0) {
            throw new IllegalArgumentException("Attempt to install an InspectionHook with no InspectedPort. ");
        }
        if (inspectionPort == null) {
            throw new IllegalArgumentException("Attempt to install an InspectionHook with null InspectionPort. ");
        }
        if (inspectedPorts.size() > 1) {
            throw new IllegalArgumentException("Attempt to install an InspectionHook with multiple inspected ports. ");
        }

        NetworkElement inspected = inspectedPorts.get(0);

        LOG.info(String.format("Installing inspection hook for inspected port %s\n inspection port %s", inspected,
                                inspectionPort));
        LOG.info(String.format("Tag: %d; EncType: %s; Order: %d, Fail Policy: %s", tag, encType, order, failurePolicyType));

        InspectionHookEntity retValEntity = this.txControl.required(() -> {
            InspectionPortEntity dbInspectionPort =(InspectionPortEntity) getInspectionPort(inspectionPort);
            this.utils.throwExceptionIfNullEntity(dbInspectionPort, inspectionPort);

            InspectionHookEntity inspectionHookEntity = this.utils.findInspHookByInspectedAndPort(inspected, dbInspectionPort);

            if (inspectionHookEntity == null) {
                inspectionHookEntity = this.utils.makeInspectionHookEntity(inspected, dbInspectionPort, tag,
                                                                           encType, order, failurePolicyType);
            }

            return this.em.merge(inspectionHookEntity);
        });

        return retValEntity.getHookId();
    }

    @Override
    public void removeInspectionHook(List<NetworkElement> inspectedPorts, InspectionPortElement inspectionPort)
            throws Exception {

        if (inspectedPorts != null && inspectedPorts.size() > 0) {

            NetworkElement inspectedPort = inspectedPorts.get(0);

            this.txControl.required(() -> {
                InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
                if (inspectionHook != null) {
                    this.em.remove(inspectionHook);
                } else {
                    LOG.warn(String.format("Attempt to remove nonexistent inspection hook for inspected port %s and inspection port %s",
                            inspectedPort, inspectionPort));
                }

                return null;
            });
        }
    }

    @Override
    public Long getInspectionHookTag(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getTag();
    }

    @Override
    public void setInspectionHookTag(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long tag)
            throws Exception {

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            if (inspectionHook != null) {
                inspectionHook.setTag(tag);
            } else {
                LOG.warn(String.format("Attempt to modify nonexistent inspection hook for inspected port %s and inspection port %s",
                        inspectedPort, inspectionPort));
            }

            return null;
        });
    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort) throws Exception {
        InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getFailurePolicyType();
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws Exception {

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            if (inspectionHook != null) {
                inspectionHook.setFailurePolicyType(failurePolicyType);
            }  else {
                LOG.warn(String.format("Attempt to modify nonexistent inspection hook for inspected port %s and inspection port %s",
                        inspectedPort, inspectionPort));
            }


            return null;
        });
    }

    @Override
    public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {

        this.txControl.required(() -> {
            Query q = this.em.createQuery("FROM InspectionHookEntity WHERE inspected_port_fk = :portId");
            String portId = (inspectedPort != null ? inspectedPort.getElementId() : null);
            q.setParameter("portId", portId);

            @SuppressWarnings("unchecked")
            List<InspectionHookEntity> results = q.getResultList();

            for (InspectionHookEntity inspectionHookEntity : results) {
                this.utils.removeSingleInspectionHook(inspectionHookEntity.getHookId());
            }

            return null;
        });

    }

    // Inspection port methods
    @Override
    public InspectionPortElement getInspectionPort(InspectionPortElement inspectionPort) throws Exception {

        if (inspectionPort == null) {
            return null;
        }
        String portId = inspectionPort.getElementId();

        if (portId != null) {
            try {
                return this.txControl.required(() -> {
                    InspectionPortEntity ipEntity = this.utils.txInspectionPortEntityById(portId);
                    return ipEntity;
                });
            } catch (Exception e) {
                LOG.warn("Failed to retrieve inspectionPort by id! Trying by ingress and egress.");
            }
        }

        NetworkElement ingress = inspectionPort.getIngressPort();
        NetworkElement egress = inspectionPort.getEgressPort();

        InspectionPortEntity ipEntity = this.utils.findInspPortByNetworkElements(ingress, egress);
        return ipEntity;
    }

    @Override
    public Element registerInspectionPort(InspectionPortElement inspectionPort) throws Exception {

        return this.txControl.required(() -> {

            // must be within this transaction, because if the DB retrievals inside makeInspectionPortEntry
            // are inside the required() call themselves. That makes them a part of a separate transaction

            NetworkElement ingress = inspectionPort.getIngressPort();
            NetworkElement egress = inspectionPort.getEgressPort();
            InspectionPortEntity inspectionPortEntity = this.utils.findInspPortByNetworkElements(ingress, egress);

            if (inspectionPortEntity == null) {
                inspectionPortEntity = this.utils.makeInspectionPortEntity(inspectionPort);
            }

            inspectionPortEntity = this.em.merge(inspectionPortEntity);

            return inspectionPortEntity;
        });
    }

    @Override
    public void setInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long order)
            throws Exception {

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            if (inspectionHook != null) {
                inspectionHook.setOrder(order);
            } else {
                LOG.warn(String.format("Attempt to modify nonexistent inspection hook for inspected port %s and inspection port %s",
                        inspectedPort, inspectionPort));
            }

            return null;
        });
    }

    @Override
    public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
        InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getOrder();
    }

    @Override
    public void updateInspectionHook(InspectionHookElement existingInspectionHook) throws Exception {

        if (existingInspectionHook == null) {
            throw new IllegalArgumentException("Attempt to uptdate null inspection hook!");
        }

        NetworkElement inspected = existingInspectionHook.getInspectedPort();
        InspectionPortElement inspectionPort = existingInspectionHook.getInspectionPort();

        Long tag = existingInspectionHook.getTag();
        Long order = existingInspectionHook.getOrder();
        TagEncapsulationType encType = existingInspectionHook.getEncType();
        FailurePolicyType failurePolicyType = existingInspectionHook.getFailurePolicyType();

        installInspectionHook(asList(inspected), inspectionPort, tag, encType, order, failurePolicyType);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing connection to the database");
        this.txControl.required(() -> {
            this.em.close();
            return null;
        });
    }

    @Override
    public NetworkElement registerNetworkElement(List<NetworkElement> inspectedPorts) throws Exception {
        return null;
    }

    @Override
    public NetworkElement updateNetworkElement(NetworkElement portGroup, List<NetworkElement> inspectedPorts)
            throws Exception {
        // no-op
        return null;
    }

    @Override
    public void deleteNetworkElement(NetworkElement portGroupId) throws Exception {
        // no-op
    }

    @Override
    public List<NetworkElement> getNetworkElements(NetworkElement element) throws Exception {
        return null;
    }

    @Override
    public void removeInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        if (inspectionPort == null) {
            throw new IllegalArgumentException("Attempt to delete null inspection port!");
        }

        InspectionPortElement foundInspectionPort = getInspectionPort(inspectionPort);

        if (foundInspectionPort != null) {
            this.utils.removeSingleInspectionPort(foundInspectionPort.getElementId());
        } else {
            NetworkElement ingress = inspectionPort.getIngressPort();
            NetworkElement egress = inspectionPort.getEgressPort();
            String ingressId = ingress != null ? ingress.getElementId() : null;
            String egressId = ingress != null ? egress.getElementId() : null;

            LOG.warn(String.format("Attempt to remove nonexistent inspection port for ingress %s and egress %s",
                                   ingressId, egressId));
        }
    }

    @Override
    public void removeInspectionHook(String inspectionHookId) throws Exception {
        this.utils.removeSingleInspectionHook(inspectionHookId);
    }

    @Override
    public InspectionHookElement getInspectionHook(String inspectionHookId) throws Exception {
        // TODO emanoel: Currently not needed but should be implemented also for
        // NSC.
        throw new NotImplementedException("Not expected to be called for NSC. "
                + "Currently only called for SDN controllers that support port group.");
    }
}
