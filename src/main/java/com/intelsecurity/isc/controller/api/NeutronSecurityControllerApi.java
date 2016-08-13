package com.intelsecurity.isc.controller.api;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.Port;
import org.jclouds.openstack.neutron.v2.domain.Port.UpdatePort;
import org.jclouds.openstack.neutron.v2.features.PortApi;

import com.intelsecurity.isc.controller.api.jcloud.BaseJCloudApi;
import com.intelsecurity.isc.controller.api.jcloud.Endpoint;
import com.intelsecurity.isc.controller.api.jcloud.JCloudUtil;
import com.intelsecurity.isc.controller.model.InspectionHook;
import com.intelsecurity.isc.controller.model.InspectionPort;
import com.intelsecurity.isc.plugin.controller.DefaultNetworkPort;
import com.intelsecurity.isc.plugin.controller.element.InspectionPortElement;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;
import com.intelsecurity.isc.plugin.controller.exception.NetworkPortNotFoundException;

public class NeutronSecurityControllerApi extends BaseJCloudApi {

    private static enum PortType {
        INSPECTION("Inspection"), INSPECTED("Inspected");

        private String name;

        private PortType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    Logger log = Logger.getLogger(NeutronSecurityControllerApi.class);

    static final String OPENSTACK_SERVICE_NEUTRON = "openstack-neutron";
    private NeutronApi neutronApi;

    public NeutronSecurityControllerApi(Endpoint endPoint) {
        super(endPoint);
        this.neutronApi = JCloudUtil.buildApi(NeutronApi.class, OPENSTACK_SERVICE_NEUTRON, endPoint);
    }

    public void test() throws Exception {
        this.neutronApi.getConfiguredRegions();
    }

    // Inspection port
    public InspectionPort getInspectionPort(String region, InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        String inspectionIngressPortId = inspectionPort.getIngressPort().getPortId();
        String inspectionEgressPortId = inspectionPort.getEgressPort().getPortId();

        this.log.info("Retriving inspection port ingress: '" + inspectionIngressPortId + "' , egress: '"
                + inspectionEgressPortId + "'");
        PortApi neutronPortApi = this.neutronApi.getPortApi(region);

        Port ingressPort = getPortOrThrow(neutronPortApi, inspectionIngressPortId, PortType.INSPECTION);
        Port egressPort = ingressPort;

        if (!inspectionIngressPortId.equals(inspectionEgressPortId)) {
            egressPort = getPortOrThrow(neutronPortApi, inspectionEgressPortId, PortType.INSPECTION);
        }

        if (!InspectionPort.isRegistered(ingressPort)) {
            this.log.info("Inspection port ingress: '" + inspectionIngressPortId + "' not registered.");
            return null;
        }

        if (!InspectionPort.isRegistered(egressPort)) {
            this.log.info("Inspection port egress: '" + inspectionEgressPortId + "' not registered.");
            return null;
        }

        InspectionPort validatedInspectionPort = new InspectionPort(
                new DefaultNetworkPort(inspectionIngressPortId, ingressPort.getMacAddress()),
                new DefaultNetworkPort(inspectionEgressPortId, egressPort.getMacAddress()));

        return validatedInspectionPort;
    }

    public void addInspectionPort(String region, InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        String inspectionIngressPortId = inspectionPort.getIngressPort().getPortId();
        String inspectionEgressPortId = inspectionPort.getEgressPort().getPortId();

        this.log.info("Adding inspection port ingress: '" + inspectionIngressPortId + "' , egress: '"
                + inspectionEgressPortId + "'");
        PortApi neutronPortApi = this.neutronApi.getPortApi(region);

        Port ingressPort = getPortOrThrow(neutronPortApi, inspectionIngressPortId, PortType.INSPECTION);
        updateInspectionPortProfile(ingressPort, neutronPortApi);

        if (!inspectionIngressPortId.equals(inspectionEgressPortId)) {
            Port egressPort = getPortOrThrow(neutronPortApi, inspectionEgressPortId, PortType.INSPECTION);
            updateInspectionPortProfile(egressPort, neutronPortApi);
        }
    }

