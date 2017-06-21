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
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openstack4j.model.network.Port;
import org.osc.sdk.controller.DefaultInspectionPort;
import org.osc.sdk.controller.DefaultNetworkPort;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;

import com.google.common.collect.ImmutableMap;

public class InspectionHook implements InspectionHookElement {

    private static final String KEY_FAILURE_POLICY_TYPE = "failure_policy_type";
    private static final String KEY_ORDER = "order";
    private static final String KEY_TAG = "tag";
    private static final String KEY_INSPECTED_PORT_ID = "inspected_port_id";
    private static final String KEY_INSPECTION_EGRESS_PORT_ID = "inspection_egress_port_id";
    private static final String KEY_INSPECTIONHOOK_ID = "inspectionhook_id";
    private static final String KEY_ENC_TYPE = "enc_type";
    private static final String KEY_INSPECTION_INGRESS_PORT_ID = "inspection_port_id";

    private String id;
    private String inspectedPortId;
    private InspectionPortElement inspectionPort;
    private Long tag;
    private Long order;
    private String encType;
    private String failurePolicyType;

    public InspectionHook() {
    }

    public InspectionHook(InspectionHookElement inspectionHookElement) {
        this.tag = inspectionHookElement.getTag();
        this.order = inspectionHookElement.getOrder();
        this.encType = inspectionHookElement.getEncType() == null ? null : inspectionHookElement.getEncType()
                .toString();
        this.failurePolicyType = inspectionHookElement.getFailurePolicyType() == null ? null : inspectionHookElement
                .getFailurePolicyType().toString();
        this.id = inspectionHookElement.getHookId();
        this.inspectedPortId = inspectionHookElement.getInspectedPort() == null ? null : inspectionHookElement
                .getInspectedPort().getElementId();
        this.inspectionPort = inspectionHookElement.getInspectionPort() == null ? null : inspectionHookElement
                .getInspectionPort();
    }

    @Override
    public String toString() {
        return "InspectionHook [id=" + this.id + ", inspectedPortId=" + this.inspectedPortId + ", inspectionPortId="
                + this.inspectionPort + ", tag=" + this.tag + ", order=" + this.order + ", encType=" + this.encType
                + ", failurePolicyType=" + this.failurePolicyType + "]";
    }

    public String getInspectedPortId() {
        return this.inspectedPortId;
    }

    public void setInspectedPortId(String inspectedPortId) {
        this.inspectedPortId = inspectedPortId;
    }

    @Override
    public InspectionPortElement getInspectionPort() {
        return this.inspectionPort;
    }

    public void setInspectionPort(InspectionPortElement inspectionPort) {
        this.inspectionPort = inspectionPort;
    }

    public void setHookId(String id) {
        this.id = id;
    }

