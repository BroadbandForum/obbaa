/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.aggregator.processor;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.NotificationProcessor;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NotificationForwarder implements NotificationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationForwarder.class);
    NotificationService m_notificationService;
    Aggregator m_aggregator;

    public NotificationForwarder(Aggregator aggregator, NotificationService notificationService) {
        m_notificationService = notificationService;
        m_aggregator = aggregator;
    }

    public void init() {
        try {
            m_aggregator.addProcessor(this);
        } catch (DispatchException ex) {
            LOGGER.error("NotificationForwarder init failed");
        }
    }

    public void destroy() {
        try {
            m_aggregator.removeProcessor(this);
        } catch (DispatchException ex) {
            LOGGER.error("NotificationForwarder destroy failed");
        }
    }

    @Override
    public void publishNotification(Notification notification) throws DispatchException {
        m_notificationService.sendNotification("CONFIG_CHANGE", notification);
    }
}
