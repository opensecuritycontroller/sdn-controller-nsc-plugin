package org.osc.controller.nsc.utils;

import static java.util.stream.Collectors.toList;
import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.MacAddressEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;
import org.osc.controller.nsc.entities.PortIpEntity;
import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.service.transaction.control.TransactionControl;

public class NSCUtils {

    private static final Logger LOG = Logger.getLogger(NSCUtils.class);

    private TransactionControl txControl;
    private EntityManager em;

    public NSCUtils(EntityManager em, TransactionControl txControl) {
        this.em = em;
        this.txControl = txControl;
    }

    public static MacAddressEntity makeMacAddressEntity(String macAddressStr) {
        return new MacAddressEntity(null, macAddressStr, null);
    }

    public PortIpEntity makePortIpEntity(String portIpStr) {
        return new PortIpEntity(null, portIpStr, null);
    }

    public NetworkElementEntity makeNetworkElementEntity(NetworkElement networkElement) {

        if (networkElement == null) {
            return null;
        }

        NetworkElementEntity retVal = new NetworkElementEntity();

        List<String> macAddrStrings = networkElement.getMacAddresses();
        List<MacAddressEntity> macAddrEntities = new ArrayList<>();
        if (macAddrStrings != null) {
            macAddrEntities = macAddrStrings.stream().map(s -> makeMacAddressEntity(s)).collect(toList());
            macAddrEntities.stream().forEach(p -> {
                p.setElement(retVal);
            });
        }

        List<String> portIpStrings = networkElement.getPortIPs();
        List<PortIpEntity> portIpEntities = new ArrayList<>();
        if (portIpStrings != null) {
            portIpEntities = portIpStrings.stream().map(s -> makePortIpEntity(s)).collect(toList());
            portIpEntities.stream().forEach(p -> {
                p.setElement(retVal);
            });
        }

        retVal.setElementId(networkElement.getElementId());
        retVal.setMacAddressEntities(macAddrEntities);
        retVal.setPortIpEntities(portIpEntities);

        return retVal;
    }

    public InspectionPortEntity makeInspectionPortEntity(InspectionPortElement inspectionPortElement) {

        NetworkElementEntity ingressEntity = null;
        NetworkElementEntity egressEntity = null;

        NetworkElement ingress = inspectionPortElement.getIngressPort();
        if (ingress != null) {
            ingressEntity = makeNetworkElementEntity(ingress);
        }

        NetworkElement egress = inspectionPortElement.getEgressPort();

        if (egress != null) {
            if (ingressEntity != null && ingressEntity.getElementId().equals(egress.getElementId())) {
                egressEntity = ingressEntity;
            } else {
                egressEntity = makeNetworkElementEntity(egress);
            }
        }

        return new InspectionPortEntity(null, ingressEntity, egressEntity, null);
    }


    public InspectionHookEntity makeInspectionHookEntity(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort, Long tag, TagEncapsulationType encType, Long order,
            FailurePolicyType failurePolicyType) {
        InspectionPortEntity inspectionPortEntity = makeInspectionPortEntity(inspectionPort);
        final String elementId = inspectedPort.getElementId();

        encType = (encType != null ? encType : VLAN);
        failurePolicyType = (failurePolicyType != null ? failurePolicyType : NA);

        NetworkElementEntity inspected = makeNetworkElementEntity(inspectedPort);
        InspectionHookEntity retVal = new InspectionHookEntity();

        retVal.setInspectedPort(inspected);
        retVal.setInspectionPort(inspectionPortEntity);
        retVal.setHookOrder(order);
        retVal.setTag(tag);
        retVal.setEncType(encType);
        retVal.setFailurePolicyType(failurePolicyType);

        inspectionPortEntity.setInspectionHook(retVal);
        inspected.setInspectionHook(retVal);

        return retVal;
    }

    public NetworkElement makeNetworkElement(NetworkElementEntity netwkEntity) {

        loadFullEntity(netwkEntity);
        return netwkEntity;
    }

    public NetworkElement makeNetworkElement(List<MacAddressEntity> macAddrEntities, List<PortIpEntity> portIpEntities,
            Long parentId, Long elementId) {

        List<String> macAddresses = macAddrEntities != null ?
                macAddrEntities.stream().map(s -> s.getMacAddress()).collect(Collectors.toList()) : null;

        List<String> portIPs = portIpEntities != null ?
                portIpEntities.stream().map(s -> s.getPortIp()).collect(Collectors.toList()) : null;
        String parentIdStr = parentId != null ? parentId.toString() : null;
        String elementIdStr = elementId != null ? elementId.toString() : null;

        return new NetworkElementImpl(elementIdStr, parentIdStr, macAddresses, portIPs);
    }

    public InspectionPortElement makeInspectionPortElement(InspectionPortEntity inspectionPortEntity) {

        loadFullEntity(inspectionPortEntity);
        return inspectionPortEntity;
    }

    public InspectionHookElement makeInspectionHookElement(final InspectionHookEntity inspectionHookEntity) {
        loadFullEntity(inspectionHookEntity);
        return inspectionHookEntity;
    };

    public NetworkElementEntity networkElementEntityByElementId(String elementId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<NetworkElementEntity> q = cb.createQuery(NetworkElementEntity.class);
        Root<NetworkElementEntity> r = q.from(NetworkElementEntity.class);
        q.where(cb.equal(r.get("elementId"), elementId));

        try {
            return this.em.createQuery(q).getSingleResult();
        } catch (Exception e) {
            LOG.error(String.format("Finding Network Element %s ", elementId), e); // TODO
            return null;
        }
    }

