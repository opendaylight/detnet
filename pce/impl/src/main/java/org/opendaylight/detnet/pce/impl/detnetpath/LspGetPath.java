/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.detnetpath;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;


public final class LspGetPath {

    private LspGetPath(){

    }

    public static LinkedList<GraphLink> getPath(
            Map<String, List<GraphLink>> incomingLinkMap, String srcNode,
            String destNode) {

        LinkedList<GraphLink> path = new LinkedList<>();

        if (incomingLinkMap == null
                || incomingLinkMap.isEmpty()
                || incomingLinkMap.get(destNode) == null) {
            return path;
        }

        String current = destNode;

        while (!current.equals(srcNode)) {
            GraphLink incoming;

            incoming = incomingLinkMap.get(current).get(0);

            addIncomingEdge2Path(path, incoming);

            current = incoming.getSource().getSourceNode();
        }

        return path;
    }

    private static void addIncomingEdge2Path(LinkedList<GraphLink> path,
                                             GraphLink incoming) {
        path.addFirst(incoming);
    }

}
