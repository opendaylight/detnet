/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.topology;

import com.google.common.annotations.VisibleForTesting;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.detnet.pce.impl.provider.PcePathDb;
import org.opendaylight.detnet.pce.impl.provider.PcePathImpl;
import org.opendaylight.detnet.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DetnetBandwidthApiListener;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.LinkBandwidthChange;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.link.bandwidth.change.OldLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.PceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.DestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.TcDelay;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.TcDelayBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.Domains;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.domains.Segments;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Ltps;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.ltp.TrafficClasses;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyProvider implements DetnetBandwidthApiListener {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyProvider.class);
    public static final String DEFAULT_TOPO_ID_STRING = "detnet-topology";
    public static final Integer DOMAIN_ID = 1;
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker.RpcRegistration<PceApiService> pceService;
    private static TopologyProvider instance;
    protected Map<Integer, Graph<String, GraphLink>> topoGraphMap =
            new ConcurrentHashMap<Integer, Graph<String, GraphLink>>();
    protected Map<Integer, Graph<String, GraphLink>> topoGraphMapAllLink =
            new ConcurrentHashMap<Integer, Graph<String, GraphLink>>();
    private PcePathImpl pcePathImpl = PcePathImpl.getInstance();
    protected ExecutorService executor = Executors.newFixedThreadPool(1);

    public TopologyProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }


    @VisibleForTesting
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public static TopologyProvider getInstance() {
        return instance;
    }

    public void destroy() {
        topoGraphMap.clear();
        topoGraphMapAllLink.clear();

    }

    public void init() {
        //LOG.info("TopologyProvider Session Initiated");
        PcePathDb.getInstance().setDataBroker(dataBroker);
        pceService = rpcRegistry.addRpcImplementation(PceApiService.class,pcePathImpl);
        pcePathImpl.writeDbRoot();
        setPcePathImpl(pcePathImpl);
    }

    public void close() {
        //LOG.info("TopologyProvider Closed");
        destroy();
        pcePathImpl.destroy();

        if (pceService != null) {
            pceService.close();
        }
    }

    private Graph<String, GraphLink> transformTopo2Graph(DetnetTopology detnetTopology, Integer domainId) {
        Graph<String, GraphLink> graph = new SparseMultigraph<String, GraphLink>();
        if (detnetTopology == null) {
            return graph;
        }
        addTopo2Gragh(detnetTopology, graph,domainId,"topoGraphAllLink");

        return graph;
    }

    private void addTopo2Gragh(DetnetTopology detnetTopology, Graph<String, GraphLink> graph, Integer domainId,
                                String logMessage) {

        GraphLinkBuilder graphLinkBuilder = new GraphLinkBuilder();
        if (detnetTopology.getDetnetLink() != null) {
            for (DetnetLink link : detnetTopology.getDetnetLink()) {
                if (linkBelongsDomain(domainId, link, detnetTopology)) {
                    graphLinkBuilder.setLinkId(link.getLinkId());
                    graphLinkBuilder.setSource(new SourceBuilder()
                            .setSourceNode(link.getLinkSource().getSourceNode())
                            .setSourceTp(link.getLinkSource().getSourceTp())
                            .build());
                    graphLinkBuilder.setDest(new DestBuilder()
                            .setDestNode(link.getLinkDest().getDestNode())
                            .setDestTp(link.getLinkDest().getDestTp())
                            .build());
                    graphLinkBuilder.setMetric(link.getMetric());
                    graphLinkBuilder.setAvailableDetnetBandwidth(link.getAvailableDetnetBandwidth());
                    graphLinkBuilder.setTcDelay(buildTcDelay(detnetTopology.getDetnetNode(), link));

                    addLink2Gragh(graphLinkBuilder.build(), graph, logMessage,domainId);
                }
            }
        }
    }

    private List<TcDelay> buildTcDelay(List<DetnetNode> detnetNode, DetnetLink link) {
        List<TcDelay> tcDelayList = new ArrayList<TcDelay>();
        for (DetnetNode node : detnetNode) {
            if (link.getLinkSource().getSourceNode().equals(node.getNodeId())) {
                for (Ltps ltp : node.getLtps()) {
                    if (ltp.getTpId().equals(link.getLinkSource().getSourceTp())) {
                        for (TrafficClasses trafficClass : ltp.getTrafficClasses()) {
                            tcDelayList.add(new TcDelayBuilder()
                                    .setTrafficClass(trafficClass.getTcIndex())
                                    .setDelay(link.getLinkDelay().longValue() + node.getProcessDelay().longValue()
                                            + trafficClass.getMaximumQueueDelay().longValue())
                                    .build());
                        }
                    }
                }
            }
        }
        return tcDelayList;
    }

    protected void addLink2Gragh(GraphLink link, Graph<String, GraphLink> graph, String logMessage,Integer domainId) {
        String srcId = link.getSource().getSourceNode();
        String destId = link.getDest().getDestNode();

        if (srcId.equals(destId)) {
            return;
        }

        // Make sure the vertex are there before adding the edge
        graph.addVertex(srcId);
        graph.addVertex(destId);

        // add the link between
        if (!graph.containsEdge(link)) {
            graph.addEdge(link, srcId, destId, EdgeType.DIRECTED);
        }
        linkInfo2Log("topo:addlink to domain " + domainId + logMessage , link);
    }

