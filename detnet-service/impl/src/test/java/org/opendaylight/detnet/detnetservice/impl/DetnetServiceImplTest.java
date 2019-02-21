/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.detnetservice.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.CreateDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.CreateDetnetServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DeleteDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DeleteDetnetServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.DeviceDetnetServiceManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.Services;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.Nodes;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.nodes.DomainServiceKey;
import org.opendaylight.yangtools.yang.common.RpcResult;


public class DetnetServiceImplTest extends AbstractConcurrentDataBrokerTest {
    private DataBroker dataBroker;
    DetnetServiceImpl detnetServiceImpl;
    DetnetDriverApiService driverApiService = mock(DetnetDriverApiService.class);

    @Before
    public void setUp() throws Exception {
        dataBroker = getDataBroker();
        DetnetServiceDb.getInstance().setDataBroker(dataBroker);
        DetnetServiceDb.getInstance().setDriverApiService(driverApiService);
        detnetServiceImpl = DetnetServiceImpl.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        DetnetServiceDb.getInstance().distroy();
        DetnetServiceMockUtils.deleteDetnetTopology(dataBroker);
    }

    @Test
    public void checkCreateDetnetServiceInputTest() throws InterruptedException, ExecutionException {
        Future<RpcResult<Void>> result = detnetServiceImpl.createDetnetService(null);
        assertTrue(!result.get().isSuccessful());
        assertEquals("input information error!",result.get().getErrors().iterator().next().getMessage());
        CreateDetnetServiceInput input = new CreateDetnetServiceInputBuilder()
                .setStreamId(111L)
                .setDetnetPath(DetnetServiceMockUtils.buildNode14DetnetPath())
                .setClientFlow(DetnetServiceMockUtils.buildClientFlow())
                .setRelayNode(new ArrayList<>())
                .build();
        result = detnetServiceImpl.createDetnetService(input);
        assertTrue(!result.get().isSuccessful());
        assertEquals("input information error!",result.get().getErrors().iterator().next().getMessage());
    }

    @Test
    public void createDetnetServiceTest1() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        CreateDetnetServiceInput input = new CreateDetnetServiceInputBuilder()
                .setDomainId(1)
                .setStreamId(111L)
                .setDetnetPath(DetnetServiceMockUtils.buildNode14DetnetPath())
                .setClientFlow(DetnetServiceMockUtils.buildClientFlow())
                .setRelayNode(new ArrayList<>())
                .build();
        Future<RpcResult<Void>> result = detnetServiceImpl.createDetnetService(input);
        assertTrue(result.get().isSuccessful());
        Services services = DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(1,111L);
        assertEquals(2,services.getServiceProxyInstances().size());
        assertEquals(null,services.getServiceMappingInstances());
        DeviceDetnetServiceManager deviceService = DetnetServiceDb.getInstance().getDeviceService();
        assertEquals(2,deviceService.getNodes().size());
        for (Nodes node : deviceService.getNodes()) {
            if (node.getNodeId().equals("11.11.11.11")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else {
                assertTrue(node.getNodeId().equals("44.44.44.44"));
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(null,node.getDomainService().get(0).getDetnetTransportTunnels());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            }
        }
    }

