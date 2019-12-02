/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.e2eservice.impl;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.NodeDataBroker;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.ConfigE2eBandwidthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DeleteE2eBandwidthInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DetnetBandwidthApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.DetnetEncapsulationType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.flow.type.L2FlowIdentfication;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.flow.type.group.flow.type.L3FlowIdentification;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.IpFlowType;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv4;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ip.flow.identification.ip.flow.type.Ipv6;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.ConfigE2eServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.ConfigE2eServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.ConfigE2eServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DeleteE2eServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DeleteE2eServiceOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DeleteE2eServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DetnetE2eServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServiceBandwidthInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServiceBandwidthOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServiceGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServiceGateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServicePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServicePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.QueryE2eServicePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.query.e2e.service.path.output.Listener;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.query.e2e.service.path.output.ListenerBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.query.e2e.service.path.output.ListenerKey;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.query.e2e.service.path.output.Talker;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.query.e2e.service.path.output.TalkerBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.ConfigE2eGateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DeleteE2eGateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DetnetGateApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.PceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.create.path.input.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.create.path.input.EgressBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraint;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.PriorityTrafficClassMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QosMappingTemplate;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.QueueTemplate;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplates;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplatesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Ipv4Dscps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Pri8021ps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4Dscp;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4DscpKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021p;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021pKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClasses;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.queue.template.TrafficClassesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.CreateDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.CreateDetnetServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DeleteDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DeleteDetnetServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DetnetServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.DetnetPath;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.DetnetPathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.DetnetPathKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.RelayNode;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.RelayNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.RelayNodeKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.detnet.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlow;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlowKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.E2eServiceManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.ResourcesPool;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.ResourcesPoolBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.group.Listeners;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.manager.E2eService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.manager.E2eServiceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.manager.rev180830.e2e.service.manager.E2eServiceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNodeType;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.DetnetTopologyKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNodeKey;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.Ltps;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.node.LtpsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.ConfigTsnServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DeleteTsnServiceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DetnetTsnServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItems;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItemsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.config.tsn.service.input.TsnForwardingItemsKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E2eServiceImpl implements DetnetE2eServiceApiService {

    private static final Logger LOG = LoggerFactory.getLogger(E2eServiceImpl.class);
    private DataBroker dataBroker;
    private RpcConsumerRegistry rpcConsumerRegistry;

    public E2eServiceImpl(DataBroker dataBroker, RpcConsumerRegistry rpcConsumerRegistry) {
        this.dataBroker = dataBroker;
        this.rpcConsumerRegistry = rpcConsumerRegistry;
    }

    @Override
    public ListenableFuture<RpcResult<ConfigE2eServiceOutput>> configE2eService(ConfigE2eServiceInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getTopologyId(), input.getDomainId(),
                input.getStreamId(), input.getFlowType(), input.getInterval(),
                input.getMaxPacketsPerInterval(), input.getMaxPayloadSize(), input.getListeners(),
                input.getSourceNode(), input.getSourceTp())).isInputIllegal()) {
            //LOG.info("Config e2e service input error:" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Config e2e service input error.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        if (!checkE2eServiceNotExist(input)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "E2e service already exist.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Check default qos template used.");
        String templateName = checkQosMappingTemplate();
        if (null == templateName) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Qos mapping template not specified correctly.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Calculate bandwidth required based on traffic specification.");
        long bandwidthRequired = calculateE2eServiceBandwidth(input);
        if (-1 == bandwidthRequired) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Input traffic specification error.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Calculate max latency based on user to network requirements.");
        long maxLatency = calculateE2eServiceMaxLatency(input);
        if (0 == maxLatency) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Input max latency not specified correctly.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Calculate traffic class based on priority and qos template.");
        short trafficClass = calculateE2eServiceTrafficClass(input, templateName);
        if (-1 == trafficClass) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Calculate traffic class failed.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Check whether traffic class queue is detnet.");
        boolean isTrafficClassDetnet = isTrafficClassQueueDetnet(trafficClass);
        if (!isTrafficClassDetnet) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Traffic class queue is not detnet.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Calculate e2e service path.");
        CreatePathOutput createPathOutput = createE2eServicePath(input, trafficClass, bandwidthRequired, maxLatency);
        if (null == createPathOutput || null == createPathOutput.getEgress()
                || createPathOutput.getEgress().isEmpty()) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Calculate path failed.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Get (vlan, mac) from pool, save to datastore and southbound.");
        if (!getVlanMacAddressPairFromPool(input)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Get (vlanId, macAddress) pair failed.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Check netconf of nodes in path connected!");
        String netconfNotConnected = checkNetconfConnected(createPathOutput);
        if (!netconfNotConnected.equals("")) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Failed, netconf nodes not connected: " + netconfNotConnected);
            deleteE2eServiceInfoAndPath(input);
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Distribute e2e service bandwidth and gate.");
        if (!distributeBandwidthAndGate(createPathOutput, input.getTopologyId(), bandwidthRequired, trafficClass)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Distribute bandwidth and gate failed.");
            deleteE2eServiceInfoAndPath(input);
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        long clientFlowId = getClientFlowId();
        //LOG.info("Split path segment, distribute tsn and detnet service.");
        for (org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress egress : createPathOutput.getEgress()) {
            //LOG.info("Distribute tsn and detnet for egress node: {}", egress.getEgressNodeId());
            if (!splitPathAndDsitributeTsnDetnetService(egress.getPath().getPathLink(), input, clientFlowId)) {
                ConfigureResult configureResult = RpcReturnUtil
                        .getConfigResult(false, "Distribute tsn service and detnet service failed.");
                deleteE2eServiceInfoAndPath(input);
                return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                        .setConfigureResult(configureResult).build());
            }
        }
        //LOG.info("Distribute tsn service and detnet service success.");

        //LOG.info("Save e2e service to datastore.");
        if (!saveE2eServiceToDataStore(input, trafficClass, bandwidthRequired)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Save e2e service to datastore failed.");
            return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        //LOG.info("Config e2e service of streamId:{} success.", input.getStreamId());
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new ConfigE2eServiceOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    private boolean checkE2eServiceNotExist(ConfigE2eServiceInput input) {
        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier
                .create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(
                        input.getDomainId(), input.getStreamId(), input.getTopologyId()));
        if (null != DataOperator.readData(dataBroker, e2eServiceIID)) {
            //LOG.info("E2e service of streamId:{} already exist.", input.getStreamId());
            return false;
        }
        return true;
    }

    private CreatePathOutput createE2eServicePath(ConfigE2eServiceInput input, short trafficClass,
                                                  long bandwidthRequired, long maxDelay) {
        PceApiService pceApiService = rpcConsumerRegistry.getRpcService(PceApiService.class);
        List<Egress> egressList = new ArrayList<Egress>();
        for (Listeners listener : input.getListeners()) {
            Egress egress = new EgressBuilder().setEgressNodeId(listener.getDestNode()).build();
            egressList.add(egress);
        }
        PathConstraint pathConstraint = new PathConstraintBuilder()
                .setMaxDelay(maxDelay)
                .setBandwidth(bandwidthRequired)
                .build();
        CreatePathInput createPathInput = new CreatePathInputBuilder()
                .setTopoId(input.getTopologyId())
                .setDomainId(input.getDomainId())
                .setStreamId(input.getStreamId())
                .setTrafficClass(trafficClass)
                .setPathConstraint(pathConstraint)
                .setIngressNodeId(input.getSourceNode())
                .setEgress(egressList)
                .build();
        try {
            RpcResult<CreatePathOutput> createPathOutputRpcResult = pceApiService.createPath(createPathInput).get();
            //TODO
            if (createPathOutputRpcResult.isSuccessful()) {
                //LOG.info("Create e2e service path success, path : {}", createPathOutputRpcResult.getResult());
                return createPathOutputRpcResult.getResult();
            }
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(e.getMessage());

        }
        //LOG.info("Calculate e2e service path failed.");
        return null;
    }

    private void deleteE2eServiceInfoAndPath(ConfigE2eServiceInput input) {
        deleteServiceInfo(input);
        PceApiService pceApiService = rpcConsumerRegistry.getRpcService(PceApiService.class);
        RemovePathInput removePathInput = new RemovePathInputBuilder()
                .setTopoId(input.getTopologyId())
                .setSrtreamId(input.getStreamId())
                .setDomainId(input.getDomainId())
                .setIngressNodeId(input.getSourceNode())
                .build();
        try {
            if (!pceApiService.removePath(removePathInput).get().isSuccessful()) {
                //LOG.info("Remove pce path failed.");
                RpcReturnUtil.returnErr(null);
            }
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
            RpcReturnUtil.returnErr(null);
        }
    }

    private long calculateE2eServiceBandwidth(ConfigE2eServiceInput input) {
        if (0 == input.getInterval().longValue() || 0 == input.getMaxPacketsPerInterval().longValue()
                || 0 == input.getMaxPayloadSize().longValue()) {
            LOG.info("Input traffic specification error.");
            return -1;
        }
        final long perFrameOverHead = 56;
        long interval = input.getInterval().longValue();
        long maxPacketsPerInterval = input.getMaxPacketsPerInterval().longValue();
        long maxPayloadSize = input.getMaxPayloadSize().longValue();
        float maxPacketsPerSecond = 1000 / interval * maxPacketsPerInterval;
        long bandwidthRequired = (long) ((maxPayloadSize + perFrameOverHead) * maxPacketsPerSecond * 8 / 1000 + 1);
        //LOG.info("Bandwidth required of e2e service: {}", bandwidthRequired);
        return bandwidthRequired;
    }

    private long calculateE2eServiceMaxLatency(ConfigE2eServiceInput input) {
        long maxLatency = input.getMaxLatency().longValue();
        for (Listeners listeners : input.getListeners()) {
            if (0 != listeners.getMaxLatency().longValue() && listeners.getMaxLatency().longValue() < maxLatency) {
                maxLatency = listeners.getMaxLatency().longValue();
            }
        }
        return maxLatency;
    }


    private short calculateE2eServiceTrafficClass(ConfigE2eServiceInput input, String templateName) {
        long priorityValue;
        InstanceIdentifier<MappingTemplates> mappingTemplatesIID = InstanceIdentifier
                .create(PriorityTrafficClassMapping.class)
                .child(MappingTemplates.class, new MappingTemplatesKey(templateName));
        if (input.getFlowType() instanceof L2FlowIdentfication) {
            L2FlowIdentfication l2NativeFlow = (L2FlowIdentfication) input.getFlowType();
            priorityValue = l2NativeFlow.getPcp().longValue();
            InstanceIdentifier<Pri8021p> pri8021pIID = mappingTemplatesIID
                    .child(Pri8021ps.class)
                    .child(Pri8021p.class, new Pri8021pKey(priorityValue));
            Pri8021p pri8021p = DataOperator.readData(dataBroker, pri8021pIID, LogicalDatastoreType.CONFIGURATION);
            if (null != pri8021p) {
                //LOG.info("Traffic class of e2e service: {}", pri8021p.getTrafficClass());
                return pri8021p.getTrafficClass().shortValue();
            }
        } else if (input.getFlowType() instanceof L3FlowIdentification) {
            L3FlowIdentification l3NativeFlow = (L3FlowIdentification) input.getFlowType();
            IpFlowType ipFlowType = l3NativeFlow.getIpFlowType();
            if (ipFlowType instanceof Ipv4) {
                Ipv4 ipv4NativeFlow = (Ipv4)ipFlowType;
                priorityValue = ipv4NativeFlow.getDscp().longValue();
            } else {
                Ipv6 ipv6NativeFlow = (Ipv6)ipFlowType;
                priorityValue = ipv6NativeFlow.getTrafficClass().shortValue();
            }
            InstanceIdentifier<Ipv4Dscp> ipv4DscpIID = mappingTemplatesIID
                    .child(Ipv4Dscps.class)
                    .child(Ipv4Dscp.class, new Ipv4DscpKey(priorityValue));
            Ipv4Dscp ipv4Dscp = DataOperator.readData(dataBroker, ipv4DscpIID, LogicalDatastoreType.CONFIGURATION);
            if (null != ipv4Dscp) {
                LOG.info("Traffic class of e2e service: {}", ipv4Dscp.getTrafficClass());
                return ipv4Dscp.getTrafficClass().shortValue();

            }
        }
        //LOG.info("Calculate e2e service traffic class failed.");
        return -1;
    }

    private String checkQosMappingTemplate() {
        InstanceIdentifier<QosMappingTemplate> qosTemplateIID = InstanceIdentifier
                .create(QosMappingTemplate.class);
        QosMappingTemplate qosMappingTemplate = DataOperator.readData(dataBroker, qosTemplateIID);
        if (null == qosMappingTemplate) {
            //LOG.info("Default qos template not specified.");
            return null;
        }
        String templateName = qosMappingTemplate.getTemplateName();
        InstanceIdentifier<MappingTemplates> mappingTemplatesIID = InstanceIdentifier
                .create(PriorityTrafficClassMapping.class)
                .child(MappingTemplates.class, new MappingTemplatesKey(templateName));
        MappingTemplates mappingTemplates = DataOperator.readData(dataBroker, mappingTemplatesIID);
        if (null == mappingTemplates) {
            //LOG.info("Mapping template of the specified template name not exist.");
            return null;
        }
        //LOG.info("Check specified qos mapping template OK, templateName: {}", templateName);
        return templateName;
    }

    private boolean isTrafficClassQueueDetnet(short trfficClass) {
        InstanceIdentifier<TrafficClasses> trafficClassesIID = InstanceIdentifier
                .create(QueueTemplate.class)
                .child(TrafficClasses.class, new TrafficClassesKey(trfficClass));
        TrafficClasses trafficClasses = DataOperator.readData(dataBroker, trafficClassesIID);
        if (null != trafficClasses && trafficClasses.isDetnet()) {
            ////LOG.info("The queue of trafficClass is detnet.");
            return true;
        }
        //LOG.info("The queue of trafficClass is not detnet.");
        return false;
    }

    private boolean getVlanMacAddressPairFromPool(ConfigE2eServiceInput input) {
        InstanceIdentifier<ResourcesPool> resourcesPoolIID = InstanceIdentifier.create(ResourcesPool.class);
        ResourcesPool resourcesPool = DataOperator.readData(dataBroker, resourcesPoolIID);
        if (null == resourcesPool) {
            resourcesPool = new ResourcesPoolBuilder()
                    .setClientFlowId(1L)
                    .setVlanId(10)
                    .setGroupMacAddress("01:00:5e:00:01:00")
                    .build();
            if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, resourcesPoolIID, resourcesPool)) {
                //LOG.info("");
                RpcReturnUtil.returnErr(null);
            }
        }
        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier.create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(input.getDomainId(),
                        input.getStreamId(), input.getTopologyId()));
        E2eService e2eService = new E2eServiceBuilder()
                .withKey(new E2eServiceKey(input.getDomainId(), input.getStreamId(), input.getTopologyId()))
                .setVlanId(resourcesPool.getVlanId())
                .setGroupMacAddress(resourcesPool.getGroupMacAddress())
                .build();
        if (null == e2eService || !DataOperator.writeData(DataOperator.OperateType.MERGE,
                dataBroker, e2eServiceIID, e2eService)) {
            //LOG.info("Get vlanId macAddress for e2e service failed.");
            return false;
        }
        ResourcesPool newVlanMacAddress = vlanMacAddressIncrease(resourcesPool);
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, resourcesPoolIID, newVlanMacAddress);
        //LOG.info("(VlanId,macAddress) for e2e service: {}.", resourcesPool);
        return true;
    }


    private void deleteServiceInfo(ConfigE2eServiceInput input) {
        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier.create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(input.getDomainId(),
                        input.getStreamId(), input.getTopologyId()));
        DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, e2eServiceIID, null);
    }

    private ResourcesPool vlanMacAddressIncrease(ResourcesPool vlanMacAddress) {
        String hexString = vlanMacAddress.getGroupMacAddress();
        String prefix = hexString.substring(0, 9);
        long newMac = Long.parseLong(hexString.replaceAll(":", ""), 16) + 1;
        String newHexMac = Long.toHexString(newMac);
        String suffix = newHexMac.substring(newHexMac.length() - 6);
        StringBuilder stringBuffer = new StringBuilder(suffix);
        stringBuffer.insert(2, ":");
        stringBuffer.insert(5, ":");
        return new ResourcesPoolBuilder(vlanMacAddress)
                .setGroupMacAddress(prefix + stringBuffer.toString())
                .build();
    }

    private String checkNetconfConnected(CreatePathOutput output) {
        Set<String> nodeIdSet = new HashSet<String>();
        List<PathLink> pathLinkList = getPathLinkList(output.getEgress());
        StringBuilder netconfNotConnected = new StringBuilder();
        for (PathLink pathLink : pathLinkList) {
            String sourceNode = pathLink.getLinkSource().getSourceNode();
            if (!nodeIdSet.contains(sourceNode)) {
                DataBroker sourceDataBroker = NodeDataBroker.getInstance().getNodeDataBroker(sourceNode);
                if (null == sourceDataBroker) {
                    nodeIdSet.add(sourceNode);
                    netconfNotConnected.append(sourceNode);
                    netconfNotConnected.append(", ");
                }
            }
            String destNode = pathLink.getLinkDest().getDestNode();
            if (!nodeIdSet.contains(destNode)) {
                DataBroker destDataBroker = NodeDataBroker.getInstance().getNodeDataBroker(destNode);
                if (null == destDataBroker) {
                    nodeIdSet.add(destNode);
                    netconfNotConnected.append(destNode);
                    netconfNotConnected.append(", ");
                }
            }
        }
        return netconfNotConnected.length() == 0 ? ""
                : netconfNotConnected.substring(0, netconfNotConnected.length() - 2);
    }

    private boolean distributeBandwidthAndGate(CreatePathOutput output, String topologyId,
                                               long bandwidthRequired, short trafficClass) {
        List<PathLink> pathLinkList = getPathLinkList(output.getEgress());
        DetnetBandwidthApiService bandwidthApiService = rpcConsumerRegistry
                .getRpcService(DetnetBandwidthApiService.class);
        ConfigE2eBandwidthInput configE2eBandwidthInput = new ConfigE2eBandwidthInputBuilder()
                .setPathLink(pathLinkList)
                .setTopologyId(topologyId)
                .setBandwidth(bandwidthRequired)
                .setTrafficClass(trafficClass)
                .build();
        try {
            //LOG.info("Distribute bandwidth, input: {}", configE2eBandwidthInput);
            if (!bandwidthApiService.configE2eBandwidth(configE2eBandwidthInput).get().isSuccessful()) {
                //LOG.info("Config e2e service bandwidth failed.");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        DetnetGateApiService gateApiService = rpcConsumerRegistry.getRpcService(DetnetGateApiService.class);
        ConfigE2eGateInput configE2eGateInput = new ConfigE2eGateInputBuilder()
                .setPathLink(pathLinkList)
                .setTopologyId(topologyId)
                .setBandwidth(bandwidthRequired)
                .setTrafficClass(trafficClass)
                .build();
        try {
            //LOG.info("Distribute gate, input: {}", configE2eGateInput);
            if (!gateApiService.configE2eGate(configE2eGateInput).get().isSuccessful()) {
                //LOG.info("Config e2e service gate failed.");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        ///LOG.info("Config e2e service bandwidth and gate success.");
        return true;
    }

    public boolean splitPathAndDsitributeTsnDetnetService(List<PathLink> pathLinks, ConfigE2eServiceInput input,
                                                           long clientFlowId) {
        List<PathLink> pathLinkList = new ArrayList<PathLink>();
        List<RelayNode> relayNodeList = new ArrayList<RelayNode>();
        boolean isTsnSegment = false;
        boolean isDetnetSegment = false;
        int index = 0;
        for (PathLink pathLink : pathLinks) {
            String sourceNode = pathLink.getLinkSource().getSourceNode();
            String sourceInTp = null;
            if (index != 0) {
                sourceInTp = pathLinks.get(index - 1).getLinkDest().getDestTp();
            }
            String sourceOutTp = pathLink.getLinkSource().getSourceTp();
            String destNode = pathLink.getLinkDest().getDestNode();
            String destInTp = pathLink.getLinkDest().getDestTp();
            String destOutTp = null;
            if (index != pathLinks.size() - 1) {
                destOutTp = pathLinks.get(index + 1).getLinkSource().getSourceTp();
            }
            DetnetNodeType sourceNodeType = getNodeTypeInDomain(sourceNode, sourceInTp, sourceOutTp, input);
            DetnetNodeType destNodeType = getNodeTypeInDomain(destNode, destInTp, destOutTp, input);
            if (null == sourceNodeType || null == destNodeType) {
                //LOG.info("Node type in domain not specified.");
                return false;
            }
            pathLinkList.add(pathLink);
            index++;
            if (sourceNodeType.equals(DetnetNodeType.Edge) && destNodeType.equals(DetnetNodeType.Edge)) {
                DetnetEncapsulationType sourceOutEncap = getEncapsulationType(sourceNode, sourceOutTp, input);
                if (sourceOutEncap.equals(DetnetEncapsulationType.Tsn)) {
                    isTsnSegment = true;
                    //LOG.info("First pathlink in tsn segment: {}.", pathLink.getLinkId());
                } else {
                    isDetnetSegment = true;
                    //LOG.info("First pathlink in detnet segment: {}", pathLink.getLinkId());
                }
            }

            if (sourceNodeType.equals(DetnetNodeType.Edge) && destNodeType.equals(DetnetNodeType.Bridge)) {
                isTsnSegment = true;
                //LOG.info("First pathlink in tsn segment: {}.", pathLink.getLinkId());
            }
            if (isTsnSegment && destNodeType.equals(DetnetNodeType.Edge)) {
                isTsnSegment = false;
                //LOG.info("Last pathlink in tsn segment: {}.", pathLink.getLinkId());
                //LOG.info("Pathlink list of tsn segment: {}", pathLinkList);
                if (!distributeTsnService(pathLinkList, input)) {
                    return false;
                }
                pathLinkList.clear();
            }


            if (sourceNodeType.equals(DetnetNodeType.Edge) && (destNodeType.equals(DetnetNodeType.Relay)
                    || destNodeType.equals(DetnetNodeType.Transit))) {
                isDetnetSegment = true;
                //LOG.info("First pathlink in detnet segment: {}", pathLink.getLinkId());
            }
            if (sourceNodeType.equals(DetnetNodeType.Relay)) {
                String outTp = pathLink.getLinkSource().getSourceTp();
                DetnetEncapsulationType outEncapsulation = getEncapsulationType(sourceNode, outTp, input);
                String inTp = pathLinkList.get(pathLinkList.size() - 2).getLinkDest().getDestTp();
                DetnetEncapsulationType inEncapsulation = getEncapsulationType(sourceNode, inTp, input);
                //LOG.info("Relay node: {}, inTp:{}, outTp:{}", sourceNode, inTp, outTp);
                RelayNode relayNode = new RelayNodeBuilder()
                        .withKey(new RelayNodeKey(sourceNode))
                        .setRelayNodeId(sourceNode)
                        .setInEncapsulation(inEncapsulation)
                        .setOutEncapsulation(outEncapsulation)
                        .build();
                relayNodeList.add(relayNode);
            }
            if (isDetnetSegment && destNodeType.equals(DetnetNodeType.Edge)) {
                isDetnetSegment = false;
                //LOG.info("Last pathlink in detnet segment: {}.", pathLink.getLinkId());
                //LOG.info("Pathlink list of detnet segment: {}", pathLinkList);
                if (!distributeDetnetService(pathLinkList, relayNodeList, input, clientFlowId)) {
                    return false;
                }
                pathLinkList.clear();
            }
        }
        return true;
    }

    private DetnetEncapsulationType getEncapsulationType(String nodeId, String tpId, ConfigE2eServiceInput input) {
        InstanceIdentifier<Ltps> ltpsIID = InstanceIdentifier
                .create(DetnetNetworkTopology.class)
                .child(DetnetTopology.class, new DetnetTopologyKey(input.getTopologyId()))
                .child(DetnetNode.class, new DetnetNodeKey(nodeId))
                .child(Ltps.class, new LtpsKey(tpId));
        Ltps ltps = DataOperator.readData(dataBroker, ltpsIID);
        if (null == ltps) {
            //LOG.info("Get ltp encapsulation failed.");
            return null;
        }
        return ltps.getDetnetEncapsulationType();
    }

    private DetnetNodeType getNodeTypeInDomain(String nodeId, String inTp, String outTp, ConfigE2eServiceInput input) {
        DetnetEncapsulationType inEncap = null;
        DetnetEncapsulationType outEncap = null;
        if (null != inTp) {
            inEncap  = getEncapsulationType(nodeId, inTp, input);
        }
        if (null != outTp) {
            outEncap = getEncapsulationType(nodeId, outTp, input);
        }
        if (null == inEncap
                || null == outEncap
                || inEncap.equals(DetnetEncapsulationType.Tsn) && !outEncap.equals(DetnetEncapsulationType.Tsn)
                || !inEncap.equals(DetnetEncapsulationType.Tsn) && outEncap.equals(DetnetEncapsulationType.Tsn)) {
            return DetnetNodeType.Edge;
        }
        if (inEncap.equals(outEncap)) {
            if (inEncap.equals(DetnetEncapsulationType.Tsn)) {
                return DetnetNodeType.Bridge;
            }
            InstanceIdentifier<DetnetNode> detnetNodeIID = InstanceIdentifier
                    .create(DetnetNetworkTopology.class)
                    .child(DetnetTopology.class, new DetnetTopologyKey(input.getTopologyId()))
                    .child(DetnetNode.class, new DetnetNodeKey(nodeId));
            DetnetNode detnetNode = DataOperator.readData(dataBroker, detnetNodeIID);
            if (null != detnetNode && detnetNode.isIsRelayNode()) {
                return DetnetNodeType.Relay;
            }
            return DetnetNodeType.Transit;
        }
        return DetnetNodeType.Relay;
    }

    private boolean distributeTsnService(List<PathLink> pathLinkList, ConfigE2eServiceInput input) {
        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier
                .create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(
                        input.getDomainId(), input.getStreamId(), input.getTopologyId()));
        E2eService e2eService = DataOperator.readData(dataBroker, e2eServiceIID);
        List<TsnForwardingItems> forwardingItemsList = new ArrayList<TsnForwardingItems>();
        for (PathLink pathLink : pathLinkList) {
            String sourceNode = pathLink.getLinkSource().getSourceNode();
            String sourceTp = pathLink.getLinkSource().getSourceTp();
            TsnForwardingItems forwardingItems = new TsnForwardingItemsBuilder()
                    .withKey(new TsnForwardingItemsKey(sourceNode, sourceTp))
                    .setNodeId(sourceNode)
                    .setOutPort(sourceTp)
                    .build();
            forwardingItemsList.add(forwardingItems);
        }
        ConfigTsnServiceInput configTsnServiceInput = new ConfigTsnServiceInputBuilder()
                .setTsnForwardingItems(forwardingItemsList)
                .setVlanId(e2eService.getVlanId())
                .setGroupMacAddress(e2eService.getGroupMacAddress())
                .build();
        //LOG.info("Config tsn service input: {}", configTsnServiceInput);
        DetnetTsnServiceApiService tsnServiceApiService = rpcConsumerRegistry.getRpcService(
                DetnetTsnServiceApiService.class);
        try {
            if (!tsnServiceApiService.configTsnService(configTsnServiceInput).get().isSuccessful()) {
                //LOG.info("Distribute tsn service failed.");
                return  false;
            }
        } catch (InterruptedException | ExecutionException e) {
            ///LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return true;
    }

    private long getClientFlowId() {
        InstanceIdentifier<ResourcesPool> resourcesPoolIID = InstanceIdentifier.create(ResourcesPool.class);
        long clientFlowId = DataOperator.readData(dataBroker, resourcesPoolIID).getClientFlowId().longValue();
        ResourcesPool resourcesPool = new ResourcesPoolBuilder()
                .setClientFlowId(clientFlowId + 1)
                .build();
        DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, resourcesPoolIID, resourcesPool);
        return clientFlowId;
    }

    private boolean distributeDetnetService(List<PathLink> pathLinkList, List<RelayNode> relayNodeList,
                                            ConfigE2eServiceInput input, long clientFlowId) {

        ClientFlow clientFlow = new ClientFlowBuilder()
                .withKey(new ClientFlowKey(clientFlowId))
                .setClientFlowId(clientFlowId)
                .setFlowType(input.getFlowType())
                .build();
        List<ClientFlow> clientFlowList = new ArrayList<ClientFlow>();
        clientFlowList.add(clientFlow);

        String ingressNode = pathLinkList.get(0).getLinkSource().getSourceNode();
        String egressNode = pathLinkList.get(pathLinkList.size() - 1).getLinkDest().getDestNode();
        DetnetPath detnetPath = new DetnetPathBuilder()
                .withKey(new DetnetPathKey(egressNode, ingressNode))
                .setIngressNode(ingressNode)
                .setEgressNode(egressNode)
                .setPath(new PathBuilder().setPathLink(pathLinkList).build())
                .build();
        List<DetnetPath> detnetPathList = new ArrayList<DetnetPath>();
        detnetPathList.add(detnetPath);

        CreateDetnetServiceInput createDetnetServiceInput = new CreateDetnetServiceInputBuilder()
                .setDomainId(input.getDomainId())
                .setStreamId(input.getStreamId())
                .setClientFlow(clientFlowList)
                .setDetnetPath(detnetPathList)
                .setRelayNode(relayNodeList)
                .build();
        //LOG.info("Config detnet service input: {}", createDetnetServiceInput);
        DetnetServiceApiService detnetServiceApiService = rpcConsumerRegistry.getRpcService(
                DetnetServiceApiService.class);
        try {
            if (!detnetServiceApiService.createDetnetService(createDetnetServiceInput).get().isSuccessful()) {
                //LOG.info("Distribute service failed.");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return true;
    }

    private boolean saveE2eServiceToDataStore(ConfigE2eServiceInput input, short trafficClass, long bandwidthRequired) {
        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier
                .create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(input.getDomainId(), input.getStreamId(),
                        input.getTopologyId()));
        E2eService e2eService = new E2eServiceBuilder(input)
                .setTrafficClass(trafficClass)
                .setBandwidthRequired(bandwidthRequired)
                .build();
        if (!DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, e2eServiceIID, e2eService)) {
            //LOG.info("Save e2e service to datastore failed.");
            return false;
        }
        return true;
    }

    @Override
    public ListenableFuture<RpcResult<QueryE2eServiceBandwidthOutput>> queryE2eServiceBandwidth(
            QueryE2eServiceBandwidthInput input) {
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<DeleteE2eServiceOutput>> deleteE2eService(DeleteE2eServiceInput input) {
        ConfigureResult configureResult = null;
        if (null == input.getTopologyId() || null == input.getDomainId() || null == input.getStreamId()) {
            //LOG.info("Delete e2e service input error.");
            configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e service input error.");
            return RpcReturnUtil.returnSucess(
                    new DeleteE2eServiceOutputBuilder().setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier
                .create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(
                        input.getDomainId(), input.getStreamId(), input.getTopologyId()));
        E2eService e2eService = DataOperator.readData(dataBroker, e2eServiceIID);
        if (null == e2eService) {
            //LOG.info("E2e service of streamId:{} not exist.", input.getStreamId());
            configureResult = RpcReturnUtil.getConfigResult(false, "E2e service not exist.");
            return RpcReturnUtil.returnSucess(
                    new DeleteE2eServiceOutputBuilder().setConfigureResult(configureResult).build());
        }
        QueryPathInput queryPathInput = new QueryPathInputBuilder()
                .setTopoId(input.getTopologyId())
                .setDomainId(input.getDomainId())
                .setStreamId(input.getStreamId())
                .build();
        PceApiService pceApiService = rpcConsumerRegistry.getRpcService(PceApiService.class);
        try {
            QueryPathOutput queryPathOutput = pceApiService.queryPath(queryPathInput).get().getResult();
            List<PathLink> pathLinkList = getPathLinkList(queryPathOutput.getEgress());
            if (!deleteE2eServiceBandwidth(e2eService, pathLinkList)) {
                //LOG.info("Delete e2e bandwidth failed.");
                configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e bandwidth failed.");
            }
            if (!deleteE2eServiceGate(e2eService, pathLinkList)) {
                //LOG.info("Delete e2e gate failed.");
                configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e gate failed.");
            }
            if (!deleteE2eTsnService(e2eService, pathLinkList)) {
                //LOG.info("Delete e2e tsn service failed.");
                configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e tsn service failed.");
            }
            if (!deleteDetnetService(e2eService)) {
                //LOG.info("Delete e2e detnet service failed.");
                configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e detnet service failed.");
            }
            if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, e2eServiceIID, null)) {
                //LOG.info("Delete e2e service from datastore failed.");
                configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e service from datastore failed.");
            }

            RemovePathInput removePathInput = new RemovePathInputBuilder()
                    .setTopoId(e2eService.getTopologyId())
                    .setSrtreamId(e2eService.getStreamId())
                    .setDomainId(e2eService.getDomainId())
                    .setIngressNodeId(e2eService.getSourceNode())
                    .build();
            RemovePathOutput removePathOutput = pceApiService.removePath(removePathInput).get().getResult();
            if (null != removePathOutput.getEgress() && 0 != removePathOutput.getEgress().size()) {
                //LOG.info("Delete e2e service path failed.");
                configureResult = RpcReturnUtil.getConfigResult(false, "Delete e2e service path failed.");
            }

        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        if (null == configureResult) {
            //LOG.info("Delete e2e service of streamId:{} success.", input.getStreamId());
            configureResult = RpcReturnUtil.getConfigResult(true, "");
        }
        return RpcReturnUtil.returnSucess(
                new DeleteE2eServiceOutputBuilder().setConfigureResult(configureResult).build());
    }


    private boolean deleteE2eServiceBandwidth(E2eService e2eService, List<PathLink> pathLinkList) {
        DetnetBandwidthApiService bandwidthService = rpcConsumerRegistry.getRpcService(DetnetBandwidthApiService.class);
        DeleteE2eBandwidthInput deleteBandwidthInput = new DeleteE2eBandwidthInputBuilder()
                .setTopologyId(e2eService.getTopologyId())
                .setTrafficClass(e2eService.getTrafficClass())
                .setBandwidth(e2eService.getBandwidthRequired())
                .setPathLink(pathLinkList)
                .build();
        try {
            return bandwidthService.deleteE2eBandwidth(deleteBandwidthInput).get().isSuccessful();
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    private boolean deleteE2eServiceGate(E2eService e2eService, List<PathLink> pathLinkList) {
        DetnetGateApiService detnetGateApiService = rpcConsumerRegistry.getRpcService(DetnetGateApiService.class);
        DeleteE2eGateInput deleteE2eGateInput = new DeleteE2eGateInputBuilder()
                .setTopologyId(e2eService.getTopologyId())
                .setTrafficClass(e2eService.getTrafficClass())
                .setBandwidth(e2eService.getBandwidthRequired())
                .setPathLink(pathLinkList)
                .build();
        try {
            return detnetGateApiService.deleteE2eGate(deleteE2eGateInput).get().isSuccessful();
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    private boolean deleteE2eTsnService(E2eService e2eService, List<PathLink> pathLinkList) {
        List<String> tsnNodes = new ArrayList<String>();
        for (PathLink pathLink : pathLinkList) {
            tsnNodes.add(pathLink.getLinkSource().getSourceNode());
        }
        DetnetTsnServiceApiService tsnApiService = rpcConsumerRegistry.getRpcService(DetnetTsnServiceApiService.class);
        DeleteTsnServiceInput deleteTsnServiceInput = new DeleteTsnServiceInputBuilder()
                .setVlanId(e2eService.getVlanId())
                .setGroupMacAddress(e2eService.getGroupMacAddress())
                .setTsnNodes(tsnNodes)
                .build();
        try {
            return tsnApiService.deleteTsnService(deleteTsnServiceInput).get().isSuccessful();
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    private boolean deleteDetnetService(E2eService e2eService) {
        DetnetServiceApiService detnetApiService = rpcConsumerRegistry.getRpcService(DetnetServiceApiService.class);
        DeleteDetnetServiceInput deleteDetnetServiceInput = new DeleteDetnetServiceInputBuilder()
                .setDomainId(e2eService.getDomainId())
                .setStreamId(e2eService.getStreamId())
                .build();
        try {
            return detnetApiService.deleteDetnetService(deleteDetnetServiceInput).get().isSuccessful();
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    private List<PathLink> getPathLinkList(List<org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress>
                                                   egressList) {
        List<PathLink> pathLinkList = new ArrayList<PathLink>();
        for (org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress egress : egressList) {
            for (PathLink pathLink : egress.getPath().getPathLink()) {
                if (!pathLinkList.contains(pathLink)) {
                    pathLinkList.add(pathLink);
                }
            }
        }
        return pathLinkList;
    }

    @Override
    public ListenableFuture<RpcResult<QueryE2eServiceGateOutput>> queryE2eServiceGate(QueryE2eServiceGateInput input) {
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<QueryE2eServicePathOutput>> queryE2eServicePath(QueryE2eServicePathInput input) {
        if (null == input.getTopologyId() || null == input.getDomainId() || null == input.getStreamId()) {
            //LOG.info("Query e2e service path input error.");
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Query e2e service path input error.");
            return RpcReturnUtil.returnSucess(new QueryE2eServicePathOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<E2eService> e2eServiceIID = InstanceIdentifier
                .create(E2eServiceManager.class)
                .child(E2eService.class, new E2eServiceKey(
                        input.getDomainId(), input.getStreamId(), input.getTopologyId()));
        E2eService e2eService = DataOperator.readData(dataBroker, e2eServiceIID);
        if (null == e2eService) {
            //LOG.info("E2e service of streamId:{} not exist.", input.getStreamId());
            ConfigureResult configureResult = RpcReturnUtil.getConfigResult(false, "E2e service not exist.");
            return RpcReturnUtil.returnSucess(new QueryE2eServicePathOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        Talker talker = new TalkerBuilder()
                .setNodeId(e2eService.getSourceNode())
                .setTpId(e2eService.getSourceTp())
                .build();
        List<Listener> listenerList = new ArrayList<Listener>();
        for (Listeners listeners : e2eService.getListeners()) {
            Listener listener = new ListenerBuilder()
                    .withKey(new ListenerKey(listeners.getDestNode()))
                    .setNodeId(listeners.getDestNode())
                    .setTpId(listeners.getDestTp())
                    .build();
            listenerList.add(listener);
        }

        PceApiService pceApiService = rpcConsumerRegistry.getRpcService(PceApiService.class);
        QueryPathInput queryPathInput = new QueryPathInputBuilder()
                .setTopoId(input.getTopologyId())
                .setDomainId(input.getDomainId())
                .setStreamId(input.getStreamId())
                .build();
        try {
            QueryPathOutput queryPathOutput = pceApiService.queryPath(queryPathInput).get().getResult();
            List<String> linksList = new ArrayList<String>();
            for (org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.Egress egress : queryPathOutput
                    .getEgress()) {
                for (PathLink pathLink : egress.getPath().getPathLink()) {
                    if (!linksList.contains(pathLink.getLinkId())) {
                        linksList.add(pathLink.getLinkId());
                    }
                }
            }
            ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
            QueryE2eServicePathOutput queryE2eServicePathOutput = new QueryE2eServicePathOutputBuilder()
                    .setConfigureResult(configureResult)
                    .setTalker(talker)
                    .setListener(listenerList)
                    .setLinks(linksList)
                    .build();
            return RpcReturnUtil.returnSucess(queryE2eServicePathOutput);
        } catch (InterruptedException | ExecutionException e) {
            //LOG.info(Arrays.toString(e.getStackTrace()));
        }

        ConfigureResult configureResult = RpcReturnUtil
                .getConfigResult(false, "Query e2e service path exception occurred.");
        return RpcReturnUtil.returnSucess(new QueryE2eServicePathOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

}
