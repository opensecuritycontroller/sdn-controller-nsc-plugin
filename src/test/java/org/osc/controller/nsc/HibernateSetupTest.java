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
package org.osc.controller.nsc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.osc.controller.nsc.TestData.inspectionHook;
import static org.osc.controller.nsc.TestData.inspectionPort;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class HibernateSetupTest extends AbstractSampleSdnPluginTest {

    @Test
    public void testDb_PersistInspection_CorrectNumberOfMacsAdPortIps() throws Exception {
        // Arrange.
        assertEquals(null, inspectionPort.getElementId());

        // Act.
        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        InspectionPortEntity tmp = this.txControl.requiresNew(() -> {
            return this.em.find(InspectionPortEntity.class, inspectionPort.getElementId());
        });

        // Assert.
        assertNotNull(inspectionPort.getElementId());
        assertEquals(2, tmp.getEgressPort().getMacAddresses().size());
        assertEquals(2, tmp.getEgressPort().getPortIPs().size());
        assertEquals(2, tmp.getIngressPort().getMacAddresses().size());
        assertEquals(2, tmp.getIngressPort().getPortIPs().size());
    }

    @Test
    public void testDb_PersistInspectionHookOnly_BothHookAndPortArePersisted() {
        // Arrange.
        assertEquals(null, inspectionHook.getHookId());

        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });
        assertNotNull(inspectionHook.getHookId());

        // Act.
        InspectionHookEntity persistedHook = this.txControl.required(() -> {
            InspectionHookEntity ph = this.em.find(InspectionHookEntity.class, inspectionHook.getHookId());
            InspectionPortEntity iprt = this.em.find(InspectionPortEntity.class, inspectionPort.getElementId());

            assertEquals(inspectionPort.getElementId(), iprt.getElementId());
            return ph;
        });
        InspectionPortEntity persistedPort = persistedHook.getInspectionPort();

        // Assert.
        assertEquals(inspectionHook.getHookId(), persistedHook.getHookId());
        assertNotNull(persistedPort);
        assertEquals(inspectionHook.getInspectionPort().getElementId(), persistedPort.getElementId());
    }

    @Test
    public void testDb_PersistHookOnly_InspectionPortByIdFound() throws Exception {
        // Arrange.
        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        // Act.
        InspectionPortEntity foundIP = this.txControl.required(() -> {
            return this.em.find(inspectionPort.getClass(), inspectionPort.getElementId());
        });

        // Assert.
        assertNotNull(foundIP);
        assertEquals(foundIP.getElementId(), inspectionPort.getElementId());
        assertNotNull(foundIP.getEgressPort());
        assertEquals(foundIP.getEgressPort().getElementId(), inspectionPort.getEgressPort().getElementId());
        assertNotNull(foundIP.getIngressPort());
        assertEquals(foundIP.getIngressPort().getElementId(), inspectionPort.getIngressPort().getElementId());
    }
}
