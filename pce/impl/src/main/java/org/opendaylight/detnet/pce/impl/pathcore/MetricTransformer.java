/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.pathcore;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.detnet.pce.impl.util.ComUtility;
import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;

public class MetricTransformer implements ITransformer<GraphLink> {
    protected static final long LINK_METRIC_STEP = 0x1;
    List<GraphLink> contrainedLinkList = new ArrayList<GraphLink>();

    public MetricTransformer(List<GraphLink> contrainedLinks) {
        if (contrainedLinks != null && !contrainedLinks.isEmpty()) {
            this.contrainedLinkList.addAll(contrainedLinks);
        }
    }



    @Override
    public Double transform(GraphLink link) {
        double metric = ComUtility.getLinkMetric(link);

        for (GraphLink containedLink : contrainedLinkList) {
            if (containedLink.equals(link)) {
                metric -= metric / 2;
            }
        }
        return metric < 1 ? 1 : metric;
    }
}
