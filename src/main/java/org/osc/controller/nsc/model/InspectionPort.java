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
package org.osc.controller.nsc.model;

import java.util.HashMap;
import java.util.Map;

import org.jclouds.openstack.neutron.v2.domain.Port;
import org.osc.sdk.controller.DefaultInspectionPort;
import org.osc.sdk.controller.element.NetworkElement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class InspectionPort extends DefaultInspectionPort {

    private static final String KEY_INSPECTIONPORT_ID = "inspectionport_id";
    private static final String KEY_INSPECTIONPORT_EGRESS_ID = "inspectionport_egress_id";

    public InspectionPort(NetworkElement ingressPort, NetworkElement egressPort) {
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
