package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="MacAddress")
public class MacAddressNSCEntity {

	private Long id;
	private NetworkElementNSCEntity element;
	private String macAddress;
	
	@Id
    @GeneratedValue
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=LAZY)
    @JoinColumn(name="elementId")
	public NetworkElementNSCEntity getElement() {
		return element;
	}
	public void setElement(NetworkElementNSCEntity element) {
		this.element = element;
	}
	
    public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	

}
