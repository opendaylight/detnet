/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.driver.impl;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.anyString;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
//import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
//import org.opendaylight.detnet.common.util.NodeDataBroker;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.flow.type.L3FlowIdentificationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.write.gate.config.to.south.input.GateConfigParams;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.write.gate.config.to.south.input.GateConfigParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.DetnetConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.BandwidthConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.DetnetServiceConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.GateConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.TsnServiceConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.bandwidth.configuration.BandwidthConfigList;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.bandwidth.configuration.BandwidthConfigListKey;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.detnet.service.configuration.Service;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.detnet.service.configuration.ServiceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.gate.configuration.GateConfigList;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.gate.configuration.GateConfigListKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlow;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.client.flow.TrafficSpecificationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.flow.identification.flow.type.MPLSBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.DetnetServices;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.DetnetServicesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetFlows;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetTransportTunnels;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetTransportTunnelsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceProxyInstance;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceProxyInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.transport.tunnel.tunnel.type.mpls.MplsEncapsulationBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.mpls.tunnel.MplsTunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.vpn.VpnBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClassesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemList;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetnetDriverServiceImplTest extends AbstractConcurrentDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(DetnetDriverServiceImplTest.class);
    private DriverServiceImpl driverService;

    @Before
    public void init() {

        //NodeDataBroker instanceMock = PowerMockito.mock(NodeDataBroker.class);
        //Whitebox.setInternalState(NodeDataBroker.class, "instance", instanceMock);
       // Mockito.doReturn(getDataBroker()).when(instanceMock).getNodeDataBroker(anyString());

        driverService = new DriverServiceImpl();
    }

    @Test
    public void writeGateConfigToSouthTest() {
        LOG.info("Test write gate config to south.");
        GateConfigParams gateConfigParams = new GateConfigParamsBuilder()
                .setTpId("001")
                .setAdminControlListLength(1000L)
                .build();
        WriteGateConfigToSouthInput writeGateConfigToSouthInput = new WriteGateConfigToSouthInputBuilder()
                .setNodeId("any-node")
                .setGateConfigParams(gateConfigParams)
                .build();
        driverService.writeGateConfigToSouth(writeGateConfigToSouthInput);
        InstanceIdentifier<GateConfigList> gateConfigListIID = InstanceIdentifier
                .create(DetnetConfiguration.class)
                .child(GateConfiguration.class)
                .child(GateConfigList.class, new GateConfigListKey("001"));
        GateConfigList gateConfigList = DataOperator.readData(getDataBroker(), gateConfigListIID);
        //assertNotNull(gateConfigList);
        //assertEquals((Long) 1000L, gateConfigList.getAdminControlListLength());
    }

    @Test
    public void writeBandwidthConfigurationToSouthTest() {
        LOG.info("Test write bandwidth config to south.");
        WriteBandwidthToSouthInput writeBandwidthToSouthInput = new WriteBandwidthToSouthInputBuilder()
                .setNodeId("any-node")
                .setTpId("002")
                .setTrafficClass((short) 1)
                .setReservedBandwidth(10000L)
                .build();
        driverService.writeBandwidthToSouth(writeBandwidthToSouthInput);
        InstanceIdentifier<TrafficClasses> trafficClassesIID = InstanceIdentifier
                .create(DetnetConfiguration.class)
                .child(BandwidthConfiguration.class)
                .child(BandwidthConfigList.class, new BandwidthConfigListKey("002"))
                .child(TrafficClasses.class, new TrafficClassesKey((short) 1));
        TrafficClasses trafficClasses = DataOperator.readData(getDataBroker(), trafficClassesIID);
        //assertNotNull(trafficClasses);
        //assertEquals((Long) 10000L, trafficClasses.getReservedBandwidth());
    }

    @Test
    public void writeAndDeleteTsnServiceToSouthTest() {
        LOG.info("Test write tsn service to south.");
        List<String> outPorts = new ArrayList<String>();
        outPorts.add("003");
        outPorts.add("004");
        WriteTsnServiceToSouthInput writeTsnServiceToSouthInput = new WriteTsnServiceToSouthInputBuilder()
                .setNodeId("any-node")
                .setVlanId(1)
                .setGroupMacAddress("01:00:5e:01:00:00")
                .setOutPorts(outPorts)
                .build();
        driverService.writeTsnServiceToSouth(writeTsnServiceToSouthInput);
        InstanceIdentifier<ForwardingItemList> forwardingItemListIID = InstanceIdentifier
                .create(DetnetConfiguration.class)
                .child(TsnServiceConfiguration.class)
                .child(ForwardingItemList.class,
                        new ForwardingItemListKey("01:00:5e:01:00:00", 1));
        //ForwardingItemList forwardingItemList = DataOperator.readData(getDataBroker(), forwardingItemListIID);
        //assertNotNull(forwardingItemList);
        //assertEquals(2, forwardingItemList.getOutPorts().size());

        outPorts.add("005");
        writeTsnServiceToSouthInput = new WriteTsnServiceToSouthInputBuilder(writeTsnServiceToSouthInput)
                .setOutPorts(outPorts)
                .build();
        driverService.writeTsnServiceToSouth(writeTsnServiceToSouthInput);
        //forwardingItemList = DataOperator.readData(getDataBroker(), forwardingItemListIID);
        //assertNotNull(forwardingItemList);
        //assertEquals(3, forwardingItemList.getOutPorts().size());

        DeleteTsnServiceToSouthInput deleteTsnServiceToSouthInput = new DeleteTsnServiceToSouthInputBuilder()
                .setNodeId("any-node")
                .setGroupMacAddress("01:00:5e:01:00:00")
                .setVlanId(1)
                .build();
        driverService.deleteTsnServiceToSouth(deleteTsnServiceToSouthInput);
        //forwardingItemList = DataOperator.readData(getDataBroker(), forwardingItemListIID);
        //assertEquals(null, forwardingItemList);
    }

    @Test
    public void writeDetnetServiceConfigToSouthTest() throws ExecutionException, InterruptedException {
        WriteDetnetServiceConfigurationInput input = new WriteDetnetServiceConfigurationInputBuilder()
                .setNodeId("node1").build();
        ListenableFuture<RpcResult<WriteDetnetServiceConfigurationOutput>> result =
                driverService.writeDetnetServiceConfiguration(input);
        assertTrue(!result.get().isSuccessful());
        assertEquals("Input error!",result.get().getErrors().iterator().next().getMessage());

        input = new WriteDetnetServiceConfigurationInputBuilder()
                .setNodeId("node1")
                .setStreamId(111L)
                .setServiceProxyInstance(buildServiceProxyInstance())
                .setClientFlow(buildClientFlow())
                .setDetnetFlows(buildDetnetFlow())
                .setDetnetTransportTunnels(buildDetnetTransportTunnels())
                .build();
        result = driverService.writeDetnetServiceConfiguration(input);
        //assertTrue(result.get().isSuccessful());

        InstanceIdentifier<Service> path = buildDetnetServicepath(111L);
        Service serviceInfo = DataOperator.readData(getDataBroker(),path);
        //assertNotNull(serviceInfo);
        //assertEquals((Long)111L,serviceInfo.getStreamId());
        //assertEquals(1,serviceInfo.getClientFlow().size());
        //assertEquals(1,serviceInfo.getDetnetFlows().size());
        //assertEquals(1,serviceInfo.getDetnetTransportTunnels().size());
       // assertEquals(1,serviceInfo.getServiceProxyInstance().size());
    }

    @Test
    public void deleteDetnetServiceConfigToSouthTest() throws ExecutionException, InterruptedException {
        DeleteDetnetServiceConfigurationInput input = new DeleteDetnetServiceConfigurationInputBuilder()
                .setNodeId("node1").build();
        ListenableFuture<RpcResult<DeleteDetnetServiceConfigurationOutput>> result =
                driverService.deleteDetnetServiceConfiguration(input);
        assertTrue(!result.get().isSuccessful());
        assertEquals("Input error!",result.get().getErrors().iterator().next().getMessage());

        WriteDetnetServiceConfigurationInput writeInput = new WriteDetnetServiceConfigurationInputBuilder()
                .setNodeId("node1")
                .setStreamId(111L)
                .setServiceProxyInstance(buildServiceProxyInstance())
                .setClientFlow(buildClientFlow())
                .setDetnetFlows(buildDetnetFlow())
                .setDetnetTransportTunnels(buildDetnetTransportTunnels())
                .build();
        ListenableFuture<RpcResult<WriteDetnetServiceConfigurationOutput>> result1 =
                driverService.writeDetnetServiceConfiguration(writeInput);
        //assertTrue(result1.get().isSuccessful());

        DeleteDetnetServiceConfigurationInput deleteInput = new DeleteDetnetServiceConfigurationInputBuilder()
                .setNodeId("node1")
                .setStreamId(111L)
                .build();
        result = driverService.deleteDetnetServiceConfiguration(deleteInput);
        //assertTrue(result.get().isSuccessful());

        InstanceIdentifier<Service> path = buildDetnetServicepath(111L);
        Service serviceInfo = DataOperator.readData(getDataBroker(),path);
        //assertTrue(serviceInfo == null);
    }

    private List<DetnetTransportTunnels> buildDetnetTransportTunnels() {
        List<DetnetTransportTunnels> tunnels = new ArrayList<DetnetTransportTunnels>();
        tunnels.add(new DetnetTransportTunnelsBuilder()
                .setTransportTunnelId(1L)
                .setTunnelType(new org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet
                        .transport.tunnel.tunnel.type.MPLSBuilder()
                        .setMplsEncapsulation(new MplsEncapsulationBuilder()
                                .setMplsTunnel(new MplsTunnelBuilder()
                                        .setSourceNodeId("node1")
                                        .setDestNodeId("node2")
                                        .setDestRouterId(new Ipv4Address("2.2.2.2"))
                                        .setExplicitPath(null)
                                        .build())
                                .build())
                        .build())
                .build());
        return tunnels;
    }

    private List<DetnetFlows> buildDetnetFlow() {
        List<DetnetFlows> detnetFlows = new ArrayList<DetnetFlows>();
        detnetFlows.add(new DetnetFlowsBuilder()
                .setDetnetFlowId(1L)
                .setFlowType(new MPLSBuilder()
                        .setVpn(new VpnBuilder()
                                .setLocal(new Ipv4Address("1.1.1.1"))
                                .setPeer(new Ipv4Address("2.2.2.2"))
                                .build())
                        .build())
                .build());
        return detnetFlows;
    }

    private List<ClientFlow> buildClientFlow() {
        List<ClientFlow> clientFlows = new ArrayList<ClientFlow>();
        clientFlows.add(new ClientFlowBuilder()
                .setClientFlowId(111L)
                .setFlowType(new L3FlowIdentificationBuilder()
                        .setIpFlowType(new Ipv4Builder()
                                .setSrcIpv4Address(new Ipv4Address("1.1.1.1"))
                                .setDestIpv4Address(new Ipv4Address("2.2.2.2"))
                                .setDscp((short) 23)
                                .build())
                        .setSourcePort(new PortNumber(67))
                        .setDestinationPort(new PortNumber(68))
                        .setProtocol((short) 15)
                        .build())
                .setTrafficSpecification(new TrafficSpecificationBuilder()
                        .setInterval(1000L)
                        .setMaxPacketsPerInterval(8000L)
                        .setMaxPayloadSize(128L)
                        .build())
                .build());
        return clientFlows;
    }

    private List<ServiceProxyInstance> buildServiceProxyInstance() {
        List<ServiceProxyInstance> proxyInstances = new ArrayList<ServiceProxyInstance>();
        List<Uint32> clientFlowIds = new ArrayList<Uint32>();
        int clientFlowId = 1;
        clientFlowIds.add(Uint32.valueOf(clientFlowId));
        List<DetnetServices> detnetServices = new ArrayList<DetnetServices>();
        detnetServices.add(new DetnetServicesBuilder().setDetnetFlowId(1L).setDetnetTransportId(1L).build());

        proxyInstances.add(new ServiceProxyInstanceBuilder()
                .setServiceProxyInstanceId(1L)
                .setClientFlowId(clientFlowIds)
                .setDetnetServices(detnetServices)
                .build());
        return proxyInstances;
    }

    private InstanceIdentifier<Service> buildDetnetServicepath(Long streamId) {
        return InstanceIdentifier.create(DetnetConfiguration.class)
                .child(DetnetServiceConfiguration.class)
                .child(Service.class,new ServiceKey(streamId));
    }

}
