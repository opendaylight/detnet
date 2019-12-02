/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.e2eservice.impl;
/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;*/
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
/*
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.NodeDataBroker;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DetnetBandwidthApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.QueryBandwidthParameterInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.QueryBandwidthParameterOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.DetnetEncapsulationType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.FlowType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.flow.type.L2FlowIdentficationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.flow.type.L3FlowIdentificationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.ConfigE2eServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.ConfigE2eServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.ConfigE2eServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DeleteE2eServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DeleteE2eServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServicePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServicePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServicePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DetnetGateApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.QueryGateParameterInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.QueryGateParameterOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.ietf.ethertypes.rev181001.Ethertype;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.PceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.EgressBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.EgressKey;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.egress.Path;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.egress.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.PriorityTrafficClassMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QosMappingTemplate;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QosMappingTemplateBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QueueTemplate;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplates;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplatesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Ipv4Dscps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Pri8021ps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4Dscp;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4DscpBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4DscpKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021p;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021pBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021pKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClassesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClassesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.CreateDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DeleteDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DetnetServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.ResourcesPool;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.group.Listeners;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.group.ListenersBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.group.ListenersKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkDestBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Ltps;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DetnetTsnServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.QueryTsnServiceForwardingItemInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.QueryTsnServiceForwardingItemOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;*/

