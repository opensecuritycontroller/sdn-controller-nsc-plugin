package org.osc.controller.nsc.entities;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.FetchType.EAGER;
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

import org.osc.sdk.controller.element.NetworkElement;

@Entity
public class NetworkElementEntity implements NetworkElement {

    @Id
    @Column(name = "elementId", unique = true)
    private String elementId;

    // TODO : for SFC functionality
    @Transient
    private String parentId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = EAGER, mappedBy = "element")
    private List<MacAddressEntity> macAddressEntities;

    // Making this EAGER resulted in:
    // org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "element")
    private List<PortIpEntity> portIpEntities;

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

    public NetworkElementEntity(String elementId, List<MacAddressEntity> macAddressEntities,
            List<PortIpEntity> portIpEntities, String parentId) {
        super();
        this.elementId = elementId;
        this.parentId = parentId;
        this.macAddressEntities = macAddressEntities;
        this.portIpEntities = portIpEntities;
    }


    @Override
    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

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
    @Transient
    public List<String> getPortIPs() {
        return this.portIpEntities != null ? this.portIpEntities.stream().map(PortIpEntity::getPortIp).collect(toList())
                : emptyList();
    }

    @Override
    @Transient
    public List<String> getMacAddresses() {
        return this.macAddressEntities != null
                ? this.macAddressEntities.stream().map(MacAddressEntity::getMacAddress).collect(toList()) : emptyList();
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }


}
