package org.osc.controller.nsc.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.osc.controller.nsc.model.InspectionHook;
import org.osc.sdk.controller.element.InspectionPortElement;


@Entity
@Table(name="InspectionHook")
public class InspectionHookNSCEntity {
	
	private Long id;
	private String hookId;
	private String inspectedPortId;
//    private InspectionPortElement inspectionPort; // TODOS
    private Long tag;
    private Long hookOrder;
    private String encType;
    private String failurePolicyType;
	
    public InspectionHookNSCEntity() {}
    
    public InspectionHookNSCEntity(InspectionHook hook) {
    	this.hookId = hook.getHookId();
    	this.inspectedPortId = hook.getInspectedPortId();
    	this.tag = hook.getTag();
    	this.hookOrder = hook.getOrder();
    	this.encType = hook.getEncType().name();
    	this.failurePolicyType = hook.getFailurePolicyType().name();    	
    }
    
	
    @Id
    @GeneratedValue
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getHookId() {
		return hookId;
	}
	public void setHookId(String hookId) {
		this.hookId = hookId;
	}
	
	public String getInspectedPortId() {
		return inspectedPortId;
	}
	public void setInspectedPortId(String inspectedPortId) {
		this.inspectedPortId = inspectedPortId;
	}
//	public InspectionPortElement getInspectionPort() {
//		return inspectionPort;
//	}
//	public void setInspectionPort(InspectionPortElement inspectionPort) {
//		this.inspectionPort = inspectionPort;
//	}
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
