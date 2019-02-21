/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.gate.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet.gate.api.rev180907.DetnetGateApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GateServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<DetnetGateApiService> gateApiServiceRpcRegistration;

    public GateServiceProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("GateServiceProvider Session Initiated");
        GateServiceImpl gateService = new GateServiceImpl(dataBroker, rpcProviderRegistry);
        gateApiServiceRpcRegistration = rpcProviderRegistry
                .addRpcImplementation(DetnetGateApiService.class, gateService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("GateServiceProvider session Closed");
        if (gateApiServiceRpcRegistration != null) {
            gateApiServiceRpcRegistration.close();
        }
    }
}