    public void setTag(Long tag) {
        this.tag = tag;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public void setEncType(String encType) {
        this.encType = encType;
    }

    public void setFailurePolicyType(String failurePolicyType) {
        this.failurePolicyType = failurePolicyType;
    }

    @Override
    public Long getTag() {
        return this.tag;
    }

    @Override
    public Long getOrder() {
        return this.order;
    }

    @Override
    public TagEncapsulationType getEncType() {
        return TagEncapsulationType.fromText(this.encType);
    }

    @Override
    public FailurePolicyType getFailurePolicyType() {
        if (this.failurePolicyType == null) {
            return null;
        }
        return FailurePolicyType.fromText(this.failurePolicyType);
    }

    @Override
    public String getHookId() {
        return this.id;
    }

    @Override
    public NetworkElement getInspectedPort() {
        return new DefaultNetworkPort(this.inspectedPortId, null);
    }

    /**
     * Generates an inspection hook the port if all attributes are present. Return null otherwise.
     *
     */
    public static InspectionHook generateInspectionHookFromPort(Port port) {
        Map<String, Object> portProfile = port.getProfile();
        InspectionHook inspectionHook = new InspectionHook();
        DefaultNetworkPort ingressPort = new DefaultNetworkPort();
        DefaultNetworkPort egressPort = new DefaultNetworkPort();

        inspectionHook.inspectionPort = new DefaultInspectionPort(ingressPort, egressPort, null);
        int relaventAttributes = 0;
        for (Entry<String, Object> attribute : portProfile.entrySet()) {
            switch (attribute.getKey()) {
            case KEY_ENC_TYPE:
                inspectionHook.encType = isAttributeEmpty(attribute.getValue()) ? null : attribute.getValue().toString();
                relaventAttributes++;
                break;
            case KEY_INSPECTIONHOOK_ID:
                inspectionHook.id = attribute.getValue().toString();
                relaventAttributes++;
                break;
            case KEY_INSPECTION_INGRESS_PORT_ID:
                ingressPort.setElementId(attribute.getValue().toString());
                relaventAttributes++;
                break;
            case KEY_INSPECTION_EGRESS_PORT_ID:
                egressPort.setElementId(attribute.getValue().toString());
                relaventAttributes++;
                break;
            case KEY_INSPECTED_PORT_ID:
                inspectionHook.inspectedPortId = attribute.getValue().toString();
                relaventAttributes++;
                break;
            case KEY_TAG:
                inspectionHook.tag = isAttributeEmpty(attribute.getValue()) ? null : new Double(attribute.getValue().toString()).longValue();
                relaventAttributes++;
                break;
            case KEY_ORDER:
                inspectionHook.order = new Double(attribute.getValue().toString()).longValue();
                relaventAttributes++;
                break;
            case KEY_FAILURE_POLICY_TYPE:
                inspectionHook.failurePolicyType = attribute.getValue().toString();
                relaventAttributes++;
                break;
            }
        }
        return relaventAttributes >= 7 ? inspectionHook : null;
    }

    private static boolean isAttributeEmpty(Object attribute) {
        return attribute == null ||
                (attribute instanceof Map<?,?> && ((Map<?,?>)attribute).isEmpty()) ||
                StringUtils.isBlank(attribute.toString());
    }

    /**
     * Adds the inspection hook attributes to a binding profile map and returns the updated map
     *
     */
    public static ImmutableMap<String, Object> updateBindingProfile(InspectionHook inspectionHook,
                                                                    Map<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);

        updatedPortProfile.put(KEY_ENC_TYPE, inspectionHook.encType == null ? "" : inspectionHook.encType);
        updatedPortProfile.put(KEY_INSPECTIONHOOK_ID, inspectionHook.id);
        updatedPortProfile.put(KEY_INSPECTION_INGRESS_PORT_ID, inspectionHook.inspectionPort.getIngressPort().getElementId());
        updatedPortProfile.put(KEY_INSPECTION_EGRESS_PORT_ID, inspectionHook.inspectionPort.getEgressPort().getElementId());
        updatedPortProfile.put(KEY_INSPECTED_PORT_ID, inspectionHook.inspectedPortId);
        updatedPortProfile.put(KEY_TAG, inspectionHook.tag == null ? "" : inspectionHook.tag);
        updatedPortProfile.put(KEY_ORDER, inspectionHook.order);
        updatedPortProfile.put(KEY_FAILURE_POLICY_TYPE, inspectionHook.failurePolicyType);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static ImmutableMap<String, Object> removeBindingProfile(Map<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);

        updatedPortProfile.remove(KEY_ENC_TYPE);
        updatedPortProfile.remove(KEY_INSPECTIONHOOK_ID);
        updatedPortProfile.remove(KEY_INSPECTION_INGRESS_PORT_ID);
        updatedPortProfile.remove(KEY_INSPECTION_EGRESS_PORT_ID);
        updatedPortProfile.remove(KEY_INSPECTED_PORT_ID);
        updatedPortProfile.remove(KEY_TAG);
        updatedPortProfile.remove(KEY_ORDER);
        updatedPortProfile.remove(KEY_FAILURE_POLICY_TYPE);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static boolean isInspectionHookRegistered(Port port) {
        return generateInspectionHookFromPort(port) != null;
    }
}
