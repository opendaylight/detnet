/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.util;

import edu.uci.ics.jung.graph.Graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.TcDelay;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ComUtility {
    public static final long DEFAULT_METRIC = 0x0a;
    public static final long DEFAULT_DELAY = 0x64;
    private static final Logger LOG = LoggerFactory.getLogger(ComUtility.class);

    private ComUtility() {
    }

    public static Long getLinkMetric(GraphLink link) {

        if (link.getMetric() == null) {
            return DEFAULT_METRIC;
        } else {
            return link.getMetric();
        }
    }

    public static Long getLinkDelay(GraphLink link, Short trafficClass) {
        for (TcDelay tcDelay : link.getTcDelay()) {
            if (tcDelay.getTrafficClass().equals(trafficClass)) {
                return tcDelay.getDelay();
            }
        }
        return DEFAULT_DELAY;
    }

    public static String getLinkString(GraphLink link) {
        return link.getSource().getSourceNode() + ":" + link.getSource().getSourceTp()
                + "-----" + link.getDest().getDestNode() + ":" + link.getDest().getDestTp();
    }


    public static List<GraphLink> getLinkInGraph(Graph<String, GraphLink> graph, String sourceNode, String sourceTp,
                                            String destNode, String destTp) {
        List<GraphLink> linksFound = new LinkedList<>();
        if ((!graph.containsVertex(sourceNode))
                || (!graph.containsVertex(destNode))) {
            LOG.error("source:" + sourceNode.toString() + " dest:" + destNode.toString());
            return linksFound;
        }

        Collection<GraphLink> links = graph.findEdgeSet(sourceNode, destNode);
        if ((links == null) || (links.isEmpty())) {
            return linksFound;
        }

        for (GraphLink linkTemp : links) {
            if (linkTemp.getDest().getDestTp().equals(destTp)
                    && (linkTemp.getSource().getSourceTp().equals(sourceTp))) {
                linksFound.add(linkTemp);
            }
        }

        return linksFound;
    }

    public static List<GraphLink> getOtherLink(Graph<String, GraphLink> graph, GraphLink link) {
        List<GraphLink> linksList = new LinkedList<>();
        if ((!graph.containsVertex(link.getDest().getDestNode()))
                || (!graph.containsVertex(link.getSource().getSourceNode()))) {
            LOG.error(link.toString());
            return linksList;
        }

        Collection<GraphLink> links = graph.findEdgeSet(link.getSource().getSourceNode(),
                link.getDest().getDestNode());
        if ((links == null) || (links.isEmpty())) {
            return linksList;
        }

        for (GraphLink otherLink : links) {
            if (otherLink.getDest().getDestTp().equals(link.getDest().getDestTp())
                    && (otherLink.getSource().getSourceTp().equals(link.getSource().getSourceTp()))) {
                linksList.add(otherLink);
            }
        }

        return linksList;
    }

    public static List<GraphLink> getReverseLink(Graph<String, GraphLink> graph, GraphLink link) {
        List<GraphLink> reverseLinks = getLinkInGraph(graph,
                link.getDest().getDestNode(),
                link.getDest().getDestTp(),
                link.getSource().getSourceNode(),
                link.getSource().getSourceTp());

        if ((reverseLinks != null) && (!reverseLinks.isEmpty())) {
            return reverseLinks;
        }

        Collection<GraphLink> links = graph.findEdgeSet(link.getDest().getDestNode(),
                link.getSource().getSourceNode());
        if ((links == null) || (links.isEmpty())) {
            return null;
        }
        return null;
    }


    public static List<PathLink> transform2PathLink(List<DetnetLink> links) {
        LinkedList<PathLink> path = new LinkedList<>();
        if (links == null) {
            return path;
        }

        for (DetnetLink link : links) {
            path.addLast(new PathLinkBuilder(link).build());
        }

        return path;
    }
}
