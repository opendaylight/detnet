/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.e2eservice.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet.e2e.service.api.rev180907.DetnetE2eServiceApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E2eServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(E2eServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker.RpcRegistration<DetnetE2eServiceApiService> e2eService;
    private E2eServiceImpl e2eServiceImpl;

    public E2eServiceProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("E2eServiceProvider Session Initiated");
        e2eServiceImpl = new E2eServiceImpl(dataBroker,rpcRegistry);
        e2eService = rpcRegistry.addRpcImplementation(DetnetE2eServiceApiService.class,e2eServiceImpl);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("E2eServiceProvider Closed");
        if (e2eService != null) {
            e2eService.close();
        }
    }
}