package com.intelsecurity.isc.controller.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.intelsecurity.isc.plugin.controller.DefaultNetworkPortElement;
import com.intelsecurity.isc.plugin.controller.FailurePolicyType;
import com.intelsecurity.isc.plugin.controller.element.InspectionHookElement;
import com.intelsecurity.isc.plugin.controller.element.NetworkPortElement;
import com.intelsecurity.isc.plugin.controller.element.TagEncapsulationType;

@XmlRootElement(name = "inspection_hook")
@XmlAccessorType(XmlAccessType.FIELD)
public class InspectionHook implements InspectionHookElement {

    @XmlElement
    public String id;

    @XmlElement(name = "inspected_port_id")
    public String inspectedPortId;

    @XmlElement(name = "inspection_port_id")
    public String inspectionPortId;

    @XmlElement
    public Long tag;

    @XmlElement
    public Long order;

    @XmlElement(name = "enc_type")
    public String encType;

    @XmlElement(name = "failure_policy_type")
    public String failurePolicyType;

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
        return "InspectionHook [id=" + id + ", inspectedPortId=" + inspectedPortId + ", inspectionPortId="
                + inspectionPortId + ", order=" + order + ", tag=" + tag + "]";
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
        return FailurePolicyType.fromText(this.failurePolicyType);
    }

    @Override
    public String getHookId() {
        return this.id;
    }

    @Override
    public NetworkPortElement getInspectedPort() {
        return new DefaultNetworkPortElement(this.inspectedPortId, null);
    }

    @Override
    public NetworkPortElement getInspectionPort() {
        return new DefaultNetworkPortElement(this.inspectionPortId, null);
    }
}
