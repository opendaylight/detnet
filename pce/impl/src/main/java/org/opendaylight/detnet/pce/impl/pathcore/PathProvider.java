/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.pce.impl.pathcore;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.detnet.pce.impl.detnetpath.LspGetPath;
import org.opendaylight.detnet.pce.impl.detnetpath.PathUnifyKey;
import org.opendaylight.detnet.pce.impl.provider.PceResult;
import org.opendaylight.detnet.pce.impl.topology.TopologyProvider;
import org.opendaylight.detnet.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PathProvider<T extends ITransformer<GraphLink>> {
    private static final Logger LOG = LoggerFactory.getLogger(PathProvider.class);
    private String ingressNodeId;
    private String egressNodeId;
    private String topoId;
    private Short trafficClass;
    private LinkedList<GraphLink> path;
    private List<GraphLink> oldPath;
    private List<GraphLink> tryToOverlapLinks = Lists.newArrayList();
    private List<GraphLink> excludePaths = new ArrayList<GraphLink>();
    private PathUnifyKey pathUnifyKey;
    private long pathMetric;
    private long pathDelay;
    private ICalcStrategy<String, GraphLink> strategy;
    private ITransformerFactory factory;
    private boolean failRollback = false;
    private PathConstraint pathConstraint;

    public <F extends ITransformerFactory<T>> PathProvider(String ingressNodeId, PathUnifyKey pathUnifyKey,
                                                           String egressNodeId, String topoId,
                                                           ICalcStrategy<String, GraphLink> strategy, F factory) {
        this.ingressNodeId = ingressNodeId;
        this.pathUnifyKey = pathUnifyKey;
        this.egressNodeId = egressNodeId;
        this.topoId = topoId;
        this.strategy = strategy;
        this.factory = factory;
    }

    public List<GraphLink> getPath() {
        return this.path;
    }

    public long getPathMetric() {
        return this.pathMetric;
    }

    public long getPathDelay() {
        return this.pathDelay;
    }

    public void clearTryToOverlapLinks() {
        tryToOverlapLinks.clear();
    }


    public void setOldPath(List<GraphLink> oldPath) {
        this.oldPath = oldPath;
    }

    public void addTryToOverlapPath(List<GraphLink> avoidPath) {
        if (avoidPath != null) {
            tryToOverlapLinks.addAll(avoidPath);
        }
    }


    public void setFailRollback(boolean failRollback) {
        this.failRollback = failRollback;
    }

    public void calcPath(PceResult result) {

        calcPathProcess();

        if (failRollback && ((path == null) || (path.isEmpty()))) {
            result.setCalcFail(true);
            return;
        }
    }

    private void calcPathProcess() {
        if (pathConstraint == null && excludePaths.isEmpty()) {
            calcShortestPath();
            //recordPerPort();
        } else {
            calcConstrainedPath();
        }
    }

    private void calcConstrainedPath() {
        List<String> destNodeList = new ArrayList<>();
        destNodeList.add(egressNodeId);
        ContrainedOptimalPath cspf = new ContrainedOptimalPath(ingressNodeId, egressNodeId,
                TopologyProvider.getInstance().getTopoGraph(topoId,pathUnifyKey.getDomainId()), strategy);
        //cspf.setEdgeMetric(getMetricTransform());
        cspf.setExcludePath(excludePaths);
        cspf.setDestNodeList(destNodeList);
        cspf.setTrafficClass(trafficClass);
        if (pathConstraint != null) {
            cspf.setBandwidth(pathConstraint.getBandwidth().longValue());
            cspf.setMaxDelay(pathConstraint.getMaxDelay().longValue());
        }
/*
        Map<String, List<DetnetLink>> incomingMap = cspf.getIncomingEdgeMap();
        if (!incomingMap.containsKey(egressNodeId)) {
            return;
        }
*/

        path = cspf.calcCspf(ingressNodeId);
        calcPathMetric();
        calcPathDelay();
    }

/*    private List<DetnetLink> transformPath(List<GraphLink> graphLinks) {
        //TODo
        return null;
    }*/


    private void calcShortestPath() {
        Map<String, List<GraphLink>> incomingMap = calcIncomingMap();
        if (!incomingMap.containsKey(egressNodeId)) {
            return;
        }

        path = LspGetPath.getPath(incomingMap, ingressNodeId, egressNodeId);
        calcPathMetric();
    }

    private Map<String, List<GraphLink>> calcIncomingMap() {
        List<String> destNodeList = new ArrayList<String>();
        destNodeList.add(egressNodeId);

        OptimalPath<String, GraphLink> sp = new OptimalPath(
                ingressNodeId, TopologyProvider.getInstance().getTopoGraph(topoId,pathUnifyKey.getDomainId()),strategy);
        sp.setDestNodeList(destNodeList);
        sp.setEdgeMeasure(getMetricTransform());

        sp.calcSpt();
        return sp.getIncomingEdgeMap();
    }


    private void calcPathMetric() {
        pathMetric = 0;
        if ((path == null) || (path.isEmpty())) {
            return;
        }
        for (GraphLink link : path) {
            pathMetric += ComUtility.getLinkMetric(link);
        }
    }

    private void calcPathDelay() {
        pathDelay = 0;
        if ((path == null) || (path.isEmpty())) {
            return;
        }
        for (GraphLink link : path) {
            pathDelay += ComUtility.getLinkDelay(link,trafficClass);
        }
    }

    private ITransformer<GraphLink> getMetricTransform() {
        return (T) factory.create(tryToOverlapLinks);
    }

    public void setExcludePath(List<GraphLink> excludeLinks) {
        this.excludePaths = excludeLinks;
    }

    public void setPathConstraint(PathConstraint pathConstraint) {
        this.pathConstraint = pathConstraint;
    }

    public void setTrafficClass(Short trafficClass) {
        this.trafficClass = trafficClass;
    }
}
