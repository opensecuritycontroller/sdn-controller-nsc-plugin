package org.osc.controller.nsc.entities;


import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="InspectionPort")
public class InspectionPortNSCEntity {

	private Long id;
	private NetworkElementNSCEntity ingress;
	private NetworkElementNSCEntity egress;
	
	@Column(name="inspectionHookId")
	private InspectionHookNSCEntity inspectionHook;
	
	@Id
    @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=false, fetch=LAZY, mappedBy="ingressInspectionPort", optional=true)
	@JoinColumn(name="ingressId", nullable=true, unique=true, updatable=true)
	public NetworkElementNSCEntity getIngress() {
		return ingress;
	}
	public void setIngress(NetworkElementNSCEntity ingress) { 	
		this.ingress = ingress;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=false, fetch=LAZY, mappedBy="egressInspectionPort", optional=true)
	@JoinColumn(name="egressId", nullable=true, unique=true, updatable=true)
	public NetworkElementNSCEntity getEgress() {
		return egress;
	}
	public void setEgress(NetworkElementNSCEntity egress) {
		this.egress = egress;
	}
	
	@OneToOne(mappedBy="inspectionPort")
	@JoinColumn(name="inspectionHookId", nullable=true)
    public InspectionHookNSCEntity getInspectionHook() {
		return inspectionHook;
	}
	public void setInspectionHook(InspectionHookNSCEntity inspectionHook) {
		this.inspectionHook = inspectionHook;
	}

}
