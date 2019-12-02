/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.pce.impl.pathcore;
import edu.uci.ics.jung.graph.Graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.detnet.pce.impl.detnetpath.LspGetPath;
import org.opendaylight.detnet.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;

public class ContrainedOptimalPath extends OptimalPath<String,GraphLink> {
    private String tailNode;
    private List<GraphLink> excludePath;
    private Long bandwidth = 0L;
    private Long maxDelay = 0L;
    private Short trafficClass;


    public ContrainedOptimalPath(String bfirNodeId, String tailNode, Graph<String, GraphLink> topoGraph,
                                 ICalcStrategy<String, GraphLink> strategy) {
        super(bfirNodeId,topoGraph,strategy);
        this.tailNode = tailNode;
        setSourceData(new SourceDataExImpl(bfirNodeId));
    }

/*    public void setEdgeMetric(ITransformer<DetnetLink> edgeMetric) {
        this.edgeMetric = edgeMetric;
    }*/

    public void setExcludePath(List<GraphLink> excludePath) {
        this.excludePath = excludePath;
    }

    public LinkedList<GraphLink> calcCspf(String sourceNode) {
        calcSpt();
        Map<String, List<GraphLink>> incomingMap = getIncomingEdgeMap();
        if (!incomingMap.containsKey(tailNode)) {
            return new LinkedList<GraphLink>();
        }
        return LspGetPath.getPath(incomingMap, sourceNode, tailNode);
    }

    public void setBandwidth(Long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void setMaxDelay(Long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public void setTrafficClass(Short trafficClass) {
        this.trafficClass = trafficClass;
    }

    protected class SourceDataExImpl extends SourceDataImpl {

        public SourceDataExImpl(String sourceNode) {
            super(sourceNode);
        }

        public void add2TentList(String localNode, String neighborNode, GraphLink incomingEdge) {
            if (!hasEnoughBw(incomingEdge)) {
                return;
            }
            if (isLinkExcluded(incomingEdge)) {
                return;
            }
            if (!isDelayEligibal(localNode,incomingEdge)) {
                return;
            }

            super.add2TentList(localNode, neighborNode, incomingEdge);
            addBestEdge2TentDelay(localNode,neighborNode,incomingEdge);

        }

        private void addBestEdge2TentDelay(String localNode, String neighborNode, GraphLink incomingEdge) {
            if (maxDelay == 0L) {
                return;
            }
            long lessDelay = maxDelay;
            List<GraphLink> temList = new LinkedList<GraphLink>();
            List<GraphLink> edgeList = tentIncomingEdgesMap.get(neighborNode);
            if (edgeList.contains(incomingEdge)) {
                for (GraphLink edge : edgeList) {
                    long incomingEdgeDelay = ComUtility.getLinkDelay(edge,trafficClass);
                    String preNode = edge.getSource().getSourceNode();

                    long preLocNodeDelay = pathDelayMap.get(preNode).longValue();
                    if (incomingEdgeDelay + preLocNodeDelay < lessDelay) {
                        lessDelay = incomingEdgeDelay + preLocNodeDelay;
                        temList.clear();
                        temList.add(edge);
                    } else if (incomingEdgeDelay + preLocNodeDelay == lessDelay) {
                        temList.add(edge);
                    }
                }
                if (!temList.isEmpty()) {
                    edgeList.clear();
                    edgeList.addAll(temList);
                }
                if (edgeList.contains(incomingEdge)) {
                    addTentDelay(neighborNode, lessDelay);
                }
            }
        }

        private void addTentDelay(String node, long delay) {
            tentDelayMap.put(node,delay);
        }

        private boolean isDelayEligibal(String localNode, GraphLink incomingEdge) {
            if (maxDelay == 0) {
                return true;
            }
            long locNodeDelay = pathDelayMap.get(localNode).longValue();
            Long incomingDelay = ComUtility.getLinkDelay(incomingEdge,trafficClass);
            return locNodeDelay + incomingDelay <= maxDelay;
        }

        private boolean hasEnoughBw(GraphLink incomingEdge) {
            if (bandwidth == 0) {
                return true;
            }
            return incomingEdge.getAvailableDetnetBandwidth().shortValue() >= bandwidth;
        }

        private boolean isLinkExcluded(GraphLink link) {
            for (GraphLink excludeLink : excludePath) {
                if (excludeLink.getDest().equals(link.getDest())
                        && excludeLink.getSource().equals(link.getSource())) {
                    return true;
                }
            }
            return false;
        }
/*
        private void addBestEdge2TentDelay(NodeId localNode, NodeId neighborNode, Link incomingEdge) {
            if ((maxDelay == 4294967295L) && (reverseMaxDelay == 4294967295L )) {
                return;
            }

            //we choose positive least delay link to TentDelayMap
            long lessDelay = maxDelay;
            List<Link> temList = new LinkedList<>();
            List<Link> edgeList = tentIncomingEdgesMap.get(neighborNode);
            if (edgeList.contains(incomingEdge)) {
                for (Link edge : edgeList) {
                    long incomingEdgeDelay = ComUtility.getLinkDelay(edge);
                    NodeId preNode = edge.getSource().getSourceNode();

                    PathDelay preLocNodeDelay = (PathDelay) pathDelayMap.get(preNode);
                    long preNodePositiveDelay = preLocNodeDelay.getPostiveDelay();
                    if (incomingEdgeDelay + preNodePositiveDelay < lessDelay) {
                        lessDelay = incomingEdgeDelay + preNodePositiveDelay;
                        temList.clear();
                        temList.add(edge);
                    } else if (incomingEdgeDelay + preNodePositiveDelay == lessDelay) {
                        temList.add(edge);
                    }
                }
                if (!temList.isEmpty()) {
                    edgeList.clear();
                    edgeList.addAll(temList);
                }
                if (edgeList.contains(incomingEdge)) {
                    PathDelay locNodeDelay = (PathDelay) pathDelayMap.get(localNode);
                    long reverseLinkDelay = ComUtility.getReverseLinkDelay(graph, edgeList.get(0));
                    addTentDelay(neighborNode, lessDelay, locNodeDelay.getReverseDelay() + reverseLinkDelay);
                }
            }
        }

        private void addTentDelay(NodeId node, long delay, long reverseDelay) {
            PathDelay pathTentDelay = new PathDelay(delay,reverseDelay);

            tentDelayMap.put(node,pathTentDelay);
        }*/

    }
}
