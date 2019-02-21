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
import java.util.concurrent.Future;

import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddNodesToSegmentInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddNodesToSegmentOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddNodesToSegmentOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddTopologyIdInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddTopologyIdOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddTopologyIdOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeLtpInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeLtpOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeLtpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetLinkInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetLinkOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeLtpInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeLtpOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeLtpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeTrafficClassInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeTrafficClassOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeTrafficClassOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteNodesFromSegmentInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteNodesFromSegmentOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteNodesFromSegmentOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteSegmentsFromDomainInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteSegmentsFromDomainOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteSegmentsFromDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DetnetTopologyApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.LoadTopologyIdOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.LoadTopologyIdOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.load.topology.id.output.Topology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.load.topology.id.output.TopologyBuilder;
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
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.Domains;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DomainsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DomainsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Segments;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.SegmentsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClassesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClassesKey;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyServiceImpl implements DetnetTopologyApiService {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyServiceImpl.class);
    private TopologyDataManager topologyDataManager;

    public TopologyServiceImpl(TopologyDataManager topologyDataManager) {
        this.topologyDataManager = topologyDataManager;
    }

    @Override
    public Future<RpcResult<AddDetnetLinkOutput>> addDetnetLink(AddDetnetLinkInput input) {
        AddDetnetLinkOutputBuilder configBuilder = new AddDetnetLinkOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getLinkBandwidth()
                || null == input.getLinkId() || null == input.getLinkSource().getSourceNode()
                || null == input.getLinkSource().getSourceTp() || null == input.getLinkDest().getDestNode()
                || null == input.getLinkDest().getDestTp() || null == input.getMaximumReservableBandwidth()
                || null == input.getAvailableDetnetBandwidth()) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }

        LinkSource linkSource = new LinkSourceBuilder()
                .setSourceNode(input.getLinkSource().getSourceNode())
                .setSourceTp(input.getLinkSource().getSourceTp())
                .build();
        LinkDest linkDest = new LinkDestBuilder()
                .setDestNode(input.getLinkDest().getDestNode())
                .setDestTp(input.getLinkDest().getDestTp())
                .build();
        DetnetLinkBuilder detnetLinkBuilder = new DetnetLinkBuilder();
        detnetLinkBuilder.setLinkId(input.getLinkId());
        detnetLinkBuilder.setKey(new DetnetLinkKey(input.getLinkId()))
                .setLinkSource(linkSource)
                .setLinkDest(linkDest)
                .setMaximumReservableBandwidth(input.getMaximumReservableBandwidth())
                .setLinkBandwidth(input.getLinkBandwidth())
                .setAvailableDetnetBandwidth(input.getAvailableDetnetBandwidth());
        if (null != input.getReservedDetnetBandwidth()) {
            detnetLinkBuilder.setReservedDetnetBandwidth(input.getReservedDetnetBandwidth());
        }
        if (null != input.getLoss()) {
            detnetLinkBuilder.setLoss(input.getLoss());
        }
        if (null != input.getMetric()) {
            detnetLinkBuilder.setMetric(input.getMetric());
        }
        if (null != input.getLinkDelay()) {
            detnetLinkBuilder.setLinkDelay(input.getLinkDelay());
        }

        if (!topologyDataManager.writeDetnetLink(input.getTopologyId(), input.getLinkId(),
                detnetLinkBuilder.build())) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "write link to datastore failed!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }
        configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(configBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<ConfigDetnetNodeLtpOutput>> configDetnetNodeLtp(ConfigDetnetNodeLtpInput input) {
        ConfigDetnetNodeLtpOutputBuilder configDetnetNodeLtpOutputBuilder = new ConfigDetnetNodeLtpOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getNodeId()
                || null == input.getTpId()) {
            configDetnetNodeLtpOutputBuilder.setConfigureResult(
                    RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(configDetnetNodeLtpOutputBuilder.build()).buildFuture();
        }

        LtpsBuilder ltpsBuilder = new LtpsBuilder();
        ltpsBuilder.setTpId(input.getTpId());
        ltpsBuilder.setKey(new LtpsKey(input.getTpId()));
        if (null != input.getIfName()) {
            ltpsBuilder.setIfName(input.getIfName());
        }
        if (null != input.getTpIndex()) {
            ltpsBuilder.setTpIndex(input.getTpIndex());
        }
        if (null != input.getTpIpPrefix()) {
            ltpsBuilder.setTpIpPrefix(input.getTpIpPrefix());
        }
        if (null != input.getDetnetEncapsulationType()) {
            ltpsBuilder.setDetnetEncapsulationType(input.getDetnetEncapsulationType());
        }
        if (null != input.getNodePreofCapability()) {
            ltpsBuilder.setNodePreofCapability(input.getNodePreofCapability());
        }
        if (null != input.getTrafficClasses()) {
            ltpsBuilder.setTrafficClasses(input.getTrafficClasses());
        }
        if (!topologyDataManager.writeDetnetNodeLtp(input.getTopologyId(), input.getNodeId(),
                input.getTpId(), ltpsBuilder.build())) {
            configDetnetNodeLtpOutputBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false,
                    "write ltp to datastore failed!"));
            return RpcResultBuilder.success(configDetnetNodeLtpOutputBuilder.build()).buildFuture();
        }
        configDetnetNodeLtpOutputBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(configDetnetNodeLtpOutputBuilder.build()).buildFuture();
    }

    @Override
    public  Future<RpcResult<ConfigDetnetNodeTrafficClassOutput>> configDetnetNodeTrafficClass(
            ConfigDetnetNodeTrafficClassInput input) {
        ConfigDetnetNodeTrafficClassOutputBuilder configBuilder = new ConfigDetnetNodeTrafficClassOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getNodeId() || null == input.getTpId()
                || null == input.getTcIndex()) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }

        TrafficClassesBuilder trafficClassesBuilder = new TrafficClassesBuilder();
        trafficClassesBuilder.setTcIndex(input.getTcIndex())
                .setKey(new TrafficClassesKey(input.getTcIndex()));
        if (null != input.getMaximumQueueDelay()) {
            trafficClassesBuilder.setMaximumQueueDelay(input.getMaximumQueueDelay());
        }
        if (null != input.getMinimumQueueDelay()) {
            trafficClassesBuilder.setMinimumQueueDelay(input.getMinimumQueueDelay());
        }

        if (!topologyDataManager.writeDetnetNodeTrafficClass(input.getTopologyId(), input.getNodeId(),
                input.getTpId(), input.getTcIndex(), trafficClassesBuilder.build())) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "write node to datastore failed!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }
        configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(configBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<ConfigDetnetNodeOutput>> configDetnetNode(ConfigDetnetNodeInput input) {
        ConfigDetnetNodeOutputBuilder configBuilder = new ConfigDetnetNodeOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getNodeId()) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }

        DetnetNodeBuilder detnetNodeBuilder = new DetnetNodeBuilder();
        detnetNodeBuilder.setNodeId(input.getNodeId());
        detnetNodeBuilder.setKey(new DetnetNodeKey(input.getNodeId()));
        if (null != input.getName()) {
            detnetNodeBuilder.setName(input.getName());
        }
        if (null != input.getProcessDelay()) {
            detnetNodeBuilder.setProcessDelay(input.getProcessDelay());
        }
        if (null != input.getLatitude()) {
            detnetNodeBuilder.setLatitude(input.getLatitude());
        }
        if (null != input.getLongitude()) {
            detnetNodeBuilder.setLongitude(input.getLongitude());
        }
        if (null != input.getIpv4Prefix()) {
            detnetNodeBuilder.setIpv4Prefix(input.getIpv4Prefix());
        }
        if (null != input.getIpv6Prefix()) {
            detnetNodeBuilder.setIpv6Prefix(input.getIpv6Prefix());
        }
        if (null != input.isIsRelayNode()) {
            detnetNodeBuilder.setIsRelayNode(input.isIsRelayNode());
        }

        if (!topologyDataManager.writeDetnetNode(input.getTopologyId(), input.getNodeId(),
                detnetNodeBuilder.build())) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "write node to datastore failed!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }
        configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(configBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteDetnetNodeLtpOutput>> deleteDetnetNodeLtp(
            DeleteDetnetNodeLtpInput input) {
        DeleteDetnetNodeLtpOutputBuilder deleteBuilder = new DeleteDetnetNodeLtpOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getNodeId() || null == input.getTpId()) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (null == topologyDataManager.getDetnetNodeLtp(input.getTopologyId(), input.getNodeId(), input.getTpId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "tp is not exist!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (!topologyDataManager.deleteDetnetNodeLtp(input.getTopologyId(), input.getNodeId(), input.getTpId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                    false, "delete node form datastore failed!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }
        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<AddNodesToSegmentOutput>> addNodesToSegment(AddNodesToSegmentInput input) {
        LOG.info("addNodesToSegment input {}", input);
        AddNodesToSegmentOutputBuilder addBuilder = new AddNodesToSegmentOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getSegmentId()
                || null == input.getDetnetNodes()) {
            addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(addBuilder.build()).buildFuture();
        }
        for (String node : input.getDetnetNodes()) {
            DetnetNode detnetNode = topologyDataManager.getDetnetNode(input.getTopologyId(), node);
            if (null == detnetNode) {
                addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "node " + node + " is not exist!"));
                return RpcResultBuilder.success(addBuilder.build()).buildFuture();
            } else {
                DetnetNodeBuilder detnetNodeBuilder = new DetnetNodeBuilder(detnetNode);
                List<Segments> segmentsList = detnetNode.getSegments();
                SegmentsBuilder segments = new SegmentsBuilder();
                segments.setSegmentId(input.getSegmentId());
                if (null != segmentsList) {
                    if (!segmentsList.contains(segments.build())) {
                        segmentsList.add(segments.build());
                    }
                } else {
                    List<Segments> newSegmentsList = new ArrayList<>();
                    newSegmentsList.add(segments.build());
                    detnetNodeBuilder.setSegments(newSegmentsList);
                }
                if (!topologyDataManager.writeDetnetNode(input.getTopologyId(), node,
                        detnetNodeBuilder.build())) {
                    addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                            false, "add node " + node
                                    + " to segment " + input.getSegmentId() + " failed!"));
                    return RpcResultBuilder.success(addBuilder.build()).buildFuture();
                }
            }
        }
        addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(addBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryDomainTopologyOutput>> queryDomainTopology(QueryDomainTopologyInput input) {
        LOG.info("queryDomainTopology input {}", input);
        if (null == input || null == input.getTopologyId() || null == input.getDomainId()) {
            return RpcReturnUtil.returnErr("Illegal argument!");
        }
        QueryDomainTopologyOutputBuilder queryBuilder = new QueryDomainTopologyOutputBuilder();
        Domains domains = topologyDataManager.getDomain(input.getTopologyId(), input.getDomainId());
        if (null == domains) {
            return RpcResultBuilder.success(queryBuilder.build()).buildFuture();
        }
        List<DetnetNode> detnetNodesList = topologyDataManager.getDomainNodes(input.getTopologyId(), domains);
        List<DetnetLink> detnetLinksList = topologyDataManager.getDomainLinks(input.getTopologyId(), domains);
        List<org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.query.domain.topology.output.DetnetNode>
                nodesList = new ArrayList<>();
        List<org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.query.domain.topology.output.DetnetLink>
                linksList = new ArrayList<>();
        for (DetnetNode
                node : detnetNodesList) {
            org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.query.domain.topology.output.DetnetNode
                    newNode = new org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.query.domain
                    .topology.output.DetnetNodeBuilder(node).build();
            nodesList.add(newNode);
        }
        for (DetnetLink
                link : detnetLinksList) {
            org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.query.domain.topology.output.DetnetLink
                    newLink = new org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.query.domain
                    .topology.output.DetnetLinkBuilder(link).build();
            linksList.add(newLink);
        }
        queryBuilder.setDetnetNode(nodesList).setDetnetLink(linksList);
        return RpcResultBuilder.success(queryBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<ConfigSegmentsToDomainOutput>> configSegmentsToDomain(ConfigSegmentsToDomainInput input) {
        LOG.info("configSegmentsToDomain input {}", input);
        ConfigSegmentsToDomainOutputBuilder configBuilder = new ConfigSegmentsToDomainOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getDomainId()
                || null == input.getSegments()) {
            configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(configBuilder.build()).buildFuture();
        }
        Domains domain = topologyDataManager.getDomain(input.getTopologyId(), input.getDomainId());
        List<org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet
                .topology.domains.Segments> newSegmentsList = new ArrayList<>();
        if (null != domain) {
            //LOG.info("domain != NULL {}", domain);
            List<org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet
                    .topology.domains.Segments> segmentsList = domain.getSegments();
            DomainsBuilder domainsBuilder = new DomainsBuilder(domain);

            if (null != segmentsList) {
                //LOG.info("segmentsList  != NULL {}", segmentsList);
                segmentsList = topologyDataManager.processDomainSegments(segmentsList, input.getSegments());
                domainsBuilder.setSegments(segmentsList);
            } else {
                //LOG.info("segmentsList == NULL {}");
                newSegmentsList = topologyDataManager.processDomainSegments(newSegmentsList, input.getSegments());
                domainsBuilder.setSegments(newSegmentsList);
            }
            if (!topologyDataManager.writeDomain(input.getTopologyId(), input.getDomainId(), domainsBuilder.build())) {
                configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                        false, "add segments to domain " + input.getDomainId() + " failed!"));
                return RpcResultBuilder.success(configBuilder.build()).buildFuture();
            }
        } else {
            //LOG.info("domain == NULL {}");
            DomainsBuilder domainsBuilder = new DomainsBuilder();
            newSegmentsList = topologyDataManager.processDomainSegments(newSegmentsList, input.getSegments());
            domainsBuilder.setSegments(newSegmentsList)
                    .setDomainId(input.getDomainId())
                    .setKey(new DomainsKey(input.getDomainId()));
            if (!topologyDataManager.writeDomain(input.getTopologyId(), input.getDomainId(), domainsBuilder.build())) {
                configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                        false, "add segments to domain " + input.getDomainId() + " failed!"));
                return RpcResultBuilder.success(configBuilder.build()).buildFuture();
            }
        }
        configBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(configBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteDetnetNodeTrafficClassOutput>> deleteDetnetNodeTrafficClass(
            DeleteDetnetNodeTrafficClassInput input) {
        DeleteDetnetNodeTrafficClassOutputBuilder deleteBuilder = new
                DeleteDetnetNodeTrafficClassOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getNodeId()
                || null == input.getTpId() || null == input.getTcIndex()) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (null == topologyDataManager.getDetnetNodeTrafficClass(
                input.getTopologyId(), input.getNodeId(), input.getTpId(), input.getTcIndex())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "trafic class is not exist!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (!topologyDataManager.deleteDetnetNodeTrafficClass(
                input.getTopologyId(), input.getNodeId(), input.getTpId(), input.getTcIndex())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false,
                    "delete trafic class form datastore failed!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }
        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteSegmentsFromDomainOutput>> deleteSegmentsFromDomain(
            DeleteSegmentsFromDomainInput input) {
        DeleteSegmentsFromDomainOutputBuilder deleteBuilder = new DeleteSegmentsFromDomainOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getDomainId()
                || null == input.getSegmentId()) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }
        if (null == topologyDataManager.getSegment(input.getTopologyId(), input.getDomainId(), input.getSegmentId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "segment is not exist!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }
        if (topologyDataManager.getDomain(input.getTopologyId(), input.getDomainId()).getSegments().size() == 1) {
            if (!topologyDataManager.deleteDomain(input.getTopologyId(), input.getDomainId())) {
                deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                        false, "delete segment form datastore failed!"));
                return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
            }
        } else {
            if (!topologyDataManager.deleteSegment(input.getTopologyId(), input.getDomainId(), input.getSegmentId())) {
                deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                        false, "delete segment form datastore failed!"));
                return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
            }
        }

        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteNodesFromSegmentOutput>> deleteNodesFromSegment(DeleteNodesFromSegmentInput input) {
        DeleteNodesFromSegmentOutputBuilder deleteBuilder = new DeleteNodesFromSegmentOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getSegmentId()
                || null == input.getDetnetNodes()) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        for (String node : input.getDetnetNodes()) {
            DetnetNode detnetNode = topologyDataManager.getDetnetNode(input.getTopologyId(), node);
            LOG.info("detnetNode {}", detnetNode);
            if (null == detnetNode) {
                deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                        false, "node " + node + " is not exist!"));
                return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
            } else {
                DetnetNodeBuilder detnetNodeBuilder = new DetnetNodeBuilder(detnetNode);
                List<Segments> segmentsList = detnetNode.getSegments();
                Segments segments = new SegmentsBuilder()
                        .setSegmentId(input.getSegmentId())
                        .build();
                if (null != segmentsList && segmentsList.contains(segments)) {
                    //LOG.info("segment exist {}", segmentsList);
                    segmentsList.remove(segments);
                    if (!topologyDataManager.writeDetnetNode(input.getTopologyId(), node,
                            detnetNodeBuilder.build())) {
                        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                                false, "remove node " + node
                                        + " from segment " + input.getSegmentId() + " failed!"));
                        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
                    }
                }
            }
        }
        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteDetnetNodeOutput>> deleteDetnetNode(DeleteDetnetNodeInput input) {
        DeleteDetnetNodeOutputBuilder deleteBuilder = new DeleteDetnetNodeOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getNodeId()) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (null == topologyDataManager.getDetnetNode(input.getTopologyId(), input.getNodeId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "node is not exist!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (!topologyDataManager.deleteDetnetNode(input.getTopologyId(), input.getNodeId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                    false, "delete node form datastore failed!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }
        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteDetnetLinkOutput>> deleteDetnetLink(DeleteDetnetLinkInput input) {
        DeleteDetnetLinkOutputBuilder deleteBuilder = new DeleteDetnetLinkOutputBuilder();
        if (null == input || null == input.getTopologyId() || null == input.getLinkId()) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (null == topologyDataManager.getDetnetLink(input.getTopologyId(), input.getLinkId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "link is not exist!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }

        if (!topologyDataManager.deleteDetnetLink(input.getTopologyId(), input.getLinkId())) {
            deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                    false, "delete link form datastore failed!"));
            return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
        }
        deleteBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(deleteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<LoadTopologyIdOutput>> loadTopologyId() {
        LoadTopologyIdOutputBuilder loadTopologyIdOutputBuilder = new LoadTopologyIdOutputBuilder();
        List<Topology> topologyList = new ArrayList<>();
        DetnetNetworkTopology topology = topologyDataManager.getDetnetNetworkTopology();
        LOG.info("topology {}", topology);
        if (null == topology) {
            return RpcResultBuilder.success(loadTopologyIdOutputBuilder.build()).buildFuture();
        }

        List<DetnetTopology> detnetTopologyList = topology.getDetnetTopology();
        for (DetnetTopology detnetTopology : detnetTopologyList) {
            if (null != detnetTopology) {
                TopologyBuilder topologyBuilder = new TopologyBuilder();
                topologyBuilder.setTopologyId(detnetTopology.getTopologyId());
                topologyList.add(topologyBuilder.build());
            }
        }
        loadTopologyIdOutputBuilder.setTopology(topologyList);
        return RpcResultBuilder.success(loadTopologyIdOutputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<AddTopologyIdOutput>> addTopologyId(AddTopologyIdInput input) {
        AddTopologyIdOutputBuilder addBuilder = new AddTopologyIdOutputBuilder();
        if (null == input || null == input.getTopologyId()) {
            addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "Illegal argument!"));
            return RpcResultBuilder.success(addBuilder.build()).buildFuture();
        }
        if (null != topologyDataManager.getTopologyId(input.getTopologyId())) {
            addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(false, "topology id is exist!"));
            return RpcResultBuilder.success(addBuilder.build()).buildFuture();
        }
        DetnetTopology topology = new DetnetTopologyBuilder()
                .setTopologyId(input.getTopologyId())
                .setKey(new DetnetTopologyKey(input.getTopologyId()))
                .build();
        if (!topologyDataManager.writeTopologyId(input.getTopologyId(), topology)) {
            addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(
                    false, "write topology id to datastore failed!"));
            return RpcResultBuilder.success(addBuilder.build()).buildFuture();
        }
        addBuilder.setConfigureResult(RpcReturnUtil.getConfigResult(true, ""));
        return RpcResultBuilder.success(addBuilder.build()).buildFuture();
    }
}