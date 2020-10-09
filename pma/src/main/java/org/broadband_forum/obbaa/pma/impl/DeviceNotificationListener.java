package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.device.registrator.impl.OnuStateChangeCallbackRegistrator.ONU_STATE_CHANGE_NS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.pma.DeviceNotificationClientListener;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * This class is used for reaction from DPU
 */
public class DeviceNotificationListener implements NotificationListener {

    private static final Logger LOGGER = Logger.getLogger(DeviceNotificationListener.class);
    private static final String INTERFACE_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String PREFIX_INTERFACE = "if";
    private static final String PORT_REF_NS = "urn:bbf:yang:bbf-interface-port-reference";
    private static final String PREFIX_PORT_REF = "bbf-if-port-ref";
    private static final String HARDWARE_NS = "urn:ietf:params:xml:ns:yang:ietf-hardware";
    private static final String PREFIX_HARDWARE = "hw";
    private Device m_device;
    private NotificationService m_notificationService;
    private DeviceAdapterId m_deviceAdapterId;
    private AdapterContext m_adapterContext;
    private PmaRegistry m_pmaRegistry;
    private List<DeviceNotificationClientListener> m_deviceNotificationClientListeners;

    public DeviceNotificationListener(Device device, DeviceAdapterId deviceAdapterId, NotificationService notificationService,
                                      AdapterContext adapterContext, PmaRegistry pmaRegistry,
                                      List<DeviceNotificationClientListener> clientListeners) {
        m_device = device;
        m_notificationService = notificationService;
        m_deviceAdapterId = deviceAdapterId;
        m_adapterContext = adapterContext;
        m_pmaRegistry = pmaRegistry;
        m_deviceNotificationClientListeners = clientListeners;
    }

