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

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;

/**
 * <p>
 * Parser for ONU state change notification
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class ONUNotification extends NetconfNotification {

    private static final Logger LOGGER = Logger.getLogger(ONUNotification.class);
    private static final String IETF_INTERFACES_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String BBF_XPON_ONU_STATE_NS = "urn:bbf:yang:bbf-xpon-onu-state";
    private static final String BBF_XPON_ONU_STATES_NS = "urn:bbf:yang:bbf-xpon-onu-states";
    private static final String BBF_XPON_NS = "urn:bbf:yang:bbf-xpon";
    private static final String ONU_PRESENCE_STATE_CHANGE_XPATH = "/nc:notification/ietf-interfaces:interfaces-state/"
            + "ietf-interfaces:interface/bbf-xpon:channel-termination/bbf-xpon-onu-state:onu-presence-state-change/";
    private String m_serialNumber;
    private String m_oltDeviceName;
    private Document m_document;
    private QName m_notificationType;
    private String m_ONUStateChangeNameSpace;
    private String m_event;
    private final Notification m_notification;
    private volatile XPathExpression c_xPathSn;
    private volatile XPathExpression c_xPathRegId;
    private volatile XPathExpression c_xPathOnuid;
    private volatile XPathExpression c_xPathOnuState;
    private volatile XPathExpression c_xPathCtermRef;
    private volatile XPathExpression c_xPathOnuStateLastChange;
    private volatile XPathExpression c_xPathVAniRef;
    private MessageFormatter m_messageFormatter;


    public ONUNotification(final Notification notification, String oltDeviceName, MessageFormatter messageFormatter)
            throws NetconfMessageBuilderException {
        super(notification.getNotificationDocument());
        m_notification = notification;
        m_oltDeviceName = oltDeviceName;
        m_document = getNotificationDocument();
        m_notificationType = notification.getType();
        m_ONUStateChangeNameSpace = m_notificationType.getNamespace().toString();
        m_event = m_notificationType.getLocalName();
        m_messageFormatter = messageFormatter;
        initXPath();
    }

    private void initXPath() {
        synchronized (ONUNotification.class) {
            try {
                NamespaceContext ctx = new NamespaceContext() {
                    public String getNamespaceURI(String prefix) {
                        if (prefix.equals("nc")) {
                            return "urn:ietf:params:xml:ns:netconf:notification:1.0";
                        }
                        if (prefix.equals("bbf-xpon-onu-state")) {
                            return BBF_XPON_ONU_STATE_NS;
                        }
                        if (prefix.equals("bbf-xpon-onu-states")) {
                            return BBF_XPON_ONU_STATES_NS;
                        }
                        if (prefix.equals("ietf-interfaces")) {
                            return IETF_INTERFACES_NS;
                        }
                        if (prefix.equals("bbf-xpon")) {
                            return BBF_XPON_NS;
                        }
                        return null;
                    }

                    public Iterator getPrefixes(String val) {
                        return null;
                    }

                    public String getPrefix(String uri) {
                        return null;
                    }
                };
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(ctx);
                if (m_ONUStateChangeNameSpace.equals(BBF_XPON_ONU_STATES_NS)) {
                    c_xPathSn = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:detected-serial-number/text()");
                    c_xPathRegId = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:detected-registration-id/text()");
                    c_xPathOnuid = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:onu-id/text()");
                    c_xPathOnuState = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:onu-state/text()");
                    c_xPathCtermRef = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:channel-termination-ref/text()");
                    c_xPathOnuStateLastChange = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:onu-state-last-change/text()");
                    c_xPathVAniRef = xpath.compile("/nc:notification/bbf-xpon-onu-states:onu-state-change/"
                            + "bbf-xpon-onu-states:v-ani-ref/text()");
                } else {
                    c_xPathSn = xpath.compile(ONU_PRESENCE_STATE_CHANGE_XPATH
                            + "bbf-xpon-onu-state:detected-serial-number/text()");
                    c_xPathRegId = xpath.compile(ONU_PRESENCE_STATE_CHANGE_XPATH
                            + "bbf-xpon-onu-state:detected-registration-id/text()");
                    c_xPathOnuid = xpath.compile(ONU_PRESENCE_STATE_CHANGE_XPATH
                            + "bbf-xpon-onu-state:onu-id/text()");
                    c_xPathOnuState = xpath.compile(ONU_PRESENCE_STATE_CHANGE_XPATH
                            + "bbf-xpon-onu-state:onu-presence-state/text()");
                    c_xPathCtermRef = xpath.compile("/nc:notification/ietf-interfaces:interfaces-state/ietf-interfaces:interface/"
                            + "ietf-interfaces:name/text()");
                    c_xPathOnuStateLastChange = xpath.compile(ONU_PRESENCE_STATE_CHANGE_XPATH
                            + "bbf-xpon-onu-state:last-change/text()");
                    c_xPathVAniRef = xpath.compile(ONU_PRESENCE_STATE_CHANGE_XPATH
                            + "bbf-xpon-onu-state:v-ani-ref/text()");
                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public QName getType() {
        return m_notificationType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSerialNo() == null) ? 0 : getSerialNo().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ONUNotification notification = (ONUNotification) obj;
        if (this.getSerialNo() == null || notification.getSerialNo() == null) {
            if (this.getSerialNo() == null && notification.getSerialNo() == null) {
                return true;
            }
            return false;
        }
        return this.getSerialNo().equals(notification.getSerialNo());
    }

    public String getEvent() {
        return m_event;
    }

    public String getSerialNo() {
        if (m_serialNumber == null) {
            try {
                m_serialNumber = c_xPathSn.evaluate(m_document);
            } catch (XPathExpressionException e) {
                LOGGER.error("Error while evaluating onu serial number", e);
            }
        }
        return m_serialNumber;
    }

    public String getRegId() {
        try {
            return c_xPathRegId.evaluate(m_document).trim();
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while evaluating onu registration id", e);
        }
        return null;
    }

    public String getOnuState() {
        try {
            String state = c_xPathOnuState.evaluate(m_document);
            if (state.contains(":")) {
                state = state.trim().split(":")[1];
            }
            return state;
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while evaluating onu state", e);
        }
        return null;
    }

    public String getOnuId() {
        try {
            return c_xPathOnuid.evaluate(m_document);
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while evaluating onu id", e);
        }
        return null;
    }

    public String getVAniRef() {
        try {
            return c_xPathVAniRef.evaluate(m_document);
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while evaluating V-Ani-Ref", e);
        }
        return null;
    }

    public String getChannelTermRef() {
        try {
            return c_xPathCtermRef.evaluate(m_document);
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while evaluating channel term ref", e);
        }
        return null;
    }

    public String getOnuStateLastChanged() {
        try {
            return c_xPathOnuStateLastChange.evaluate(m_document);
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while evaluating channel term ref", e);
        }
        return null;
    }

    public String getOltDeviceName() {
        return m_oltDeviceName;
    }

    public Notification getNotification() {
        return m_notification;
    }

    public static boolean isONUStateChangeNotif(QName notificationType) {
        boolean onuNotif = false;
        if (IETF_INTERFACES_NS.equals(notificationType.getNamespace().toString())) {
            onuNotif = true;
        } else if (ONUConstants.ONU_STATE_CHANGE_NS.equals(notificationType.getNamespace().toString())) {
            onuNotif = true;
        }
        return onuNotif;
    }

    public String getMappedEvent() {
        String mappedEvent = null;
        String onuState = getOnuState();
        switch (onuState) {
            case "onu-present":
            case "onu-present-and-on-intended-channel-termination":
            case "onu-present-and-in-wavelength-discovery":
            case "onu-present-and-discovery-tune-failed":
            case "onu-present-and-no-v-ani-known-and-o5-failed":
            case "onu-present-and-no-v-ani-known-and-o5-failed-no-onu-id":
            case "onu-present-and-no-v-ani-known-and-o5-failed-undefined":
            case "onu-present-and-v-ani-known-and-o5-failed":
            case "onu-present-and-v-ani-known-and-o5-failed-no-id":
            case "onu-present-and-v-ani-known-and-o5-failed-no-onu-id":
            case "onu-present-and-v-ani-known-and-o5-failed-undefined":
            case "onu-present-and-no-v-ani-known-and-in-o5":
            case "onu-present-and-no-v-ani-known-and-o5-passed":
            case "onu-present-and-no-v-ani-known-and-unclaimed":
            case "onu-present-and-v-ani-known-but-intended-ct-unknown":
            case "onu-present-and-in-discovery":
                if (m_messageFormatter instanceof GpbFormatter) {
                    mappedEvent = ONUConstants.CREATE_ONU;
                } else {
                    mappedEvent = ONUConstants.ONU_DETECTED;
                }
                break;
            case "onu-present-and-emergency-stopped":
            case "onu-not-present":
            case "onu-not-present-with-v-ani":
            case "onu-not-present-without-v-ani":
                if (m_messageFormatter instanceof GpbFormatter) {
                    mappedEvent = ONUConstants.DELETE_ONU;
                } else {
                    mappedEvent = ONUConstants.ONU_UNDETECTED;
                }
                break;
            default:
                mappedEvent = ONUConstants.NOT_APPLICABLE;
        }
        return mappedEvent;
    }
}

