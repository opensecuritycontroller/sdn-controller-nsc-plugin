package com.intelsecurity.isc.controller.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InspectionHookInput {

    @XmlElement(name = "inspection_hook")
    public InspectionHook inspectionHook;

    @Override
    public String toString() {
        return "InspectionHookInput [inspectionHook=" + inspectionHook + "]";
    }
}
