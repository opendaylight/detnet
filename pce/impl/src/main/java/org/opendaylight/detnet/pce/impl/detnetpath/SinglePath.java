/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.detnetpath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.detnet.pce.impl.pathcore.MetricStrategy;
import org.opendaylight.detnet.pce.impl.pathcore.MetricTransformer;
import org.opendaylight.detnet.pce.impl.pathcore.MetricTransformerFactory;
import org.opendaylight.detnet.pce.impl.pathcore.PathCompator;
import org.opendaylight.detnet.pce.impl.pathcore.PathProvider;
import org.opendaylight.detnet.pce.impl.provider.PcePathDb;
import org.opendaylight.detnet.pce.impl.provider.PceResult;
import org.opendaylight.detnet.pce.impl.topology.PathsRecordPerDomain;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.create.path.input.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraint;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.DestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.graph.link.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SinglePath implements IPath {
    private static final Logger LOG = LoggerFactory.getLogger(SinglePath.class);
    private Long streamId;
    private String topoId;
    private String ingressNodeId;
    private String egressNodeId;
    private Short trafficClass;
    private PathConstraint pathConstraint;
    private LinkedList<DetnetLink> path;
    private long pathMetric;
    private Long pathDelay;
    private PathUnifyKey pathUnifyKey;
    private boolean pathUpdateFlag = false;


    public SinglePath(String topoId, Long streamId, Integer domainId, String ingressNode, Egress egressNode,
                      Short tafficClass, PathConstraint pathConstraint) {
        this.ingressNodeId = ingressNode;
        this.egressNodeId = egressNode.getEgressNodeId();
        this.topoId = topoId;
        this.streamId = streamId;
        this.trafficClass = tafficClass;
        this.pathConstraint = pathConstraint;
        this.pathUnifyKey = getPathUnifyKey(streamId,domainId, ingressNodeId, egressNodeId);
    }

    public PceResult calcPath(boolean failRollback, List<DetnetLink> tryToOverlapPath) {
        PathProvider<MetricTransformer> pathProvider = new PathProvider(ingressNodeId, pathUnifyKey, egressNodeId,
                topoId,new MetricStrategy<String, DetnetLink>(), new MetricTransformerFactory());

        pathProvider.setPathConstraint(pathConstraint);
        pathProvider.setOldPath(transToGraphLinks(path));
        pathProvider.setFailRollback(failRollback);
        pathProvider.setTrafficClass(trafficClass);
        if ((tryToOverlapPath != null) && (!tryToOverlapPath.isEmpty())) {
            pathProvider.addTryToOverlapPath(transToGraphLinks(tryToOverlapPath));
        }

        PceResult result = new PceResult();
        pathProvider.calcPath(result);
        if (failRollback && (result.isCalcFail())) {
            return result;
        }
        path = transToDetnetLinks(pathProvider.getPath());
        pathMetric = pathProvider.getPathMetric();
        pathDelay = pathProvider.getPathDelay();
        return result;
    }

    private LinkedList<DetnetLink> transToDetnetLinks(List<GraphLink> paths) {
        LinkedList<DetnetLink> detnetLinks = new LinkedList<DetnetLink>();
        for (GraphLink link : paths) {
            detnetLinks.add(new DetnetLinkBuilder()
                    .setLinkId(link.getLinkId())
                    .setLinkSource(new LinkSourceBuilder()
                            .setSourceNode(link.getSource().getSourceNode())
                            .setSourceTp(link.getSource().getSourceTp())
                            .build())
                    .setLinkDest(new LinkDestBuilder()
                            .setDestNode(link.getDest().getDestNode())
                            .setDestTp(link.getDest().getDestTp())
                            .build())
                    .build());
        }
        return detnetLinks;
    }

    private List<GraphLink> transToGraphLinks(List<DetnetLink> paths) {
        List<GraphLink> graphLinks = new ArrayList<GraphLink>();
        if (paths != null) {
            for (DetnetLink link : paths) {
                graphLinks.add(new GraphLinkBuilder()
                        .setLinkId(link.getLinkId())
                        .setSource(new SourceBuilder()
                                .setSourceNode(link.getLinkSource().getSourceNode())
                                .setSourceTp(link.getLinkSource().getSourceTp())
                                .build())
                        .setDest(new DestBuilder()
                                .setDestNode(link.getLinkDest().getDestNode())
                                .setDestTp(link.getLinkDest().getDestTp())
                                .build())
                        .build());
            }
        }
        return graphLinks;
    }

    public long getPathMetric() {
        return this.pathMetric;
    }

    public long getPathDelay() {
        return this.pathDelay;
    }

    public Integer getDomainId() {
        return this.pathUnifyKey.getDomainId();
    }


    @Override
    public void writeDb() {
        PcePathDb.getInstance().writePath(this);
    }


    @Override
    public void removeDb() {

        PcePathDb.getInstance().removePath(getDomainId(),getStreamId(), getEgressNodeId());

    }

    @Override
    public List<DetnetLink> getPath() {
        return path;
    }

    @Override
    public String getIngressNodeId() {
        return ingressNodeId;
    }

    @Override
    public void destroy() {
        PathsRecordPerDomain.getInstance().remove(getDomainId(), pathUnifyKey);
    }

    private static PathUnifyKey getPathUnifyKey(Long streamId, Integer domainId, String ingressNodeId,
                                                String egressNodeId) {
        return new PathUnifyKey(streamId,domainId,ingressNodeId, egressNodeId);
    }

    public String getEgressNodeId() {
        return egressNodeId;
    }

    public Long getStreamId() {
        return streamId;
    }

    public void refreshPath(List<DetnetLink> tryToOverlapPath) {
        //LOG.info("Single path refresh:" + pathUnifyKey.toString());
        LinkedList<DetnetLink> oldPath = new LinkedList<DetnetLink>(path);
        long oldMetric = pathMetric;

        calcPath(false,tryToOverlapPath);

        if (!PathCompator.isPathEqual(oldPath, path) || oldMetric != pathMetric) {
            writeDb();
            if (!PathCompator.isPathEqual(oldPath, path)) {
                this.pathUpdateFlag = true;
                //LOG.info(pathUnifyKey.toString() + " Path change: old path--"
                       // + oldPath + "; new path--" + path);
            }
        }
    }

    public boolean isPathUpdate() {
        return pathUpdateFlag;
    }
}
