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

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.openstack4j.model.network.Port;
import org.osc.controller.nsc.api.openstack4j.BaseOpenstack4jApi;
import org.osc.controller.nsc.api.openstack4j.Endpoint;
import org.osc.controller.nsc.model.InspectionHook;
import org.osc.controller.nsc.model.InspectionPort;
import org.osc.sdk.controller.DefaultNetworkPort;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.exception.NetworkPortNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NeutronSecurityControllerApi extends BaseOpenstack4jApi {

    private enum PortType {
        INSPECTION("Inspection"), INSPECTED("Inspected");

        private String name;

        PortType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    Logger log = Logger.getLogger(NeutronSecurityControllerApi.class);

    public NeutronSecurityControllerApi(Endpoint endPoint) {
        super(endPoint);
    }

    public void test() throws Exception {
    }

    // Inspection port
    public InspectionPort getInspectionPort(String region, InspectionPortElement inspectionPort) throws Exception {
        String inspectionIngressPortId = inspectionPort.getIngressPort().getElementId();
        String inspectionEgressPortId = inspectionPort.getEgressPort().getElementId();

        this.log.info("Retrieving inspection port ingress: '" + inspectionIngressPortId + "' , egress: '" + inspectionEgressPortId + "'");

        Port ingressPort = getPortOrThrow(region, inspectionIngressPortId, PortType.INSPECTION);
        Port egressPort = ingressPort;

        if (!inspectionIngressPortId.equals(inspectionEgressPortId)) {
            egressPort = getPortOrThrow(region, inspectionEgressPortId, PortType.INSPECTION);
        }

        if (!InspectionPort.isRegistered(ingressPort)) {
            this.log.info("Inspection port ingress: '" + inspectionIngressPortId + "' not registered.");
            return null;
        }

        if (!InspectionPort.isRegistered(egressPort)) {
            this.log.info("Inspection port egress: '" + inspectionEgressPortId + "' not registered.");
            return null;
        }

        return new InspectionPort(
                new DefaultNetworkPort(inspectionIngressPortId, ingressPort.getMacAddress()),
                new DefaultNetworkPort(inspectionEgressPortId, egressPort.getMacAddress()));
    }

    public void addInspectionPort(String region, InspectionPortElement inspectionPort)
            throws Exception {
        String inspectionIngressPortId = inspectionPort.getIngressPort().getElementId();
        String inspectionEgressPortId = inspectionPort.getEgressPort().getElementId();

        this.log.info("Adding inspection port ingress: '" + inspectionIngressPortId + "' , egress: '"
                + inspectionEgressPortId + "'");

        Port ingressPort = getPortOrThrow(region, inspectionIngressPortId, PortType.INSPECTION);
        updateInspectionPortProfile(region, ingressPort, inspectionIngressPortId, inspectionEgressPortId);

        if (!inspectionIngressPortId.equals(inspectionEgressPortId)) {
            Port egressPort = getPortOrThrow(region, inspectionEgressPortId, PortType.INSPECTION);
            updateInspectionPortProfile(region, egressPort, inspectionIngressPortId, inspectionEgressPortId);
        }
    }

    // Inspection Hooks
    public InspectionHook getInspectionHookByPorts(String region, String inspectedPortId,
                                                   InspectionPortElement inspectionPort) throws Exception {
        String inspectionIngressPortId = inspectionPort.getIngressPort().getElementId();
        String inspectionEgressPortId = inspectionPort.getEgressPort().getElementId();
        List<InspectionHook> hooks = getInspectionHooksByPort(region, inspectedPortId);
        if (hooks != null) {
            for (InspectionHook hook : hooks) {
                if (isPortIdMatch(hook.getInspectionPort().getIngressPort(), inspectionIngressPortId)
                        && isPortIdMatch(hook.getInspectionPort().getEgressPort(), inspectionEgressPortId)) {
                    return hook;
                }
            }
        }
        return null;
    }

    public void deleteInspectionHookByPorts(String region, String inspectedPortId, InspectionPortElement inspectionPort)
            throws Exception {
        InspectionHook inspectionHook = getInspectionHookByPorts(region, inspectedPortId, inspectionPort);
        if (inspectionHook != null) {
            deleteInspectionHook(region, inspectionHook.getInspectedPortId());
        }
    }

    public List<InspectionHook> getInspectionHooksByPort(String region, String inspectedPortId) throws Exception {
        this.log.info("Retriving inspection hook for port '" + inspectedPortId + "'");

        Port inspectedPort = getPortOrThrow(region, inspectedPortId, PortType.INSPECTED);

        InspectionHook inspectionHook = InspectionHook.generateInspectionHookFromPort(inspectedPort);

        if (inspectionHook == null) {
            this.log.info("Inspection hook for port '" + inspectedPortId + "' not registered.");
            return null;
        }

        return Arrays.asList(inspectionHook);
    }

    public InspectionHook getInspectionHook(String region, String inspectedPortId) throws Exception {
        this.log.info("Retriving inspection hook '" + inspectedPortId + "'");

        Port port = getPortOrThrow(region, inspectedPortId, PortType.INSPECTED);

        InspectionHook inspectionHook = InspectionHook.generateInspectionHookFromPort(port);
        if (inspectionHook == null) {
            throw new Exception(String.format("Port with Id: '%s' is not an inspection hook", inspectedPortId));
        }
        return inspectionHook;
    }

    public void addInspectionHook(String region, InspectionHook inspectionHook)
            throws NetworkPortNotFoundException, Exception {
        this.log.info("Adding inspection hook '" + inspectionHook + "'");

        updateInspectionHookProfile(region, inspectionHook, true);

    }

    public void updateInspectionHook(String region, InspectionHook inspectionHook)
            throws NetworkPortNotFoundException, Exception {
        this.log.info("Updating inspection hook '" + inspectionHook + "'");

        updateInspectionHookProfile(region, inspectionHook, false);
    }

    public void deleteInspectionHook(String region, String inspectedPortId) throws Exception {
        this.log.info("Deleting inspection hook for port '" + inspectedPortId + "'");

        Port inspectedPort = getPortOrThrow(region, inspectedPortId, PortType.INSPECTED);

        if (!InspectionHook.isInspectionHookRegistered(inspectedPort)) {
            this.log.info(
                    String.format("Inspected port with Id: '%s' does not an have inspection hook", inspectedPortId));
            return;
        }

        try {
            getOs().useRegion(region);
            ImmutableMap<String, Object> bindingProfile = InspectionHook.removeBindingProfile(inspectedPort.getProfile());
            getOs().networking().port().update(inspectedPort.toBuilder().profile(bindingProfile).build());
        } finally {
            getOs().removeRegion();
        }
    }

    /**
     * return true if port id in the port matches the port id provided
     */
    private boolean isPortIdMatch(NetworkElement port, String portId) {
        return port != null && portId.equals(port.getElementId());
    }

    private void updateInspectionPortProfile(String region, Port portProfileToUpdate, String portId, String egressPortId) {
        if (InspectionPort.isRegistered(portProfileToUpdate)) {
            this.log.info("Inspection port '" + portProfileToUpdate.getId() + "' already registered");
        } else {
            try {
                getOs().useRegion(region);
                ImmutableMap<String, Object> bindingProfile = InspectionPort.updateBindingProfile(portId, egressPortId,
                        portProfileToUpdate.getProfile());
                getOs().networking().port().update(portProfileToUpdate.toBuilder().profile(bindingProfile).build());
            } finally {
                getOs().removeRegion();
            }
        }
    }

    private void updateInspectionHookProfile(String region, InspectionHook inspectionHook, boolean generateId)
            throws Exception {

        String inspectionIngressPortId = inspectionHook.getInspectionPort().getIngressPort().getElementId();
        String inspectionEgressPortId = inspectionHook.getInspectionPort().getEgressPort().getElementId();

        Port inspectedPort = getPortOrThrow(region, inspectionHook.getInspectedPortId(), PortType.INSPECTED);
        Port inspectionIngressPort = getPortOrThrow(region, inspectionIngressPortId, PortType.INSPECTION);

        Port inspectionEgressPort;
        if (inspectionIngressPortId.equals(inspectionEgressPortId)) {
            inspectionEgressPort = inspectionIngressPort;
        } else {
            inspectionEgressPort = getPortOrThrow(region, inspectionEgressPortId, PortType.INSPECTION);
        }

        if (!InspectionPort.isRegistered(inspectionIngressPort) && !InspectionPort.isRegistered(inspectionEgressPort)) {
            throw new Exception(
                    String.format("Port with Id: '%s' is not an inspection port", inspectionHook.getInspectionPort()));
        }

        if (generateId) {
            inspectionHook.setHookId(UUID.randomUUID().toString());
        }

        try {
            getOs().useRegion(region);
            ImmutableMap<String, Object> bindingProfile = InspectionHook.updateBindingProfile(inspectionHook, inspectedPort.getProfile());
            getOs().networking().port().update(inspectedPort.toBuilder().profile(bindingProfile).build());
        } finally {
            getOs().removeRegion();
        }

    }

    private Port getPortOrThrow(String region, String portId, PortType type) throws NetworkPortNotFoundException {
        getOs().useRegion(region);
        Port port;
        try {
            port = getOs().networking().port().get(portId);
        } finally {
            getOs().removeRegion();
        }
        if (port == null) {
            throw new NetworkPortNotFoundException(portId, String.format("%s Port with Id: '%s' not found", type, portId));
        }
        return port;
    }

}
