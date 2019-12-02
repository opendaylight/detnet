/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.gate.impl;
import com.google.common.util.concurrent.ListenableFuture;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteGateConfigToSouthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.write.gate.config.to.south.input.GateConfigParams;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.write.gate.config.to.south.input.GateConfigParamsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DetnetGateApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.QueryGateParameterInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.QueryGateParameterOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.QueryGateParameterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.query.gate.parameter.output.GateParameterBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QueueTemplate;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.GateConfigManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminBasetime;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminBasetimeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminControlList;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminControlListBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminCycletime;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.AdminCycletimeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.admin.control.list.GateStates;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.group.admin.control.list.GateStatesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.manager.GateConfig;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.manager.GateConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.gate.config.manager.GateConfigKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateServiceImpl implements DetnetGateApiService {

    private static final Logger LOG = LoggerFactory.getLogger(GateServiceImpl.class);
    private DataBroker dataBroker;
    private DetnetDriverApiService detnetDriverApiService;

    public GateServiceImpl(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry) {
        this.dataBroker = dataBroker;
        detnetDriverApiService = rpcConsumerRegistry.getRpcService(DetnetDriverApiService.class);
    }

    @Override
    public ListenableFuture<RpcResult<ConfigE2eGateOutput>> configE2eGate(ConfigE2eGateInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getBandwidth(), input.getTrafficClass(),
                input.getPathLink(), input.getTopologyId())).isInputIllegal()) {
            //LOG.info("Config gate input error!");
            return RpcReturnUtil.returnErr("Input error:" + checkResult.getErrorCause());
        }

        AdminBasetime newAdminBasetime = getAdminBaseTime();

        //LOG.info("Start config gate for source port of each link!");
        List<PathLink> successLinks = new ArrayList<PathLink>();
        boolean isfailed = false;
        for (PathLink pathLink : input.getPathLink()) {
            //LOG.info("Config gate for link id: {}", pathLink.getLinkId());
            InstanceIdentifier<GateConfig> gateConfigIID = getGateConfigIID(pathLink);
            GateConfig oldGateConfig = DataOperator.readData(dataBroker, gateConfigIID);
            GateConfigBuilder newGateConfigBuilder;
            String nodeId = pathLink.getLinkSource().getSourceNode();
            String tpId = pathLink.getLinkSource().getSourceTp();
            if (null != oldGateConfig) {
                newGateConfigBuilder = new GateConfigBuilder(oldGateConfig);
            } else {
                newGateConfigBuilder = new GateConfigBuilder()
                        .setNodeId(nodeId)
                        .setTpId(tpId)
                        .withKey(new GateConfigKey(pathLink.getLinkSource()
                                .getSourceNode(),pathLink.getLinkSource().getSourceTp()))
                        .setAdminControlListLength(getAdminControlListLength(input))
                        .setAdminCycletime(getAdminCycleTime())
                        .setAdminGateStates((short) 127);

            }

            AdminControlList newAdminControlList = getNewAdminControlList(newGateConfigBuilder, input);
            if (null == newAdminControlList) {
                isfailed = true;
                //LOG.info("Calculate new gate control list failed!");
            }
            GateConfig newGateConfig = newGateConfigBuilder
                    .setAdminBasetime(newAdminBasetime)
                    .setAdminControlList(newAdminControlList)
                    .setConfigChange(true)
                    .setGateEnabled(true)
                    .build();
            //LOG.info(newGateConfig.toString());
            DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, gateConfigIID, newGateConfig);

            GateConfigParams gateConfigParams = new GateConfigParamsBuilder(newGateConfig)
                    .build();
            WriteGateConfigToSouthInput writeGateToSouthInput = new WriteGateConfigToSouthInputBuilder()
                    .setNodeId(nodeId)
                    .setGateConfigParams(gateConfigParams)
                    .build();
            try {
                if (!detnetDriverApiService.writeGateConfigToSouth(writeGateToSouthInput).get().isSuccessful()) {
                    isfailed = true;
                    //LOG.info("Write gate config to south bound failed, nodeId:", nodeId);
                    RpcReturnUtil.returnErr("Write gate config to south bound failed.");
                }
            } catch (InterruptedException | ExecutionException e) {
                //LOG.info(Arrays.toString(e.getStackTrace()));
                RpcReturnUtil.returnErr("Write gate config to south bound failed.");
            }

            if (isfailed) {
                deleteSuccessGates(successLinks, input);
                return RpcReturnUtil.returnErr("Config e2e service gate failed.");
            }

            successLinks.add(pathLink);
        }

        return RpcReturnUtil.returnSucess(new ConfigE2eGateOutputBuilder().build());
    }



    private void deleteSuccessGates(List<PathLink> successLinks, ConfigE2eGateInput input) {
        DeleteE2eGateInput deleteE2eGateInput = new DeleteE2eGateInputBuilder()
                .setPathLink(successLinks)
                .setTopologyId(input.getTopologyId())
                .setBandwidth(input.getBandwidth())
                .setTrafficClass(input.getTrafficClass())
                .build();
        try {
            if (!deleteE2eGate(deleteE2eGateInput).get().isSuccessful()) {
                //LOG.info("Delete success links gate failed.");
                RpcReturnUtil.returnErr("Delete e2e service gate failed.");
            }
        } catch (InterruptedException | ExecutionException e) {
            RpcReturnUtil.returnErr("Delete e2e service gate failed.");
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
    }

    private long getPortBandwidth(String topologyId) {
        InstanceIdentifier<DetnetTopology> detnetTopologyIID = InstanceIdentifier
                .create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(topologyId));
        return DataOperator.readData(dataBroker, detnetTopologyIID)
                .getDetnetLink()
                .get(0)
                .getLinkBandwidth().longValue();
    }

    //Each open of gate represents 1M bit
    private long getAdminControlListLength(ConfigE2eGateInput input) {
        long portBandwidth = getPortBandwidth(input.getTopologyId());
        long controlListLength = portBandwidth / 1000;
        //LOG.info("Calculate control list length: {}", controlListLength);
        return controlListLength;
    }

    //Each gate interval can transport 100K bit
    private AdminCycletime getAdminCycleTime() {
        //LOG.info("Calculate control list cycle time: 100ms");
        return new AdminCycletimeBuilder().setDenominator(10L).build();
    }

    private AdminBasetime getAdminBaseTime() {
        long currentMillis = System.currentTimeMillis();
        long seconds = currentMillis / 1000;
        long nanoSeconds = (long) (currentMillis % 1000 * Math.pow(10,6));
        //LOG.info("Get current UTC time:{} second, {} nanosecond.", seconds, nanoSeconds);
        return new AdminBasetimeBuilder()
                .setSeconds(BigInteger.valueOf(seconds))
                .setNanoseconds(nanoSeconds)
                .build();
    }

    InstanceIdentifier<GateConfig> getGateConfigIID(PathLink pathLink) {
        return InstanceIdentifier.create(GateConfigManager.class)
                .child(GateConfig.class, new GateConfigKey(pathLink.getLinkSource().getSourceNode(),
                        pathLink.getLinkSource().getSourceTp()));
    }

    private AdminControlList getNewAdminControlList(GateConfigBuilder configBuilder, ConfigE2eGateInput input) {
        long adminControlListLength = configBuilder.getAdminControlListLength().longValue();

        long timeInterval = (long) (Math.pow(10,9)
                / configBuilder.getAdminCycletime().getDenominator().longValue()
                / adminControlListLength);
        //LOG.info("Admin control list time interval : {} ns", timeInterval);
        AdminControlListBuilder builder = new AdminControlListBuilder();
        builder.setTimeInterval(timeInterval);

        InstanceIdentifier<QueueTemplate> queueTemplateIID = InstanceIdentifier.create(QueueTemplate.class);
        List<TrafficClasses> trafficClassesList = DataOperator
                .readData(dataBroker, queueTemplateIID)
                .getTrafficClasses();
        short detnetQueues = 0;
        for (TrafficClasses trafficClasses : trafficClassesList) {
            if (trafficClasses.isDetnet()) {
                detnetQueues += Math.pow(2,trafficClasses.getTrafficClass().byteValue());
            }
        }
        short nonDetnetQueues = (short) (255 - detnetQueues);

        List<GateStates> gateStates = new ArrayList<GateStates>();
        if (null == configBuilder.getAdminControlList()) {
            //LOG.info("First time calculate admin control list for node: {}, tp: {} , cycle list length: {}",
                    //configBuilder.getNodeId(), configBuilder.getTpId(), adminControlListLength);
            for (long i = 0;i < adminControlListLength;i++) {
                gateStates.add(new GateStatesBuilder().setGateState(nonDetnetQueues).build());
            }
        } else {
            gateStates = configBuilder.getAdminControlList().getGateStates();
        }

        long e2eServiceBandwidth = input.getBandwidth().longValue();
        long bandwidthOfEachGate = getPortBandwidth(input.getTopologyId()) / adminControlListLength;
        long gateNumberToBeOpen = (e2eServiceBandwidth % bandwidthOfEachGate == 0)
                ? (e2eServiceBandwidth / bandwidthOfEachGate)
                : (e2eServiceBandwidth / bandwidthOfEachGate + 1);
        //LOG.info("Gate to be open for e2e service: {}", gateNumberToBeOpen);

        ListIterator<GateStates> listIterator = gateStates.listIterator();
        int index = 0;
        while (listIterator.hasNext() && gateNumberToBeOpen > 0) {
            GateStates gateState = listIterator.next();
            if ((gateState.getGateState().shortValue() & detnetQueues) == 0) {
                //LOG.info("Find a free interval, list index: {}", index);
                listIterator.set(new GateStatesBuilder()
                        .setGateState((short) (gateState.getGateState().shortValue()
                                + (short) Math.pow(2,input.getTrafficClass().doubleValue())))
                        .build());
                gateNumberToBeOpen --;
            } else {
                RpcReturnUtil.returnErr("Used gate state");
                //LOG.info("Used gate state: {}", Integer.toBinaryString(gateState.getGateState()));
            }
            index ++;
        }
        if (gateNumberToBeOpen > 0) {
            //LOG.info("No enough gates to open.");
            RpcReturnUtil.returnErr("No enough gates to open.");
        }
        return builder.setGateStates(gateStates).build();
    }

    @Override
    public ListenableFuture<RpcResult<DeleteE2eGateOutput>> deleteE2eGate(DeleteE2eGateInput input) {

        //LOG.info("Start delete gate for source port of each link!");
        for (PathLink pathLink : input.getPathLink()) {
            //LOG.info("Delete gate for link id: {}", pathLink.getLinkId());
            InstanceIdentifier<GateConfig> gateConfigIID = getGateConfigIID(pathLink);
            GateConfig gateConfig = DataOperator.readData(dataBroker, gateConfigIID);
            if (null == gateConfig) {
                //LOG.info("Delete e2e gate error.");
                return RpcReturnUtil.returnErr("Read e2e gate manager datastore failed!");
            }
            long adminControlListLength = gateConfig.getAdminControlListLength().longValue();
            long e2eServiceBandwidth = input.getBandwidth().longValue();
            long bandwidthOfEachGate = getPortBandwidth(input.getTopologyId()) / adminControlListLength;
            long gateNumberToBeClosed = (e2eServiceBandwidth % bandwidthOfEachGate == 0)
                    ? (e2eServiceBandwidth / bandwidthOfEachGate)
                    : (e2eServiceBandwidth / bandwidthOfEachGate + 1);
            //LOG.info("Gate number to be closed: {}", gateNumberToBeClosed);
            AdminControlList adminControlList = gateConfig.getAdminControlList();
            List<GateStates> gateStates = adminControlList.getGateStates();
            ListIterator<GateStates> listIterator = gateStates.listIterator();
            int index = 0;
            while (listIterator.hasNext() && gateNumberToBeClosed > 0) {
                GateStates gateState = listIterator.next();
                short trafficClassData = (short) Math.pow(2, input.getTrafficClass().doubleValue());
                if ((gateState.getGateState().intValue() & trafficClassData) != 0) {
                    //LOG.info("Find a opened interval, index: {}", index);
                    listIterator.set(new GateStatesBuilder()
                            .setGateState((short) (gateState.getGateState().intValue() - trafficClassData))
                            .build());
                    gateNumberToBeClosed--;
                }
                index ++;
            }
            if (gateNumberToBeClosed > 0) {
                //LOG.info("No enough gates to close.");
                return RpcReturnUtil.returnErr("No enough gates to close.");
            }
            AdminControlList newAdminControlList = new AdminControlListBuilder(adminControlList)
                    .setGateStates(gateStates)
                    .build();
            GateConfig newGateConfig = new GateConfigBuilder(gateConfig)
                    .setAdminControlList(newAdminControlList)
                    .build();
            if (!DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, gateConfigIID, newGateConfig)) {
                //LOG.info("Write datastore failed.");
                return RpcReturnUtil.returnErr("Write datastore failed.");
            }

            GateConfigParams gateConfigParams = new GateConfigParamsBuilder(newGateConfig)
                    .build();
            WriteGateConfigToSouthInput writeGateToSouthInput = new WriteGateConfigToSouthInputBuilder()
                    .setNodeId(gateConfig.getNodeId())
                    .setGateConfigParams(gateConfigParams)
                    .build();
            try {
                if (!detnetDriverApiService.writeGateConfigToSouth(writeGateToSouthInput).get().isSuccessful()) {
                    RpcReturnUtil.returnErr("Write gate config to south bound failed.");
                    //LOG.info("Write gate config to south bound failed, nodeId: {}", gateConfig.getNodeId());
                }
            } catch (InterruptedException | ExecutionException e) {
                RpcReturnUtil.returnErr("Write gate config to south bound failed.");
                //LOG.info(Arrays.toString(e.getStackTrace()));
            }
        }

        return RpcReturnUtil.returnSucess(new DeleteE2eGateOutputBuilder().build());
    }


    @Override
    public ListenableFuture<RpcResult<QueryGateParameterOutput>> queryGateParameter(QueryGateParameterInput input) {
        InstanceIdentifier<GateConfig> gateConfigIID = InstanceIdentifier.create(GateConfigManager.class)
                .child(GateConfig.class, new GateConfigKey(input.getNodeId(), input.getTpId()));
        GateConfig gateConfig = DataOperator.readData(dataBroker, gateConfigIID);
        QueryGateParameterOutput output = new QueryGateParameterOutputBuilder()
                .setGateParameter(new GateParameterBuilder(gateConfig).build())
                .build();

        return RpcReturnUtil.returnSucess(output);
    }

}
