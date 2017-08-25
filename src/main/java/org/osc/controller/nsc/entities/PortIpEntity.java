package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class PortIpEntity {

    private Long id;
    private NetworkElementEntity element;
    private String portIp;

    public PortIpEntity() {

    }

    public PortIpEntity(Long id, String portIp, NetworkElementEntity element) {
        this.id = id;
        this.portIp = portIp;
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

    public String getPortIp() {
        return this.portIp;
    }

    public void setPortIp(String portIp) {
        this.portIp = portIp;
    }
}
