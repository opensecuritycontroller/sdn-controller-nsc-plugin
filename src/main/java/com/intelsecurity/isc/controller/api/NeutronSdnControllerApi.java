package com.intelsecurity.isc.controller.api;

import org.apache.log4j.Logger;

import com.intelsecurity.isc.controller.model.InspectionHook;
import com.intelsecurity.isc.controller.model.InspectionPort;
import com.intelsecurity.isc.plugin.controller.FailurePolicyType;
import com.intelsecurity.isc.plugin.controller.Status;
import com.intelsecurity.isc.plugin.controller.TagEncapsulationType;
import com.intelsecurity.isc.plugin.controller.api.SdnControllerApi;
import com.intelsecurity.isc.plugin.controller.element.InspectionHookElement;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;
import com.intelsecurity.isc.plugin.controller.element.VirtualizationConnectorElement;
import com.mcafee.vmidc.rest.client.exception.RestClientException;
import com.sun.jersey.api.client.ClientResponse;

public class NeutronSdnControllerApi implements SdnControllerApi {

    Logger log = Logger.getLogger(NeutronSdnControllerApi.class);

    private VirtualizationConnectorElement vc;

    public static NeutronSdnControllerApi create() {
        return new NeutronSdnControllerApi();
    }

    public static SdnControllerApi create(VirtualizationConnectorElement vc) throws Exception {
        return new NeutronSdnControllerApi(vc);
    }

    public NeutronSdnControllerApi() {
    }

    public NeutronSdnControllerApi(VirtualizationConnectorElement vc) throws Exception {
        this.vc = vc;
    }

    @Override
    public void installInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType) throws Exception {

        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        InspectionHook inspectionHook = new InspectionHook();
        inspectionHook.inspectedPortId = inspectedPort.getPortId();
        inspectionHook.inspectionPortId = inspectionPort.getPortId();
        inspectionHook.order = order;
        inspectionHook.tag = tag;
        inspectionHook.encType = encType.toString();
        inspectionHook.failurePolicyType = failurePolicyType.toString();
        nsca.addInspectionHook(inspectionHook);
    }

    @Override
    public void removeInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        try {
            nsca.deleteInspectionHookByPorts(inspectedPort.getPortId(), inspectionPort.getPortId());
        } catch (RestClientException rce) {
            Integer responseCode = rce.getResponseCode();
            if (responseCode == null || !rce.getResponseCode().equals(ClientResponse.Status.NOT_FOUND.getStatusCode())) {
                throw rce;
            }
            this.log.info("Inspection hook for port '" + inspectedPort + "' not found");
        }
    }

    @Override
    public void removeAllInspectionHooks(NetworkPortElement inspectedPort) throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        try {
            nsca.deleteInspectionHookByPorts(inspectedPort.getPortId(), null);
        } catch (RestClientException rce) {
            Integer responseCode = rce.getResponseCode();
            if (responseCode == null || !rce.getResponseCode().equals(ClientResponse.Status.NOT_FOUND.getStatusCode())) {
                throw rce;
            }
            this.log.info("Inspection hooks for port '" + inspectedPort + "' not found");
        }
    }

    @Override
    public void setInspectionHookTag(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort, Long tag)
            throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        InspectionHook inspectionHook = nsca.getInspectionHookByPorts(inspectedPort.getPortId(),
                inspectionPort.getPortId());
        inspectionHook.tag = tag;
        nsca.updateInspectionHook(inspectionHook.id, inspectionHook);
    }

    @Override
    public Long getInspectionHookTag(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getTag();
    }

    @Override
    public void registerInspectionPort(NetworkPortElement inspectionPort) throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        InspectionPort inspPort = null;
        try {
            inspPort = nsca.getInspectionPortByPort(inspectionPort.getPortId());
        } catch (RestClientException rce) {
            Integer responseCode = rce.getResponseCode();
            if (responseCode == null || !rce.getResponseCode().equals(ClientResponse.Status.NOT_FOUND.getStatusCode())) {
                throw rce;
            }
            this.log.info("Inspection port '" + inspectionPort + "' not found");
        }

        if (inspPort != null) {
            return;
        }

        inspPort = new InspectionPort();
        inspPort.portId = inspectionPort.getPortId();
        nsca.addInspectionPort(inspPort);
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        InspectionHook inspectionHook = nsca.getInspectionHookByPorts(inspectedPort.getPortId(),
                inspectionPort.getPortId());
        inspectionHook.failurePolicyType = failurePolicyType.toString();
        nsca.updateInspectionHook(inspectionHook.id, inspectionHook);
    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkPortElement inspectedPort,
            NetworkPortElement inspectionPort) throws Exception {
        return getInspectionHook(inspectedPort, inspectionPort).getFailurePolicyType();
    }

    @Override
    public InspectionHookElement getInspectionHook(NetworkPortElement inspectedPort, NetworkPortElement inspectionPort)
            throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        try {
            return nsca.getInspectionHookByPorts(inspectedPort.getPortId(), inspectionPort.getPortId());
        } catch (RestClientException rce) {
            Integer responseCode = rce.getResponseCode();
            if (responseCode == null || !rce.getResponseCode().equals(ClientResponse.Status.NOT_FOUND.getStatusCode())) {
                throw rce;
            }
            this.log.info("Inspection hook for port '" + inspectedPort + "' not found");
            return null;
        }
    }

    @Override
    public void updateInspectionHook(InspectionHookElement inspectionHookElement) throws Exception {
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        InspectionHook inspectionHook = new InspectionHook(inspectionHookElement);
        nsca.updateInspectionHook(inspectionHook.getHookId(), inspectionHook);
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
    public boolean isOffboxRedirectionSupported() {
        return false;
    }

    @Override
    public Status getStatus() throws Exception {
        Status status = null;
        NeutronSecurityControllerApi nsca = new NeutronSecurityControllerApi(this.vc);
        // TODO: Future, we should not rely on list ports instead we should send a valid status based on is SDN controller ready to serve  
        nsca.getInspectionPorts();
        status = new Status(getName(), getVersion(), true);
        return status;
    }
}
