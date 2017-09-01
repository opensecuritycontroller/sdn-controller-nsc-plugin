package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.osc.sdk.controller.element.NetworkElement;

@Entity
public class NetworkElementEntity implements NetworkElement {

    @Id
    @Column(name = "elementId", unique = true)
    private String elementId;

    // TODO : for SFC functionality
    @Transient
    private String parentId;

    @ElementCollection(fetch = EAGER)
    private List<String> macAddresses;

    @ElementCollection(fetch = LAZY)
    private List<String> portIPs;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "ingressPortId", nullable = true, updatable = true)
    private InspectionPortEntity ingressInspectionPort;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "egressPortId", nullable = true, updatable = true)
    private InspectionPortEntity egressInspectionPort;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "inspectionHookId", nullable = true, updatable = true)
    private InspectionHookEntity inspectionHook;

    public NetworkElementEntity() {
    }

    public NetworkElementEntity(String elementId, List<String> macAddressEntities,
            List<String> portIpEntities, String parentId) {
        super();
        this.elementId = elementId;
        this.parentId = parentId;
        this.macAddresses = macAddressEntities;
        this.portIPs = portIpEntities;
    }


    @Override
    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public InspectionPortEntity getIngressInspectionPort() {
        return this.ingressInspectionPort;
    }

    public void setIngressInspectionPort(InspectionPortEntity inspectionPort) {
        this.ingressInspectionPort = inspectionPort;
    }

    public InspectionPortEntity getEgressInspectionPort() {
        return this.egressInspectionPort;
    }

    public void setEgressInspectionPort(InspectionPortEntity inspectionPort) {
        this.egressInspectionPort = inspectionPort;
    }

    public InspectionHookEntity getInspectionHook() {
        return this.inspectionHook;
    }

    public void setInspectionHook(InspectionHookEntity inspectionHook) {
        this.inspectionHook = inspectionHook;
    }

    @Override
    public List<String> getPortIPs() {
        return this.portIPs;
    }

    public void setPortIPs(List<String> portIPs) {
        this.portIPs = portIPs;
    }

    @Override
    public List<String> getMacAddresses() {
        return this.macAddresses;
    }

    public void setMacAddresses(List<String> macAddresses) {
        this.macAddresses = macAddresses;
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }
}
