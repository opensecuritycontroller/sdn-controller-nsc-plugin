package org.osc.controller.nsc.entities;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;

@Entity
public class InspectionHookEntity implements InspectionHookElement {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "hookId", unique = true)
    private String hookId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "inspectedPortId", nullable = true, updatable = true)
    private NetworkElementEntity inspectedPort;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "inspectionPortId", nullable = true, updatable = true)
    private InspectionPortEntity inspectionPort;
    private Long tag;

    // "order" is a sql keyword. Avoid column named "order"
    @Column(name = "hookOrder")
    private Long order;

    @Enumerated(STRING)
    private TagEncapsulationType encType;

    @Enumerated(STRING)
    private FailurePolicyType failurePolicyType;

    public InspectionHookEntity() {
    }

    @Override
    public String getHookId() {
        return this.hookId;
    }

    public void setHookId(String hookId) {
        this.hookId = hookId;
    }

    @Override
    public NetworkElementEntity getInspectedPort() {
        return this.inspectedPort;
    }

    public void setInspectedPort(NetworkElementEntity inspectedPort) {
        this.inspectedPort = inspectedPort;
    }

    @Override
    public InspectionPortEntity getInspectionPort() {
        return this.inspectionPort;
    }

    public void setInspectionPort(InspectionPortEntity inspectionPort) {
        this.inspectionPort = inspectionPort;
    }

    @Override
    public Long getTag() {
        return this.tag;
    }

    public void setTag(Long tag) {
        this.tag = tag;
    }

    @Override
    public Long getOrder() {
        return this.order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    @Override
    public TagEncapsulationType getEncType() {
        return this.encType;
    }

    public void setEncType(TagEncapsulationType encType) {
        this.encType = encType;
    }

    @Override
    public FailurePolicyType getFailurePolicyType() {
        return this.failurePolicyType;
    }

    public void setFailurePolicyType(FailurePolicyType failurePolicyType) {
        this.failurePolicyType = failurePolicyType;
    }
}
