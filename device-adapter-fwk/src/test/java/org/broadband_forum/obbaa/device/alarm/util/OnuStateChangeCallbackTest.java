package org.broadband_forum.obbaa.device.alarm.util;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.STATE_CHANGE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.broadband_forum.obbaa.device.alarm.callback.OnuStateChangeCallback;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OnuStateChangeCallbackTest {

    @Mock
    private NotificationService m_notificationService;
    private OnuStateChangeCallback m_onuStateChangeCallBack;
    private NetconfNotification m_notification;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_onuStateChangeCallBack =  new OnuStateChangeCallback(m_notificationService, "urn:bbf:yang:bbf-xpon-onu-states");
    }

    @After
    public void tearDown() {
        RequestScope.resetScope();
    }

    @Test
    public void testNotificationReceived() throws NetconfMessageBuilderException {
        String notificationStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "  <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">\n" +
                "    <bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>\n" +
                "    <bbf-xpon-onu-states:channel-termination-ref>Int1</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "    <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "    <bbf-xpon-onu-states:onu-state xmlns:bbf-xpon-onu-types=\"urn:bbf:yang:bbf-xpon-more-types\">bbf-xpon-onu-types:onu-" +
                "present</bbf-xpon-onu-states:onu-state>\n" +
                "    <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                "  </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>";

        m_notification = new NetconfNotification(DocumentUtils.stringToDocument(notificationStr));
        m_onuStateChangeCallBack.onNotificationReceived(m_notification, null, null);
        verify(m_notificationService).sendNotification(STATE_CHANGE, m_notification);
    }

    @Test
    public void testNotificationReceivedWithDifferentQname() throws NetconfMessageBuilderException {
        String notificationStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "  <bbf-xpon-onu-states:onu-state-change-for-test xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">\n" +
                "    <bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>\n" +
                "    <bbf-xpon-onu-states:channel-termination-ref>Int1</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "    <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "    <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                "  </bbf-xpon-onu-states:onu-state-change-for-test>\n" +
                "</notification>";

        m_notification = new NetconfNotification(DocumentUtils.stringToDocument(notificationStr));
        m_onuStateChangeCallBack.onNotificationReceived(m_notification, null, null);
        verifyZeroInteractions(m_notificationService);
    }
}
