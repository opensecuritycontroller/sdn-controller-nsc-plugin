package com.intelsecurity.isc.controller.model;

import java.util.HashMap;
import java.util.Map;

import org.jclouds.openstack.neutron.v2.domain.Port;

import com.google.common.collect.ImmutableMap;
import com.intelsecurity.isc.plugin.controller.DefaultNetworkPort;
import com.intelsecurity.isc.plugin.controller.element.InspectionPortElement;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;

public class InspectionPort implements InspectionPortElement {

    private String portId;

    @Override
    public String toString() {
        return "InspectedPort [portId=" + this.portId + "]";
    }

    @Override
    public String getId() {
        return this.portId;
    }

    public void setId(String portId) {
        this.portId = portId;
    }

    @Override
    public NetworkPortElement getInspectionPort() {
        return new DefaultNetworkPort(this.portId, null);
    }

    public static ImmutableMap<String, Object> updateBindingProfile(String portId,
            ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);
        updatedPortProfile.put("inspectionport_id", portId);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static ImmutableMap<String, Object> removeBindingProfile(ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);
        updatedPortProfile.remove("inspectionport_id");

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static boolean isRegistered(Port port) {
        ImmutableMap<String, Object> portProfile = port.getProfile();
        for (String attributeName : portProfile.keySet()) {
            if (attributeName.equals("inspectionport_id")) {
                // Inspection port already registered
                return true;
            }
        }
        return false;
    }

}
