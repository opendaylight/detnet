/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.detnetservice.impl;

public class SegmentPathKey {
    protected Integer domainId;
    protected Long streamId;
    protected String ingressNode;
    protected String egressNode;

    public SegmentPathKey(Integer domainId, Long streamId, String ingressNode, String egressNode) {
        this.domainId = domainId;
        this.streamId = streamId;
        this.ingressNode = ingressNode;
        this.egressNode = egressNode;
    }

    public SegmentPathKey(SegmentPathKey source) {
        if (null != source.streamId) {
            this.streamId = source.streamId;
        }
        if (null != source.domainId) {
            this.domainId = source.domainId;
        }
        if (null != source.ingressNode) {
            this.ingressNode = source.ingressNode;
        }
        if (null != source.egressNode) {
            this.egressNode = source.egressNode;
        }
    }


    public String getIngressNode() {
        return ingressNode;
    }

    public String getEgressNode() {
        return egressNode;
    }

    public Long getStreamId() {
        return streamId;
    }

    public Integer getDomainId() {
        return domainId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ingressNode == null) ? 0 : ingressNode.hashCode());
        result = prime * result + ((egressNode == null) ? 0 : egressNode.hashCode());
        result = prime * result + ((streamId == null) ? 0 : streamId.hashCode());
        result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());

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
        SegmentPathKey other = (SegmentPathKey) obj;
        if (ingressNode == null) {
            if (other.ingressNode != null) {
                return false;
            }
        } else if (!ingressNode.equals(other.ingressNode)) {
            return false;
        }
        if (egressNode == null) {
            if (other.egressNode != null) {
                return false;
            }
        } else if (!egressNode.equals(other.egressNode)) {
            return false;
        }

        if (streamId == null) {
            if (other.streamId != null) {
                return false;
            }
        } else if (!streamId.equals(other.streamId)) {
            return false;
        }
        if (domainId == null) {
            if (other.domainId != null) {
                return false;
            }
        } else if (!domainId.equals(other.domainId)) {
            return false;
        }
        return true;
    }

}
