package org.osc.controller.nsc.model;

import java.util.HashMap;
import java.util.Map;

import org.jclouds.openstack.neutron.v2.domain.Port;
import org.osc.sdk.controller.DefaultInspectionPort;
import org.osc.sdk.controller.element.NetworkPortElement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class InspectionPort extends DefaultInspectionPort {

    private static final String KEY_INSPECTIONPORT_ID = "inspectionport_id";
    private static final String KEY_INSPECTIONPORT_EGRESS_ID = "inspectionport_egress_id";

    public InspectionPort(NetworkPortElement ingressPort, NetworkPortElement egressPort) {
        super(ingressPort, egressPort);
    }

    public static ImmutableMap<String, Object> updateBindingProfile(String ingressPortId, String egressPortId,
            ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);
        updatedPortProfile.put(KEY_INSPECTIONPORT_ID, ingressPortId);
        updatedPortProfile.put(KEY_INSPECTIONPORT_EGRESS_ID, egressPortId);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static ImmutableMap<String, Object> removeBindingProfile(ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);
        updatedPortProfile.remove(KEY_INSPECTIONPORT_ID);
        updatedPortProfile.remove(KEY_INSPECTIONPORT_EGRESS_ID);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static boolean isRegistered(Port port) {
        ImmutableSet<String> keySet = port.getProfile().keySet();
        return keySet.contains(KEY_INSPECTIONPORT_ID) && keySet.contains(KEY_INSPECTIONPORT_EGRESS_ID);
    }

}
