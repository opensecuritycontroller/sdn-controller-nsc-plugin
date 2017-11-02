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
import static org.osc.controller.nsc.TestData.*;
import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osc.sdk.controller.element.Element;
import org.powermock.modules.junit4.PowerMockRunner;;

@RunWith(PowerMockRunner.class)
public class RedirectionApiUtilsTest extends AbstractSampleSdnPluginTest {

    private RedirectionApiUtils utils;

    @Before
    @Override
    public void setup() {
        super.setup();
        this.utils = new RedirectionApiUtils(this.em, this.txControl);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        InMemDB.close();
    }

    @Test
    public void testUtils_PersistHookOnly_InspectionPortByNetworkElementsFound() throws Exception {
        // Arrange.
        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        // Act.
        InspectionPortEntity foundPort = this.utils.findInspPortByNetworkElements(ingress, egress);

        // Assert.
        assertNotNull(foundPort);
        assertEquals(inspectionPort.getElementId(), foundPort.getElementId());
    }

    @Test
    public void testUtils_PersistHookOnly_InspectedNetworkElementByElementIdFound() throws Exception {
        // Arrange.
        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        // Act.
        NetworkElementEntity foundNE = this.txControl.required(() -> {
            NetworkElementEntity e = this.utils.txNetworkElementEntityByElementId(inspected.getElementId());
            return e;
        });

        // Assert.
        assertNotNull(foundNE);
        assertNotNull(foundNE.getMacAddresses());
        assertEquals(1, foundNE.getMacAddresses().size());

    }

    @Test
    public void testUtils_PersistHookOnly_InspHookByInspectedAndPortFound() throws Exception {
        // Arrange.
        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        // Act.
        InspectionHookEntity foundIH = this.txControl.required(() -> {
            return this.utils.findInspHookByInspectedAndPort(inspected, inspectionPort);
        });

        // Assert.
        assertEquals(foundIH.getHookId(), inspectionHook.getHookId());
        assertNotNull(foundIH.getInspectionPort());
        assertEquals(foundIH.getInspectionPort().getElementId(), inspectionPort.getElementId());

    }

    @Test
    public void testUtils_RemoveInstalledInspectionHook_InspectionHookDisappears() throws Exception {
        // Arrange.
        @SuppressWarnings("resource")
        SampleSdnRedirectionApi redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // expected before installInspectionHook
        Element registeredElement = redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        InspectionHookEntity inspectionHookEntity = this.txControl.required(() -> {
            return this.em.find(InspectionHookEntity.class, hookId);
        });
        assertNotNull(inspectionHookEntity);

        // Act.
        this.utils.removeSingleInspectionHook(hookId);

        inspectionHookEntity = this.txControl.required(() -> {
            return this.em.find(InspectionHookEntity.class, hookId);
        });

        // Assert.
        assertEquals(null, inspectionHookEntity);
    }
}
