package org.osc.controller.nsc.entities;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.FetchType.LAZY;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="NetworkElement")
public class NetworkElementNSCEntity {
	
	private Long id;
	

	private String elementId;
	
	private List<MacAddressNSCEntity> macAddressEntities;
	private List<PortIpNSCEntity> portIpEntities;
	
	private InspectionPortNSCEntity inspectionPort;
	
	@Id
    @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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

	@OneToOne(fetch=LAZY, optional=false)
	@JoinColumn(name="inspectionPortId", nullable=true)
	public InspectionPortNSCEntity getInspectionPort() {
		return inspectionPort;
	}
	public void setInspectionPort(InspectionPortNSCEntity inspectionPort) {
		this.inspectionPort = inspectionPort;
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
