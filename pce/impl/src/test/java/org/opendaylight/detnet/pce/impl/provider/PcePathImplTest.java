/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.pce.impl.detnetpath.ServiceInstance;
import org.opendaylight.detnet.pce.impl.topology.PathsRecordPerDomain;
import org.opendaylight.detnet.pce.impl.topology.TopologyProvider;

import org.opendaylight.detnet.pce.impl.util.TopoMockUtils;
import org.opendaylight.detnet.pce.impl.util.Utils;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstance;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstanceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class PcePathImplTest extends AbstractConcurrentDataBrokerTest {
    private DataBroker dataBroker;
    PcePathImpl pcePathProvider;

    TopologyProvider topologyProvider;

    @Before
    public void setUp() throws Exception {
        dataBroker = getDataBroker();
        PcePathDb.getInstance().setDataBroker(dataBroker);
        pcePathProvider = PcePathImpl.getInstance();
        PcePathDb.getInstance().pathDataWriteDbRoot();

        topologyProvider = new TopologyProvider(dataBroker,null);
        topologyProvider.setExecutor(MoreExecutors.newDirectExecutorService());
        topologyProvider.setPcePathImpl(pcePathProvider);
    }

    @After
    public void tearDown() throws Exception {
        TopologyProvider.getInstance().destroy();
        pcePathProvider.destroy();
        PathsRecordPerDomain.getInstance().destroy();
    }

    @Test
    public void createPathInputCheckTest() throws InterruptedException, ExecutionException {
        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);

        assertTrue(!output.get().isSuccessful());
        assertEquals("Illegal argument!",output.get().getErrors().iterator().next().getMessage());

        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(null,serviceInstance);
    }


    @Test
    public void createPathFailTest() throws InterruptedException, ExecutionException {
        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setEgress(Utils.build2EgressInfo("node2","node4"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(40L).build())
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);

        List<Egress> egressList = output.get().getResult().getEgress();
        for (Egress egress : egressList) {
            Utils.checkPathNull(egress.getPath().getPathLink());
        }

        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(null,serviceInstance);
    }


    @Test
    public void createPathSuccessTest() throws InterruptedException, ExecutionException {
        List<DetnetLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links,dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setEgress(Utils.build2EgressInfo("node2","node4"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);

        List<Egress> egresses = output.get().getResult().getEgress();
        assertEquals(2,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node2")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(90L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node4")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node4");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(180L,egress.getPath().getPathDelay().longValue());
            }
        }

        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertNotEquals(serviceInstance, null);
        assertTrue(serviceInstance.getAllPath().size() == 2);

        PathInstance pathInstance = PcePathDb.getInstance().readPathInstance(1,1111L);
        assertEquals("node1",pathInstance.getIngressNodeId());
        assertEquals(2,pathInstance.getEgress().size());
        for (Egress egress : pathInstance.getEgress()) {
            for (Egress outEgress : egresses) {
                if (outEgress.getEgressNodeId().equals(egress.getEgressNodeId())) {
                    Utils.assertPathData(outEgress.getPath(),egress.getPath());
                }
            }
        }
        removeServiceInstance(1,1111L,"node1");
    }

    @Test
    public void createPathInputCheckWhenUpdateTest() throws InterruptedException, ExecutionException {
        List<DetnetLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links,dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setEgress(Utils.build2EgressInfo("node2","node4"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        pcePathProvider.createPath(input);
        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(2,serviceInstance.getAllPath().size());

        input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node2")
                .setStreamId(1111L)
                .setEgress(Utils.build2EgressInfo("node3","node5"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);

        assertTrue(!output.get().isSuccessful());
        assertEquals("ingress Node associated with the same stream-id is not match!",
                output.get().getErrors().iterator().next().getMessage());

        serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(2,serviceInstance.getAllPath().size());
        removeServiceInstance(1,1111L,"node1");
    }


    @Test
    public void pathCalcWhenUpdateTests() throws InterruptedException, ExecutionException {
        List<DetnetLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links,dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setEgress(Utils.build2EgressInfo("node2","node4"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        pcePathProvider.createPath(input);
        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(2,serviceInstance.getAllPath().size());

        input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setTrafficClass((short) 5)
                .setEgress(Utils.build2EgressInfo("node3","node5"))
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);

        List<Egress> egresses = output.get().getResult().getEgress();
        assertEquals(3,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node2")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(90L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node4")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node4");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(180L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node3")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node3");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(90L,egress.getPath().getPathDelay().longValue());
            }
        }

        PathInstance pathInstance = PcePathDb.getInstance().readPathInstance(1,1111L);
        assertEquals("node1",pathInstance.getIngressNodeId());
        assertEquals(3,pathInstance.getEgress().size());
        for (Egress egress : pathInstance.getEgress()) {
            for (Egress outEgress : egresses) {
                if (outEgress.getEgressNodeId().equals(egress.getEgressNodeId())) {
                    Utils.assertPathData(outEgress.getPath(),egress.getPath());
                }
            }
        }
        removeServiceInstance(1,1111L,"node1");
    }

    @Test
    public void removePathInputCheckTest() throws InterruptedException, ExecutionException {
        List<DetnetLink> links = TopoMockUtils.buildFourNodeTopo();
        Utils.writeLinksToDB(links,dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node1")
                .setStreamId(1111L)
                .setEgress(Utils.build2EgressInfo("node2","node4"))
                .setTrafficClass((short) 5)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(200L).build())
                .build();
        pcePathProvider.createPath(input);
        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(2,serviceInstance.getAllPath().size());

        RemovePathInput removeInput = new RemovePathInputBuilder()
                .setDomainId(1)
                .setSrtreamId(1111L)
                .setIngressNodeId("node3")
                .build();
        Future<RpcResult<RemovePathOutput>> output = pcePathProvider.removePath(removeInput);

        assertTrue(!output.get().isSuccessful());
        assertEquals("ingress Node associated with the same stream-id is not match!",
                output.get().getErrors().iterator().next().getMessage());

        serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(2,serviceInstance.getAllPath().size());

        removeInput = new RemovePathInputBuilder()
                .setDomainId(1)
                .setSrtreamId(2222L)
                .setIngressNodeId("node3")
                .build();
        output = pcePathProvider.removePath(removeInput);

        assertTrue(output.get().isSuccessful());
        serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(2,serviceInstance.getAllPath().size());
        removeServiceInstance(1,1111L,"node1");
    }



    private void removeServiceInstance(Integer domainId, Long streamId, String ingressNode) {
        RemovePathInput input = new RemovePathInputBuilder()
                .setDomainId(domainId).setSrtreamId(streamId).setIngressNodeId(ingressNode).build();
        pcePathProvider.removePath(input);
    }



    @Test
    public void removeAllPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node(),dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

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
        assertEquals(3,output.get().getResult().getEgress().size());

        RemovePathInput removeInput = new RemovePathInputBuilder()
                .setDomainId(1)
                .setSrtreamId(1111L)
                .setIngressNodeId("node1")
                .build();
        Future<RpcResult<RemovePathOutput>> removeOutput = pcePathProvider.removePath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getEgress());

        removeInput = new RemovePathInputBuilder()
                .setDomainId(1)
                .setSrtreamId(1111L)
                .setIngressNodeId("node1")
                .build();
        removeOutput = pcePathProvider.removePath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        assertEquals(null,removeOutput.get().getResult().getEgress());

        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertEquals(null,serviceInstance);

        PathInstance pathInstance = PcePathDb.getInstance().readPathInstance(1,1111L);
        assertEquals(null,pathInstance);

    }

    @Test
    public void removeSpecifiedPathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node(),dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

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
        assertEquals(3,output.get().getResult().getEgress().size());

        List<org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.remove.path.input
                .Egress> egressList = new ArrayList<>();
        egressList.add(new org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.remove.path.input
                .EgressBuilder().setEgressNodeId("node2").build());
        RemovePathInput removeInput = new RemovePathInputBuilder()
                .setDomainId(1)
                .setSrtreamId(1111L)
                .setIngressNodeId("node1")
                .setEgress(egressList)
                .build();
        Future<RpcResult<RemovePathOutput>> removeOutput = pcePathProvider.removePath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());

        List<Egress> egresses = removeOutput.get().getResult().getEgress();
        assertEquals(2,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());

            if (egress.getEgressNodeId().equals("node4")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node4");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(100L,egress.getPath().getPathDelay().longValue());
            } else {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node5");
                assertEquals(20L,egress.getPath().getPathMetric().intValue());
                assertEquals(100L,egress.getPath().getPathDelay().longValue());
            }
        }

        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertNotEquals(null,serviceInstance);
        assertTrue(serviceInstance.getAllPath().size() == 2);

        PathInstance pathInstance = PcePathDb.getInstance().readPathInstance(1,1111L);
        assertNotEquals(null,pathInstance);
        assertEquals(2,pathInstance.getEgress().size());
        for (Egress egress : pathInstance.getEgress()) {
            for (Egress outEgress : egresses) {
                if (outEgress.getEgressNodeId().equals(egress.getEgressNodeId())) {
                    Utils.assertPathData(outEgress.getPath(),egress.getPath());
                }
            }
        }

        egressList.clear();
        egressList.add(new org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.remove.path.input
                .EgressBuilder().setEgressNodeId("node4").build());
        removeInput = new RemovePathInputBuilder()
                .setDomainId(1)
                .setSrtreamId(1111L)
                .setIngressNodeId("node1")
                .setEgress(egressList)
                .build();
        removeOutput = pcePathProvider.removePath(removeInput);
        assertTrue(removeOutput.get().isSuccessful());
        egresses = removeOutput.get().getResult().getEgress();
        assertEquals(1,egresses.size());

        serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,1111L));
        assertNotEquals(null,serviceInstance);
        assertTrue(serviceInstance.getAllPath().size() == 1);

        pathInstance = PcePathDb.getInstance().readPathInstance(1,1111L);
        assertNotEquals(null,pathInstance);
        assertEquals(1,pathInstance.getEgress().size());
        for (Egress egress : pathInstance.getEgress()) {
            for (Egress outEgress : egresses) {
                if (outEgress.getEgressNodeId().equals(egress.getEgressNodeId())) {
                    Utils.assertPathData(outEgress.getPath(),egress.getPath());
                }
            }
        }

        removeServiceInstance(1,1111L,"node1");
    }


    @Test
    public void queryPathInputCheckTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node(),dataBroker);
        TopoMockUtils.buildNodeInOneDomain(true,dataBroker);

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

        QueryPathInput queryInput = new QueryPathInputBuilder()
                .setStreamId(1111L)
                .build();
        Future<RpcResult<QueryPathOutput>> queryOutput = pcePathProvider.queryPath(queryInput);

        assertTrue(!queryOutput.get().isSuccessful());
        assertEquals("Illegal argument!",queryOutput.get().getErrors().iterator().next().getMessage());

        queryInput = new QueryPathInputBuilder()
                .setDomainId(2)
                .setStreamId(1111L)
                .build();
        queryOutput = pcePathProvider.queryPath(queryInput);

        assertTrue(!queryOutput.get().isSuccessful());
        assertEquals("path instance does not exists!",queryOutput.get().getErrors().iterator().next().getMessage());

    }

    @Test
    public void queryPathInstancePathTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node(),dataBroker);
        TopoMockUtils.buildNodeInTwoDomain(true,dataBroker);
        Utils.buildPathInstanceForQueryAndRecovery(pcePathProvider);

        QueryPathInput queryInput = new QueryPathInputBuilder()
                .setDomainId(1).setStreamId(1111L)
                .build();
        Future<RpcResult<QueryPathOutput>> queryOutput = pcePathProvider.queryPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        List<Egress> egresses = queryOutput.get().getResult().getEgress();
        assertEquals(3,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node2")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(50L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node4")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node4");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(100L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node5")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node5");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(100L,egress.getPath().getPathDelay().longValue());
            }
        }

        queryInput = new QueryPathInputBuilder()
                .setDomainId(1).setStreamId(2222L)
                .build();
        queryOutput = pcePathProvider.queryPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        egresses = queryOutput.get().getResult().getEgress();
        assertEquals(2,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node1")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node2","node1");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(70L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node5")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node2","node5");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(70L,egress.getPath().getPathDelay().longValue());
            }
        }

        queryInput = new QueryPathInputBuilder()
                .setDomainId(2).setStreamId(3333L)
                .build();
        queryOutput = pcePathProvider.queryPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        egresses = queryOutput.get().getResult().getEgress();
        assertEquals(2,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node2")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node4","node2");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(90L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node6")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node4","node6");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(90L,egress.getPath().getPathDelay().longValue());
            }
        }

        removeServiceInstance(1,1111L,"node1");
        removeServiceInstance(1,2222L,"node2");
        removeServiceInstance(2,3333L,"node4");
    }


    @Test
    public void calcPathInTwoSubDomainTest() throws ExecutionException, InterruptedException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node(),dataBroker);
        TopoMockUtils.buildNodeInTwoDomain(false,dataBroker);
        Utils.buildPathInstanceForQueryAndRecovery(pcePathProvider);

        QueryPathInput queryInput = new QueryPathInputBuilder()
                .setDomainId(1).setStreamId(1111L)
                .build();
        Future<RpcResult<QueryPathOutput>> queryOutput = pcePathProvider.queryPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        List<Egress> egresses = queryOutput.get().getResult().getEgress();
        assertEquals(3,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node2")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(50L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node4")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node4");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(100L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node5")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node1","node2","node5");
                assertEquals(20L,egress.getPath().getPathMetric().longValue());
                assertEquals(100L,egress.getPath().getPathDelay().longValue());
            }
        }

        queryInput = new QueryPathInputBuilder()
                .setDomainId(1).setStreamId(2222L)
                .build();
        queryOutput = pcePathProvider.queryPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        egresses = queryOutput.get().getResult().getEgress();
        assertEquals(2,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node1")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node2","node1");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(70L,egress.getPath().getPathDelay().longValue());
            }
            if (egress.getEgressNodeId().equals("node5")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node2","node5");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(70L,egress.getPath().getPathDelay().longValue());
            }
        }

        queryInput = new QueryPathInputBuilder()
                .setDomainId(2).setStreamId(3333L)
                .build();
        queryOutput = pcePathProvider.queryPath(queryInput);
        assertTrue(queryOutput.get().isSuccessful());
        egresses = queryOutput.get().getResult().getEgress();
        assertEquals(1,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            assertEquals("node6",egress.getEgressNodeId());
            Utils.checkPath(egress.getPath().getPathLink(),"node4","node6");
            assertEquals(10L,egress.getPath().getPathMetric().longValue());
            assertEquals(90L,egress.getPath().getPathDelay().longValue());
        }

    }




}


