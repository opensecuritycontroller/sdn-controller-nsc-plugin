package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class InspectionPortEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "ingressId", nullable = true, updatable = true)
    private NetworkElementEntity ingress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "egressId", nullable = true, updatable = true)
    private NetworkElementEntity egress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "inspectionHookId", nullable = true, updatable = true)
    private InspectionHookEntity inspectionHook;

    public InspectionPortEntity() {
    }

    public InspectionPortEntity(Long id, NetworkElementEntity ingress, NetworkElementEntity egress,
            InspectionHookEntity inspectionHook) {
        this.id = id;
        this.ingress = ingress;
        this.egress = egress;
        this.inspectionHook = inspectionHook;

        if (ingress != null) {
            ingress.setIngressInspectionPort(this);
        }

        if (egress != null) {
            egress.setEgressInspectionPort(this);
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NetworkElementEntity getIngress() {
        return this.ingress;
    }

    public void setIngress(NetworkElementEntity ingress) {
        this.ingress = ingress;
    }

    public NetworkElementEntity getEgress() {
        return this.egress;
    }

    public void setEgress(NetworkElementEntity egress) {
        this.egress = egress;
    }

    public InspectionHookEntity getInspectionHook() {
        return this.inspectionHook;
    }

    public void setInspectionHook(InspectionHookEntity inspectionHook) {
        this.inspectionHook = inspectionHook;
    }
}
