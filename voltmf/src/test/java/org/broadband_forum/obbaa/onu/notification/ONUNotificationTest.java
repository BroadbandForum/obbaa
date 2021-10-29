/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * Unit tests that tests parsing of onu-state-change notification
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class ONUNotificationTest {
    Notification notification;
    private final String deviceName = "onu";
    private final String serialNum = "ABCD12345678";
    private final String regId = "ABCD1234";
    private final String chanTermRef = "CT-1";
    private final String vaniRef = "onu_25";
    private final String onuId = "25";
    private final String onuPresentAndUnexpectedState = "onu-present-and-unexpected";
    private final String onuPresentAndOnChannelTerm = "onu-present-and-on-intended-channel-termination";
    @Mock
    Device onuDevice;
    ONUNotification onuNotification;
    MessageFormatter messageFormatter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(onuDevice.getDeviceName()).thenReturn(deviceName);
    }

    @Test
    public void test_1_0_NotificationFormat() throws NetconfMessageBuilderException {
        notification = getDetectNotificationFor_1_0();
        messageFormatter = new JsonFormatter();
        onuNotification = new ONUNotification(notification, deviceName,messageFormatter);
        assertEquals(onuNotification.getSerialNo(), serialNum);
        assertEquals(onuNotification.getChannelTermRef(), chanTermRef);
        assertEquals(onuNotification.getOnuId(), onuId);
        assertEquals(onuNotification.getOnuState(), onuPresentAndUnexpectedState);
        assertEquals(onuNotification.getMappedEvent(), ONUConstants.NOT_APPLICABLE);
    }

    @Test
    public void test_2_0_NotificationFormat() throws NetconfMessageBuilderException {
        messageFormatter = new GpbFormatter();
        notification = getDetectNotificationFor_2_0();
        onuNotification = new ONUNotification(notification, deviceName,messageFormatter);
        assertEquals(onuNotification.getSerialNo(), serialNum);
        assertEquals(onuNotification.getChannelTermRef(), chanTermRef);
        assertEquals(onuNotification.getOnuId(), onuId);
        assertEquals(onuNotification.getOnuState(), onuPresentAndOnChannelTerm);
        assertEquals(onuNotification.getVAniRef(), vaniRef);
        assertEquals(onuNotification.getRegId(), regId);
        assertEquals(onuNotification.getMappedEvent(), ONUConstants.CREATE_ONU);
    }

    private Notification getDetectNotificationFor_1_0() throws NetconfMessageBuilderException {
        String notificationString = TestUtil.loadAsString("/onu-state-change-notification_1_0.txt");
        notificationString = String.format(notificationString, serialNum, onuId, chanTermRef, onuPresentAndUnexpectedState, regId, vaniRef);
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(notificationString));
        return notification;
    }

    private Notification getDetectNotificationFor_2_0() throws NetconfMessageBuilderException {
        String notificationString = TestUtil.loadAsString("/onu-state-change-notification_2_0.txt");
        notificationString = String.format(notificationString, chanTermRef, serialNum, regId, onuPresentAndOnChannelTerm, onuId, vaniRef);
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(notificationString));
        return notification;
    }
}
