package com.intelsecurity.isc.controller.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InspectionHooks {

    @XmlElement(name = "inspection_hooks")
    public List<InspectionHook> inspectionHooks;

    @Override
    public String toString() {
        return "InspectionHooks [inspectionHooks=" + inspectionHooks + "]";
    }
}
