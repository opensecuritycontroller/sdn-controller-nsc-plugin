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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.osc.sdk.controller.element.InspectionPortElement;

@Entity
@Table(name = "INSPECTION_PORT")
public class InspectionPortEntity implements InspectionPortElement {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "element_id", unique = true)
    private String elementId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "ingress_fk", nullable = true, updatable = true)
    private NetworkElementEntity ingressPort;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "egress_fk", nullable = true, updatable = true)
    private NetworkElementEntity egressPort;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, mappedBy="inspectionPort")
    private Set<InspectionHookEntity> inspectionHooks;

    public InspectionPortEntity() {
    }

    public InspectionPortEntity(String elementId, NetworkElementEntity ingress, NetworkElementEntity egress) {
        this.elementId = elementId;
        this.ingressPort = ingress;
        this.egressPort = egress;
        this.inspectionHooks = new HashSet<>();
    }

    @Override
    public String getElementId() {
        return this.elementId;
    }

    public void setId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public NetworkElementEntity getIngressPort() {
        return this.ingressPort;
    }

    public void setIngressPort(NetworkElementEntity ingressPort) {
        this.ingressPort = ingressPort;
    }

    @Override
    public NetworkElementEntity getEgressPort() {
        return this.egressPort;
    }

    public void setEgressPort(NetworkElementEntity egressPort) {
        this.egressPort = egressPort;
    }

    public Set<InspectionHookEntity> getInspectionHooks() {
        return this.inspectionHooks;
    }

    public void setInspectionHooks(Collection<InspectionHookEntity> inspectionHooks) {
        this.inspectionHooks = new HashSet<>(inspectionHooks);
    }

    @Override
    public String getParentId() {
        // TODO Implement for SFC
        return null;
    }

    @Override
    public String toString() {
        return "InspectionPortEntity [elementId=" + this.elementId + ", ingressPort=" + this.ingressPort + ", egressPort="
                + this.egressPort + ", inspectionHooks=" + this.inspectionHooks + "]";
    }
}