    @Test
    public void createDetnetServiceTest2() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        Future<RpcResult<Void>> result = createDetnetServiceWithRelayNodes();
        assertTrue(result.get().isSuccessful());
        Services services = DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(1,111L);
        assertEquals(2,services.getServiceProxyInstances().size());
        assertEquals(1,services.getServiceMappingInstances().size());
        DeviceDetnetServiceManager deviceService = DetnetServiceDb.getInstance().getDeviceService();
        assertEquals(3,deviceService.getNodes().size());
        for (Nodes node : deviceService.getNodes()) {
            if (node.getNodeId().equals("11.11.11.11")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else if (node.getNodeId().equals("44.44.44.44")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(null,node.getDomainService().get(0).getDetnetTransportTunnels());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else {
                assertTrue(node.getNodeId().equals("22.22.22.22"));
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(null,node.getDomainService().get(0).getServiceProxyInstance());
                assertEquals(2,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(1,node.getDomainService().get(0).getServiceMappingInstance().size());
            }
        }
    }

    @Test
    public void createDetnetServiceTest3() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        DetnetServiceMockUtils.writeDetnetTopology(dataBroker);
        Future<RpcResult<Void>> result = createDetnetServiceWithRelayNodes1();
        assertTrue(result.get().isSuccessful());
        Services services = DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(1,111L);
        assertEquals(4,services.getServiceProxyInstances().size());
        assertEquals(2,services.getServiceMappingInstances().size());
        DeviceDetnetServiceManager deviceService = DetnetServiceDb.getInstance().getDeviceService();
        assertEquals(5,deviceService.getNodes().size());
        for (Nodes node : deviceService.getNodes()) {
            if (node.getNodeId().equals("11.11.11.11")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(2,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(2,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(2,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else if (node.getNodeId().equals("44.44.44.44")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(null,node.getDomainService().get(0).getDetnetTransportTunnels());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else if (node.getNodeId().equals("33.33.33.33")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(null,node.getDomainService().get(0).getServiceProxyInstance());
                assertEquals(2,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(1,node.getDomainService().get(0).getServiceMappingInstance().size());
            } else if (node.getNodeId().equals("55.55.55.55")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(null,node.getDomainService().get(0).getDetnetTransportTunnels());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else {
                assertTrue(node.getNodeId().equals("22.22.22.22"));
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(null,node.getDomainService().get(0).getServiceProxyInstance());
                assertEquals(2,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(1,node.getDomainService().get(0).getServiceMappingInstance().size());
            }
        }
    }

    @Test
    public void createDetnetServiceTest5() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        DetnetServiceMockUtils.writeDetnetTopology(dataBroker);
        CreateDetnetServiceInput input = new CreateDetnetServiceInputBuilder()
                .setDomainId(1)
                .setStreamId(111L)
                .setDetnetPath(DetnetServiceMockUtils.buildNode135SinglePath())
                .setClientFlow(DetnetServiceMockUtils.buildClientFlow())
                .setRelayNode(DetnetServiceMockUtils.buildRelayNodesForSeg())
                .build();
        Future<RpcResult<Void>> result = detnetServiceImpl.createDetnetService(input);
        assertTrue(result.get().isSuccessful());
        Services services = DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(1,111L);
        assertEquals(2,services.getServiceProxyInstances().size());
        assertEquals(1,services.getServiceMappingInstances().size());
        DeviceDetnetServiceManager deviceService = DetnetServiceDb.getInstance().getDeviceService();
        assertEquals(3,deviceService.getNodes().size());
        for (Nodes node : deviceService.getNodes()) {
            if (node.getNodeId().equals("11.11.11.11")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            } else if (node.getNodeId().equals("33.33.33.33")) {
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(null,node.getDomainService().get(0).getServiceProxyInstance());
                assertEquals(2,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetTransportTunnels().size());
                assertEquals(1,node.getDomainService().get(0).getServiceMappingInstance().size());
            } else {
                assertTrue(node.getNodeId().equals("55.55.55.55"));
                assertTrue(node.getDomainService().get(0).getKey().equals(new DomainServiceKey(1,111L)));
                assertEquals(1,node.getDomainService().get(0).getServiceProxyInstance().size());
                assertEquals(1,node.getDomainService().get(0).getDetnetFlows().size());
                assertEquals(null,node.getDomainService().get(0).getDetnetTransportTunnels());
                assertEquals(null,node.getDomainService().get(0).getServiceMappingInstance());
            }
        }
    }

    @Test
    public void createDetnetServiceTest4() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnErr("ERROR!"));
        Future<RpcResult<Void>> result = createDetnetServiceWithRelayNodes1();
        assertTrue(!result.get().isSuccessful());
        assertTrue(result.get().getErrors().iterator().next().getMessage().contains("ERROR!"));
    }

    @Test
    public void deleteDetnetServiceTest() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        when(driverApiService.deleteDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        Future<RpcResult<Void>> result = createDetnetServiceWithRelayNodes();
        assertTrue(result.get().isSuccessful());
        Services services = DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(1,111L);
        assertEquals(2,services.getServiceProxyInstances().size());
        assertEquals(1,services.getServiceMappingInstances().size());
        DeviceDetnetServiceManager deviceService = DetnetServiceDb.getInstance().getDeviceService();
        assertEquals(3,deviceService.getNodes().size());

        DeleteDetnetServiceInput deleteInput = new DeleteDetnetServiceInputBuilder()
                .setDomainId(1)
                .setStreamId(111L)
                .build();
        result = detnetServiceImpl.deleteDetnetService(deleteInput);
        assertTrue(result.get().isSuccessful());
        services = DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(1,111L);
        assertEquals(null,services);
        deviceService = DetnetServiceDb.getInstance().getDeviceService();
        assertEquals(3,deviceService.getNodes().size());
        for (Nodes node : deviceService.getNodes()) {
            assertTrue(node.getDomainService().isEmpty());
        }

        deleteInput = new DeleteDetnetServiceInputBuilder().setStreamId(1L).build();
        result = detnetServiceImpl.deleteDetnetService(deleteInput);
        assertTrue(!result.get().isSuccessful());
        assertEquals("input information error!",result.get().getErrors().iterator().next().getMessage());

        deleteInput = new DeleteDetnetServiceInputBuilder().setDomainId(2).setStreamId(22L).build();
        result = detnetServiceImpl.deleteDetnetService(deleteInput);
        assertTrue(result.get().isSuccessful());
    }

    @Test
    public void deleteDetnetServiceTest1() throws InterruptedException, ExecutionException {
        when(driverApiService.writeDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnSucess(null));
        when(driverApiService.deleteDetnetServiceConfiguration(any())).thenReturn(RpcReturnUtil.returnErr("ERROR!"));
        Future<RpcResult<Void>> result = createDetnetServiceWithRelayNodes();
        assertTrue(result.get().isSuccessful());

        DeleteDetnetServiceInput deleteInput = new DeleteDetnetServiceInputBuilder()
                .setDomainId(1)
                .setStreamId(111L)
                .build();
        result = detnetServiceImpl.deleteDetnetService(deleteInput);
        assertTrue(!result.get().isSuccessful());
        assertTrue(result.get().getErrors().iterator().next().getMessage().contains("ERROR!"));
    }

    private Future<RpcResult<Void>> createDetnetServiceWithRelayNodes() throws ExecutionException,
            InterruptedException {
        CreateDetnetServiceInput input = new CreateDetnetServiceInputBuilder()
                .setDomainId(1)
                .setStreamId(111L)
                .setDetnetPath(DetnetServiceMockUtils.buildNode14DetnetPath())
                .setClientFlow(DetnetServiceMockUtils.buildClientFlow())
                .setRelayNode(DetnetServiceMockUtils.buildRelayNodes())
                .build();
        return detnetServiceImpl.createDetnetService(input);
    }

    private Future<RpcResult<Void>> createDetnetServiceWithRelayNodes1() throws ExecutionException,
            InterruptedException {
        CreateDetnetServiceInput input = new CreateDetnetServiceInputBuilder()
                .setDomainId(1)
                .setStreamId(111L)
                .setDetnetPath(DetnetServiceMockUtils.buildNode145DetnetPath())
                .setClientFlow(DetnetServiceMockUtils.buildClientFlow())
                .setRelayNode(DetnetServiceMockUtils.buildRelayNodes())
                .build();
        return detnetServiceImpl.createDetnetService(input);
    }

}


