/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.provider;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.pce.impl.detnetpath.ServiceInstance;
import org.opendaylight.detnet.pce.impl.detnetpath.SinglePath;
import org.opendaylight.detnet.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.PathData;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.PathDataBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.EgressBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.EgressKey;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstance;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstanceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.egress.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PcePathDb {
    private DataBroker dataBroker;
    private static PcePathDb instance = new PcePathDb();

    public PcePathDb() {
    }

    public static PcePathDb getInstance() {
        return instance;
    }

    public void pathDataWriteDbRoot() {
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker, buildPathDataDbRootPath(),
                new PathDataBuilder().build(),LogicalDatastoreType.OPERATIONAL);
    }


    public InstanceIdentifier<PathData> buildPathDataDbRootPath() {
        return InstanceIdentifier.create(PathData.class);
    }

    public void writeServiceInstance(ServiceInstance serviceInstance) {
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,
                buildPathInstancePath(serviceInstance.getDomainId(),serviceInstance.getStreamId()),
                serviceInstanceCreator(serviceInstance),LogicalDatastoreType.OPERATIONAL);
    }

    public PathInstance readPathInstance(Integer domainId, Long streamId) {
        return DataOperator.readData(dataBroker,buildPathInstancePath(domainId,streamId),
                LogicalDatastoreType.OPERATIONAL);
    }

    private InstanceIdentifier<PathInstance> buildPathInstancePath(Integer domainId, Long streamId) {
        return InstanceIdentifier.create(PathData.class)
                .child(PathInstance.class,new PathInstanceKey(domainId,streamId));
    }

    private PathInstance serviceInstanceCreator(ServiceInstance serviceInstance) {
        return new PathInstanceBuilder()
                .setStreamId(serviceInstance.getStreamId())
                .setDomainId(serviceInstance.getDomainId())
                .setIngressNodeId(serviceInstance.getIngressNodeId())
                .setTrafficClass(serviceInstance.getTrafficClass())
                .setPathConstraint(serviceInstance.getPathConstraint())
                .build();
    }

    public static InstanceIdentifier<Egress> buildPathDbPath(Integer domain, Long streamId, String egressNode) {
        return InstanceIdentifier.create(PathData.class)
                .child(PathInstance.class,new PathInstanceKey(domain,streamId))
                .child(Egress.class,new EgressKey(egressNode));
    }

    public void writePath(SinglePath singlePath) {
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildPathDbPath(singlePath.getDomainId(),
                singlePath.getStreamId(),singlePath.getEgressNodeId()), pathsCreator(singlePath),
                LogicalDatastoreType.OPERATIONAL);
    }


    private Egress pathsCreator(SinglePath singlePath) {
        return new EgressBuilder()
                .setEgressNodeId(singlePath.getEgressNodeId())
                .setPath(new PathBuilder()
                        .setPathLink(ComUtility.transform2PathLink(singlePath.getPath()))
                        .setPathMetric(singlePath.getPathMetric())
                        .setPathDelay(singlePath.getPathDelay())
                        .build())
                .build();
    }

    public void removeServiceInstance(ServiceInstance serviceInstance) {
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,
                buildPathInstancePath(serviceInstance.getDomainId(),serviceInstance.getStreamId()),null,
                LogicalDatastoreType.OPERATIONAL);
    }

    public void removePath(Integer domainId, Long streamId, String egressNode) {
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,
                buildPathDbPath(domainId,streamId,egressNode),null,LogicalDatastoreType.OPERATIONAL);
    }

    public DetnetTopology getDetnetTopology(String topoId) {
        InstanceIdentifier<DetnetTopology> path = InstanceIdentifier.builder(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(topoId))
                .build();

        return DataOperator.readData(dataBroker,path, LogicalDatastoreType.CONFIGURATION);
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }
}
