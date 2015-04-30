package com.intelsecurity.isc.controller.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InspectionPorts {

    @XmlElement(name = "inspection_ports")
    public List<InspectionPort> inspectionPorts;

    @Override
    public String toString() {
        return "InspectionPorts [inspectionPorts=" + inspectionPorts + "]";
    }
}
