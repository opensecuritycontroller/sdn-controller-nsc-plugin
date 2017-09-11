/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc.entities;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.osc.sdk.controller.element.NetworkElement;

@Entity
@Table(name = "NETWORK_ELEMENT")
public class NetworkElementEntity implements NetworkElement {

    @Id
    @Column(name = "element_id", unique = true)
    private String elementId;

    // TODO : for SFC functionality
    @Transient
    private String parentId;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "NETWORK_ELEMENT_MACADDRESSES",
            joinColumns = @JoinColumn(name = "network_element_fk"),
            foreignKey = @ForeignKey(name = "FK_NETWORK_ELEMENT_MACADDRESSES_NETWORK_ELEMENT"))
    private List<String> macAddresses;

    @ElementCollection(fetch = LAZY)
    @CollectionTable(name = "NETWORK_ELEMENT_PORTIPS",
            joinColumns = @JoinColumn(name = "network_element_fk"),
            foreignKey = @ForeignKey(name = "FK_NETWORK_ELEMENT_PORTIPS_NETWORK_ELEMENT"))
    private List<String> portIPs;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "inspection_hook_fk", nullable = true, updatable = true)
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

    @Override
    public String toString() {
        return "NetworkElementEntity [elementId=" + this.elementId + ", parentId=" + this.parentId + ", macAddresses="
                + this.macAddresses + ", portIPs=" + this.portIPs + "]";
    }
}
