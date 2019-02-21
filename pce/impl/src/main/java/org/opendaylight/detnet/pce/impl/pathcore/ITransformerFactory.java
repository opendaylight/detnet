/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.pce.impl.pathcore;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.detnet.pce.rev180911.GraphLink;

public interface ITransformerFactory<T> {
    T create(List<GraphLink> contrainedLinks);
}
