package org.osc.controller.nsc.utils;

import static java.util.stream.Collectors.toList;
import static org.osc.sdk.controller.FailurePolicyType.NA;
import static org.osc.sdk.controller.TagEncapsulationType.VLAN;

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

	private static final Logger LOGGER = Logger.getLogger(NSCUtils.class);

	private TransactionControl txControl;
	private EntityManager em;

	public NSCUtils(EntityManager em, TransactionControl txControl) {
		this.em = em;
		this.txControl = txControl;
	}

	public static MacAddressEntity makeMacAddressEntity(String macAddressStr) {
		MacAddressEntity retVal = new MacAddressEntity();
		retVal.setMacAddress(macAddressStr != null ? macAddressStr : null);
		return retVal;
	}

	public static PortIpEntity makePortIpEntity(String portIpStr) {
		PortIpEntity retVal = new PortIpEntity();
		retVal.setPortIp(portIpStr != null ? portIpStr : null);
		return retVal;
	}

	public static NetworkElementEntity makeNetworkElementEntity(NetworkElement ne) {
		NetworkElementEntity retVal = new NetworkElementEntity();

		if (ne == null) {
			return retVal;
		}

		retVal.setElementId(ne.getElementId());

		List<String> macAddrStrings = ne.getMacAddresses();
		List<MacAddressEntity> macAddrEntities = null;
		if (macAddrStrings != null) {
			macAddrEntities = macAddrStrings.stream().map(s -> makeMacAddressEntity(s)).collect(toList());
			macAddrEntities.stream().forEach(p -> {
				p.setElement(retVal);
			});
		}

		List<String> portIpStrings = ne.getPortIPs();
		List<PortIpEntity> portIpEntities = null;
		if (portIpStrings != null) {
			portIpEntities = portIpStrings.stream().map(s -> makePortIpEntity(s)).collect(toList());
			portIpEntities.stream().forEach(p -> {
				p.setElement(retVal);
			});
		}

		retVal.setElementId(ne.getElementId());
		retVal.setMacAddressEntities(macAddrEntities);
		retVal.setPortIpEntities(portIpEntities);

		return retVal;
	}

	public static InspectionPortEntity makeInspectionPortEntity(InspectionPortElement ipe) {
		InspectionPortEntity retVal = new InspectionPortEntity();

		NetworkElement ingress = ipe.getIngressPort();
		if (ingress != null) {
			NetworkElementEntity ingressEntity = makeNetworkElementEntity(ingress);
			ingressEntity.setIngressInspectionPort(retVal);
			retVal.setIngress(ingressEntity);
		}
		NetworkElement egress = ipe.getEgressPort();
		if (egress != null) {
			NetworkElementEntity egressEntity = makeNetworkElementEntity(egress);
			egressEntity.setEgressInspectionPort(retVal);
			retVal.setEgress(egressEntity);
		}

		return retVal;
	}

	public InspectionHookEntity makeInspectionHookEntity(NetworkElement inspectedPort,
			InspectionPortElement inspectionPort, Long tag, TagEncapsulationType encType, Long order,
			FailurePolicyType failurePolicyType) {
		InspectionPortEntity inspPortEntity = makeInspectionPortEntity(inspectionPort);
		final String elementId = inspectedPort.getElementId();

		encType = (encType != null ? encType : VLAN);
        failurePolicyType = (failurePolicyType != null ? failurePolicyType : NA);

		NetworkElementEntity inspected = null;
		try {
			inspected = this.txControl.required(() -> this.em.find(NetworkElementEntity.class, elementId));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.error(e);
		}

		InspectionHookEntity retVal = new InspectionHookEntity();

		retVal.setInspectedPort(inspected);
		retVal.setInspectionPort(inspPortEntity);
		retVal.setHookOrder(order);
		retVal.setTag(tag);
		retVal.setEncType(encType.name());
		retVal.setFailurePolicyType(failurePolicyType.name());

		inspPortEntity.setInspectionHook(retVal);

		return retVal;
	}

	public static NetworkElement makeNetworkElement(final NetworkElementEntity netwkEntity) {

		if (netwkEntity == null) {
			return null;
		}

		InspectionPortEntity iPortEntity = netwkEntity.getIngressInspectionPort();
		InspectionPortEntity ePortEntity = netwkEntity.getEgressInspectionPort();
		InspectionHookEntity eHookEntity = netwkEntity.getInspectionHook();

		final String parent;
		if (iPortEntity != null) {
			parent = iPortEntity.getId() != null ? iPortEntity.getId().toString() : null;
		} else if (ePortEntity != null) {
			parent = ePortEntity.getId() != null ? ePortEntity.getId().toString() : null;
		} else if (eHookEntity != null) {
			parent = eHookEntity.getHookId() != null ? eHookEntity.getHookId() : null;
		} else {
			parent = null;
		}

		NetworkElement retVal = new NetworkElement() {
			private List<String> macAddresses = netwkEntity.getMacAddresses();
			private List<String> portIps = netwkEntity.getPortIps();
			private String parentId = parent;
			private String elementId = netwkEntity.getElementId();

			@Override
			public List<String> getPortIPs() {
				return this.portIps;
			}

			@Override
			public String getParentId() {
				return this.parentId;
			}

			@Override
			public List<String> getMacAddresses() {
				return this.macAddresses;
			}

			@Override
			public String getElementId() {
				return this.elementId;
			}
		};

		return retVal;
	}

	public static InspectionPortElement makeInspectionPortElement(InspectionPortEntity entity) {

		if (entity == null) {
			return null;
		}

		InspectionPortElement retVal = new InspectionPortElement() {
			private NetworkElement ingressPort = makeNetworkElement(entity.getIngress());
			private NetworkElement egressPort = makeNetworkElement(entity.getEgress());
			private String parentId = (entity.getInspectionHook() != null ? entity.getInspectionHook().getHookId()
					: null);
			private String elementId = (entity.getId() != null ? entity.getId().toString() : null);

			@Override
			public NetworkElement getIngressPort() {
				return this.ingressPort;
			}

			@Override
			public NetworkElement getEgressPort() {
				return this.egressPort;
			}

			@Override
			public String getElementId() {
				return this.elementId;
			}

			@Override
			public String getParentId() {
				return this.parentId;
			}
		};

		return retVal;
	}

	public static InspectionHookElement makeInspectionHookElement(final InspectionHookEntity entity) {

		if (entity == null) {
			return null;
		}

		return new InspectionHookElement() {

			private Long tag = entity.getTag();
			private Long hookOrder = entity.getHookOrder();
			private InspectionPortElement inspectionPort = makeInspectionPortElement(entity.getInspectionPort());
			private NetworkElement inspected = makeNetworkElement(entity.getInspectedPort());
			private String hookId = entity.getHookId();
			private FailurePolicyType policyType;
			private TagEncapsulationType encType;

			{
				if (entity.getFailurePolicyType() != null) {
					this.policyType = FailurePolicyType.fromText(entity.getFailurePolicyType());
				}

				if (entity.getEncType() != null) {
					this.encType = TagEncapsulationType.fromText(entity.getEncType());
				}
			}

			@Override
			public Long getTag() {
				return this.tag;
			}

			@Override
			public Long getOrder() {
				return this.hookOrder;
			}

			@Override
			public InspectionPortElement getInspectionPort() {
				return this.inspectionPort; // TODO
			}

			@Override
			public NetworkElement getInspectedPort() {
				// TODO Auto-generated method stub
				return this.inspected;
			}

			@Override
			public String getHookId() {
				return this.hookId;
			}

			@Override
			public FailurePolicyType getFailurePolicyType() {
				return this.policyType;
			}

			@Override
			public TagEncapsulationType getEncType() {
				return this.encType;
			}
		};
	};

	public NetworkElementEntity networkElementEntityByElementId(String elementId) {
		CriteriaBuilder cb = this.em.getCriteriaBuilder();

		CriteriaQuery<NetworkElementEntity> q = cb.createQuery(NetworkElementEntity.class);
		Root<NetworkElementEntity> r = q.from(NetworkElementEntity.class);
		q.where(cb.equal(r.get("elementId"), elementId));

		try {
			return this.em.createQuery(q).getSingleResult();
		} catch (Exception e) {
			LOGGER.error(String.format("Finding Network Element %s ", elementId), e); // TODO
			return null;
		}
	}

	public InspectionPortEntity inspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
		return this.txControl.required(() -> txInspPortByNetworkElements(ingress, egress));
	}

	private InspectionPortEntity txInspPortByNetworkElements(NetworkElement ingress, NetworkElement egress) {
		String ingressId = ingress != null ? ingress.getElementId() : null;
		String egressId = ingress != null ? egress.getElementId() : null;

		Query q = this.em.createQuery("FROM InspectionPortEntity WHERE ingressId = :ingId AND egressId = :egId ");
		q.setParameter("ingId", ingressId);
		q.setParameter("egId", egressId);

		// CriteriaBuilder cb = this.em.getCriteriaBuilder();
		// CriteriaQuery<InspectionPortNSCEntity> q =
		// cb.createQuery(InspectionPortNSCEntity.class);
		// Root<InspectionPortNSCEntity> r =
		// q.from(InspectionPortNSCEntity.class);
		// Predicate byIngress = cb.equal(r.get("ingress"), ingress);
		// Predicate byEgress = cb.equal(r.get("egress"), egress);
		// q.where(paramExpression)

		try {
			@SuppressWarnings("unchecked")
			List<InspectionPortEntity> ports = q.getResultList();
			if (ports == null || ports.size() == 0) {
				LOGGER.warn(String.format("No Inspection ports by ingress %s and egress %s", ingressId, egressId));
				return null;
			} else if (ports.size() > 0) {
				LOGGER.warn(String.format("Multiple results! Inspection ports by ingress %s and egress %s", ingressId,
						egressId));
			}
			return ports.get(0);

		} catch (Exception e) {
			LOGGER.error(String.format("Finding Inspection ports by ingress %s and egress %s", ingress.getElementId(),
					egress.getElementId()), e); // TODO
			return null;
		}

	}

	public InspectionHookEntity inspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
		return this.txControl.required(() -> {
			InspectionHookEntity e = txInspHookByInspectedAndPort(inspected, element);
			loadFullEntity(e);
			return e;
		});
	}

	private InspectionHookEntity txInspHookByInspectedAndPort(NetworkElement inspected, InspectionPortElement element) {
		CriteriaBuilder cb = this.em.getCriteriaBuilder();

		// Paranoid
		NetworkElement ingress = element != null ? element.getIngressPort() : null;
		NetworkElement egress = element != null ? element.getEgressPort() : null;

		String inspectedId = inspected != null ? inspected.getElementId() : null;

		InspectionPortEntity inspPort = inspPortByNetworkElements(ingress, egress);

		Long portId = inspPort != null ? inspPort.getId() : null;

		Query q = this.em.createQuery(
				"FROM InspectionHookEntity WHERE inspectedPortId = :inspectedId AND inspectionPortId = :inspectionId ");
		q.setParameter("inspectedId", inspectedId);
		q.setParameter("inspectionId", portId);

		// CriteriaQuery<InspectionHookNSCEntity> q =
		// cb.createQuery(InspectionHookNSCEntity.class);
		// Root<InspectionHookNSCEntity> r =
		// q.from(InspectionHookNSCEntity.class);
		// Predicate byPort = cb.equal(r.get("inspectionPortId"), portId);
		// Predicate byInspected = cb.equal(r.get("inspectedPortId"),
		// egress.getElementId());
		// q.where(cb.and(byPort, byInspected));

		try {
			List<InspectionHookEntity> ports = q.getResultList();
			if (ports == null || ports.size() == 0) {
				LOGGER.warn(String.format("No Inspection hooks by inspected %s and port %d", inspectedId, portId));
				return null;
			} else if (ports.size() > 0) {
				LOGGER.warn(String.format("Multiple results! Inspection hooks by inspected %s and port %d", inspectedId,
						portId));
			}
			return ports.get(0);

		} catch (Exception e) {
			LOGGER.error(String.format("Finding Inspection hooks by inspected %s and port %d", inspectedId, portId), e); // TODO
			return null;
		}

	}

	public InspectionPortEntity txInspectionPortEntityById(Long id) {
		return this.em.find(InspectionPortEntity.class, id);
	}

	public NetworkElementEntity txNetworkElementEntityById(Long id) {
		return this.em.find(NetworkElementEntity.class, id);
	}

	public static NetworkElement makeNetworkElement(List<MacAddressEntity> mas, List<PortIpEntity> ips, Long pid,
			Long eid) {

		final List<String> macAddresses = mas != null
				? mas.stream().map(s -> s.getMacAddress()).collect(Collectors.toList()) : null;
		final List<String> portIps = ips != null ? ips.stream().map(s -> s.getPortIp()).collect(Collectors.toList())
				: null;
		final String parentId = pid != null ? pid.toString() : null;
		final String elementId = eid != null ? eid.toString() : null;

		NetworkElement retVal = new NetworkElement() {
			@Override
			public List<String> getPortIPs() {
				return portIps;
			}

			@Override
			public String getParentId() {
				return parentId;
			}

			@Override
			public List<String> getMacAddresses() {
				return macAddresses;
			}

			@Override
			public String getElementId() {
				return elementId;
			}
		};

		return retVal;
	}

	private static void loadFullEntity(InspectionHookEntity e) {
		makeInspectionHookElement(e);
	}

}
