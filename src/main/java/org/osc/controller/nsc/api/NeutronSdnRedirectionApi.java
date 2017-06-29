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

import org.apache.commons.lang.NotImplementedException;
import org.osc.controller.nsc.api.openstack4j.Endpoint;
import org.osc.controller.nsc.model.InspectionHook;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.api.SdnRedirectionApi;
import org.osc.sdk.controller.element.Element;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osc.sdk.controller.exception.NetworkPortNotFoundException;

import java.util.List;

public class NeutronSdnRedirectionApi implements SdnRedirectionApi {

    private VirtualizationConnectorElement vc;
    private String region;

    public NeutronSdnRedirectionApi() {
    }

    public NeutronSdnRedirectionApi(VirtualizationConnectorElement vc, String region) {
        this.vc = vc;
        this.region = region;
    }

    @Override
    public InspectionHookElement getInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            return neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
        }
    }

    @Override
    public String installInspectionHook(List<NetworkElement> inspectedPort, InspectionPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {
        InspectionHook inspectionHook = new InspectionHook();
        // For NSC only one inspected port is expected
        inspectionHook.setInspectedPortId(inspectedPort.iterator().next().getElementId());
        inspectionHook.setInspectionPort(inspectionPort);
        inspectionHook.setOrder(order);
        inspectionHook.setTag(tag);
        inspectionHook.setEncType(encType == null ? null : encType.toString());
        inspectionHook.setFailurePolicyType(failurePolicyType.toString());

//        NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
//        neutronApi.addInspectionHook(this.region, inspectionHook);
        return inspectionHook.getHookId();
    }

    @Override
    public void removeInspectionHook(List<NetworkElement> inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            neutronApi.deleteInspectionHookByPorts(this.region, inspectedPort.iterator().next().getElementId(), inspectionPort);
//        } catch (NetworkPortNotFoundException nfe) {
//            this.log.info(String.format("Port with Id: '%s' not found", nfe.getPortId()));
//        }
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
        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(),
                    inspectionPort);
            inspectionHook.setTag(tag);
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkElement inspectedPort,
                                                            InspectionPortElement inspectionPort) throws Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getFailurePolicyType();
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws Exception {
//        NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
//        InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
//        inspectionHook.setFailurePolicyType(failurePolicyType.toString());
//        neutronApi.updateInspectionHook(this.region, inspectionHook);
    }

    @Override
    public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {
//        NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
//        List<InspectionHook> inspectionHooksByPort = neutronApi.getInspectionHooksByPort(this.region, inspectedPort.getElementId());
//        if (inspectionHooksByPort != null) {
//            for (InspectionHook hook : inspectionHooksByPort) {
//                neutronApi.deleteInspectionHook(this.region, hook.getInspectedPortId());
//            }
//        }
        //this.log.info(String.format("Inspected Port with Id: '%s' not found", nfe.getPortId()));
    }

    // Inspection port methods
    @Override
    public InspectionPortElement getInspectionPort(InspectionPortElement inspectionPort) throws Exception {
//        NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
        return neutronApi.getInspectionPort(this.region, inspectionPort);
    }

    @Override
    public Element registerInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))){
//            neutronApi.addInspectionPort(this.region, inspectionPort);
//        }
		return null;
    }

    @Override
    public void setInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long order)
            throws Exception {
//        NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc));
//        InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(), inspectionPort);
//        inspectionHook.setOrder(order);
//        neutronApi.updateInspectionHook(this.region, inspectionHook);
    }

    @Override
    public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort) throws Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getOrder();
    }

    @Override
    public void updateInspectionHook(InspectionHookElement existingInspectionHook) throws Exception {
        NetworkElement inspectedPort = existingInspectionHook.getInspectedPort();
        InspectionPortElement inspectionPort = existingInspectionHook.getInspectionPort();

        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(),
                    inspectionPort);
            inspectionHook.setInspectedPortId(inspectedPort.getElementId());
            inspectionHook.setInspectionPort(existingInspectionHook.getInspectionPort());
            inspectionHook.setOrder(existingInspectionHook.getOrder());
            inspectionHook.setTag(existingInspectionHook.getTag());
            inspectionHook.setEncType(existingInspectionHook.getEncType().toString());
            inspectionHook.setFailurePolicyType(existingInspectionHook.getFailurePolicyType().toString());
            neutronApi.updateInspectionHook(this.region, inspectionHook);
        }
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }

    @Override
    public NetworkElement registerNetworkElement(List<NetworkElement> inspectedPorts) throws Exception {
        return null;
    }

    @Override
    public NetworkElement updateNetworkElement(NetworkElement portGroup, List<NetworkElement> inspectedPorts) throws Exception {
        //no-op
        return null;
    }

    @Override
    public void deleteNetworkElement(NetworkElement portGroupId) throws Exception {
        //no-op
    }

    @Override
    public List<NetworkElement> getNetworkElements(NetworkElement element) throws Exception {
        return null;
    }

    @Override
    public void removeInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        // no-op

    }

    @Override
    public void removeInspectionHook(String inspectionHookId) throws Exception {
        // TODO emanoel: Currently not needed but should be implemented also for NSC.
        throw new NotImplementedException("Not expected to be called for NSC. "
                + "Currently only called for SDN controllers that support port group.");
    }

    @Override
    public InspectionHookElement getInspectionHook(String inspectionHookId) throws Exception {
        // TODO emanoel: Currently not needed but should be implemented also for NSC.
        throw new NotImplementedException("Not expected to be called for NSC. "
                + "Currently only called for SDN controllers that support port group.");
    }
}
