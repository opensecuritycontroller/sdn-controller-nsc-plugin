package org.osc.controller.nsc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.osc.sdk.controller.element.NetworkElement;

public class NetworkElementImpl implements NetworkElement {

    private String elementId;
    private String parentId;
    private List<String> macAddresses = new ArrayList<>();
    private List<String> portIPs = new ArrayList<>();

    public NetworkElementImpl() {
    }

    public NetworkElementImpl(String elementId, String parentId, List<String> macAddresses, List<String> portIPs) {
        super();
        this.elementId = elementId;
        this.parentId = parentId;
        this.macAddresses = macAddresses;
        this.portIPs = portIPs;
    }

    public NetworkElementImpl(String elementId, String parentId, String macAddress, String portIP) {
        super();
        this.elementId = elementId;
        this.parentId = parentId;
        this.macAddresses.add(macAddress);
        this.portIPs.add(portIP);
    }

    @Override
    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public List<String> getMacAddresses() {
        return this.macAddresses;
    }

    @Override
    public List<String> getPortIPs() {
        return this.portIPs;
    }

    public void setPortIps(Collection<String> portIps) {
        this.portIPs = new ArrayList<>(portIps);
    }
}
