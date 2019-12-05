/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.detnetservice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.DetnetEncapsulationType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.ClientFlowTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.client.flow.type.flow.type.L3FlowIdentificationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.DetnetPath;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.DetnetPathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.RelayNode;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.RelayNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.detnet.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlow;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.client.flow.TrafficSpecificationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class DetnetServiceMockUtils extends AbstractConcurrentDataBrokerTest {

    public static DetnetLink buildLink(String srcNode, String srcPort, String destPort, String destNode, long metric) {
        return new DetnetLinkBuilder()
                .setLinkSource(buildSrc(srcNode, srcPort))
                .setLinkDest(buildDest(destNode, destPort))
                .setLinkId(srcNode + srcPort + destPort + destNode)
                .setMetric(metric)
                .setLinkDelay(10L)
                .setAvailableDetnetBandwidth(500000L)
                .setMaximumReservableBandwidth(800000L)
                .setReservedDetnetBandwidth(0L)
                .build();
    }

    public static LinkDest buildDest(String destNode, String destPort) {
        LinkDestBuilder build = new LinkDestBuilder();

        if (destNode != null) {
            build.setDestNode(destNode);
        }
        if (destPort != null) {
            build.setDestTp(destPort);
        }
        return build.build();
    }

    public static LinkSource buildSrc(String srcNode, String srcPort) {
        LinkSourceBuilder build = new LinkSourceBuilder();

        if (srcNode != null) {
            build.setSourceNode(srcNode);
        }
        if (srcPort != null) {
            build.setSourceTp(srcPort);
        }
        return build.build();
    }

    public static List<PathLink> buildNode14PathLinks() throws ExecutionException, InterruptedException {
        List<PathLink> links = new ArrayList<>();
        links.add(new PathLinkBuilder(DetnetServiceMockUtils
                .buildLink("11.11.11.11", "link12", "link21", "22.22.22.22",10)).build());
        links.add(new PathLinkBuilder(DetnetServiceMockUtils
                .buildLink("22.22.22.22", "link24", "link42", "44.44.44.44",10)).build());
        return links;
    }

    public static List<PathLink> buildNode15PathLinks() throws ExecutionException, InterruptedException {
        List<PathLink> links = new ArrayList<>();
        links.add(new PathLinkBuilder(DetnetServiceMockUtils
                .buildLink("11.11.11.11", "link13", "link31", "33.33.33.33",10)).build());
        links.add(new PathLinkBuilder(DetnetServiceMockUtils
                .buildLink("33.33.33.33", "link35", "link53", "55.55.55.55",10)).build());
        return links;
    }

    public static List<DetnetPath> buildNode14DetnetPath() throws ExecutionException, InterruptedException {
        List<DetnetPath> detnetPaths = new ArrayList<>();
        List<PathLink> links = buildNode14PathLinks();
        detnetPaths.add(new DetnetPathBuilder()
                .setIngressNode("11.11.11.11")
                .setEgressNode("44.44.44.44")
                .setPath(new PathBuilder()
                        .setPathLink(links)
                        .build())
                .build());
        return detnetPaths;
    }

    public static List<DetnetPath> buildNode145DetnetPath() throws ExecutionException, InterruptedException {
        List<DetnetPath> detnetPaths = new ArrayList<>();
        List<PathLink> links = buildNode14PathLinks();
        List<PathLink> links1 = buildNode15PathLinks();
        detnetPaths.add(new DetnetPathBuilder()
                .setIngressNode("11.11.11.11")
                .setEgressNode("44.44.44.44")
                .setPath(new PathBuilder()
                        .setPathLink(links)
                        .build())
                .build());

        detnetPaths.add(new DetnetPathBuilder()
                .setIngressNode("11.11.11.11")
                .setEgressNode("55.55.55.55")
                .setPath(new PathBuilder()
                        .setPathLink(links1)
                        .build())
                .build());
        return detnetPaths;
    }

    public static List<ClientFlow> buildClientFlow() {
        List<ClientFlow> clientFlows = new ArrayList<>();
        clientFlows.add(new ClientFlowBuilder()
                .setClientFlowId(111L)
                .setClientFlowType(new ClientFlowTypeBuilder()
                                .setFlowType(new L3FlowIdentificationBuilder()
                                        .setIpFlowType(new Ipv4Builder()
                                                .setSrcIpv4Address(new Ipv4Address("1.1.1.1"))
                                                .setDestIpv4Address(new Ipv4Address("2.2.2.2"))
                                                .setDscp((short) 23)
                                                .build())
                                        .setSourcePort(new PortNumber(67))
                                        .setDestinationPort(new PortNumber(68))
                                        .setProtocol((short) 15)
                                        .build())
                        .build())
                .setTrafficSpecification(new TrafficSpecificationBuilder()
                        .setInterval(1000L)
                        .setMaxPacketsPerInterval(8000L)
                        .setMaxPayloadSize(128L)
                        .build())
                .build());
        return clientFlows;
    }

    public static List<RelayNode> buildRelayNodes() {
        List<RelayNode> relayNodes = new ArrayList<>();
        relayNodes.add(new RelayNodeBuilder()
                .setRelayNodeId("22.22.22.22")
                .setInEncapsulation(DetnetEncapsulationType.Mpls)
                .setOutEncapsulation(DetnetEncapsulationType.Mpls)
                .build());
        relayNodes.add(new RelayNodeBuilder()
                .setRelayNodeId("33.33.33.33")
                .setInEncapsulation(DetnetEncapsulationType.Mpls)
                .setOutEncapsulation(DetnetEncapsulationType.Mpls)
                .build());
        return relayNodes;
    }

    public static List<RelayNode> buildRelayNodesForSeg() {
        List<RelayNode> relayNodes = new ArrayList<>();
        relayNodes.add(new RelayNodeBuilder()
                .setRelayNodeId("33.33.33.33")
                .setInEncapsulation(DetnetEncapsulationType.Mpls)
                .setOutEncapsulation(DetnetEncapsulationType.Ipv6)
                .build());
        return relayNodes;
    }

    public static List<DetnetPath> buildNode135SinglePath() throws ExecutionException, InterruptedException {
        List<DetnetPath> detnetPaths = new ArrayList<>();
        List<PathLink> links = buildNode15PathLinks();
        detnetPaths.add(new DetnetPathBuilder()
                .setIngressNode("11.11.11.11")
                .setEgressNode("55.55.55.55")
                .setPath(new PathBuilder()
                        .setPathLink(links)
                        .build())
                .build());
        return detnetPaths;
    }

    public static void writeDetnetTopology(DataBroker dataBroker) {
        List<DetnetNode> nodes = new ArrayList<>();
        nodes.add(buildNode("11.11.11.11","192.168.1.1/32","2001:db8:3c4d:11::/64"));
        nodes.add(buildNode("22.22.22.22","192.168.2.2/32","2001:db8:3c4d:12::/64"));
        nodes.add(buildNode("33.33.33.33","192.168.3.3/32","2001:db8:3c4d:13::/64"));
        nodes.add(buildNode("44.44.44.44","192.168.4.4/32","2001:db8:3c4d:14::/64"));
        nodes.add(buildNode("55.55.55.55","192.168.5.5/32","2001:db8:3c4d:15::/64"));
        nodes.add(buildNode("66.66.66.66","192.168.6.6/32","2001:db8:3c4d:16::/64"));
        DetnetTopology data = new DetnetTopologyBuilder().setTopologyId("topology").setDetnetNode(nodes).build();
        InstanceIdentifier<DetnetTopology> path = InstanceIdentifier.create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class,new DetnetTopologyKey("topology"));
        DataOperator.writeData(DataOperator.OperateType.PUT,dataBroker,path,data);
    }

    public static void deleteDetnetTopology(DataBroker dataBroker) {
        InstanceIdentifier<DetnetTopology> path = InstanceIdentifier.create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class,new DetnetTopologyKey("topology"));
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,path,null);
    }

    private static DetnetNode buildNode(String nodeId, String ipv4, String ipv6) {
        return new DetnetNodeBuilder()
                .setNodeId(nodeId)
                .setIpv4Prefix(new Ipv4Prefix(ipv4))
                .setIpv6Prefix(new Ipv6Prefix(ipv6))
                .build();
    }
}