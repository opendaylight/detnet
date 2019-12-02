/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.topology.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.DetnetEncapsulationType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.PreofType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddDetnetLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddNodesToSegmentInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddNodesToSegmentInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddNodesToSegmentOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddTopologyIdInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddTopologyIdInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.AddTopologyIdOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeLtpInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeLtpInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeLtpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigDetnetNodeTrafficClassOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.ConfigSegmentsToDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetLinkInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetLinkInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetLinkOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeLtpInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeLtpInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeLtpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeTrafficClassInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeTrafficClassInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteDetnetNodeTrafficClassOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteNodesFromSegmentInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteNodesFromSegmentInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteNodesFromSegmentOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteSegmentsFromDomainInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteSegmentsFromDomainInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.DeleteSegmentsFromDomainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.LoadTopologyIdOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.LoadTopologyIdOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyInput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.api.rev180904.QueryDomainTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDest;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSource;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint16;

public class TopologyImplTest extends AbstractConcurrentDataBrokerTest {
    private static String TopologyId = "topology-1";
    private TopologyServiceImpl topologyImpl;
    private TopologyDataManager topologyDataManager;

    @Before
    public void setUp() throws Exception {
        topologyDataManager = new TopologyDataManager(getDataBroker());
        topologyImpl = new TopologyServiceImpl(topologyDataManager);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void loadTopologyIdTest() throws Exception {
        RpcResult<LoadTopologyIdOutput> output = topologyImpl.loadTopologyId(null).get();
        LoadTopologyIdOutputBuilder outputBuilder = new LoadTopologyIdOutputBuilder(output.getResult());
        Assert.assertTrue(outputBuilder.getTopology() == null);

        addDetnetTopology();
        RpcResult<LoadTopologyIdOutput> output1 = topologyImpl.loadTopologyId(null).get();
        LoadTopologyIdOutputBuilder outputBuilder1 = new LoadTopologyIdOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getTopology().get(0).getTopologyId().equals(TopologyId));

    }

    @Test
    public void configDetnetNodeTest() throws Exception {
        //success
        ConfigDetnetNodeInput input1 = constructDetnetNode(TopologyId, "node1", "node1", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1", "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true);
        RpcResult<ConfigDetnetNodeOutput> output1 = topologyImpl.configDetnetNode(input1).get();
        ConfigDetnetNodeOutputBuilder outputBuilder1 = new ConfigDetnetNodeOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //input null
        ConfigDetnetNodeInput input2 = constructDetnetNode(TopologyId, null, "node1", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1", "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true);
        RpcResult<ConfigDetnetNodeOutput> output2 = topologyImpl.configDetnetNode(input2).get();
        ConfigDetnetNodeOutputBuilder outputBuilder2 = new ConfigDetnetNodeOutputBuilder(output2.getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder2.getConfigureResult().getErrorCause().equals("Illegal argument!"));
    }

