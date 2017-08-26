package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class InspectionHookEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "hookId", unique = true)
    private String hookId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "inspectedPortId", nullable = true, updatable = true)
    private NetworkElementEntity inspectedPort;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "inspectionPortId", nullable = true, updatable = true)
    private InspectionPortEntity inspectionPort;
    private Long tag;
    private Long hookOrder;
    private String encType;
    private String failurePolicyType;

    public InspectionHookEntity() {
    }

    public String getHookId() {
        return this.hookId;
    }

    public void setHookId(String hookId) {
        this.hookId = hookId;
    }

    public NetworkElementEntity getInspectedPort() {
        return this.inspectedPort;
    }

    public void setInspectedPort(NetworkElementEntity inspectedPort) {
        this.inspectedPort = inspectedPort;
    }

    public InspectionPortEntity getInspectionPort() {
        return this.inspectionPort;
    }

    public void setInspectionPort(InspectionPortEntity inspectionPort) {
        this.inspectionPort = inspectionPort;
    }

    public Long getTag() {
        return this.tag;
    }

    public void setTag(Long tag) {
        this.tag = tag;
    }

    public Long getHookOrder() {
        return this.hookOrder;
    }

    public void setHookOrder(Long hookOrder) {
        this.hookOrder = hookOrder;
    }

    public String getEncType() {
        return this.encType;
    }

    public void setEncType(String encType) {
        this.encType = encType;
    }

    public String getFailurePolicyType() {
        return this.failurePolicyType;
    }

    public void setFailurePolicyType(String failurePolicyType) {
        this.failurePolicyType = failurePolicyType;
    }
}
