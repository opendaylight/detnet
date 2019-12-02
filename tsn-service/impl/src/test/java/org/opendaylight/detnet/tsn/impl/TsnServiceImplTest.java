/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.tsn.impl;
/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;*/
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
/*
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemList;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.tsn.service.manager.TsnService;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItems;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItemsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItemsKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;*/

public class TsnServiceImplTest extends AbstractConcurrentDataBrokerTest {
    /*
    private static final Logger LOG = LoggerFactory.getLogger(TsnServiceImplTest.class);
    private DataBroker dataBroker;
    private TsnServiceImpl tsnServiceImpl;
    private RpcConsumerRegistry rpcConsumerRegistry;


    @Before
    public void init() {
        dataBroker = getDataBroker();
        rpcConsumerRegistry = mock(RpcConsumerRegistry.class);
        when(rpcConsumerRegistry.getRpcService(DetnetDriverApiService.class))
                .thenReturn(new DetnetDriverApiServiceMock());
        tsnServiceImpl = new TsnServiceImpl(dataBroker, rpcConsumerRegistry);
        mockDataForQueryAndDelete("07", "01:00:5e:00:02:00", 1, "fei-01", "fei-02");
        mockDataForQueryAndDelete("08", "01:00:5e:00:02:00", 1, "fei-01");
    }

    @Test
    public void nullInputTest() {
        ConfigTsnServiceInput configTsnServiceInput = new ConfigTsnServiceInputBuilder()
                .setGroupMacAddress("")
                .setVlanId(1)
                .build();
        try {
            boolean configResult = tsnServiceImpl.configTsnService(configTsnServiceInput).get().isSuccessful();
            assertEquals(false, configResult);
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }

        DeleteTsnServiceInput deleteTsnServiceInput = new DeleteTsnServiceInputBuilder()
                .setVlanId(1)
                .setGroupMacAddress("")
                .setTsnNodes(null)
                .build();
        try {
            boolean deleteResult = tsnServiceImpl.deleteTsnService(deleteTsnServiceInput).get().isSuccessful();
            assertEquals(false, deleteResult);
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
        LOG.info("Null input test success.");
    }

    @Test
    public void configTsnServiceTest() {
        List<TsnForwardingItems> forwardingItemsList = new ArrayList<TsnForwardingItems>();
        TsnForwardingItems tsnForwardingItems1 = new TsnForwardingItemsBuilder()
                .withKey(new TsnForwardingItemsKey("01","fei-01"))
                .setNodeId("01")
                .setOutPort("fei-01")
                .build();
        TsnForwardingItems tsnForwardingItems2 = new TsnForwardingItemsBuilder()
                .withKey(new TsnForwardingItemsKey("01","fei-02"))
                .setNodeId("01")
                .setOutPort("fei-02")
                .build();
        TsnForwardingItems tsnForwardingItems3 = new TsnForwardingItemsBuilder()
                .withKey(new TsnForwardingItemsKey("02","fei-01"))
                .setNodeId("02")
                .setOutPort("fei-01")
                .build();
        TsnForwardingItems tsnForwardingItems4 = new TsnForwardingItemsBuilder()
                .withKey(new TsnForwardingItemsKey("02","fei-02"))
                .setNodeId("02")
                .setOutPort("fei-02")
                .build();
        TsnForwardingItems tsnForwardingItems5 = new TsnForwardingItemsBuilder()
                .withKey(new TsnForwardingItemsKey("03","fei-01"))
                .setNodeId("03")
                .setOutPort("fei-01")
                .build();
        forwardingItemsList.add(tsnForwardingItems1);
        forwardingItemsList.add(tsnForwardingItems2);
        forwardingItemsList.add(tsnForwardingItems3);
        forwardingItemsList.add(tsnForwardingItems4);
        forwardingItemsList.add(tsnForwardingItems5);
        ConfigTsnServiceInput configTsnServiceInput = new ConfigTsnServiceInputBuilder()
                .setGroupMacAddress("01:00:5e:00:01:00")
                .setVlanId(1)
                .setTsnForwardingItems(forwardingItemsList)
                .build();
        try {
            boolean configResult = tsnServiceImpl.configTsnService(configTsnServiceInput).get().isSuccessful();
            assertEquals(true, configResult);
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        InstanceIdentifier<ForwardingItemList> forwardingItemListIID = tsnServiceImpl.getTsnServiceIID("02")
                .child(ForwardingItemList.class, new ForwardingItemListKey("01:00:5e:00:01:00", 1));
        ForwardingItemList forwardingItemList = DataOperator.readData(dataBroker, forwardingItemListIID);
        assertNotNull(forwardingItemList);
        assertEquals(2, forwardingItemList.getOutPorts().size());
        //LOG.info(forwardingItemList.toString());
        forwardingItemListIID = tsnServiceImpl.getTsnServiceIID("03")
                .child(ForwardingItemList.class, new ForwardingItemListKey("01:00:5e:00:01:00", 1));
        forwardingItemList = DataOperator.readData(dataBroker, forwardingItemListIID);
        assertNotNull(forwardingItemList);
        assertEquals(1, forwardingItemList.getOutPorts().size());
        //LOG.info("Test config tsn service success.");
    }

    @Test
    public void deleteTsnServiceTest() {
        List<String> tsnNodes = new ArrayList<String>();
        tsnNodes.add("07");
        tsnNodes.add("08");
        tsnNodes.add("100");
        DeleteTsnServiceInput deleteTsnServiceInput = new DeleteTsnServiceInputBuilder()
                .setTsnNodes(tsnNodes)
                .setGroupMacAddress("01:00:5e:00:02:00")
                .setVlanId(1)
                .build();
        try {
            boolean deleteResult = tsnServiceImpl.deleteTsnService(deleteTsnServiceInput).get().isSuccessful();
            assertEquals(true, deleteResult);
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        InstanceIdentifier<ForwardingItemList> forwardingItemListIID = tsnServiceImpl.getTsnServiceIID("07")
                .child(ForwardingItemList.class, new ForwardingItemListKey("01:00:5e:00:02:00", 1));
        ForwardingItemList forwardingItemList = DataOperator.readData(dataBroker, forwardingItemListIID);
        assertEquals(null, forwardingItemList);
        forwardingItemListIID = tsnServiceImpl.getTsnServiceIID("08")
                .child(ForwardingItemList.class, new ForwardingItemListKey("01:00:5e:00:02:00", 1));
        forwardingItemList = DataOperator.readData(dataBroker, forwardingItemListIID);
        assertEquals(null, forwardingItemList);
        //LOG.info("Test delete tsn service success.");
    }

    @Test
    public void deleteSuccessNodesTsnServiceTest() {
        Set<String> successNodes = new HashSet<String>();
        successNodes.add("07");
        successNodes.add("08");
        boolean deleteSuccessNodesResult = tsnServiceImpl.deleteTsnService(successNodes, "01:00:5e:00:02:00", 1);
        assertEquals(true, deleteSuccessNodesResult);
    }

    @Test
    public void queryTsnServiceTest() {
        InstanceIdentifier<TsnService> tsnServiceIID = tsnServiceImpl.getTsnServiceIID("07");
        TsnService tsnService = DataOperator.readData(dataBroker, tsnServiceIID);
        assertNotNull(tsnService);
        assertEquals(1, tsnService.getForwardingItemList().size());
        assertEquals(2, tsnService.getForwardingItemList().get(0).getOutPorts().size());
        assertEquals("01:00:5e:00:02:00", tsnService.getForwardingItemList().get(0).getGroupMacAddress());
        tsnServiceIID = tsnServiceImpl.getTsnServiceIID("08");
        tsnService = DataOperator.readData(dataBroker, tsnServiceIID);
        assertEquals(1, tsnService.getForwardingItemList().size());
        assertEquals(1, tsnService.getForwardingItemList().get(0).getOutPorts().size());
        assertEquals("01:00:5e:00:02:00", tsnService.getForwardingItemList().get(0).getGroupMacAddress());
        LOG.info("Test query tsn service success.");
    }

    private void mockDataForQueryAndDelete(String nodeId, String groupMacAddress, int vlanId, String... outPorts) {
        InstanceIdentifier<ForwardingItemList> forwardingItemListIID = tsnServiceImpl.getTsnServiceIID(nodeId)
                .child(ForwardingItemList.class, new ForwardingItemListKey(groupMacAddress, vlanId));
        List<String> outPortList = new ArrayList<String>();
        Collections.addAll(outPortList, outPorts);
        ForwardingItemList forwardingItemList = new ForwardingItemListBuilder()
                .withKey(new ForwardingItemListKey(groupMacAddress, vlanId))
                .setGroupMacAddress(groupMacAddress)
                .setVlanId(vlanId)
                .setOutPorts(outPortList)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, forwardingItemListIID, forwardingItemList);
    }

    private class DetnetDriverApiServiceMock implements DetnetDriverApiService {

        @Override
        public ListenableFuture<RpcResult<DeleteDetnetServiceConfigurationOutput>> deleteDetnetServiceConfiguration(
                DeleteDetnetServiceConfigurationInput input) {
            return null;
        }

        @Override
        public ListenableFuture<RpcResult<WriteGateConfigToSouthOutput>> writeGateConfigToSouth(
                WriteGateConfigToSouthInput input) {
            return null;
        }

        @Override
        public ListenableFuture<RpcResult<DeleteTsnServiceToSouthOutput>>  deleteTsnServiceToSouth(
                DeleteTsnServiceToSouthInput input) {
            return RpcReturnUtil.returnSucess(null);
        }

        @Override
        public ListenableFuture<RpcResult<WriteBandwidthToSouthOutput>> writeBandwidthToSouth(
                WriteBandwidthToSouthInput input) {
            return null;
        }

        @Override
        public ListenableFuture<RpcResult<DeleteTsnServiceToSouthOutput>> writeTsnServiceToSouth(
                WriteTsnServiceToSouthInput input) {
            return RpcReturnUtil.returnSucess(null);
        }

        @Override
        public ListenableFuture<RpcResult<WriteDetnetServiceConfigurationOutput>> writeDetnetServiceConfiguration(
                WriteDetnetServiceConfigurationInput input) {
            return null;
        }
    }
*/
}
