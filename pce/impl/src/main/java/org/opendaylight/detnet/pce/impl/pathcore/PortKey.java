/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.pathcore;

import org.opendaylight.yang.gen.v1.urn.detnet.topology.rev180823.detnet.network.topology.detnet.topology.DetnetLink;

public class PortKey {
    private String node;
    private String tp;

    public PortKey(String node, String tp) {
        this.node = node;
        this.tp = tp;
    }

    @Override
    public String toString() {
        return "node:" + node + "tp:" + tp;
    }

    public PortKey(DetnetLink link) {
        this.node = link.getLinkSource().getSourceNode();
        this.tp = link.getLinkSource().getSourceTp();
    }

    public String getNode() {
        return node;
    }

    public String getTp() {
        return tp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((tp == null) ? 0 : tp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PortKey other = (PortKey) obj;
        if (node == null) {
            if (other.node != null) {
                return false;
            }
        } else if (!node.equals(other.node)) {
            return false;
        }
        if (tp == null) {
            if (other.tp != null) {
                return false;
            }
        } else if (!tp.equals(other.tp)) {
            return false;
        }
        return true;
    }
}
