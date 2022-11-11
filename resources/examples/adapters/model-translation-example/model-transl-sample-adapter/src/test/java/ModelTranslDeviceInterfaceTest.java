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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.model.tls.ModelTranslDeviceInterface;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ModelTranslDeviceInterfaceTest {

    public static final String INT_NAME_EXCEEDS_10_CHARACTERS = "The interface name should not exceed 10 characters";
    public static final String THE_MAXIMUM_INSTANCES_WHICH_CAN_BE_CREATED_FOR_INTERFACES_IS_3 = "The maximum instances which can be created for interfaces is 3";
    private ModelTranslDeviceInterface m_deviceInterface;
    @Mock
    private NetconfConnectionManager m_ncm;
    private Device m_device;
    private EditConfigRequest m_editRequest;
    private EditConfigElement m_configElement;
    private List<Element> m_rootElements;
    private NetConfResponse m_pmaDsGetConfigRes;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_pmaDsGetConfigRes = new NetConfResponse().setMessageId("test");
        m_device = new Device();
        m_device.setDeviceName("UT-device");
        m_deviceInterface = new ModelTranslDeviceInterface(m_ncm);
        when(m_ncm.isConnected(m_device)).thenReturn(true);
        m_editRequest = new EditConfigRequest();
        m_configElement = spy(new EditConfigElement());
        m_rootElements = new ArrayList<>();
    }

    @Test
    public void testGetWhenDeviceConnected() throws ExecutionException {
        GetRequest request = new GetRequest();
        m_deviceInterface.get(m_device, request);
        verify(m_ncm).executeNetconf(m_device, request);
    }

    @Test
    public void testGetWhenDeviceNotConnected() throws ExecutionException {
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        GetRequest request = new GetRequest();
        try {
            m_deviceInterface.get(m_device, request);
        } catch (IllegalStateException e) {
            assertEquals("Device UT-device is not connected", e.getMessage());
        }
    }

    @Test
    public void testGetConfigWhenDeviceConnected() throws ExecutionException {
        GetConfigRequest request = new GetConfigRequest();
        m_deviceInterface.getConfig(m_device, request);
        verify(m_ncm).executeNetconf(m_device, request);
    }

    @Test
    public void testGetConfigWhenDeviceNotConnected() throws ExecutionException {
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        GetConfigRequest request = new GetConfigRequest();
        try {
            m_deviceInterface.getConfig(m_device, request);
        } catch (IllegalStateException e) {
            assertEquals("Device UT-device is not connected", e.getMessage());
        }
    }

    @Test
    public void testVeto() throws NetconfMessageBuilderException, SubSystemValidationException {
        Element element = transformToElement("<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "\t<interface>\n" +
                "\t\t<name>TestInt</name>\n" +
                "\t\t<type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</type>\n" +
                "\t\t<enabled>true</enabled>\n" +
                "\t</interface>\n" +
                "</interfaces>\n");
        m_rootElements.add(element);
        m_configElement.setConfigElementContents(m_rootElements);
        m_editRequest.setConfigElement(m_configElement);
        //verify veto passes without any exception
        m_deviceInterface.veto(m_device, m_editRequest, null, DocumentUtils.stringToDocument(FileUtil.loadAsString("/veto-less-than-3-ints.xml")));

        //verify veto fails when more than 3 instances exist in the datastore
        try {
            m_deviceInterface.veto(m_device, m_editRequest,null, DocumentUtils.stringToDocument(FileUtil.loadAsString("/veto-more-than-3-ints.xml")));
        } catch (SubSystemValidationException e) {
            assertEquals(THE_MAXIMUM_INSTANCES_WHICH_CAN_BE_CREATED_FOR_INTERFACES_IS_3, e.getMessage());
        }

        //verify veto fails when the length of interface exceeds 10 characters
        element = transformToElement("<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "\t<interface>\n" +
                "\t\t<name>TestInterface</name>\n" +
                "\t\t<type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</type>\n" +
                "\t\t<enabled>true</enabled>\n" +
                "\t</interface>\n" +
                "</interfaces>\n");
        m_rootElements.add(element);
        m_configElement.setConfigElementContents(m_rootElements);
        m_editRequest.setConfigElement(m_configElement);
        try {
            m_deviceInterface.veto(m_device, m_editRequest,null, DocumentUtils.stringToDocument(FileUtil.loadAsString("/veto-less-than-3-ints.xml")));
        } catch (SubSystemValidationException e) {
            assertEquals(INT_NAME_EXCEEDS_10_CHARACTERS, e.getMessage());
        }

        //NetconfMessageBuilder Exception during veto
        doThrow(new NetconfMessageBuilderException("Simply throw exception")).when(m_configElement).getXmlElement();
        try {
            m_deviceInterface.veto(m_device, m_editRequest, null, DocumentUtils.stringToDocument(FileUtil.loadAsString("/veto-less-than-3-ints.xml")));
        } catch (SubSystemValidationException e) {
            assertEquals("Error occurred during veto", e.getMessage());
        }
    }

    @Test
    public void testGetConnectionState() {
        m_deviceInterface.getConnectionState(m_device);
        verify(m_ncm).getConnectionState(m_device);
    }

    @Test
    public void testAlign() throws ExecutionException, NetconfMessageBuilderException {
        //verify the same request is send when there is no description when device is connected
        ArgumentCaptor<EditConfigRequest> newEditReq = ArgumentCaptor.forClass(EditConfigRequest.class);
        Element element = transformToElement("<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "\t<interface>\n" +
                "\t\t<name>TestInt</name>\n" +
                "\t\t<type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</type>\n" +
                "\t\t<enabled>true</enabled>\n" +
                "\t</interface>\n" +
                "</interfaces>\n");
        m_rootElements.add(element);
        m_configElement.setConfigElementContents(m_rootElements);
        m_editRequest.setConfigElement(m_configElement);
        m_deviceInterface.align(m_device, m_editRequest, m_pmaDsGetConfigRes);
        verify(m_ncm).executeNetconf(eq(m_device.getDeviceName()), newEditReq.capture());
        assertEquals(FileUtil.loadAsString("/align-req-wo-description-response.xml"), newEditReq.getValue().requestToString());

        //verify description is removed from the interface in the edit-config request when device is connected
        ArgumentCaptor<EditConfigRequest> newEditReq2 = ArgumentCaptor.forClass(EditConfigRequest.class);
        element = transformToElement("<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "\t<interface>\n" +
                "\t\t<name>TestInt</name>\n" +
                "\t\t<type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</type>\n" +
                "\t\t<description>Just Testing</description>\n" +
                "\t\t<enabled>true</enabled>\n" +
                "\t</interface>\n" +
                "</interfaces>\n");
        m_rootElements.clear();
        m_rootElements.add(element);
        m_configElement.setConfigElementContents(m_rootElements);
        m_editRequest.setConfigElement(m_configElement);
        m_deviceInterface.align(m_device, m_editRequest, m_pmaDsGetConfigRes);
        verify(m_ncm, times(2)).executeNetconf(eq(m_device.getDeviceName()), newEditReq2.capture());
        assertEquals(FileUtil.loadAsString("/align-req-with-description-response.xml"), newEditReq2.getValue().requestToString());

        //verify exception when sending edit-config for alignment when device is connected
        doThrow(new NetconfMessageBuilderException("Simply throw exception")).when(m_configElement).getXmlElement();
        try {
            m_deviceInterface.align(m_device, m_editRequest, m_pmaDsGetConfigRes);
        } catch (RuntimeException e) {
            assertEquals("Error while aligning device", e.getMessage());
        }

        //verify exception is thrown when device is not connected
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        try {
            m_deviceInterface.align(m_device, m_editRequest, m_pmaDsGetConfigRes);
        } catch (RuntimeException e) {
            assertEquals("Device UT-device is not connected", e.getMessage());
        }
    }

    @Test
    public void testForceAlign() throws NetconfMessageBuilderException, ExecutionException {
        //verify cc sent does not contain description when the getConfig response does not contain description
        ArgumentCaptor<CopyConfigRequest> ccRequest = ArgumentCaptor.forClass(CopyConfigRequest.class);
        NetConfResponse getConfigResponse = new NetConfResponse();
        getConfigResponse.setData(DocumentUtils.stringToDocument(FileUtil.loadAsString("/getresponse-without-decription.xml")).getDocumentElement());
        m_deviceInterface.forceAlign(m_device, getConfigResponse);
        verify(m_ncm).executeNetconf(eq(m_device.getDeviceName()), ccRequest.capture());
        assertEquals(FileUtil.loadAsString("/cc-without-desc-in-get-response.xml"), ccRequest.getValue().requestToString());

        //verify cc sent does not contain description when the getConfig response contains description
        ArgumentCaptor<CopyConfigRequest> ccRequest2 = ArgumentCaptor.forClass(CopyConfigRequest.class);
        getConfigResponse.setData(DocumentUtils.stringToDocument(FileUtil.loadAsString("/getresponse-with-description.xml")).getDocumentElement());
        m_deviceInterface.forceAlign(m_device, getConfigResponse);
        verify(m_ncm, times(2)).executeNetconf(eq(m_device.getDeviceName()), ccRequest2.capture());
        assertTrue(!ccRequest2.getValue().requestToString().contains("description"));

        //verify exception is thrown when device is not connected
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        try {
            m_deviceInterface.forceAlign(m_device, getConfigResponse);
        } catch (RuntimeException e) {
            assertEquals("Device UT-device is not connected", e.getMessage());
        }
    }

    @Test
    public void testNormalizeNotification() throws NetconfMessageBuilderException {
        String notifStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "\t<eventTime>2019-07-08T10:19:49+00:00</eventTime>\n" +
                "\t<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">\n" +
                "\t\t<alarms:resource xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='int1']</alarms:resource>\n" +
                "\t\t<alarms:alarm-type-id xmlns:samp-al=\"urn:broadband-forum-org:yang:sample-types\">samp-al:alarm-type1</alarms:alarm-type-id>\n" +
                "\t\t<alarms:alarm-type-qualifier/>\n" +
                "\t\t<alarms:time>2019-07-08T09:22:11.230Z</alarms:time>\n" +
                "\t\t<alarms:perceived-severity>major</alarms:perceived-severity>\n" +
                "\t\t<alarms:alarm-text>raisealarm</alarms:alarm-text>\n" +
                "\t</alarms:alarm-notification>\n" +
                "</notification>\n";
        String expectedAlarm = "<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\"\n" +
                "                           xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "   <alarms:resource xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='int1']</alarms:resource>\n" +
                "   <alarms:alarm-type-id xmlns:samp-al=\"urn:broadband-forum-org:yang:sample-types\">samp-al:alarm-type1</alarms:alarm-type-id>\n" +
                "   <alarms:alarm-type-qualifier/>\n" +
                "   <alarms:time>2019-07-08T09:22:11.230Z</alarms:time>\n" +
                "   <alarms:perceived-severity>warning</alarms:perceived-severity>\n" +
                "   <alarms:alarm-text>raiseAlarm :: Normalized Vendor alarm</alarms:alarm-text>\n" +
                "</alarms:alarm-notification>";
        NetconfNotification notification = new NetconfNotification(DocumentUtils.stringToDocument(notifStr));
        assertEquals(expectedAlarm, DocumentUtils.documentToPrettyString(m_deviceInterface.normalizeNotification(notification).getNotificationElement()).trim());
        assertNotNull(notification.getEventTime());
    }

    @Test
    public void testNotificationTypeNotAlarmNotification() throws NetconfMessageBuilderException {
        String notifStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "\t<eventTime>2019-07-08T10:19:49+00:00</eventTime>\n" +
                "\t<alarms:new-sample-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">\n" +
                "\t</alarms:new-sample-notification>\n" +
                "</notification>\n";
        NetconfNotification notification = new NetconfNotification(DocumentUtils.stringToDocument(notifStr));
        assertNull(m_deviceInterface.normalizeNotification(notification));
    }

    private static Element transformToElement(String xmldata) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xmldata.getBytes(StandardCharsets.UTF_8));
            Document doc = dBuilder.parse(stream);
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
