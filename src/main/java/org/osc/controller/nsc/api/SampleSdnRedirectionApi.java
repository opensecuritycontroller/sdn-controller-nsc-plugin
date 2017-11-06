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

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.entities.PortGroupEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSdnRedirectionApi implements SdnRedirectionApi {

    private static final Logger LOG = LoggerFactory.getLogger(SampleSdnRedirectionApi.class);

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

        // Null args -> warning on read, error on update, exception on create
        if (inspectedPort == null) {
            LOG.warn("Attempt to find an Inspection Hook with null Inspected Port");
            return null;
        }
        if (inspectionPort == null) {
            LOG.warn("Attempt to find an Inspection Hook with null Inspection Port");
            return null;
        }

        try {
            return this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
        } catch (Exception e) {
            LOG.error("Exception finding Network Element (Inspected {} ; Inspection Port {}):", inspectedPort,
                    inspectionPort, e);
            return null;
        }
    }

    @Override
    public String installInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            Long tag, TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {

        if (inspectedPort == null) {
            throw new IllegalArgumentException("Attempt to install an Inspection Hook with no Inspected Port");
        }
        if (inspectionPort == null) {
            throw new IllegalArgumentException("Attempt to install an Inspection Hook with null Inspection Port");
        }

        LOG.info("Installing Inspection Hook for (Inspected {} ; Inspection Port {}):", inspectedPort, inspectionPort);
        LOG.info("Tag: {}; EncType: {}; Order: {}, Fail Policy: {}", tag, encType, order, failurePolicyType);

        InspectionHookEntity retValEntity = this.txControl.required(() -> {
            InspectionPortEntity dbInspectionPort = (InspectionPortEntity) getInspectionPort(inspectionPort);
            this.utils.throwExceptionIfNullEntity(dbInspectionPort, inspectionPort);

            InspectionHookEntity inspectionHookEntity = this.utils.findInspHookByInspectedAndPort(inspectedPort, dbInspectionPort);

            if (inspectionHookEntity == null) {
                inspectionHookEntity = this.utils.makeInspectionHookEntity(inspectedPort, dbInspectionPort, tag,
                        encType, order, failurePolicyType);
            } else {

                inspectionHookEntity.setEncType(encType);
                inspectionHookEntity.setOrder(order);
                inspectionHookEntity.setFailurePolicyType(failurePolicyType);
                inspectionHookEntity.setTag(tag);
            }

            return this.em.merge(inspectionHookEntity);
        });

        return retValEntity.getHookId();
    }

    @Override
    public void removeInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
        if (inspectedPort == null) {
            LOG.warn("Attempt to remove an Inspection Hook with no Inspected Port");
            return;
        }
        if (inspectionPort == null) {
            LOG.warn("Attempt to remove an Inspection Hook with null Inspection Port");
            return;
        }

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort,
                    inspectionPort);
            if (inspectionHook != null) {
                this.utils.removeSingleInspectionHook(inspectionHook.getHookId());
            } else {
                LOG.warn(
                        "Attempt to remove nonexistent inspection hook for Inspected {} and Inspection Port {}",
                        inspectedPort, inspectionPort);
            }

            return null;
        });
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
        if (inspectedPort == null) {
            LOG.error("Attempt to modify an Inspection Hook with null Inspected Port");
            return;
        }
        if (inspectionPort == null) {
            LOG.error("Attempt to modify an Inspection Hook with null Inspection Port");
            return;
        }

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            if (inspectionHook != null) {
                inspectionHook.setTag(tag);
            } else {
                LOG.warn("Attempt to modify nonexistent inspection hook for inspected port {} and inspection port {}",
                        inspectedPort, inspectionPort);
            }

            return null;
        });
    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort) throws Exception {
        if (inspectedPort == null) {
            LOG.warn("Attempt to find an Inspection Hook with null Inspected Port");
            return null;
        }
        if (inspectionPort == null) {
            LOG.warn("Attempt to find an Inspection Hook with null Inspection Port");
            return null;
        }

        InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getFailurePolicyType();
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws Exception {
        if (inspectedPort == null) {
            LOG.error("Attempt to modify an Inspection Hook with null Inspected Port");
            return;
        }
        if (inspectionPort == null) {
            LOG.error("Attempt to modify an Inspection Hook with null Inspection Port");
            return;
        }

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            if (inspectionHook != null) {
                inspectionHook.setFailurePolicyType(failurePolicyType);
            }  else {
                LOG.warn("Attempt to modify nonexistent inspection hook for inspected port {} and inspection port {}",
                        inspectedPort, inspectionPort);
            }

            return null;
        });
    }

    @Override
    public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {
        if (inspectedPort == null) {
            LOG.warn("Attempt to remove Inspection Hooks for null Inspected Port");
            return;
        }
        if (inspectedPort.getElementId() == null) {
            LOG.warn("Attempt to remove Inspection Hooks for Inspected Port with no id");
            return;
        }

        String inspectedId = inspectedPort.getElementId();

        this.txControl.required(() -> {
            List<InspectionHookEntity> results = this.utils.txInspectionHookEntitiesByInspected(inspectedId);

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
            LOG.warn("Attempt to find null InspectionPort");
            return null;
        }

        String portId = inspectionPort.getElementId();

        if (portId != null) {
            try {
                return this.txControl.required(() -> {
                    return this.utils.txInspectionPortEntityById(portId);
                });
            } catch (Exception e) {
                LOG.warn("Failed to retrieve InspectionPort by id! Trying by ingress and egress");
            }
        } else {
            LOG.warn("Failed to retrieve InspectionPort by id! Trying by ingress and egress");
        }

        NetworkElement ingress = inspectionPort.getIngressPort();
        NetworkElement egress = inspectionPort.getEgressPort();

        return this.utils.findInspPortByNetworkElements(ingress, egress);
    }

    @Override
    public Element registerInspectionPort(InspectionPortElement inspectionPort) throws Exception {
        if (inspectionPort == null) {
            throw new IllegalArgumentException("Attempt to register null InspectionPort");
        }

        return this.txControl.required(() -> {

            // must be within this transaction, because if the DB retrievals inside makeInspectionPortEntry
            // are inside the required() call themselves. That makes them a part of a separate transaction

            InspectionPortEntity inspectionPortEntity = (InspectionPortEntity) getInspectionPort(inspectionPort);

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
        if (inspectedPort == null) {
            LOG.error("Attempt to modify an Inspection Hook with null Inspected Port");
            return;
        }
        if (inspectionPort == null) {
            LOG.error("Attempt to modify an Inspection Hook with null Inspection Port");
            return;
        }

        this.txControl.required(() -> {
            InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
            if (inspectionHook != null) {
                inspectionHook.setOrder(order);
            } else {
                LOG.warn("Attempt to modify nonexistent Inspection Hook for Inspected port {} and Inspection Port {}",
                        inspectedPort, inspectionPort);
            }

            return null;
        });
    }

    @Override
    public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
        if (inspectedPort == null) {
            LOG.warn("Attempt to find an Inspection Hook with null Inspected Port");
            return null;
        }
        if (inspectionPort == null) {
            LOG.warn("Attempt to find an Inspection Hook with null Inspection Port");
            return null;
        }

        InspectionHookEntity inspectionHook = this.utils.findInspHookByInspectedAndPort(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getOrder();
    }

    @Override
    public void updateInspectionHook(InspectionHookElement existingInspectionHook) throws Exception {
        if (existingInspectionHook == null) {
            throw new IllegalArgumentException("Attempt to update a null Inspection Hook!");
        }

        NetworkElement inspected = existingInspectionHook.getInspectedPort();
        InspectionPortElement inspectionPort = existingInspectionHook.getInspectionPort();

        Long tag = existingInspectionHook.getTag();
        Long order = existingInspectionHook.getOrder();
        TagEncapsulationType encType = existingInspectionHook.getEncType();
        FailurePolicyType failurePolicyType = existingInspectionHook.getFailurePolicyType();

        installInspectionHook(inspected, inspectionPort, tag, encType, order, failurePolicyType);
    }

    @Override
    public NetworkElement getNetworkElementByDeviceOwnerId(String deviceOwnerId) throws Exception {
        // For ports belonging to a security group we should find an exact match (pre-populated in the plugin DB using information from kubernetes)
        NetworkElement devicePort = this.utils.findNetworkElementEntityByDeviceOwnerId(deviceOwnerId);

        if (devicePort != null) {
            return devicePort;
        }

        // If did not find an exact match we will try to find the ports assigned to pods using the prefix of the id.
        // This prefix is the deployment spec name in OSC which is easy to predict and therefore pre-populate in the plugin DB to facilitate demos.
        String[] deviceOwnerIdParts = deviceOwnerId.split("-");

        String deviceOwnerPrefixId = deviceOwnerIdParts[0];
        String deviceId = deviceOwnerIdParts[deviceOwnerIdParts.length - 1];

        List<NetworkElementEntity> devicePorts = this.utils.findNetworkElementEntitiesByDeviceOwnerPrefixId(deviceOwnerPrefixId);

        if (!devicePorts.isEmpty()) {
            // We will return any port that matches this prefix, we are using a mod hash here to add some level of randomness.
            int index = Math.abs(deviceId.hashCode() % devicePorts.size());
            devicePort = devicePorts.get(index);
            LOG.info("Returning device port index {} id {}", index, devicePort.getElementId());
        }

        return devicePort;
    }

    @Override
    public NetworkElement registerNetworkElement(List<NetworkElement> inspectedPorts) throws Exception {
        if (!RedirectionApiUtils.supportsPortGroup()) {
            throw new UnsupportedOperationException("Registering a network element is only supported for SDN supporting port group.");
        }

        PortGroupEntity newPortGroupEntity = this.utils.makePortGroupEntity(inspectedPorts);
        return this.txControl.required(() -> this.em.merge(newPortGroupEntity));
    }

    @Override
    public NetworkElement updateNetworkElement(NetworkElement portGroup, List<NetworkElement> inspectedPorts)
            throws Exception {
        if (!RedirectionApiUtils.supportsPortGroup()) {
            throw new UnsupportedOperationException("Updating a network element is only supported for SDN supporting port group.");
        }

        this.utils.throwExceptionIfNullElement(portGroup, "Null passed for the portGroup paramater");

        return this.txControl.required(() -> {
            PortGroupEntity portGroupEntity = this.utils.txPortGroupEntity(portGroup.getElementId(), portGroup.getParentId());

            if (portGroupEntity == null) {
                throw new IllegalArgumentException(
                        String.format("No port group entity found for the id '%s' and parentId %s",
                                portGroup.getElementId(), portGroup.getParentId()));
            }

            portGroupEntity.setVirtualPorts(this.utils.networkElementsToEntities(inspectedPorts));
            return this.em.merge(portGroupEntity);
        });
    }

    @Override
    public void deleteNetworkElement(NetworkElement portGroup) throws Exception {
        if (!RedirectionApiUtils.supportsPortGroup()) {
            throw new UnsupportedOperationException("Deleting a network element is only supported for SDN supporting port group.");
        }

        this.txControl.required(() -> {
            PortGroupEntity portGroupEntity = this.utils.txPortGroupEntity(portGroup.getElementId(), portGroup.getParentId());

            if (portGroupEntity == null) {
                LOG.info(
                        "No port group entity found for the id '{}' and parentId {} while deleting a port group. Noop.",
                        portGroup.getElementId(), portGroup.getParentId());
                return null;
            }

            portGroupEntity.getVirtualPorts().forEach(virtualPort -> virtualPort.setPortGroup(null));
            portGroupEntity.getVirtualPorts().clear();
            this.em.remove(portGroupEntity);

            return null;
        });
    }

    @Override
    public List<NetworkElement> getNetworkElements(NetworkElement element) throws Exception {
        if (!RedirectionApiUtils.supportsPortGroup()) {
            throw new UnsupportedOperationException("Retrieving the network elements is only supported for SDN supporting port group.");
        }

        // TODO emanoel: This method is currently not referenced by OSC.
        return null;
    }

    @Override
    public void removeInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        if (inspectionPort == null) {
            LOG.warn("Attempt to remove a null Inspection Port");
            return;
        }

        InspectionPortElement foundInspectionPort = getInspectionPort(inspectionPort);

        if (foundInspectionPort != null) {
            this.utils.removeSingleInspectionPort(foundInspectionPort.getElementId());
        } else {
            NetworkElement ingress = inspectionPort.getIngressPort();
            NetworkElement egress = inspectionPort.getEgressPort();

            LOG.warn("Attempt to remove nonexistent Inspection Port for ingress {} and egress {}", ingress, egress);
        }
    }

    @Override
    public void removeInspectionHook(String inspectionHookId) throws Exception {
        if (inspectionHookId == null) {
            LOG.warn("Attempt to remove an Inspection Hook with null id");
            return;
        }

        this.utils.removeSingleInspectionHook(inspectionHookId);
    }

    @Override
    public InspectionHookElement getInspectionHook(String inspectionHookId) throws Exception {
        if (inspectionHookId == null) {
            LOG.warn("Attempt to get Inspection Hook with null id");
            return null;
        }

        return this.txControl.required(() -> this.em.find(InspectionHookEntity.class, inspectionHookId));
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing connection to the database");
        this.txControl.required(() -> {
            this.em.close();
            return null;
        });
    }

    // Support methods for REST API's

    public List<String> getInspectionHooksIds() throws Exception {

        return this.txControl.supports(() -> {
            return this.utils.txInspectionHookEntities().stream().map(InspectionHookEntity::getHookId)
                    .collect(Collectors.toList());
        });
    }

    public Element updateInspectionPort(InspectionPortEntity inspectionPort) throws Exception {

        InspectionPortEntity inspectionPortEntity = (InspectionPortEntity) getInspectionPort(inspectionPort);
        this.utils.throwExceptionIfNullElement(inspectionPortEntity);

        if (inspectionPort.getIngressPort().equals(null)
                || inspectionPort.getIngressPort().getElementId().equals(null)) {
            this.utils.throwExceptionIfNullElement(inspectionPort.getIngressPort(), "Ingress port is null");
        }
        if (inspectionPort.getEgressPort().equals(null) || inspectionPort.getEgressPort().getElementId().equals(null)) {
            this.utils.throwExceptionIfNullElement(inspectionPort.getEgressPort(), "Egress port is null");
        }

        this.utils.throwExceptionIfIdMismatch(inspectionPortEntity.getEgressPort().getElementId(),
                inspectionPort.getEgressPort().getElementId());
        this.utils.throwExceptionIfIdMismatch(inspectionPortEntity.getIngressPort().getElementId(),
                inspectionPort.getIngressPort().getElementId());

        return this.txControl.required(() -> {
            inspectionPortEntity.setEgressPort(inspectionPort.getEgressPort());
            inspectionPortEntity.setIngressPort(inspectionPort.getIngressPort());

            return this.em.merge(inspectionPortEntity);
        });
    }

    public List<String> getInspectionPortsIds() throws Exception {

        return this.txControl.supports(() -> {
            return this.utils.txInspectionPortEntities().stream().map(InspectionPortEntity::getElementId)
                    .collect(Collectors.toList());
        });
    }

    public NetworkElement registerPort(NetworkElementEntity nwElement) throws Exception {

        NetworkElementEntity element = this.utils.findNetworkElementEntityByElementId(nwElement.getElementId());
        if (element != null) {
            String msg = String.format("Network element already exists id: %s\n", nwElement.getElementId());
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.txControl.required(() -> {
            NetworkElementEntity portElement = new NetworkElementEntity();
            portElement.setElementId(nwElement.getElementId());
            portElement.setMacAddresses(nwElement.getMacAddresses());
            portElement.setPortIPs(nwElement.getPortIPs());
            portElement.setDeviceOwnerId(nwElement.getDeviceOwnerId());
            portElement.setParentId(nwElement.getParentId());

            this.em.merge(portElement);

            return portElement;
        });
    }

    public NetworkElementEntity updatePort(NetworkElementEntity nwElement) throws Exception {

        NetworkElementEntity element = this.utils.findNetworkElementEntityByElementId(nwElement.getElementId());
        this.utils.throwExceptionIfNullElement(element, "Port Element does not exists");

        return this.txControl.required(() -> {
            element.setMacAddresses(nwElement.getMacAddresses());
            element.setPortIPs(nwElement.getPortIPs());
            element.setDeviceOwnerId(nwElement.getDeviceOwnerId());
            element.setParentId(nwElement.getParentId());

            this.em.merge(element);

            return element;
        });
    }

    public void deletePort(String id) throws Exception {

        this.txControl.required(() -> {
            NetworkElementEntity element = this.utils.txNetworkElementEntityByElementId(id);

            if (element == null) {
                LOG.warn("Attempt to delete network element for id {} and network element not found, no-op.", id);
                return null;
            }

            this.em.remove(element);
            return null;
        });
    }

    public List<String> getPortIds() throws Exception {

        return this.txControl.supports(() -> {
            return this.utils.txNetworkElementEntities().stream().map(NetworkElementEntity::getElementId)
                    .collect(Collectors.toList());
        });
    }

    public NetworkElementEntity getPort(String id) throws Exception {

        return this.txControl.supports(() -> {

            return this.utils.txNetworkElementEntityByElementId(id);
        });
    }
}
