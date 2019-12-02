/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.qos.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.ConfigMappingTemplateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.ConfigMappingTemplateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.DeleteMappingTemplateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.DeleteMappingTemplateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.QueryMappingTemplateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.QueryMappingTemplateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.QueryMappingTemplateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.PriorityTrafficClassMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.mapping.group.PriorityMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.mapping.group.PriorityMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplates;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplatesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Ipv4Dscps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Pri8021ps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4Dscp;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021p;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QosServiceImplTest extends AbstractConcurrentDataBrokerTest {
    private static final Logger LOG = LoggerFactory.getLogger(QosServiceImplTest.class);
    private static final String TEMPLATE_NAME = "Test-mapping-template";
    DataBroker dataBroker;
    QosServiceImpl qosService;

    @Before
    public void init() {
        dataBroker = getDataBroker();
        qosService = new QosServiceImpl(dataBroker);
    }

    @Test
    public void qosServiceImplTest() {

        List<PriorityMapping> priorityMappingList = new ArrayList<PriorityMapping>();
        priorityMappingList.add(getPriorityMapping((short) 0, 0, 0, 7));
        priorityMappingList.add(getPriorityMapping((short) 1, 1, 8, 15));
        priorityMappingList.add(getPriorityMapping((short) 2, 5, 40, 47));
        priorityMappingList.add(getPriorityMapping((short) 3, 6, 48, 55));
        priorityMappingList.add(getPriorityMapping((short) 4, 2, 16, 23));
        priorityMappingList.add(getPriorityMapping((short) 5, 3, 24, 31));
        priorityMappingList.add(getPriorityMapping((short) 6, 4, 32, 39));
        priorityMappingList.add(getPriorityMapping((short) 7, 7, 56, 63));


        ConfigMappingTemplateInput configMappingTemplateInput = new ConfigMappingTemplateInputBuilder()
                .setTemplateName(TEMPLATE_NAME)
                .setPriorityMapping(priorityMappingList)
                .build();
        qosService.configMappingTemplate(configMappingTemplateInput);
        InstanceIdentifier<MappingTemplates> templatesIID = InstanceIdentifier
                .create(PriorityTrafficClassMapping.class)
                .child(MappingTemplates.class, new MappingTemplatesKey(TEMPLATE_NAME));
        MappingTemplates mappingTemplates = DataOperator.readData(dataBroker, templatesIID);
        assertNotNull(mappingTemplates);
        Pri8021ps pri8021ps = mappingTemplates.getPri8021ps();
        assertEquals(8, pri8021ps.getPri8021p().size());
        List<Long> expected1 = new ArrayList<Long>();
        for (long i = 0;i < 8;i++) {
            expected1.add(i);
        }
        List<Long> actual1 = new ArrayList<Long>();
        for (Pri8021p pri8021p : pri8021ps.getPri8021p()) {
            actual1.add(pri8021p.getValue8021p().longValue());
        }
        assertEquals(true, expected1.containsAll(actual1));
        assertEquals(true, actual1.containsAll(expected1));

        Ipv4Dscps ipv4Dscps = mappingTemplates.getIpv4Dscps();
        List<Long> expected2 = new ArrayList<Long>();
        for (long i = 0;i < 64;i++) {
            expected2.add(i);
        }
        List<Long> actual2 = new ArrayList<Long>();
        for (Ipv4Dscp ipv4Dscp : ipv4Dscps.getIpv4Dscp()) {
            actual2.add(ipv4Dscp.getDscpValue().longValue());
        }
        assertEquals(true, expected2.containsAll(actual2));
        assertEquals(true, actual2.containsAll(expected2));
        assertEquals(64, ipv4Dscps.getIpv4Dscp().size());
        LOG.info("Test config qos mapping template success.");

        QueryMappingTemplateInput queryMappingTemplateInput = new QueryMappingTemplateInputBuilder()
                .setTemplateName(TEMPLATE_NAME)
                .build();
        try {
            QueryMappingTemplateOutput output = qosService.queryMappingTemplate(queryMappingTemplateInput)
                    .get().getResult();
            assertEquals(8, output.getPriorityMapping().size());
        } catch (InterruptedException | ExecutionException e) {
            LOG.info(Arrays.toString(e.getStackTrace()));
        }
        LOG.info("Test query qos mapping template success.");

        DeleteMappingTemplateInput deleteMappingTemplateInput = new DeleteMappingTemplateInputBuilder()
                .setTemplateName(TEMPLATE_NAME)
                .build();

        qosService.deleteMappingTemplate(deleteMappingTemplateInput);

        MappingTemplates mappingTemplates1 = DataOperator.readData(dataBroker, templatesIID);
        assertEquals(null, mappingTemplates1);

        priorityMappingList.remove(getPriorityMapping((short) 3, 6, 48, 55));
        priorityMappingList.add(getPriorityMapping((short) 3, 6, 49, 57));

        configMappingTemplateInput = new ConfigMappingTemplateInputBuilder(configMappingTemplateInput)
                .setPriorityMapping(priorityMappingList)
                .build();
        qosService.configMappingTemplate(configMappingTemplateInput);
        mappingTemplates1 = DataOperator.readData(dataBroker, templatesIID);
        assertEquals(null, mappingTemplates1);
        LOG.info("Test delete qos mapping template success.");
    }



    private PriorityMapping getPriorityMapping(short trafficClass, long pcpValue, long dscpLeft, long dscpRight) {
        List<Uint32> dscpValues = new ArrayList<Uint32>();
        for (long i = dscpLeft;i <= dscpRight;i++) {
            dscpValues.add(Uint32.valueOf(i));
        }
        List<Uint32> pcpValues = new ArrayList<Uint32>();
        pcpValues.add(Uint32.valueOf(pcpValue));
        return new PriorityMappingBuilder()
                .setTrafficClass(trafficClass)
                .setPcpValues(pcpValues)
                .setDscpValues(dscpValues)
                .build();
    }
}
