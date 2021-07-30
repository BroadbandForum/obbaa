/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.broadband_forum.obbaa.onu.util.VoltMFTestConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class NotificationRequestTest {

    @Mock
    public NotificationRequest m_notificationRequest;

    public String m_onuDeviceName;
    private String m_oltDeviceName;
    private String m_channelTermRef;
    private String m_onuId;
    private String m_notificationEvent;
    private HashMap<String, String> m_labels;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_onuDeviceName = VoltMFTestConstants.ONU_DEVICE_NAME;
        m_oltDeviceName = VoltMFTestConstants.OLT_NAME;
        m_onuId = VoltMFTestConstants.ONU_ID_VALUE;
        m_channelTermRef = VoltMFTestConstants.CHANNEL_TERMINATION;
        m_notificationEvent = VoltMFTestConstants.REQUEST_EVENT;
        m_labels = new HashMap<>();

        m_notificationRequest = Mockito.spy(new NotificationRequest(m_onuDeviceName, m_oltDeviceName, m_channelTermRef, m_onuId,
                m_notificationEvent, m_labels));
    }

    @Test
    public void testGetJsonNotificationDefaultMsgId() {
        when(m_notificationRequest.getOnuDeviceName()).thenReturn(m_onuDeviceName);
        when(m_notificationRequest.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);
        String actualResponse = m_notificationRequest.getJsonNotification();
        verify(m_notificationRequest, times(2)).getOnuDeviceName();
        assertEquals(actualResponse, VoltMFTestConstants.EXPECTED_RESPONSE_DEFAULT_MESSAGE_ID);
    }

    @Test
    public void testGetJsonNotificationNotDefaultMsgId() {
        when(m_notificationRequest.getOnuDeviceName()).thenReturn(m_onuDeviceName);
        when(m_notificationRequest.getMessageId()).thenReturn("1");
        String actualResponse = m_notificationRequest.getJsonNotification();
        verify(m_notificationRequest, times(1)).getOnuDeviceName();
        assertEquals(actualResponse, VoltMFTestConstants.EXPECTED_RESPONSE_NOT_DEFAULT_MESSAGE_ID);
    }

    @Test
    public void testGetJsonNotificationDeviceNull() {
        when(m_notificationRequest.getOnuDeviceName()).thenReturn(null);
        when(m_notificationRequest.getEvent()).thenReturn(m_notificationEvent);
        when(m_notificationRequest.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);
        String actualResponse = m_notificationRequest.getJsonNotification();
        verify(m_notificationRequest, times(2)).getOnuDeviceName();
        assertEquals(actualResponse, VoltMFTestConstants.EXPECTED_RESPONSE_WHEN_DEVICE_IS_NULL);
    }

    @Test
    public void testGetJsonNotificationForDetectDevice() {
        when(m_notificationRequest.getOnuDeviceName()).thenReturn(m_onuDeviceName);
        when(m_notificationRequest.getEvent()).thenReturn(ONUConstants.DETECT_EVENT);
        when(m_notificationRequest.getMessageId()).thenReturn(ONUConstants.DEFAULT_MESSAGE_ID);
        String actualResponse = m_notificationRequest.getJsonNotification();
        verify(m_notificationRequest, times(2)).getOnuDeviceName();
        assertEquals(actualResponse, VoltMFTestConstants.EXPECTED_RESPONSE_FOR_DETECT_DEVICE);
    }

    @Test
    public void testGetJsonNotificationForUndetectDevice() {
        when(m_notificationRequest.getOnuDeviceName()).thenReturn(m_onuDeviceName);
        when(m_notificationRequest.getEvent()).thenReturn(ONUConstants.UNDETECT_EVENT);
        when(m_notificationRequest.getMessageId()).thenReturn(ONUConstants.DEFAULT_MESSAGE_ID);
        String actualResponse = m_notificationRequest.getJsonNotification();
        verify(m_notificationRequest, times(2)).getOnuDeviceName();
        assertEquals(actualResponse, VoltMFTestConstants.EXPECTED_RESPONSE_FOR_UNDETECT_DEVICE);
    }
}