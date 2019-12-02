/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.common.util;

import com.google.common.base.Optional;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeDataBroker {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDataBroker.class);
    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID = InstanceIdentifier
            .create(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));
    private static NodeDataBroker instance = new NodeDataBroker();
    private MountPointService mountPointService;

    private NodeDataBroker() {

    }

    public void setMountPointService(MountPointService mountPointService) {
        this.mountPointService = mountPointService;
    }

    public static NodeDataBroker getInstance() {
        return instance;
    }

    public DataBroker getNodeDataBroker(String nodeId) {
        if (mountPointService == null) {
            LOG.info("Init mount point service failed!");
            return null;
        }
        Optional<MountPoint> mountPointOptional = mountPointService.getMountPoint(NETCONF_TOPO_IID
                .child(Node.class, new NodeKey(new NodeId(nodeId))));
        if (mountPointOptional.isPresent()) {
            MountPoint nodeMountPoint = mountPointOptional.get();
            Optional<DataBroker> nodeDataBroker =  nodeMountPoint.getService(DataBroker.class);
            if (nodeDataBroker.isPresent()) {
                return nodeDataBroker.get();
            }
        }

        LOG.error("Get data broker for node: {} failed!", nodeId);
        return null;
    }
}
