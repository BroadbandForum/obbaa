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

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Unit tests that tests parsing of onu-state-change notification
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class ONUNotificationTest {
    Notification notification;
    String deviceName = "onu";
    @Mock
    Device onuDevice;
    ONUNotification onuNotification;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(onuDevice.getDeviceName()).thenReturn(deviceName);
    }

    @Test
    public void testNotificationFormat() throws NetconfMessageBuilderException {
        String serialNum = "ABCD12345678";
        String regId = "ABCD1234";
        String chanTermRef = "CT-1";
        String vaniRef = "onu_25";
        String onuId = "25";
        String onuState = "onu-present-and-on-intended-channel-termination";
        notification = getDetectNotification(serialNum, regId, chanTermRef, vaniRef, onuId, onuState);
        onuNotification = new ONUNotification(notification, deviceName);
        assertEquals(onuNotification.getSerialNo(), serialNum);
        assertEquals(onuNotification.getChannelTermRef(), chanTermRef);
        assertEquals(onuNotification.getOnuId(), onuId);
        assertEquals(onuNotification.getOnuState(), onuState);
        assertEquals(onuNotification.getRegId(), regId);
        assertEquals(onuNotification.getVAniRef(), vaniRef);
        assertEquals(onuNotification.getMappedEvent(), ONUConstants.DETECT_EVENT);
    }

    private Notification getDetectNotification(String serialNum, String regId, String chanTermref, String vaniRef,
                                               String onuId, String onuState) throws NetconfMessageBuilderException {
        String onuNotification = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "    <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">                               \n" +
                "        <bbf-xpon-onu-states:detected-serial-number>" + serialNum + "</bbf-xpon-onu-states:detected-serial-number>\n" +
                "        <bbf-xpon-onu-states:onu-id>" + onuId + "</bbf-xpon-onu-states:onu-id>\n" +
                "        <bbf-xpon-onu-states:channel-termination-ref>" + chanTermref + "</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "        <bbf-xpon-onu-states:onu-state>" + onuState + "</bbf-xpon-onu-states:onu-state>\n" +
                "        <bbf-xpon-onu-states:detected-registration-id>" + regId + "</bbf-xpon-onu-states:detected-registration-id>\n" +
                "        <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:53:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "        <bbf-xpon-onu-states:v-ani-ref>" + vaniRef + "</bbf-xpon-onu-states:v-ani-ref>\n" +
                "    </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>";
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(onuNotification));
        return notification;
    }
}
