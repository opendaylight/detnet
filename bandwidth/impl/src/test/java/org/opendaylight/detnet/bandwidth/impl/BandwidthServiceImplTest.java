/*
 * Copyright (c) 2018 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.bandwidth.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.BandwidthConfigManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwidth.config.manager.BandwidthConfig;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwidth.config.manager.BandwidthConfigKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClassesKey;
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

public class BandwidthServiceImplTest extends AbstractConcurrentDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(BandwidthServiceImplTest.class);
    private static final String TOPOLOGY_ID = "default-detnet-topology";
    private BandwidthServiceImpl bandwidthService;
    private DataBroker dataBroker;
    private List<PathLink> linkList = new ArrayList<>();

    @Before
    public void init() {
        dataBroker = getDataBroker();
        RpcConsumerRegistry rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        when(rpcConsumerRegistry.getRpcService(DetnetDriverApiService.class))
                .thenReturn(new DetnetDriverApiServiceMock());

        bandwidthService = new BandwidthServiceImpl(getDataBroker(), rpcConsumerRegistry);

        DetnetLink detnetLink1 = new DetnetLinkBuilder()
                .setLinkId("1111")
                .setKey(new DetnetLinkKey("1111"))
                .setLinkSource(new LinkSourceBuilder().setSourceNode("0001").setSourceTp("fei-001").build())
                .setMaximumReservableBandwidth(1000000L)
                .setReservedDetnetBandwidth(0L)
                .setAvailableDetnetBandwidth(1000000L)
                .build();
        DetnetLink detnetLink2 = new DetnetLinkBuilder(detnetLink1)
                .setLinkId("2222")
                .setKey(new DetnetLinkKey("2222"))
                .setLinkSource(new LinkSourceBuilder().setSourceNode("0002").setSourceTp("fei-002").build())
                .setReservedDetnetBandwidth(100000L)
                .setAvailableDetnetBandwidth(900000L)
                .build();
        DetnetLink detnetLink3 = new DetnetLinkBuilder(detnetLink1)
                .setLinkId("3333")
                .setKey(new DetnetLinkKey("3333"))
                .setLinkSource(new LinkSourceBuilder().setSourceNode("0003").setSourceTp("fei-003").build())
                .setReservedDetnetBandwidth(200000L)
                .setAvailableDetnetBandwidth(800000L)
                .build();
        List<DetnetLink> detnetLinks = new ArrayList<>();
        detnetLinks.add(detnetLink1);
        detnetLinks.add(detnetLink2);
        detnetLinks.add(detnetLink3);
        DetnetTopology detnetTopology = new DetnetTopologyBuilder()
                .setTopologyId(TOPOLOGY_ID)
                .setDetnetLink(detnetLinks)
                .build();
        InstanceIdentifier<DetnetTopology> detnetTopologyIID = InstanceIdentifier
                .create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(TOPOLOGY_ID));
        DataOperator.writeData(DataOperator.OperateType.PUT,dataBroker,detnetTopologyIID,detnetTopology);


        linkList.add(new PathLinkBuilder(detnetLink1).build());
        linkList.add(new PathLinkBuilder(detnetLink2).build());
        linkList.add(new PathLinkBuilder(detnetLink3).build());
    }



    @Test
    public void configE2eServiceBandwidthTest() {

        LOG.info("Config e2e service bandwidth test start.");
        ConfigE2eBandwidthInput configE2eBandwidthInput = new ConfigE2eBandwidthInputBuilder()
                .setTopologyId(TOPOLOGY_ID)
                .setBandwidth(100000L)
                .setTrafficClass((short) 3)
                .setPathLink(linkList)
                .build();
        bandwidthService.configE2eBandwidth(configE2eBandwidthInput);
        InstanceIdentifier<TrafficClasses> trafficClassesIID = getTrafficClassesIID("0002", "fei-002");

        long bandwidthReserved = DataOperator.readData(dataBroker, trafficClassesIID).getReservedBandwidth();
        assertEquals(bandwidthReserved, 100000L);
        configE2eBandwidthInput = new ConfigE2eBandwidthInputBuilder(configE2eBandwidthInput)
                .setBandwidth(50000L)
                .build();
        bandwidthService.configE2eBandwidth(configE2eBandwidthInput);
        bandwidthReserved = DataOperator.readData(dataBroker, trafficClassesIID).getReservedBandwidth();
        assertEquals(bandwidthReserved, 150000L);

        InstanceIdentifier<DetnetLink> detnetLinkIID = getDetnetLinkIID("2222");
        long reservedDetnetBandwidth = DataOperator.readData(dataBroker, detnetLinkIID).getReservedDetnetBandwidth();
        long avaliableDetnetBandwidth = DataOperator.readData(dataBroker, detnetLinkIID).getAvailableDetnetBandwidth();
        assertEquals(reservedDetnetBandwidth, 250000L);
        assertEquals(avaliableDetnetBandwidth, 750000L);
        LOG.info("Config e2e service bandwidth test success.");

        LOG.info("Delete e2e service bandwidth test start.");
        DeleteE2eBandwidthInput deleteE2eBandwidthInput = new DeleteE2eBandwidthInputBuilder()
                .setTopologyId(TOPOLOGY_ID)
                .setBandwidth(60000L)
                .setTrafficClass((short) 3)
                .setPathLink(linkList)
                .build();
        bandwidthService.deleteE2eBandwidth(deleteE2eBandwidthInput);
        bandwidthReserved = DataOperator.readData(dataBroker, trafficClassesIID).getReservedBandwidth();
        assertEquals(bandwidthReserved, 90000L);

        detnetLinkIID = getDetnetLinkIID("3333");
        reservedDetnetBandwidth = DataOperator.readData(dataBroker, detnetLinkIID).getReservedDetnetBandwidth();
        avaliableDetnetBandwidth = DataOperator.readData(dataBroker, detnetLinkIID).getAvailableDetnetBandwidth();
        assertEquals(reservedDetnetBandwidth, 290000L);
        assertEquals(avaliableDetnetBandwidth, 710000L);
        LOG.info("Delete e2e service bandwidth test success.");
    }

    private InstanceIdentifier<TrafficClasses> getTrafficClassesIID(String nodeId, String tpId) {
        return InstanceIdentifier.create(BandwidthConfigManager.class)
                .child(BandwidthConfig.class, new BandwidthConfigKey(nodeId,tpId))
                .child(TrafficClasses.class, new TrafficClassesKey((short) 3));
    }

    private InstanceIdentifier<DetnetLink> getDetnetLinkIID(String linkId) {
        return InstanceIdentifier.create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(TOPOLOGY_ID))
                .child(DetnetLink.class, new DetnetLinkKey(linkId));
    }

    private class DetnetDriverApiServiceMock implements DetnetDriverApiService {

        @Override
        public Future<RpcResult<Void>> deleteDetnetServiceConfiguration(DeleteDetnetServiceConfigurationInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> writeGateConfigToSouth(WriteGateConfigToSouthInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> deleteTsnServiceToSouth(DeleteTsnServiceToSouthInput input) {
            return null;
        }

        @Override
        public Future<RpcResult<Void>> writeBandwidthToSouth(WriteBandwidthToSouthInput input) {
            return RpcReturnUtil.returnSucess(null);
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
