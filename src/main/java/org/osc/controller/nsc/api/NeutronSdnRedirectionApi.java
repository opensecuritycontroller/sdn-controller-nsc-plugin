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
package org.osc.controller.nsc.api;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.utils.NSCUtils;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.api.SdnRedirectionApi;
import org.osc.sdk.controller.element.Element;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osc.sdk.controller.exception.NetworkPortNotFoundException;
import org.osgi.service.transaction.control.TransactionControl;

public class NeutronSdnRedirectionApi implements SdnRedirectionApi {

	private static final Logger LOGGER = Logger.getLogger(NeutronSdnRedirectionApi.class);

	private VirtualizationConnectorElement vc;
	private String region;

	private TransactionControl txControl;
	private EntityManager em;
	private NSCUtils utils;

	public NeutronSdnRedirectionApi() {
	}

	public NeutronSdnRedirectionApi(VirtualizationConnectorElement vc, String region, TransactionControl txControl,
			EntityManager em) {
		this.vc = vc;
		this.region = region;
		this.txControl = txControl;
		this.em = em;
		this.utils = new NSCUtils(em, txControl);
	}

	@Override
	public InspectionHookElement getInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
			throws Exception {
		try {
			InspectionHookEntity entity = this.utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
			return NSCUtils.makeInspectionHookElement(entity);
		} catch (Exception e) {
			String inspectedPortId = inspectedPort != null ? inspectedPort.getElementId() : null;
			String inspectionPortId = inspectionPort != null ? inspectionPort.getElementId() : null;
			LOGGER.error(String.format("Finding Network Element (inspected %s ; inspectnPort %s) :", inspectedPortId,
					inspectionPortId), e); // TODO
			return null;
		}
	}

