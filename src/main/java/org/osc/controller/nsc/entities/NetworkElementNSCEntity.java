package org.osc.controller.nsc.entities;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.FetchType.LAZY;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="NetworkElement")
public class NetworkElementNSCEntity {
	
	
	private String elementId;
	
	private List<MacAddressNSCEntity> macAddressEntities;
	private List<PortIpNSCEntity> portIpEntities;
	
	@Column(name="ingressPortId")
	private InspectionPortNSCEntity ingressInspectionPort;
	
	@Column(name="egressPortId")
	private InspectionPortNSCEntity egressInspectionPort;
	
	@Column(name="inspectionHookId")
	private InspectionHookNSCEntity inspectionHook;
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "elementId", unique = true)
	public String getElementId() {
		return elementId;
	}
	
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="element", targetEntity=MacAddressNSCEntity.class)
	public List<MacAddressNSCEntity> getMacAddressEntities() {
		return macAddressEntities;
	}
	public void setMacAddressEntities(List<MacAddressNSCEntity> macAddressEntities) {
		this.macAddressEntities = macAddressEntities;
	}
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=LAZY, mappedBy="element", targetEntity=PortIpNSCEntity.class)
	public List<PortIpNSCEntity> getPortIpEntities() {
		return portIpEntities;
	}
	public void setPortIpEntities(List<PortIpNSCEntity> portIpEntities) {
		this.portIpEntities = portIpEntities;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=false, fetch=LAZY, optional=true)
	@JoinColumn(name="ingressPortId", nullable=true, updatable=true)
	public InspectionPortNSCEntity getIngressInspectionPort() {
		return ingressInspectionPort;
	}
	public void setIngressInspectionPort(InspectionPortNSCEntity inspectionPort) {
		this.ingressInspectionPort = inspectionPort;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=false, fetch=LAZY, optional=true)
	@JoinColumn(name="egressPortId", nullable=true, updatable=true)
	public InspectionPortNSCEntity getEgressInspectionPort() {
		return egressInspectionPort;
	}
	public void setEgressInspectionPort(InspectionPortNSCEntity inspectionPort) {
		this.egressInspectionPort = inspectionPort;
	}

	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=false, fetch=LAZY, optional=true)
	@JoinColumn(name="inspectionHookId", nullable=true, updatable=true)
	public InspectionHookNSCEntity getInspectionHook() {
		return inspectionHook;
	}
	public void setInspectionHook(InspectionHookNSCEntity inspectionHook) {
		this.inspectionHook = inspectionHook;
	}

	
	@Transient
	public List<String> getPortIps() {
		return portIpEntities != null ? portIpEntities.stream().map(PortIpNSCEntity::getPortIp).collect(toList())
				: emptyList();
	}
	
	@Transient
	public List<String> getMacAddresses() {
		return macAddressEntities != null ? macAddressEntities.stream().map(MacAddressNSCEntity::getMacAddress).collect(toList())
				: emptyList();
	}
}
