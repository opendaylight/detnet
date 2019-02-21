/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.detnetservice.impl;

import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.links.PathLink;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.CreateDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DeleteDetnetServiceInput;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DetnetServiceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.DetnetPath;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.RelayNode;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.detnet.path.Path;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.create.detnet.service.input.detnet.path.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.client.flows.at.uni.ClientFlow;
import org.opendaylight.yang.gen.v1.urn.detnet.service.instance.rev180904.detnet.service.instance.manager.Services;
import org.opendaylight.yang.gen.v1.urn.detnet.service.resource.rev181204.DetnetServiceResource;
import org.opendaylight.yang.gen.v1.urn.detnet.service.resource.rev181204.DetnetServiceResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.service.resource.rev181204.ResourceType;
import org.opendaylight.yangtools.yang.common.RpcResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DetnetServiceImpl implements DetnetServiceApiService {
    private static final Logger LOG = LoggerFactory.getLogger(DetnetServiceImpl.class);
    private ConcurrentHashMap<SegmentPathKey, ServiceInstance> serviceInstances = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Set<SegmentPathKey>> relayNodeSegmentMap = new ConcurrentHashMap<>();
    private static DetnetServiceImpl instance = new DetnetServiceImpl();

    private DetnetServiceImpl() {
    }

    public static DetnetServiceImpl getInstance() {
        return instance;
    }

    @Override
    public Future<RpcResult<Void>> deleteDetnetService(DeleteDetnetServiceInput input) {
        if (input == null || input.getDomainId() == null || input.getStreamId() == null) {
            return RpcReturnUtil.returnErr("input information error!");
        }
        LOG.info("DeleteDetnetServiceInput:" + input);
        Services services = getServicesFromDB(input.getDomainId(),input.getStreamId());
        String result = DetnetServiceDb.getInstance()
                .deleteDetnetServiceFromDB(input.getDomainId(),input.getStreamId(), services);
        if (!result.equals("")) {
            LOG.info("DeleteDetnetService failed! resultMsg: " + result);
            return RpcReturnUtil.returnErr(result);
        }
        return RpcReturnUtil.returnSucess(null);
    }

    private Services getServicesFromDB(Integer domainId, Long streamId) {
        return DetnetServiceDb.getInstance().getServicesFromServiceManagerDB(domainId,streamId);
    }

    @Override
    public Future<RpcResult<Void>> createDetnetService(CreateDetnetServiceInput input) {
        if (input == null || input.getDomainId() == null || input.getStreamId() == null
                || input.getDetnetPath() == null || input.getDetnetPath().isEmpty()) {
            return RpcReturnUtil.returnErr("input information error!");
        }
        LOG.info("CreateDetnetServiceInput:" + input);
        StringBuffer resultMsg = new StringBuffer();
        for (DetnetPath path : input.getDetnetPath()) {
            resultMsg.append(generateSegmentsAndServiceInstances(input.getDomainId(),input.getStreamId(),
                    input.getClientFlow(), path.getIngressNode(), path.getEgressNode(),path.getPath(),
                    input.getRelayNode()));
            serviceInstances.clear();
            relayNodeSegmentMap.clear();
        }
        if (!resultMsg.toString().equals("")) {
            LOG.info("CreateDetnetService failed! resultMsg: " + resultMsg);
            return RpcReturnUtil.returnErr(resultMsg.toString());
        }
        return RpcReturnUtil.returnSucess(null);
    }

    private String generateSegmentsAndServiceInstances(Integer domainId, Long streamId, List<ClientFlow> clientFlow,
                                                     String sourceNode, String destNode, Path path,
                                                     List<RelayNode> relayNodes) {

        List<PathLink> links = new ArrayList<>();
        Path segmentPath;
        String segIngressNode;
        String segEgressNode;
        Integer linkNum;
        StringBuffer resultMsg = new StringBuffer();
        for (PathLink link : path.getPathLink()) {
            links.add(link);
            RelayNode relayNode = getRelayNodeUseLinkDest(link,relayNodes);
            if (relayNode != null) {
                List<PathLink> tempLinks = new ArrayList<>(links);
                segmentPath = new PathBuilder().setPathLink(tempLinks).build();
                linkNum = segmentPath.getPathLink().size();
                segIngressNode = segmentPath.getPathLink().get(0).getLinkSource().getSourceNode();
                segEgressNode = segmentPath.getPathLink().get(linkNum - 1).getLinkDest().getDestNode();
                resultMsg.append(processRelayNodeInstance(domainId,streamId,sourceNode,segIngressNode,segEgressNode,
                        segmentPath,clientFlow,relayNode));
                links.clear();
                //links.add(link);
            }
        }
        List<PathLink> tempPath = new ArrayList<>(links);
        segmentPath = new PathBuilder().setPathLink(tempPath).build();
        linkNum = segmentPath.getPathLink().size();
        segIngressNode = segmentPath.getPathLink().get(0).getLinkSource().getSourceNode();
        segEgressNode = segmentPath.getPathLink().get(linkNum - 1).getLinkDest().getDestNode();

        SegmentPathKey positivePathKey = new SegmentPathKey(domainId,streamId,segIngressNode,segEgressNode);
        ServiceInstance positiveInstance = buildServiceInstance(positivePathKey,segmentPath,false);
        SegmentPathKey reversePathKey = new SegmentPathKey(domainId,streamId,segEgressNode,segIngressNode);
        ServiceInstance reverseInstance = buildServiceInstance(reversePathKey,null,true);
        RelayNode node = isRelayNode(segIngressNode,relayNodes);
        if (node != null) {
            positiveInstance.setEncapsulationType(node.getOutEncapsulation());
            reverseInstance.setEncapsulationType(node.getOutEncapsulation());
        }
        if (segIngressNode.equals(sourceNode)) {
            resultMsg.append(createIngressProxyInstance(segIngressNode,positiveInstance,clientFlow));
        }
        if (segEgressNode.equals(destNode)) {
            resultMsg.append(getRelayAndCreateInstance(segIngressNode,positivePathKey));
            resultMsg.append(createEgressProxyInstance(segEgressNode,reverseInstance));
        }
        return resultMsg.toString();
    }

    private RelayNode isRelayNode(String node, List<RelayNode> relayNodes) {
        if (relayNodes == null || relayNodes.isEmpty()) {
            return null;
        } else {
            for (RelayNode relayNode : relayNodes) {
                if (relayNode.getRelayNodeId().equals(node)) {
                    return relayNode;
                }
            }
        }
        return null;
    }



    private ServiceInstance buildServiceInstance(SegmentPathKey segmentPathKey, Path segmentPath,
                                                 boolean isInDirection) {

        ServiceInstance serviceInstance = getServiceInstance(segmentPathKey);
        if (serviceInstance == null) {
            Long instanceId = generateId(ResourceType.ServiceInstanceId);
            serviceInstance = new ServiceInstance(segmentPathKey,instanceId,segmentPath);
            serviceInstances.put(segmentPathKey,serviceInstance);
        }
        if (isInDirection) {
            serviceInstance.setDirectionType(ServiceInstance.SegmentDirectionType.In);
        } else {
            serviceInstance.setDirectionType(ServiceInstance.SegmentDirectionType.Out);
        }
        return serviceInstance;
    }

    public Long generateId(ResourceType type) {
        Long id = null;
        DetnetServiceResource serviceResource = DetnetServiceDb.getInstance().getServiceResource();
        DetnetServiceResourceBuilder builder = new DetnetServiceResourceBuilder();
        switch (type) {
            case ProxyInstanceId:
                if (serviceResource != null) {
                    id = serviceResource.getProxyInstanceId();
                }
                id = (id == null ? 1L : id + 1);
                builder.setProxyInstanceId(id);
                break;
            case MappingInstanceId:
                if (serviceResource != null) {
                    id = serviceResource.getMappingInstanceId();
                }
                id = (id == null ? 1L : id + 1);
                builder.setMappingInstanceId(id);
                break;
            case ServiceInstanceId:
                if (serviceResource != null) {
                    id = serviceResource.getServiceInstanceId();
                }
                id = (id == null ? 1L : id + 1);
                builder.setServiceInstanceId(id);
                break;
            case DetnetFlowId:
                if (serviceResource != null) {
                    id = serviceResource.getDetnetFlowId();
                }
                id = (id == null ? 1L : id + 1);
                builder.setDetnetFlowId(id);
                break;
            case TransportTunnelId:
                if (serviceResource != null) {
                    id = serviceResource.getTransportTunnelId();
                }
                id = (id == null ? 1L : id + 1);
                builder.setTransportTunnelId(id);
                break;
            default:
                break;
        }
        DetnetServiceDb.getInstance().writeResourceIdToDB(builder.build());
        return id;
    }

    private ServiceInstance getServiceInstance(SegmentPathKey segmentPathKey) {
        return serviceInstances.get(segmentPathKey);
    }

    private ServiceInstance getServiceInstance(Set<SegmentPathKey> segPathSet,
                                               ServiceInstance.SegmentDirectionType directionType) {
        ServiceInstance serviceInstance ;
        for (SegmentPathKey pathKey : segPathSet) {
            serviceInstance = getServiceInstance(pathKey);
            if (serviceInstance.getDirectionType().equals(directionType)) {
                return serviceInstance;
            }
        }
        return null;
    }


    private RelayNode getRelayNodeUseLinkDest(PathLink link, List<RelayNode> relayNodes) {
        return isRelayNode(link.getLinkDest().getDestNode(),relayNodes);
    }

    private String processRelayNodeInstance(Integer domainId, Long streamId, String sourceNode, String ingressNode,
                                          String egressNode, Path segmentPath, List<ClientFlow> clientFlow,
                                          RelayNode relayNode) {
        LOG.debug("processRelayNodeInstance: domain-" + domainId + "streamId-" + streamId + "sourceNode-" + sourceNode
                + "seg-ingress-" + ingressNode + "seg-egress-" + egressNode + "relayNode-" + relayNode);
        String result = "";
        SegmentPathKey positivePathKey = new SegmentPathKey(domainId,streamId,ingressNode,egressNode);
        ServiceInstance positiveInstance = buildServiceInstance(positivePathKey,segmentPath,false);
        positiveInstance.setEncapsulationType(relayNode.getInEncapsulation());
        SegmentPathKey reversePathKey = new SegmentPathKey(domainId,streamId,egressNode,ingressNode);
        ServiceInstance reverseInstance = buildServiceInstance(reversePathKey,null,true);
        reverseInstance.setEncapsulationType(relayNode.getInEncapsulation());

        if (ingressNode.equals(sourceNode)) {
            result += createIngressProxyInstance(ingressNode,positiveInstance,clientFlow);
        }

        result += getRelayAndCreateInstance(ingressNode, positivePathKey);
        Set<SegmentPathKey> reverseSegPathSet = getRelayNodeSegmentMap(egressNode);
        reverseSegPathSet.add(reversePathKey);
        relayNodeSegmentMap.put(egressNode, reverseSegPathSet);
        return result;
    }

    private String getRelayAndCreateInstance(String ingressNode, SegmentPathKey positivePathKey) {
        Set<SegmentPathKey> positiveSegPathSet = getRelayNodeSegmentMap(ingressNode);
        String result = "";
        positiveSegPathSet.add(positivePathKey);
        result += createMappingInstance(ingressNode,positiveSegPathSet);
        relayNodeSegmentMap.remove(ingressNode);
        return result;
    }

    private String createIngressProxyInstance(String ingressNode, ServiceInstance serviceInstance,
                                            List<ClientFlow> clientFlow) {
        String result = "";
        result += serviceInstance.createServiceInstance();
        Long proxyInstanceId = generateId(ResourceType.ProxyInstanceId);
        result += DetnetServiceDb.getInstance().writeIngressProxyInstanceToDB(ingressNode,proxyInstanceId,
                serviceInstance, clientFlow);
        return result;
    }

    private String createMappingInstance(String relayNode, Set<SegmentPathKey> segPathSet) {

        ServiceInstance inServiceInstance = getServiceInstance(segPathSet, ServiceInstance.SegmentDirectionType.In);
        ServiceInstance outServiceInstance = getServiceInstance(segPathSet, ServiceInstance.SegmentDirectionType.Out);
        String result = "";
        if (inServiceInstance != null && outServiceInstance != null) {
            result += inServiceInstance.createServiceInstance();
            result += outServiceInstance.createServiceInstance();
            Long mappingInstanceId = generateId(ResourceType.MappingInstanceId);
            result += DetnetServiceDb.getInstance().writeMappingInstanceToDB(relayNode, mappingInstanceId,
                    inServiceInstance, outServiceInstance);
        }
        return result;
    }

    private String createEgressProxyInstance(String egressNode, ServiceInstance serviceInstance) {
        String result = "";
        result += serviceInstance.createServiceInstance();
        Long proxyInstanceId = generateId(ResourceType.ProxyInstanceId);
        result += DetnetServiceDb.getInstance()
                .writeEgressProxyInstanceToDB(egressNode,proxyInstanceId,serviceInstance);
        return result;
    }

    private Set<SegmentPathKey> getRelayNodeSegmentMap(String ingressNode) {
        Set<SegmentPathKey> segmentPathKeySet;
        segmentPathKeySet = relayNodeSegmentMap.get(ingressNode);
        return segmentPathKeySet == null ? new ConcurrentSet<>() : segmentPathKeySet;
    }
}