	@Override
	public String installInspectionHook(List<NetworkElement> inspectedPort, InspectionPortElement inspectionPort,
			Long tag, TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
			throws NetworkPortNotFoundException, Exception {

		NetworkElement inspected = null;
		if (inspectedPort != null && inspectedPort.size() > 0) {
			inspected = inspectedPort.iterator().next();
		}

		InspectionHookEntity inspHookEntity = this.utils.makeInspectionHookEntity(inspected, inspectionPort, tag, encType,
				order, failurePolicyType);
		this.txControl.required(() -> {

		    String hookId = inspHookEntity.getHookId();

		    if (this.em.find(InspectionHookEntity.class, hookId) != null) {
		        this.em.merge(inspHookEntity);
		    } else {
		        this.em.persist(inspHookEntity);
		    }

			return null;
		});

		return null;
	}

	@Override
	public void removeInspectionHook(List<NetworkElement> inspectedPort, InspectionPortElement inspectionPort)
			throws Exception {

		if (inspectedPort != null && inspectedPort.size() > 0) {

			NetworkElement inspected = inspectedPort.get(0);

			this.txControl.requiresNew(() -> {
				InspectionHookEntity entity = this.utils.inspHookByInspectedAndPort(inspected, inspectionPort);
				if (entity != null) {
					this.em.remove(entity);
				}
				return null;
			});
		}

	}

	@Override
	public Long getInspectionHookTag(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
			throws NetworkPortNotFoundException, Exception {
		InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
		return inspectionHook == null ? null : inspectionHook.getTag();
	}

	@Override
	public void setInspectionHookTag(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long tag)
			throws Exception {

		this.txControl.required(() -> {
			InspectionHookEntity entity = this.utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
			entity.setTag(tag);
			return null;
		});

	}

	@Override
	public FailurePolicyType getInspectionHookFailurePolicy(NetworkElement inspectedPort,
			InspectionPortElement inspectionPort) throws Exception {
		InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
		return inspectionHook == null ? null : inspectionHook.getFailurePolicyType();
	}

	@Override
	public void setInspectionHookFailurePolicy(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
			FailurePolicyType failurePolicyType) throws Exception {

		this.txControl.required(() -> {
			InspectionHookEntity entity = this.utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
			String typeStr = failurePolicyType != null ? failurePolicyType.name() : null;
			entity.setFailurePolicyType(typeStr);
			return null;
		});
	}

	@Override
	public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {

		this.txControl.required(() -> {
			Query q = this.em.createQuery("DELETE FROM InspectionHookEntity WHERE inspectedPortId = :portId");
			String portId = inspectedPort != null ? inspectedPort.getElementId() : null;
			q.setParameter("portId", portId);

			q.executeUpdate();
			q = this.em.createQuery("FROM InspectionHookEntity WHERE inspectedPortId = :portId");
			q.setParameter("portId", portId);
			if (q.getMaxResults() > 0) {
				LOGGER.error("Deleting inspection hooks for inspectedPortId failed! inspectedPortId: " + portId);
			}
			return null;
		});

	}

	// Inspection port methods
	@Override
	public InspectionPortElement getInspectionPort(InspectionPortElement inspectionPort) throws Exception {

		if (inspectionPort == null) {
			return null;
		}

		try {
			String portIdStr = inspectionPort != null ? inspectionPort.getElementId() : null;

			return this.txControl.required(() -> {
				Long portId = null;
				try {
					portId = Long.parseLong(portIdStr);
				} catch (NumberFormatException nfe) {
					LOGGER.error("Inspection port id of non-numeric format passed to NSC controller " + portIdStr);
					return null;
				}

				InspectionPortEntity ipEntity = this.utils.txInspectionPortEntityById(portId);
				return NSCUtils.makeInspectionPortElement(ipEntity);
			});
		} catch (Exception e)  {
			LOGGER.warn( "Failed to retrieve inspectionPort by id! Trying by ingress and egress." );
		}

		NetworkElement ingress = inspectionPort.getIngressPort();
		NetworkElement egress = inspectionPort.getEgressPort();

		InspectionPortEntity ipEntity = this.utils.inspPortByNetworkElements(ingress, egress);
		return NSCUtils.makeInspectionPortElement(ipEntity);

	}

	@Override
	public Element registerInspectionPort(InspectionPortElement inspectionPort) throws Exception {



		return this.txControl.required(() -> {

		    // must be within this transaction, because if the DB retrievals inside makeInspectionPortEntry
		    // are inside the required() call themselves. That makes them a part of a separate transaction
		    InspectionPortEntity entity = this.utils.makeInspectionPortEntity(inspectionPort);
			if (this.em.contains(entity)) {
				this.em.merge(entity);
			} else {
				this.em.persist(entity);
			}

			this.em.flush();

			InspectionPortEntity e = this.em.find(InspectionPortEntity.class, entity.getId());

			return NSCUtils.makeInspectionPortElement(e);
		});
	}

	@Override
	public void setInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long order)
			throws Exception {

		this.txControl.required(() -> {
			InspectionHookEntity entity = this.utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
			entity.setHookOrder(order);
			return null;
		});
	}

	@Override
	public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
			throws Exception {
		InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
		return inspectionHook == null ? null : inspectionHook.getOrder();
	}

	@Override
	public void updateInspectionHook(InspectionHookElement existingInspectionHook) throws Exception {

		if (existingInspectionHook == null) {
			return;
		}

		installInspectionHook(Arrays.asList(existingInspectionHook.getInspectedPort()),
				existingInspectionHook.getInspectionPort(), existingInspectionHook.getTag(),
				existingInspectionHook.getEncType(), existingInspectionHook.getOrder(),
				existingInspectionHook.getFailurePolicyType());
	}

	@Override
	public void close() throws Exception {
	    this.txControl.required(() -> {
    		this.em.close();
    		return null;
	    });
	}

	@Override
	public NetworkElement registerNetworkElement(List<NetworkElement> inspectedPorts) throws Exception {
		return null;
	}

	@Override
	public NetworkElement updateNetworkElement(NetworkElement portGroup, List<NetworkElement> inspectedPorts)
			throws Exception {
		// no-op
		return null;
	}

	@Override
	public void deleteNetworkElement(NetworkElement portGroupId) throws Exception {
		// no-op
	}

	@Override
	public List<NetworkElement> getNetworkElements(NetworkElement element) throws Exception {
		return null;
	}

	@Override
	public void removeInspectionPort(InspectionPortElement inspectionPort)
			throws NetworkPortNotFoundException, Exception {
		// no-op

	}

	@Override
	public void removeInspectionHook(String inspectionHookId) throws Exception {
		// TODO emanoel: Currently not needed but should be implemented also for
		// NSC.
		throw new NotImplementedException("Not expected to be called for NSC. "
				+ "Currently only called for SDN controllers that support port group.");
	}

	@Override
	public InspectionHookElement getInspectionHook(String inspectionHookId) throws Exception {
		// TODO emanoel: Currently not needed but should be implemented also for
		// NSC.
		throw new NotImplementedException("Not expected to be called for NSC. "
				+ "Currently only called for SDN controllers that support port group.");
	}

}