    @Override
    public void notificationReceived(final Notification notification) {
        LOGGER.info(String.format("Received device notification from device: %s with notification: %s",
                m_device.getDeviceName(), notification.notificationToString()));
        Notification normalizedNotification = m_adapterContext.getDeviceInterface().normalizeNotification(notification);
        if (normalizedNotification != null) {
            RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
                @Override
                protected Void execute() throws RequestScopeExecutionException {
                    try {
                        NotificationContext context = new NotificationContext();
                        context.put(Device.class.getSimpleName(), m_device);
                        context.put(DeviceAdapterId.class.getSimpleName(), m_deviceAdapterId);
                        QName onuStateChangeNotification = QName.create(ONU_STATE_CHANGE_NS, "onu-state-change");
                        if (normalizedNotification.getType() != null) {
                            for (DeviceNotificationClientListener listener : m_deviceNotificationClientListeners) {
                                listener.deviceNotificationReceived(m_device, normalizedNotification);
                            }
                        }
                        if (onuStateChangeNotification.equals(normalizedNotification.getType())) {
                            getDeviceSlotAndPort(m_device.getDeviceName(), normalizedNotification);
                        }
                        m_notificationService.executeCallBack(normalizedNotification, context);
                    } catch (Exception e) {
                        LOGGER.error("Error while processing notification", e);
                    }
                    return null;
                }
            });
        } else {
            LOGGER.debug("The normalized notification received from the adapter is null");
        }
    }

    private void getDeviceSlotAndPort(String deviceName, Notification notification) {
        try {
            GetConfigRequest getConfig = new GetConfigRequest();
            getConfig.setSourceRunning();
            getConfig.setMessageId("internal");
            Map<NetConfResponse, List<Notification>> response = m_pmaRegistry.executeNC(deviceName, getConfig.requestToString());
            String xpathExpression;
            List<String> evaluatedXpath = new ArrayList<>();
            Pair<String, Element> channelTermRefAndDetectedRefId = fetchChannelRefAndDetectedRefId(notification);
            Document responseDocument = response.entrySet().iterator().next().getKey().getResponseDocument();
            String channelTermRef = channelTermRefAndDetectedRefId.getFirst();
            if (channelTermRef != null && !channelTermRef.isEmpty()) {
                xpathExpression = "//if:interfaces/if:interface[if:name='" + channelTermRef + "']/bbf-if-port-ref:port-layer-if/text()";
                evaluatedXpath = evaluateXPath(responseDocument, xpathExpression);
            }
            String port = "";
            String slot = "";
            if (!evaluatedXpath.isEmpty()) {
                port = evaluatedXpath.get(0);
                xpathExpression = "//hw:hardware/hw:component[hw:name='" + port + "']/hw:parent/text()";
                evaluatedXpath = evaluateXPath(responseDocument, xpathExpression);
                if (!evaluatedXpath.isEmpty()) {
                    slot = evaluatedXpath.get(0);
                }
            }
            String attachmentPoint = deviceName + "-" + slot + "-" + port;
            LOGGER.debug(String.format("The OltName-OltSlot-OltPort combination is %s", attachmentPoint));
            if (attachmentPoint.length() > 36) {
                LOGGER.warn(String.format("For detected-registration-id %s - length exceeds 36 characters as per"
                        + " bbf-xpon-onu-states yang", attachmentPoint));
            }
            Element detectedrefIdEle = channelTermRefAndDetectedRefId.getSecond();
            appendDetectedRefIdToNotification(notification, attachmentPoint, detectedrefIdEle);
        } catch (Exception e) {
            LOGGER.error("Error occurred during evaluating the notification", e);
        }
    }

    private void appendDetectedRefIdToNotification(Notification notification, String attachmentPoint, Element detectedrefIdEle) {
        if (detectedrefIdEle == null) {
            Element detectedTermId = notification.getNotificationElement().getOwnerDocument().createElementNS(ONU_STATE_CHANGE_NS,
                    "detected-registration-id");
            detectedTermId.setTextContent(attachmentPoint);
            notification.getNotificationElement().appendChild(detectedTermId);
        } else {
            detectedrefIdEle.setTextContent(attachmentPoint);
        }
    }

    private Pair<String, Element> fetchChannelRefAndDetectedRefId(Notification notification) {
        String channelTerminationRef = null;
        Element detectedRegId = null;
        QName channelTerminationRefQname = QName.create(ONU_STATE_CHANGE_NS,
                "channel-termination-ref");
        QName detectedRegistrationIdQname = QName.create(ONU_STATE_CHANGE_NS,
                "detected-registration-id");
        NodeList list = notification.getNotificationElement().getChildNodes();
        for (int index = 0; index < list.getLength(); index++) {
            Node innerNode = list.item(index);
            if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element innerNodeEle = (Element) innerNode;
                String innerNodeName = innerNodeEle.getLocalName();
                QName attributeQName = QName.create(innerNodeEle.getNamespaceURI(), innerNodeName);
                try {
                    if (channelTerminationRefQname.equals(attributeQName)) {
                        channelTerminationRef = innerNodeEle.getTextContent().trim();
                    } else if (detectedRegistrationIdQname.equals(attributeQName)) {
                        detectedRegId = innerNodeEle;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error when extracting channel channel-termination-ref/detected-registration-id from"
                            + "onu-state-change notification", e);
                }
            }
        }
        return new Pair<>(channelTerminationRef, detectedRegId);
    }

    private static List<String> evaluateXPath(Document document, String xpathExpression) {
        // Create XPathFactory object
        XPathFactory xpathFactory = XPathFactory.newInstance();
        // Create XPath object
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public Iterator getPrefixes(String arg0) {
                return Collections.emptyIterator();
            }

            @Override
            public String getPrefix(String url) {
                if (url != null) {
                    switch (url) {
                        case INTERFACE_NS:
                            return PREFIX_INTERFACE;
                        case PORT_REF_NS:
                            return PREFIX_PORT_REF;
                        case HARDWARE_NS:
                            return PREFIX_HARDWARE;
                        default:
                            return null;
                    }
                }
                return null;
            }

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix != null) {
                    switch (prefix) {
                        case PREFIX_INTERFACE:
                            return INTERFACE_NS;
                        case PREFIX_PORT_REF:
                            return PORT_REF_NS;
                        case PREFIX_HARDWARE:
                            return HARDWARE_NS;
                        default:
                            return null;
                    }
                }
                return null;
            }
        });

        List<String> values = new ArrayList<>();
        try {
            // Create XPathExpression object
            XPathExpression expr = xpath.compile(xpathExpression);
            // Evaluate expression result on XML document
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                values.add(nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Error during the XPathEvaluation of " + xpathExpression);
        }
        return values;
    }
}
