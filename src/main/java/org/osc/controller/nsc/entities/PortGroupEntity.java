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

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.osc.sdk.controller.element.NetworkElement;

@Entity
@Table(name = "PORT_GROUP")
public class PortGroupEntity implements NetworkElement {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "element_id", unique = true)
    private String elementId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, mappedBy="portGroup")
    private Set<PortEntity> virtualPorts;

    @Column(name = "parent_id")
    private String parentId;

    public PortGroupEntity() {
    }

    public PortGroupEntity(String elementId, String parentId, Set<PortEntity> virtualPorts) {
        this.elementId = elementId;
        this.parentId = parentId;
        this.virtualPorts = virtualPorts;
    }

    @Override
    public String getElementId() {
        return this.elementId;
    }

    public void setId(String elementId) {
        this.elementId = elementId;
    }

    public void setVirtualPorts(Set<PortEntity> virtualPorts) {
        this.virtualPorts = virtualPorts;
    }

    public Set<PortEntity> getVirtualPorts() {
        return this.virtualPorts;
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }

    @Override
    public List<String> getMacAddresses() {
        return null;
    }

    @Override
    public List<String> getPortIPs() {
        return null;
    }
}
