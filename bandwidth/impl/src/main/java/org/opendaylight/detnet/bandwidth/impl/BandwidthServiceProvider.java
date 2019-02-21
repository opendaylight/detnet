/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.bandwidth.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet.bandwidth.api.rev180907.DetnetBandwidthApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BandwidthServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BandwidthServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker.RpcRegistration<DetnetBandwidthApiService> bandwidthApiServiceRpcRegistration;


    public BandwidthServiceProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("BandwidthServiceProvider Session Initiated");
        BandwidthServiceImpl bandwidthService = new BandwidthServiceImpl(dataBroker, rpcRegistry);
        bandwidthApiServiceRpcRegistration = rpcRegistry
                .addRpcImplementation(DetnetBandwidthApiService.class, bandwidthService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("BandwidthServiceProvider Closed");
        if (bandwidthApiServiceRpcRegistration != null) {
            bandwidthApiServiceRpcRegistration.close();
        }
    }
}