/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.pce.impl.provider;
/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
*/
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
//import org.opendaylight.detnet.pce.impl.detnetpath.ServiceInstance;
//import org.opendaylight.detnet.pce.impl.topology.PathsRecordPerDomain;
import org.opendaylight.detnet.pce.impl.topology.TopologyProvider;
/*
import org.opendaylight.detnet.pce.impl.util.TopoMockUtils;
import org.opendaylight.detnet.pce.impl.util.Utils;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.LinkBandwidthChange;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.LinkBandwidthChangeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.link.bandwidth.change.OldLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstance;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstanceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yangtools.yang.common.RpcResult;
*/

public class TopologyProviderTest extends AbstractConcurrentDataBrokerTest {
    private DataBroker dataBroker;
    PcePathImpl pcePathProvider;

    TopologyProvider topologyProvider;
/*
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

    public void linkChange_bandwidthChangeTest() throws InterruptedException, ExecutionException {
        Utils.writeLinksToDB(TopoMockUtils.getTopo6Node(),dataBroker);
        TopoMockUtils.buildNodeInTwoDomain(true,dataBroker);
        Utils.buildPathInstanceForQueryAndRecovery(pcePathProvider);

        notifyLinkBandwidthChange("node2","link25","link52","node5",300000L);
        notifyLinkBandwidthChange("node2","link24","link42","node4",400000L);
        notifyLinkBandwidthChange("node2","link21","link12","node1",400000L);
        notifyLinkBandwidthChange("node4","link46","link64","node6",400000L);
        notifyLinkBandwidthChange("node4","link42","link24","node2",400000L);

        CreatePathInput input = new CreatePathInputBuilder()
                .setDomainId(1)
                .setIngressNodeId("node2")
                .setStreamId(4444L)
                .setEgress(Utils.build2EgressInfo("node1","node5"))
                .setTrafficClass((short) 0)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(350000L).setMaxDelay(200L).build())
                .build();
        Future<RpcResult<CreatePathOutput>> output = pcePathProvider.createPath(input);

        List<Egress> egresses = output.get().getResult().getEgress();
        assertEquals(2,egresses.size());
        for (Egress egress : egresses) {
            assertNotEquals(null,egress.getPath().getPathLink());
            assertTrue(!egress.getPath().getPathLink().isEmpty());
            if (egress.getEgressNodeId().equals("node1")) {
                Utils.checkPath(egress.getPath().getPathLink(),"node2","node1");
                assertEquals(10L,egress.getPath().getPathMetric().longValue());
                assertEquals(40L,egress.getPath().getPathDelay().longValue());
            } else {
                Utils.checkPath(egress.getPath().getPathLink(),"node2","node4","node6","node5");
                assertEquals(30L,egress.getPath().getPathMetric().longValue());
                assertEquals(120L,egress.getPath().getPathDelay().longValue());
            }
        }

        ServiceInstance serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(1,4444L));
        assertNotEquals(serviceInstance, null);
        assertTrue(serviceInstance.getAllPath().size() == 2);

        PathInstance pathInstance = PcePathDb.getInstance().readPathInstance(1,4444L);
        assertEquals("node2",pathInstance.getIngressNodeId());
        assertEquals(2,pathInstance.getEgress().size());
        for (Egress egress : pathInstance.getEgress()) {
            for (Egress outEgress : egresses) {
                if (outEgress.getEgressNodeId().equals(egress.getEgressNodeId())) {
                    Utils.assertPathData(outEgress.getPath(),egress.getPath());
                }
            }
        }

        notifyLinkBandwidthChange("node4","link46","link64","node6",50000L);

        input = new CreatePathInputBuilder()
                .setDomainId(2)
                .setIngressNodeId("node4")
                .setStreamId(5555L)
                .setEgress(Utils.build1EgressInfo("node6"))
                .setTrafficClass((short) 2)
                .setPathConstraint(new PathConstraintBuilder().setBandwidth(100000L).setMaxDelay(150L).build())
                .build();
        output = pcePathProvider.createPath(input);

        egresses = output.get().getResult().getEgress();
        assertTrue(egresses.isEmpty());

        serviceInstance = pcePathProvider.getServiceInstance(new PathInstanceKey(2,4444L));
        assertEquals(null,serviceInstance);

        pathInstance = PcePathDb.getInstance().readPathInstance(2,4444L);
        assertEquals(null,pathInstance);

        removeServiceInstance(1,1111L,"node1");
        removeServiceInstance(1,2222L,"node2");
        removeServiceInstance(2,3333L,"node4");
        removeServiceInstance(1,4444L,"node2");
        removeServiceInstance(2,5555L,"node4");
    }


    private void removeServiceInstance(Integer domainId, Long streamId, String ingressNode) {
        RemovePathInput input = new RemovePathInputBuilder()
                .setDomainId(domainId).setSrtreamId(streamId).setIngressNodeId(ingressNode).build();
        pcePathProvider.removePath(input);
    }

    private void notifyLinkBandwidthChange(String srcNode, String srcTp, String destTp, String destNode,
                                           Long bandwidth) {
        DetnetLink link = TopoMockUtils.buildLink(srcNode,srcTp,destTp,destNode,10);
        Utils.writeLinkToDB(TopoMockUtils.buildLinkWithBandwidth(link,bandwidth),dataBroker);
        LinkBandwidthChange linkChange = new LinkBandwidthChangeBuilder()
                .setOldLink(new OldLinkBuilder(link).build())
                .setNewAvailableBandwidth(bandwidth)
                .build();
        topologyProvider.onLinkBandwidthChange(linkChange);
    }*/

}

