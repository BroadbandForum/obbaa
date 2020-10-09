package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.device.registrator.impl.OnuStateChangeCallbackRegistrator.ONU_STATE_CHANGE_NS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.DeviceNotificationClientListener;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DeviceNotificationListenerTest {
    private DeviceNotificationListener m_deviceNotificationListener;
    private final String m_deviceName = "DPU1";

    @Mock
    private Device m_device;

    @Mock
    private DeviceAdapterId m_deviceAdapterId;

    @Mock
    private NotificationService m_notificationService;

    private Notification m_notification;
    @Mock
    private AdapterContext m_adapterContext;
    @Mock
    private DeviceInterface m_deviceInterface;
    private Notification m_normalizedNotification;
    @Mock
    private PmaRegistry m_pmaRegistry;
    private String m_getConfigResponseStr;
    @Captor
    private ArgumentCaptor<NotificationContext> m_notificationContextCaptor;
    @Captor
    private ArgumentCaptor<Notification> m_notificationCaptor;

    private List<DeviceNotificationClientListener> m_deviceNotificationClientListeners = new ArrayList<>();

    @Before
    public void setUp() throws NetconfMessageBuilderException, ExecutionException {
        MockitoAnnotations.initMocks(this);
        m_deviceNotificationListener = new DeviceNotificationListener(m_device, m_deviceAdapterId, m_notificationService, m_adapterContext, m_pmaRegistry, m_deviceNotificationClientListeners);
        when(m_device.getDeviceName()).thenReturn(m_deviceName);
        String notifStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "\t<eventTime>2019-07-08T10:19:49+00:00</eventTime>\n" +
                "\t<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">\n" +
                "\t\t<alarms:resource xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='int1']</alarms:resource>\n" +
                "\t\t<alarms:alarm-type-id xmlns:samp-al=\"urn:broadband-forum-org:yang:sample-types\">samp-al:alarm-type1</alarms:alarm-type-id>\n" +
                "\t\t<alarms:perceived-severity>major</alarms:perceived-severity>\n" +
                "\t\t<alarms:alarm-text>raisealarm</alarms:alarm-text>\n" +
                "\t</alarms:alarm-notification>\n" +
                "</notification>\n";
        m_notification = new NetconfNotification(DocumentUtils.stringToDocument(notifStr));
        String normalizedNotif = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "\t<eventTime>2019-07-08T10:19:49+00:00</eventTime>\n" +
                "\t<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">\n" +
                "\t\t<alarms:resource xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='int1']</alarms:resource>\n" +
                "\t\t<alarms:alarm-type-id xmlns:samp-al=\"urn:broadband-forum-org:yang:sample-types\">samp-al:alarm-type2</alarms:alarm-type-id>\n" +
                "\t\t<alarms:perceived-severity>warning</alarms:perceived-severity>\n" +
                "\t\t<alarms:alarm-text>raisealarm:new</alarms:alarm-text>\n" +
                "\t</alarms:alarm-notification>\n" +
                "</notification>\n";
        m_normalizedNotification = new NetconfNotification(DocumentUtils.stringToDocument(normalizedNotif));
        when(m_adapterContext.getDeviceInterface()).thenReturn(m_deviceInterface);

        m_getConfigResponseStr = "<rpc-reply message-id=\"internal\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "\t<data>\n" +
                "\t\t<hw:hardware xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n" +
                "\t\t\t<hw:component>\n" +
                "\t\t\t\t<hw:name>SLOT</hw:name>\n" +
                "\t\t\t\t<hw:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:idref1</hw:class>\n" +
                "\t\t\t\t<bbf-hw-ext:model-name xmlns:bbf-hw-ext=\"urn:bbf:yang:bbf-hardware-extension\">12345</bbf-hw-ext:model-name>\n" +
                "\t\t\t\t<hw:parent>C2</hw:parent>\n" +
                "\t\t\t\t<hw:parent-rel-pos>1</hw:parent-rel-pos>\n" +
                "\t\t\t</hw:component>\n" +
                "\t\t\t<hw:component>\n" +
                "\t\t\t\t<hw:name>PORT</hw:name>\n" +
                "\t\t\t\t<hw:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:idref1</hw:class>\n" +
                "\t\t\t\t<hw:parent>SLOT</hw:parent>\n" +
                "\t\t\t\t<hw:parent-rel-pos>1</hw:parent-rel-pos>\n" +
                "\t\t\t</hw:component>\n" +
                "\t\t</hw:hardware>\n" +
                "\t\t<if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "\t\t\t<if:interface>\n" +
                "\t\t\t\t<if:name>chterm</if:name>\n" +
                "\t\t\t\t<if:enabled>true</if:enabled>\n" +
                "\t\t\t\t<bbf-if-port-ref:port-layer-if xmlns:bbf-if-port-ref=\"urn:bbf:yang:bbf-interface-port-reference\">PORT</bbf-if-port-ref:port-layer-if>\n" +
                "\t\t\t\t<if:type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-termination</if:type>\n" +
                "\t\t\t</if:interface>\n" +
                "\t\t</if:interfaces>\n" +
                "\t</data>\n" +
                "</rpc-reply>";
        NetConfResponse netConfResponse = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(m_getConfigResponseStr));
        Map<NetConfResponse, List<Notification>> map = new HashMap<>();
        map.put(netConfResponse, Collections.emptyList());
        when(m_pmaRegistry.executeNC(any(), any())).thenReturn(map);
        m_deviceNotificationClientListeners.add(new DeviceNotificationClientListener() {
            @Override
            public void deviceNotificationReceived(Device device, Notification notification) {
                // Testing purpose
            }
        });
    }

    @Test
    public void testNotificationReceivedNoNormalization() {
        when(m_deviceInterface.normalizeNotification(m_notification)).thenReturn(m_notification);
        m_deviceNotificationListener.notificationReceived(m_notification);
        verify(m_notificationService).executeCallBack(eq(m_notification), m_notificationContextCaptor.capture());
        NotificationContext context = m_notificationContextCaptor.getValue();
        assertEquals(m_device, context.get(Device.class.getSimpleName()));
        assertEquals(m_deviceAdapterId, context.get(DeviceAdapterId.class.getSimpleName()));
    }

    @Test
    public void testNotificationReceivedForNormalization() {
        when(m_deviceInterface.normalizeNotification(m_notification)).thenReturn(m_normalizedNotification);
        m_deviceNotificationListener.notificationReceived(m_notification);
        verify(m_notificationService).executeCallBack(eq(m_normalizedNotification), m_notificationContextCaptor.capture());
        NotificationContext context = m_notificationContextCaptor.getValue();
        assertEquals(m_device, context.get(Device.class.getSimpleName()));
        assertEquals(m_deviceAdapterId, context.get(DeviceAdapterId.class.getSimpleName()));
    }

    @Test
    public void testNormalizedNotificationIsNull() {
        when(m_deviceInterface.normalizeNotification(m_notification)).thenReturn(null);
        m_deviceNotificationListener.notificationReceived(m_notification);
        verifyZeroInteractions(m_notificationService);
    }

    @Test
    public void testOnuStateChangeNotificationWithDetectedRegIdAbsent() throws NetconfMessageBuilderException {
        String onuNotificationStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "  <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">\n" +
                "    <bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>\n" +
                "    <bbf-xpon-onu-states:channel-termination-ref>chterm</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "    <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "    <bbf-xpon-onu-states:onu-state xmlns:bbf-xpon-onu-types=\"urn:bbf:yang:bbf-xpon-more-types\">bbf-xpon-onu-types:onu-present</bbf-xpon-onu-states:onu-state>\n" +
                "    <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                "  </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>\n";
        m_notification = new NetconfNotification(DocumentUtils.stringToDocument(onuNotificationStr));
        when(m_deviceInterface.normalizeNotification(m_notification)).thenReturn(m_notification);

        m_deviceNotificationListener.notificationReceived(m_notification);
        verifyNotification("DPU1-SLOT-PORT");
    }

    @Test
    public void testOnuStateChangeNotitifcationReplaceDetectedRegId() throws NetconfMessageBuilderException {
        String onuNotificationStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "  <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">\n" +
                "    <bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>\n" +
                "    <bbf-xpon-onu-states:channel-termination-ref>chterm</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "    <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "    <bbf-xpon-onu-states:onu-state xmlns:bbf-xpon-onu-types=\"urn:bbf:yang:bbf-xpon-more-types\">bbf-xpon-onu-types:onu-present</bbf-xpon-onu-states:onu-state>\n" +
                "    <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                "    <bbf-xpon-onu-states:detected-registration-id>shouldBeReplaced</bbf-xpon-onu-states:detected-registration-id>\n" +
                "  </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>\n";
        m_notification = new NetconfNotification(DocumentUtils.stringToDocument(onuNotificationStr));
        when(m_deviceInterface.normalizeNotification(m_notification)).thenReturn(m_notification);

        m_deviceNotificationListener.notificationReceived(m_notification);
        verifyNotification("DPU1-SLOT-PORT");
    }

    @Test
    public void testOnuStateChangeNotitifcationWithChannelRefNotInDS() throws NetconfMessageBuilderException {
        String onuNotificationStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "  <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">\n" +
                "    <bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>\n" +
                "    <bbf-xpon-onu-states:channel-termination-ref>chtermnotpresentinds</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "    <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "    <bbf-xpon-onu-states:onu-state xmlns:bbf-xpon-onu-types=\"urn:bbf:yang:bbf-xpon-more-types\">bbf-xpon-onu-types:onu-present</bbf-xpon-onu-states:onu-state>\n" +
                "    <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                "    <bbf-xpon-onu-states:detected-registration-id>shouldBeReplaced</bbf-xpon-onu-states:detected-registration-id>\n" +
                "  </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>\n";
        m_notification = new NetconfNotification(DocumentUtils.stringToDocument(onuNotificationStr));
        when(m_deviceInterface.normalizeNotification(m_notification)).thenReturn(m_notification);

        m_deviceNotificationListener.notificationReceived(m_notification);
        verifyNotification("DPU1--");
    }

    private Element fetchDetectedRefId(Notification notification) {
        Element detectedRegId = null;
        QName detectedRegistrationIdQname = QName.create(ONU_STATE_CHANGE_NS,
                "detected-registration-id");
        NodeList list = notification.getNotificationElement().getChildNodes();
        for (int index = 0; index < list.getLength(); index++) {
            Node innerNode = list.item(index);
            if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element innerNodeEle = (Element) innerNode;
                String innerNodeName = innerNodeEle.getLocalName();
                QName attributeQName = QName.create(innerNodeEle.getNamespaceURI(), innerNodeName);
                if (detectedRegistrationIdQname.equals(attributeQName)) {
                    detectedRegId = innerNodeEle;
                }
            }
        }
        return detectedRegId;
    }

    private void verifyNotification(String expectedAttchmentPoint) {
        verify(m_notificationService).executeCallBack(m_notificationCaptor.capture(), m_notificationContextCaptor.capture());
        NotificationContext context = m_notificationContextCaptor.getValue();
        assertEquals(m_device, context.get(Device.class.getSimpleName()));
        assertEquals(m_deviceAdapterId, context.get(DeviceAdapterId.class.getSimpleName()));
        assertEquals(expectedAttchmentPoint, fetchDetectedRefId(m_notificationCaptor.getValue()).getTextContent());
    }
}