/*
    private void removeLinkFromGraphAllLink(GraphLink link,Graph<String, GraphLink> topoGraphAllLink) {
        if (!topoGraphAllLink.containsEdge(link)) {
            return;
        }

        topoGraphAllLink.removeEdge(link);
        String srcId = link.getSource().getSourceNode();
        String destId = link.getDest().getDestNode();
        if (0 == topoGraphAllLink.getNeighborCount(srcId)) {
            topoGraphAllLink.removeVertex(srcId);
        }
        if (0 == topoGraphAllLink.getNeighborCount(destId)) {
            topoGraphAllLink.removeVertex(destId);
        }
        linkInfo2Log("topo:removelink from topoGraphAllLink", link);
    }
/*
    protected List<DetnetLink> getLinks(Integer domainId) {
        List<DetnetLink> allDetnetLinks = PcePathDb.getInstance().getAllDetnetLinks();
        List<DetnetLink> bierLinks = new ArrayList<>();
        if (allDetnetLinks != null) {
            for (DetnetLink bierLink : allDetnetLinks) {
                if (linkBelongsDomain(domainId,bierLink, detnetTopology)) {
                    bierLinks.add(bierLink);
                }
            }
        }
        return bierLinks;
    }*/


    private DetnetTopology getTopology(String topoId) {
        return PcePathDb.getInstance().getDetnetTopology(topoId);
    }

    private boolean linkBelongsDomain(Integer domainId, DetnetLink detnetLink, DetnetTopology detnetTopology) {
        String srcNode = detnetLink.getLinkSource().getSourceNode();
        String destNode = detnetLink.getLinkDest().getDestNode();
        return nodeBelongsDomain(domainId,srcNode,detnetTopology)
                && nodeBelongsDomain(domainId,destNode,detnetTopology);
    }

    private boolean nodeBelongsDomain(Integer domainId, String node, DetnetTopology detnetTopology) {
        List<Integer> segmentIdList = new ArrayList<Integer>();
        for (Domains domain : detnetTopology.getDomains()) {
            if (domain.getDomainId().equals(Uint16.valueOf(domainId))) {
                for (Segments segment :domain.getSegments()) {
                    segmentIdList.add(segment.getSegmentId().intValue());
                }
            }
        }
        for (DetnetNode detnetNode : detnetTopology.getDetnetNode()) {
            if (detnetNode.getNodeId().equals(node)) {
                for (org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Segments
                        segment : detnetNode.getSegments()) {
                    if (segmentIdList.contains(segment.getSegmentId().intValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setPcePathImpl(PcePathImpl pcePathImpl) {
        this.pcePathImpl = pcePathImpl;
    }

    public Graph<String, GraphLink> getTopoGraph(String topoId,Integer domainId) {

        Graph<String, GraphLink> topoGraph = topoGraphMap.get(domainId);
        if (topoGraph == null) {
            synchronized (this) {
                topoGraph = topoGraphMap.get(domainId);
                if (topoGraph == null) {
                    topoGraph = newTopoGraph(topoId,domainId);
                    if (topoGraph == null) {
                       // LOG.error("getTopoGraph:topoGraph is null!");
                        return null;
                    }
                }
            }
        }
        //LOG.debug("getTopoGraph: domain-" + domainId + " graph-" + topoGraph);
        return topoGraph;
    }

    public static void setInstance(TopologyProvider provider) {
        instance = provider;
    }

    private Graph<String, GraphLink> newTopoGraph(String topoId,Integer domainId) {
        //List<DetnetLink> links = getLinks(domainId);
        DetnetTopology detnetTopology = getTopology(topoId);
        Graph<String, GraphLink> topoGraphAllLink = transformTopo2Graph(detnetTopology,domainId);
        if (topoGraphAllLink == null) {
            return null;
        }
        topoGraphMapAllLink.put(domainId, topoGraphAllLink);

        Graph<String, GraphLink> topoGraph = newTopoGraphBidirect(topoGraphAllLink,domainId);
        if (topoGraph == null) {
            return null;
        }
        topoGraphMap.put(domainId, topoGraph);

        return topoGraph;
    }


    private Graph<String, GraphLink> newTopoGraphBidirect(Graph<String, GraphLink> topoGraphAllLink,Integer domainId) {
        Graph<String, GraphLink> graphNew = new SparseMultigraph<String, GraphLink>();
        if (topoGraphAllLink == null) {
            return graphNew;
        }

        List<GraphLink> linksReverse;

        for (String localNode : topoGraphAllLink.getVertices()) {
            for (GraphLink outEdge : topoGraphAllLink.getOutEdges(localNode)) {
                if (graphNew.containsEdge(outEdge)) {
                    continue;
                }
                linksReverse = ComUtility.getReverseLink(topoGraphAllLink, outEdge);
                if ((linksReverse != null) && (!linksReverse.isEmpty())) {
                    addLink2Gragh(outEdge, graphNew, "topoGraph",domainId);
                    for (GraphLink link : linksReverse) {
                        addLink2Gragh(link, graphNew, "topoGraph",domainId);
                    }
                }
            }
        }

        return graphNew;
    }

    protected void addLink(GraphLink link, Graph<String, GraphLink> topoGraphAllLink,
                           Graph<String, GraphLink> topoGraph,Integer domain) {

        addLink2Gragh(link, topoGraphAllLink, " ,topoGraphAllLink,link: ",domain);

        List<GraphLink> linkReverse = ComUtility.getReverseLink(topoGraphAllLink, link);
        if ((linkReverse == null) || (linkReverse.isEmpty())) {
            //LOG.info("reverse link not exist,link=" + ComUtility.getLinkString(link));
            return;
        }
        addLink2Graph(link, linkReverse, topoGraph, " ,topoGraph,link: ",domain);
        //pcePathImpl.refreshAllPath(domainId);
    }

    protected void removeLink(GraphLink link, Graph<String, GraphLink> topoGraphAllLink,
                              Graph<String, GraphLink> topoGraph, Integer domain) {
        removeLinkFromGraph(link, topoGraphAllLink, "topoGraphAllLink");

        if (topoGraph.containsEdge(link)) {
            removeLinkFromGraph(link, topoGraph, "topoGraph");
            List<GraphLink> otherLinks = ComUtility.getOtherLink(topoGraph, link);
            if ((null == otherLinks) || (otherLinks.isEmpty())) {

                List<GraphLink> linksReverse = ComUtility.getReverseLink(topoGraph, link);
                if ((linksReverse == null) || (linksReverse.isEmpty())) {
                    //LOG.error("no reverse link!", link.toString());
                } else {
                    removeLinksFromGraph(linksReverse, topoGraph);
                }
            }

            //pcePathImpl.refreshAllPath(domainId);
        }
    }

    private void addLink2Graph(GraphLink link, List<GraphLink> linksReverse,
                               Graph<String, GraphLink> topoGraph, String logMessage,Integer domainId) {
        addLink2Gragh(link, topoGraph,logMessage,domainId);
        for (GraphLink graphLink : linksReverse) {
            addLink2Gragh(graphLink, topoGraph, logMessage,domainId);
        }
    }

    private void removeLinkFromGraph(GraphLink link, Graph<String, GraphLink> graph, String logMessage) {
        if (!graph.containsEdge(link)) {
            return;
        }

        graph.removeEdge(link);

        String srcId = link.getSource().getSourceNode();
        String destId = link.getDest().getDestNode();

        if (!graph.containsVertex(srcId)) {
            //LOG.error("srcId does not exist!", link.toString());
            return;
        } else {
            if (0 == graph.getNeighborCount(srcId)) {
                graph.removeVertex(srcId);
            }
        }
        if (!graph.containsVertex(destId)) {
            //LOG.error("destId does not exist!", link.toString());
            return;
        } else {
            if (0 == graph.getNeighborCount(destId)) {
                graph.removeVertex(destId);
            }
        }
        linkInfo2Log("topo:removelink from " + logMessage , link);
    }

    private void removeLinksFromGraph(List<GraphLink> links, Graph<String, GraphLink> topoGraph) {
        if ((links == null) || (links.isEmpty())) {
            return;
        }
        for (GraphLink link : links) {
            removeLinkFromGraph(link, topoGraph, "topoGraph");
        }
    }



    private void linkInfo2Log(String headInfo, GraphLink link) {
        //LOG.info(headInfo + " {" + link + "} ");
    }

    @Override
    public void onLinkBandwidthChange(LinkBandwidthChange notification) {
        OldLink oldLink = notification.getOldLink();
        Long newAvailableDetnetBandwidth = notification.getNewAvailableBandwidth().longValue();
        changeLink(oldLink, newAvailableDetnetBandwidth,topoGraphMapAllLink,"topoGraphAllLink");
        changeLink(oldLink,newAvailableDetnetBandwidth,topoGraphMap,"topoGraph");

    }

    private void changeLink(OldLink oldLink, Long newAvailableDetnetBandwidth,
                            Map<Integer, Graph<String, GraphLink>> graphMap, String logMessage) {
        for (Graph<String,GraphLink> graphAllLink :graphMap.values()) {
            Collection<GraphLink> outEdges = graphAllLink.getOutEdges(oldLink.getLinkSource().getSourceNode());
            List<GraphLink> outLinks = new ArrayList<GraphLink>(outEdges);
            if (oldLink != null && !outLinks.isEmpty()) {
                for (GraphLink link : outLinks) {
                    if (link.getLinkId().equals(oldLink.getLinkId())) {
                        GraphLinkBuilder newLinkBuilder = new GraphLinkBuilder(link);
                        changeLinkFromGraph(link,
                                newLinkBuilder.setAvailableDetnetBandwidth(newAvailableDetnetBandwidth).build(),
                                graphAllLink,logMessage);
                    }
                }
            }
        }
    }

    private void changeLinkFromGraph(GraphLink oldLink, GraphLink newLink,
                                            Graph<String, GraphLink> graph,String logMessage) {
        if (!graph.containsEdge(oldLink)) {
            return;
        }
        graph.removeEdge(oldLink);
        String srcId = newLink.getSource().getSourceNode();
        String destId = newLink.getDest().getDestNode();
        graph.addEdge(newLink, srcId, destId, EdgeType.DIRECTED);
        linkInfo2Log("topo:changelink to " + logMessage , newLink);
    }


}
