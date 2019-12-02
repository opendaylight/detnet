/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.pce.impl.provider.PcePathImpl;
import org.opendaylight.detnet.pce.impl.topology.TopologyProvider;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.create.path.input.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.create.path.input.EgressBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.egress.Path;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class Utils extends AbstractConcurrentDataBrokerTest {
    public static void checkPath(List<PathLink> actualPath, String node1, String node2, String node3) {
        assertEquals(2, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
        assertEquals(actualPath.get(1).getLinkSource().getSourceNode(), node2);
        assertEquals(actualPath.get(1).getLinkDest().getDestNode(), node3);
    }

    public static void checkPath(List<PathLink> actualPath, String node1, String node2) {
        assertEquals(1, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
    }

    public static void checkPath(List<PathLink> actualPath, String node1, String node2, String node3, String node4) {
        assertEquals(3, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
        assertEquals(actualPath.get(1).getLinkSource().getSourceNode(), node2);
        assertEquals(actualPath.get(1).getLinkDest().getDestNode(), node3);
        assertEquals(actualPath.get(2).getLinkSource().getSourceNode(), node3);
        assertEquals(actualPath.get(2).getLinkDest().getDestNode(), node4);
    }

    public static void checkPath(List<PathLink> actualPath, String node1, String node2, String node3,
                                 String node4, String node5) {
        assertEquals(4, actualPath.size());
        assertEquals(actualPath.get(0).getLinkSource().getSourceNode(), node1);
        assertEquals(actualPath.get(0).getLinkDest().getDestNode(), node2);
        assertEquals(actualPath.get(1).getLinkSource().getSourceNode(), node2);
        assertEquals(actualPath.get(1).getLinkDest().getDestNode(), node3);
        assertEquals(actualPath.get(2).getLinkSource().getSourceNode(), node3);
        assertEquals(actualPath.get(2).getLinkDest().getDestNode(), node4);
        assertEquals(actualPath.get(3).getLinkSource().getSourceNode(), node4);
        assertEquals(actualPath.get(3).getLinkDest().getDestNode(), node5);
    }

    public static void checkPathNull(List<PathLink> actualPath) {
        assertEquals(actualPath.size(),0);
    }



    public static void writeLinkToDB(DetnetLink link, DataBroker dataBroker) {
        InstanceIdentifier<DetnetLink> path = InstanceIdentifier.builder(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(TopologyProvider.DEFAULT_TOPO_ID_STRING))
                .child(DetnetLink.class, new DetnetLinkKey(link.getLinkId()))
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT,dataBroker,path,link);
    }

    public static void writeLinksToDB(List<DetnetLink> links, DataBroker dataBroker) {
        for (DetnetLink link:links) {
            writeLinkToDB(link,dataBroker);
        }
    }

    public static void deleteLinkInDB(DetnetLink link) {
        InstanceIdentifier<DetnetLink> path = InstanceIdentifier.builder(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(TopologyProvider.DEFAULT_TOPO_ID_STRING))
                .child(DetnetLink.class, new DetnetLinkKey(link.getLinkId()))
                .build();
        //DbProvider.getInstance().deleteData(LogicalDatastoreType.CONFIGURATION,path);
    }

    public static List<Egress> build1EgressInfo(String nodeId1) {
        List<Egress> egressList = new ArrayList<Egress>();
        egressList.add(new EgressBuilder().setEgressNodeId(nodeId1).build());
        return egressList;
    }

    public static List<Egress> build2EgressInfo(String nodeId1, String nodeId2) {
        List<Egress> egressList = new ArrayList<Egress>();
        egressList.add(new EgressBuilder().setEgressNodeId(nodeId1).build());
        egressList.add(new EgressBuilder().setEgressNodeId(nodeId2).build());
        return egressList;
    }

    public static List<Egress> build3EgressInfo(String nodeId1, String nodeId2, String nodeId3) {
        List<Egress> bferList = new ArrayList<Egress>();
        bferList.add(new EgressBuilder().setEgressNodeId(nodeId1).build());
        bferList.add(new EgressBuilder().setEgressNodeId(nodeId2).build());
        bferList.add(new EgressBuilder().setEgressNodeId(nodeId3).build());
        return bferList;
    }

    public static void assertPathData(Path path, Path pathData) {
        assertEquals(path.getPathLink().size(),pathData.getPathLink().size());
        assertEquals(path.getPathMetric(),pathData.getPathMetric());
        assertEquals(path.getPathDelay(),pathData.getPathDelay());
        for (PathLink pathLink : pathData.getPathLink()) {
            PathLink linkTemp = new PathLinkBuilder(pathLink).build();
            path.getPathLink().contains(linkTemp);
        }
    }

    public static void buildPathInstanceForQueryAndRecovery(PcePathImpl pcePathProvider)
            throws ExecutionException, InterruptedException {

        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setEgress(Utils.build3EgressInfo("node2","node4","node5"))
                .setTrafficClass((short) 1)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);
        assertTrue(output.get().isSuccessful());

        input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node2")
                .setStreamId(2222L)
                .setEgress(Utils.build2EgressInfo("node1","node5"))
                .setTrafficClass((short) 3)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        output = pcePathProvider.createPath(input);
        assertTrue(output.get().isSuccessful());

        input = new CreatePathInputBuilder()
                .setDomainId(2)
                .setIngressNodeId("node4")
                .setStreamId(3333L)
                .setEgress(Utils.build2EgressInfo("node2","node6"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        output = pcePathProvider.createPath(input);
        assertTrue(output.get().isSuccessful());
    }
}
