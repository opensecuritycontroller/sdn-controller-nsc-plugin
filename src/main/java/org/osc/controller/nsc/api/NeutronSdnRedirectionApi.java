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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.hibernate.jpa.criteria.CriteriaBuilderImpl;
import org.osc.controller.nsc.entities.InspectionHookNSCEntity;
import org.osc.controller.nsc.entities.InspectionPortNSCEntity;
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

    public NeutronSdnRedirectionApi(VirtualizationConnectorElement vc, 
    								String region, 
    								TransactionControl txControl, 
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
        	InspectionHookNSCEntity entity = utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);        	         
        	return NSCUtils.makeInspectionHookElement(entity);
        } catch (Exception e) {
        	String inspectedPortId = inspectedPort != null ? inspectedPort.getElementId() : null;
        	String inspectionPortId = inspectionPort != null ?  inspectionPort.getElementId() : null;
            LOGGER.error(String.format("Finding Network Element (inspected %s ; inspectnPort %s) :", 
            		                   inspectedPortId, inspectionPortId), e); // TODO
            return null;
        }
    }

    @Override
    public String installInspectionHook(List<NetworkElement> inspectedPort, InspectionPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
                    throws NetworkPortNotFoundException, Exception {
    	
    	NetworkElement inspected = null;
    	if (inspectedPort != null && inspectedPort.size() > 0) {
    		inspected = inspectedPort.iterator().next();
    	}
    	
    	InspectionHookNSCEntity inspHookEntity = utils.makeInspectionHookEntity(inspected, inspectionPort, tag, encType, order, failurePolicyType);    	
    	txControl.required(() -> { em.persist(inspHookEntity); return null;});
    	
    	return null;
    }

    @Override
    public void removeInspectionHook(List<NetworkElement> inspectedPort, InspectionPortElement inspectionPort)
            throws Exception {
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            neutronApi.deleteInspectionHookByPorts(this.region, inspectedPort.iterator().next().getElementId(), inspectionPort);
//        } catch (NetworkPortNotFoundException nfe) {
//            this.log.info(String.format("Port with Id: '%s' not found", nfe.getPortId()));
//        }
    	
    	if (inspectedPort != null && inspectedPort.size() > 0) {

    		NetworkElement inspected = inspectedPort.get(0);
    		
        	txControl.requiresNew(() -> {
        		InspectionHookNSCEntity entity = utils.inspHookByInspectedAndPort(inspected, inspectionPort);
        		if (entity != null) {
        			em.remove(entity);    			
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
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(),
//                    inspectionPort);
//            inspectionHook.setTag(tag);
//            neutronApi.updateInspectionHook(this.region, inspectionHook);
//        }
    
	    txControl.required(() -> {
	    	InspectionHookNSCEntity entity = utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
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
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(),
//                    inspectionPort);
//            inspectionHook.setFailurePolicyType(failurePolicyType.toString());
//            neutronApi.updateInspectionHook(this.region, inspectionHook);
//        }

    	txControl.required(() -> {
        	InspectionHookNSCEntity entity = utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
        	String typeStr = failurePolicyType != null ? failurePolicyType.name() : null;
        	entity.setFailurePolicyType(typeStr);
        	return null;
        });
    }

    @Override
    public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            List<InspectionHook> inspectionHooksByPort = neutronApi.getInspectionHooksByPort(this.region,
//                    inspectedPort.getElementId());
//            if (inspectionHooksByPort != null) {
//                for (InspectionHook hook : inspectionHooksByPort) {
//                    neutronApi.deleteInspectionHook(this.region, hook.getInspectedPortId());
//                }
//            }
//        }
    	
    	txControl.required(() -> {
    		Query q =  em.createQuery("DELETE FROM InspectionHook WHERE inspectedPortId = :portId");    		
    		String portId = inspectedPort != null ? inspectedPort.getElementId() : null;
    		q.setParameter("portId", portId);
    		
    		q.executeUpdate();
    		q =  em.createQuery("FROM InspectionHook WHERE inspectedPortId = :portId");
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
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            return neutronApi.getInspectionPort(this.region, inspectionPort);
//        }
    	
    	String portIdStr = inspectionPort != null ? inspectionPort.getElementId() : null;
    	
    	return txControl.required(() -> {
    		
    		Long portId = null;
    		try {
    			portId = Long.parseLong(portIdStr);
    		} catch (NumberFormatException nfe) {
    			LOGGER.error("Inspection port id of non-numeric format passed to NSC controller " + portIdStr);
    			return null;
    		}
    		
    		InspectionPortNSCEntity e = utils.txInspectionPortEntityById(portId);
        	return NSCUtils.makeInspectionPortElement(e);    		
		}); 
    	

    }

    @Override
    public Element registerInspectionPort(InspectionPortElement inspectionPort) throws Exception {
    	
    	InspectionPortNSCEntity entity = NSCUtils.makeInspectionPortEntity(inspectionPort);
    	
    	 
		return txControl.required(() -> { 
	    		em.persist(entity); 
	    		em.flush(); 
	    		
	    		InspectionPortNSCEntity e = em.find(InspectionPortNSCEntity.class, entity.getId());
	    		
	    		return NSCUtils.makeInspectionPortElement(e);
			});
    }

    @Override
    public void setInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long order)
            throws Exception {
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            InspectionHook inspectionHook = neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(),
//                    inspectionPort);
//            inspectionHook.setOrder(order);
//            neutronApi.updateInspectionHook(this.region, inspectionHook);
//        }	
    	
	    txControl.required(() -> {
	    	InspectionHookNSCEntity entity = utils.inspHookByInspectedAndPort(inspectedPort, inspectionPort);
	    	entity.setHookOrder(order);
	    	return null;
	    });
    }

    @Override
    public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort) throws Exception {
        InspectionHookElement inspectionHook = getInspectionHook(inspectedPort, inspectionPort);
        return inspectionHook == null ? null : inspectionHook.getOrder();
    }

    @Override
    public void updateInspectionHook(InspectionHookElement existingInspectionHook) throws Exception {
//        try (NeutronSecurityControllerApi neutronApi = new NeutronSecurityControllerApi(new Endpoint(this.vc))) {
//            InspectionHookNSCEntity inspectionHook = new InspectionHookNSCEntity();
            // neutronApi.getInspectionHookByPorts(this.region, inspectedPort.getElementId(),
//                    inspectionPort);
//            inspectionHook.setInspectedPortId(inspectedPort.getElementId());
//            inspectionHook.setInspectionPort(existingInspectionHook.getInspectionPort());
//            inspectionHook.setOrder(existingInspectionHook.getOrder());
//            inspectionHook.setTag(existingInspectionHook.getTag());
//            inspectionHook.setEncType(existingInspectionHook.getEncType().toString());
//            inspectionHook.setFailurePolicyType(existingInspectionHook.getFailurePolicyType().toString());
//            neutronApi.updateInspectionHook(this.region, inspectionHook);
//        }

    	if (existingInspectionHook == null) {
    		return;
    	}
    	
    	installInspectionHook(Arrays.asList(existingInspectionHook.getInspectedPort()), 
    						  existingInspectionHook.getInspectionPort(),
    						  existingInspectionHook.getTag(),
    						  existingInspectionHook.getEncType(),
    						  existingInspectionHook.getOrder(),
    						  existingInspectionHook.getFailurePolicyType());    	
    }

    @Override
    public void close() throws Exception {    	
        // Nothing to do
    }

    @Override
    public NetworkElement registerNetworkElement(List<NetworkElement> inspectedPorts) throws Exception {
        return null;
    }

    @Override
    public NetworkElement updateNetworkElement(NetworkElement portGroup, List<NetworkElement> inspectedPorts) throws Exception {
        //no-op
        return null;
    }

    @Override
    public void deleteNetworkElement(NetworkElement portGroupId) throws Exception {
        //no-op
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
        // TODO emanoel: Currently not needed but should be implemented also for NSC.
        throw new NotImplementedException("Not expected to be called for NSC. "
                + "Currently only called for SDN controllers that support port group.");
    }

    @Override
    public InspectionHookElement getInspectionHook(String inspectionHookId) throws Exception {
        // TODO emanoel: Currently not needed but should be implemented also for NSC.
        throw new NotImplementedException("Not expected to be called for NSC. "
                + "Currently only called for SDN controllers that support port group.");
    }
    
}
