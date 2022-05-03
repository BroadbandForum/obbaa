package org.broadband_forum.obbaa.onu.notification;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.util.VOLTManagementUtil;
import org.broadband_forum.obbaa.onu.util.VOLTMgmtRequestCreationUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Element;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OnuAuthenticationReportNotification.class})
public class OnuAuthenticationReportNotificationTest {

    public static final String GET_RESPONSE = "/get-response.xml";
    private static final AtomicLong m_messageId = new AtomicLong(1000);
    private final String VANI_REF = "Onu_1";
    private final String OLT_NAME = "pOLT";
    String resultDevice = "/onu-authentication-result-for-device.xml";
    String resultNotification = "/onu-authentication-result-for-notification.xml";
    String resultDeviceForNullValues = "/onu-authentication-result-for-device-null-values.xml";

    OnuAuthenticationReportNotification m_onuAuthenticationReportNotification;
    @Mock
    Device m_device;
    @Mock
    ONUNotification m_onuNotification;
    @Mock
    DeviceMgmt m_deviceMgmt;
    @Mock
    OnuConfigInfo m_onuConfigInfo;
    @Mock
    ExpectedAttachmentPoint m_expectedAttachmentPoint;
    String resultNotificationForNullValues = "/onu-authentication-result-for-notification-null-values.xml";
    @Mock
    NetconfConnectionManager m_netconfConnectionManager;
    @Mock
    Future<NetConfResponse> m_responseFuture;
    @Mock
    NetConfResponse m_netConfResponse;
    @Mock
    DeviceState m_deviceState;
    @Mock
    OnuStateInfo m_onuStateInfo;
    @Mock
    ActualAttachmentPoint m_actualAttachmentPoint;
    @Mock
    TxService m_txService;
    @Mock
    DeviceManager m_deviceManager;

    Element m_dataElement = DocumentUtils.stringToDocument(TestUtil.loadAsString(GET_RESPONSE)).getDocumentElement();

    String m_serialNumber = "ABCD12345678";
    String m_registrationId = "REG123";
    String m_oltName = "OLT1";
    String m_channelPartition = "CT_1";
    String m_vaniName = "vomci-use";
    String m_onuId = "1";
    String m_onuType = "ONU";
    String m_onuName = "ONU1";

    public OnuAuthenticationReportNotificationTest() throws NetconfMessageBuilderException {
    }

    @Test
    public void testOnuAuthReportNotificationByDevice() throws NetconfMessageBuilderException {
        String authStatus = "vomci-expected-by-olt-but-inconsistent-pmaa-mgmt-mode";
        when(m_device.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_device.getDeviceName()).thenReturn(m_onuName);
        when(m_device.isMediatedSession()).thenReturn(true);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceMgmt.getDeviceType()).thenReturn(m_onuType);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn(m_channelPartition);
        when(m_actualAttachmentPoint.getOnuId()).thenReturn(m_onuId);
        when(m_actualAttachmentPoint.getvAniName()).thenReturn(m_vaniName);
        when(m_actualAttachmentPoint.getOltName()).thenReturn(m_oltName);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(m_serialNumber);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(m_registrationId);
        m_onuAuthenticationReportNotification = new OnuAuthenticationReportNotification(m_device, null, m_netconfConnectionManager,
                authStatus, m_deviceManager, m_txService, null);
        Element result = m_onuAuthenticationReportNotification.getOnuAuthenticationReportNotificationElement(m_device);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultDevice)));
    }

    @Test
    public void testOnuAuthReportNotificationByDeviceWhenValuesAreNull() throws NetconfMessageBuilderException {
        when(m_device.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_device.getDeviceName()).thenReturn(null);
        when(m_device.isMediatedSession()).thenReturn(true);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceMgmt.getDeviceType()).thenReturn(m_onuType);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(null);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(null);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getOltName()).thenReturn(null);
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn(null);
        when(m_actualAttachmentPoint.getOnuId()).thenReturn(null);
        when(m_actualAttachmentPoint.getvAniName()).thenReturn(null);
        m_onuAuthenticationReportNotification = new OnuAuthenticationReportNotification(null, null, m_netconfConnectionManager,
                null, m_deviceManager, m_txService, "ont1");
        Element result = m_onuAuthenticationReportNotification.getOnuAuthenticationReportNotificationElement(m_device);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultDeviceForNullValues)));
    }

    @Test
    public void testOnuAuthReportNotificationByNotification() throws NetconfMessageBuilderException, ExecutionException, InterruptedException {
        String onuAuthStatus = "unable-to-authenticate-onu";
        when(m_onuNotification.getOltDeviceName()).thenReturn(m_oltName);
        when(m_onuNotification.getChannelTermRef()).thenReturn(m_channelPartition);
        when(m_onuNotification.getOnuId()).thenReturn(m_onuId);
        when(m_onuNotification.getSerialNo()).thenReturn(m_serialNumber);
        when(m_onuNotification.getRegId()).thenReturn(m_registrationId);
        when(m_onuNotification.getVAniRef()).thenReturn(m_vaniName);
        when(m_onuNotification.getOnuState()).thenReturn("vomci-expected-by-olt-but-inconsistent-pmaa-mgmt-mode");
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_device);
        when(m_device.getDeviceName()).thenReturn(OLT_NAME);

        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(OLT_NAME, VANI_REF);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_netconfConnectionManager.executeNetconf(any(Device.class), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(String.valueOf(m_messageId));
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);

        m_onuAuthenticationReportNotification = new OnuAuthenticationReportNotification(null, null, m_netconfConnectionManager,
                onuAuthStatus, m_deviceManager , m_txService, "ont1");
        Element result = m_onuAuthenticationReportNotification.getOnuAuthenticationReportNotificationElement(m_onuNotification);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultNotification)));
    }

    @Test
    public void testOnuAuthReportNotificationByNotificationWhenValuesNull() throws NetconfMessageBuilderException, ExecutionException, InterruptedException {
        when(m_netconfConnectionManager.executeNetconf(anyString(), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn("1000");
        m_onuAuthenticationReportNotification = new OnuAuthenticationReportNotification(null, m_onuNotification, m_netconfConnectionManager,
                null, m_deviceManager, m_txService, "ont1");
        Element result = m_onuAuthenticationReportNotification.getOnuAuthenticationReportNotificationElement(m_onuNotification);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultNotificationForNullValues)));
    }
}
