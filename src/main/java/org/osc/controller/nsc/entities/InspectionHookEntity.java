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

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;

@Entity
@Table(name = "INSPECTION_HOOK")
public class InspectionHookEntity implements InspectionHookElement {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "hook_id", unique = true)
    private String hookId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false, fetch = EAGER, optional = true)
    @JoinColumn(name = "inspected_port_fk", nullable = true, updatable = true)
    private NetworkElementEntity inspectedPort;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = EAGER, optional = true)
    @JoinColumn(name = "inspection_port_fk", nullable = true, updatable = true)
    private InspectionPortEntity inspectionPort;

    private Long tag;

    // "order" is a sql keyword. Avoid column named "order"
    @Column(name = "hook_order")
    private Long hookOrder;

    @Enumerated(STRING)
    @Column(name = "enc_type")
    private TagEncapsulationType encType;

    @Enumerated(STRING)
    @Column(name = "failure_policy_type")
    private FailurePolicyType failurePolicyType;

    public InspectionHookEntity() {
    }

    @Override
    public String getHookId() {
        return this.hookId;
    }

    public void setHookId(String hookId) {
        this.hookId = hookId;
    }

    @Override
    public NetworkElementEntity getInspectedPort() {
        return this.inspectedPort;
    }

    public void setInspectedPort(NetworkElementEntity inspectedPort) {
        this.inspectedPort = inspectedPort;
    }

    @Override
    public InspectionPortEntity getInspectionPort() {
        return this.inspectionPort;
    }

    public void setInspectionPort(InspectionPortEntity inspectionPort) {
        this.inspectionPort = inspectionPort;
    }

    @Override
    public Long getTag() {
        return this.tag;
    }

    public void setTag(Long tag) {
        this.tag = tag;
    }

    @Override
    public Long getOrder() {
        return this.hookOrder;
    }

    public void setOrder(Long order) {
        this.hookOrder = order;
    }

    @Override
    public TagEncapsulationType getEncType() {
        return this.encType;
    }

    public void setEncType(TagEncapsulationType encType) {
        this.encType = encType;
    }

    @Override
    public FailurePolicyType getFailurePolicyType() {
        return this.failurePolicyType;
    }

    public void setFailurePolicyType(FailurePolicyType failurePolicyType) {
        this.failurePolicyType = failurePolicyType;
    }

    @Override
    public String toString() {
        return "InspectionHookEntity [hookId=" + this.hookId + ", inspectedPort=" + this.inspectedPort +
               ", tag=" + this.tag + ", hookOrder=" + this.hookOrder + ", encType=" + this.encType
               + ", failurePolicyType=" + this.failurePolicyType + "]";
    }
}
