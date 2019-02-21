/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.tsn.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.NotificationProvider;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.TsnServiceManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemList;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.tsn.service.manager.TsnService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.tsn.service.manager.TsnServiceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DetnetTsnServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.QueryTsnServiceForwardingItemInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.QueryTsnServiceForwardingItemOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.QueryTsnServiceForwardingItemOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItems;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsnServiceImpl implements DetnetTsnServiceApiService {

    private static final Logger LOG = LoggerFactory.getLogger(TsnServiceImpl.class);
    private DataBroker dataBroker;
    private DetnetDriverApiService detnetDriverApiService;

    public TsnServiceImpl(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry) {
        this.dataBroker = dataBroker;
        this.detnetDriverApiService = rpcConsumerRegistry.getRpcService(DetnetDriverApiService.class);
    }


    @Override
    public Future<RpcResult<DeleteTsnServiceOutput>> deleteTsnService(DeleteTsnServiceInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getTsnNodes(), input.getVlanId(),
                input.getGroupMacAddress())).isInputIllegal()) {
            LOG.info("Delete tsn service input error!" + checkResult.getErrorCause());
            NotificationProvider.getInstance()
                    .notify(NotificationProvider.reportMessage("Input error."));
            return RpcReturnUtil.returnErr("Input error.");
        }

        boolean allDeleteSuccess = true;
        for (String nodeId : input.getTsnNodes()) {
            InstanceIdentifier<TsnService> tsnServiceIID = getTsnServiceIID(nodeId);
            if (null == DataOperator.readData(dataBroker, tsnServiceIID)) {
                continue;
            }
            InstanceIdentifier<ForwardingItemList> forwardingItemListIID = tsnServiceIID
                    .child(ForwardingItemList.class, new ForwardingItemListKey(
                            input.getGroupMacAddress(), input.getVlanId()));
            if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, forwardingItemListIID, null)) {
                LOG.info("Delete tsn service failed, nodeId: {}", nodeId);
                allDeleteSuccess = false;
            }
            DeleteTsnServiceToSouthInput deleteTsnServiceToSouthInput = new DeleteTsnServiceToSouthInputBuilder()
                    .setNodeId(nodeId)
                    .setVlanId(input.getVlanId())
                    .setGroupMacAddress(input.getGroupMacAddress())
                    .build();
            try {
                if (!detnetDriverApiService.deleteTsnServiceToSouth(deleteTsnServiceToSouthInput).get()
                        .isSuccessful()) {
                    LOG.info("Delete tsn service to south failed, nodeId: {}", nodeId);
                    allDeleteSuccess = false;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.info(Arrays.toString(e.getStackTrace()));
            }
        }
        return allDeleteSuccess
                ? RpcReturnUtil.returnSucess(new DeleteTsnServiceOutputBuilder().build())
                : RpcReturnUtil.returnErr("Delete datastore failed.");
    }

    public boolean deleteTsnService(Set<String> successNodes, String groupMacAddress, int vlanId) {
        List<String> tsnNodes = new ArrayList<>();
        tsnNodes.addAll(successNodes);
        DeleteTsnServiceInput deleteTsnServiceInput = new DeleteTsnServiceInputBuilder()
                .setTsnNodes(tsnNodes)
                .setGroupMacAddress(groupMacAddress)
                .setVlanId(vlanId)
                .build();
        try {
            if (!deleteTsnService(deleteTsnServiceInput).get().isSuccessful()) {
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return true;
    }

    @Override
    public Future<RpcResult<ConfigTsnServiceOutput>> configTsnService(ConfigTsnServiceInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getTsnForwardingItems(), input.getVlanId(),
                input.getGroupMacAddress())).isInputIllegal()) {
            LOG.info("Config tsn service input error!" + checkResult.getErrorCause());
            NotificationProvider.getInstance()
                    .notify(NotificationProvider.reportMessage("Input error."));
            return RpcReturnUtil.returnErr("Input error.");
        }

        String groupMacAddress = input.getGroupMacAddress();
        int vlanId = input.getVlanId();
        Set<String> successNodes = new HashSet<>();
        for (TsnForwardingItems tsnForwardingItems : input.getTsnForwardingItems()) {
            InstanceIdentifier<ForwardingItemList> forwardingItemListIID = getTsnServiceIID(
                    tsnForwardingItems.getNodeId())
                    .child(ForwardingItemList.class, new ForwardingItemListKey(groupMacAddress, vlanId));
            List<String> outPortList = new ArrayList<>();
            outPortList.add(tsnForwardingItems.getOutPort());
            ForwardingItemList forwardingItemList = new ForwardingItemListBuilder()
                    .setKey(new ForwardingItemListKey(groupMacAddress, vlanId))
                    .setGroupMacAddress(groupMacAddress)
                    .setVlanId(vlanId)
                    .setOutPorts(outPortList)
                    .build();

            WriteTsnServiceToSouthInput writeTsnServiceToSouthInput = new WriteTsnServiceToSouthInputBuilder()
                    .setNodeId(tsnForwardingItems.getNodeId())
                    .setGroupMacAddress(groupMacAddress)
                    .setVlanId(vlanId)
                    .setOutPorts(outPortList)
                    .build();
            boolean isWriteTsnToSouthSuccess = false;
            try {
                isWriteTsnToSouthSuccess = detnetDriverApiService.writeTsnServiceToSouth(writeTsnServiceToSouthInput)
                        .get().isSuccessful();
            } catch (InterruptedException | ExecutionException e) {
                LOG.info(Arrays.toString(e.getStackTrace()));
            }

            if (!DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, forwardingItemListIID,
                    forwardingItemList) || !isWriteTsnToSouthSuccess) {
                LOG.info("Config tsn service failed, nodeId: {}", tsnForwardingItems.getNodeId());
                if (!deleteTsnService(successNodes, groupMacAddress, vlanId)) {
                    LOG.info("Delete succeed nodes failed.");
                }
                return RpcReturnUtil.returnErr("Write datastore failed.");
            }
            successNodes.add(tsnForwardingItems.getNodeId());
        }
        return RpcReturnUtil.returnSucess(new ConfigTsnServiceOutputBuilder().build());
    }

    @Override
    public Future<RpcResult<QueryTsnServiceForwardingItemOutput>> queryTsnServiceForwardingItem(
            QueryTsnServiceForwardingItemInput input) {
        InstanceIdentifier<TsnService> tsnServiceIID = getTsnServiceIID(input.getNodeId());
        TsnService tsnService = DataOperator.readData(dataBroker, tsnServiceIID);
        if (null == tsnService) {
            LOG.info("Read datastore failed or  data of this nodeId not exist.");
            return RpcReturnUtil.returnErr("Query failed.");
        }
        QueryTsnServiceForwardingItemOutput output = new QueryTsnServiceForwardingItemOutputBuilder()
                .setForwardingItemList(tsnService.getForwardingItemList())
                .build();
        return RpcReturnUtil.returnSucess(output);
    }

    public InstanceIdentifier<TsnService> getTsnServiceIID(String nodeId) {
        return InstanceIdentifier
                .create(TsnServiceManager.class)
                .child(TsnService.class, new TsnServiceKey(nodeId));
    }
}
