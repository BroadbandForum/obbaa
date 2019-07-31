package org.broadband_forum.obbaa.device.alarm.callback;

import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBack;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnuStateChangeCallback implements NotificationCallBack {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnuStateChangeCallback.class);
    private final QName m_onuStateChangeNotificationType;
    private NotificationService m_notificationService;

    public OnuStateChangeCallback(NotificationService notificationService, String moduleNs) {
        m_notificationService = notificationService;
        m_onuStateChangeNotificationType = QName.create(moduleNs, "onu-state-change");
    }

    @Override
    public void onNotificationReceived(Notification notification, NotificationContext context, DateTime receivedTime) {
        if (m_onuStateChangeNotificationType.equals(notification.getType())) {
            LOGGER.debug(String.format("This notification is going to be forwarded to the NBI %s",
                    notification.notificationToPrettyString()));
            m_notificationService.sendNotification(NetconfResources.STATE_CHANGE, notification);
        }
    }

    @Override
    public void resynchronize(Object deviceRefId) {
        //TO-DO
    }
}