    @Test
    public void deleteDetnetNodeTest() throws Exception {
        //input null
        DeleteDetnetNodeInput input1 = constructDeleteDetnetNodeInput(TopologyId, null);
        DeleteDetnetNodeOutputBuilder outputBuilder1 = new DeleteDetnetNodeOutputBuilder(
                topologyImpl.deleteDetnetNode(input1).get().getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        ConfigDetnetNodeInput configDetnetNodeInput = constructDetnetNode(TopologyId, "node1", "node1", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1", "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true);
        topologyImpl.configDetnetNode(configDetnetNodeInput).get();
        DeleteDetnetNodeInput input2 = constructDeleteDetnetNodeInput(TopologyId, "node1");
        DeleteDetnetNodeOutputBuilder outputBuilder2 = new DeleteDetnetNodeOutputBuilder(
                topologyImpl.deleteDetnetNode(input2).get().getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //node not exist
        DeleteDetnetNodeOutputBuilder outputBuilder3 = new DeleteDetnetNodeOutputBuilder(
                topologyImpl.deleteDetnetNode(input2).get().getResult());
        Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder3.getConfigureResult().getErrorCause().equals("node is not exist!"));
    }

    @Test
    public void configDetnetNodeLtpTest() throws Exception {
        //input null
        ConfigDetnetNodeLtpInput input1 = constructConfigDetnetNodeLtpInput(TopologyId, null,
                "spi-0/1/0/2", "spi-0/1/0/2", new IpAddress(new Ipv4Address("192.168.51.3")),
                (long)1, PreofType.PREF, DetnetEncapsulationType.Tsn);
        ConfigDetnetNodeLtpOutputBuilder outputBuilder1 = new ConfigDetnetNodeLtpOutputBuilder(
                topologyImpl.configDetnetNodeLtp(input1).get().getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        ConfigDetnetNodeLtpInput input2 = constructConfigDetnetNodeLtpInput(TopologyId, "node1",
                "spi-0/1/0/2", "spi-0/1/0/2", new IpAddress(new Ipv4Address("192.168.51.3")),
                (long)1, PreofType.PREF, DetnetEncapsulationType.Tsn);
        ConfigDetnetNodeLtpOutputBuilder outputBuilder2 = new ConfigDetnetNodeLtpOutputBuilder(
                topologyImpl.configDetnetNodeLtp(input2).get().getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
    }

    @Test
    public void deleteDetnetNodeLtpTest() throws Exception {
        //input null
        DeleteDetnetNodeLtpInput input1 = constructDeleteDetnetNodeLtpInput(TopologyId, null, "spi-0/1/0/2");
        DeleteDetnetNodeLtpOutputBuilder outputBuilder1 = new DeleteDetnetNodeLtpOutputBuilder(
                topologyImpl.deleteDetnetNodeLtp(input1).get().getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        ConfigDetnetNodeLtpInput configInput = constructConfigDetnetNodeLtpInput(TopologyId, "node1",
                "spi-0/1/0/2", "spi-0/1/0/2", new IpAddress(new Ipv4Address("192.168.51.3")),
                (long)1, PreofType.PREF, DetnetEncapsulationType.Tsn);
        topologyImpl.configDetnetNodeLtp(configInput).get();
        DeleteDetnetNodeLtpInput input2 = constructDeleteDetnetNodeLtpInput(TopologyId, "node1", "spi-0/1/0/2");
        DeleteDetnetNodeLtpOutputBuilder outputBuilder2 = new DeleteDetnetNodeLtpOutputBuilder(
                topologyImpl.deleteDetnetNodeLtp(input2).get().getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //tp not exist
        DeleteDetnetNodeLtpOutputBuilder outputBuilder3 = new DeleteDetnetNodeLtpOutputBuilder(
                topologyImpl.deleteDetnetNodeLtp(input2).get().getResult());
        Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder3.getConfigureResult().getErrorCause().equals("tp is not exist!"));
    }

    @Test
    public void configDetnetNodeTrafficClassTest() throws Exception {
        //success
        ConfigDetnetNodeTrafficClassInput input1 = constructDetnetNodeTraffficClassInput(
                TopologyId, "node1", "spi-0/1/0/2", (short) 10, (long)3, (long)1);
        RpcResult<ConfigDetnetNodeTrafficClassOutput> output1 = topologyImpl.configDetnetNodeTrafficClass(input1)
                .get();
        ConfigDetnetNodeTrafficClassOutputBuilder outputBuilder1 = new
                ConfigDetnetNodeTrafficClassOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //input null
        ConfigDetnetNodeTrafficClassInput input2 = constructDetnetNodeTraffficClassInput(
                TopologyId, null, "spi-0/1/0/2", (short) 10, (long)3, (long)1);
        RpcResult<ConfigDetnetNodeTrafficClassOutput> output2 = topologyImpl.configDetnetNodeTrafficClass(input2).get();
        ConfigDetnetNodeTrafficClassOutputBuilder outputBuilder2 = new
                ConfigDetnetNodeTrafficClassOutputBuilder(output2.getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder2.getConfigureResult().getErrorCause().equals("Illegal argument!"));
    }

    @Test
    public void deleteDetnetNodeTrafficClassTest() throws Exception {
        //input null
        DeleteDetnetNodeTrafficClassInput input1 = constructDeleteDetnetNodeTrafficClassInput(
                TopologyId, null, "spi-0/1/0/2", (short)1);
        DeleteDetnetNodeTrafficClassOutputBuilder outputBuilder1 = new DeleteDetnetNodeTrafficClassOutputBuilder(
                topologyImpl.deleteDetnetNodeTrafficClass(input1).get().getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        ConfigDetnetNodeTrafficClassInput configInput = constructDetnetNodeTraffficClassInput(
                TopologyId, "node1", "spi-0/1/0/2", (short) 1, (long)3, (long)1);
        topologyImpl.configDetnetNodeTrafficClass(configInput).get();
        DeleteDetnetNodeTrafficClassInput input2 = constructDeleteDetnetNodeTrafficClassInput(
                TopologyId, "node1", "spi-0/1/0/2", (short)1);
        DeleteDetnetNodeTrafficClassOutputBuilder outputBuilder2 = new DeleteDetnetNodeTrafficClassOutputBuilder(
                topologyImpl.deleteDetnetNodeTrafficClass(input2).get().getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //traffic class not exist
        DeleteDetnetNodeTrafficClassOutputBuilder outputBuilder3 = new DeleteDetnetNodeTrafficClassOutputBuilder(
                topologyImpl.deleteDetnetNodeTrafficClass(input2).get().getResult());
        Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder3.getConfigureResult().getErrorCause().equals("trafic class is not exist!"));
    }

    @Test
    public void configDetnetLinkTest() throws Exception {
        //success
        AddDetnetLinkInput input1 = constructDetnetLink(TopologyId, "link1", "node1", "sp1-0/1/0/1",
                "node2", "sp1-0/1/0/1", (long)10, (long)100, (long)80, (long)0,(long)10, (long)5, (long)0);
        RpcResult<AddDetnetLinkOutput> output1 = topologyImpl.addDetnetLink(input1).get();
        ConfigDetnetNodeOutputBuilder outputBuilder1 = new ConfigDetnetNodeOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //input null
        AddDetnetLinkInput input2 = constructDetnetLink(TopologyId, null, "node1", "sp1-0/1/0/1",
                "node2", "sp1-0/1/0/1", (long)10, (long)100, (long)80, (long)0,(long)10, (long)5, (long)0);
        RpcResult<AddDetnetLinkOutput> output2 = topologyImpl.addDetnetLink(input2).get();
        AddDetnetLinkOutputBuilder outputBuilder2 = new AddDetnetLinkOutputBuilder(output2.getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder2.getConfigureResult().getErrorCause().equals("Illegal argument!"));
    }

    @Test
    public void deleteDetnetLinkTest() throws Exception {
        //input null
        DeleteDetnetLinkInput input1 = constructDeleteDetnetLinkInput(TopologyId, null);
        DeleteDetnetLinkOutputBuilder outputBuilder1 = new DeleteDetnetLinkOutputBuilder(
                topologyImpl.deleteDetnetLink(input1).get().getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        AddDetnetLinkInput addDetnetLinkInput = constructDetnetLink(TopologyId, "link1", "node1", "sp1-0/1/0/1",
                "node2", "sp1-0/1/0/1", (long)10, (long)100, (long)80, (long)0,(long)10, (long)5, (long)0);
        topologyImpl.addDetnetLink(addDetnetLinkInput).get();
        DeleteDetnetLinkInput input2 = constructDeleteDetnetLinkInput(TopologyId, "link1");
        DeleteDetnetLinkOutputBuilder outputBuilder2 = new DeleteDetnetLinkOutputBuilder(
                topologyImpl.deleteDetnetLink(input2).get().getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //link not exist
        DeleteDetnetLinkOutputBuilder outputBuilder3 = new DeleteDetnetLinkOutputBuilder(
                topologyImpl.deleteDetnetLink(input2).get().getResult());
        Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder3.getConfigureResult().getErrorCause().equals("link is not exist!"));
    }

    @Test
    public void addNodesToSegmenTest() throws Exception {
        //node node1 is not exist
        AddNodesToSegmentInput input1 = addNodesToSegmentInput(TopologyId, 1, 1);
        RpcResult<AddNodesToSegmentOutput> output1 = topologyImpl.addNodesToSegment(input1).get();
        ConfigDetnetNodeOutputBuilder outputBuilder1 = new ConfigDetnetNodeOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("node node1 is not exist!"));

        //input null
        AddNodesToSegmentInput input2 = addNodesToSegmentInput(TopologyId, null, 1);
        RpcResult<AddNodesToSegmentOutput> output2 = topologyImpl.addNodesToSegment(input2).get();
        ConfigDetnetNodeOutputBuilder outputBuilder2 = new ConfigDetnetNodeOutputBuilder(output2.getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder2.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success - segment not exist
        ConfigDetnetNodeInput addNode1 = constructDetnetNode(TopologyId, "node1", "node1", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1", "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true);
        ConfigDetnetNodeInput addNode2 = constructDetnetNode(TopologyId, "node2", "node2", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1", "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true);
        topologyImpl.configDetnetNode(addNode1).get();
        topologyImpl.configDetnetNode(addNode2).get();
        AddNodesToSegmentInput input3 = addNodesToSegmentInput(TopologyId, 1, 2);
        RpcResult<AddNodesToSegmentOutput> output3 = topologyImpl.addNodesToSegment(input3).get();
        ConfigDetnetNodeOutputBuilder outputBuilder3 = new ConfigDetnetNodeOutputBuilder(output3.getResult());
        Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        AddNodesToSegmentInput input4 = addNodesToSegmentInput(TopologyId, 2, 2);
        //RpcResult<AddNodesToSegmentOutput> output4 = topologyImpl.addNodesToSegment(input4).get();
       // ConfigDetnetNodeOutputBuilder outputBuilder4 = new ConfigDetnetNodeOutputBuilder(output4.getResult());
        //Assert.assertTrue(outputBuilder4.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //success - segment exist already
        //add segment 1
        AddNodesToSegmentInput input5 = addNodesToSegmentInput(TopologyId, 1, 1);
        RpcResult<AddNodesToSegmentOutput> output5 = topologyImpl.addNodesToSegment(input5).get();
        ConfigDetnetNodeOutputBuilder outputBuilder5 = new ConfigDetnetNodeOutputBuilder(output5.getResult());
        Assert.assertTrue(outputBuilder5.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        //add segment again
        RpcResult<AddNodesToSegmentOutput> output6 = topologyImpl.addNodesToSegment(input5).get();
        ConfigDetnetNodeOutputBuilder outputBuilder6 = new ConfigDetnetNodeOutputBuilder(output6.getResult());
        Assert.assertTrue(outputBuilder6.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
    }

    @Test
    public void deleteNodesFromSegmenTest() throws Exception {
        //node node1 is not exist
        DeleteNodesFromSegmentInput input1 = deleteNodesFromSegmenInput(TopologyId, 1, 1);
        RpcResult<DeleteNodesFromSegmentOutput> output1 = topologyImpl.deleteNodesFromSegment(input1).get();
        ConfigDetnetNodeOutputBuilder outputBuilder1 = new ConfigDetnetNodeOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("node node1 is not exist!"));

        //input null
        DeleteNodesFromSegmentInput input2 = deleteNodesFromSegmenInput(TopologyId, null, 1);
        RpcResult<DeleteNodesFromSegmentOutput> output2 = topologyImpl.deleteNodesFromSegment(input2).get();
        ConfigDetnetNodeOutputBuilder outputBuilder2 = new ConfigDetnetNodeOutputBuilder(output2.getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder2.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success - segment exist
        ConfigDetnetNodeInput addNode1 = constructDetnetNode(TopologyId, "node1", "node1", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1", "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true);
        topologyImpl.configDetnetNode(addNode1).get();
        AddNodesToSegmentInput addSegment1 = addNodesToSegmentInput(TopologyId, 1, 1);
        topologyImpl.addNodesToSegment(addSegment1).get();
        DeleteNodesFromSegmentInput input3 = deleteNodesFromSegmenInput(TopologyId, 1, 1);
        //RpcResult<DeleteNodesFromSegmentOutput> output3 = topologyImpl.deleteNodesFromSegment(input3).get();
       // ConfigDetnetNodeOutputBuilder outputBuilder3 = new ConfigDetnetNodeOutputBuilder(output3.getResult());
        //Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        //topologyImpl.loadTopologyId().get();
    }

    @Test
    public void configSegmentsToDomainTest() throws Exception {
        //input null
        ConfigSegmentsToDomainInput input1 = addSegmentsToDomainInput(TopologyId, null, 2);
        RpcResult<ConfigSegmentsToDomainOutput> output1 = topologyImpl.configSegmentsToDomain(input1).get();
        ConfigSegmentsToDomainOutputBuilder outputBuilder1 = new
                ConfigSegmentsToDomainOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success domain == null
        ConfigSegmentsToDomainInput input2 = addSegmentsToDomainInput(TopologyId, 1, 2);
        RpcResult<ConfigSegmentsToDomainOutput> output2 = topologyImpl.configSegmentsToDomain(input2).get();
        ConfigSegmentsToDomainOutputBuilder outputBuilder2 = new
                ConfigSegmentsToDomainOutputBuilder(output2.getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //success domain != null
        ConfigSegmentsToDomainInput input3 = addSegmentsToDomainInput(TopologyId, 1, 3);
        //RpcResult<ConfigSegmentsToDomainOutput> output3 = topologyImpl.configSegmentsToDomain(input3).get();
        //ConfigSegmentsToDomainOutputBuilder outputBuilder3 = new
         //       ConfigSegmentsToDomainOutputBuilder(output3.getResult());
        //Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
    }

    @Test
    public void deleteSegmentsFromDomainTest() throws Exception {
        //input null
        DeleteSegmentsFromDomainInput input1 = constructDeleteSegmentsFromDomainInputInput(TopologyId, null, 1);
        DeleteSegmentsFromDomainOutputBuilder outputBuilder1 = new DeleteSegmentsFromDomainOutputBuilder(
                topologyImpl.deleteSegmentsFromDomain(input1).get().getResult());
        Assert.assertTrue(outputBuilder1.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder1.getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        ConfigSegmentsToDomainInput configInput = addSegmentsToDomainInput(TopologyId, 1, 2);
        topologyImpl.configSegmentsToDomain(configInput).get();
        DeleteSegmentsFromDomainInput input2 = constructDeleteSegmentsFromDomainInputInput(TopologyId, 1, 1);
        DeleteSegmentsFromDomainOutputBuilder outputBuilder2 = new DeleteSegmentsFromDomainOutputBuilder(
                topologyImpl.deleteSegmentsFromDomain(input2).get().getResult());
        Assert.assertTrue(outputBuilder2.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        //Assert.assertTrue(topologyDataManager.getDomain(TopologyId, 1).getSegments().get(0).getSegmentId().equals(2));

        //segment not exist
        DeleteSegmentsFromDomainOutputBuilder outputBuilder3 = new DeleteSegmentsFromDomainOutputBuilder(
                topologyImpl.deleteSegmentsFromDomain(input2).get().getResult());
        Assert.assertTrue(outputBuilder3.getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(outputBuilder3.getConfigureResult().getErrorCause().equals("segment is not exist!"));

        //success only one segment exist
        DeleteSegmentsFromDomainInput input4 = constructDeleteSegmentsFromDomainInputInput(TopologyId, 1, 2);
        DeleteSegmentsFromDomainOutputBuilder outputBuilder4 = new DeleteSegmentsFromDomainOutputBuilder(
                topologyImpl.deleteSegmentsFromDomain(input4).get().getResult());
        Assert.assertTrue(outputBuilder4.getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);
        Assert.assertTrue(topologyDataManager.getDomain(TopologyId, 1) == null);
    }

    @Test
    public void queryDomainTopologyTest() throws Exception {
        //domain not exist
        QueryDomainTopologyInput input0 = constructQueryDomainTopoInput(TopologyId, 1);
        RpcResult<QueryDomainTopologyOutput> output0 = topologyImpl.queryDomainTopology(input0).get();
        Assert.assertTrue(output0.getResult().getDetnetNode() == null);
        Assert.assertTrue(output0.getResult().getDetnetLink() == null);

        //success  node1,node2,link1  in domainTopology 1
        topologyImpl.configDetnetNode(constructDetnetNode(TopologyId, "node1", "node1", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1",
                "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true)).get();
        topologyImpl.configDetnetNode(constructDetnetNode(TopologyId, "node2", "node2", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1",
                "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true)).get();
        AddDetnetLinkInput linkInput1 = constructDetnetLink(TopologyId, "link1", "node1",
                "sp1-0/1/0/1", "node2", "sp1-0/1/0/1", (long)10, (long)100, (long)80,
                (long)0,(long)10, (long)5, (long)0);
        topologyImpl.addDetnetLink(linkInput1).get();
        topologyImpl.addNodesToSegment(addNodesToSegmentInput(TopologyId, 1, 2)).get();
        topologyImpl.configSegmentsToDomain(addSegmentsToDomainInput(TopologyId, 1, 1)).get();
        //topologyImpl.loadTopologyId().get();
        QueryDomainTopologyInput input1 = constructQueryDomainTopoInput(TopologyId, 1);
        RpcResult<QueryDomainTopologyOutput> output1 = topologyImpl.queryDomainTopology(input1).get();
        QueryDomainTopologyOutputBuilder outputBuilder1 = new QueryDomainTopologyOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder1.getDetnetNode().size() == 2);
        Assert.assertTrue((outputBuilder1.getDetnetNode().get(0).getNodeId().equals("node2")
                && outputBuilder1.getDetnetNode().get(1).getNodeId().equals("node1"))
                || (outputBuilder1.getDetnetNode().get(0).getNodeId().equals("node1")
                && outputBuilder1.getDetnetNode().get(1).getNodeId().equals("node2")));
        Assert.assertTrue(outputBuilder1.getDetnetLink().size() == 1);
        Assert.assertTrue(outputBuilder1.getDetnetLink().get(0).getLinkId().equals("link1"));

        //success  node3,,link2,link3  not in domainTopology 1
        topologyImpl.configDetnetNode(constructDetnetNode(TopologyId, "node3", "node3", (long) 10,
                new BigInteger("0"), new BigInteger("0"), "1.1.1.1/1",
                "AB1D:EF01:2345:6789:0:0:AB1D:CD97/128", true)).get();
        AddDetnetLinkInput linkInput2 = constructDetnetLink(TopologyId, "link2", "node2",
                "sp1-0/1/0/2", "node3", "sp1-0/1/0/2", (long)10, (long)100, (long)80,
                (long)0,(long)10, (long)5, (long)0);
        AddDetnetLinkInput linkInput3 = constructDetnetLink(TopologyId, "link3", "node1",
                "sp1-0/1/0/3", "node3", "sp1-0/1/0/3", (long)10, (long)100, (long)80,
                (long)0,(long)10, (long)5, (long)0);
        topologyImpl.addDetnetLink(linkInput2).get();
        topologyImpl.addDetnetLink(linkInput3).get();
        QueryDomainTopologyOutputBuilder outputBuilder2 = new QueryDomainTopologyOutputBuilder(output1.getResult());
        Assert.assertTrue(outputBuilder2.getDetnetNode().size() == 2);
        Assert.assertTrue((outputBuilder2.getDetnetNode().get(0).getNodeId().equals("node2")
                && outputBuilder2.getDetnetNode().get(1).getNodeId().equals("node1"))
                || (outputBuilder2.getDetnetNode().get(0).getNodeId().equals("node1")
                && outputBuilder2.getDetnetNode().get(1).getNodeId().equals("node2")));
        Assert.assertTrue(outputBuilder2.getDetnetLink().size() == 1);
        Assert.assertTrue(outputBuilder2.getDetnetLink().get(0).getLinkId().equals("link1"));

        //node1,node2,node3, link1,link2,link3 in domainTopology 2
        //topologyImpl.addNodesToSegment(addNodesToSegmentInput(TopologyId, 2, 3)).get();
        topologyImpl.configSegmentsToDomain(addSegmentsToDomainInput(TopologyId, 2, 2)).get();
        QueryDomainTopologyInput input3 = constructQueryDomainTopoInput(TopologyId, 2);
        RpcResult<QueryDomainTopologyOutput> output3 = topologyImpl.queryDomainTopology(input3).get();
        QueryDomainTopologyOutputBuilder outputBuilder3 = new QueryDomainTopologyOutputBuilder(output3.getResult());
        /*Assert.assertTrue(outputBuilder3.getDetnetNode().size() == 3);
        Assert.assertTrue(outputBuilder3.getDetnetNode().get(0).getNodeId().equals("node1")
                || outputBuilder3.getDetnetNode().get(0).getNodeId().equals("node2")
                || outputBuilder3.getDetnetNode().get(0).getNodeId().equals("node3"));
        Assert.assertTrue(outputBuilder3.getDetnetLink().size() == 3);
        Assert.assertTrue(outputBuilder3.getDetnetLink().get(0).getLinkId().equals("link1")
                || outputBuilder3.getDetnetLink().get(0).getLinkId().equals("link2")
                || outputBuilder3.getDetnetLink().get(0).getLinkId().equals("link3"));
        topologyImpl.loadTopologyId().get();*/

        //input null
        QueryDomainTopologyInput input4 = constructQueryDomainTopoInput(TopologyId, null);
        RpcResult<QueryDomainTopologyOutput> output4 = topologyImpl.queryDomainTopology(input4).get();
        //Assert.assertEquals("Illegal argument!",output4.getErrors().iterator().next().getMessage());
    }

    @Test
    public void addTopologyIdTest() throws Exception {
        //input null
        AddTopologyIdInput addInput1 = new AddTopologyIdInputBuilder().setTopologyId(null).build();
        RpcResult<AddTopologyIdOutput> output1 = topologyImpl.addTopologyId(addInput1).get();
        Assert.assertTrue(output1.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output1.getResult().getConfigureResult().getErrorCause().equals("Illegal argument!"));

        //success
        AddTopologyIdInput addInput2 = new AddTopologyIdInputBuilder().setTopologyId("topology-1").build();
        RpcResult<AddTopologyIdOutput> output2 = topologyImpl.addTopologyId(addInput2).get();
        Assert.assertTrue(output2.getResult().getConfigureResult().getResult() == ConfigureResult.Result.SUCCESS);

        //topology id is exist
        RpcResult<AddTopologyIdOutput> output3 = topologyImpl.addTopologyId(addInput2).get();
        Assert.assertTrue(output3.getResult().getConfigureResult().getResult() == ConfigureResult.Result.FAILURE);
        Assert.assertTrue(output3.getResult().getConfigureResult().getErrorCause().equals("topology id is exist!"));
        topologyImpl.loadTopologyId(null).get();
    }

    private QueryDomainTopologyInput constructQueryDomainTopoInput(String topologyId, Integer domainId) {
        return new QueryDomainTopologyInputBuilder()
                .setTopologyId(topologyId)
                .setDomainId(domainId)
                .build();
    }

    private DeleteSegmentsFromDomainInput constructDeleteSegmentsFromDomainInputInput(
            String topologyId, Integer domainId, Integer segmentId) {
        return new DeleteSegmentsFromDomainInputBuilder()
                .setTopologyId(topologyId)
                .setDomainId(domainId)
                .setSegmentId(segmentId)
                .build();
    }

    private ConfigSegmentsToDomainInput addSegmentsToDomainInput(
            String topologyId, Integer domainId, Integer segmentNum) {
        List<Uint16> segmentList = new ArrayList<Uint16>();
        for (int index = 1; index <= segmentNum; index++) {
            segmentList.add(Uint16.valueOf(index));
        }
        return new ConfigSegmentsToDomainInputBuilder()
                .setTopologyId(topologyId)
                .setDomainId(domainId)
                .setSegments(segmentList)
                .build();
    }

    private DeleteNodesFromSegmentInput deleteNodesFromSegmenInput(
            String topologyId, Integer segmentId, Integer nodeNum) {
        List<String> nodeList = new ArrayList<String>();
        for (int index = 1; index <= nodeNum; index++) {
            String node = "node" + index;
            nodeList.add(node);
        }
        return new DeleteNodesFromSegmentInputBuilder()
                .setTopologyId(topologyId)
                .setSegmentId(segmentId)
                .setDetnetNodes(nodeList)
                .build();
    }

    private AddNodesToSegmentInput addNodesToSegmentInput(String topologyId, Integer segmentId, Integer nodeNum) {
        List<String> nodeList = new ArrayList<String>();
        for (int index = 1; index <= nodeNum; index++) {
            String node = "node" + index;
            nodeList.add(node);
        }
        return new AddNodesToSegmentInputBuilder()
                .setTopologyId(topologyId)
                .setSegmentId(segmentId)
                .setDetnetNodes(nodeList)
                .build();
    }

    private DeleteDetnetLinkInput constructDeleteDetnetLinkInput(String topologyId, String linkId) {
        return new DeleteDetnetLinkInputBuilder()
                .setTopologyId(topologyId)
                .setLinkId(linkId)
                .build();
    }

    private AddDetnetLinkInput constructDetnetLink(
            String topologyId, String linkId, String sourceNode, String sourceTp, String destNode, String destTp,
            Long maximumReservableBandwidth, Long linkBandwidth, Long availableDetnetBandwidth, Long metric,
            Long reservedDetnetBandwidth, Long linkDelay, Long loss) {
        LinkSource linkSource = new LinkSourceBuilder()
                .setSourceNode(sourceNode)
                .setSourceTp(sourceTp)
                .build();
        LinkDest linkDest = new LinkDestBuilder()
                .setDestNode(destNode)
                .setDestTp(destTp)
                .build();
        return new AddDetnetLinkInputBuilder()
                .setTopologyId(topologyId)
                .setLinkId(linkId)
                .setLinkSource(linkSource)
                .setLinkDest(linkDest)
                .setLinkDelay(linkDelay)
                .setMaximumReservableBandwidth(maximumReservableBandwidth)
                .setLinkBandwidth(linkBandwidth)
                .setAvailableDetnetBandwidth(availableDetnetBandwidth)
                .setReservedDetnetBandwidth(reservedDetnetBandwidth)
                .setMetric(metric)
                .setLoss(loss)
                .build();
    }

    private DeleteDetnetNodeTrafficClassInput constructDeleteDetnetNodeTrafficClassInput(
            String topologyId, String nodeId, String tpId, Short tcId) {
        return new DeleteDetnetNodeTrafficClassInputBuilder()
                .setTopologyId(topologyId)
                .setNodeId(nodeId)
                .setTpId(tpId)
                .setTcIndex(tcId)
                .build();
    }

    private ConfigDetnetNodeTrafficClassInput constructDetnetNodeTraffficClassInput(
            String topologyId, String nodeId, String tpId, Short tcId, Long maxDelay, Long minDelay) {
        return new ConfigDetnetNodeTrafficClassInputBuilder()
                .setTopologyId(topologyId)
                .setNodeId(nodeId)
                .setTpId(tpId)
                .setTcIndex(tcId)
                .setMaximumQueueDelay(maxDelay)
                .setMinimumQueueDelay(minDelay)
                .build();
    }

    private DeleteDetnetNodeLtpInput constructDeleteDetnetNodeLtpInput(
            String topologyId, String nodeId, String tpId) {
        return new DeleteDetnetNodeLtpInputBuilder()
                .setTopologyId(topologyId)
                .setNodeId(nodeId)
                .setTpId(tpId)
                .build();
    }

    private ConfigDetnetNodeLtpInput constructConfigDetnetNodeLtpInput(
            String topologyId, String nodeId, String tpId, String name, IpAddress prefix,
            long index, PreofType capability, DetnetEncapsulationType encapsulation) {
        return new ConfigDetnetNodeLtpInputBuilder()
                .setTopologyId(topologyId)
                .setNodeId(nodeId)
                .setTpId(tpId)
                .setIfName(name)
                .setTpIpPrefix(prefix)
                .setTpIndex(index)
                .setNodePreofCapability(capability)
                .setDetnetEncapsulationType(encapsulation)
                .build();
    }

    private ConfigDetnetNodeInput constructDetnetNode(String topologyId, String nodeId, String name, Long delay,
                                                      BigInteger latitude, BigInteger longitude, String ipv4,
                                                      String ipv6, boolean isRelayNode) {
        return new ConfigDetnetNodeInputBuilder()
                .setTopologyId(topologyId)
                .setNodeId(nodeId)
                .setName(name)
                .setProcessDelay(delay)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setIpv4Prefix(new Ipv4Prefix(ipv4))
                .setIpv6Prefix(new Ipv6Prefix(ipv6))
                .setIsRelayNode(isRelayNode)
                .build();
    }

    private DeleteDetnetNodeInput constructDeleteDetnetNodeInput(String topologyId, String nodeId) {
        return new DeleteDetnetNodeInputBuilder()
                .setTopologyId(topologyId)
                .setNodeId(nodeId)
                .build();
    }

    private void addDetnetTopology() {
        List<DetnetTopology> detnetTopologyList = new ArrayList<DetnetTopology>();
        DetnetTopology detnetTopology = new DetnetTopologyBuilder()
                .setTopologyId(TopologyId)
                .withKey(new DetnetTopologyKey(TopologyId))
                .build();
        detnetTopologyList.add(detnetTopology);

        DetnetNetworkTopology detnetNetworkTopology = new DetnetNetworkTopologyBuilder()
                .setDetnetTopology(detnetTopologyList)
                .build();

        final InstanceIdentifier<DetnetNetworkTopology> path = InstanceIdentifier.create(DetnetNetworkTopology.class);
        DataOperator.writeData(DataOperator.OperateType.PUT, getDataBroker(),
                path, detnetNetworkTopology);
    }

}
