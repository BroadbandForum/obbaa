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

package org.broadband_forum.obbaa.onu.util;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

public class VOLTMgmtRequestCreationUtilTest {

    private final String ONU_NAME = "onu1";
    private final String OLT_NAME = "OLT1";
    private final String CHANNEL_TERM_REF = "CT_1";
    private final String ONU_ID = "1";

    @Mock
    Device m_onuDevice;
    @Mock
    ONUNotification m_onuNotification;
    @Mock
    UnknownONU m_unknwonONU;
    String createOnuRequestToVomci = "/create-onu-request-to-vomci.xml";
    String createOnuRequestToProxy = "/create-onu-request-to-proxy.xml";
    String deleteOnuRequestTovomci = "/delete-onu-request-to-vomci.xml";
    String deleteOnuRequestToProxy = "/delete-onu-request-to-proxy.xml";
    String setOnuCommVomci = "/set-onu-comm-request-to-vomci.xml";
    String setOnuCommProxy = "/set-onu-comm-request-to-proxy.xml";
    String internalGetRequest = "/internal-get-request.xml";
    String undetectOnuJsonNotification = "/undetect-onu-notification.json";
    String detectOnuJsonNotification = "/detect-onu-notification.json";
    String onuAuthReportActionRequestFile = "/onu-authentication-report-action-request.xml";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPrepareCreateOnuRequest() throws IOException, SAXException {
        ActionRequest vomciRequest = VOLTMgmtRequestCreationUtil.prepareCreateOnuRequest(ONU_NAME, false);
        assertNotNull(vomciRequest);
        assertXMLEquals(TestUtil.loadAsXml(createOnuRequestToVomci), vomciRequest);
        ActionRequest proxyRequest = VOLTMgmtRequestCreationUtil.prepareCreateOnuRequest(ONU_NAME, true);
        assertNotNull(proxyRequest);
        assertXMLEquals(TestUtil.loadAsXml(createOnuRequestToProxy), proxyRequest);
    }

    @Test
    public void testPrepareDeleteOnuRequest() throws IOException, SAXException {
        ActionRequest deleteOnuVomci = VOLTMgmtRequestCreationUtil.prepareDeleteOnuRequest(ONU_NAME, false);
        assertNotNull(deleteOnuVomci);
        assertXMLEquals(TestUtil.loadAsXml(deleteOnuRequestTovomci), deleteOnuVomci);
        ActionRequest deleteOnuProxy = VOLTMgmtRequestCreationUtil.prepareDeleteOnuRequest(ONU_NAME, true);
        assertNotNull(deleteOnuProxy);
        assertXMLEquals(TestUtil.loadAsXml(deleteOnuRequestToProxy), deleteOnuProxy);
    }

    @Test
    public void testPrepareSetOnuCommunicationRequest() throws IOException, SAXException {
        ActionRequest setOnuCommVomciAction = VOLTMgmtRequestCreationUtil.prepareSetOnuCommunicationRequest(ONU_NAME, true, OLT_NAME, CHANNEL_TERM_REF, ONU_ID, "vOLTMF_Kafka", "proxy-grpc-1", false);
        assertNotNull(setOnuCommVomciAction);
        assertXMLEquals(TestUtil.loadAsXml(setOnuCommVomci), setOnuCommVomciAction);
        ActionRequest setOnuCommproxyAction = VOLTMgmtRequestCreationUtil.prepareSetOnuCommunicationRequest(ONU_NAME, true, OLT_NAME, CHANNEL_TERM_REF, ONU_ID, "vOMCi-grpc-1", OLT_NAME, true);
        assertNotNull(setOnuCommproxyAction);
        assertXMLEquals(TestUtil.loadAsXml(setOnuCommProxy), setOnuCommproxyAction);
    }

    @Test
    public void testOnuAuthReportActionRequest() throws NetconfMessageBuilderException, IOException, SAXException {
        String serialNumber = "ABCD12345678";
        String onuManagementMode = "baa-xpon-onu-types:use-vomci";
        String vaniName = "test";
        String channelTermination = "CT_1";
        Boolean authSuccess = true;
        ActionRequest onuAuthenicationReportActionRequest = VOLTMgmtRequestCreationUtil.prepareOnuAuthenicationReportActionRequest(ONU_NAME,
                authSuccess, vaniName, onuManagementMode, serialNumber, channelTermination);
        String onuAuthenicationReportActionRequestString = DocumentUtils.documentToPrettyString(onuAuthenicationReportActionRequest.getRequestDocument());
        assertNotNull(onuAuthenicationReportActionRequest);
        TestUtil.assertXMLStringEquals(TestUtil.loadAsString(onuAuthReportActionRequestFile), onuAuthenicationReportActionRequestString);
    }

    @Test
    public void testPrepareInternalGetRequest() throws IOException, SAXException {
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareInternalGetRequest(ONU_NAME);
        assertNotNull(getRequest);
        assertXMLEquals(TestUtil.loadAsXml(internalGetRequest), getRequest);
    }

    @Test
    public void testPrepareDetectForPreconfiguredDeviceUnknownONU() {
        String jsonNotification = null;
        when(m_unknwonONU.getOnuId()).thenReturn(ONU_ID);
        when(m_unknwonONU.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_unknwonONU.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", "bbf");
        NotificationRequest notificationRequest = VOLTMgmtRequestCreationUtil.prepareDetectForPreconfiguredDevice(ONU_NAME, m_unknwonONU, labels);
        assertNotNull(notificationRequest);
        jsonNotification = notificationRequest.getJsonNotification();
        assertEquals(prepareJsonResponseFromFile(detectOnuJsonNotification), jsonNotification);

    }

    @Test
    public void testPrepareDetectForPreconfiguredDeviceOnuNotificationTest() {
        String jsonNotification = null;
        when(m_onuNotification.getOnuId()).thenReturn(ONU_ID);
        when(m_onuNotification.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", "bbf");
        NotificationRequest notificationRequest = VOLTMgmtRequestCreationUtil.prepareDetectForPreconfiguredDevice(ONU_NAME, m_onuNotification, labels);
        assertNotNull(notificationRequest);
        jsonNotification = notificationRequest.getJsonNotification();
        assertEquals(prepareJsonResponseFromFile(detectOnuJsonNotification), jsonNotification);
    }

    @Test
    public void testPrepareUndetectKafkaMessage() {
        String jsonNotification = null;
        when(m_onuNotification.getOnuId()).thenReturn(ONU_ID);
        when(m_onuNotification.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_onuDevice.getDeviceName()).thenReturn(ONU_NAME);
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", "bbf");
        NotificationRequest notificationRequest = VOLTMgmtRequestCreationUtil.prepareUndetectKafkaMessage(m_onuDevice, m_onuNotification, labels);
        assertNotNull(notificationRequest);
        jsonNotification = notificationRequest.getJsonNotification();
        assertEquals(prepareJsonResponseFromFile(undetectOnuJsonNotification), jsonNotification);
    }

    private String prepareJsonResponseFromFile(String name) {
        return TestUtil.loadAsString(name).trim();
    }

}
