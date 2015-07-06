package com.intelsecurity.isc.controller.api;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;

import com.intelsecurity.isc.controller.model.InspectionHook;
import com.intelsecurity.isc.controller.model.InspectionHookInput;
import com.intelsecurity.isc.controller.model.InspectionHooks;
import com.intelsecurity.isc.controller.model.InspectionPort;
import com.intelsecurity.isc.controller.model.InspectionPortInput;
import com.intelsecurity.isc.controller.model.InspectionPorts;
import com.intelsecurity.isc.plugin.controller.element.VirtualizationConnectorElement;
import com.mcafee.vmidc.util.EncryptionUtil;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class NeutronSecurityControllerApi {

    Logger log = Logger.getLogger(NeutronSecurityControllerApi.class);

    private NeutronSecurityControllerRestClient client;

    public NeutronSecurityControllerApi(VirtualizationConnectorElement vc) {
        this.client = new NeutronSecurityControllerRestClient(vc.getControllerIpAddress(), vc.getControllerUsername(),
                EncryptionUtil.decrypt(vc.getControllerPassword()));
    }

    public InspectionHook getInspectionHook(String inspectionHookId) throws Exception {
        return this.client.getResource("inspectionhooks/" + inspectionHookId, InspectionHook.class);
    }

    public InspectionHook getInspectionHookByInspectedPort(String inspectedPortId) throws Exception {
        return getInspectionHookByPorts(inspectedPortId, null);
    }

    public InspectionHook getInspectionHookByPorts(String inspectedPortId, String inspectionPortId) throws Exception {
        MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();
        paramMap.putSingle("inspected_port_id", inspectedPortId);
        InspectionHooks inspectionHooks = this.client.getResource("inspectionhooks", InspectionHooks.class, paramMap);
        if (inspectionHooks != null) {
        	for (InspectionHook hook : inspectionHooks.inspectionHooks) {
        		if (hook.getInspectionPort().getPortId().equals(inspectionPortId)) {
        			return inspectionHooks.inspectionHooks.get(0);
        		}
        	}
        }
        return null;
    }

    public List<InspectionHook> getInspectionHooks() throws Exception {
        return this.client.getResource("inspectionhooks", InspectionHooks.class).inspectionHooks;
    }

    public InspectionHook addInspectionHook(InspectionHook inspectionHook) throws Exception {
        InspectionHookInput ihi = new InspectionHookInput();
        ihi.inspectionHook = inspectionHook;
        return this.client.postResource("inspectionhooks", InspectionHookInput.class, ihi).inspectionHook;
    }

    public void updateInspectionHook(String inspectionHookId, InspectionHook inspectionHook) throws Exception {
        InspectionHookInput ihi = new InspectionHookInput();
        ihi.inspectionHook = inspectionHook;
        this.client.putResource("inspectionhooks/" + inspectionHookId, ihi);
    }

    public void deleteInspectionHook(String inspectionHookId) throws Exception {
        this.client.deleteResource("inspectionhooks/" + inspectionHookId);
    }

    public void deleteInspectionHookByPorts(String inspectedPortId, String inspectionPortId) throws Exception {
        InspectionHook inspectionHook = getInspectionHookByInspectedPort(inspectedPortId);
        if (inspectionHook != null) {
            deleteInspectionHook(inspectionHook.id);
        }
    }

    public List<InspectionPort> getInspectionPorts() throws Exception {
        return this.client.getResource("inspectionports", InspectionPorts.class).inspectionPorts;
    }

    public InspectionPort getInspectionPortByPort(String inspectionPortId) throws Exception {
        MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();
        paramMap.putSingle("port_id", inspectionPortId);
        return this.client.getResource("inspectionports", InspectionPort.class, paramMap);
    }

    public InspectionPort getInspectionPort(String inspectionId) throws Exception {
        return this.client.getResource("inspectionports/" + inspectionId, InspectionPort.class);
    }

    public InspectionPort addInspectionPort(InspectionPort inspectionPort) throws Exception {
        InspectionPortInput ipi = new InspectionPortInput();
        ipi.inspectionPort = inspectionPort;
        return this.client.postResource("inspectionports", InspectionPortInput.class, ipi).inspectionPort;
    }

    public void deleteInspectionPort(String inspectionId) throws Exception {
        this.client.deleteResource("inspectionports/" + inspectionId);
    }

}
