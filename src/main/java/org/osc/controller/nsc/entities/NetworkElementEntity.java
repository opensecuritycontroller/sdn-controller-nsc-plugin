package org.osc.controller.nsc.entities;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
public class NetworkElementEntity {

    private String elementId;
    private List<MacAddressEntity> macAddressEntities;
    private List<PortIpEntity> portIpEntities;

    @Column(name = "ingressPortId")
    private InspectionPortEntity ingressInspectionPort;

    @Column(name = "egressPortId")
    private InspectionPortEntity egressInspectionPort;

    @Column(name = "inspectionHookId")
    private InspectionHookEntity inspectionHook;

    @Id
    @Column(name = "elementId", unique = true)
    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "element")
    public List<MacAddressEntity> getMacAddressEntities() {
        return this.macAddressEntities;
    }

    public void setMacAddressEntities(List<MacAddressEntity> macAddressEntities) {
        this.macAddressEntities = macAddressEntities;

        if (macAddressEntities != null) {
            for (MacAddressEntity e : macAddressEntities) {
                if (e != null) {
                    e.setElement(this);
                }
            }
        }
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "element")
    public List<PortIpEntity> getPortIpEntities() {
        return this.portIpEntities;
    }

    public void setPortIpEntities(List<PortIpEntity> portIpEntities) {
        this.portIpEntities = portIpEntities;

        if (portIpEntities != null) {
            for (PortIpEntity e : portIpEntities) {
                if (e != null) {
                    e.setElement(this);
                }
            }
        }
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "ingressPortId", nullable = true, updatable = true)
    public InspectionPortEntity getIngressInspectionPort() {
        return this.ingressInspectionPort;
    }

    public void setIngressInspectionPort(InspectionPortEntity inspectionPort) {
        this.ingressInspectionPort = inspectionPort;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "egressPortId", nullable = true, updatable = true)
    public InspectionPortEntity getEgressInspectionPort() {
        return this.egressInspectionPort;
    }

    public void setEgressInspectionPort(InspectionPortEntity inspectionPort) {
        this.egressInspectionPort = inspectionPort;
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = LAZY, optional = true)
    @JoinColumn(name = "inspectionHookId", nullable = true, updatable = true)
    public InspectionHookEntity getInspectionHook() {
        return this.inspectionHook;
    }

    public void setInspectionHook(InspectionHookEntity inspectionHook) {
        this.inspectionHook = inspectionHook;
    }

    @Transient
    public List<String> getPortIps() {
        return this.portIpEntities != null ?
                    this.portIpEntities.stream().map(PortIpEntity::getPortIp).collect(toList())
                    : emptyList();
    }

    @Transient
    public List<String> getMacAddresses() {
        return this.macAddressEntities != null ?
                    this.macAddressEntities.stream().map(MacAddressEntity::getMacAddress).collect(toList())
                    : emptyList();
    }
}
