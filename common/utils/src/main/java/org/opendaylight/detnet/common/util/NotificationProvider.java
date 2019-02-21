/*
 * Copyright (c) 2018 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.detnet.common.util;

import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ReportMessage;
import org.opendaylight.yang.gen.v1.urn.detnet.common.rev180904.ReportMessageBuilder;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NotificationProvider {
    private static NotificationProvider instance = new NotificationProvider();
    private NotificationPublishService notificationService;
    private static final Logger LOG = LoggerFactory.getLogger(NotificationProvider.class);

    private NotificationProvider() {

    }

    public void setNotificationService(NotificationPublishService notificationService) {
        this.notificationService = notificationService;
    }

    public static NotificationProvider getInstance() {
        return instance;
    }

    public <T extends Notification> void notify(T notification) {
        if (null != notificationService) {
            LOG.info("notification publish");
            notificationService.offerNotification(notification);
        }
    }

    public static ReportMessage reportMessage(String failureReason) {
        return new ReportMessageBuilder()
                .setFailureReason(failureReason)
                .build();
    }
}
