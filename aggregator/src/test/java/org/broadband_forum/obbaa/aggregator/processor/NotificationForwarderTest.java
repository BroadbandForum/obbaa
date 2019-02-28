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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NotificationForwarderTest {

    private static final String NOTIFICATION = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
            "<eventTime>2019-01-24T09:46:40+00:00</eventTime>\n" +
            "<netconf-config-change xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-notifications\">\n" +
            "<datastore>running</datastore>\n" +
            "<changed-by>\n" +
            "<username>PMA_USER</username>\n" +
            "<session-id>1</session-id>\n" +
            "<source-host/>\n" +
            "</changed-by>\n" +
            "<edit>\n" +
            "<target xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='interfaceB']</target>\n" +
            "<operation>delete</operation>\n" +
            "</edit>\n" +
            "</netconf-config-change>\n" +
            "</notification>";
    private NotificationForwarder m_notificatonForwarder;
    @Mock
    private Aggregator m_aggregator;
    @Mock
    private NotificationService m_notificationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_notificatonForwarder = new NotificationForwarder(m_aggregator, m_notificationService);
    }

    @Test
    public void init() throws Exception {
        m_notificatonForwarder.init();
        verify(m_aggregator).addProcessor(m_notificatonForwarder);

    }

    @Test
    public void destroy() throws Exception {
        m_notificatonForwarder.destroy();
        verify(m_aggregator).removeProcessor(m_notificatonForwarder);
    }

    @Test
    public void publishNotification() throws Exception {
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(NOTIFICATION));
        m_notificatonForwarder.publishNotification(notification);
        verify(m_notificationService).sendNotification(eq("CONFIG_CHANGE"), any(Notification.class));
    }

}