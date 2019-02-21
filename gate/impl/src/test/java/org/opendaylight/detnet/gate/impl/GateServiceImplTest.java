/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.gate.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QueueTemplate;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QueueTemplateBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClassesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClassesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.GateConfigManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminControlList;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.admin.control.list.GateStates;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.manager.GateConfig;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.manager.GateConfigKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.link.LinkSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateServiceImplTest extends AbstractConcurrentDataBrokerTest {

    private static final Logger LOG = LoggerFactory.getLogger(GateServiceImplTest.class);
    private DataBroker dataBroker;
    private RpcConsumerRegistry rpcConsumerRegistry;
    private GateServiceImpl gateService;
    private static final String TOPOLOGY_ID = "test-detnet-topology";

    @Before
    public void init() {
        LOG.info("Init test context.");
        dataBroker = getDataBroker();
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        when(rpcConsumerRegistry.getRpcService(DetnetDriverApiService.class))
                .thenReturn(new DetnetDriverApiServiceMock());
        gateService = new GateServiceImpl(dataBroker, rpcConsumerRegistry);

        mockDetnetTopology();
        mockQosQueueTemplate();

    }



    @Test
    public void getServiceImplTest() {
        PathLink pathLink1 = new PathLinkBuilder()
                .setLinkId("1111")
                .setLinkSource(new LinkSourceBuilder().setSourceNode("0001").setSourceTp("fei-001").build())
                .build();
        PathLink pathLink2 = new PathLinkBuilder()
                .setLinkId("2222")
                .setLinkSource(new LinkSourceBuilder().setSourceNode("0002").setSourceTp("fei-002").build())
                .build();
        List<PathLink> pathLinkList = new ArrayList<>();
        pathLinkList.add(pathLink1);
        pathLinkList.add(pathLink2);
        ConfigE2eGateInput configE2eGateInput = new ConfigE2eGateInputBuilder()
                .setTopologyId(TOPOLOGY_ID)
                .setBandwidth(2600L)
                .setTrafficClass((short) 4)
                .setPathLink(pathLinkList)
                .build();
        gateService.configE2eGate(configE2eGateInput);

        InstanceIdentifier<GateConfig> gateConfigIID = InstanceIdentifier
                .create(GateConfigManager.class)
                .child(GateConfig.class, new GateConfigKey("0001","fei-001"));
        AdminControlList adminControlList = DataOperator
                .readData(dataBroker, gateConfigIID)
                .getAdminControlList();
        assertEquals(adminControlList.getTimeInterval().longValue(), 100000L);
        List<GateStates> gateList = adminControlList.getGateStates();
        assertEquals(1000, gateList.size());
        assertEquals((short) 159, gateList.get(0).getGateState().shortValue());
        assertEquals((short) 159, gateList.get(1).getGateState().shortValue());
        assertEquals((short) 159, gateList.get(2).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(3).getGateState().shortValue());

        configE2eGateInput = new ConfigE2eGateInputBuilder()
                .setTopologyId(TOPOLOGY_ID)
                .setBandwidth(3200L)
                .setTrafficClass((short) 6)
                .setPathLink(pathLinkList)
                .build();
        gateService.configE2eGate(configE2eGateInput);

        adminControlList = DataOperator
                .readData(dataBroker, gateConfigIID)
                .getAdminControlList();
        assertEquals(adminControlList.getTimeInterval().longValue(), 100000L);
        gateList = adminControlList.getGateStates();
        assertEquals(1000, gateList.size());
        assertEquals((short) 159, gateList.get(0).getGateState().shortValue());
        assertEquals((short) 159, gateList.get(1).getGateState().shortValue());
        assertEquals((short) 159, gateList.get(2).getGateState().shortValue());
        assertEquals((short) 207, gateList.get(3).getGateState().shortValue());
        assertEquals((short) 207, gateList.get(4).getGateState().shortValue());
        assertEquals((short) 207, gateList.get(5).getGateState().shortValue());
        assertEquals((short) 207, gateList.get(6).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(7).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(8).getGateState().shortValue());

        DeleteE2eGateInput deleteE2eGateInput = new DeleteE2eGateInputBuilder()
                .setTopologyId(TOPOLOGY_ID)
                .setBandwidth(2800L)
                .setTrafficClass((short) 6)
                .setPathLink(pathLinkList)
                .build();
        gateService.deleteE2eGate(deleteE2eGateInput);
        adminControlList = DataOperator
                .readData(dataBroker, gateConfigIID)
                .getAdminControlList();
        assertEquals(adminControlList.getTimeInterval().longValue(), 100000L);
        gateList = adminControlList.getGateStates();
        assertEquals(1000, gateList.size());
        assertEquals((short) 159, gateList.get(0).getGateState().shortValue());
        assertEquals((short) 159, gateList.get(1).getGateState().shortValue());
        assertEquals((short) 159, gateList.get(2).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(3).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(4).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(5).getGateState().shortValue());
        assertEquals((short) 207, gateList.get(6).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(7).getGateState().shortValue());
        assertEquals((short) 143, gateList.get(8).getGateState().shortValue());
    }

    private void mockDetnetTopology() {
        DetnetLink detnetLink = new DetnetLinkBuilder()
                .setKey(new DetnetLinkKey("1111"))
                .setLinkBandwidth(1000000L)
                .build();
        List<DetnetLink> linkList = new ArrayList<>();
        linkList.add(detnetLink);
        InstanceIdentifier<DetnetTopology> detnetTopologyIID = InstanceIdentifier
                .create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(TOPOLOGY_ID));
        DetnetTopology detnetTopology = new DetnetTopologyBuilder()
                .setKey(new DetnetTopologyKey(TOPOLOGY_ID))
                .setDetnetLink(linkList)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, detnetTopologyIID, detnetTopology);
    }

    private void mockQosQueueTemplate() {
        InstanceIdentifier<QueueTemplate> queueTemplateIID = InstanceIdentifier
                .create(QueueTemplate.class);
        TrafficClasses trafficClasses7 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 7))
                .setTrafficClass((short) 7)
                .setDetnet(false)
                .build();
        TrafficClasses trafficClasses6 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 6))
                .setTrafficClass((short) 6)
                .setDetnet(true)
                .build();
        TrafficClasses trafficClasses5 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 5))
                .setTrafficClass((short) 5)
                .setDetnet(true)
                .build();
        TrafficClasses trafficClasses4 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 4))
                .setTrafficClass((short) 4)
                .setDetnet(true)
                .build();
        TrafficClasses trafficClasses3 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 3))
                .setTrafficClass((short) 3)
                .setDetnet(false)
                .build();
        TrafficClasses trafficClasses2 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 2))
                .setTrafficClass((short) 2)
                .setDetnet(false)
                .build();
        TrafficClasses trafficClasses1 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 1))
                .setTrafficClass((short) 1)
                .setDetnet(false)
                .build();
        TrafficClasses trafficClasses0 = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey((short) 0))
                .setTrafficClass((short) 0)
                .setDetnet(false)
                .build();
        List<TrafficClasses> trafficClassesList = Arrays.asList(
                trafficClasses7,
                trafficClasses6,
                trafficClasses5,
                trafficClasses4,
                trafficClasses3,
                trafficClasses2,
                trafficClasses1,
                trafficClasses0);
        QueueTemplate queueTemplate = new QueueTemplateBuilder()
                .setTrafficClasses(trafficClassesList)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, queueTemplateIID, queueTemplate);
    }

    private class DetnetDriverApiServiceMock implements DetnetDriverApiService {

        @Override
        public Future<RpcResult<Void>> deleteDetnetServiceConfiguration(DeleteDetnetServiceConfigurationInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> writeGateConfigToSouth(WriteGateConfigToSouthInput input) {
            return RpcReturnUtil.returnSucess(null);
        }

        @Override
        public Future<RpcResult<Void>> deleteTsnServiceToSouth(DeleteTsnServiceToSouthInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> writeBandwidthToSouth(WriteBandwidthToSouthInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> writeTsnServiceToSouth(WriteTsnServiceToSouthInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> writeDetnetServiceConfiguration(WriteDetnetServiceConfigurationInput input) {
            return null;
        }
    }
}
