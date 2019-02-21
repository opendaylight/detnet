/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.driver.impl;

import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteTsnServiceToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.DetnetConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.BandwidthConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.DetnetServiceConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.GateConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.TsnServiceConfiguration;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.bandwidth.configuration.BandwidthConfigList;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.bandwidth.configuration.BandwidthConfigListKey;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.detnet.service.configuration.Service;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.detnet.service.configuration.ServiceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.detnet.service.configuration.ServiceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.gate.configuration.GateConfigList;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.gate.configuration.GateConfigListBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.yang.service.rev181210.detnet.configuration.gate.configuration.GateConfigListKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClassesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClassesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemList;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.forwarding.item.list.group.ForwardingItemListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverServiceImpl implements DetnetDriverApiService {

    private static final Logger LOG = LoggerFactory.getLogger(DriverServiceImpl.class);

    @Override
    public Future<RpcResult<Void>> deleteDetnetServiceConfiguration(DeleteDetnetServiceConfigurationInput input) {
        LOG.info("Delete detnet service configuration input: " + input);
        if (null == input.getNodeId() || null == input.getStreamId()) {
            return RpcReturnUtil.returnErr("Input error!");
        }
        InstanceIdentifier<Service> detnetServicePath = buildDetnetServicepath(input.getStreamId());
        if (!DataOperator.writeNetconfData(input.getNodeId(), DataOperator.OperateType.DELETE, detnetServicePath,
                null, LogicalDatastoreType.CONFIGURATION)) {
            LOG.info("Delete detnet service configuration to node failed!node: " + input.getNodeId());
            return RpcReturnUtil.returnErr("Delete detnet service configuration to node failed!node: " + input.getNodeId());
        }
        return RpcReturnUtil.returnSucess(null);
    }

    @Override
    public Future<RpcResult<Void>> writeGateConfigToSouth(WriteGateConfigToSouthInput input) {
        if (null == input.getNodeId() || null == input.getGateConfigParams()) {
            LOG.info("Write gate config to south bound input error.");
            return RpcReturnUtil.returnErr("Input error.");
        }
        String tpId = input.getGateConfigParams().getTpId();
        InstanceIdentifier<GateConfigList> gateConfigListIID = InstanceIdentifier
                .create(DetnetConfiguration.class)
                .child(GateConfiguration.class)
                .child(GateConfigList.class, new GateConfigListKey(tpId));

        GateConfigList gateConfigList = new GateConfigListBuilder(input.getGateConfigParams())
                .setKey(new GateConfigListKey(tpId))
                .build();

        if (!DataOperator.writeNetconfData(input.getNodeId(), DataOperator.OperateType.MERGE, gateConfigListIID,
                gateConfigList, LogicalDatastoreType.CONFIGURATION)) {
            LOG.info("Write gate configuration to south bound failed.");
            return RpcReturnUtil.returnErr("");
        }

        return RpcReturnUtil.returnSucess(null);
    }

    @Override
    public Future<RpcResult<Void>> writeBandwidthToSouth(WriteBandwidthToSouthInput input) {
        if (null == input.getNodeId() || null == input.getTpId() || null == input.getTrafficClass()
                || null == input.getReservedBandwidth()) {
            LOG.info("Write bandwidth config to south bound input error.");
            return RpcReturnUtil.returnErr("Input error.");
        }
        InstanceIdentifier<TrafficClasses> trafficClassesIID = InstanceIdentifier
                .create(DetnetConfiguration.class)
                .child(BandwidthConfiguration.class)
                .child(BandwidthConfigList.class, new BandwidthConfigListKey(input.getTpId()))
                .child(TrafficClasses.class, new TrafficClassesKey(input.getTrafficClass()));

        TrafficClasses trafficClasses = new TrafficClassesBuilder()
                .setKey(new TrafficClassesKey(input.getTrafficClass()))
                .setTcIndex(input.getTrafficClass())
                .setReservedBandwidth(input.getReservedBandwidth())
                .build();
        if (!DataOperator.writeNetconfData(input.getNodeId(), DataOperator.OperateType.MERGE, trafficClassesIID,
                trafficClasses, LogicalDatastoreType.CONFIGURATION)) {
            LOG.info("Write bandwidth configuration to south bound failed.");
            return RpcReturnUtil.returnErr("");
        }
        return RpcReturnUtil.returnSucess(null);
    }

    @Override
    public Future<RpcResult<Void>> writeTsnServiceToSouth(WriteTsnServiceToSouthInput input) {
        if (null == input.getNodeId() || null == input.getVlanId() || null == input.getGroupMacAddress()
                || null == input.getOutPorts()) {
            LOG.info("Write tsn service to south bound input error.");
            return RpcReturnUtil.returnErr("Input error.");
        }
        InstanceIdentifier<ForwardingItemList> forwardingItemListIID = getForwardingItemListIID(
                input.getGroupMacAddress(), input.getVlanId());
        ForwardingItemList forwardingItemList = new ForwardingItemListBuilder()
                .setKey(new ForwardingItemListKey(input.getGroupMacAddress(), input.getVlanId()))
                .setVlanId(input.getVlanId())
                .setGroupMacAddress(input.getGroupMacAddress())
                .setOutPorts(input.getOutPorts())
                .build();
        if (!DataOperator.writeNetconfData(input.getNodeId(), DataOperator.OperateType.MERGE, forwardingItemListIID,
                forwardingItemList, LogicalDatastoreType.CONFIGURATION)) {
            LOG.info("Write tsn service to south bound failed.");
            return RpcReturnUtil.returnErr("");
        }
        return RpcReturnUtil.returnSucess(null);
    }

    @Override
    public Future<RpcResult<Void>> writeDetnetServiceConfiguration(WriteDetnetServiceConfigurationInput input) {
        LOG.info("Write detnet service configuration input: " + input);
        if (null == input.getNodeId() || null == input.getStreamId()) {
            return RpcReturnUtil.returnErr("Input error!");
        }
        InstanceIdentifier<Service> detnetServicePath = buildDetnetServicepath(input.getStreamId());
        Service detnetService = new ServiceBuilder(input).build();
        if (!DataOperator.writeNetconfData(input.getNodeId(), DataOperator.OperateType.MERGE, detnetServicePath,
                detnetService, LogicalDatastoreType.CONFIGURATION)) {
            LOG.info("Write detnet service configuration to node failed!node: " + input.getNodeId());
            return RpcReturnUtil.returnErr("Write detnet service configuration to node failed!node: " + input.getNodeId());
        }
        return RpcReturnUtil.returnSucess(null);
    }

    @Override
    public Future<RpcResult<Void>> deleteTsnServiceToSouth(DeleteTsnServiceToSouthInput input) {
        if (null == input.getNodeId() || null == input.getVlanId() || null == input.getGroupMacAddress()) {
            LOG.info("Delete tsn service to south bound input error.");
            return RpcReturnUtil.returnErr("Input error.");
        }
        InstanceIdentifier<ForwardingItemList> forwardingItemListIID = getForwardingItemListIID(
                input.getGroupMacAddress(), input.getVlanId());
        if (!DataOperator.writeNetconfData(input.getNodeId(), DataOperator.OperateType.DELETE, forwardingItemListIID,
                null, LogicalDatastoreType.CONFIGURATION)) {
            LOG.info("Delete tsn service to south bound failed.");
            return RpcReturnUtil.returnErr("");
        }
        return RpcReturnUtil.returnSucess(null);
    }

    private InstanceIdentifier<ForwardingItemList> getForwardingItemListIID(String macAddress, int vlanId) {
        return InstanceIdentifier
                .create(DetnetConfiguration.class)
                .child(TsnServiceConfiguration.class)
                .child(ForwardingItemList.class,
                        new ForwardingItemListKey(macAddress, vlanId));
    }

    private InstanceIdentifier<Service> buildDetnetServicepath(Long streamId) {
        return InstanceIdentifier.create(DetnetConfiguration.class)
                .child(DetnetServiceConfiguration.class)
                .child(Service.class,new ServiceKey(streamId));
    }
}
