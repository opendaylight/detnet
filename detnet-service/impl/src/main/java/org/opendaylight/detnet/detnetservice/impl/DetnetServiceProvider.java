/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.detnetservice.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet.driver.api.rev181221.DetnetDriverApiService;
import org.opendaylight.yang.gen.v1.urn.detnet.service.api.rev180904.DetnetServiceApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetnetServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DetnetServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final DetnetDriverApiService driverService;
    private BindingAwareBroker.RpcRegistration<DetnetServiceApiService> detnetService;
    private DetnetServiceImpl detnetServiceImpl = DetnetServiceImpl.getInstance();

    public DetnetServiceProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
        this.driverService = rpcRegistry.getRpcService(DetnetDriverApiService.class);
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("DetnetServiceProvider Session Initiated");
        DetnetServiceDb.getInstance().setDataBroker(dataBroker);
        detnetService = rpcRegistry.addRpcImplementation(DetnetServiceApiService.class,detnetServiceImpl);
        DetnetServiceDb.getInstance().setDriverApiService(driverService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("DetnetServiceProvider Closed");
        if (detnetService != null) {
            detnetService.close();
        }
    }
}