/*
 * Copyright (c) 2018 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.common.util;

import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(UtilsProvider.class);
    private NotificationPublishService notificationService;
    private MountPointService mountPointService;

    public UtilsProvider(NotificationPublishService notificationService, MountPointService mountPointService) {
        this.notificationService = notificationService;
        this.mountPointService = mountPointService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        NodeDataBroker.getInstance().setMountPointService(mountPointService);
        NotificationProvider.getInstance().setNotificationService(notificationService);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ServiceProvider Closed");
    }
}
