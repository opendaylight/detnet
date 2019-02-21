/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.topology.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.Ltp;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.Domains;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DomainsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.domains.Segments;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.domains.SegmentsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Ltps;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClassesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyDataManager {
    private static final Logger LOG =  LoggerFactory.getLogger(TopologyDataManager.class);
    private final DataBroker dataBroker;

    public TopologyDataManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public DetnetNetworkTopology getDetnetNetworkTopology() {
        return DataOperator.readData(dataBroker, getDetnetNetworkTopologyPath());
    }

    public DetnetTopology getDetnetTopology(String topologyId) {
        return DataOperator.readData(dataBroker, getDetnetTopologyPath(topologyId));
    }

    public DetnetNode getDetnetNode(String topologyId, String nodeId) {
        return DataOperator.readData(dataBroker, getDetnetNodePath(topologyId, nodeId));
    }

    public boolean writeDetnetNode(String topologyId,String  nodeId, DetnetNode node) {
        return DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                getDetnetNodePath(topologyId, nodeId), node);
    }

    public boolean deleteDetnetNode(String topologyId,String  nodeId) {
        return DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker,
                getDetnetNodePath(topologyId, nodeId), null);
    }

    public DetnetLink getDetnetLink(String topologyId, String linkId) {
        return DataOperator.readData(dataBroker, getDetnetLinkPath(topologyId, linkId));
    }

    public boolean writeDetnetLink(String topologyId,String  linkId, DetnetLink link) {
        return DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                getDetnetLinkPath(topologyId, linkId), link);
    }

    public boolean deleteDetnetLink(String topologyId,String  linkId) {
        return DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker,
                getDetnetLinkPath(topologyId, linkId), null);
    }

    public Ltp getDetnetNodeLtp(String topologyId, String nodeId, String tpId) {
        return DataOperator.readData(dataBroker, getDetnetNodeLtpPath(topologyId, nodeId, tpId));
    }

    public boolean writeDetnetNodeLtp(String topologyId, String  nodeId, String tpId, Ltps ltp) {
        return DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                getDetnetNodeLtpPath(topologyId, nodeId, tpId), ltp);
    }

    public boolean deleteDetnetNodeLtp(String topologyId,String  nodeId, String tpId) {
        return DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker,
                getDetnetNodeLtpPath(topologyId, nodeId, tpId), null);
    }

    public TrafficClasses getDetnetNodeTrafficClass(String topologyId, String nodeId, String tpId, Short tcId) {
        return DataOperator.readData(dataBroker, getDetnetNodeTrafficClassPath(topologyId, nodeId, tpId, tcId));
    }

    public boolean writeDetnetNodeTrafficClass(String topologyId,String  nodeId, String tpId, Short tcId,
                                               TrafficClasses traffic) {
        return DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                getDetnetNodeTrafficClassPath(topologyId, nodeId, tpId, tcId), traffic);
    }

    public boolean deleteDetnetNodeTrafficClass(String topologyId,String  nodeId, String tpId, Short tcId) {
        return DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker,
                getDetnetNodeTrafficClassPath(topologyId, nodeId, tpId, tcId), null);
    }

    public Domains getDomain(String topologyId, Integer domainId) {
        return DataOperator.readData(dataBroker, getDomainPath(topologyId, domainId));
    }

    public boolean writeDomain(String topologyId, Integer domainId, Domains domain) {
        return DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                getDomainPath(topologyId, domainId), domain);
    }

    public boolean deleteDomain(String topologyId, Integer domainId) {
        return DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker,
                getDomainPath(topologyId, domainId), null);
    }

    public Segments getSegment(String topologyId, Integer domainId, Integer segmentId) {
        return DataOperator.readData(dataBroker, getSegmentPath(topologyId, domainId, segmentId));
    }

    public boolean deleteSegment(String topologyId, Integer domainId, Integer segmentId) {
        return DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker,
                getSegmentPath(topologyId, domainId, segmentId), null);
    }

    public List<Segments> processDomainSegments(List<Segments> segmentsList, List<Integer> listSegment) {
        for (Integer segmnet : listSegment) {
            org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology
                    .domains.Segments segments = new org.opendaylight.yang.gen.v1.urn.detnet.topology
                    .rev180823.detnet.network.topology.detnet.topology.domains.SegmentsBuilder()
                    .setSegmentId(segmnet).build();
            if (!segmentsList.contains(segments)) {
                segmentsList.add(segments);
            }
        }
        return segmentsList;
    }

    public List<DetnetNode> getDomainNodes(String topologyId, Domains domains) {
        List<DetnetNode> detnetNodesList = new ArrayList<>();
        DetnetTopology detnetTopology = getDetnetTopology(topologyId);
        if (null == detnetTopology) {
            LOG.error("queryDomainTopology rpc detnet-Topology is not exist!");
            return detnetNodesList;
        }
        List<DetnetNode> allNodesList = new DetnetTopologyBuilder(detnetTopology).getDetnetNode();
        for (DetnetNode node : allNodesList) {
            if (isNodeBelongToDomain(domains, node)) {
                detnetNodesList.add(node);
            }
        }
        return detnetNodesList;
    }

    public List<DetnetLink> getDomainLinks(String topologyId, Domains domains) {
        List<DetnetLink> detnetLinksList = new ArrayList<>();
        DetnetTopology detnetTopology = getDetnetTopology(topologyId);
        if (null == detnetTopology) {
            LOG.error("queryDomainTopology rpc detnet-Topology is not exist!");
            return detnetLinksList;
        }
        List<DetnetNode> allNodesList = detnetTopology.getDetnetNode();
        List<DetnetLink> linkList = detnetTopology.getDetnetLink();
        for (DetnetLink link : linkList) {
            String sourcNodeId = link.getLinkSource().getSourceNode();
            String destNodeId = link.getLinkDest().getDestNode();
            DetnetNode sourceNode = null;
            DetnetNode destNode = null;
            for (DetnetNode node : allNodesList) {
                if (node.getNodeId().equals(sourcNodeId)) {
                    sourceNode = node;
                } else if (node.getNodeId().equals(destNodeId)) {
                    destNode = node;
                }
            }
            if (null != sourceNode && null != destNode) {
                boolean findSourceFlag = isNodeBelongToDomain(domains, sourceNode);
                boolean findDestFlag = isNodeBelongToDomain(domains, destNode);
                if (findSourceFlag && findDestFlag) {
                    detnetLinksList.add(link);
                }
            }
        }
        return detnetLinksList;
    }

    private boolean isNodeBelongToDomain(Domains domains, DetnetNode node) {
        boolean findFlag = false;
        if (null == node.getSegments() || domains.getSegments().size() == 0) {
            return false;
        }
        List<org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Segments> segments = node
                .getSegments();
        if (null == segments) {
            return false;
        }
        List<Segments> segmentsList = domains.getSegments();
        for (org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Segments segment : segments) {
            for (Segments segmentsObj : segmentsList) {
                if (segment.getSegmentId().equals(segmentsObj.getSegmentId())) {
                    findFlag = true;
                    break;
                }
            }
            if (findFlag) {
                break;
            }
        }
        return findFlag;
    }

    public DetnetTopology getTopologyId(String topologyId) {
        return DataOperator.readData(dataBroker, getDetnetTopologyPath(topologyId));
    }

    public boolean writeTopologyId(String topologyId, DetnetTopology topology) {
        return DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                getDetnetTopologyPath(topologyId), topology);
    }

    private InstanceIdentifier<Segments> getSegmentPath(String topologyId, Integer domainId, Integer segmentId) {
        return getDomainPath(topologyId, domainId).child(Segments.class, new SegmentsKey(segmentId));
    }

    private InstanceIdentifier<Domains> getDomainPath(String topologyId, Integer domainId) {
        return getDetnetTopologyPath(topologyId).child(Domains.class, new DomainsKey(domainId));
    }

    private InstanceIdentifier<DetnetNetworkTopology> getDetnetNetworkTopologyPath() {
        return InstanceIdentifier.create(DetnetNetworkTopology.class);
    }

    private InstanceIdentifier<DetnetTopology> getDetnetTopologyPath(String topologyId) {
        return getDetnetNetworkTopologyPath().child(DetnetTopology.class, new DetnetTopologyKey(topologyId));
    }

    private InstanceIdentifier<DetnetNode> getDetnetNodePath(String topologyId, String nodeId) {
        return getDetnetTopologyPath(topologyId).child(DetnetNode.class, new DetnetNodeKey(nodeId));
    }

    private InstanceIdentifier<DetnetLink> getDetnetLinkPath(String topologyId, String linkId) {
        return getDetnetTopologyPath(topologyId).child(DetnetLink.class, new DetnetLinkKey(linkId));
    }

    private InstanceIdentifier<Ltps> getDetnetNodeLtpPath(String topologyId, String nodeId, String tpId) {
        return getDetnetNodePath(topologyId, nodeId).child(Ltps.class, new LtpsKey(tpId));
    }

    private InstanceIdentifier<TrafficClasses> getDetnetNodeTrafficClassPath(
            String topologyId, String nodeId, String tpId, Short tcId) {
        return getDetnetNodeLtpPath(topologyId, nodeId, tpId)
                .child(TrafficClasses.class, new TrafficClassesKey(tcId));
    }
}
