/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.pce.impl.detnetpath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.detnet.common.util.NotificationProvider;
import org.opendaylight.detnet.pce.impl.provider.PcePathDb;
import org.opendaylight.detnet.pce.impl.topology.PathsRecordPerDomain;
import org.opendaylight.detnet.pce.impl.topology.TopologyProvider;
import org.opendaylight.detnet.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.PathUpdate;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.PathUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.create.path.input.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraint;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.EgressBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.egress.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceInstance {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceInstance.class);
    private Long streamId;
    private Integer domainId;
    private String topoId;
    private String ingressNodeId;
    private Short trafficClass;
    private PathConstraint pathConstraint;
    private LinkedHashMap<PathUnifyKey, SinglePath> paths = new LinkedHashMap<PathUnifyKey, SinglePath>();
    private LinkedList<DetnetLink> allPaths = new LinkedList<DetnetLink>();
    private boolean pathUpdateFlag = false;

    public ServiceInstance(CreatePathInput input) {
        this.streamId = input.getStreamId().longValue();
        this.ingressNodeId = input.getIngressNodeId();
        this.topoId = input.getTopoId() == null ? TopologyProvider.DEFAULT_TOPO_ID_STRING : input.getTopoId();
        this.domainId = input.getDomainId().intValue();
        this.trafficClass = input.getTrafficClass().shortValue();
        this.pathConstraint = input.getPathConstraint();
    }

    public void calcPath(CreatePathInput input, boolean isUpdate) {
       // boolean isFailRollback = true;
/*        String ingressNode = input.getIngressNodeId();
        Integer domain = input.getDomainId();
        Long streamId = input.getStreamId();
        Short trafficClass = input.getTrafficClass();
        PathConstraint pathConstraint = input.getPathConstraint();*/
        for (Egress egress : input.getEgress()) {
            SinglePath singlePath = new SinglePath(topoId, streamId,domainId, ingressNodeId,egress,trafficClass,
                    pathConstraint);
            singlePath.calcPath(true,allPaths);
            if ((singlePath.getPath() == null) || (singlePath.getPath().isEmpty())) {
                //do nothing

            } else {
                if (isUpdate) {
                    singlePath.writeDb();
                }
                PathUnifyKey pathKey = new PathUnifyKey(streamId,domainId,singlePath.getIngressNodeId(),
                        singlePath.getEgressNodeId());
                paths.put(pathKey,singlePath);
                PathsRecordPerDomain.getInstance().add(this.domainId, pathKey);
                if (singlePath.getPath() != null && !singlePath.getPath().isEmpty()) {
                    allPaths.addAll(singlePath.getPath());
                }

            }
        }
    }


    public void writeServiceInstanceToDB() {
        PcePathDb.getInstance().writeServiceInstance(this);
        for (SinglePath singlePath : paths.values()) {
            singlePath.writeDb();
        }
    }

    public SinglePath getPath(PathUnifyKey key) {
        return this.paths.get(key);
    }

    public boolean isPathEmpty() {
        return this.paths.isEmpty();
    }

    public Long getStreamId() {
        return this.streamId;
    }

    public String getIngressNodeId() {
        return this.ingressNodeId;
    }

    public Integer getDomainId() {
        return this.domainId;
    }

    public Short getTrafficClass() {
        return trafficClass;
    }

    public PathConstraint getPathConstraint() {
        return pathConstraint;
    }

    public List<SinglePath> getAllPath() {
        List<SinglePath> singlePaths = new ArrayList<SinglePath>();
        if (!paths.isEmpty()) {
            singlePaths.addAll(paths.values());
        }
        return singlePaths;
    }

    public void removePath(SinglePath path) {
        if (null == path) {
            return;
        }
        PathUnifyKey key = new PathUnifyKey(path.getStreamId(),path.getDomainId(),path.getIngressNodeId(),
                path.getEgressNodeId());

        paths.remove(key);
        if (path.getPath() != null) {
            for (DetnetLink link : path.getPath()) {
                allPaths.removeFirstOccurrence(link);
            }
        }
        path.removeDb();
    }

    public void removeAllPath() {
        for (SinglePath path : paths.values()) {
            path.destroy();
        }

        paths.clear();
    }

    public void removeServiceInstanceDB() {
        PcePathDb.getInstance().removeServiceInstance(this);
    }

    public void refreshPath() {
        //LOG.info("ServiceInstance path refresh: domainId-" + getDomainId() + ", streamId-" + getStreamId());
        allPaths.clear();
        for (SinglePath singlePath : paths.values()) {
            singlePath.refreshPath(allPaths);
            if (singlePath.getPath() != null && !singlePath.getPath().isEmpty()) {
                allPaths.addAll(singlePath.getPath());
            }
            if (singlePath.isPathUpdate()) {
                pathUpdateFlag = true;
            }
        }
        if (isServiceInstancePathUpdate()) {
            notifyPathChange();
        }
    }

    public void notifyPathChange() {
        PathUpdate notification = new PathUpdateBuilder()
                .setTopoId(TopologyProvider.DEFAULT_TOPO_ID_STRING)
                .setStreamId(streamId)
                .setIngressNodeId(ingressNodeId)
                .setDomainId(domainId)
                .setEgress(buildEgresses())
                .build();

       // LOG.info("notifyPathChange: domainId -" + getDomainId() + "streamId -" + getStreamId() + " path change! ");
        NotificationProvider.getInstance().notify(notification);
    }

    public List<org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress> buildEgresses() {
        List<org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress> egressList =
                new ArrayList<org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress>();
        for (SinglePath singlePath : paths.values()) {
            egressList.add(new EgressBuilder()
                    .setEgressNodeId(singlePath.getEgressNodeId())
                    .setPath(new PathBuilder()
                            .setPathLink(ComUtility.transform2PathLink(singlePath.getPath()))
                            .setPathMetric(singlePath.getPathMetric())
                            .setPathDelay(singlePath.getPathDelay())
                            .build())
                    .build());
        }
        return egressList;
    }

    public void destroy() {
        paths.clear();
    }

    public boolean isServiceInstancePathUpdate() {
        return pathUpdateFlag;
    }

    public String getTopoId() {
        return topoId;
    }



/*    public List<Link> getAllLinks() {
        Set<Link> allLinkSet = new HashSet<>();
        for (DetnetLink link : allPaths) {
            allLinkSet.add(new LinkBuilder().setLinkId(link.getLinkId()).build());
        }
        return new ArrayList<>(allLinkSet);
    }*/
}
