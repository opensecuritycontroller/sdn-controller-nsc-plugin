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

import static org.junit.Assert.*;
import static org.osc.controller.nsc.TestData.*;
import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.PortEntity;
import org.osc.controller.nsc.entities.PortGroupEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osc.sdk.controller.element.Element;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;

@RunWith(MockitoJUnitRunner.class)
public class SampleSdnRedirectionApiTest extends AbstractSampleSdnPluginTest {

    private SampleSdnRedirectionApi redirApi;

    @Before
    @Override
    public void setup() {
        super.setup();
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);
    }

    @Test
    public void testApi_RegisterInspectionPort_NoLazyInitExceptionOnReturnedInspectionPortElement() throws Exception {
        // Arrange.
        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // Act.
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        // Assert.
        // Here we are mostly afraid of LazyInitializationException
        assertNotNull(inspectionPortElement.getElementId());
        inspectionPortElement.getParentId();
        assertNotNull(inspectionPortElement.getIngressPort());
        assertNotNull(inspectionPortElement.getEgressPort());
        assertNotNull(inspectionPortElement.getEgressPort().getMacAddresses());
        assertNotNull(inspectionPortElement.getEgressPort().getElementId());
        assertNotNull(inspectionPortElement.getIngressPort());
        assertNotNull(inspectionPortElement.getIngressPort().getMacAddresses());
        assertNotNull(inspectionPortElement.getIngressPort().getElementId());
    }

    @Test
    public void testApi_RegisterInspectionPort_NoLazyInitExceptionOnIngressElement() throws Exception {

        // Arrange.
        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);
        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // Act.
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        // Assert.
        PortEntity foundIngress = this.txControl.required(() -> {
            return utils.txPortEntityByElementId(ingress.getElementId());
        });

        assertNotNull(foundIngress);
        assertEquals(inspectionPortElement.getIngressPort().getElementId(), foundIngress.getElementId());

        // Here we are afraid of lazyInitializationException
        foundIngress.getMacAddresses();
        foundIngress.getPortIPs();
        foundIngress.getElementId();
        foundIngress.getParentId();
    }

    @Test
    public void testApi_RegisterInspectionPort_NoLazyInitExceptionOnEgressElement() throws Exception {

        // Arrange.
        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);
        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // Act.
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        // Assert.
        PortEntity foundEgress = this.txControl.required(() -> {
            return utils.txPortEntityByElementId(egress.getElementId());
        });

        assertNotNull(foundEgress);
        assertEquals(inspectionPortElement.getEgressPort().getElementId(), foundEgress.getElementId());

        // Here we are afraid of lazyInitializationException
        foundEgress.getMacAddresses();
        foundEgress.getPortIPs();
        foundEgress.getElementId();
        foundEgress.getParentId();
    }

    @Test
    public void testApi_RegisterInspectionPort_NoLazyInitOnGetInspectionPortReturnValue() throws Exception {
        // Arrange.
        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // Act.
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        // Assert.
        InspectionPortElement foundInspPortElement = this.redirApi.getInspectionPort(inspectionPortElement);

        // Here we are afraid of lazyInitializationException
        assertEquals(inspectionPortElement.getIngressPort().getElementId(),
                foundInspPortElement.getIngressPort().getElementId());
        assertEquals(inspectionPortElement.getEgressPort().getElementId(),
                foundInspPortElement.getEgressPort().getElementId());
        assertEquals(inspectionPortElement.getElementId(), foundInspPortElement.getElementId());

        assertEquals(null, inspectionPortElement.getParentId());
        assertEquals(null, foundInspPortElement.getParentId());
    }

    @Test
    public void testApi_RegisterInspectionPortWithNetworkElementsAlreadyPersisted_Succeeds() throws Exception {
        // Arrange.
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        this.txControl.required(() -> {
            this.em.persist(ingress);
            return null;
        });

        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // Act.
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        // Assert.
        assertNotNull(inspectionPortElement);
        assertNotNull(inspectionPortElement.getElementId());
        assertNotNull(inspectionPortElement.getIngressPort());
        assertNotNull(inspectionPortElement.getEgressPort());
        assertEquals(ingress.getElementId(), inspectionPortElement.getIngressPort().getElementId());
        assertEquals(egress.getElementId(), inspectionPortElement.getEgressPort().getElementId());
    }

    // Test applicable only for SUPPORTS_PORT_GROUP_VALUE == true, the default current value is false
    //@Test
    public void testRegisterNetworkElement_NetworkElementCreated() throws Exception {
        // Arrange.
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);
        ingress.setParentId(UUID.randomUUID().toString());
        this.txControl.required(() -> {
            this.em.persist(ingress);
            return null;
        });

        // Act.
        NetworkElement portGroup = this.redirApi.registerNetworkElement(Arrays.asList(ingress));

        // Assert.
        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        PortGroupEntity createdPortGroupEntity = utils.findPortGroupEntity(portGroup.getElementId(), portGroup.getParentId());

        PortEntity updatedVirtualPort = utils.findPortEntityByElementId(ingress.getElementId());

        assertNotNull(createdPortGroupEntity);
        assertNotNull(createdPortGroupEntity.getElementId());
        assertNotNull(createdPortGroupEntity.getParentId());
        assertNotNull(updatedVirtualPort.getPortGroup());
        assertEquals(portGroup.getElementId(), updatedVirtualPort.getPortGroup().getElementId());
    }

    // Test applicable only for SUPPORTS_PORT_GROUP_VALUE == true, the default current value is false
    //@Test
    public void testApi_DeleteNetworkElement_NetworkElementDeleted() throws Exception {
        // Arrange.
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);
        ingress.setParentId(UUID.randomUUID().toString());
        this.txControl.required(() -> {
            this.em.persist(ingress);
            return null;
        });

        NetworkElement portGroup = this.redirApi.registerNetworkElement(Arrays.asList(ingress));

        // Act.
        this.redirApi.deleteNetworkElement(portGroup);

        // Assert.
        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        PortGroupEntity deletedPortGroupEntity = utils.findPortGroupEntity(portGroup.getElementId(), portGroup.getParentId());

        assertNull(deletedPortGroupEntity);

        PortEntity updatedVirtualPort = utils.findPortEntityByElementId(ingress.getElementId());

        assertNotNull(updatedVirtualPort);
        assertNotNull(portGroup);
        assertNotNull(portGroup.getElementId());
        assertNull(updatedVirtualPort.getPortGroup());
    }

    @Test
    public void testApi_InstallInspectionHook_Succeeds() throws Exception {

        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);

        // Act.
        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        // Assert.
        assertNotNull(hookId);

        InspectionHookElement inspectionHookElement = this.txControl.required(() -> {
            InspectionHookElement tmp = this.em.find(InspectionHookEntity.class, hookId);
            assertNotNull(tmp);
            assertNotNull(tmp.getInspectionPort());
            return tmp;
        });

        // Here we are mostly afraid of LazyInitializationException
        assertNotNull(inspectionHookElement);
        assertNotNull(inspectionHookElement.getHookId());
        assertNotNull(inspectionHookElement.getInspectionPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort().getMacAddresses());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort().getPortIPs());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort().getMacAddresses());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort().getPortIPs());
        assertNotNull(inspectionHookElement.getInspectionPort().getIngressPort());
        assertNotNull(inspectionHookElement.getInspectionPort().getEgressPort());
        assertNotNull(inspectionHookElement.getInspectedPort().getMacAddresses());
        assertNotNull(inspectionHookElement.getInspectedPort().getPortIPs());
    }

    @Test
    public void testApi_GetInspectionHookByInspectedAndPort_Succeeds() throws Exception {

        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        InspectionHookElement foundInspectionHook = this.redirApi.getInspectionHook(inspected,
                inspectionPortElement);
        assertEquals("Inspection Hook already in the database before installed!", null, foundInspectionHook);

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);

        foundInspectionHook = this.redirApi.getInspectionHook(inspected, inspectionPortElement);
        assertEquals("Inspection Hook already in the database before installed!", null, foundInspectionHook);

        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        // Act.
        foundInspectionHook = this.redirApi.getInspectionHook(inspected, inspectionPortElement);

        // Assert.
        assertNotNull("Not found Inspection Hook by ports after install!", foundInspectionHook);
        assertNotNull(foundInspectionHook.getHookId());
        assertEquals(hookId, foundInspectionHook.getHookId());

        InspectionHookElement foundInspectionHookById = this.redirApi.getInspectionHook(hookId);
        assertNotNull("Not found Inspection Hook by id after install!", foundInspectionHookById);
        assertNotNull(foundInspectionHookById.getHookId());
        assertEquals(hookId, foundInspectionHookById.getHookId());
    }

    @Test
    public void testApi_RemoveInspectionHookByPorts_InspectionHookDisappears() throws Exception {
        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookEntity inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookEntity);
        assertEquals(hookId, inspectionHookEntity.getHookId());

        // Act.
        this.redirApi.removeInspectionHook(inspectionHookEntity.getInspectedPort(),
                inspectionHookEntity.getInspectionPort());

        // Assert.
        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApi_RemoveInspectionHookById_InspectionHookDisappears() throws Exception {
        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookEntity inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookEntity);
        assertEquals(hookId, inspectionHookEntity.getHookId());

        // Act.
        this.redirApi.removeInspectionHook(inspectionHookEntity.getHookId());

        // Assert.
        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApi_RemoveAllInspectionHooks_InspectionHookDisappears() throws Exception {
        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

        assertNotNull(hookId);

        InspectionHookElement inspectionHookElement = this.txControl.required(() -> {
            InspectionHookElement tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertNotNull(inspectionHookElement);
        assertEquals(hookId, inspectionHookElement.getHookId());

        assertNotNull(inspectionHookElement.getInspectionPort());
        assertEquals(registeredElement.getElementId(), inspectionHookElement.getInspectionPort().getElementId());

        // Act.
        this.redirApi.removeAllInspectionHooks(inspected);

        // Assert.
        int nInspectionHooks = this.txControl.required(() -> {
            List<InspectionHookEntity> list = this.em
                    .createQuery("FROM InspectionHookEntity", InspectionHookEntity.class).getResultList();
            return list.size();
        });

        assertEquals(0, nInspectionHooks);
    }

    @Test
    public void testApi_RemoveAllInspectionHooks_InspectedPortDisappears() throws Exception {
        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        String inspectedId = inspected.getElementId();
        assertNotNull(inspectedId);

        this.txControl.required(() -> {
            PortEntity tmp = this.em.find(PortEntity.class, inspectedId);
            assertEquals(null, tmp);
            return null;
        });

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);
        String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L, NA);

        assertNotNull(hookId);

        PortEntity foundInspectedPort = this.txControl.required(() -> {
            return this.em.find(PortEntity.class, inspectedId);
        });

        assertNotNull(foundInspectedPort);
        assertEquals(inspectedId, foundInspectedPort.getElementId());
        assertNotNull(foundInspectedPort.getInspectionHook());
        assertEquals(hookId, foundInspectedPort.getInspectionHook().getHookId());

        // Act.
        this.redirApi.removeAllInspectionHooks(inspected);

        // Assert.
        this.txControl.required(() -> {
            PortEntity tmpNE = this.em.find(PortEntity.class, inspectedId);
            assertEquals(null, tmpNE);
            return null;
        });
    }

    @Test
    public void testApi_RemoveAllInspectionHooks_PortPairRemains() throws Exception {
        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        String inspectedId = inspected.getElementId();
        assertNotNull(inspectedId);

        this.txControl.required(() -> {
            PortEntity tmp = this.em.find(PortEntity.class, inspectedId);
            assertEquals(null, tmp);
            return null;
        });

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);
        String elementId = registeredElement.getElementId();

        InspectionPortEntity foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertNotNull(foundInspectionPort);
        assertEquals(elementId, foundInspectionPort.getElementId());

        // Act.
        this.redirApi.removeAllInspectionHooks(inspected);

        // Assert.
        foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertNotNull(foundInspectionPort);
        assertEquals(elementId, foundInspectionPort.getElementId());
    }

    @Test
    public void testApi_RemoveInspectionPort_InspectionPortDisappears() throws Exception {
        // Arrange.
        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        String inspectedId = inspected.getElementId();
        assertNotNull(inspectedId);

        // expected before installInspectionHook
        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertTrue(registeredElement instanceof InspectionPortEntity);
        String elementId = registeredElement.getElementId();

        InspectionPortEntity foundInspectionPort = this.txControl.required(() -> {
            InspectionPortEntity tmpInspectionPort = this.em.find(InspectionPortEntity.class, elementId);
            assertNotNull(tmpInspectionPort);
            return tmpInspectionPort;
        });

        assertEquals(elementId, foundInspectionPort.getElementId());

        // Act.
        // The inspectionPortElement does not have an id. Should still work.
        this.redirApi.removeInspectionPort(inspectionPortElement);

        // Assert.
        foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertEquals(null, foundInspectionPort);
    }

    @Test
    public void testApiRegisterPort() throws Exception {
        //Arrange
        port.setParentId(UUID.randomUUID().toString());
        port.setDeviceOwnerId("DeviceOwnerId");

        //Act
        NetworkElement registeredPort = this.redirApi.registerPort(port);

        //Assert
        assertNotNull(registeredPort);
        assertNotNull(registeredPort.getElementId());
        assertNotNull(registeredPort.getParentId());
        assertNotNull(registeredPort.getMacAddresses());
        assertNotNull(registeredPort.getPortIPs());
    }

    @Test
    public void testApiDeletePort() throws Exception {
        //Arrange
        port.setParentId(UUID.randomUUID().toString());
        port.setDeviceOwnerId("DeviceOwnerId");

        this.redirApi.registerPort(port);

        //Act
        this.redirApi.deletePort(port.getElementId());

        //Assert
        assertEquals(null, this.redirApi.getPort(port.getElementId()));
    }

    @Test
    public void testGetPortIds() throws Exception {
        //Arrange
        this.redirApi.registerPort(ingress);
        this.redirApi.registerPort(egress);
        this.redirApi.registerPort(port);

        //Act
        List<String> portIds = this.redirApi.getPortIds();

        //Assert
        assertNotNull(portIds);
        assertEquals(3, portIds.size());
        assertTrue(portIds.contains(ingress.getElementId()));
        assertTrue(portIds.contains(egress.getElementId()));
        assertTrue(portIds.contains(port.getElementId()));
    }

    @Test
    public void testApiGetPort() throws Exception {
        //Arrange
        port.setParentId(UUID.randomUUID().toString());
        port.setDeviceOwnerId("DeviceOwnerId");

        assertNotNull(this.redirApi.registerPort(port));

        //Act
        PortEntity registeredPort = this.redirApi.getPort(port.getElementId());

        //Assert
        assertNotNull(registeredPort);
        assertNotNull(registeredPort.getElementId());
        assertNotNull(registeredPort.getParentId());
        assertNotNull(registeredPort.getMacAddresses());
        assertNotNull(registeredPort.getPortIPs());
    }

    @Test
    public void testApiUpdatePort() throws Exception {
        //Arrange
        final ArrayList<String> macAdresses = new ArrayList<String>(
                Arrays.asList("ff:ff:ff:bb:aa:02", "ff:ff:ff:bb:aa:03"));

        final ArrayList<String> ipAdresses = new ArrayList<String>(Arrays.asList("10.5.2.3", "10.5.3.4"));

        final String newParentId = "NewParentId";

        final String newDeviceOwnerId = "NewDeviceOwnerId";

        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        this.redirApi.registerPort(port);

        PortEntity portEntity = this.redirApi.getPort(port.getElementId());
        portEntity.setMacAddresses(macAdresses);
        portEntity.setPortIPs(ipAdresses);
        portEntity.setParentId(newParentId);
        portEntity.setDeviceOwnerId(newDeviceOwnerId);

        //Act
        PortEntity updatedPort = this.redirApi.updatePort(portEntity);

        //Assert
        assertNotNull(updatedPort);
        assertEquals(newDeviceOwnerId, updatedPort.getDeviceOwnerId());
        assertEquals(newParentId, updatedPort.getParentId());
        assertEquals(macAdresses, updatedPort.getMacAddresses());
        assertEquals(ipAdresses, updatedPort.getPortIPs());
    }

    @Test
    public void testApiGetInspectionPortsIds() throws Exception {
        //Arrange
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

        //Act
        final String inspectionPortElementId = inspectionPortElement.getElementId();

        //Assert
        assertEquals(1, this.redirApi.getInspectionPortsIds().size());
        assertTrue(this.redirApi.getInspectionPortsIds().contains(inspectionPortElementId));
    }

    @Test
    public void testApiGetInspectionHooksIds() throws Exception {
        //Arrange
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        Element registeredElement = this.redirApi.registerInspectionPort(inspectionPortElement);

        assertNotNull(registeredElement);
        assertNotNull(registeredElement.getElementId());

        //Act
        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L, NA);

        //Assert
        assertNotNull(hookId);
        assertTrue(this.redirApi.getInspectionHooksIds().contains(hookId));
        assertEquals(1, this.redirApi.getInspectionHooksIds().size());
    }

    @Test
    public void testApiUpdateInspectionPort() throws Exception {
        //Arrange
        final ArrayList<String> newIngressMacAdresses = new ArrayList<String>(
                Arrays.asList("ff:ff:ff:bb:cc:01", "ff:ff:ff:bb:aa:01"));

        final ArrayList<String> newIngressIPAdresses = new ArrayList<String>(Arrays.asList("10.5.2.3", "10.5.3.4"));

        final ArrayList<String> newEgressMacAdresses = new ArrayList<String>(
                Arrays.asList("ff:ff:ff:bb:aa:02", "ff:ff:ff:bb:aa:03"));

        final ArrayList<String> newEggressIPAdresses = new ArrayList<String>(Arrays.asList("10.5.2.5", "10.5.3.6"));

        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        assertNotNull(this.redirApi.registerPort(ingress));
        assertNotNull(this.redirApi.registerPort(egress));

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);
        InspectionPortEntity registeredPortEntity = (InspectionPortEntity) this.redirApi
                .registerInspectionPort(inspectionPortElement);

        ingress.setMacAddresses(newIngressMacAdresses);
        ingress.setPortIPs(newIngressIPAdresses);

        egress.setMacAddresses(newEgressMacAdresses);
        egress.setPortIPs(newEggressIPAdresses);

        registeredPortEntity.setEgressPort(egress);
        registeredPortEntity.setIngressPort(ingress);

        assertNotNull(registeredPortEntity.getEgressPort().getElementId());
        assertNotNull(registeredPortEntity.getIngressPort().getElementId());

        //Act
        InspectionPortEntity updatedPortEntity = (InspectionPortEntity) this.redirApi
                .updateInspectionPort(registeredPortEntity);

        //Assert
        assertNotNull(updatedPortEntity);
        assertEquals(egress.getElementId(), updatedPortEntity.getEgressPort().getElementId());
        assertTrue(egress.getMacAddresses().containsAll(updatedPortEntity.getEgressPort().getMacAddresses()));
        assertEquals(ingress.getElementId(), updatedPortEntity.getIngressPort().getElementId());
        assertTrue(ingress.getMacAddresses().containsAll(updatedPortEntity.getIngressPort().getMacAddresses()));
    }

    @Test
    public void testApiGetPortForUnregisteredPort() throws Exception {
        //Arrange
        port.setParentId(UUID.randomUUID().toString());
        port.setDeviceOwnerId("DeviceOwnerId");

        //Act
        PortEntity registeredPort = this.redirApi.getPort(port.getElementId());

        //Assert
        assertNull(registeredPort);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApiUpdateUnregisteredPort() throws Exception {
        //Arrange
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        port.setParentId("peNewParentId");
        port.setDeviceOwnerId("peNewDeviceOwnerId");

        //Act
        this.redirApi.updatePort(port);
    }
}
