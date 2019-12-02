/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.qos.impl;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.detnet.common.util.DataCheck;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.ConfigMappingTemplateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.ConfigMappingTemplateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.ConfigMappingTemplateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.DeleteMappingTemplateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.DeleteMappingTemplateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.DeleteMappingTemplateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.DetnetQosTemplateApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.QueryMappingTemplateInput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.QueryMappingTemplateOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.api.rev180906.QueryMappingTemplateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.PriorityTrafficClassMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.mapping.group.PriorityMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplates;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.MappingTemplatesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Ipv4Dscps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.Pri8021ps;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.TcToPriorityMapping;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.TcToPriorityMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4Dscp;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4DscpBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.ipv4.dscps.Ipv4DscpKey;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021p;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021pBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.qos.template.rev180903.priority.traffic._class.mapping.mapping.templates.pri._8021ps.Pri8021pKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QosServiceImpl implements DetnetQosTemplateApiService {

    private static final Logger LOG = LoggerFactory.getLogger(QosServiceImpl.class);
    private DataBroker dataBroker;

    public QosServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public ListenableFuture<RpcResult<ConfigMappingTemplateOutput>> configMappingTemplate(
            ConfigMappingTemplateInput input) {

        boolean nullParameter = DataCheck.checkNotNull(input, input.getTemplateName(), input.getPriorityMapping())
                .isInputIllegal();
        if (!nullParameter || !isInputComplete(input)) {
            ConfigureResult configureResult = RpcReturnUtil.getConfigResult(
                    false, "Config qos template input error.");
            return RpcReturnUtil.returnSucess(new ConfigMappingTemplateOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }

        InstanceIdentifier<MappingTemplates> mappingTemplatesIID = getMappingTemplatesIID(input.getTemplateName());

        for (PriorityMapping priorityMapping : input.getPriorityMapping()) {
            for (Uint32 pcpValue : priorityMapping.getPcpValues()) {
                //LOG.info("Config mapping for pcp: {}", pcpValue);
                InstanceIdentifier<Pri8021p> pri8021pIID = mappingTemplatesIID
                        .child(Pri8021ps.class)
                        .child(Pri8021p.class, new Pri8021pKey(pcpValue));
                Pri8021p pri8021p = new Pri8021pBuilder()
                        .setValue8021p(pcpValue)
                        .setTrafficClass(priorityMapping.getTrafficClass())
                        .build();
                if (!DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, pri8021pIID, pri8021p)) {
                    //LOG.info("Write datastore : {} failed!", pcpValue);
                    ConfigureResult configureResult = RpcReturnUtil.getConfigResult(
                            false, "Write datastore failed.");
                    return RpcReturnUtil.returnSucess(new ConfigMappingTemplateOutputBuilder()
                            .setConfigureResult(configureResult).build());
                }
            }

            for (Uint32 dscpValue : priorityMapping.getDscpValues()) {
                //LOG.info("Config mapping for dscp: {}", dscpValue);
                InstanceIdentifier<Ipv4Dscp> ipv4DscpIID = mappingTemplatesIID
                        .child(Ipv4Dscps.class)
                        .child(Ipv4Dscp.class, new Ipv4DscpKey(dscpValue));
                Ipv4Dscp ipv4Dscp = new Ipv4DscpBuilder()
                        .setDscpValue(dscpValue)
                        .setTrafficClass(priorityMapping.getTrafficClass())
                        .build();
                if (!DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, ipv4DscpIID, ipv4Dscp)) {
                    //LOG.info("Write datastore : {} failed!", dscpValue);
                    ConfigureResult configureResult = RpcReturnUtil.getConfigResult(
                            false, "Write datastore failed.");
                    return RpcReturnUtil.returnSucess(new ConfigMappingTemplateOutputBuilder()
                            .setConfigureResult(configureResult).build());
                }
            }
        }
        InstanceIdentifier<TcToPriorityMapping> tcToPriorityMappingIID = mappingTemplatesIID
                .child(TcToPriorityMapping.class);
        TcToPriorityMapping tcToPriorityMapping = new TcToPriorityMappingBuilder()
                .setPriorityMapping(input.getPriorityMapping())
                .build();
        DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, tcToPriorityMappingIID, tcToPriorityMapping);
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new ConfigMappingTemplateOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    @Override
    public ListenableFuture<RpcResult<QueryMappingTemplateOutput>> queryMappingTemplate(
            QueryMappingTemplateInput input) {
        InstanceIdentifier<MappingTemplates> mappingTemplatesIID = getMappingTemplatesIID(input.getTemplateName());
        if (null == DataOperator.readData(dataBroker, mappingTemplatesIID)) {
            //LOG.info("Query failed, not exist.");
            return RpcReturnUtil.returnSucess(new QueryMappingTemplateOutputBuilder().build());
        }
        InstanceIdentifier<TcToPriorityMapping> tcToPriorityMappingIID = getMappingTemplatesIID(input.getTemplateName())
                .child(TcToPriorityMapping.class);
        List<PriorityMapping> priorityMappings = DataOperator.readData(dataBroker, tcToPriorityMappingIID)
                .getPriorityMapping();
        QueryMappingTemplateOutput output = new QueryMappingTemplateOutputBuilder()
                .setPriorityMapping(priorityMappings)
                .build();
        return RpcReturnUtil.returnSucess(output);
    }

    @Override
    public ListenableFuture<RpcResult<DeleteMappingTemplateOutput>> deleteMappingTemplate(
            DeleteMappingTemplateInput input) {
        InstanceIdentifier<MappingTemplates> mappingTemplatesIID = getMappingTemplatesIID(input.getTemplateName());
        if (null == DataOperator.readData(dataBroker, mappingTemplatesIID)) {
            //LOG.info("Delete failed, not exist.");
            ConfigureResult configureResult = RpcReturnUtil.getConfigResult(
                    false, "Template not exist.");
            return RpcReturnUtil.returnSucess(new DeleteMappingTemplateOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        if (!DataOperator.writeData(DataOperator.OperateType.DELETE, dataBroker, mappingTemplatesIID, null)) {
            ConfigureResult configureResult = RpcReturnUtil.getConfigResult(
                    false, "Delete mapping template failed.");
            return RpcReturnUtil.returnSucess(new DeleteMappingTemplateOutputBuilder()
                    .setConfigureResult(configureResult).build());
        }
        ConfigureResult configureResult = RpcReturnUtil.getConfigResult(true, "");
        return RpcReturnUtil.returnSucess(new DeleteMappingTemplateOutputBuilder()
                .setConfigureResult(configureResult).build());
    }

    private boolean isInputComplete(ConfigMappingTemplateInput input) {
        List<PriorityMapping> priorityMappings = input.getPriorityMapping();
        short pri8021pCount = 0;
        List<Long> expect = new ArrayList<Long>();
        for (long i = 0;i < 64;i++) {
            expect.add(i);
        }
        List<Long> actual = new ArrayList<Long>();
        for (PriorityMapping priorityMapping : priorityMappings) {
            for (Uint32 pcpValue : priorityMapping.getPcpValues()) {
                pri8021pCount += (short) (1 << pcpValue.shortValue());
            }
            for (Uint32 ipv4Dscp : priorityMapping.getDscpValues()) {
                actual.add(ipv4Dscp.longValue());
            }
        }

        if (255 != pri8021pCount) {
           // LOG.info("Input error : 8021p.");
            return false;
        }
        if (!expect.containsAll(actual) || !actual.containsAll(expect)) {
            //LOG.info("Input error : ipv4Dscp.");
            return false;
        }
        return true;
    }

    private InstanceIdentifier<MappingTemplates> getMappingTemplatesIID(String templateName) {
        return InstanceIdentifier
                .create(PriorityTrafficClassMapping.class)
                .child(MappingTemplates.class, new MappingTemplatesKey(templateName));
    }

}
