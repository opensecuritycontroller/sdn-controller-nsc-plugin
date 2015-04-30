package com.intelsecurity.isc.controller.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "inspection_port")
@XmlAccessorType(XmlAccessType.FIELD)
public class InspectionPort {

    @XmlElement
    public String id;

    @XmlElement(name = "port_id")
    public String portId;

    @XmlElement(name = "inspection_hooks")
    public List<InspectionHook> inspectionHooks;

    @Override
    public String toString() {
        return "InspectedPort [id=" + id + ", portId=" + portId + ", inspectionHooks=" + inspectionHooks + "]";
    }
}
