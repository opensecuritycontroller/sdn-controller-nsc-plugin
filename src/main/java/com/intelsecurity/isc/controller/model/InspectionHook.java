package com.intelsecurity.isc.controller.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jclouds.openstack.neutron.v2.domain.Port;

import com.google.common.collect.ImmutableMap;
import com.intelsecurity.isc.plugin.controller.DefaultNetworkPort;
import com.intelsecurity.isc.plugin.controller.FailurePolicyType;
import com.intelsecurity.isc.plugin.controller.TagEncapsulationType;
import com.intelsecurity.isc.plugin.controller.element.InspectionHookElement;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;

public class InspectionHook implements InspectionHookElement {

    private String id;
    private String inspectedPortId;
    private String inspectionPortId;
    private Long tag;
    private Long order;
    private String encType;
    private String failurePolicyType;

    public InspectionHook(InspectionHookElement inspectionHookElement) {
        this.tag = inspectionHookElement.getTag();
        this.order = inspectionHookElement.getOrder();
        this.encType = inspectionHookElement.getEncType() == null ? null : inspectionHookElement.getEncType()
                .toString();
        this.failurePolicyType = inspectionHookElement.getFailurePolicyType() == null ? null : inspectionHookElement
                .getFailurePolicyType().toString();
        this.id = inspectionHookElement.getHookId();
        this.inspectedPortId = inspectionHookElement.getInspectedPort() == null ? null : inspectionHookElement
                .getInspectedPort().getPortId();
        this.inspectionPortId = inspectionHookElement.getInspectionPort() == null ? null : inspectionHookElement
                .getInspectionPort().getPortId();
    }

    public InspectionHook() {
    }

    @Override
    public String toString() {
        return "InspectionHook [id=" + this.id + ", inspectedPortId=" + this.inspectedPortId + ", inspectionPortId="
                + this.inspectionPortId + ", tag=" + this.tag + ", order=" + this.order + ", encType=" + this.encType
                + ", failurePolicyType=" + this.failurePolicyType + "]";
    }

    public String getInspectedPortId() {
        return this.inspectedPortId;
    }

    public void setInspectedPortId(String inspectedPortId) {
        this.inspectedPortId = inspectedPortId;
    }

    public String getInspectionPortId() {
        return this.inspectionPortId;
    }

    public void setInspectionPortId(String inspectionPortId) {
        this.inspectionPortId = inspectionPortId;
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
    public NetworkPortElement getInspectedPort() {
        return new DefaultNetworkPort(this.inspectedPortId, null);
    }

    @Override
    public NetworkPortElement getInspectionPort() {
        return new DefaultNetworkPort(this.inspectionPortId, null);
    }


    /**
     * Generates an inspection hook the port if all attributes are present. Return null otherwise.
     *
     */
    public static InspectionHook generateInspectionHookFromPort(Port port) {
        ImmutableMap<String, Object> portProfile = port.getProfile();
        InspectionHook inspectionHook = new InspectionHook();
        int relaventAttributes = 0;
        for (Entry<String, Object> attribute : portProfile.entrySet()) {
            switch (attribute.getKey()) {
                case "enc_type":
                    inspectionHook.encType = attribute.getValue().toString();
                    relaventAttributes++;
                    break;
                case "inspectionhook_id":
                    inspectionHook.id = attribute.getValue().toString();
                    relaventAttributes++;
                    break;
                case "inspection_port_id":
                    inspectionHook.inspectionPortId = attribute.getValue().toString();
                    relaventAttributes++;
                    break;
                case "inspected_port_id":
                    inspectionHook.inspectedPortId = attribute.getValue().toString();
                    relaventAttributes++;
                    break;
                case "tag":
                    inspectionHook.tag = new Double(attribute.getValue().toString()).longValue();
                    relaventAttributes++;
                    break;
                case "order":
                    inspectionHook.order = new Double(attribute.getValue().toString()).longValue();
                    relaventAttributes++;
                    break;
                case "failure_policy_type":
                    inspectionHook.failurePolicyType = attribute.getValue().toString();
                    relaventAttributes++;
                    break;
            }
        }
        return relaventAttributes >= 6 ? inspectionHook : null;
    }

    /**
     * Adds the inspection hook attributes to a binding profile map and returns the updated map
     *
     */
    public static ImmutableMap<String, Object> updateBindingProfile(InspectionHook inspectionHook,
            ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);

        updatedPortProfile.put("enc_type", inspectionHook.encType);

        updatedPortProfile.put("inspectionhook_id", inspectionHook.id);

        updatedPortProfile.put("inspection_port_id", inspectionHook.inspectionPortId);

        updatedPortProfile.put("inspected_port_id", inspectionHook.inspectedPortId);

        updatedPortProfile.put("tag", inspectionHook.tag);

        updatedPortProfile.put("order", inspectionHook.order);

        updatedPortProfile.put("failure_policy_type", inspectionHook.failurePolicyType);

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static ImmutableMap<String, Object> removeBindingProfile(ImmutableMap<String, Object> existingPortProfile) {
        Map<String, Object> updatedPortProfile = new HashMap<>(existingPortProfile);

        updatedPortProfile.remove("enc_type");
        updatedPortProfile.remove("inspectionhook_id");
        updatedPortProfile.remove("inspection_port_id");
        updatedPortProfile.remove("inspected_port_id");
        updatedPortProfile.remove("tag");
        updatedPortProfile.remove("order");
        updatedPortProfile.remove("failure_policy_type");

        return ImmutableMap.copyOf(updatedPortProfile);
    }

    public static boolean isInspectionHookRegistered(Port port) {
        return generateInspectionHookFromPort(port) != null;
    }
}
