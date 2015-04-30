package com.intelsecurity.isc.controller.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InspectionPortInput {

    @XmlElement(name = "inspection_port")
    public InspectionPort inspectionPort;

    @Override
    public String toString() {
        return "InspectionPort [inspectionPort=" + inspectionPort + "]";
    }
}