    public InspectionPortEntity findInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        return this.txControl.required(() -> txInspPortByNetworkElements(ingress, egress));
    }

    private InspectionPortEntity txInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
        String ingressId = ingress != null ? ingress.getElementId() : null;
        String egressId = ingress != null ? egress.getElementId() : null;

        Query q = this.em.createQuery("FROM InspectionPortEntity WHERE ingressId = :ingId AND egressId = :egId ");
        q.setParameter("ingId", ingressId);
        q.setParameter("egId", egressId);

        try {
            @SuppressWarnings("unchecked")
            List<InspectionPortEntity> ports = q.getResultList();
            if (ports == null || ports.size() == 0) {
                LOG.warn(String.format("No Inspection ports by ingress %s and egress %s", ingressId, egressId));
                return null;
            } else if (ports.size() > 1) {
                LOG.warn(String.format("Multiple results! Inspection ports by ingress %s and egress %s", ingressId,
                        egressId));
            }
            return ports.get(0);

        } catch (Exception e) {
            LOG.error(String.format("Finding Inspection ports by ingress %s and egress %s", ingress.getElementId(),
                    egress.getElementId()), e); // TODO
            return null;
        }
    }

    public InspectionHookEntity findInspHookByInspectedAndPort(NetworkElement inspected,
            InspectionPortElement element) {
        return this.txControl.required(() -> {
            InspectionHookEntity e = txInspHookByInspectedAndPort(inspected, element);
            loadFullEntity(e);
            return e;
        });
    }

    public InspectionPortEntity txInspectionPortEntityById(String id) {
        return this.em.find(InspectionPortEntity.class, id);
    }

    public NetworkElementEntity txNetworkElementEntityById(Long id) {
        return this.em.find(NetworkElementEntity.class, id);
    }

    public void removeSingleInspectionHook(InspectionHookEntity inspectionHookEntity) {

        this.txControl.required(() -> {
            NetworkElementEntity networkElementEntity = inspectionHookEntity.getInspectedPort();
            InspectionPortEntity inspectionPortEntity = inspectionHookEntity.getInspectionPort();

            inspectionHookEntity.setInspectionPort(null);
            inspectionPortEntity.setInspectionHook(null);
            inspectionHookEntity.setInspectedPort(null);
            networkElementEntity.setInspectionHook(null);
            this.em.remove(inspectionHookEntity);
            return null;
        });
    }

    private InspectionHookEntity txInspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        // Paranoid
        NetworkElement ingress = element != null ? element.getIngressPort() : null;
        NetworkElement egress = element != null ? element.getEgressPort() : null;

        String inspectedId = inspected != null ? inspected.getElementId() : null;

        InspectionPortEntity inspPort = findInspPortByNetworkElements(ingress, egress);

        String portId = inspPort != null ? inspPort.getElementId() : null;

        Query q = this.em.createQuery(
                "FROM InspectionHookEntity WHERE inspectedPortId = :inspectedId AND inspectionPortId = :inspectionId ");
        q.setParameter("inspectedId", inspectedId);
        q.setParameter("inspectionId", portId);

        try {
            List<InspectionHookEntity> inspectionHooks = q.getResultList();
            if (inspectionHooks == null || inspectionHooks.size() == 0) {
                LOG.warn(String.format("No Inspection hooks by inspected %s and port %d", inspectedId, portId));
                return null;
            } else if (inspectionHooks.size() > 1) {
                LOG.warn(String.format("Multiple results! Inspection hooks by inspected %s and port %d", inspectedId,
                        portId));
            }
            return inspectionHooks.get(0);

        } catch (Exception e) {
            LOG.error(String.format("Finding Inspection hooks by inspected %s and port %d", inspectedId, portId), e); // TODO
            return null;
        }

    }

    private void loadFullEntity(InspectionHookEntity inspectionHookEntity) {
        if (inspectionHookEntity == null) {
            return;
        }
        inspectionHookEntity.getElementId();
        inspectionHookEntity.getEncType();
        inspectionHookEntity.getFailurePolicyType();
        inspectionHookEntity.getHookOrder();

        NetworkElementEntity inspected = inspectionHookEntity.getInspectedPort();
        loadFullEntity(inspected);
        InspectionPortEntity inspectionPort = inspectionHookEntity.getInspectionPort();
        loadFullEntity(inspectionPort);
    }

    private void loadFullEntity(InspectionPortEntity inspectionPortEntity) {
        if (inspectionPortEntity == null) {
            return;
        }
        NetworkElementEntity ingress = inspectionPortEntity.getIngressPort();
        NetworkElementEntity egress = inspectionPortEntity.getEgressPort();

        loadFullEntity(ingress);
        loadFullEntity(egress);
        inspectionPortEntity.getParentId();
    }

    private void loadFullEntity(NetworkElementEntity networkElementEntity) {
        if (networkElementEntity == null) {
            return;
        }
        networkElementEntity.getElementId();
        networkElementEntity.getParentId();
        networkElementEntity.getPortIPs();
        networkElementEntity.getMacAddresses();
        networkElementEntity.getParentId();
    }
}
