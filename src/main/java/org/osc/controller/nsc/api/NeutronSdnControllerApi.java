package org.osc.controller.nsc.api;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.osc.controller.nsc.api.jcloud.Endpoint;
import org.osc.controller.nsc.model.InspectionHook;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.FlowInfo;
import org.osc.sdk.controller.FlowPortInfo;
import org.osc.sdk.controller.Status;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osc.sdk.controller.exception.NetworkPortNotFoundException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/* This must be of scope prototype as the Virtualization connector and region are stateful */
@Component(scope=ServiceScope.PROTOTYPE, property="osc.plugin.name=NSC")
public class NeutronSdnControllerApi implements SdnControllerApi {

    Logger log = Logger.getLogger(NeutronSdnControllerApi.class);
    private static final String EMPTY_STRING = "";

    private VirtualizationConnectorElement vc;
    private String region;

    public NeutronSdnControllerApi() {
    }

    public NeutronSdnControllerApi(VirtualizationConnectorElement vc, String region) throws Exception {
        this.vc = vc;
        this.region = region;
    }

    @Override
    public InspectionHookElement getInspectionHook(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort) throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
            return neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
        }
    }

    @Override
    public void installInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {

        InspectionHook inspectionHook = new InspectionHook();
        inspectionHook.setInspectedPortId(inspectedPort.getElementId());
        inspectionHook.setInspectionPort(inspectionPort);
        inspectionHook.setOrder(order);
        inspectionHook.setTag(tag);
        inspectionHook.setEncType(encType == null ? null : encType.toString());
        inspectionHook.setFailurePolicyType(failurePolicyType.toString());

        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
            neutronApi.addInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public void updateInspectionHook(InspectionHookElement inspectionHookElement)
            throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            InspectionHook inspectionHook = new InspectionHook(inspectionHookElement);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public void removeInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            neutronApi.deleteInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
        } catch (NetworkPortNotFoundException nfe) {
            this.log.info(String.format("Port with Id: '%s' not found", nfe.getPortId()));
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
            throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
            inspectionHook.setTag(tag);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort) throws NetworkPortNotFoundException, Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getFailurePolicyType();
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
            inspectionHook.setFailurePolicyType(failurePolicyType.toString());
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            List<InspectionHook> inspectionHooksByPort = neutronApi.getInspectionHooksByPort(this.region, inspectedPort.getElementId());
            if (inspectionHooksByPort != null) {
                for (InspectionHook hook : inspectionHooksByPort) {
                    neutronApi.deleteInspectionHook(this.region, hook.getInspectedPortId());
                }
            }
        } catch (NetworkPortNotFoundException nfe) {
            this.log.info(String.format("Inspected Port with Id: '%s' not found", nfe.getPortId()));
        }
    }

    // Inspection port methods
    @Override
    public InspectionPortElement getInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
            return neutronApi.getInspectionPort(this.region, inspectionPort);
        }
    }

    @Override
    public String registerInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
            neutronApi.addInspectionPort(this.region, inspectionPort);
        }
        return EMPTY_STRING;
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
        // TODO: Future. We should not rely on list ports instead we should send a valid status
        // based on is SDN controller ready to serve
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
            neutronApi.test();
        }
        return new Status(getName(), getVersion(), true);
    }

    @Override
    public void setInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            Long order) throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
            inspectionHook.setOrder(order);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getOrder();
    }

    @Override
    public void updateInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
            inspectionHook.setInspectedPortId(inspectedPort.getElementId());
            inspectionHook.setInspectionPort(inspectionPort);
            inspectionHook.setOrder(order);
            inspectionHook.setTag(tag);
            inspectionHook.setEncType(encType.toString());
            inspectionHook.setFailurePolicyType(failurePolicyType.toString());
            neutronApi.updateInspectionHook(this.region, inspectionHook);
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

    @Override
    public HashMap<String, FlowPortInfo> queryPortInfo(HashMap<String, FlowInfo> portsQuery) throws Exception {
        throw new NotImplementedException("NSC SDN Controller does not support flow based query");
    }

    @Override
    public boolean isSupportQueryPortInfo() {
        return false;
    }

    @Override
    public NetworkElement registerNetworkElement(List<NetworkElement> inspectedPorts) throws Exception {
        return null;
    }

    @Override
    public void updateNetworkElement(NetworkElement portGroup, List<NetworkElement> inspectedPorts) throws Exception {
        //no-op
    }

    @Override
    public void deleteNetworkElement(NetworkElement portGroupId) throws Exception {
      //no-op
    }

    @Override
    public List<NetworkElement> getNetworkElements(NetworkElement element) throws Exception {
        return null;
    }

}