public class E2eServiceImplTest extends AbstractConcurrentDataBrokerTest {
    /*
    private static final Logger LOG = LoggerFactory.getLogger(E2eServiceImplTest.class);
    private static final String TOPOLOGY = "test-detnet-topolgoy";
    private static final String TEMPLATE = "test-qos-template";
    private DataBroker dataBroker;
    private E2eServiceImpl e2eService;
    private RpcConsumerRegistry rpcConsumerRegistry;
    private PceApiServiceMock pceApiServiceMock;
    private DetnetBandwidthServiceMock detnetBandwidthServiceMock;
    private DetnetGateServiceMock detnetGateServiceMock;
    private DetnetTsnServiceMock detnetTsnServiceMock;
    private DetnetServiceMock detnetServiceMock;
    private List<PathLink> pathLinkList1;
    private List<PathLink> pathLinkList2;
    private List<Listeners> listenersList;
    private FlowType flowType;
    private List<Egress> egressList;
    private ConfigE2eServiceInput configE2eServiceInput;

    @Before
    public void init() {
        LOG.info("init test context.");
        dataBroker = getDataBroker();
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        e2eService = new E2eServiceImpl(dataBroker, rpcConsumerRegistry);
        pceApiServiceMock = new PceApiServiceMock();
        when(rpcConsumerRegistry.getRpcService(PceApiService.class))
                .thenReturn(pceApiServiceMock);
        detnetBandwidthServiceMock = new DetnetBandwidthServiceMock();
        when(rpcConsumerRegistry.getRpcService(DetnetBandwidthApiService.class))
                .thenReturn(detnetBandwidthServiceMock);
        detnetGateServiceMock = new DetnetGateServiceMock();
        when(rpcConsumerRegistry.getRpcService(DetnetGateApiService.class))
                .thenReturn(detnetGateServiceMock);
        detnetTsnServiceMock = new DetnetTsnServiceMock();
        when(rpcConsumerRegistry.getRpcService(DetnetTsnServiceApiService.class))
                .thenReturn(detnetTsnServiceMock);
        detnetServiceMock = new DetnetServiceMock();
        when(rpcConsumerRegistry.getRpcService(DetnetServiceApiService.class))
                .thenReturn(detnetServiceMock);
        NodeDataBroker instanceMock = PowerMockito.mock(NodeDataBroker.class);
        Whitebox.setInternalState(NodeDataBroker.class, "instance", instanceMock);
        Mockito.doReturn(getDataBroker()).when(instanceMock).getNodeDataBroker(anyString());

        initTopology();
        initQosMappingTemplate();
        initQueueTemplate();
        initListenersAndFlowType();
    }

    /*
                  4                  12     15
              E3----R5           E9----B11----E13
           2 /        \ 6     9 / |     |
        1   /          \   8   /  |     |
    E1----B2           T7----R8   |11   |14
            \          /       \  |     |
            3\   5    /7     10 \ | 13  |  16
              E4----R6           E10---B12----E14
    */
    /*
    public void initTopology() {
        DetnetLink detnetLink1 = getDetnetLink("01", "01", "01-01", "02", "02-01");
        writeDetnetLink(TOPOLOGY, detnetLink1);
        DetnetLink detnetLink2 = getDetnetLink("02", "02", "02-02", "03", "03-01");
        writeDetnetLink(TOPOLOGY, detnetLink2);
        DetnetLink detnetLink3 = getDetnetLink("03", "02", "02-03", "04", "04-01");
        writeDetnetLink(TOPOLOGY, detnetLink3);
        DetnetLink detnetLink4 = getDetnetLink("04", "03", "03-02", "05", "05-01");
        writeDetnetLink(TOPOLOGY, detnetLink4);
        DetnetLink detnetLink5 = getDetnetLink("05", "04", "04-02", "06", "06-01");
        writeDetnetLink(TOPOLOGY, detnetLink5);
        DetnetLink detnetLink6 = getDetnetLink("06", "05", "05-02", "07", "07-01");
        writeDetnetLink(TOPOLOGY, detnetLink6);
        DetnetLink detnetLink7 = getDetnetLink("07", "06", "06-02", "07", "07-02");
        writeDetnetLink(TOPOLOGY, detnetLink7);
        DetnetLink detnetLink8 = getDetnetLink("08", "07", "07-03", "08", "08-01");
        writeDetnetLink(TOPOLOGY, detnetLink8);
        DetnetLink detnetLink9 = getDetnetLink("09", "08", "08-02", "09", "09-01");
        writeDetnetLink(TOPOLOGY, detnetLink9);
        DetnetLink detnetLink10 = getDetnetLink("10", "08", "08-03", "10", "10-01");
        writeDetnetLink(TOPOLOGY, detnetLink10);
        DetnetLink detnetLink11 = getDetnetLink("11", "09", "09-02", "10", "10-02");
        writeDetnetLink(TOPOLOGY, detnetLink11);
        DetnetLink detnetLink12 = getDetnetLink("12", "09", "09-03", "11", "11-01");
        writeDetnetLink(TOPOLOGY, detnetLink12);
        DetnetLink detnetLink13 = getDetnetLink("13", "10", "10-03", "12", "12-01");
        writeDetnetLink(TOPOLOGY, detnetLink13);
        DetnetLink detnetLink14 = getDetnetLink("14", "11", "11-03", "12", "12-03");
        writeDetnetLink(TOPOLOGY, detnetLink14);
        DetnetLink detnetLink15 = getDetnetLink("15", "11", "11-02", "13", "13-01");
        writeDetnetLink(TOPOLOGY, detnetLink15);
        DetnetLink detnetLink16 = getDetnetLink("16", "12", "12-02", "14", "14-01");
        writeDetnetLink(TOPOLOGY, detnetLink16);

        pathLinkList1 = getPathLinkList(detnetLink1, detnetLink2, detnetLink4, detnetLink6, detnetLink8,
                detnetLink9, detnetLink12, detnetLink15);
        pathLinkList2 = getPathLinkList(detnetLink1, detnetLink3, detnetLink5, detnetLink7, detnetLink8,
                detnetLink10, detnetLink13, detnetLink16);

        writeDetnetNodeTp(TOPOLOGY, "01", "01-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "02", "02-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "02", "02-02", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "02", "02-03", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "03", "03-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "03", "03-02", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "04", "04-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "04", "04-02", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "05", "05-01", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "05", "05-02", DetnetEncapsulationType.Ipv6);
        writeDetnetNodeTp(TOPOLOGY, "06", "06-01", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "06", "06-02", DetnetEncapsulationType.Ipv6);
        writeDetnetNodeTp(TOPOLOGY, "07", "07-01", DetnetEncapsulationType.Ipv6);
        writeDetnetNodeTp(TOPOLOGY, "07", "07-02", DetnetEncapsulationType.Ipv6);
        writeDetnetNodeTp(TOPOLOGY, "07", "07-03", DetnetEncapsulationType.Ipv6);
        writeDetnetNodeTp(TOPOLOGY, "08", "08-01", DetnetEncapsulationType.Ipv6);
        writeDetnetNodeTp(TOPOLOGY, "08", "08-02", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "08", "08-03", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "09", "09-01", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "09", "09-02", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "10", "10-01", DetnetEncapsulationType.Mpls);
        writeDetnetNodeTp(TOPOLOGY, "10", "10-02", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "11", "11-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "11", "11-02", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "12", "12-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "12", "12-02", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "13", "13-01", DetnetEncapsulationType.Tsn);
        writeDetnetNodeTp(TOPOLOGY, "14", "14-01", DetnetEncapsulationType.Tsn);
    }

    public void initQosMappingTemplate() {
        writePriorityMapping(TEMPLATE, "8021p", 0L, 3L, (short) 1);
        writePriorityMapping(TEMPLATE, "8021p", 4L, 5L, (short) 2);
        writePriorityMapping(TEMPLATE, "8021p", 6L, 7L, (short) 3);
        writePriorityMapping(TEMPLATE, "dscp", 0L, 7L, (short) 1);
        writePriorityMapping(TEMPLATE, "dscp", 8L, 15L, (short) 2);
        writePriorityMapping(TEMPLATE, "dscp", 16L, 63L, (short) 3);
        InstanceIdentifier<QosMappingTemplate> qosMappingTemplateIID = InstanceIdentifier
                .create(QosMappingTemplate.class);
        QosMappingTemplate qosMappingTemplate = new QosMappingTemplateBuilder()
                .setTemplateName(TEMPLATE)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, qosMappingTemplateIID, qosMappingTemplate);
    }

    public void initQueueTemplate() {
        writeQueueTempalte((short) 1, true);
        writeQueueTempalte((short) 2, true);
        writeQueueTempalte((short) 3, false);
    }

    public void initListenersAndFlowType() {
        listenersList = new ArrayList<>();
        Listeners listeners1 = new ListenersBuilder()
                .setKey(new ListenersKey("13", "13-01"))
                .setDestNode("13")
                .setDestTp("13-01")
                .setMaxLatency(10L)
                .setNumSeamlessTrees((short) 1)
                .build();
        listenersList.add(listeners1);

        Listeners listeners2 = new ListenersBuilder()
                .setKey(new ListenersKey("14", "14-01"))
                .setDestNode("14")
                .setDestTp("14-01")
                .setMaxLatency(20L)
                .setNumSeamlessTrees((short) 1)
                .build();
        listenersList.add(listeners2);
        flowType = new L2FlowIdentficationBuilder()
                .setVlanId(100)
                .setSourceMacAddress(new MacAddress("10:00:00:00:00:00"))
                .setDestinationMacAddress(new MacAddress("01:00:5e:10:00:00"))
                .setEthertype(Ethertype.Ipv4)
                .setPcp((short) 1)
                .build();
        egressList = new ArrayList<>();
        Path path1 = new PathBuilder()
                .setPathLink(pathLinkList1)
                .build();
        Egress outEgress1 = new EgressBuilder()
                .setEgressNodeId("13")
                .setKey(new EgressKey("13"))
                .setPath(path1)
                .build();
        egressList.add(outEgress1);
        Path path2 = new PathBuilder()
                .setPathLink(pathLinkList2)
                .build();
        Egress outEgress2 = new EgressBuilder()
                .setEgressNodeId("14")
                .setKey(new EgressKey("14"))
                .setPath(path2)
                .build();
        egressList.add(outEgress2);

        configE2eServiceInput = new ConfigE2eServiceInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(1L)
                .setSourceNode("01")
                .setSourceTp("01-01")
                .setListeners(listenersList)
                .setInterval(250L)
                .setMaxPacketsPerInterval(1000L)
                .setMaxPayloadSize(1588L)
                .setMaxLatency(25L)
                .setFlowType(flowType)
                .build();
    }

    @Test
    public void testConfigE2eService() throws ExecutionException, InterruptedException {
        ConfigE2eServiceInput input = new ConfigE2eServiceInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(2L)
                .build();
        Future<RpcResult<ConfigE2eServiceOutput>> futureOutput = e2eService.configE2eService(input);
        ConfigureResult configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("Config e2e service input error.", configureResult.getErrorCause());
    }

    @Test
    public void testConfigE2eService1() throws ExecutionException, InterruptedException {
        flowType = new L2FlowIdentficationBuilder()
                .setVlanId(100)
                .setSourceMacAddress(new MacAddress("10:00:00:00:00:00"))
                .setDestinationMacAddress(new MacAddress("01:00:5e:10:00:00"))
                .setEthertype(Ethertype.Ipv4)
                .setPcp((short) 6)
                .build();
        configE2eServiceInput = new ConfigE2eServiceInputBuilder(configE2eServiceInput)
                .setStreamId(3L)
                .setFlowType(flowType)
                .build();
        Future<RpcResult<ConfigE2eServiceOutput>> futureOutput = e2eService.configE2eService(configE2eServiceInput);
        ConfigureResult configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("Traffic class queue is not detnet.", configureResult.getErrorCause());
    }

    @Test
    public void testConfigE2eService2() throws ExecutionException, InterruptedException {
        configE2eServiceInput = new ConfigE2eServiceInputBuilder(configE2eServiceInput)
                .setInterval(250L)
                .setMaxPacketsPerInterval(0L)
                .build();
        Future<RpcResult<ConfigE2eServiceOutput>> futureOutput = e2eService.configE2eService(configE2eServiceInput);
        ConfigureResult configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("Input traffic specification error.", configureResult.getErrorCause());
    }

    @Test
    public void testConfigE2eService3() throws ExecutionException, InterruptedException {
        configE2eServiceInput = new ConfigE2eServiceInputBuilder(configE2eServiceInput)
                .setMaxLatency(0L)
                .build();
        Future<RpcResult<ConfigE2eServiceOutput>> futureOutput = e2eService.configE2eService(configE2eServiceInput);
        ConfigureResult configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("Input max latency not specified correctly.", configureResult.getErrorCause());
    }

    @Test
    public void testConfigQueryAndDeleteE2eService() throws ExecutionException, InterruptedException {



        LOG.info("Config e2e service input: " + configE2eServiceInput.toString());
        Future<RpcResult<ConfigE2eServiceOutput>> futureOutput = e2eService.configE2eService(configE2eServiceInput);
        ConfigureResult configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("SUCCESS", configureResult.getResult().getName());
        futureOutput = e2eService.configE2eService(configE2eServiceInput);
        configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("E2e service already exist.", configureResult.getErrorCause());

        InstanceIdentifier<ResourcesPool> resourcesPoolIID = InstanceIdentifier
                .create(ResourcesPool.class);
        ResourcesPool resourcesPool = DataOperator.readData(dataBroker, resourcesPoolIID);
        assertNotNull(resourcesPool);
        assertEquals((Long) 2L, resourcesPool.getClientFlowId());
        assertEquals((Integer) 10, resourcesPool.getVlanId());
        assertEquals("01:00:5e:00:01:01", resourcesPool.getGroupMacAddress());

        flowType = new L3FlowIdentificationBuilder()
                .setIpFlowType(new Ipv4Builder().setDscp((short) 1).build())
                .build();
        configE2eServiceInput = new ConfigE2eServiceInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(2L)
                .setSourceNode("01")
                .setSourceTp("01-01")
                .setListeners(listenersList)
                .setInterval(250L)
                .setMaxPacketsPerInterval(1000L)
                .setMaxPayloadSize(1588L)
                .setMaxLatency(25L)
                .setFlowType(flowType)
                .build();
        futureOutput = e2eService.configE2eService(configE2eServiceInput);
        configureResult = futureOutput.get().getResult().getConfigureResult();
        assertEquals("SUCCESS", configureResult.getResult().getName());
        resourcesPool = DataOperator.readData(dataBroker, resourcesPoolIID);
        assertNotNull(resourcesPool);
        assertEquals((Long) 3L, resourcesPool.getClientFlowId());
        assertEquals((Integer) 10, resourcesPool.getVlanId());
        assertEquals("01:00:5e:00:01:02", resourcesPool.getGroupMacAddress());

        LOG.info("Test query e2e service path of streamId: 1");
        QueryE2eServicePathInput queryE2eServicePathInput = new QueryE2eServicePathInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(1L)
                .build();
        QueryE2eServicePathOutput queryE2eServicePathOutput = e2eService.queryE2eServicePath(
                queryE2eServicePathInput).get().getResult();
        assertEquals("SUCCESS", queryE2eServicePathOutput.getConfigureResult().getResult().getName());
        assertEquals(14, queryE2eServicePathOutput.getLinks().size());

        LOG.info("Test delete e2e service of streamId: 1");
        DeleteE2eServiceInput deleteE2eServiceInput = new DeleteE2eServiceInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(1L)
                .build();
        configureResult = e2eService.deleteE2eService(deleteE2eServiceInput).get()
                .getResult().getConfigureResult();
        assertEquals("SUCCESS", configureResult.getResult().getName());
    }

    @Test
    public void testDeleteE2eService() throws ExecutionException, InterruptedException {
        DeleteE2eServiceInput deleteE2eServiceInput = new DeleteE2eServiceInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(1L)
                .build();
        ConfigureResult configureResult = e2eService.deleteE2eService(deleteE2eServiceInput).get()
                .getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("E2e service not exist.", configureResult.getErrorCause());
    }

    @Test
    public void testDeleteE2eService1() throws ExecutionException, InterruptedException {
        DeleteE2eServiceInput deleteE2eServiceInput = new DeleteE2eServiceInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .build();
        ConfigureResult configureResult = e2eService.deleteE2eService(deleteE2eServiceInput).get()
                .getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("Delete e2e service input error.", configureResult.getErrorCause());

    }

    @Test
    public void testQueryE2eServicePath() throws ExecutionException, InterruptedException {
        QueryE2eServicePathInput queryE2eServicePathInput = new QueryE2eServicePathInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .build();
        ConfigureResult configureResult = e2eService.queryE2eServicePath(queryE2eServicePathInput).get()
                .getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("Query e2e service path input error.", configureResult.getErrorCause());
    }

    @Test
    public void testQeuryE2eServicePath1() throws ExecutionException, InterruptedException {
        QueryE2eServicePathInput queryE2eServicePathInput = new QueryE2eServicePathInputBuilder()
                .setTopologyId(TOPOLOGY)
                .setDomainId(1)
                .setStreamId(2L)
                .build();
        ConfigureResult configureResult = e2eService.queryE2eServicePath(queryE2eServicePathInput).get()
                .getResult().getConfigureResult();
        assertEquals("FAILURE", configureResult.getResult().getName());
        assertEquals("E2e service not exist.", configureResult.getErrorCause());
    }

    private class PceApiServiceMock implements PceApiService {

        @Override
        public Future<RpcResult<QueryPathOutput>> queryPath(QueryPathInput input) {
            QueryPathOutput queryPathOutput = new QueryPathOutputBuilder()
                    .setIngressNodeId("01")
                    .setEgress(egressList)
                    .build();
            return RpcReturnUtil.returnSucess(queryPathOutput);
        }

        @Override
        public Future<RpcResult<CreatePathOutput>> createPath(CreatePathInput input) {
            CreatePathOutput createPathOutput = new CreatePathOutputBuilder()
                    .setStreamId(input.getStreamId())
                    .setIngressNodeId(input.getIngressNodeId())
                    .setEgress(egressList)
                    .build();
            return RpcReturnUtil.returnSucess(createPathOutput);
        }

        @Override
        public Future<RpcResult<RemovePathOutput>> removePath(RemovePathInput input) {
            RemovePathOutput removePathOutput = new RemovePathOutputBuilder()
                    .setIngressNodeId("01")
                    .setEgress(null)
                    .build();
            return RpcReturnUtil.returnSucess(removePathOutput);
        }
    }

    private class DetnetBandwidthServiceMock implements DetnetBandwidthApiService {

        @Override
        public Future<RpcResult<QueryBandwidthParameterOutput>> queryBandwidthParameter(
                QueryBandwidthParameterInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<ConfigE2eBandwidthOutput>> configE2eBandwidth(ConfigE2eBandwidthInput input) {
            return RpcReturnUtil.returnSucess(new ConfigE2eBandwidthOutputBuilder().build());
        }

        @Override
        public Future<RpcResult<DeleteE2eBandwidthOutput>> deleteE2eBandwidth(DeleteE2eBandwidthInput input) {
            return RpcReturnUtil.returnSucess(new DeleteE2eBandwidthOutputBuilder().build());
        }
    }

    private class DetnetGateServiceMock implements DetnetGateApiService {

        @Override
        public Future<RpcResult<DeleteE2eGateOutput>> deleteE2eGate(DeleteE2eGateInput input) {
            return RpcReturnUtil.returnSucess(new DeleteE2eGateOutputBuilder().build());
        }

        @Override
        public Future<RpcResult<QueryGateParameterOutput>> queryGateParameter(QueryGateParameterInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<ConfigE2eGateOutput>> configE2eGate(ConfigE2eGateInput input) {
            return RpcReturnUtil.returnSucess(new ConfigE2eGateOutputBuilder().build());
        }
    }

    private class DetnetTsnServiceMock implements DetnetTsnServiceApiService {

        @Override
        public Future<RpcResult<DeleteTsnServiceOutput>> deleteTsnService(DeleteTsnServiceInput input) {
            return RpcReturnUtil.returnSucess(new DeleteTsnServiceOutputBuilder().build());
        }

        @Override
        public Future<RpcResult<ConfigTsnServiceOutput>> configTsnService(ConfigTsnServiceInput input) {
            return RpcReturnUtil.returnSucess(new ConfigTsnServiceOutputBuilder().build());
        }

        @Override
        public Future<RpcResult<QueryTsnServiceForwardingItemOutput>> queryTsnServiceForwardingItem(
                QueryTsnServiceForwardingItemInput input) {
            return null;
        }
    }

    private class DetnetServiceMock implements DetnetServiceApiService {

        @Override
        public Future<RpcResult<Void>> deleteDetnetService(DeleteDetnetServiceInput input) {
            return RpcReturnUtil.returnSucess(null);
        }

        @Override
        public Future<RpcResult<Void>> createDetnetService(CreateDetnetServiceInput input) {
            return RpcReturnUtil.returnSucess(null);
        }
    }



    private InstanceIdentifier<DetnetTopology> getTopologyIID(String topologyId) {
        return InstanceIdentifier
                .create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(topologyId));
    }

    private void writeDetnetLink(String topologyId, DetnetLink detnetLink) {
        InstanceIdentifier<DetnetLink> detnetLinkIID = getTopologyIID(topologyId)
                .child(DetnetLink.class, new DetnetLinkKey(detnetLink.getLinkId()));
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, detnetLinkIID, detnetLink);
    }

    private DetnetLink getDetnetLink(String linkId, String srcNode, String srcTp, String destNode, String destTp) {
        return new DetnetLinkBuilder()
                .setKey(new DetnetLinkKey(linkId))
                .setLinkId(linkId)
                .setLinkSource(new LinkSourceBuilder().setSourceNode(srcNode).setSourceTp(srcTp).build())
                .setLinkDest(new LinkDestBuilder().setDestNode(destNode).setDestTp(destTp).build())
                .setMaximumReservableBandwidth(0L)
                .setAvailableDetnetBandwidth(0L)
                .setReservedDetnetBandwidth(0L)
                .build();
    }

    private void writeDetnetNodeTp(String topologyId, String nodeId, String tpId, DetnetEncapsulationType type) {
        InstanceIdentifier<Ltps> ltpsIID = getTopologyIID(topologyId)
                .child(DetnetNode.class, new DetnetNodeKey(nodeId))
                .child(Ltps.class, new LtpsKey(tpId));
        Ltps ltps = new LtpsBuilder()
                .setKey(new LtpsKey(tpId))
                .setTpId(tpId)
                .setDetnetEncapsulationType(type)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, ltpsIID, ltps);
    }

    private List<PathLink> getPathLinkList(DetnetLink... detnetLinks) {
        List<PathLink> pathLinkList = new ArrayList<>();
        for (DetnetLink detnetLink : detnetLinks) {
            PathLink pathLink = new PathLinkBuilder()
                    .setLinkId(detnetLink.getLinkId())
                    .setLinkSource(detnetLink.getLinkSource())
                    .setLinkDest(detnetLink.getLinkDest())
                    .build();
            pathLinkList.add(pathLink);
        }
        return pathLinkList;
    }

    private void writePriorityMapping(String templateName, String type, long value1, long value2, short trafficClass) {
        if (type.equals("8021p")) {
            for (long value = value1;value <= value2;value++) {
                InstanceIdentifier<Pri8021p> pri8021pIID = InstanceIdentifier
                        .create(PriorityTrafficClassMapping.class)
                        .child(MappingTemplates.class, new MappingTemplatesKey(templateName))
                        .child(Pri8021ps.class)
                        .child(Pri8021p.class, new Pri8021pKey(value));
                Pri8021p pri8021p = new Pri8021pBuilder()
                        .setKey(new Pri8021pKey(value))
                        .setValue8021p(value)
                        .setTrafficClass(trafficClass)
                        .build();
                DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, pri8021pIID, pri8021p);
            }

        } else {
            for (long value = value1;value <= value2;value++) {
                InstanceIdentifier<Ipv4Dscp> ipv4DscpIID = InstanceIdentifier
                        .create(PriorityTrafficClassMapping.class)
                        .child(MappingTemplates.class, new MappingTemplatesKey(templateName))
                        .child(Ipv4Dscps.class)
                        .child(Ipv4Dscp.class, new Ipv4DscpKey(value));
                Ipv4Dscp ipv4Dscp = new Ipv4DscpBuilder()
                        .setKey(new Ipv4DscpKey(value))
                        .setDscpValue(value)
                        .setTrafficClass(trafficClass)
                        .build();
                DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, ipv4DscpIID, ipv4Dscp);
            }
        }
    }

    private void writeQueueTempalte(short trafficClass, boolean isDetnet) {
        InstanceIdentifier<TrafficClasses> trafficClassesIID = InstanceIdentifier
                .create(QueueTemplate.class)
                .child(TrafficClasses.class, new TrafficClassesKey(trafficClass));
        TrafficClasses trafficClasses = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey(trafficClass))
                .setTrafficClass(trafficClass)
                .setDetnet(isDetnet)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, trafficClassesIID, trafficClasses);
    }*/
}
