/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.Domains;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DomainsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DomainsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Ltps;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Segments;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.SegmentsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClassesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TopoMockUtils extends AbstractConcurrentDataBrokerTest {
    public static final String DEFAULT_TOPO = "detnet-topology";

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

    public static DetnetLink buildLinkWithBandwidth(DetnetLink link, Long bandwidth) {
        return new DetnetLinkBuilder(link).setAvailableDetnetBandwidth(bandwidth).build();
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

    public static List<DetnetLink> buildLinkPair(String node, String port, String oppesitPort,
                                               String oppesiteNode,long metric) {
        List<DetnetLink> list = new ArrayList<>();

        list.add(buildLink(node, port, oppesitPort, oppesiteNode,metric));
        list.add(buildLink(oppesiteNode, oppesitPort, port, node,metric));

        return list;
    }


/*


    /*        10
    *       R1-----R2
    *       |      |
    *  10   |      | 10
    *       |      |
    *       R3-----R4
    *          10
    */

    public static List<DetnetLink> buildFourNodeTopo() throws ExecutionException, InterruptedException {
        List<DetnetLink> links = new ArrayList<>();
        links.addAll(TopoMockUtils.buildLinkPair("node1", "link12", "link21", "node2",10));
        links.addAll(TopoMockUtils.buildLinkPair("node2", "link24", "link42", "node4",10));
        links.addAll(TopoMockUtils.buildLinkPair("node1", "link13", "link31", "node3",10));
        links.addAll(TopoMockUtils.buildLinkPair("node3", "link34", "link43", "node4",10));
        return links;
    }



    /*   10        10
 *  R1--------R2--------R5
 *  |         |         |
 *  | 10      | 10      | 10
 *  |         |         |
 *  R3--------R4-------R6
 *      10        10
 */
    public static List<DetnetLink> getTopo6Node() {
        List<DetnetLink> links = new ArrayList<>();

        links.addAll(TopoMockUtils.buildLinkPair("node1", "link12", "link21", "node2", 10));

        links.addAll(TopoMockUtils.buildLinkPair("node1", "link13", "link31", "node3", 10));

        links.addAll(TopoMockUtils.buildLinkPair("node2", "link25", "link52", "node5", 10));

        links.addAll(TopoMockUtils.buildLinkPair("node5", "link56", "link65", "node6", 10));

        links.addAll(TopoMockUtils.buildLinkPair("node3", "link34", "link43", "node4", 10));

        links.addAll(TopoMockUtils.buildLinkPair("node4", "link46", "link64", "node6", 10));

        links.addAll(TopoMockUtils.buildLinkPair("node2", "link24", "link42", "node4", 10));

        return links;
    }

    public static void buildNodeInOneDomain(boolean isNode2InDomain1, DataBroker dataBroker) {
        //domain 1,segment 1
        List<String> nodeIdList = new ArrayList<>();
        nodeIdList.add("node1");
        if (isNode2InDomain1) {
            nodeIdList.add("node2");
        }
        nodeIdList.add("node3");
        nodeIdList.add("node4");
        nodeIdList.add("node5");
        nodeIdList.add("node6");
        List<Segments> segments = new ArrayList<>();
        segments.add(new SegmentsBuilder().setSegmentId(1).build());
        for (String node : nodeIdList) {
            DetnetNode detnetNode = buildDetnetNodeInfo(node,segments);
            DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildDetnetNodePath(node),detnetNode);
        }
        Domains domain = new DomainsBuilder().setDomainId(1).setSegments(trans(segments)).build();
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildDomainPath(1),domain);
    }

    private static InstanceIdentifier<Domains> buildDomainPath(int domainId) {
        return InstanceIdentifier.create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(DEFAULT_TOPO))
                .child(Domains.class,new DomainsKey(domainId));
    }

    private static List<org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology
            .detnet.topology.domains.Segments> trans(List<Segments> segments) {
        List<org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology
                .detnet.topology.domains.Segments> segmentList = new ArrayList<>();
        for (Segments segment : segments) {
            segmentList.add(new org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology
                    .detnet.topology.domains.SegmentsBuilder().setSegmentId(segment.getSegmentId()).build());
        }
        return segmentList;
    }

    public static void buildNodeInTwoDomain(boolean isNode2InDomain2, DataBroker dataBroker) {
        //domain 1,segment 1,node1-6;domain 2, segment 2,node1-4,node6;
        List<String> nodeIdList = new ArrayList<>();
        nodeIdList.add("node1");
        nodeIdList.add("node2");
        nodeIdList.add("node3");
        nodeIdList.add("node4");
        nodeIdList.add("node5");
        nodeIdList.add("node6");
        List<Segments> segments = new ArrayList<>();
        segments.add(new SegmentsBuilder().setSegmentId(1).build());
        segments.add(new SegmentsBuilder().setSegmentId(2).build());
        for (String node : nodeIdList) {
            DetnetNode detnetNode;
            if (node.equals("node2") && !isNode2InDomain2) {
                List<Segments> segmentList = new ArrayList<>();
                segmentList.add(new SegmentsBuilder().setSegmentId(1).build());
                detnetNode = buildDetnetNodeInfo(node,segmentList);
            } else {
                detnetNode = buildDetnetNodeInfo(node, segments);
            }
            DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildDetnetNodePath(node),detnetNode);
        }
        segments.clear();
        segments.add(new SegmentsBuilder().setSegmentId(1).build());
        Domains domain = new DomainsBuilder().setDomainId(1).setSegments(trans(segments)).build();
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildDomainPath(1),domain);
        segments.clear();
        segments.add(new SegmentsBuilder().setSegmentId(2).build());
        domain = new DomainsBuilder().setDomainId(2).setSegments(trans(segments)).build();
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildDomainPath(2),domain);
    }

    private static DetnetNode buildDetnetNodeInfo(String node, List<Segments> segments) {
        return new DetnetNodeBuilder()
                .setNodeId(node)
                .setProcessDelay(10L)
                .setSegments(segments)
                .setLtps(buildLtps(node))
                .build();
    }

    private static List<Ltps> buildLtps(String node) {
        List<Ltps> ltpList = new ArrayList<>();
        if (node.equals("node1")) {
            ltpList.add(new LtpsBuilder().setTpId("link12").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link13").setTrafficClasses(buildTrafficClass()).build());
        }
        if (node.equals("node2")) {
            ltpList.add(new LtpsBuilder().setTpId("link21").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link25").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link24").setTrafficClasses(buildTrafficClass()).build());
        }
        if (node.equals("node3")) {
            ltpList.add(new LtpsBuilder().setTpId("link31").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link34").setTrafficClasses(buildTrafficClass()).build());
        }
        if (node.equals("node4")) {
            ltpList.add(new LtpsBuilder().setTpId("link42").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link43").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link46").setTrafficClasses(buildTrafficClass()).build());
        }
        if (node.equals("node5")) {
            ltpList.add(new LtpsBuilder().setTpId("link52").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link56").setTrafficClasses(buildTrafficClass()).build());
        }
        if (node.equals("node6")) {
            ltpList.add(new LtpsBuilder().setTpId("link65").setTrafficClasses(buildTrafficClass()).build());
            ltpList.add(new LtpsBuilder().setTpId("link64").setTrafficClasses(buildTrafficClass()).build());
        }
        return ltpList;
    }

    private static List<TrafficClasses> buildTrafficClass() {
        List<TrafficClasses> trafficClassList = new ArrayList<>();
        for (Short i = 0; i < 8; i++) {
            trafficClassList.add(new TrafficClassesBuilder()
                    .setTcIndex(i)
                    .setMinimumQueueDelay(10L + 1 * i)
                    .setMaximumQueueDelay(20L + 10 * i)
                    .build());
        }
        return trafficClassList;
    }

    private static InstanceIdentifier<DetnetNode> buildDetnetNodePath(String node) {
        return InstanceIdentifier.create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(DEFAULT_TOPO))
                .child(DetnetNode.class,new DetnetNodeKey(node));
    }
}