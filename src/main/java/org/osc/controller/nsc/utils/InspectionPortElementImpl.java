package org.osc.controller.nsc.utils;

import org.osc.sdk.controller.DefaultInspectionPort;
import org.osc.sdk.controller.element.NetworkElement;

public class InspectionPortElementImpl extends DefaultInspectionPort {

    private String elementId;
    private String parentId;

    public InspectionPortElementImpl() {
    }

    public InspectionPortElementImpl(NetworkElement ingress, NetworkElement egress, String elementId, String parentId) {
        super(ingress, egress, null);

        this.elementId = elementId;
        this.parentId = parentId;
    }

    @Override
    public String getElementId() {
        return this.elementId;
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }
}
