package org.osc.controller.nsc.entities;


import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
	
    @Id
    @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="inspectionPort", optional=true, targetEntity=NetworkElementNSCEntity.class)
	@JoinColumn(name="ingressId", nullable=true, unique=true, updatable=true)
	public NetworkElementNSCEntity getIngressId() {
		return ingress;
	}
	public void setIngressId(NetworkElementNSCEntity ingressId) { 	
		this.ingress = ingressId;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="inspectionPort", optional=true, targetEntity=NetworkElementNSCEntity.class)
	@JoinColumn(name="egressId", nullable=true, unique=true, updatable=true)
	public NetworkElementNSCEntity getEgressId() {
		return egress;
	}
	public void setEgressId(NetworkElementNSCEntity egressId) {
		this.egress = egressId;
	}
	
//	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="inspectionPort", optional=true)
//	@JoinColumn(name="ingressId", nullable=true)

}
