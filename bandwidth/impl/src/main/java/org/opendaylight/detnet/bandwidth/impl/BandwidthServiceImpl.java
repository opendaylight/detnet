/*
 * Copyright (c) 2018 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.bandwidth.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.NotificationProvider;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DetnetBandwidthApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.LinkBandwidthChange;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.LinkBandwidthChangeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.QueryBandwidthParameterInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.QueryBandwidthParameterOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.QueryBandwidthParameterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.link.bandwidth.change.OldLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteBandwidthToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.BandwidthConfigManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwidth.config.manager.BandwidthConfig;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwidth.config.manager.BandwidthConfigKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClassesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.bandwith.manager.group.TrafficClassesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLinkKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BandwidthServiceImpl implements DetnetBandwidthApiService {

    private static final Logger LOG = LoggerFactory.getLogger(BandwidthServiceImpl.class);
    private DataBroker dataBroker;
    private DetnetDriverApiService detnetDriverApiService;

    public BandwidthServiceImpl(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry) {
        this.dataBroker = dataBroker;
        this.detnetDriverApiService = rpcConsumerRegistry.getRpcService(DetnetDriverApiService.class);
    }

    @Override
    public Future<RpcResult<ConfigE2eBandwidthOutput>> configE2eBandwidth(ConfigE2eBandwidthInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getBandwidth(), input.getTrafficClass(),
                input.getPathLink())).isInputIllegal()) {
            LOG.info("Config bandwidth input error!");
            return RpcReturnUtil.returnErr("Input error:" + checkResult.getErrorCause());
        }

        LOG.info("Config e2e service bandwidth.");
        List<PathLink> successLinkList = new ArrayList<>();
        for (PathLink link : input.getPathLink()) {
            LOG.info("Write bandwidth manager datastore, link:{}",link.getLinkId());
            InstanceIdentifier<TrafficClasses> trafficClassesIID = getTrafficClassesIID(link, input.getTrafficClass());
            TrafficClasses trafficClasses = DataOperator.readData(dataBroker, trafficClassesIID);
            long bandwidthToBeReserved = input.getBandwidth();
            if (null != trafficClasses) {
                bandwidthToBeReserved += trafficClasses.getReservedBandwidth();
            }
            LOG.debug("Total bandwidth reserved for link:{} is：{}", link.getLinkId(), bandwidthToBeReserved);
            TrafficClasses newTrfficClasses = new TrafficClassesBuilder().setTcIndex(input.getTrafficClass())
                    .setReservedBandwidth(bandwidthToBeReserved).build();

            LOG.info("Write topology link bandwidth datastore, link:{}",link.getLinkId());
            InstanceIdentifier<DetnetLink> detnetLinkIID = getDetnetLinkIID(link, input.getTopologyId());
            DetnetLink detnetLink = DataOperator.readData(dataBroker, detnetLinkIID);

            DetnetLink newDetnetLink = new DetnetLinkBuilder(link)
                    .setReservedDetnetBandwidth(detnetLink.getReservedDetnetBandwidth() + input.getBandwidth())
                    .setAvailableDetnetBandwidth(detnetLink.getAvailableDetnetBandwidth() - input.getBandwidth())
                    .build();
            LOG.debug("Write bandwidth to south, nodeId:{}", link.getLinkSource().getSourceNode());
            WriteBandwidthToSouthInput writeBandwidthToSouthInput = new WriteBandwidthToSouthInputBuilder()
                    .setNodeId(link.getLinkSource().getSourceNode())
                    .setTpId(link.getLinkSource().getSourceTp())
                    .setTrafficClass(newTrfficClasses.getTcIndex())
                    .setReservedBandwidth(newTrfficClasses.getReservedBandwidth())
                    .build();
            boolean isWriteToSouthSuccess = false;
            try {
                isWriteToSouthSuccess = detnetDriverApiService.writeBandwidthToSouth(writeBandwidthToSouthInput)
                        .get().isSuccessful();
            } catch (InterruptedException | ExecutionException e) {
                LOG.info(Arrays.toString(e.getStackTrace()));
            }

            boolean isWriteBandwidthManagerSuccess = DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker,
                    trafficClassesIID, newTrfficClasses);
            boolean isWriteTopologySuccess = DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker,
                    detnetLinkIID, newDetnetLink);

            if (!isWriteBandwidthManagerSuccess || !isWriteTopologySuccess || !isWriteToSouthSuccess) {
                LOG.debug("Config link {} bandwidth failed!", link.getLinkId());
                deleteSucceedLinksBandwidth(input.getTopologyId(), successLinkList, input.getBandwidth(),
                        input.getTrafficClass());
                if (isWriteBandwidthManagerSuccess) {
                    TrafficClasses resetTrafficClasses = new TrafficClassesBuilder(newTrfficClasses)
                            .setReservedBandwidth(bandwidthToBeReserved - input.getBandwidth())
                            .build();
                    DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, trafficClassesIID,
                            resetTrafficClasses);
                }
                return RpcReturnUtil.returnErr("Config bandwidth failed!");
            }
            successLinkList.add(link);
            LinkBandwidthChange linkBandwidthChange = new LinkBandwidthChangeBuilder()
                    .setOldLink(new OldLinkBuilder(detnetLink).build())
                    .setNewAvailableBandwidth(newDetnetLink.getAvailableDetnetBandwidth())
                    .build();
            NotificationProvider.getInstance().notify(linkBandwidthChange);

        }
        LOG.info("Config bandwidth success!");
        return RpcReturnUtil.returnSucess(new ConfigE2eBandwidthOutputBuilder().build());
    }

    private void deleteSucceedLinksBandwidth(String topologyId, List<PathLink> pathLinks, long bandwidth,
                                             short trafficClass) {
        DeleteE2eBandwidthInput deleteE2eBandwidthInput = new DeleteE2eBandwidthInputBuilder()
                .setTopologyId(topologyId)
                .setPathLink(pathLinks)
                .setBandwidth(bandwidth)
                .setTrafficClass(trafficClass)
                .build();
        Future<RpcResult<DeleteE2eBandwidthOutput>> output = deleteE2eBandwidth(deleteE2eBandwidthInput);
        try {
            if (!output.get().isSuccessful()) {
                LOG.info("Delete successLinkList bandwidth failed!");
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
    }

    private InstanceIdentifier<TrafficClasses> getTrafficClassesIID(PathLink link, short trafficClass) {
        return InstanceIdentifier.create(BandwidthConfigManager.class)
                .child(BandwidthConfig.class, new BandwidthConfigKey(link.getLinkSource()
                        .getSourceNode(),link.getLinkSource().getSourceTp()))
                .child(TrafficClasses.class, new TrafficClassesKey(trafficClass));
    }

    private InstanceIdentifier<DetnetLink> getDetnetLinkIID(PathLink link, String topologyId) {
        return InstanceIdentifier.create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(topologyId))
                .child(DetnetLink.class, new DetnetLinkKey(link.getLinkId()));
    }

    @Override
    public Future<RpcResult<DeleteE2eBandwidthOutput>> deleteE2eBandwidth(DeleteE2eBandwidthInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getBandwidth(), input.getTrafficClass(),
                input.getPathLink())).isInputIllegal()) {
            LOG.info("Delete bandwidth input error!");
            return RpcReturnUtil.returnErr("Input error:" + checkResult.getErrorCause());
        }

        boolean deleteAllLinkBandWidthSuccess = true;
        for (PathLink link : input.getPathLink()) {
            LOG.info("Delete bandwidth datastore, link:{}", link.getLinkId());
            InstanceIdentifier<TrafficClasses> trafficClassesIID = getTrafficClassesIID(link, input.getTrafficClass());
            TrafficClasses trafficClasses = DataOperator.readData(dataBroker, trafficClassesIID);
            TrafficClasses newTrafficClasses = new TrafficClassesBuilder(trafficClasses)
                    .setReservedBandwidth(trafficClasses.getReservedBandwidth() - input.getBandwidth())
                    .build();

            LOG.info("Delete topology link bandwidth datasotre, link:{}", link.getLinkId());
            InstanceIdentifier<DetnetLink> detnetLinkIID = getDetnetLinkIID(link, input.getTopologyId());
            DetnetLink detnetLink = DataOperator.readData(dataBroker, detnetLinkIID);
            DetnetLink newDetnetLink = new DetnetLinkBuilder(detnetLink)
                    .setReservedDetnetBandwidth(detnetLink.getReservedDetnetBandwidth() - input.getBandwidth())
                    .setAvailableDetnetBandwidth(detnetLink.getAvailableDetnetBandwidth() + input.getBandwidth())
                    .build();

            if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, trafficClassesIID, newTrafficClasses)
                    || !DataOperator.writeData(DataOperator.OperateType.MERGE,
                    dataBroker, detnetLinkIID, newDetnetLink)) {
                LOG.info("Delete bandwidth failed, link:{}", link.getLinkId());
                deleteAllLinkBandWidthSuccess = false;
            }

            WriteBandwidthToSouthInput writeBandwidthToSouthInput = new WriteBandwidthToSouthInputBuilder()
                    .setNodeId(link.getLinkSource().getSourceNode())
                    .setTpId(link.getLinkSource().getSourceTp())
                    .setTrafficClass(newTrafficClasses.getTcIndex())
                    .setReservedBandwidth(newTrafficClasses.getReservedBandwidth())
                    .build();
            try {
                if (!detnetDriverApiService.writeBandwidthToSouth(writeBandwidthToSouthInput).get().isSuccessful()) {
                    LOG.info("Delete bandwidth to south failed, link:{}", link.getLinkId());
                    deleteAllLinkBandWidthSuccess = false;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.info(Arrays.toString(e.getStackTrace()));
            }

            LinkBandwidthChange linkBandwidthChange = new LinkBandwidthChangeBuilder()
                    .setOldLink(new OldLinkBuilder(detnetLink).build())
                    .setNewAvailableBandwidth(newDetnetLink.getAvailableDetnetBandwidth())
                    .build();
            NotificationProvider.getInstance().notify(linkBandwidthChange);
        }

        if (!deleteAllLinkBandWidthSuccess) {
            return RpcReturnUtil.returnErr("There link bandwidth recycle failed!");
        }
        return RpcReturnUtil.returnSucess(new DeleteE2eBandwidthOutputBuilder().build());
    }

    @Override
    public Future<RpcResult<QueryBandwidthParameterOutput>> queryBandwidthParameter(
            QueryBandwidthParameterInput input) {

        InstanceIdentifier<BandwidthConfig> bandwidthConfigIID = InstanceIdentifier
                .create(BandwidthConfigManager.class)
                .child(BandwidthConfig.class, new BandwidthConfigKey(input.getNodeId(),input.getTpId()));
        BandwidthConfig bandwidthConfig = DataOperator.readData(dataBroker,bandwidthConfigIID);
        QueryBandwidthParameterOutput output = new QueryBandwidthParameterOutputBuilder()
                .setNodeId(input.getNodeId())
                .setTpId(input.getTpId())
                .setTrafficClasses(bandwidthConfig.getTrafficClasses())
                .build();
        return RpcReturnUtil.returnSucess(output);
    }
}
