/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.detnetservice.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.DetnetEncapsulationType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.detnet.path.Path;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.flow.identification.flow.type.IPBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.flow.identification.flow.type.MPLSBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetFlows;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetTransportTunnels;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetTransportTunnelsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.transport.tunnel.tunnel.type.IPv4Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.transport.tunnel.tunnel.type.IPv6Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.transport.tunnel.tunnel.type.ipv4.Ipv4EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.transport.tunnel.tunnel.type.ipv6.Ipv6EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.transport.tunnel.tunnel.type.mpls.MplsEncapsulationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.mpls.tunnel.MplsTunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.mpls.tunnel.mpls.tunnel.ExplicitPathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.vpn.VpnBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.resource.rev181204.ResourceType;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;

public class ServiceInstance {
    private SegmentPathKey segmentPathKey;
    private Long instanceId;
    private LinkedList<DetnetLink> path;
    private DetnetEncapsulationType encapsulationType = DetnetEncapsulationType.Mpls;
    private Long detnetFlowId = null;
    private Long transportTunnelId = null;
    private SegmentDirectionType directionType;

    public ServiceInstance(SegmentPathKey segmentPathKey,Long instanceId,Path servicePath) {
        this.segmentPathKey = segmentPathKey;
        this.instanceId = instanceId;
        this.path = servicePath == null ? null : transPath(servicePath.getPathLink());
    }

    private LinkedList<DetnetLink> transPath(List<PathLink> pathLinks) {
        LinkedList<DetnetLink> pathList = new LinkedList<>();
        for (PathLink pathLink : pathLinks) {
            pathList.addLast(new DetnetLinkBuilder(pathLink).build());
        }
        return pathList;
    }

    public SegmentPathKey getSegmentPathKey() {
        return segmentPathKey;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public LinkedList<DetnetLink> getPath() {
        return path;
    }

    public DetnetEncapsulationType getEncapsulationType() {
        return encapsulationType;
    }

    public void setEncapsulationType(DetnetEncapsulationType encapsulationType) {
        this.encapsulationType = encapsulationType;
    }

    public String createServiceInstance() {
        Integer domainId = segmentPathKey.getDomainId();
        Long streamId = segmentPathKey.getStreamId();
        String ingressNode = segmentPathKey.getIngressNode();
        String egressNode = segmentPathKey.getEgressNode();
        detnetFlowId = DetnetServiceImpl.getInstance().generateId(ResourceType.DetnetFlowId);
        DetnetTransportTunnels detnetTransportTunnels = null;
        if (directionType.equals(SegmentDirectionType.Out)) {
            transportTunnelId = DetnetServiceImpl.getInstance().generateId(ResourceType.TransportTunnelId);
            detnetTransportTunnels = buildTransportTunnel(ingressNode,egressNode);
        }
        DetnetFlows detnetFlows = buildDetnetFlow(ingressNode,egressNode);
        return DetnetServiceDb.getInstance().writeServiceInstanceToDeviceManagerDB(domainId,streamId,ingressNode,
                detnetFlows, detnetTransportTunnels);
    }

    private DetnetTransportTunnels buildTransportTunnel(String ingressNode, String egressNode) {
        DetnetTransportTunnelsBuilder transportTunnels = new DetnetTransportTunnelsBuilder();
        transportTunnels.setTransportTunnelId(transportTunnelId);
        if (encapsulationType != null) {
            switch (encapsulationType) {
                case Mpls:
                    transportTunnels.setTunnelType(new org.opendaylight.yang.gen.v1.urn.detnet.service.instance
                            .rev180904.detnet.transport.tunnel.tunnel.type.MPLSBuilder()
                            .setMplsEncapsulation(new MplsEncapsulationBuilder()
                                    .setMplsTunnel(new MplsTunnelBuilder()
                                            .setSourceNodeId(ingressNode)
                                            .setDestNodeId(egressNode)
                                            .setDestRouterId(getIpv4Address(egressNode))
                                            .setExplicitPath(new ExplicitPathBuilder()
                                                    .setPathLink(buildPathLinks(path))
                                                    .build())
                                            .build())
                                    .build())
                            .build());
                    break;
                case Ipv4:
                    transportTunnels.setTunnelType(new IPv4Builder()
                            .setIpv4Encapsulation(new Ipv4EncapsulationBuilder()
                                    .setSrcIpv4Address(getIpv4Address(ingressNode))
                                    .setDestIpv4Address(getIpv4Address(egressNode))
                                    .build())
                            .build());
                    break;
                case Ipv6:
                    transportTunnels.setTunnelType(new IPv6Builder()
                            .setIpv6Encapsulation(new Ipv6EncapsulationBuilder()
                                    .setSrcIpv6Address(getIpv6Address(ingressNode))
                                    .setDestIpv6Address(getIpv6Address(egressNode))
                                    .setNextHeader((short) 6)
                                    .build())
                            .build());
                    break;
                default:
                    break;
            }
        }
        return transportTunnels.build();
    }

    private List<PathLink> buildPathLinks(LinkedList<DetnetLink> paths) {
        List<PathLink> pathLinks = new ArrayList<>();
        for (DetnetLink link : paths) {
            pathLinks.add(new PathLinkBuilder(link).build());
        }
        return pathLinks;
    }

    private DetnetFlows buildDetnetFlow(String local, String peer) {
        DetnetFlowsBuilder detnetFlows = new DetnetFlowsBuilder();
        detnetFlows.setDetnetFlowId(detnetFlowId);
        if (encapsulationType != null) {
            switch (encapsulationType) {
                case Mpls:
                    detnetFlows.setFlowType(new MPLSBuilder()
                            .setVpn(new VpnBuilder()
                                    .setLocal(getIpv4Address(local))
                                    .setPeer(getIpv4Address(peer))
                                    .build())
                            .build());
                    break;
                case Ipv4:
                    detnetFlows.setFlowType(new IPBuilder()
                            .setIpFlowType(new Ipv4Builder()
                                    .setSrcIpv4Address(getIpv4Address(local))
                                    .setDestIpv4Address(getIpv4Address(peer))
                                    .build())
                            .build());
                    break;
                case Ipv6:
                    detnetFlows.setFlowType(new IPBuilder()
                            .setIpFlowType(new Ipv6Builder()
                                    .setSrcIpv6Address(getIpv6Address(local))
                                    .setDestIpv6Address(getIpv6Address(peer))
                                    .build())
                            .build());
                    break;
                default:
                    break;
            }
        }
        return detnetFlows.build();
    }

    private Ipv4Address getIpv4Address(String nodeId) {
        return DetnetServiceDb.getInstance().getIpv4Address(nodeId);
    }

    private Ipv6Address getIpv6Address(String nodeId) {
        return DetnetServiceDb.getInstance().getIpv6Address(nodeId);
    }

    public Long getDetnetFlowId() {
        return detnetFlowId;
    }

    public Long getTransportTunnelId() {
        return transportTunnelId;
    }

    public SegmentDirectionType getDirectionType() {
        return directionType;
    }

    public void setDirectionType(SegmentDirectionType directionType) {
        this.directionType = directionType;
    }

    public enum SegmentDirectionType {
        In(0, "in-segment"),
        Out(1, "out-segment");

        String name;
        int value;

        SegmentDirectionType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getIntValue() {
            return value;
        }
    }
}
