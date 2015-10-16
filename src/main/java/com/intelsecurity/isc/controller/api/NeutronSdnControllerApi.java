package com.intelsecurity.isc.controller.api;

import java.util.List;

import org.apache.log4j.Logger;

import com.intelsecurity.isc.controller.api.jcloud.Endpoint;
import com.intelsecurity.isc.controller.model.InspectionHook;
import com.intelsecurity.isc.plugin.controller.FailurePolicyType;
import com.intelsecurity.isc.plugin.controller.Status;
import com.intelsecurity.isc.plugin.controller.TagEncapsulationType;
import com.intelsecurity.isc.plugin.controller.api.SdnControllerApi;
import com.intelsecurity.isc.plugin.controller.element.InspectionHookElement;
import com.intelsecurity.isc.plugin.controller.element.InspectionPortElement;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;
import com.intelsecurity.isc.plugin.controller.element.VirtualizationConnectorElement;
import com.intelsecurity.isc.plugin.controller.exception.NetworkPortNotFoundException;

public class NeutronSdnControllerApi implements SdnControllerApi {

    Logger log = Logger.getLogger(NeutronSdnControllerApi.class);

    private VirtualizationConnectorElement vc;
    private String region;

    public NeutronSdnControllerApi() {
    }

    public NeutronSdnControllerApi(VirtualizationConnectorElement vc, String region) throws Exception {
        this.vc = vc;
        this.region = region;
    }

    @Override
    public InspectionHookElement getInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            InspectionHook inspectionHookElement = neutronApi.getInspectionHookByPorts(this.region,
                    inspectedPort.getPortId(), inspectionPort.getPortId());
            return inspectionHookElement;
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public void installInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {

        InspectionHook inspectionHook = new InspectionHook();
        inspectionHook.setInspectedPortId(inspectedPort.getPortId());
        inspectionHook.setInspectionPortId(inspectionPort.getPortId());
        inspectionHook.setOrder(order);
        inspectionHook.setTag(tag);
        inspectionHook.setEncType(encType.toString());
        inspectionHook.setFailurePolicyType(failurePolicyType.toString());
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            neutronApi.addInspectionHook(this.region, inspectionHook);
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public void updateInspectionHook(InspectionHookElement inspectionHookElement)
            throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            InspectionHook inspectionHook = new InspectionHook(inspectionHookElement);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public void removeInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            neutronApi.deleteInspectionHookByPorts(this.region, inspectedPort.getPortId(), inspectionPort.getPortId());
        } catch (NetworkPortNotFoundException nfe) {
            this.log.info(String.format("Port with Id: '%s' not found", nfe.getPortId()));
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public Long getInspectionHookTag(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getTag();
    }

    @Override
    public void setInspectionHookTag(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort, Long tag)
            throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getPortId(),
                    inspectionPort.getPortId());
            inspectionHook.setTag(tag);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkPortElement inspectedPort,
            NetworkPortElement inspectionPort) throws NetworkPortNotFoundException, Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);

        return inspectionHook == null ? null : inspectionHook.getFailurePolicyType();
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getPortId(),
                    inspectionPort.getPortId());
            inspectionHook.setFailurePolicyType(failurePolicyType.toString());
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public void removeAllInspectionHooks(NetworkPortElement inspectedPort) throws Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            List<InspectionHook> inspectionHooksByPort = neutronApi.getInspectionHooksByPort(this.region,
                    inspectedPort.getPortId());
            if (inspectionHooksByPort != null) {
                for (InspectionHook hook : inspectionHooksByPort) {
                    neutronApi.deleteInspectionHook(this.region, hook.getInspectedPortId());
                }
            }
        } catch (NetworkPortNotFoundException nfe) {
            this.log.info(String.format("Inspected Port with Id: '%s' not found", nfe.getPortId()));
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    // Inspection port methods
    @Override
    public InspectionPortElement getInspectionPort(NetworkPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            return neutronApi.getInspectionPortByPort(this.region, inspectionPort.getPortId());
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public void registerInspectionPort(NetworkPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            neutronApi.addInspectionPort(this.region, inspectionPort.getPortId());
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public String getName() {
        return "NSC";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public void setVirtualizationConnector(VirtualizationConnectorElement vc) {
        this.vc = vc;
    }

    @Override
    public Status getStatus() throws Exception {
        Status status = null;
        // TODO: Future. We should not rely on list ports instead we should send a valid status
        // based on is SDN controller ready to serve
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            neutronApi.test();
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
        status = new Status(getName(), getVersion(), true);
        return status;
    }

    @Override
    public void setInspectionHookOrder(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort, Long order)
            throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getPortId(),
                    inspectionPort.getPortId());
            inspectionHook.setOrder(order);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public Long getInspectionHookOrder(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getOrder();
    }

    @Override
    public void updateInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {
        NeutronSecurityControllerApi neutronApi = null;
        try {
            neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getPortId(),
                    inspectionPort.getPortId());
            inspectionHook.setInspectedPortId(inspectedPort.getPortId());
            inspectionHook.setInspectionPortId(inspectionPort.getPortId());
            inspectionHook.setOrder(order);
            inspectionHook.setTag(tag);
            inspectionHook.setEncType(encType.toString());
            inspectionHook.setFailurePolicyType(failurePolicyType.toString());
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        } finally {
            if (neutronApi != null) {
                neutronApi.close();
            }
        }
    }

    @Override
    public boolean isOffboxRedirectionSupported() {
        return false;
    }

    @Override
    public boolean isServiceFunctionChainingSupported() {
        return false;
    }

    @Override
    public boolean isFailurePolicySupported() {
        return false;
    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }

    @Override
    public boolean isUsingProviderCreds() {
        return true;
    }

}
