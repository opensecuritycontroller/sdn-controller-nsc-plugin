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
import static org.osc.controller.nsc.SampleSdnRedirectionApiTestData.*;
import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.entities.PortGroupEntity;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osc.sdk.controller.element.Element;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SampleSdnRedirectionApiTest {

    public EntityManager em;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    TestTransactionControl txControl;

    @InjectMocks
    private SampleSdnRedirectionApi redirApi;

    @Before
    public void setup() {
        this.em = InMemDB.getEntityManager();
        this.txControl.init(this.em);
        setupDataObjects();
    }

    @After
    public void tearDown() throws Exception {
        InMemDB.close();
    }

    @Test
    public void verifyCorrectNumberOfMacsAdPortIps() throws Exception {

        assertEquals(null, inspectionPort.getElementId());

        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        assertNotNull(inspectionPort.getElementId());

        InspectionPortEntity tmp = this.txControl.requiresNew(() -> {
            return this.em.find(InspectionPortEntity.class, inspectionPort.getElementId());
        });

        assertEquals(2, tmp.getEgressPort().getMacAddresses().size());
        assertEquals(2, tmp.getEgressPort().getPortIPs().size());
        assertEquals(2, tmp.getIngressPort().getMacAddresses().size());
        assertEquals(2, tmp.getIngressPort().getPortIPs().size());
    }

    @Test
    public void verifyHookAndPortPersistedAfterSingleHookPersistenceWithObjectGraphSetUp() {

        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        InspectionHookEntity persistedHook = this.txControl.required(() -> {
            InspectionHookEntity ph = this.em.find(InspectionHookEntity.class, inspectionHook.getHookId());
            InspectionPortEntity iprt = this.em.find(InspectionPortEntity.class, inspectionPort.getElementId());

            assertEquals(inspectionPort.getElementId(), iprt.getElementId());
            return ph;
        });

        assertNotNull(inspectionHook.getHookId());
        assertEquals(inspectionHook.getHookId(), persistedHook.getHookId());

        InspectionPortEntity persistedPort = persistedHook.getInspectionPort();

        assertNotNull(persistedPort);
        assertEquals(inspectionHook.getInspectionPort().getElementId(), persistedPort.getElementId());
    }

    @Test
    public void testUtilsInspPortByNetworkElements() throws Exception {

        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        InspectionPortEntity foundPort = utils.findInspPortByNetworkElements(ingress, egress);

        assertNotNull(foundPort);
        assertEquals(inspectionPort.getElementId(), foundPort.getElementId());

    }

    @Test
    public void testUtilsNetworkElementEntityByElementId() throws Exception {

        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        NetworkElementEntity foundNE = this.txControl.required(() -> {
            NetworkElementEntity e = utils.txNetworkElementEntityByElementId(inspected.getElementId());
            return e;
        });

        assertNotNull(foundNE);
        assertNotNull(foundNE.getMacAddresses());
        assertEquals(1, foundNE.getMacAddresses().size());

    }

    @Test
    public void testUtilsInspHookByInspectedAndPort() throws Exception {
        this.txControl.required(() -> {
            this.em.persist(inspectionHook);
            return inspectionHook;
        });

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        InspectionHookEntity foundIH = this.txControl.required(() -> {
            InspectionPortEntity ipe = this.em.find(inspectionPort.getClass(), inspectionPort.getElementId());
            assertNotNull(ipe);

            NetworkElement ne = inspected;
            InspectionPortElement prte = ipe;
            InspectionHookEntity ihe = utils.findInspHookByInspectedAndPort(ne, prte);

            assertNotNull(ihe);
            assertEquals(inspectionHook.getHookId(), ihe.getHookId());
            //            assertNotNull(ihe.getInspectionPort());
            //            assertEquals(ihe.getInspectionPort().getElementId(), ipe.getElementId());
            return ihe;
        });

        assertEquals(foundIH.getHookId(), inspectionHook.getHookId());
        assertNotNull(foundIH.getInspectionPort());
        assertEquals(foundIH.getInspectionPort().getElementId(), inspectionPort.getElementId());

    }

    @Test
    public void testUtilsRemoveSingleInspectionHook() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

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

        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);

        utils.removeSingleInspectionHook(hookId);

        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApiRegisterInspectionPort() throws Exception {
        RedirectionApiUtils utils = new RedirectionApiUtils(this.em, this.txControl);
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);

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
        inspectionPortElement.getIngressPort().getParentId();
        inspectionPortElement.getEgressPort().getParentId();

        final InspectionPortElement inspectionPortElementTmp = inspectionPortElement;
        NetworkElementEntity foundIngress = this.txControl.required(() -> {
            NetworkElementEntity elementEntity = utils
                    .txNetworkElementEntityByElementId(inspectionPortElementTmp.getIngressPort().getElementId());
            return elementEntity;
        });

        assertNotNull(foundIngress);
        assertEquals(inspectionPortElement.getIngressPort().getElementId(), foundIngress.getElementId());

        // Here we are afraid of lazyInitializationException
        foundIngress.getMacAddresses();
        foundIngress.getPortIPs();
        foundIngress.getElementId();
        foundIngress.getParentId();

        InspectionPortElement foundInspPortElement = this.redirApi.getInspectionPort(inspectionPortElement);
        assertEquals(inspectionPortElement.getIngressPort().getElementId(),
                foundInspPortElement.getIngressPort().getElementId());
        assertEquals(inspectionPortElement.getEgressPort().getElementId(),
                foundInspPortElement.getEgressPort().getElementId());
        assertEquals(inspectionPortElement.getElementId(), foundInspPortElement.getElementId());

        assertEquals(null, inspectionPortElement.getParentId());
        assertEquals(null, foundInspPortElement.getParentId());
    }

    @Test
    public void testApiRegisterInspectionPortWithNetworkElementsAlreadyPersisted() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        this.txControl.required(() -> {
            this.em.persist(ingress);
            return null;
        });

        InspectionPortElement inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // ... and the test
        inspectionPortElement = (InspectionPortElement) this.redirApi.registerInspectionPort(inspectionPortElement);
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

        NetworkElementEntity updatedVirtualPort = utils.findNetworkElementEntityByElementId(ingress.getElementId());

        assertNotNull(createdPortGroupEntity);
        assertNotNull(createdPortGroupEntity.getElementId());
        assertNotNull(createdPortGroupEntity.getParentId());
        assertNotNull(updatedVirtualPort.getPortGroup());
        assertEquals(portGroup.getElementId(), updatedVirtualPort.getPortGroup().getElementId());
    }

    // Test applicable only for SUPPORTS_PORT_GROUP_VALUE == true, the default current value is false
    //@Test
    public void testDeleteNetworkElement_NetworkElementDeleted() throws Exception {
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

        NetworkElementEntity updatedVirtualPort = utils.findNetworkElementEntityByElementId(ingress.getElementId());

        assertNotNull(updatedVirtualPort);
        assertNotNull(portGroup);
        assertNotNull(portGroup.getElementId());
        assertNull(updatedVirtualPort.getPortGroup());
    }

    @Test
    public void testApiInstallInspectionHook() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);
        final String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L,
                NA);

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
    public void testApiGetInspectionHook() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

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

        assertNotNull(hookId);
        foundInspectionHook = this.redirApi.getInspectionHook(inspected, inspectionPortElement);
        assertNotNull("Not found Inspection Hook by ports after install!", foundInspectionHook);
        foundInspectionHook = this.redirApi.getInspectionHook(hookId);
        assertNotNull("Not found Inspection Hook by id after install!", foundInspectionHook);
    }

    @Test
    public void testApiRemoveInspectionHookByPorts_InspectionHookDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

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

        this.redirApi.removeInspectionHook(inspectionHookEntity.getInspectedPort(),
                inspectionHookEntity.getInspectionPort());

        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApiRemoveInspectionHookById_InspectionHookDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

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

        this.redirApi.removeInspectionHook(inspectionHookEntity.getHookId());

        inspectionHookEntity = this.txControl.required(() -> {
            InspectionHookEntity tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookEntity);
    }

    @Test
    public void testApiRemoveAllInspectionHooks_InspectionHookDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

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

        this.redirApi.removeAllInspectionHooks(inspected);

        inspectionHookElement = this.txControl.required(() -> {
            InspectionHookElement tmpInspectionHook = this.em.find(InspectionHookEntity.class, hookId);
            return tmpInspectionHook;
        });

        assertEquals(null, inspectionHookElement);

        int nInspectionHooks = this.txControl.required(() -> {
            List<InspectionHookEntity> list = this.em
                    .createQuery("FROM InspectionHookEntity", InspectionHookEntity.class).getResultList();
            return list.size();
        });

        assertEquals(0, nInspectionHooks);
    }

    @Test
    public void testApiRemoveAllInspectionHooks_InspectedPortDisappears() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        String inspectedId = inspected.getElementId();
        assertNotNull(inspectedId);

        this.txControl.required(() -> {
            NetworkElementEntity tmp = this.em.find(NetworkElementEntity.class, inspectedId);
            assertEquals(null, tmp);
            return null;
        });

        // expected before installInspectionHook
        this.redirApi.registerInspectionPort(inspectionPortElement);
        String hookId = this.redirApi.installInspectionHook(inspected, inspectionPortElement, 0L, VLAN, 0L, NA);

        assertNotNull(hookId);

        NetworkElementEntity foundInspectedPort = this.txControl.required(() -> {
            return this.em.find(NetworkElementEntity.class, inspectedId);
        });

        assertNotNull(foundInspectedPort);
        assertEquals(inspectedId, foundInspectedPort.getElementId());
        assertNotNull(foundInspectedPort.getInspectionHook());
        assertEquals(hookId, foundInspectedPort.getInspectionHook().getHookId());

        this.redirApi.removeAllInspectionHooks(inspected);

        this.txControl.required(() -> {
            NetworkElementEntity tmpNE = this.em.find(NetworkElementEntity.class, inspectedId);
            assertEquals(null, tmpNE);
            return null;
        });
    }

    @Test
    public void testApiRemoveAllInspectionHooks_PortPairRemains() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

        InspectionPortEntity inspectionPortElement = new InspectionPortEntity(null, ingress, egress);

        String inspectedId = inspected.getElementId();
        assertNotNull(inspectedId);

        this.txControl.required(() -> {
            NetworkElementEntity tmp = this.em.find(NetworkElementEntity.class, inspectedId);
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

        this.redirApi.removeAllInspectionHooks(inspected);

        foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertNotNull(foundInspectionPort);
        assertEquals(elementId, foundInspectionPort.getElementId());
    }

    @Test
    public void testApiRemoveInspectionPort() throws Exception {
        this.redirApi = new SampleSdnRedirectionApi(this.txControl, this.em);

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

        // The inspectionPortElement does not have an id. Should still work.
        this.redirApi.removeInspectionPort(inspectionPortElement);

        foundInspectionPort = this.txControl.required(() -> {
            return this.em.find(InspectionPortEntity.class, elementId);
        });

        assertEquals(null, foundInspectionPort);
    }
}
