/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.clock.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2DsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2DsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2DsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2PortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2PortDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2PortDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2TimePropertiesDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2TimePropertiesDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2TimePropertiesDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2DsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2DsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2DsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2PortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2PortDsOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2PortDsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2TimePropertiesInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2TimePropertiesOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2TimePropertiesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Detnet1588v2ApiService;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Query1588v2NodeConfigInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Query1588v2NodeConfigOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Query1588v2NodeConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.ds.input.DefaultDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.ds.input.DefaultDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.query._1588v2.node.config.output.InstanceListOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.query._1588v2.node.config.output.InstanceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.ClockIdentityType;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.Detnet1588v2Config;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.PtpDevice;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.PtpDeviceKey;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.ptp.device.InstanceList;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.ptp.device.InstanceListKey;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.DefaultDs;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.DefaultDsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.PortDsList;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.PortDsListBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.PortDsListKey;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.TimePropertiesDs;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.TimePropertiesDsBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detnet1588v2ServiceImpl implements Detnet1588v2ApiService {
    private static final Logger LOG = LoggerFactory.getLogger(Detnet1588v2ServiceImpl.class);
    private DataBroker dataBroker;

    public Detnet1588v2ServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }


    @Override
    public Future<RpcResult<Config1588v2TimePropertiesDsOutput>> config1588v2TimePropertiesDs(
            Config1588v2TimePropertiesDsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
            input.getTimePropertiesInput())).isInputIllegal()) {
            LOG.info("Config 1588v2 time properties default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Config 1588v2 time properties input error.");
            return RpcReturnUtil.returnSucess(new Config1588v2TimePropertiesDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        if (!isPtpSupported(input.getNodeId())) {
            ConfigureResult configureResult = RpcReturnUtil.getConfigResult(false, "Node do not support ptp.");
            return RpcReturnUtil.returnSucess(new Config1588v2TimePropertiesDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<TimePropertiesDs> timePropertiesDsIID = getInstanceListIID(
                input.getNodeId(), input.getInstanceNumber()).child(TimePropertiesDs.class);
        TimePropertiesDs timePropertiesDs = new TimePropertiesDsBuilder(input.getTimePropertiesInput()).build();
        if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, timePropertiesDsIID, timePropertiesDs)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Write time properties ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Config1588v2TimePropertiesDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Config1588v2TimePropertiesDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Config1588v2PortDsOutput>> config1588v2PortDs(Config1588v2PortDsInput input) {
        LOG.info("dsfdfd:" + input.getNodeId());
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
                input.getPortNumber(), input.getPortDsInput())).isInputIllegal()) {
            LOG.info("Config 1588v2 port default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Config 1588v2 port ds input error.");
            return RpcReturnUtil.returnSucess(new Config1588v2PortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        if (!isPtpSupported(input.getNodeId())) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Node do not support ptp.");
            return RpcReturnUtil.returnSucess(new Config1588v2PortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<PortDsList> portDsIID = getInstanceListIID(input.getNodeId(), input.getInstanceNumber())
                .child(PortDsList.class, new PortDsListKey(input.getPortNumber()));
        PortDsList portDsList = new PortDsListBuilder(input.getPortDsInput())
                .setPortNumber(input.getPortNumber())
                .setKey(new PortDsListKey(input.getPortNumber()))
                .build();
        if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, portDsIID, portDsList)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Write port ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Config1588v2PortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Config1588v2PortDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Delete1588v2DsOutput>> delete1588v2Ds(Delete1588v2DsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber()))
                .isInputIllegal()) {
            LOG.info("Delete 1588v2 default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete 1588v2 default ds input error!");
            return RpcReturnUtil.returnSucess(new Delete1588v2DsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<DefaultDs> defaultDsIID = getInstanceListIID(input.getNodeId(), input.getInstanceNumber())
                .child(DefaultDs.class);
        if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, defaultDsIID, null)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete default ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Delete1588v2DsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Delete1588v2DsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Query1588v2NodeConfigOutput>> query1588v2NodeConfig(Query1588v2NodeConfigInput input) {
        if (null == input.getNodeId()) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Input error.");
            return RpcReturnUtil.returnSucess(new Query1588v2NodeConfigOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<PtpDevice> ptpDeviceIID = InstanceIdentifier
                .create(Detnet1588v2Config.class)
                .child(PtpDevice.class, new PtpDeviceKey(input.getNodeId()));
        PtpDevice ptpDevice = DataOperator.readData(dataBroker, ptpDeviceIID);
        if (null == ptpDevice) {
            LOG.info("Ptp device of nodeId: {} not exist.", input.getNodeId());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Ptp device not exist.");
            return RpcReturnUtil.returnSucess(new Query1588v2NodeConfigOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        List<InstanceList> instanceList = ptpDevice.getInstanceList();
        List<InstanceListOutput> outputInstanceList = new ArrayList<>();
        for (InstanceList instance : instanceList) {
            String clockIdentity = null;
            if (null != instance.getDefaultDs() && null != instance.getDefaultDs().getClockIdentity()) {
                clockIdentity = getClockIdentityFromByteArray(instance.getDefaultDs().getClockIdentity().getValue());
            }
            InstanceListOutput instanceOutput = new InstanceListOutputBuilder(instance)
                    .setClockIdentity(clockIdentity)
                    .build();
            outputInstanceList.add(instanceOutput);
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        Query1588v2NodeConfigOutput queryNodeOutput = new Query1588v2NodeConfigOutputBuilder()
                .setInstanceListOutput(outputInstanceList)
                .setConfigureResult(configureResult)
                .build();
        return RpcReturnUtil.returnSucess(queryNodeOutput);
    }

    @Override
    public Future<RpcResult<Config1588v2DsOutput>> config1588v2Ds(Config1588v2DsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
                input.getDefaultDsInput())).isInputIllegal()) {
            LOG.info("Config 1588v2 default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Config 1588v2 default ds input error!");
            return RpcReturnUtil.returnSucess(new Config1588v2DsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        if (!isPtpSupported(input.getNodeId())) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Node do not support ptp!");
            return RpcReturnUtil.returnSucess(new Config1588v2DsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<DefaultDs> defaultDsIID = getInstanceListIID(input.getNodeId(), input.getInstanceNumber())
                .child(DefaultDs.class);

        byte[] clockIdentity = getClockIntentity(input.getClockIdentity());
        DefaultDsInput defaultDsInput = new DefaultDsInputBuilder(input.getDefaultDsInput())
                .setClockIdentity(new ClockIdentityType(clockIdentity))
                .build();
        DefaultDs defaultDs = new DefaultDsBuilder(defaultDsInput).build();
        if (!DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, defaultDsIID, defaultDs)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Write default ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Config1588v2DsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Config1588v2DsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Delete1588v2PortDsOutput>> delete1588v2PortDs(Delete1588v2PortDsInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber(),
                input.getPortNumber())).isInputIllegal()) {
            LOG.info("Delete 1588v2 port default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete 1588v2 port ds input error.");
            return RpcReturnUtil.returnSucess(new Delete1588v2PortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<PortDsList> portDsIID = getInstanceListIID(input.getNodeId(), input.getInstanceNumber())
                .child(PortDsList.class, new PortDsListKey(input.getPortNumber()));
        if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, portDsIID, null)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete port ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Delete1588v2PortDsOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Delete1588v2PortDsOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public Future<RpcResult<Delete1588v2TimePropertiesOutput>> delete1588v2TimeProperties(
            Delete1588v2TimePropertiesInput input) {
        DataCheck.CheckResult checkResult;
        if (!(checkResult = DataCheck.checkNotNull(input, input.getNodeId(), input.getInstanceNumber()))
                .isInputIllegal()) {
            LOG.info("Delete 1588v2 time properties default ds input error!" + checkResult.getErrorCause());
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete 1588v2 time properties input error.");
            return RpcReturnUtil.returnSucess(new Delete1588v2TimePropertiesOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        InstanceIdentifier<TimePropertiesDs> timePropertiesDsIID = getInstanceListIID(
                input.getNodeId(), input.getInstanceNumber()).child(TimePropertiesDs.class);
        if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, timePropertiesDsIID, null)) {
            ConfigureResult configureResult = RpcReturnUtil
                    .getConfigResult(false, "Delete time properties ds datastore failed.");
            return RpcReturnUtil.returnSucess(new Delete1588v2TimePropertiesOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new Delete1588v2TimePropertiesOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    public InstanceIdentifier<InstanceList> getInstanceListIID(String nodeId, int instanceNumber) {
        return InstanceIdentifier
                .create(Detnet1588v2Config.class)
                .child(PtpDevice.class, new PtpDeviceKey(nodeId))
                .child(InstanceList.class, new InstanceListKey(instanceNumber));
    }

    private boolean isPtpSupported(String nodeId) {
        InstanceIdentifier<PtpDevice> ptpDeviceIID = InstanceIdentifier
                .create(Detnet1588v2Config.class)
                .child(PtpDevice.class, new PtpDeviceKey(nodeId));
        PtpDevice ptpDevice = DataOperator.readData(dataBroker, ptpDeviceIID);
        if (null == ptpDevice || !ptpDevice.isPtpSupported()) {
            LOG.info("Node not exist or ptp not supported.");
            return false;
        }
        return true;
    }

    private byte[] getClockIntentity(String clockIdentityInput) {
        byte[] clockIdentity = new byte[8];
        byte[] nullClockIdentity = new byte[0];
        String[] strBytes = clockIdentityInput.split(":");
        if (strBytes.length > 8) {
            LOG.info("Clock identity length error.");
            return nullClockIdentity;
        }
        for (int index = 0;index < strBytes.length;index++) {
            Integer intValue = Integer.valueOf(strBytes[index]);
            if (intValue < 0 || intValue > 127) {
                LOG.info("Clock identity value error.");
                return nullClockIdentity;
            }
            byte byteValue = Byte.valueOf(strBytes[index]);

            clockIdentity[8 - strBytes.length + index] = byteValue;
        }
        return clockIdentity;
    }

    private String getClockIdentityFromByteArray(byte[] clockIdenityInput) {
        boolean flag = false;
        StringBuilder clockIdentity = new StringBuilder();
        for (int i = 0;i < 8;i++) {
            if (!flag && clockIdenityInput[i] == 0) {
                continue;
            }
            if (flag || clockIdenityInput[i] != 0) {
                flag = true;
                clockIdentity.append(":");
                clockIdentity.append(String.valueOf(clockIdenityInput[i]));
            }
        }
        return clockIdentity.substring(1);
    }
}
