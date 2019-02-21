/*
 * Copyright © 2018 ZTE,Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.tsn.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.detnet.tsn.service.api.rev180910.DetnetTsnServiceApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsnServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TsnServiceProvider.class);
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private BindingAwareBroker.RpcRegistration<DetnetTsnServiceApiService> tsnApiServiceRpcRegistration;

    public TsnServiceProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("TsnServiceProvider Session Initiated");
        TsnServiceImpl tsnService = new TsnServiceImpl(dataBroker, rpcRegistry);
        tsnApiServiceRpcRegistration = rpcRegistry
                .addRpcImplementation(DetnetTsnServiceApiService.class, tsnService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("TsnServiceProvider session Closed");
        if (tsnApiServiceRpcRegistration != null) {
            tsnApiServiceRpcRegistration.close();
        }
    }
}