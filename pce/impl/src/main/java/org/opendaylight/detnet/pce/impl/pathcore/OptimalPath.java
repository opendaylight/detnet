/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.pce.impl.pathcore;

import edu.uci.ics.jung.graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class OptimalPath<V, E> implements ISpt<V, E> {
    private Graph<V, E> graph;
    private V sourceNode;
    private ISourceData<V, E> sd;
    protected ITransformer<E> edgeMeasure;
    private List<V> destNodeList;
    private ICalcStrategy<V, E> strategy;

    public OptimalPath(V sourceNode, Graph<V, E> graph, ICalcStrategy<V, E> strategy) {
        this.sourceNode = sourceNode;
        this.graph = graph;
        this.strategy = strategy;
    }

    public void setSourceData(ISourceData<V, E> sourceData) {
        this.sd = sourceData;
    }

    public void setEdgeMeasure(ITransformer<E> edgeMeasure) {
        this.edgeMeasure = edgeMeasure;
    }

    public void setDestNodeList(List<V> destNodeList) {
        this.destNodeList = destNodeList;
    }

    public void calcSpt() {
        ISourceData<V, E> sourceData = getSourceData();

        if ((graph == null) || (graph.getOutEdges(sourceNode) == null)) {
            return;
        }

        boolean onlyCalcSpecifiedNode = destNodeList != null;
        Map<V, V> destNodeMap = null;
        if (onlyCalcSpecifiedNode) {
            destNodeMap = genDestNodeMap(destNodeList);
        }

        V localNode = sourceNode;
        while (localNode != null) {
            for (E outEdge : graph.getOutEdges(localNode)) {
                for (V neighborNode : graph.getIncidentVertices(outEdge)) {

                    if (neighborNode.equals(localNode)) {
                        continue;
                    }

                    if (sourceData.isNodeAlreadyInPathList(neighborNode)) {
                        continue;
                    }

                    sourceData.add2TentList(localNode, neighborNode, outEdge);
                }
            }

            localNode = sourceData.moveOptimalTentNode2PathList();
            if (onlyCalcSpecifiedNode) {
                destNodeMap.remove(localNode);
                if (destNodeMap.isEmpty()) {
                    break;
                }
            }
        }
    }

    private Map<V, V> genDestNodeMap(List<V> destNodes) {
        Map<V, V> destNodeMap = new HashMap<V, V>();
        for (V node : destNodes) {
            destNodeMap.put(node, node);
        }

        return destNodeMap;
    }

    private ISourceData<V, E> getSourceData() {
        if (null == sd) {
            sd = new SourceDataImpl(sourceNode);
        }
        return sd;
    }

    @Override
    public Map<V, List<E>> getIncomingEdgeMap() {
        return sd.getIncomingEdgeMap();
    }

    @Override
    public Map<V, Number> getDistanceMap() {
        return sd.getDistance();
    }

    @Override
    public LinkedList<V> getDistanceOrderList() {
        return sd.getDistanceOrderList();
    }

    protected class SourceDataImpl implements ISourceData<V, E> {
        //protected V sourceNode;
        protected Map<V, List<E>> tentIncomingEdgesMap;
        protected Map<V, Number> tentDistanceMap;
        protected Map<V, Number> tentDelayMap;
        protected TreeMap<V, Number> tentDistanceTree;

        protected Map<V, Number> pathDistanceMap;
        protected Map<V, Number> pathDelayMap;
        protected Map<V, List<E>> pathIncomingEdgeMap;
        protected LinkedList<V> distanceOrderList;

        public SourceDataImpl(V sourceNode) {
            //this.sourceNode = sourceNode;

            tentIncomingEdgesMap = new HashMap<V, List<E>>();
            tentDistanceMap = new HashMap<V, Number>();
            tentDelayMap = new HashMap<V, Number>();
            tentDistanceTree = new TreeMap<V, Number>(comparator);

            pathDistanceMap = new HashMap<V, Number>();
            pathDistanceMap.put(sourceNode, 0);

            pathDelayMap = new HashMap<V, Number>();
            pathDelayMap.put(sourceNode,0);

            pathIncomingEdgeMap = new HashMap<V, List<E>>();
            pathIncomingEdgeMap.put(sourceNode, null);

            distanceOrderList = new LinkedList<V>();
            distanceOrderList.addLast(sourceNode);
        }

        @Override
        public V moveOptimalTentNode2PathList() {
            Entry<V, Number> optimalNodeDistanceEntry = strategy.getOptimalNodeInTentMap(tentDistanceTree);

            if (optimalNodeDistanceEntry == null) {
                return null;
            }
            V node = optimalNodeDistanceEntry.getKey();
            Number distance = optimalNodeDistanceEntry.getValue();
            removeTentDistance(node);
            Number pathDelay = tentDelayMap.get(node);
            removeTentDelay(node);

            pathDistanceMap.put(node, distance);
            pathDelayMap.put(node,pathDelay);

            List<E> incomingEdges = tentIncomingEdgesMap.get(node);
            tentIncomingEdgesMap.remove(node);

            pathIncomingEdgeMap.put(node, incomingEdges);
            distanceOrderList.addLast(node);

            return optimalNodeDistanceEntry.getKey();
        }

        private void removeTentDelay(V node) {
            tentDelayMap.remove(node);
        }

        @Override
        public void add2TentList(V localNode, V neighborNode, E incomingEdge) {
            Number locNodeDistance = pathDistanceMap.get(localNode);
            Number neighborNodeDistance = tentDistanceMap.get(neighborNode);
            long incomingEdgeMeasure = strategy.getEdgeMeasure(edgeMeasure, incomingEdge);
            if (neighborNodeDistance == null) {
                addTentDistance(neighborNode,
                        strategy.transEdgeMeasure(locNodeDistance.longValue(),incomingEdgeMeasure));

                List<E> edgeList = new ArrayList<E>();
                edgeList.add(incomingEdge);
                tentIncomingEdgesMap.put(neighborNode, edgeList);
            } else if (strategy.isCurNodeMoreOptimal(locNodeDistance.longValue(), incomingEdgeMeasure,
                    neighborNodeDistance.longValue())) {
                addTentDistance(neighborNode,
                        strategy.transEdgeMeasure(locNodeDistance.longValue(),incomingEdgeMeasure));

                List<E> edgeList = tentIncomingEdgesMap.get(neighborNode);
                edgeList.clear();
                edgeList.add(incomingEdge);
            } else if (strategy.transEdgeMeasure(locNodeDistance.longValue(), incomingEdgeMeasure)
                    == neighborNodeDistance.longValue()) {
                List<E> edgeList = tentIncomingEdgesMap.get(neighborNode);
                edgeList.add(incomingEdge);
            }
        }

        @Override
        public boolean isNodeAlreadyInPathList(V node) {
            return pathDistanceMap.get(node) != null;
        }

        @Override
        public long getDistance(V node) {
            return pathDistanceMap.get(node).longValue();
        }

        @Override
        public Map<V, Number> getDistance() {
            return pathDistanceMap;
        }

        @Override
        public Map<V, List<E>> getIncomingEdgeMap() {
            return pathIncomingEdgeMap;
        }


        @Override
        public LinkedList<V> getDistanceOrderList() {
            return distanceOrderList;
        }


        protected void addTentDistance(V node, Number metric) {
            if (tentDistanceMap.containsKey(node)) {
                removeTentDistance(node);
            }
            tentDistanceMap.put(node, metric);
            tentDistanceTree.put(node, metric);
        }

        protected void removeTentDistance(V node) {
            tentDistanceTree.remove(node);
            tentDistanceMap.remove(node);
        }


        protected Comparator<V> comparator = new Comparator<V>() {

            @Override
            public int compare(V o1, V o2) {
                Number n1 = tentDistanceMap.get(o1);
                Number n2 = tentDistanceMap.get(o2);

                if (n1.longValue() != n2.longValue()) {
                    return (n1.longValue() > n2.longValue()) ? 1 : -1;
                }

                return (o1.hashCode() > o2.hashCode()) ? 1 :
                        (o1.hashCode() == o2.hashCode()) ? 0 : -1;
            }

        };

    }

}
