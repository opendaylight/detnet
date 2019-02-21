/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.clock.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2DsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2DsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2PortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2PortDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2TimePropertiesDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Config1588v2TimePropertiesDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2DsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2DsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2PortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2PortDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2TimePropertiesInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Delete1588v2TimePropertiesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Query1588v2NodeConfigInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Query1588v2NodeConfigInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Query1588v2NodeConfigOutput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.ds.input.DefaultDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.ds.input.DefaultDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.port.ds.input.PortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.port.ds.input.PortDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.time.properties.ds.input.TimePropertiesInput;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.config._1588v2.time.properties.ds.input.TimePropertiesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.ClockIdentityType;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.DelayMechanismEnumeration;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.Detnet1588v2Config;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.PtpDevice;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.PtpDeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.detnet._1588v2.config.PtpDeviceKey;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.DefaultDs;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.PortDsList;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.PortDsListKey;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.instance.list.group.TimePropertiesDs;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.rev180828.port.ds.entry.PortIdentityBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Detnet1588v2ServiceImplTest extends AbstractConcurrentDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(Detnet1588v2ServiceImplTest.class);
    private DataBroker dataBroker;
    private Detnet1588v2ServiceImpl detnet1588v2Service;

    @Before
    public void init() {
        dataBroker = getDataBroker();
        detnet1588v2Service = new Detnet1588v2ServiceImpl(dataBroker);

        mockPtpSupported("001", "002");
    }

    @Test
    public void detnet1588v2DefaultDsTest() throws ExecutionException, InterruptedException {
        //ClockIdentityType clockIdentityType = new ClockIdentityType(new byte[]{0,0,0,0,0,0,0,1});
        DefaultDsInput defaultDsInput = new DefaultDsInputBuilder()
                .setDomainNumber((short) 1)
                .setClockIdentity(null)
                .setPriority1((short) 248)
                .build();
        Config1588v2DsInput config1588v2DsInput = new Config1588v2DsInputBuilder()
                .setNodeId("001")
                .setDefaultDsInput(defaultDsInput)
                .build();
        detnet1588v2Service.config1588v2Ds(config1588v2DsInput);
        InstanceIdentifier<DefaultDs> defaultDsIID = detnet1588v2Service.getInstanceListIID("001", 1)
                .child(DefaultDs.class);
        DefaultDs defaultDs = DataOperator.readData(dataBroker, defaultDsIID);
        assertEquals(null, defaultDs);
        config1588v2DsInput = new Config1588v2DsInputBuilder(config1588v2DsInput)
                .setInstanceNumber(1)
                .setClockIdentity("1:0:15:0:1")
                .build();
        detnet1588v2Service.config1588v2Ds(config1588v2DsInput);
        defaultDs = DataOperator.readData(dataBroker, defaultDsIID);
        assertNotNull(defaultDs);
        assertEquals(Short.valueOf("1"), defaultDs.getDomainNumber());
        assertEquals(Short.valueOf("248"), defaultDs.getPriority1());
        assertEquals(Arrays.toString(new byte[]{0, 0, 0, 1, 0, (byte) 15, 0, 1}), Arrays.toString(
                defaultDs.getClockIdentity().getValue()));
        assertEquals(null, defaultDs.getVersionNumber());

        Query1588v2NodeConfigInput query1588v2NodeConfigInput = new Query1588v2NodeConfigInputBuilder()
                .setNodeId("001")
                .build();
        Query1588v2NodeConfigOutput query1588v2NodeConfigOutput = detnet1588v2Service.query1588v2NodeConfig(
                query1588v2NodeConfigInput).get().getResult();
        assertNotNull(query1588v2NodeConfigOutput.getInstanceListOutput());
        assertEquals("1:0:15:0:1", query1588v2NodeConfigOutput.getInstanceListOutput().get(0).getClockIdentity());

        config1588v2DsInput = new Config1588v2DsInputBuilder()
                .setNodeId("002")
                .setInstanceNumber(1)
                .setDefaultDsInput(defaultDsInput)
                .build();
        detnet1588v2Service.config1588v2Ds(config1588v2DsInput);
        defaultDsIID = detnet1588v2Service.getInstanceListIID("002", 1)
                .child(DefaultDs.class);
        defaultDs = DataOperator.readData(dataBroker, defaultDsIID);
        assertEquals(null, defaultDs);

        LOG.info("Test config default ds success.");
        Delete1588v2DsInput delete1588v2DsInput = new Delete1588v2DsInputBuilder()
                .setNodeId("001")
                .setInstanceNumber(1)
                .build();
        detnet1588v2Service.delete1588v2Ds(delete1588v2DsInput);
        defaultDs = DataOperator.readData(dataBroker, defaultDsIID);
        assertEquals(null, defaultDs);
        LOG.info("Test delete default ds success.");
    }

    @Test
    public void detnet1588v2TimePropertiesDsTest() {
        TimePropertiesInput timePropertiesInput = new TimePropertiesInputBuilder()
                .setTimeSource((short) 6)
                .setPtpTimescale(true)
                .build();
        Config1588v2TimePropertiesDsInput configTimePropertiesDsInput = new Config1588v2TimePropertiesDsInputBuilder()
                .setNodeId("001")
                .setTimePropertiesInput(timePropertiesInput)
                .build();
        detnet1588v2Service.config1588v2TimePropertiesDs(configTimePropertiesDsInput);
        InstanceIdentifier<TimePropertiesDs> timePropertiesDsIID = detnet1588v2Service.getInstanceListIID("001", 1)
                .child(TimePropertiesDs.class);
        TimePropertiesDs timePropertiesDs = DataOperator.readData(dataBroker, timePropertiesDsIID);
        assertEquals(null, timePropertiesDs);
        configTimePropertiesDsInput = new Config1588v2TimePropertiesDsInputBuilder(configTimePropertiesDsInput)
                .setInstanceNumber(1)
                .build();
        detnet1588v2Service.config1588v2TimePropertiesDs(configTimePropertiesDsInput);
        timePropertiesDs = DataOperator.readData(dataBroker, timePropertiesDsIID);
        assertNotNull(timePropertiesDs);
        Delete1588v2TimePropertiesInput deleteTimePropertiesInput = new Delete1588v2TimePropertiesInputBuilder()
                .setNodeId("001")
                .setInstanceNumber(1)
                .build();
        detnet1588v2Service.delete1588v2TimeProperties(deleteTimePropertiesInput);
        timePropertiesDs = DataOperator.readData(dataBroker, timePropertiesDsIID);
        assertEquals(null, timePropertiesDs);
        LOG.info("Test delete time properties success.");
    }

    @Test
    public void detnet1588v2PortDsTest() {
        ClockIdentityType clockIdentityType = new ClockIdentityType(new byte[]{0,0,0,0,0,0,0,1});
        PortDsInput portDsInput = new PortDsInputBuilder()
                .setPortIdentity(new PortIdentityBuilder().setClockIdentity(clockIdentityType).setPortNumber(1).build())
                .setDelayMechanism(DelayMechanismEnumeration.P2P)
                .setUnderlyingInterface("fei-0/1/0/1")
                .setAnnounceReceiptTimeout((short) 20)
                .build();
        Config1588v2PortDsInput config1588v2PortDsInput = new Config1588v2PortDsInputBuilder()
                .setInstanceNumber(1)
                .setPortNumber(1)
                .setPortDsInput(portDsInput)
                .build();
        detnet1588v2Service.config1588v2PortDs(config1588v2PortDsInput);
        InstanceIdentifier<PortDsList> portDsIID = detnet1588v2Service.getInstanceListIID("001", 1)
                .child(PortDsList.class, new PortDsListKey(1));
        PortDsList portDsList = DataOperator.readData(dataBroker, portDsIID);
        assertEquals(null, portDsList);
        config1588v2PortDsInput = new Config1588v2PortDsInputBuilder(config1588v2PortDsInput)
                .setNodeId("001")
                .setClockIdentity("1:0:15:0:1")
                .build();
        detnet1588v2Service.config1588v2PortDs(config1588v2PortDsInput);
        portDsList = DataOperator.readData(dataBroker, portDsIID);
        assertNotNull(portDsList);
        assertEquals((Integer) 1, portDsList.getPortNumber());
        assertEquals(DelayMechanismEnumeration.P2P, portDsList.getDelayMechanism());
//        assertEquals(Arrays.toString(new byte[]{0, 0, 0, 1, 0, (byte) 15, 0, 1}), Arrays.toString(
//                portDsList.getPortIdentity().getClockIdentity().getValue()));
        LOG.info("Test config port ds success.");

        Delete1588v2PortDsInput delete1588v2PortDsInput = new Delete1588v2PortDsInputBuilder()
                .setNodeId("001")
                .setInstanceNumber(1)
                .setPortNumber(1)
                .build();
        detnet1588v2Service.delete1588v2PortDs(delete1588v2PortDsInput);
        portDsList = DataOperator.readData(dataBroker, portDsIID);
        assertEquals(null, portDsList);
        LOG.info("Test delete port ds success.");
    }

    private void mockPtpSupported(String nodeId1, String nodeId2) {
        InstanceIdentifier<PtpDevice> ptpDeviceIID = InstanceIdentifier
                .create(Detnet1588v2Config.class)
                .child(PtpDevice.class, new PtpDeviceKey(nodeId1));
        PtpDevice ptpDevice = new PtpDeviceBuilder()
                .setPtpSupported(true)
                .setNodeId(nodeId1)
                .setKey(new PtpDeviceKey(nodeId1))
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, ptpDeviceIID, ptpDevice);
        InstanceIdentifier<PtpDevice> ptpDeviceIID2 = InstanceIdentifier
                .create(Detnet1588v2Config.class)
                .child(PtpDevice.class, new PtpDeviceKey(nodeId2));
        PtpDevice ptpDevice2 = new PtpDeviceBuilder()
                .setPtpSupported(false)
                .setNodeId(nodeId2)
                .setKey(new PtpDeviceKey(nodeId2))
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, ptpDeviceIID2, ptpDevice2);
    }
}
