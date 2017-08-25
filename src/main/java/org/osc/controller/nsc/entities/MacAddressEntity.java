package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class MacAddressEntity {

    private Long id;
    private NetworkElementEntity element;
    private String macAddress;

    public MacAddressEntity() {

    }

    public MacAddressEntity(Long id, String macAddress, NetworkElementEntity element) {
        this.id = id;
        this.macAddress = macAddress;
        this.element = element;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "elementId")
    public NetworkElementEntity getElement() {
        return this.element;
    }

    public void setElement(NetworkElementEntity element) {
        this.element = element;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
