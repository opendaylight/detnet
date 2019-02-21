/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.driver.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverServiceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DriverServiceProvider.class);

    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<DetnetDriverApiService> detnetDriverApiServiceRpcRegistration;

    public DriverServiceProvider(final RpcProviderRegistry rpcRegistry) {
        this.rpcProviderRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("Detnet driver service Session Initiated");
        DriverServiceImpl driverServiceImpl = new DriverServiceImpl();
        detnetDriverApiServiceRpcRegistration = rpcProviderRegistry
                .addRpcImplementation(DetnetDriverApiService.class, driverServiceImpl);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("Detnet driver service session Closed");
        if (detnetDriverApiServiceRpcRegistration != null) {
            detnetDriverApiServiceRpcRegistration.close();
        }
    }
}