    // Inspection Hooks
    public InspectionHook getInspectionHookByPorts(String region, String inspectedPortId,
            InspectionPortElement inspectionPort) throws NetworkPortNotFoundException, Exception {
        String inspectionIngressPortId = inspectionPort.getIngressPort().getPortId();
        String inspectionEgressPortId = inspectionPort.getEgressPort().getPortId();
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
            throws NetworkPortNotFoundException, Exception {
        InspectionHook inspectionHook = getInspectionHookByPorts(region, inspectedPortId, inspectionPort);
        if (inspectionHook != null) {
            deleteInspectionHook(region, inspectionHook.getInspectedPortId());
        }
    }

    public List<InspectionHook> getInspectionHooksByPort(String region, String inspectedPortId)
            throws NetworkPortNotFoundException, Exception {
        this.log.info("Retriving inspection hook for port '" + inspectedPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port inspectedPort = getPortOrThrow(neutronPortApi, inspectedPortId, PortType.INSPECTED);

        InspectionHook inspectionHook = InspectionHook.generateInspectionHookFromPort(inspectedPort);

        if (inspectionHook == null) {
            this.log.info("Inspection hook for port '" + inspectedPortId + "' not registered.");
            return null;
        }

        return Arrays.asList(inspectionHook);
    }

    public InspectionHook getInspectionHook(String region, String inspectedPortId)
            throws NetworkPortNotFoundException, Exception {
        this.log.info("Retriving inspection hook '" + inspectedPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port port = getPortOrThrow(neutronPortApi, inspectedPortId, PortType.INSPECTED);

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

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port inspectedPort = getPortOrThrow(neutronPortApi, inspectedPortId, PortType.INSPECTED);

        if (!InspectionHook.isInspectionHookRegistered(inspectedPort)) {
            this.log.info(
                    String.format("Inspected port with Id: '%s' does not an have inspection hook", inspectedPortId));
            return;
        }

        UpdatePort updatedPort = Port.updateBuilder()
                .profile(InspectionHook.removeBindingProfile(inspectedPort.getProfile())).build();
        neutronPortApi.update(inspectedPortId, updatedPort);
    }

    @Override
    protected List<? extends Closeable> getApis() {
        return Arrays.asList(this.neutronApi);
    }

    /**
     * return true if port id in the port matches the port id provided
     */
    private boolean isPortIdMatch(NetworkPortElement port, String portId) {
        return port != null && portId.equals(port.getPortId());
    }

    private void updateInspectionPortProfile(Port port, PortApi neutronPortApi) {
        if (InspectionPort.isRegistered(port)) {
            this.log.info("Inspection port '" + port.getId() + "' already registered");
            return;
        } else {
            UpdatePort updatedPort = Port.updateBuilder()
                    .profile(InspectionPort.updateBindingProfile(port.getId(), port.getProfile())).build();
            neutronPortApi.update(port.getId(), updatedPort);
        }
    }

    private void updateInspectionHookProfile(String region, InspectionHook inspectionHook, boolean generateId)
            throws NetworkPortNotFoundException, Exception {
        PortApi neutronPortApi = this.neutronApi.getPortApi(region);

        String inspectionIngressPortId = inspectionHook.getInspectionPort().getIngressPort().getPortId();
        String inspectionEgressPortId = inspectionHook.getInspectionPort().getEgressPort().getPortId();
        Port inspectionEgressPort = null;

        Port inspectedPort = getPortOrThrow(neutronPortApi, inspectionHook.getInspectedPortId(), PortType.INSPECTED);
        Port inspectionIngressPort = getPortOrThrow(neutronPortApi, inspectionIngressPortId, PortType.INSPECTION);
        if (inspectionIngressPortId.equals(inspectionEgressPortId)) {
            inspectionEgressPort = inspectionIngressPort;
        } else {
            inspectionEgressPort = getPortOrThrow(neutronPortApi, inspectionEgressPortId, PortType.INSPECTION);
        }

        if (!InspectionPort.isRegistered(inspectionIngressPort) && !InspectionPort.isRegistered(inspectionEgressPort)) {
            throw new Exception(
                    String.format("Port with Id: '%s' is not an inspection port", inspectionHook.getInspectionPort()));
        }

        if (generateId) {
            inspectionHook.setHookId(UUID.randomUUID().toString());
        }

        UpdatePort updatedPort = Port.updateBuilder()
                .profile(InspectionHook.updateBindingProfile(inspectionHook, inspectedPort.getProfile())).build();
        neutronPortApi.update(inspectionHook.getInspectedPortId(), updatedPort);
    }

    private Port getPortOrThrow(PortApi neutronPortApi, String portId, PortType type)
            throws NetworkPortNotFoundException {
        Port port = neutronPortApi.get(portId);
        if (port == null) {
            throw new NetworkPortNotFoundException(portId,
                    String.format("%s Port with Id: '%s' not found", type, portId));
        }
        return port;
    }

}
