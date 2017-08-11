package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name="InspectionHook")
public class InspectionHookNSCEntity {
	
	private String hookId;
	private NetworkElementNSCEntity inspectedPort;
    private InspectionPortNSCEntity inspectionPort; 
    private Long tag;
    private Long hookOrder;
    private String encType;
    private String failurePolicyType;
	
    public InspectionHookNSCEntity() {}
    
	
	@Id
	public String getHookId() {
		return hookId;
	}
	public void setHookId(String hookId) {
		this.hookId = hookId;
	}
	
	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=false, fetch=LAZY, optional=true, mappedBy="inspectionHook")
	@JoinColumn(name="inspectedPortId", nullable=true, unique=true, updatable=true)
	public NetworkElementNSCEntity getInspectedPort() {
		return inspectedPort;
	}
	public void setInspectedPort(NetworkElementNSCEntity inspectedPort) {
		this.inspectedPort = inspectedPort;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, optional=true)
	@JoinColumn(name="inspectionPortId", nullable=true, unique=true, updatable=true)
	public InspectionPortNSCEntity getInspectionPort() {
		return inspectionPort;
	}
	public void setInspectionPort(InspectionPortNSCEntity inspectionPort) {
		this.inspectionPort = inspectionPort;
	}
	public Long getTag() {
		return tag;
	}
	public void setTag(Long tag) {
		this.tag = tag;
	}
	public Long getHookOrder() {
		return hookOrder;
	}
	public void setHookOrder(Long hookOrder) {
		this.hookOrder = hookOrder;
	}
	public String getEncType() {
		return encType;
	}
	public void setEncType(String encType) {
		this.encType = encType;
	}
	public String getFailurePolicyType() {
		return failurePolicyType;
	}
	public void setFailurePolicyType(String failurePolicyType) {
		this.failurePolicyType = failurePolicyType;
	}

}