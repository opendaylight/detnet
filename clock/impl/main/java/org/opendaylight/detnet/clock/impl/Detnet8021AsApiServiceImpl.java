/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.clock.impl;

import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevPortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevPortDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevPortDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevPortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevPortDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevPortDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Detnet8021asRevApiService;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.Detnet8021asRevConfig;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828._default.ds.entry.DefaultDataSet;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828._default.ds.entry.DefaultDataSetBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.GptpDevice;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.GptpDeviceKey;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.GptpInstance;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.GptpInstanceKey;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.gptp.instance.PortDataSet;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.gptp.instance.PortDataSetBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.gptp.instance.PortDataSetKey;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detnet8021AsApiServiceImpl implements Detnet8021asRevApiService {
    private static final Logger LOG = LoggerFactory.getLogger(Detnet8021AsApiServiceImpl.class);
    private DataBroker dataBroker;

    public Detnet8021AsApiServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public Future<RpcResult<Config8021asRevPortDsOutput>> config8021asRevPortDs(Config8021asRevPortDsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
                input.getPortNumber(), input.getPortDsInput())).isInputIllegal()) {
            LOG.info("Config 8021As port ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Config 8021as port ds input error.");
            return RpcReturnUtil.returnSucess(new Config8021asRevPortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        if (!isGptpSupported(input.getNodeId())) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Node do not support gPTP.");
            return RpcReturnUtil.returnSucess(new Config8021asRevPortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<PortDataSet> portDsIID = getGptpInstanceIID(input.getNodeId(), input.getInstanceNumber())
                .child(PortDataSet.class, new PortDataSetKey(input.getPortNumber()));
        PortDataSet portDataSet = new PortDataSetBuilder(input.getPortDsInput())
                .setPortNumber(input.getPortNumber())
                .setKey(new PortDataSetKey(input.getPortNumber()))
                .build();
        if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, portDsIID, portDataSet)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Write port ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Config8021asRevPortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Config8021asRevPortDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Config8021asRevDsOutput>> config8021asRevDs(Config8021asRevDsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
                input.getDefaultDsInput())).isInputIllegal()) {
            LOG.info("Config 8021As default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Config 8021As default ds input error!");
            return RpcReturnUtil.returnSucess(new Config8021asRevDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        if (!isGptpSupported(input.getNodeId())) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Node do not support gPTP.");
            return RpcReturnUtil.returnSucess(new Config8021asRevDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<DefaultDataSet> defaultDsIID = getGptpInstanceIID(input.getNodeId(),
                input.getInstanceNumber()).child(DefaultDataSet.class);
        DefaultDataSet defaultDataSet = new DefaultDataSetBuilder(input.getDefaultDsInput()
                .getDefaultDataSet()).build();
        if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, defaultDsIID, defaultDataSet)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Write port ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Config8021asRevDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Config8021asRevDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Delete8021asRevPortDsOutput>> delete8021asRevPortDs(Delete8021asRevPortDsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
                input.getPortNumber())).isInputIllegal()) {
            LOG.info("Delete 8021As port ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete 8021As port ds input error!");
            return RpcReturnUtil.returnSucess(new Delete8021asRevPortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<PortDataSet> portDsIID = getGptpInstanceIID(input.getNodeId(), input.getInstanceNumber())
                .child(PortDataSet.class, new PortDataSetKey(input.getPortNumber()));
        if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, portDsIID, null)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete port ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Delete8021asRevPortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Delete8021asRevPortDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Delete8021asRevDsOutput>> delete8021asRevDs(Delete8021asRevDsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber()))
                .isInputIllegal()) {
            LOG.info("Delete 8021As default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete 8021As default ds input error!");
            return RpcReturnUtil.returnSucess(new Delete8021asRevDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<DefaultDataSet> defaultDataSetIID = getGptpInstanceIID(input.getNodeId(),
                input.getInstanceNumber()).child(DefaultDataSet.class);
        if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, defaultDataSetIID, null)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete port ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Delete8021asRevDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Delete8021asRevDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    public InstanceIdentifier<GptpInstance> getGptpInstanceIID(String nodeId, short instanceNumber) {
        return InstanceIdentifier.create(Detnet8021asRevConfig.class)
                .child(GptpDevice.class, new GptpDeviceKey(nodeId))
                .child(GptpInstance.class, new GptpInstanceKey(instanceNumber));
    }

    private boolean isGptpSupported(String nodeId) {
        InstanceIdentifier<GptpDevice> gptpDeviceIID = InstanceIdentifier
                .create(Detnet8021asRevConfig.class)
                .child(GptpDevice.class, new GptpDeviceKey(nodeId));
        GptpDevice gptpDevice = DataOperator.readData(dataBroker, gptpDeviceIID);
        if (null == gptpDevice || !gptpDevice.isGptpSupported()) {
            LOG.info("Node not exist or gptp not supported.");
            return false;
        }
        return true;
    }
}
