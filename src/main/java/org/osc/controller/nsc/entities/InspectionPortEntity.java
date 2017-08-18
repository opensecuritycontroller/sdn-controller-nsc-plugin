package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class InspectionPortEntity {

	private Long id;

	@Column(name = "ingressId")
	private NetworkElementEntity ingress;

	@Column(name = "egressId")
	private NetworkElementEntity egress;

	@Column(name = "inspectionHookId")
	private InspectionHookEntity inspectionHook;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
	@JoinColumn(name = "ingressId", nullable = true, updatable = true)
	public NetworkElementEntity getIngress() {
		return ingress;
	}

	public void setIngress(NetworkElementEntity ingress) {
		this.ingress = ingress;
	}

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
	@JoinColumn(name = "egressId", nullable = true, updatable = true)
	public NetworkElementEntity getEgress() {
		return egress;
	}

	public void setEgress(NetworkElementEntity egress) {
		this.egress = egress;
	}

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
	@JoinColumn(name = "inspectionHookId", nullable = true, updatable = true)
	public InspectionHookEntity getInspectionHook() {
		return inspectionHook;
	}

	public void setInspectionHook(InspectionHookEntity inspectionHook) {
		this.inspectionHook = inspectionHook;
	}

}
