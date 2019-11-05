package org.broadband_forum.obbaa.pma;

import org.broadband_forum.obbaa.netconf.api.messages.Notification;

public interface NonNCNotificationHandler {

    /**
     * This api must be called by VDA when a notification is received.
     * @param ip : ip of the device from which notification is received
     * @param port : port of the device from which notification is received
     * @param notification : Notification received from the device
     */
    void handleNotification(String ip, String port, Notification notification);

}
