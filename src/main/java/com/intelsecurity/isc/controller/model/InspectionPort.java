package com.intelsecurity.isc.controller.model;

import java.util.HashMap;
import java.util.Map;

import org.jclouds.openstack.neutron.v2.domain.Port;

import com.google.common.collect.ImmutableMap;
import com.intelsecurity.isc.plugin.controller.DefaultInspectionPort;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;

public class InspectionPort extends DefaultInspectionPort {

    private static final String KEY_INSPECTIONPORT_ID = "inspectionport_id";

    public InspectionPort(NetworkPortElement ingressPort, NetworkPortElement egressPort) {
        super(ingressPort, egressPort);
    }

    public static ImmutableMap<String, Object> updateBindingProfile(String portId,
            ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);
        updatedPortProfile.put(KEY_INSPECTIONPORT_ID, portId);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static ImmutableMap<String, Object> removeBindingProfile(ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);
        updatedPortProfile.remove(KEY_INSPECTIONPORT_ID);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static boolean isRegistered(Port port) {
        ImmutableMap<String, Object> portProfile = port.getProfile();
        for (String attributeName : portProfile.keySet()) {
            if (attributeName.equals(KEY_INSPECTIONPORT_ID)) {
                // Inspection port already registered
                return true;
            }
        }
        return false;
    }

}
