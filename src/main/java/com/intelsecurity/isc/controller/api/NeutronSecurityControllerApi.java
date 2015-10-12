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
    public InspectionPort getInspectionPortByPort(String region, String inspectionPortId)
            throws NetworkPortNotFoundException, Exception {
        this.log.info("Retriving Inspection port '" + inspectionPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port port = getPortOrThrow(neutronPortApi, inspectionPortId, PortType.INSPECTION);

        if (!InspectionPort.isRegistered(port)) {
            this.log.info("Inspection port '" + inspectionPortId + "' not registered.");
            return null;
        }

        InspectionPort inspectionPort = new InspectionPort();
        inspectionPort.setId(inspectionPortId);
        return inspectionPort;
    }

    public void addInspectionPort(String region, String inspectionPortId) throws NetworkPortNotFoundException,
            Exception {
        PortApi neutronPortApi = this.neutronApi.getPortApi(region);

        this.log.info("Adding Inspection port '" + inspectionPortId + "'");
        Port port = getPortOrThrow(neutronPortApi, inspectionPortId, PortType.INSPECTION);

        if (InspectionPort.isRegistered(port)) {
            this.log.info("Inspection port '" + inspectionPortId + "' already registered");
            return;
        }

        UpdatePort updatedPort = Port.updateBuilder()
                .profile(InspectionPort.updateBindingProfile(inspectionPortId, port.getProfile())).build();
        neutronPortApi.update(inspectionPortId, updatedPort);
    }

    public void deleteInspectionPort(String region, String inspectionPortId) throws NetworkPortNotFoundException,
            Exception {
        this.log.info("Deleting Inspection port '" + inspectionPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port port = getPortOrThrow(neutronPortApi, inspectionPortId, PortType.INSPECTION);

        if (!InspectionPort.isRegistered(port)) {
            this.log.info("Inspection port '" + inspectionPortId + "' not registered.");
            return;
        }

        UpdatePort updatedPort = Port.updateBuilder().profile(InspectionPort.removeBindingProfile(port.getProfile()))
                .build();
        neutronPortApi.update(inspectionPortId, updatedPort);
    }

    // Inspection Hooks
    public InspectionHook getInspectionHookByPorts(String region, String inspectedPortId, String inspectionPortId)
            throws NetworkPortNotFoundException, Exception {
        List<InspectionHook> hooks = getInspectionHooksByPort(region, inspectedPortId);
        if (hooks != null) {
            for (InspectionHook hook : hooks) {
                if (hook.getInspectionPort() != null && hook.getInspectionPort().getPortId() != null
                        && hook.getInspectionPort().getPortId().equals(inspectionPortId)) {
                    return hook;
                }
            }
        }
        return null;
    }

    public void deleteInspectionHookByPorts(String region, String inspectedPortId, String inspectionPortId)
            throws NetworkPortNotFoundException, Exception {
        InspectionHook inspectionHook = getInspectionHookByPorts(region, inspectedPortId, inspectionPortId);
        if (inspectionHook != null) {
            deleteInspectionHook(region, inspectionHook.getInspectedPortId());
        }
    }

    public List<InspectionHook> getInspectionHooksByPort(String region, String inspectedPortId)
            throws NetworkPortNotFoundException, Exception {
        this.log.info("Retriving Inspection Hook for port '" + inspectedPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port inspectedPort = getPortOrThrow(neutronPortApi, inspectedPortId, PortType.INSPECTED);

        InspectionHook inspectionHook = InspectionHook.generateInspectionHookFromPort(inspectedPort);

        if (inspectionHook == null) {
            this.log.info("Inspection hook for port '" + inspectedPortId + "' not registered.");
            return null;
        }

        return Arrays.asList(inspectionHook);
    }

    public InspectionHook getInspectionHook(String region, String inspectedPortId) throws NetworkPortNotFoundException,
            Exception {
        this.log.info("Retriving Inspection hook '" + inspectedPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port port = getPortOrThrow(neutronPortApi, inspectedPortId, PortType.INSPECTED);

        InspectionHook inspectionHook = InspectionHook.generateInspectionHookFromPort(port);
        if (inspectionHook == null) {
            throw new Exception(String.format("Port with Id: '%s' is not an inspection hook", inspectedPortId));
        }
        return inspectionHook;
    }

    public void addInspectionHook(String region, InspectionHook inspectionHook) throws NetworkPortNotFoundException,
            Exception {
        this.log.info("Adding Inspection Hook '" + inspectionHook + "'");

        updateInspectionHookProfile(region, inspectionHook, true);

    }

    public void updateInspectionHook(String region, InspectionHook inspectionHook) throws NetworkPortNotFoundException,
            Exception {
        this.log.info("Updating Inspection Hook '" + inspectionHook + "'");

        updateInspectionHookProfile(region, inspectionHook, false);
    }

    public void deleteInspectionHook(String region, String inspectedPortId) throws Exception {
        this.log.info("Deleting Inspection Hook for port '" + inspectedPortId + "'");

        PortApi neutronPortApi = this.neutronApi.getPortApi(region);
        Port inspectedPort = getPortOrThrow(neutronPortApi, inspectedPortId, PortType.INSPECTED);

        if (!InspectionHook.isInspectionHookRegistered(inspectedPort)) {
            this.log.info(String.format("Inspected Port with Id: '%s' does not an have inspection hook",
                    inspectedPortId));
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

    private void updateInspectionHookProfile(String region, InspectionHook inspectionHook, boolean generateId)
            throws NetworkPortNotFoundException, Exception {
        PortApi neutronPortApi = this.neutronApi.getPortApi(region);

        Port inspectedPort = getPortOrThrow(neutronPortApi, inspectionHook.getInspectedPortId(), PortType.INSPECTED);
        Port inspectionPort = getPortOrThrow(neutronPortApi, inspectionHook.getInspectionPortId(), PortType.INSPECTION);

        if (!InspectionPort.isRegistered(inspectionPort)) {
            throw new Exception(String.format("Port with Id: '%s' is not an inspection port",
                    inspectionHook.getInspectionPortId()));
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
            throw new NetworkPortNotFoundException(portId, String.format("%s Port with Id: '%s' not found", type,
                    portId));
        }
        return port;
    }

}
