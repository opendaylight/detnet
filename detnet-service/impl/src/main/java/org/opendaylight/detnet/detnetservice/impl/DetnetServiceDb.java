/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.detnetservice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.detnet.common.util.DataOperator;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.PreofType;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DeleteDetnetServiceConfigurationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInput;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.WriteDetnetServiceConfigurationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.DetnetServiceInstanceManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.DeviceDetnetServiceManager;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.SequenceNumberType;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlow;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.DetnetServices;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.DetnetServicesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetFlows;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetFlowsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetTransportTunnels;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.DetnetTransportTunnelsKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceMappingInstance;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceMappingInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceMappingInstanceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceProxyInstance;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.info.ServiceProxyInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.Services;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.ServicesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.services.ServiceMappingInstances;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.services.ServiceMappingInstancesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.services.ServiceMappingInstancesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.services.ServiceProxyInstances;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.services.ServiceProxyInstancesBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.services.ServiceProxyInstancesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.mapping.instance.InSegmentBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.mapping.instance.OutSegmentBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.Nodes;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.NodesKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.nodes.DomainService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.nodes.DomainServiceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.device.detnet.service.manager.nodes.DomainServiceKey;
import org.opendaylight.yang.gen.v1.urn.detnet.service.resource.rev181204.DetnetServiceResource;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.DetnetNetworkTopology;
import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DetnetServiceDb {
    private DataBroker dataBroker;
    private DetnetDriverApiService driverApiService;
    private static final Logger LOG = LoggerFactory.getLogger(DetnetServiceDb.class);
    private static DetnetServiceDb instance = new DetnetServiceDb();

    public DetnetServiceDb() {
    }

    public static DetnetServiceDb getInstance() {
        return instance;
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public String writeIngressProxyInstanceToDB(String node, Long proxyInstanceId, ServiceInstance serviceInstance,
                                               List<ClientFlow> clientFlow) {
        LOG.info("writeIngressProxyInstanceToDB");
        SegmentPathKey pathKey = serviceInstance.getSegmentPathKey();
        Integer domainId = pathKey.getDomainId();
        Long streamId = pathKey.getStreamId();
        writeProxyInstanceToServiceManagerDB(node, domainId, streamId, proxyInstanceId);
        return writeProxyInstanceToDeviceManagerDB(node, domainId, streamId, proxyInstanceId, serviceInstance,
                clientFlow);
    }

    private String writeProxyInstanceToDeviceManagerDB(String node, Integer domainId, Long streamId,
                                                       Long proxyInstanceId, ServiceInstance serviceInstance,
                                                       List<ClientFlow> clientFlow) {
        ServiceProxyInstance proxyInstance;
        DomainServiceBuilder domainServiceBuilder = new DomainServiceBuilder();
        WriteDetnetServiceConfigurationInputBuilder inputBuilder = new WriteDetnetServiceConfigurationInputBuilder();
        inputBuilder.setNodeId(node);
        inputBuilder.setStreamId(streamId);
        domainServiceBuilder.setDomainId(domainId);
        domainServiceBuilder.setStreamId(streamId);
        if (clientFlow != null && !clientFlow.isEmpty()) {
            domainServiceBuilder.setClientFlow(clientFlow);
            inputBuilder.setClientFlow(clientFlow);
            proxyInstance = new ServiceProxyInstanceBuilder()
                    .setServiceProxyInstanceId(proxyInstanceId)
                    .setSequenceNumberMode(SequenceNumberType.Generation)
                    .setServiceOperation(PreofType.NONE)
                    .setDetnetServices(createServiceInstance(serviceInstance))
                    .setClientFlowId(getClientFlowIds(clientFlow))
                    .build();
        } else {
            proxyInstance = new ServiceProxyInstanceBuilder()
                    .setServiceProxyInstanceId(proxyInstanceId)
                    .setServiceOperation(PreofType.NONE)
                    .setDetnetServices(createServiceInstance(serviceInstance))
                    .build();
        }
        List<ServiceProxyInstance> proxyInstanceList = new ArrayList<>();
        proxyInstanceList.add(proxyInstance);
        inputBuilder.setServiceProxyInstance(proxyInstanceList);
        domainServiceBuilder.setServiceProxyInstance(proxyInstanceList);

        InstanceIdentifier<DomainService> domainServicePath = buildDomainServicePath(node, domainId, streamId);
        DomainService domainService = domainServiceBuilder.build();
        DataOperator.writeData(DataOperator.OperateType.MERGE, dataBroker, domainServicePath, domainService);
        LOG.info("writeProxyInstanceToDeviceManagerDB: nodeId-" + node + " ,domain-service--" + domainService);

        Future<RpcResult<Void>> resultFuture = driverApiService.writeDetnetServiceConfiguration(inputBuilder.build());
        try {
            if (!resultFuture.get().isSuccessful()) {
                return resultFuture.get().getErrors().iterator().next().getMessage();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("write detnet service proxy instance to node error! Exception: " + e.getMessage());
            return e.getMessage();
        }
        return "";
    }

    private void writeProxyInstanceToServiceManagerDB(String node, Integer domainId, Long streamId,
                                                      Long proxyInstanceId) {
        ServiceProxyInstances proxyInstances = new ServiceProxyInstancesBuilder()
                .setServiceProxyInstanceId(proxyInstanceId)
                .setNodeId(node)
                .build();
        InstanceIdentifier<ServiceProxyInstances> proxyInstancesPath = buildProxyInstancePath(domainId,streamId,node,
                proxyInstanceId);
        LOG.info("writeProxyInstanceToServiceManagerDB: domainId-" + domainId + " ,streamId-" + streamId
                + " ,proxyInstance-" + proxyInstances);
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,proxyInstancesPath,proxyInstances);
    }

    private List<Long> getClientFlowIds(List<ClientFlow> clientFlows) {
        List<Long> clientFlowIds = new ArrayList<>();
        for (ClientFlow clientFlow : clientFlows) {
            clientFlowIds.add(clientFlow.getClientFlowId());
        }
        return clientFlowIds;
    }

    private List<DetnetServices> createServiceInstance(ServiceInstance serviceInstance) {
        List<DetnetServices> services = new ArrayList<>();
        services.add(new DetnetServicesBuilder()
                .setDetnetFlowId(serviceInstance.getDetnetFlowId())
                .setDetnetTransportId(serviceInstance.getTransportTunnelId())
                .build());
        return services;
    }

    private InstanceIdentifier<DomainService> buildDomainServicePath(String node, Integer domainId, Long streamId) {
        return InstanceIdentifier.create(DeviceDetnetServiceManager.class)
                .child(Nodes.class,new NodesKey(node))
                .child(DomainService.class,new DomainServiceKey(domainId,streamId));
    }


    private InstanceIdentifier<ServiceProxyInstances> buildProxyInstancePath(Integer domainId, Long streamId,
                                                                             String node, Long proxyInstanceId) {
        return InstanceIdentifier.create(DetnetServiceInstanceManager.class)
                .child(Services.class,new ServicesKey(domainId,streamId))
                .child(ServiceProxyInstances.class,new ServiceProxyInstancesKey(node,proxyInstanceId));
    }

    public String writeEgressProxyInstanceToDB(String node, Long proxyInstanceId, ServiceInstance serviceInstance) {
        LOG.info("writeEgressProxyInstanceToDB");
        SegmentPathKey pathKey = serviceInstance.getSegmentPathKey();
        Integer domainId = pathKey.getDomainId();
        Long streamId = pathKey.getStreamId();
        writeProxyInstanceToServiceManagerDB(node, domainId, streamId, proxyInstanceId);
        return writeProxyInstanceToDeviceManagerDB(node,domainId,streamId,proxyInstanceId,serviceInstance,null);
    }

    public String writeMappingInstanceToDB(String relayNode, Long mappingInstanceId, ServiceInstance inServiceInstance,
                                         ServiceInstance outServiceInstance) {
        LOG.info("writeMappingInstanceToDB");
        SegmentPathKey pathKey = inServiceInstance.getSegmentPathKey();
        Integer domainId = pathKey.getDomainId();
        Long streamId = pathKey.getStreamId();
        writeMappingInstanceToServiceManagerDB(relayNode,domainId,streamId,mappingInstanceId);
        return writeMappingInstanceToDeviceManagerDB(relayNode,domainId,streamId,mappingInstanceId,inServiceInstance,
                outServiceInstance);

    }

    private String writeMappingInstanceToDeviceManagerDB(String node, Integer domainId, Long streamId,
                                                       Long mappingInstanceId, ServiceInstance inServiceInstance,
                                                       ServiceInstance outServiceInstance) {
        ServiceMappingInstance mappingInstance;
        mappingInstance = new ServiceMappingInstanceBuilder()
                .setServiceMappingInstanceId(mappingInstanceId)
                .setSequenceNumberMode(SequenceNumberType.Copy)
                .setServiceOperation(PreofType.NONE)
                .setInSegment(new InSegmentBuilder()
                        .setDetnetServices(createServiceInstance(inServiceInstance))
                        .build())
                .setOutSegment(new OutSegmentBuilder()
                        .setDetnetServices(createServiceInstance(outServiceInstance))
                        .build())
                .build();

        InstanceIdentifier<ServiceMappingInstance> serviceMappingInstancePath = buildServiceMappingPath(node,domainId,
                streamId,mappingInstanceId);
        LOG.info("writeMappingInstanceToDeviceManagerDB: domainId-" + domainId + " ,streamId-" + streamId
                + " ,nodeId-" + node + " ,mappingInstance-" + mappingInstance);
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,serviceMappingInstancePath,mappingInstance);
        List<ServiceMappingInstance> mappingInstances = new ArrayList<>();
        mappingInstances.add(mappingInstance);
        WriteDetnetServiceConfigurationInput input = new WriteDetnetServiceConfigurationInputBuilder()
                .setNodeId(node)
                .setStreamId(streamId)
                .setServiceMappingInstance(mappingInstances)
                .build();
        Future<RpcResult<Void>> rpcResult = driverApiService.writeDetnetServiceConfiguration(input);
        try {
            if (!rpcResult.get().isSuccessful()) {
                LOG.info("Write detnet service mapping instance to node error! nodeId: " + node
                        + " streamId: " + streamId);
                return rpcResult.get().getErrors().iterator().next().getMessage();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Write detnet service mapping instance to node error! Exception: " + e.getMessage());
            return e.getMessage();
        }
        return "";
    }

    private InstanceIdentifier<ServiceMappingInstance> buildServiceMappingPath(String node, Integer domainId,
                                                                               Long streamId, Long mappingInstanceId) {
        return InstanceIdentifier.create(DeviceDetnetServiceManager.class)
                .child(Nodes.class,new NodesKey(node))
                .child(DomainService.class, new DomainServiceKey(domainId,streamId))
                .child(ServiceMappingInstance.class,new ServiceMappingInstanceKey(mappingInstanceId));
    }

    private void writeMappingInstanceToServiceManagerDB(String node, Integer domainId, Long streamId,
                                                        Long mappingInstanceId) {
        ServiceMappingInstances mappingInstances = new ServiceMappingInstancesBuilder()
                .setServiceMappingInstanceId(mappingInstanceId)
                .setNodeId(node)
                .build();
        InstanceIdentifier<ServiceMappingInstances> mappingInstancesPath = buildMappingInstancePath(domainId,streamId,
                node, mappingInstanceId);
        LOG.info("writeMappingInstanceToServiceManagerDB: domainId-" + domainId + " ,streamId-" + streamId
                + " ,mappingInstance-" + mappingInstances);
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,mappingInstancesPath,mappingInstances);
    }

    private InstanceIdentifier<ServiceMappingInstances> buildMappingInstancePath(Integer domainId, Long streamId,
                                                                                 String node, Long mappingInstanceId) {
        return InstanceIdentifier.create(DetnetServiceInstanceManager.class)
                .child(Services.class,new ServicesKey(domainId,streamId))
                .child(ServiceMappingInstances.class,new ServiceMappingInstancesKey(node,mappingInstanceId));
    }

    public String writeServiceInstanceToDeviceManagerDB(Integer domainId, Long streamId, String node,
                                                      DetnetFlows detnetFlows,
                                                      DetnetTransportTunnels detnetTransportTunnels) {
        String result = "";
        result += writeDetnetFlowsToDB(domainId,streamId,node,detnetFlows);
        if (detnetTransportTunnels != null) {
            result += writeTransportTunnelsToDB(domainId, streamId, node, detnetTransportTunnels);
        }
        return result;
    }

    private String writeTransportTunnelsToDB(Integer domainId, Long streamId, String node,
                                           DetnetTransportTunnels detnetTransportTunnels) {
        InstanceIdentifier<DetnetTransportTunnels> transportTunnelsPath = buidTransportTunnelsPath(domainId,streamId,
                node,detnetTransportTunnels.getKey());
        LOG.info("writeTransportTunnelsToDB: domainId-" + domainId + " ,streamId-" + streamId + " ,nodeId-"
                + node + " ,detnettransportTunnel-" + detnetTransportTunnels);
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,transportTunnelsPath,detnetTransportTunnels);

        List<DetnetTransportTunnels> tunnelList = new ArrayList<>();
        tunnelList.add(detnetTransportTunnels);
        WriteDetnetServiceConfigurationInput input = new WriteDetnetServiceConfigurationInputBuilder()
                .setNodeId(node)
                .setStreamId(streamId)
                .setDetnetTransportTunnels(tunnelList)
                .build();
        Future<RpcResult<Void>> rpcResult = driverApiService.writeDetnetServiceConfiguration(input);
        try {
            if (!rpcResult.get().isSuccessful()) {
                LOG.info("Write detnet service detnet transport-tunnel to node error! nodeId: " + node
                        + " streamId: " + streamId);
                return rpcResult.get().getErrors().iterator().next().getMessage();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Write detnet service detnet transport-tunnel to node error! Exception: " + e.getMessage());
            return e.getMessage();
        }
        return "";
    }

    private InstanceIdentifier<DetnetTransportTunnels> buidTransportTunnelsPath(Integer domainId, Long streamId,
                                                                                String node,
                                                                                DetnetTransportTunnelsKey key) {
        return InstanceIdentifier.create(DeviceDetnetServiceManager.class)
                .child(Nodes.class,new NodesKey(node))
                .child(DomainService.class,new DomainServiceKey(domainId,streamId))
                .child(DetnetTransportTunnels.class,new DetnetTransportTunnelsKey(key));
    }

    private String writeDetnetFlowsToDB(Integer domainId, Long streamId, String node, DetnetFlows detnetFlows) {
        InstanceIdentifier<DetnetFlows> detnetFlowsPath = buildDetnetFlowsPath(domainId,streamId,node,
                detnetFlows.getKey());
        LOG.info("writeDetnetFlowsToDB: domainId-" + domainId + " ,streamId-" + streamId + " ,nodeId-" + node
                + " ,detnetFlows-" + detnetFlows);
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,detnetFlowsPath,detnetFlows);
        List<DetnetFlows> detnetFlowList = new ArrayList<>();
        detnetFlowList.add(detnetFlows);
        WriteDetnetServiceConfigurationInput input = new WriteDetnetServiceConfigurationInputBuilder()
                .setNodeId(node).setStreamId(streamId).setDetnetFlows(detnetFlowList).build();
        Future<RpcResult<Void>> rpcResult = driverApiService.writeDetnetServiceConfiguration(input);
        try {
            if (!rpcResult.get().isSuccessful()) {
                LOG.info("Write detnet service detnet flow to node error! nodeId: " + node
                        + " streamId: " + streamId);
                return rpcResult.get().getErrors().iterator().next().getMessage();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Write detnet service detnet flow to node error! Exception: " + e.getMessage());
            return e.getMessage();
        }
        return "";
    }

    private InstanceIdentifier<DetnetFlows> buildDetnetFlowsPath(Integer domainId, Long streamId, String node,
                                                                 DetnetFlowsKey key) {
        return InstanceIdentifier.create(DeviceDetnetServiceManager.class)
                .child(Nodes.class,new NodesKey(node))
                .child(DomainService.class, new DomainServiceKey(domainId,streamId))
                .child(DetnetFlows.class,new DetnetFlowsKey(key));
    }

    public Services getServicesFromServiceManagerDB(Integer domainId, Long streamId) {
        InstanceIdentifier<Services> servicesPath = buildServicesPath(domainId, streamId);
        Services data = DataOperator.readData(dataBroker,servicesPath);
        LOG.info("getServicesFromServiceManagerDB:domainId-" + domainId + " ,streamId-" + data);
        return data;
    }

    private InstanceIdentifier<Services> buildServicesPath(Integer domainId, Long streamId) {
        return InstanceIdentifier.create(DetnetServiceInstanceManager.class)
                    .child(Services.class,new ServicesKey(domainId,streamId));
    }

    public String deleteDetnetServiceFromDB(Integer domainId, Long streamId, Services services) {
        LOG.debug("deleteDetnetServiceFromDB");
        StringBuffer errorMsg = new StringBuffer();
        if (services != null && services.getServiceProxyInstances() != null) {
            for (ServiceProxyInstances proxyInstance : services.getServiceProxyInstances()) {
                Future<RpcResult<Void>> rpcResult = deleteDetnetServiceFromDeviceManagerDB(proxyInstance.getNodeId(),
                        domainId,streamId);
                try {
                    if (!rpcResult.get().isSuccessful()) {
                        LOG.info("Delete detnet service proxy instance to node error! streamId: " + streamId
                                + "nodeId :" + proxyInstance.getNodeId());
                        errorMsg.append(rpcResult.get().getErrors().iterator().next().getMessage());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOG.info("Delete detnet service proxy instance to node error! Exception: " + e.getMessage());
                    errorMsg.append(e.getMessage());
                }
            }
        }
        if (services != null && services.getServiceMappingInstances() != null) {
            for (ServiceMappingInstances mappingInstance : services.getServiceMappingInstances()) {
                Future<RpcResult<Void>> rpcResult = deleteDetnetServiceFromDeviceManagerDB(mappingInstance.getNodeId(),
                        domainId,streamId);
                try {
                    if (!rpcResult.get().isSuccessful()) {
                        LOG.info("Delete detnet service mapping instance to node error! streamId: " + streamId
                                + "nodeId :" + mappingInstance.getNodeId());
                        errorMsg.append(rpcResult.get().getErrors().iterator().next().getMessage());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOG.info("Delete detnet service mapping instance to node error! Exception: " + e.getMessage());
                    errorMsg.append(e.getMessage());
                }
            }
        }
        deleteDetnetServiceFromServiceManagerDB(domainId,streamId);
        return errorMsg.toString();
    }

    private void deleteDetnetServiceFromServiceManagerDB(Integer domainId, Long streamId) {
        LOG.debug("deleteDetnetServiceFromServiceManagerDB: domainId-" + domainId + " ,streamId-" + streamId);
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,buildServicesPath(domainId,streamId),null);
    }

    private Future<RpcResult<Void>> deleteDetnetServiceFromDeviceManagerDB(String nodeId, Integer domainId,
                                                                           Long streamId) {
        InstanceIdentifier<DomainService> domainServicePath = buildDomainServicePath(nodeId,domainId,streamId);
        LOG.debug("deleteDetnetServiceFromDeviceManagerDB: domainId-" + domainId + " ,streamId-" + streamId
                + " ,nodeId-" + nodeId);
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,domainServicePath,null);
        DeleteDetnetServiceConfigurationInput input = new DeleteDetnetServiceConfigurationInputBuilder()
                .setNodeId(nodeId).setStreamId(streamId).build();
        return driverApiService.deleteDetnetServiceConfiguration(input);
    }

    public DetnetServiceResource getServiceResource() {
        InstanceIdentifier<DetnetServiceResource> serviceResourcePath = buildDetnetServiceResourcePath();
        return DataOperator.readData(dataBroker,serviceResourcePath);
    }

    private InstanceIdentifier<DetnetServiceResource> buildDetnetServiceResourcePath() {
        return InstanceIdentifier.create(DetnetServiceResource.class);
    }

    public void writeResourceIdToDB(DetnetServiceResource data) {
        DataOperator.writeData(DataOperator.OperateType.MERGE,dataBroker,buildDetnetServiceResourcePath(),data);
    }

    public DeviceDetnetServiceManager getDeviceService() {
        InstanceIdentifier<DeviceDetnetServiceManager> path = buildDeviceDetnetServiceManagerPath();
        DeviceDetnetServiceManager data = DataOperator.readData(dataBroker,path);
        LOG.info("getDeviceService:-" + data);
        return data;
    }

    private InstanceIdentifier<DeviceDetnetServiceManager> buildDeviceDetnetServiceManagerPath() {
        return InstanceIdentifier.create(DeviceDetnetServiceManager.class);
    }

    public void distroy() {
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,buildDetnetServiceResourcePath(),null);
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,buildDeviceDetnetServiceManagerPath(),null);
        DataOperator.writeData(DataOperator.OperateType.DELETE,dataBroker,buildDetnetServiceResourcePath(),null);
    }

    public void setDriverApiService(DetnetDriverApiService driverApiService) {
        this.driverApiService = driverApiService;
    }

    public Ipv4Address getIpv4Address(String nodeId) {
        DetnetNode detnetNode = getDetnetNode(nodeId);
        if (detnetNode != null && detnetNode.getIpv4Prefix() != null) {
            return new Ipv4Address(getIpAddress(detnetNode.getIpv4Prefix().getValue()));
        }
        return null;
    }

    private String getIpAddress(String ipPrefix) {
        return ipPrefix.substring(0,ipPrefix.indexOf("/"));
    }

    private InstanceIdentifier<DetnetNetworkTopology> buildDetnetTopoPath() {
        return InstanceIdentifier.create(DetnetNetworkTopology.class);
    }

    public Ipv6Address getIpv6Address(String nodeId) {
        DetnetNode detnetNode = getDetnetNode(nodeId);
        if (detnetNode != null && detnetNode.getIpv6Prefix() != null) {
            return new Ipv6Address(getIpAddress(detnetNode.getIpv6Prefix().getValue()));
        }
        return null;
    }

    private DetnetNode getDetnetNode(String nodeId) {
        DetnetNetworkTopology detnetTopology = DataOperator.readData(dataBroker,buildDetnetTopoPath());
        if (detnetTopology != null && detnetTopology.getDetnetTopology() != null
                && !detnetTopology.getDetnetTopology().isEmpty()) {
            List<DetnetNode> nodeList = detnetTopology.getDetnetTopology().get(0).getDetnetNode();
            for (DetnetNode detnetNode : nodeList) {
                if (detnetNode.getNodeId().equals(nodeId)) {
                    return detnetNode;
                }
            }
        }
        return null;
    }
}
