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

package org.broadband_forum.obbaa.protocol.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.device.listener.ProcessTrapCallBack;
import org.broadband_forum.obbaa.dmyang.entities.SnmpAuthentication;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.NonNCNotificationHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.snmp4j.CommandResponderEvent;


class SnmpTrapHandler implements ProcessTrapCallBack {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpTrapHandler.class);
    private String m_trapDeviceAddress = "0.0.0.0";
    private String m_trapListenerAddress = "0.0.0.0";
    private String m_trapListenerPort = "162";
    private static List<String> devices = new ArrayList<String>();
    private NonNCNotificationHandler m_nonNCNotificationHandler = null;

    SnmpTrapHandler(SnmpAuthentication auth, NonNCNotificationHandler nonNCNotificationHan) {
        try {
            final InetAddress inetAddress = InetAddress.getByName(auth.getAddress());
            m_trapListenerAddress = inetAddress.getHostAddress();
            m_trapDeviceAddress = auth.getAddress();
            m_trapListenerPort = auth.getTrapPort();
            m_nonNCNotificationHandler = nonNCNotificationHan;

            //Send trap configuration details to Target
            if (sendTrapConfig()) {
                //Stores the device IP address in the list.
                devices.add(m_trapDeviceAddress);
            }
            //ToDo:: Device IP should be removed from the list while
            //deleting the corresponding adapter instance.
        } catch (IOException e) {
            LOGGER.error("Error when creating snmp target", e);
            throw new RuntimeException("Error when creating snmp target", e);
        }
    }

    public String getTrapDeviceAddress() {
        return m_trapDeviceAddress;
    }

    public String getTrapPort() {
        return m_trapListenerPort;
    }

    public boolean sendTrapConfig() throws IOException {
        //ToDo:: Retrieve Host IP and configure Target SNMP trap configuration
        return true;
    }

    @Override
    public void processTrap(CommandResponderEvent event) {
        String senderAddress = event.getPeerAddress().toString();
        String sender = senderAddress.substring(0, senderAddress.indexOf("/"));

        if (devices.contains(sender)) {
            LOGGER.info("TRAP received from <<" + sender + ">> " + event.getPDU().toString());
            //The received event should be converted to NETCONF notification
            //before sending to PMA. Conversion differs from device to device.
            //This function sends a sample notification as below;
            Notification notification = null;
            String notifStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n"
                + "\t<eventTime>2019-07-25T05:53:36+00:00</eventTime>\n"
                + "\t<bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">\n"
                + "\t\t<bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>\n"
                + "\t\t<bbf-xpon-onu-states:channel-termination-ref>ct1</bbf-xpon-onu-states:channel-termination-ref>\n"
                + "\t\t<bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00"
                + "</bbf-xpon-onu-states:onu-state-last-change>\n"
                + "\t\t<bbf-xpon-onu-states:onu-state xmlns:bbf-xpon-onu-types=\"urn:bbf:yang:bbf-xpon-more-types\">bbf-xpon-onu-types"
                + ":onu-present-and-unexpected</bbf-xpon-onu-states:onu-state>\n"
                + "\t\t<bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n"
                + "\t</bbf-xpon-onu-states:onu-state-change>\n"
                + "</notification>\n";
            try {
                notification = new NetconfNotification(DocumentUtils.stringToDocument(notifStr));
            } catch (NetconfMessageBuilderException ex) {
                LOGGER.error("NetconfMessageBuilder ex", ex);
                throw new RuntimeException(ex);
            }

            if (null != notification) {
                LOGGER.debug("ProtTranslateSnmp: Sending trap notification to PMA");
                m_nonNCNotificationHandler.handleNotification(m_trapListenerAddress, m_trapListenerPort, notification);
            }
        }
    }
}
