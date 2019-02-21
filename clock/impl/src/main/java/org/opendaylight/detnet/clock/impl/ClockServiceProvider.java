/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.clock.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet._1588v2.api.rev180904.Detnet1588v2ApiService;
import org.opendaylight.yang.gen.v1.urn.detnet._8021as.rev.api.rev180904.Detnet8021asRevApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClockServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ClockServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker.RpcRegistration<Detnet1588v2ApiService> clock1588v2ApiServiceRpcRegistration;
    private BindingAwareBroker.RpcRegistration<Detnet8021asRevApiService> clock8021AsRevApiServiceRpcRegistration;

    public ClockServiceProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("ClockServiceProvider Session Initiated");
        Detnet1588v2ServiceImpl detnet1588v2Service = new Detnet1588v2ServiceImpl(dataBroker);
        Detnet8021AsApiServiceImpl detnet8021AsApiService = new Detnet8021AsApiServiceImpl(dataBroker);
        clock1588v2ApiServiceRpcRegistration = rpcRegistry
                .addRpcImplementation(Detnet1588v2ApiService.class, detnet1588v2Service);
        clock8021AsRevApiServiceRpcRegistration = rpcRegistry
                .addRpcImplementation(Detnet8021asRevApiService.class, detnet8021AsApiService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ClockServiceProvider Closed");
        if (clock1588v2ApiServiceRpcRegistration != null) {
            clock1588v2ApiServiceRpcRegistration.close();
        }
        if (clock8021AsRevApiServiceRpcRegistration != null) {
            clock8021AsRevApiServiceRpcRegistration.close();
        }
    }
}