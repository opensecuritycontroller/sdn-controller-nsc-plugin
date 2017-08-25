package org.osc.controller.nsc.utils;

import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;


public class InspectionHookElementImpl implements InspectionHookElement {

    private Long tag;
    private Long hookOrder;
    private InspectionPortElement inspectionPort;
    private NetworkElement inspected;
    private String hookId;
    private FailurePolicyType policyType = FailurePolicyType.NA;
    private TagEncapsulationType encType = TagEncapsulationType.VLAN;

    public InspectionHookElementImpl() {}

    public InspectionHookElementImpl(Long tag, Long hookOrder, InspectionPortElement inspectionPort,
            NetworkElement inspected, String hookId, FailurePolicyType policyType, TagEncapsulationType encType) {
        super();
        this.tag = tag;
        this.hookOrder = hookOrder;
        this.inspectionPort = inspectionPort;
        this.inspected = inspected;
        this.hookId = hookId;

        if (policyType != null) {
            this.policyType = policyType;
        }

        if (encType != null) {
            this.encType = encType;
        }
    }

    public InspectionHookElementImpl(Long tag, Long hookOrder, InspectionPortElement inspectionPort,
            NetworkElement inspected, String hookId, String policyType, String encType) {
        super();
        this.tag = tag;
        this.hookOrder = hookOrder;
        this.inspectionPort = inspectionPort;
        this.inspected = inspected;
        this.hookId = hookId;

        if (policyType != null) {
            this.policyType = FailurePolicyType.fromText(policyType);
        }

        if (encType != null) {
            this.encType = TagEncapsulationType.fromText(encType);
        }
    }


    @Override
    public Long getTag() {
        return this.tag;
    }

    @Override
    public Long getOrder() {
        return this.hookOrder;
    }

    @Override
    public InspectionPortElement getInspectionPort() {
        return this.inspectionPort; // TODO
    }

    @Override
    public NetworkElement getInspectedPort() {
        return this.inspected;
    }

    @Override
    public String getHookId() {
        return this.hookId;
    }

    @Override
    public FailurePolicyType getFailurePolicyType() {
        return this.policyType;
    }

    @Override
    public TagEncapsulationType getEncType() {
        return this.encType;
    }
};
