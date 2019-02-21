/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.opendaylight.detnet.common.util.RpcReturnUtil;
import org.opendaylight.detnet.pce.impl.detnetpath.PathUnifyKey;
import org.opendaylight.detnet.pce.impl.detnetpath.ServiceInstance;
import org.opendaylight.detnet.pce.impl.detnetpath.SinglePath;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.CreatePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.PceApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.QueryPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathInput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathOutput;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.RemovePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.api.rev180911.remove.path.input.Egress;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.constraint.PathConstraint;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.path.data.PathInstanceKey;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcePathImpl implements PceApiService {
    private static final Logger LOG = LoggerFactory.getLogger(PcePathImpl.class);
    private PcePathDb pcePathDb = PcePathDb.getInstance();
    private static PcePathImpl instance = new PcePathImpl();
    private ConcurrentHashMap<PathInstanceKey, ServiceInstance> serviceInstances = new ConcurrentHashMap<>();

    private PcePathImpl() {
    }

    public static PcePathImpl getInstance() {
        return instance;
    }

    public void writeDbRoot() {
        pcePathDb.pathDataWriteDbRoot();
    }

    @Override
    public Future<RpcResult<QueryPathOutput>> queryPath(QueryPathInput input) {
        if (input == null || input.getDomainId() == null || input.getStreamId() == null) {
            return RpcReturnUtil.returnErr("Illegal argument!");
        }
        LOG.debug(input.toString());
        ServiceInstance serviceInstance = getServiceInstance(new PathInstanceKey(input.getDomainId(),
                input.getStreamId()));
        if (serviceInstance == null) {
            return RpcReturnUtil.returnErr("path instance does not exists!");
        }
        QueryPathOutputBuilder outputBuilder = new QueryPathOutputBuilder();
        outputBuilder.setIngressNodeId(serviceInstance.getIngressNodeId());
        outputBuilder.setEgress(serviceInstance.buildEgresses());

        return RpcReturnUtil.returnSucess(outputBuilder.build());
    }

    @Override
    public Future<RpcResult<CreatePathOutput>> createPath(CreatePathInput input) {
        if (input == null || input.getDomainId() == null || input.getStreamId() == null
                || input.getIngressNodeId() == null || input.getEgress() == null || input.getEgress().isEmpty()
                || input.getTrafficClass() == null) {
            return RpcReturnUtil.returnErr("Illegal argument!");
        }
        LOG.debug(input.toString());
        ServiceInstance serviceInstance = getServiceInstance(new PathInstanceKey(input.getDomainId(),
                input.getStreamId()));
        if (serviceInstance == null) {
            serviceInstance = new ServiceInstance(input);
            serviceInstance.calcPath(input,false);
            if (!serviceInstance.isPathEmpty()) {
                serviceInstances.put(new PathInstanceKey(serviceInstance.getDomainId(),serviceInstance.getStreamId()),
                        serviceInstance);
                serviceInstance.writeServiceInstanceToDB();
            }
        } else {
            if (!serviceInstance.getIngressNodeId().equals(input.getIngressNodeId())) {
                return RpcReturnUtil.returnErr("ingress Node associated with the same stream-id is not match!");
            }
            if (!serviceInstance.getTrafficClass().equals(input.getTrafficClass())) {
                return RpcReturnUtil.returnErr("traffic-class associated with the same stream-id is not match!");
            }
            if (!isPathConstraintMatched(serviceInstance.getPathConstraint(),input.getPathConstraint())) {
                return RpcReturnUtil.returnErr("path constraint associated with the same stream-id is not match!");
            }
            serviceInstance.calcPath(input,true);
        }
        CreatePathOutputBuilder output = new CreatePathOutputBuilder();
        output.setStreamId(input.getStreamId());
        output.setIngressNodeId(input.getIngressNodeId());
        output.setEgress(serviceInstance.buildEgresses());

        return RpcReturnUtil.returnSucess(output.build());
    }

    private boolean isPathConstraintMatched(PathConstraint pathConstraint, PathConstraint inputPathConstraint) {
        if (pathConstraint != null && inputPathConstraint != null
                && pathConstraint.getBandwidth().equals(inputPathConstraint.getBandwidth())
                && pathConstraint.getMaxDelay().equals(inputPathConstraint.getMaxDelay())) {
            return true;
        } else if (inputPathConstraint == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Future<RpcResult<RemovePathOutput>> removePath(RemovePathInput input) {
        RemovePathOutputBuilder output = new RemovePathOutputBuilder();
        if (input.getDomainId() != null && input.getSrtreamId() != null && input.getIngressNodeId() != null) {
            LOG.debug(input.toString());
            ServiceInstance serviceInstance = getServiceInstance(new PathInstanceKey(input.getDomainId(),
                    input.getSrtreamId()));
            output.setIngressNodeId(input.getIngressNodeId());
            if (serviceInstance == null) {
                return RpcReturnUtil.returnSucess(output.build());
            }
            if (!serviceInstance.getIngressNodeId().equals(input.getIngressNodeId())) {
                return RpcReturnUtil.returnErr("ingress Node associated with the same stream-id is not match!");
            }

            if (input.getEgress() == null || input.getEgress().isEmpty()) {
                serviceInstance.removeAllPath();
                serviceInstance.removeServiceInstanceDB();
                serviceInstances.remove(new PathInstanceKey(input.getDomainId(),input.getSrtreamId()));
            } else {
                for (Egress egress : input.getEgress()) {
                    SinglePath singlePath = serviceInstance.getPath(new PathUnifyKey(input.getSrtreamId(),
                            input.getDomainId(), input.getIngressNodeId(),egress.getEgressNodeId()));
                    if (singlePath != null) {
                        singlePath.destroy();
                        serviceInstance.removePath(singlePath);
                    }
                }
                if (serviceInstance.isPathEmpty()) {
                    serviceInstance.removeServiceInstanceDB();
                    serviceInstances.remove(new PathInstanceKey(input.getDomainId(),input.getSrtreamId()));
                } else {
                    output.setEgress(serviceInstance.buildEgresses());
                }
            }
        }
        return RpcReturnUtil.returnSucess(output.build());
    }

    public ServiceInstance getServiceInstance(PathInstanceKey pathInstanceKey) {
        return serviceInstances.get(pathInstanceKey);
    }


    public void destroy() {
        for (ServiceInstance serviceInstance : serviceInstances.values()) {
            serviceInstance.destroy();
        }
        serviceInstances.clear();
    }
}




