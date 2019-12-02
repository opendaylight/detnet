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

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevPortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Config8021asRevPortDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevPortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Delete8021asRevPortDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.config._8021as.rev.ds.input.DefaultDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.config._8021as.rev.ds.input.DefaultDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.config._8021as.rev.port.ds.input.PortDsInput;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.config._8021as.rev.port.ds.input.PortDsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.Detnet8021asRevConfig;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828._default.ds.entry.DefaultDataSet;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828._default.ds.entry.DefaultDataSetBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.GptpDevice;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.GptpDeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.GptpDeviceKey;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.gptp.instance.PortDataSet;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.rev180828.detnet._8021as.rev.config.gptp.device.gptp.instance.PortDataSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detnet8021AsRevImplTest extends AbstractConcurrentDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(Detnet8021AsRevImplTest.class);
    private DataBroker dataBroker;
    private Detnet8021AsApiServiceImpl detnet8021AsService;

    @Before
    public void init() {
        dataBroker = getDataBroker();
        detnet8021AsService = new Detnet8021AsApiServiceImpl(dataBroker);
        mockGptpSupported("001", "002");
    }

    @Test
    public void configDefautDsTest() {
        DefaultDataSet defaultDataSet = new DefaultDataSetBuilder()
                .setDomainNumber((short) 1)
                .setExternalPortConfiguration(false)
                .setPriority1((long) 6)
                .build();
        DefaultDsInput defaultDsInput = new DefaultDsInputBuilder()
                .setDefaultDataSet(defaultDataSet)
                .build();
        Config8021asRevDsInput config8021asRevDsInput = new Config8021asRevDsInputBuilder()
                .setInstanceNumber((short) 1)
                .setDefaultDsInput(defaultDsInput)
                .build();
        detnet8021AsService.config8021asRevDs(config8021asRevDsInput);
        InstanceIdentifier<DefaultDataSet> defaultDataSetIID = detnet8021AsService.getGptpInstanceIID("001", (short) 1)
                .child(DefaultDataSet.class);
        DefaultDataSet defaultDataSet1 = DataOperator.readData(dataBroker, defaultDataSetIID);
        assertEquals(null, defaultDataSet1);
        config8021asRevDsInput = new Config8021asRevDsInputBuilder(config8021asRevDsInput)
                .setNodeId("001")
                .build();
        detnet8021AsService.config8021asRevDs(config8021asRevDsInput);
        defaultDataSet1 = DataOperator.readData(dataBroker, defaultDataSetIID);
       // assertNotNull(defaultDataSet1);
        //assertEquals(Short.valueOf("1"), defaultDataSet1.getDomainNumber());
        Delete8021asRevDsInput delete8021asRevDsInput = new Delete8021asRevDsInputBuilder()
                .setNodeId("001")
                .setInstanceNumber((short) 1)
                .build();
        detnet8021AsService.delete8021asRevDs(delete8021asRevDsInput);
        defaultDataSet1 = DataOperator.readData(dataBroker, defaultDataSetIID);
        assertEquals(null, defaultDataSet1);

        config8021asRevDsInput = new Config8021asRevDsInputBuilder(config8021asRevDsInput)
                .setNodeId("002")
                .build();
        detnet8021AsService.config8021asRevDs(config8021asRevDsInput);
        defaultDataSetIID = detnet8021AsService.getGptpInstanceIID("002", (short) 1)
                .child(DefaultDataSet.class);
        defaultDataSet1 = DataOperator.readData(dataBroker, defaultDataSetIID);
        assertEquals(null, defaultDataSet1);
        LOG.info("Test config default ds success.");

    }

    @Test
    public void config8021PortDsTest() {
        PortDsInput portDsInput = new PortDsInputBuilder()
                .setPttPortEnabled(true)
                .build();
        Config8021asRevPortDsInput config8021asRevPortDsInput = new Config8021asRevPortDsInputBuilder()
                .setNodeId("001")
                .setPortNumber((long) 1)
                .setPortDsInput(portDsInput)
                .build();
        detnet8021AsService.config8021asRevPortDs(config8021asRevPortDsInput);
        InstanceIdentifier<PortDataSet> portDataSetIID = detnet8021AsService.getGptpInstanceIID("001", (short) 1)
                .child(PortDataSet.class, new PortDataSetKey((long) 1));
        PortDataSet portDataSet = DataOperator.readData(dataBroker, portDataSetIID);
        assertEquals(null, portDataSet);
        config8021asRevPortDsInput = new Config8021asRevPortDsInputBuilder(config8021asRevPortDsInput)
                .setInstanceNumber((short) 1)
                .build();
        detnet8021AsService.config8021asRevPortDs(config8021asRevPortDsInput);
        portDataSet = DataOperator.readData(dataBroker, portDataSetIID);
        assertNotNull(portDataSet);
        assertEquals(true, portDataSet.isPttPortEnabled());
        Delete8021asRevPortDsInput delete8021asRevPortDsInput = new Delete8021asRevPortDsInputBuilder()
                .setNodeId("001")
                .setInstanceNumber((short) 1)
                .setPortNumber((long) 1)
                .build();
        detnet8021AsService.delete8021asRevPortDs(delete8021asRevPortDsInput);
        portDataSet = DataOperator.readData(dataBroker, portDataSetIID);
        assertEquals(null, portDataSet);
        LOG.info("Test config port ds success.");
    }

    private void mockGptpSupported(String nodeId1, String nodeId2) {
        InstanceIdentifier<GptpDevice> gptpDeviceIID = InstanceIdentifier
                .create(Detnet8021asRevConfig.class)
                .child(GptpDevice.class, new GptpDeviceKey(nodeId1));
        GptpDevice gptpDevice = new GptpDeviceBuilder()
                .setNodeId(nodeId1)
                .setGptpSupported(true)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, gptpDeviceIID, gptpDevice);

        InstanceIdentifier<GptpDevice> gptpDeviceIID2 = InstanceIdentifier
                .create(Detnet8021asRevConfig.class)
                .child(GptpDevice.class, new GptpDeviceKey(nodeId2));
        GptpDevice gptpDevice2 = new GptpDeviceBuilder()
                .setNodeId(nodeId2)
                .setGptpSupported(false)
                .build();
        DataOperator.writeData(DataOperator.OperateType.PUT, dataBroker, gptpDeviceIID2, gptpDevice2);
    }
}
