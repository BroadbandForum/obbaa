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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.ALARM_NOTIFICATION;
import static org.broadband_forum.obbaa.onu.ONUConstants.ALARM_TEXT_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.ALARM_TYPE_ID_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.ALARM_TYPE_QUALIFIER_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.BBF_ETH_ALARM_TYPE_MODULE_NAME;
import static org.broadband_forum.obbaa.onu.ONUConstants.BBF_HW_TX_ALARM_TYPE_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.BBF_SW_MGMT_SOFTWARE_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.BBF_XPON_ONU_ALARM_TYPE_MODULE_NAME;
import static org.broadband_forum.obbaa.onu.ONUConstants.COMPONENT_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.DEVICE_INSTANCE_IDENTIFIER_FORMAT;
import static org.broadband_forum.obbaa.onu.ONUConstants.ETH_ALARM_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.ETH_ALARM_TYPE_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.ETH_ALARM_XMLNS_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.HW_TX_ALARM_TYPE_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.HW_TX_ALARM_TYPE_XMLNS_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.HW_XMLNS_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_ALARM_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_ALARM_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_HARDWARE;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_HARDWARE_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_HARDWARE_RESOURCE_IDENTIFIER_FORMAT;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_HARDWARE_STATE_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_HW_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_INTERFACES;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_INTERFACES_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_INTERFACES_RESOURCE_IDENTIFIER_FORMAT;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_INTERFACE_XMLNS_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.INTERFACES;
import static org.broadband_forum.obbaa.onu.ONUConstants.MODEL_NAME_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.NW_MGR_XMLNS_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_NAME_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_NAME_TAG;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_NO_ONU_ID;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_UNDEFINED;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_V_ANI_KNOW_BUT_INTENDED_CT_UNKNOWN;
import static org.broadband_forum.obbaa.onu.ONUConstants.PERCEIVED_SEVERITY_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.RESOURCE_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.REVISIONS_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.REVISION_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.SERIAL_NUM_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.SOFTWARE_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.TIME_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.XML_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.XPON_ONU_ALARM_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.XPON_ONU_ALARM_TYPE_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.XPON_ONU_ALARM_XMLNS_PREFIX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoints;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.MediatedDeviceNetconfSession;
import org.broadband_forum.obbaa.onu.MediatedNetworkFunctionNetconfSession;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.UnknownONUHandler;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.message.NetworkWideTag;
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.message.ResponseData;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.onu.notification.OnuAuthenticationReportNotification;
import org.broadband_forum.obbaa.onu.notification.OnuDiscoveryResultNotification;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public final class VOLTManagementUtil {
    private static final Logger LOGGER = Logger.getLogger(VOLTManagementUtil.class);
    private static final String INTERFACE_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String PREFIX_INTERFACE = "if";
    private static final String XPON_NS = "urn:bbf:yang:bbf-xpon";
    private static final String PREFIX_XPON = "bbf-xpon";
    private static final Object LOCK = new Object();
    private static final AtomicLong MESSAGE_ID = new AtomicLong(1000);
    private static Map<String, Pair<String, String>> m_requestMap = new HashMap<>();
    private static Map<String, ArrayList<String>> m_networkFunctionMap = new HashMap<>();
    private static Map<String, ExpectedAttachmentPoint> m_matchedExpectedAttachmentPoint = new HashMap<>();
    private static Map<Future<NetConfResponse>, String> m_responseFutureAndMessageIdMap = new HashMap<>();

    private VOLTManagementUtil() {
        //Not called
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
                        case XPON_NS:
                            return PREFIX_XPON;
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
                        case PREFIX_XPON:
                            return XPON_NS;
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

    public static boolean isInPermittedAttachmentPoint(Device onuDevice, String oltDeviceName, ONUNotification onuNotification,
                                                       PmaRegistry pmaRegistry) {
        Set<ExpectedAttachmentPoint> expectedAttachmentPoints = onuDevice.getDeviceManagement().getOnuConfigInfo()
                .getExpectedAttachmentPoints().getExpectedAttachmentPointSet();
        boolean attachmentPointMatched = true;
        for (ExpectedAttachmentPoint expectedAttachmentPoint : expectedAttachmentPoints) {
            if (expectedAttachmentPoint.getOltName() != null && expectedAttachmentPoint.getChannelPartitionName() != null) {
                String actualChannelPartition = getChannelPartition(oltDeviceName, onuNotification.getChannelTermRef(), pmaRegistry);
                if (expectedAttachmentPoint.getOltName().equals(oltDeviceName)
                        && expectedAttachmentPoint.getChannelPartitionName().equals(actualChannelPartition)) {
                    attachmentPointMatched = true;
                    updateMatchedAttachmentPointMap(onuDevice.getDeviceName(), expectedAttachmentPoint);
                    break;
                } else {
                    attachmentPointMatched = false;
                }
            }
        }
        return attachmentPointMatched;
    }

    public static boolean isInPermittedAttachmentPoint(Device onuDevice, UnknownONU unknownONU, PmaRegistry pmaRegistry) {
        Set<ExpectedAttachmentPoint> expectedAttachmentPoints = onuDevice.getDeviceManagement().getOnuConfigInfo()
                .getExpectedAttachmentPoints().getExpectedAttachmentPointSet();
        boolean attachmentPointMatched = true;
        for (ExpectedAttachmentPoint expectedAttachmentPoint : expectedAttachmentPoints) {
            if (expectedAttachmentPoint.getOltName() != null && expectedAttachmentPoint.getChannelPartitionName() != null) {
                String actualChannelPartition = getChannelPartition(unknownONU.getOltDeviceName(),
                        unknownONU.getChannelTermRef(), pmaRegistry);
                if (expectedAttachmentPoint.getOltName().equals(unknownONU.getOltDeviceName())
                        && expectedAttachmentPoint.getChannelPartitionName().equals(actualChannelPartition)) {
                    attachmentPointMatched = true;
                    updateMatchedAttachmentPointMap(onuDevice.getDeviceName(), expectedAttachmentPoint);
                    break;
                } else {
                    attachmentPointMatched = false;
                }
            }
        }
        return attachmentPointMatched;
    }

    protected static void updateMatchedAttachmentPointMap(String onuDeviceName, ExpectedAttachmentPoint expectedAttachmentPoint) {
        synchronized (LOCK) {
            if (m_matchedExpectedAttachmentPoint.containsKey(onuDeviceName)) {
                m_matchedExpectedAttachmentPoint.replace(onuDeviceName, expectedAttachmentPoint);
            } else {
                m_matchedExpectedAttachmentPoint.put(onuDeviceName, expectedAttachmentPoint);
            }
        }
    }

    public static void removeMatchedAttachmentPointFromMap(String deviceName) {
        synchronized (LOCK) {
            m_matchedExpectedAttachmentPoint.remove(deviceName);
        }
    }

    private static String getChannelPartition(String oltDeviceName, String channelTermRef, PmaRegistry pmaRegistry) {
        String channelPartition = "";
        GetConfigRequest getConfig = new GetConfigRequest();
        getConfig.setSourceRunning();
        getConfig.setMessageId("internal");
        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.DEVICE,oltDeviceName);
        try {
            Map<NetConfResponse, List<Notification>> response = pmaRegistry.executeNC(resourceId, getConfig.requestToString());
            String xpathExpression;
            List<String> evaluatedXpath = new ArrayList<>();
            Document responseDocument = response.entrySet().iterator().next().getKey().getResponseDocument();
            xpathExpression = "//if:interfaces/if:interface[if:name='" + channelTermRef + "']/bbf-xpon:channel-termination/"
                    + "bbf-xpon:channel-pair-ref/text()";
            evaluatedXpath = evaluateXPath(responseDocument, xpathExpression);
            String channelPair = "";
            if (!evaluatedXpath.isEmpty()) {
                channelPair = evaluatedXpath.get(0);
                xpathExpression = "//if:interfaces/if:interface[if:name='" + channelPair + "']/bbf-xpon:channel-pair/"
                        + "bbf-xpon:channel-partition-ref/text()";
                evaluatedXpath = evaluateXPath(responseDocument, xpathExpression);
                if (!evaluatedXpath.isEmpty()) {
                    channelPartition = evaluatedXpath.get(0);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching channel partition corresponding to channelTermination: " + channelTermRef, e);
        }
        return channelPartition;
    }

    public static String generateRandomMessageId() {
        int randomNumber = (int) (Math.random() * (ONUConstants.MESSAGE_ID_MAX - ONUConstants.MESSAGE_ID_MIN + 1)
                + ONUConstants.MESSAGE_ID_MIN);
        return String.valueOf(randomNumber);
    }

    public static String processInternalGetResponseAndRetrieveHwProperties(String responseStatus, String getResponseData,
                                                                           String serialNumber) {
        String equipmentId = null;
        if (responseStatus.equals(ONUConstants.OK_RESPONSE) && !getResponseData.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(getResponseData).getJSONObject(IETF_HARDWARE_STATE_JSON_KEY)
                        .getJSONArray(COMPONENT_JSON_KEY).getJSONObject(0);
                String onuSerialNumber = jsonObject.getString(SERIAL_NUM_JSON_KEY);
                if (onuSerialNumber.equals(serialNumber)) {
                    equipmentId = jsonObject.getString(MODEL_NAME_JSON_KEY);
                }

            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        } else {
            LOGGER.info(String.format("Received %s response for internal get operation", ONUConstants.NOK_RESPONSE));
        }
        return equipmentId;
    }

    public static String processNotificationAndRetrieveOnuAlignmentStatus(String notificationData) {
        String alignmentStatus = null;
        if (!notificationData.isEmpty()) {
            try {
                JSONObject getResponseDataJson = new JSONObject(notificationData);
                alignmentStatus = getResponseDataJson.getJSONObject(ONUConstants.VOMCI_FUNC_ONU_ALIGNMENT_STATUS_JSON_KEY)
                        .optString(ONUConstants.ALIGNMENT_STATUS_JSON_KEY);
            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        } else {
            LOGGER.info("Received notification without information");
        }
        return alignmentStatus;
    }

    public static AlarmInfo processVomciNotificationAndPrepareAlarmInfo(String notificationData, String onuDeviceName) {
        String resource = null;
        String alarmTypeId = null;
        AlarmSeverity alarmSeverity = null;
        String alarmTypeQualifier = null;
        Timestamp time = null;
        String alarmText = null;
        String alarmSeverityString = null;

        if (!notificationData.isEmpty()) {
            try {
                JSONObject getResponseDataJson = new JSONObject(notificationData);
                JSONObject ietfAlarms = getResponseDataJson.getJSONObject(ONUConstants.IETF_ALARMS_ALARM_NOTIFICATION_JSON_KEY);
                resource = ietfAlarms.optString(ONUConstants.RESOURCE_JSON_KEY);
                alarmTypeId = ietfAlarms.optString(ONUConstants.ALARM_TYPE_ID_JSON_KEY);
                alarmTypeQualifier = ietfAlarms.optString(ONUConstants.ALARM_TYPE_QUALIFIER_JSON_KEY);
                time = convertStringToTimestamp(ietfAlarms.optString(ONUConstants.TIME_JSON_KEY));
                alarmSeverityString = ietfAlarms.optString(ONUConstants.PERCEIVED_SEVERITY_JSON_KEY);
                alarmText = ietfAlarms.optString(ONUConstants.ALARM_TEXT_JSON_KEY);
            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        }
        if (alarmSeverityString != null) {
            if (alarmSeverityString.contains(ONUConstants.CLEARED)) {
                alarmSeverity = AlarmSeverity.CLEAR;
            } else {
                alarmSeverity = AlarmSeverity.valueOf(alarmSeverityString.toUpperCase());
            }
        }
        return new AlarmInfo(updateAlarmTypeId(alarmTypeId), alarmTypeQualifier, updateAlarmResource(resource), time,
                alarmSeverity, alarmText, onuDeviceName);
    }

    private static String updateAlarmResource(String resource) {
        String updatedResource = null;
        if (resource != null && !resource.isEmpty()) {
            if (resource.contains(IETF_INTERFACES)) {
                String interfaceName = resource.split("name=")[1].split("]")[0];
                updatedResource = String.format(IETF_INTERFACES_RESOURCE_IDENTIFIER_FORMAT, interfaceName);
            } else if (resource.contains(IETF_HARDWARE)) {
                String componentName = resource.split("name=")[1].split("]")[0];
                updatedResource = String.format(IETF_HARDWARE_RESOURCE_IDENTIFIER_FORMAT, componentName);
            } else {
                LOGGER.error(String.format("Resource type %s is not supported currently", resource));
            }
        } else {
            LOGGER.info(String.format("Resource received is either empty or null"));
        }
        return updatedResource;
    }

    private static String updateAlarmTypeId(String alarmTypeId) {
        String updatedAlarmTypeId = null;
        if (alarmTypeId != null && !alarmTypeId.isEmpty()) {
            if (alarmTypeId.contains(BBF_XPON_ONU_ALARM_TYPE_MODULE_NAME)) {
                String moduleName = alarmTypeId.split(":")[0];
                updatedAlarmTypeId = alarmTypeId.replace(moduleName, XPON_ONU_ALARM_PREFIX);
            } else if (alarmTypeId.contains(BBF_ETH_ALARM_TYPE_MODULE_NAME)) {
                String moduleName = alarmTypeId.split(":")[0];
                updatedAlarmTypeId = alarmTypeId.replace(moduleName, ETH_ALARM_PREFIX);
            } else {
                //bbf-hardware-transceiver-alarm-types
                String moduleName = alarmTypeId.split(":")[0];
                updatedAlarmTypeId = alarmTypeId.replace(moduleName, HW_TX_ALARM_TYPE_PREFIX);
            }
        } else {
            LOGGER.info(String.format("Alarm type Id received is either empty or null"));
        }
        return updatedAlarmTypeId;
    }


    public static SchemaRegistry getDeviceSchemaRegistry(AdapterManager adapterManager, Device onuDevice) {
        SchemaRegistry onuSchemaRegistry = null;
        if (onuDevice.getDeviceManagement().getDeviceModel().equalsIgnoreCase(AdapterSpecificConstants.STANDARD)) {
            String stdAdapterKey = onuDevice.getDeviceManagement().getDeviceType().concat("-")
                    .concat(onuDevice.getDeviceManagement().getDeviceInterfaceVersion());
            onuSchemaRegistry = adapterManager.getStdAdapterContextRegistry()
                    .get(stdAdapterKey).getSchemaRegistry();
        } else {
            DeviceAdapterId onuAdapterId = new DeviceAdapterId(onuDevice.getDeviceManagement().getDeviceType(),
                    onuDevice.getDeviceManagement().getDeviceInterfaceVersion(), onuDevice.getDeviceManagement().getDeviceModel(),
                    onuDevice.getDeviceManagement().getDeviceVendor());
            onuSchemaRegistry = adapterManager.getAdapterContext(onuAdapterId).getSchemaRegistry();
        }

        return onuSchemaRegistry;
    }


    public static Element prepareAlarmNotificationElementFromAlarmInfo(AlarmInfo alarmInfo) {

        Document document = DocumentUtils.createDocument();

        Element alarmNotification = document.createElementNS(IETF_ALARM_NS, ALARM_NOTIFICATION);
        alarmNotification.setPrefix(IETF_ALARM_PREFIX);

        Element resource = document.createElementNS(IETF_ALARM_NS, RESOURCE_JSON_KEY);
        resource.setAttributeNS(XML_NS, NW_MGR_XMLNS_PREFIX, NS);
        if (alarmInfo.getSourceObjectString().contains(INTERFACES)) {
            resource.setAttributeNS(XML_NS, IETF_INTERFACE_XMLNS_PREFIX, IETF_INTERFACES_NS);
        } else {
            resource.setAttributeNS(XML_NS, HW_XMLNS_PREFIX, IETF_HW_NS);
        }
        resource.setTextContent(alarmInfo.getSourceObjectString());
        resource.setPrefix(IETF_ALARM_PREFIX);


        Element alarmTypeId = document.createElementNS(IETF_ALARM_NS, ALARM_TYPE_ID_JSON_KEY);
        if (alarmInfo.getAlarmTypeId().contains(ETH_ALARM_PREFIX)) {
            alarmTypeId.setAttributeNS(XML_NS, ETH_ALARM_XMLNS_PREFIX, ETH_ALARM_TYPE_NS);
        } else if (alarmInfo.getAlarmTypeId().contains(XPON_ONU_ALARM_PREFIX)) {
            alarmTypeId.setAttributeNS(XML_NS, XPON_ONU_ALARM_XMLNS_PREFIX, XPON_ONU_ALARM_TYPE_NS);
        } else {
            alarmTypeId.setAttributeNS(XML_NS, HW_TX_ALARM_TYPE_XMLNS_PREFIX, BBF_HW_TX_ALARM_TYPE_NS);
        }
        alarmTypeId.setPrefix(IETF_ALARM_PREFIX);
        alarmTypeId.setTextContent(alarmInfo.getAlarmTypeId());

        Element alarmTypeQualifier = document.createElementNS(IETF_ALARM_NS, ALARM_TYPE_QUALIFIER_JSON_KEY);
        alarmTypeQualifier.setPrefix(IETF_ALARM_PREFIX);
        alarmTypeQualifier.setTextContent(alarmInfo.getAlarmTypeQualifier());

        Element time = document.createElementNS(IETF_ALARM_NS, TIME_JSON_KEY);
        time.setPrefix(IETF_ALARM_PREFIX);
        time.setTextContent(String.valueOf(alarmInfo.getTime()));

        Element ps = document.createElementNS(IETF_ALARM_NS, PERCEIVED_SEVERITY_JSON_KEY);
        ps.setPrefix(IETF_ALARM_PREFIX);
        ps.setTextContent(String.valueOf(alarmInfo.getSeverity()));

        Element alarmText = document.createElementNS(IETF_ALARM_NS, ALARM_TEXT_JSON_KEY);
        alarmText.setPrefix(IETF_ALARM_PREFIX);
        alarmText.setTextContent(alarmInfo.getAlarmText());


        alarmNotification.appendChild(resource);
        alarmNotification.appendChild(alarmTypeId);
        alarmNotification.appendChild(alarmTypeQualifier);
        alarmNotification.appendChild(time);
        alarmNotification.appendChild(ps);
        alarmNotification.appendChild(alarmText);

        document.appendChild(alarmNotification);
        return document.getDocumentElement();
    }

    public static AlarmInfo updateDeviceInstanceIdentifierInAlarmInfo(AlarmInfo alarmInfo, String onuDeviceName) {
        String resource = alarmInfo.getSourceObjectString();
        resource = String.format(DEVICE_INSTANCE_IDENTIFIER_FORMAT, onuDeviceName).concat(resource);
        alarmInfo.setSourceObjectString(resource);
        return alarmInfo;
    }

    public static Set<SoftwareImage> processInternalGetResponseAndRetrieveSWProperties(String responseStatus, String getResponseData) {
        Set<SoftwareImage> softwareImageSet = new HashSet<>();
        if (responseStatus.equals(ONUConstants.OK_RESPONSE) && !getResponseData.isEmpty()) {
            try {
                JSONArray swImageJsonArray = new JSONObject(getResponseData).getJSONObject(IETF_HARDWARE_JSON_KEY)
                        .getJSONArray(COMPONENT_JSON_KEY).getJSONObject(0).getJSONObject(BBF_SW_MGMT_SOFTWARE_JSON_KEY)
                        .getJSONArray(SOFTWARE_JSON_KEY).getJSONObject(0).getJSONObject(REVISIONS_JSON_KEY)
                        .getJSONArray(REVISION_JSON_KEY);
                for (int j = 0; j < swImageJsonArray.length(); j++) {

                    JSONObject jsonObject = swImageJsonArray.optJSONObject(j);
                    SoftwareImage softwareImage = new SoftwareImage();

                    for (String key : jsonObject.keySet()) {
                        switch (key) {
                            case ONUConstants.SOFTWARE_IMAGE_ID_JSON_KEY:
                                softwareImage.setId(jsonObject.optInt(ONUConstants.SOFTWARE_IMAGE_ID_JSON_KEY));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_VERSION_JSON_KEY:
                                softwareImage.setVersion(jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_VERSION_JSON_KEY));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_PRODUCT_CODE_JSON_KEY:
                                softwareImage.setProductCode(jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_PRODUCT_CODE_JSON_KEY));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_IS_ACTIVE_JSON_KEY:
                                String isActive = jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_IS_ACTIVE_JSON_KEY);
                                softwareImage.setIsActive(Boolean.parseBoolean(isActive));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_IS_COMMITTED_JSON_KEY:
                                String isCommitted = jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_IS_COMMITTED_JSON_KEY);
                                softwareImage.setIsCommitted(Boolean.parseBoolean(isCommitted));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_IS_VALID_JSON_KEY:
                                String isValid = jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_IS_VALID_JSON_KEY);
                                softwareImage.setIsValid(Boolean.parseBoolean(isValid));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_HASH_JSON_KEY:
                                softwareImage.setHash(jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_HASH_JSON_KEY));
                                break;
                            default:
                                LOGGER.warn(String.format("improper response received from vOMCI, failed to to update software details "
                                        + "for the device. response is %s", getResponseData));

                        }
                    }

                    softwareImageSet.add(softwareImage);
                }
            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        } else {
            if (responseStatus.equals(ONUConstants.NOK_RESPONSE)) {
                LOGGER.error(String.format("Received %s response for internal get operation", ONUConstants.NOK_RESPONSE));
            }
            if (getResponseData.isEmpty() || getResponseData == null) {
                LOGGER.error(String.format("Response received from vOMCI is either null or empty :%s", getResponseData));

            }
        }
        return softwareImageSet;
    }

    public static UnknownONU buildUnknownOnu(String oltDeviceName, ONUNotification onuNotification) {
        UnknownONU onuEntity = new UnknownONU();
        onuEntity.setOltDeviceName(oltDeviceName);
        if ((onuNotification.getRegId() != null) && !onuNotification.getRegId().isEmpty()) {
            onuEntity.setRegistrationId(onuNotification.getRegId());
        }
        if ((onuNotification.getSerialNo() != null) && !onuNotification.getSerialNo().isEmpty()) {
            onuEntity.setSerialNumber(onuNotification.getSerialNo());
        }
        if ((onuNotification.getVAniRef() != null) && !onuNotification.getVAniRef().isEmpty()) {
            onuEntity.setVAniRef(onuNotification.getVAniRef());
        }
        if ((onuNotification.getOnuState() != null) && !onuNotification.getOnuState().isEmpty()) {
            onuEntity.setOnuState(onuNotification.getOnuState());
        }
        if ((onuNotification.getOnuId() != null) && !onuNotification.getOnuId().isEmpty()) {
            onuEntity.setOnuID(onuNotification.getOnuId());
        }
        if ((onuNotification.getChannelTermRef() != null) && !onuNotification.getChannelTermRef().isEmpty()) {
            onuEntity.setChannelTermRef(onuNotification.getChannelTermRef());
        }
        if ((onuNotification.getOnuStateLastChanged() != null) && !onuNotification.getOnuStateLastChanged().isEmpty()) {
            onuEntity.setOnuStateLastChange(convertStringToTimestamp(onuNotification.getOnuStateLastChanged()));
        }
        if ((onuNotification.getDeterminedOnuManagementMode() != null) && !onuNotification.getDeterminedOnuManagementMode().isEmpty()) {
            onuEntity.setDeterminedOnuManagementMode(onuNotification.getDeterminedOnuManagementMode());
        }
        if ((onuNotification.getDetectedLoId() != null) && !onuNotification.getDetectedLoId().isEmpty()) {
            onuEntity.setDetectedLoId(onuNotification.getDetectedLoId());
        }
        return onuEntity;
    }

    private static Timestamp convertStringToTimestamp(String timestampString) {
        if (timestampString != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            try {
                Date parsedDate = dateFormat.parse(timestampString);
                Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                return timestamp;
            } catch (ParseException e) {
                LOGGER.error("Field is not in the correct format for timestamp : " + timestampString);
            }
        }
        return null;
    }

    public static boolean isResponseOK(ArrayList<Boolean> respArray) {
        return (respArray == null || respArray.isEmpty() || respArray.contains(false)) ? false : true;
    }

    public static void updateOnuStateInfoInDevice(Device device, UnknownONU unknownONU, DeviceManager deviceManager, TxService txService) {
        ActualAttachmentPoint actualAttachmentPoint = new ActualAttachmentPoint();
        actualAttachmentPoint.setChannelTerminationRef(unknownONU.getChannelTermRef());
        actualAttachmentPoint.setOltName(unknownONU.getOltDeviceName());
        actualAttachmentPoint.setOnuId(unknownONU.getOnuId());
        OnuStateInfo onuStateInfo = new OnuStateInfo();
        onuStateInfo.setOnuState(unknownONU.getOnuState());
        onuStateInfo.setActualAttachmentPoint(actualAttachmentPoint);
        onuStateInfo.setEquipmentId(unknownONU.getEquipmentId());
        onuStateInfo.setOnuStateInfoId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        onuStateInfo.setDetermineOnuManagementMode(unknownONU.getDeterminedOnuManagementMode());
        actualAttachmentPoint.setAttachmentPointId(onuStateInfo.getOnuStateInfoId());
        SoftwareImages softwareImages = new SoftwareImages();
        /* below commented lines will be uncommented/enabled after vomci has the get request & response handling support */
        /* Set<SoftwareImage> softwareImageSet = unknownONU.getSoftwareImages().getSoftwareImage();
        for (SoftwareImage softwareImage : softwareImageSet) {
            softwareImage.setParentId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        }*/
        Set<SoftwareImage> softwareImageSet = Collections.emptySet(); // this line to be removed after uncommenting above lines
        softwareImages.setSoftwareImagesId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        softwareImages.setSoftwareImage(softwareImageSet);
        onuStateInfo.setSoftwareImages(softwareImages);
        txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
            deviceManager.updateOnuStateInfo(device.getDeviceName(), onuStateInfo);
            return null;
        });
    }


    public static void updateKafkaSubscriptions(String vomciFunctionName, MessageFormatter messageFormatter, NetworkFunctionDao
            networkFunctionDao, OnuKafkaConsumer onuKafkaConsumer, Map<String, Set<String>> kafkaConsumerTopicMap) {
        if (messageFormatter instanceof GpbFormatter) {
            Set<KafkaTopic> kafkaTopicSet = networkFunctionDao.getKafkaConsumerTopics(vomciFunctionName);
            Set<String> kafkaTopicNameSet = new HashSet<>();
            if (kafkaTopicSet != null && !kafkaTopicSet.isEmpty()) {
                onuKafkaConsumer.updateSubscriberTopics(kafkaTopicSet);
                for (KafkaTopic kafkaTopic : kafkaTopicSet) {
                    kafkaTopicNameSet.add(kafkaTopic.getTopicName());
                }
                kafkaConsumerTopicMap.put(vomciFunctionName, kafkaTopicNameSet);
            }
        }
    }

    public static void removeSubscriptions(String networkFunctionName, OnuKafkaConsumer onuKafkaConsumer,
                                           Map<String, Set<String>> kafkaConsumerTopicMap) {
        if (kafkaConsumerTopicMap.containsKey(networkFunctionName)) {
            Set<String> kafkaTopicNameSet = kafkaConsumerTopicMap.get(networkFunctionName);
            Set<String> topicsToUnsubscribe = new HashSet<>(kafkaTopicNameSet);
            kafkaConsumerTopicMap.remove(networkFunctionName);
            for (String kafkaTopicName : kafkaTopicNameSet) {
                for (Set<String> kafkaTopicNames : kafkaConsumerTopicMap.values()) {
                    if (kafkaTopicNames.contains(kafkaTopicName)) {
                        topicsToUnsubscribe.remove(kafkaTopicName);
                        break;
                    }
                }
            }

            if (!topicsToUnsubscribe.isEmpty()) {
                LOGGER.info("Unsubscribing topics: " + topicsToUnsubscribe);
                onuKafkaConsumer.removeSubscriberTopics(topicsToUnsubscribe);
            }
        }
    }

    public static MediatedDeviceNetconfSession getMediatedDeviceNetconfSession(Device onuDevice,
                                                                               NetconfConnectionManager netconfConnectionManager) {
        if (onuDevice == null) {
            return null;
        }
        NetconfClientSession devSession = netconfConnectionManager.getMediatedDeviceSession(onuDevice.getDeviceName());
        if (devSession != null) {
            return (MediatedDeviceNetconfSession) devSession;
        }
        return null;
    }

    public static MediatedNetworkFunctionNetconfSession getMediatedNetworkFunctionNetconfSession(String networkFunctionName,
                                                                                    NetconfConnectionManager netconfConnectionManager) {
        if (networkFunctionName != null) {
            NetconfClientSession devSession = netconfConnectionManager.getMediatedNetworkFunctionSession(
                    networkFunctionName);
            if (devSession != null) {
                return (MediatedNetworkFunctionNetconfSession) devSession;
            }
        }
        return null;
    }

    public static void persistUnknownOnuToDB(String oltDeviceName, ONUNotification onuNotification, TxService txService,
                                             UnknownONUHandler unknownONUHandler) {
        if (onuNotification != null) {
            UnknownONU onuEntity = buildUnknownOnu(oltDeviceName, onuNotification);
            LOGGER.info("Persisting unknown onu to DB: " + onuEntity);
            txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                UnknownONU matchedOnu = unknownONUHandler.findUnknownOnuEntity(onuEntity.getSerialNumber(),
                        onuEntity.getRegistrationId());
                if (matchedOnu != null) {
                    LOGGER.info("Found existing unknown ONU, updating it");
                    matchedOnu.setChannelTermRef(onuEntity.getChannelTermRef());
                    matchedOnu.setOnuID(onuEntity.getOnuId());
                    matchedOnu.setVAniRef(onuEntity.getVAniRef());
                    matchedOnu.setOnuState(onuEntity.getOnuState());
                    matchedOnu.setOltDeviceName(onuEntity.getOltDeviceName());
                    matchedOnu.setOnuStateLastChange(onuEntity.getOnuStateLastChange());
                    matchedOnu.setDeterminedOnuManagementMode(onuEntity.getDeterminedOnuManagementMode());
                    matchedOnu.setDetectedLoId(onuEntity.getDetectedLoId());
                } else {
                    unknownONUHandler.createUnknownONUEntity(onuEntity);
                }
                return null;
            });
        }
    }

    private static void sendInternalGetRequestOnKafka(NotificationRequest request, MessageFormatter messageFormatter, TxService txService,
                                                      DeviceDao deviceDao, NetworkFunctionDao networkFunctionDao,
                                                      OnuKafkaProducer onuKafkaProducer) {
        String kafkaTopicName = null;
        Object notification = null;
        AtomicReference<HashSet<String>> kafkaTopicNameSet = new AtomicReference<>(new HashSet<String>());
        request.setMessageId(ONUConstants.DEFAULT_MESSAGE_ID);
        NetworkWideTag networkWideTag = new NetworkWideTag(request.getOnuDeviceName(), request.getOltDeviceName(),
                request.getOnuId(), request.getChannelTermRef(), request.getLabels());
        if (messageFormatter instanceof JsonFormatter) {
            kafkaTopicName = ONUConstants.ONU_REQUEST_KAFKA_TOPIC;
        } else {
            txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                String networkFunctionName = deviceDao.getVomciFunctionName(request.getOnuDeviceName());
                final HashSet<String> kafkaTopicNameFinal = networkFunctionDao.getKafkaTopicNames(networkFunctionName,
                        KafkaTopicPurpose.VOMCI_REQUEST);
                if (kafkaTopicNameFinal != null && !kafkaTopicNameFinal.isEmpty()) {
                    kafkaTopicNameSet.set(kafkaTopicNameFinal);
                } else {
                    LOGGER.error(String.format("Topic Name for the Network Function %s and Topic Purpose %s was not found",
                            networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST));
                }
                return null;
            });
        }
        try {
            notification = messageFormatter.getFormattedRequest(request, ONUConstants.ONU_GET_OPERATION, null,
                    null, null, null, networkWideTag);
            if (messageFormatter instanceof JsonFormatter) {
                onuKafkaProducer.sendNotification(kafkaTopicName, notification);
            } else {
                if (!kafkaTopicNameSet.get().isEmpty()) {
                    for (String topicName : kafkaTopicNameSet.get()) {
                        onuKafkaProducer.sendNotification(topicName, notification);
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.error(String.format("Failed to send %s notification onto Kafka topic %s ", notification, kafkaTopicName), exception);
        }
    }

    public static ResponseData updateOperationTypeInResponseData(ResponseData responseData, MessageFormatter messageFormatter) {
        if (messageFormatter instanceof GpbFormatter) {
            String identifier = responseData.getIdentifier();
            if (responseData.getOperationType().equals(NetconfResources.RPC)
                    || responseData.getOperationType().equals(NetconfResources.ACTION)
                    || responseData.getOperationType().equals(NetconfResources.COPY_CONFIG)
                    || responseData.getOperationType().equals(NetconfResources.EDIT_CONFIG)) {
                if (m_requestMap.get(identifier) != null) {
                    String operationType = m_requestMap.get(identifier).getSecond();
                    String onuName = m_requestMap.get(identifier).getFirst();
                    if (operationType != null) {
                        responseData.setOperationType(operationType);
                    }
                    if (onuName != null) {
                        responseData.setOnuName(onuName);
                    }
                } else {
                    LOGGER.error(String.format("Request correlating the response id %s received for %s was not found in requestMap.",
                            identifier, responseData));
                }
            }
        }
        return responseData;
    }

    public static void retrieveAndUpdateHwSwPropertiesForUnknownONU(ResponseData responseData, TxService txService,
                                                                    UnknownONUHandler unknownONUHandler) {
        String responseStatus = responseData.getResponseStatus();
        String getResponseData = responseData.getResponsePayload();
        String oltName = responseData.getOltName();
        String channelTermRef = responseData.getChannelTermRef();
        String onuId = responseData.getOnuId();

        txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            UnknownONU matchedUnknownONU = unknownONUHandler.findMatchingUnknownOnu(oltName, channelTermRef, onuId);
            if (matchedUnknownONU != null) {
                String eqptId = processInternalGetResponseAndRetrieveHwProperties(responseStatus, getResponseData,
                        matchedUnknownONU.getSerialNumber());
                matchedUnknownONU.setEquipmentId(eqptId);
                Set<SoftwareImage> softwareImageSet = processInternalGetResponseAndRetrieveSWProperties(responseStatus,
                        getResponseData);

                SoftwareImages softwareImages = new SoftwareImages();
                for (SoftwareImage softwareImage : softwareImageSet) {
                    softwareImage.setParentId(ONU_NAME_PREFIX.concat(matchedUnknownONU.getSerialNumber()));
                }
                softwareImages.setSoftwareImagesId(ONU_NAME_PREFIX.concat(matchedUnknownONU.getSerialNumber()));
                softwareImages.setSoftwareImage(softwareImageSet);
                matchedUnknownONU.setSoftwareImages(softwareImages);
            }
            return null;
        });

    }

    public static void sendOnuDiscoveryResultNotification(Device onuDevice, String onuState, NotificationService notificationService) {
        try {
            Notification notification = new OnuDiscoveryResultNotification(onuDevice, onuState);
            notificationService.sendNotification(NetconfResources.STATE_CHANGE, notification);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(String.format("Error while sending onu discovery result notification for device %s with onu state %s",
                    onuDevice.getDeviceName(), onuState), e);
        }
    }

    public static void sendOnuAuthenticationResultNotification(Device onuDevice, ONUNotification onuNotification,
                                                               NetconfConnectionManager netconfConnectionManager,
                                                               NotificationService notificationService, String onuAuthStatus,
                                                               DeviceManager deviceManager, TxService txService) {
        Notification notification = null;
        if (onuDevice != null) {
            notification = new OnuAuthenticationReportNotification(onuDevice, null, netconfConnectionManager, onuAuthStatus,
                    deviceManager, txService, null);
            LOGGER.info("Sending onu-authetication-report notification to SDN M&C : " + notification.notificationToString());
            notificationService.sendNotification(NetconfResources.NETCONF, notification);
        } else {
            String vaniName = onuNotification.getVAniRef();
            String oltName = onuNotification.getOltDeviceName();
            Device oltDevice = txService.executeWithTxRequired(() -> deviceManager.getDevice(oltName));
            Future<NetConfResponse> netConfResponseFuture = VOLTManagementUtil.prepareGetRequestForVaniAndExecute(oltDevice,
                    vaniName, netconfConnectionManager);

            String messageId = m_responseFutureAndMessageIdMap.get(netConfResponseFuture);
            final NetConfResponse[] netConfResponse = {null};
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        netConfResponse[0] = netConfResponseFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.info(String.format("Exception while getting netconf response"));
                    }
                    Notification finalNotification = new OnuAuthenticationReportNotification(null,
                            onuNotification, netconfConnectionManager, onuAuthStatus, deviceManager, txService,
                            getOnuNameFromResponseFuture(netConfResponse[0], messageId));
                    LOGGER.info("Sending onu-authetication-report notification to SDN M&C : " + finalNotification.notificationToString());
                    notificationService.sendNotification(NetconfResources.NETCONF, finalNotification);
                    return;
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public static List<Pair<ObjectType, String>> getManagementChain(String onuDeviceName, TxService txService, DeviceDao deviceDao) {
        AtomicReference<ArrayList<String>> networkFunctionNamesList = new AtomicReference<ArrayList<String>>();
        txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            OnuManagementChain[] chains = deviceDao.getOnuManagementChains(onuDeviceName);
            ArrayList<String> nwFnNames = new ArrayList<>();
            if (chains.length < 2) {
                LOGGER.warn(String.format("Onu management chain should atleast have two values denoting vOMCI function and OLT."
                        + " ONU device %s has only %d management values", onuDeviceName, chains.length));
            } else {
                for (int i = 0; i < chains.length - 1; i++) {
                    nwFnNames.add(chains[i].getNfName());
                }
                networkFunctionNamesList.set(nwFnNames);
            }
            return null;
        });
        ArrayList<String> networkFunctionNames = networkFunctionNamesList.get();
        if (networkFunctionNames == null || networkFunctionNames.isEmpty()) {
            LOGGER.info("Trying to fetch networkFunctionNames from local map");
            networkFunctionNames = m_networkFunctionMap.get(onuDeviceName);
            m_networkFunctionMap.remove(onuDeviceName);
        }
        List<Pair<ObjectType, String>> networkFunctionList = new ArrayList<>();
        if (networkFunctionNames != null && !networkFunctionNames.isEmpty()) {
            m_networkFunctionMap.put(onuDeviceName, networkFunctionNames);
            for (int i = 0; i < networkFunctionNames.size(); i++) {
                ObjectType objectType;
                if (i == 0) {
                    objectType = ObjectType.VOMCI_FUNCTION;
                } else {
                    objectType = ObjectType.VOMCI_PROXY;
                }
                networkFunctionList.add(new Pair<ObjectType, String>(objectType, networkFunctionNames.get(i)));
            }
        } else {
            LOGGER.error("No network functions found for ONU device " + onuDeviceName);
        }
        return networkFunctionList;
    }

    public static void sendKafkaNotification(NotificationRequest request, MessageFormatter messageFormatter,
                                             OnuKafkaProducer onuKafkaProducer) {
        String kafkaTopicName = null;
        String messageId = VOLTManagementUtil.generateRandomMessageId();
        request.setMessageId(messageId);
        NetworkWideTag networkWideTag = new NetworkWideTag(request.getOnuDeviceName(), request.getOltDeviceName(),
                request.getOnuId(), request.getChannelTermRef(), request.getLabels());
        Object notification = null;
        kafkaTopicName = ONUConstants.ONU_NOTIFICATION_KAFKA_TOPIC;
        try {
            notification = messageFormatter.getFormattedRequest(request, request.getEvent(), null,
                    null, null, null, networkWideTag);
            onuKafkaProducer.sendNotification(kafkaTopicName, notification);
        } catch (Exception exception) {
            LOGGER.error(String.format("Failed to send %s notification onto Kafka topic %s ", notification, kafkaTopicName), exception);
        }
    }

    public static Future<NetConfResponse> sendOnuAuthenticationActionRequest(Device oltDevice, NetconfConnectionManager
                    netconfConnectionManager, ActionRequest actionRequest) throws InterruptedException {
        actionRequest.setMessageId(generateRandomMessageId());
        Future<NetConfResponse> netConfResponseFuture = null;
        try {
            netConfResponseFuture = netconfConnectionManager.executeNetconf(oltDevice, actionRequest);
        } catch (ExecutionException e) {
            LOGGER.error(String.format("Failed to execute Netconf request:", e));
        }
        return netConfResponseFuture;
    }

    public static String getErrorAppTagFromActionResponse(NetConfResponse response) {
        String actionResponse = null;
        if (response != null) {
            if (response.isOk()) {
                actionResponse = ONUConstants.OK_RESPONSE;
                LOGGER.info(String.format("Action ONU Authentication Report is success and no erros found"));
            } else {
                if (response.getErrors().get(0).getErrorAppTag() != null) {
                    actionResponse = response.getErrors().get(0).getErrorAppTag();
                } else {
                    /*Workaround added for pOLT simulator restriction, OBBAA-498
                    at the moment none of the released sysrepo versions supports setting error-app-tag,
                    hence we are parsing the error-message from polt simulator when error-app-tag is returned as null. */

                    actionResponse = response.getErrors().get(0).getErrorMessage();
                }
            }
        }
        return actionResponse;
    }


    public static void registerInRequestMap(AbstractNetconfRequest request, String deviceName, String operation) {
        synchronized (LOCK) {
            m_requestMap.put(request.getMessageId(), new Pair<String, String>(deviceName, operation));
        }
    }

    public static void removeRequestFromMap(String identifier) {
        synchronized (LOCK) {
            m_requestMap.remove(identifier);
        }
    }

    public static boolean isVomciToBeUsed(Device onuDevice) {
        boolean isVomci = true; //default is vOMCI=true in the YANG
        String plannedOnuMgmtModeLocal = null;
        String plannedOnuMgmtModeGlobal = null;
        Set<ExpectedAttachmentPoint> expectedAttachmentPointSet = new HashSet<>();

        plannedOnuMgmtModeGlobal = onuDevice.getDeviceManagement().getOnuConfigInfo().getPlannedOnuManagementMode();
        LOGGER.info(String.format("Global mode for ONU Mgmt is %s ", plannedOnuMgmtModeGlobal));

        if (onuDevice.getDeviceManagement() != null) {
            ExpectedAttachmentPoints expectedAttachmentPoints = onuDevice.getDeviceManagement().getOnuConfigInfo()
                    .getExpectedAttachmentPoints();

            if (expectedAttachmentPoints != null) {
                expectedAttachmentPointSet = expectedAttachmentPoints.getExpectedAttachmentPointSet();
            } else {
                LOGGER.info(String.format("Expected Attachment Points is null or not configured for the ONU"));
            }
        }

        if (expectedAttachmentPointSet != null && !expectedAttachmentPointSet.isEmpty()) {
            for (ExpectedAttachmentPoint expectedAttachmentPoint : expectedAttachmentPointSet) {
                if (m_matchedExpectedAttachmentPoint.containsKey(onuDevice.getDeviceName())) {
                    ExpectedAttachmentPoint matchedAttachmentPoint = m_matchedExpectedAttachmentPoint.get(onuDevice.getDeviceName());
                    if (matchedAttachmentPoint != null && expectedAttachmentPoint != null) {
                        if (matchedAttachmentPoint.getOltName().equals(expectedAttachmentPoint.getOltName()) && matchedAttachmentPoint
                                .getChannelPartitionName().equals(expectedAttachmentPoint.getChannelPartitionName())) {
                            plannedOnuMgmtModeLocal = expectedAttachmentPoint.getPlannedOnuManagementModeInThisOlt();
                            LOGGER.info(String.format("local mode fetched from eAP for ONU Mgmt is %s ", plannedOnuMgmtModeLocal));
                            break;
                        }
                    }
                }
            }
        }

        //if defined, the mode for the current attachment point takes precedence over the global
        if (plannedOnuMgmtModeLocal != null && !plannedOnuMgmtModeLocal.isEmpty()) {
            LOGGER.info(String.format("Planned ONU management mode(Local) is not null, use local value %s", plannedOnuMgmtModeLocal));
            if (plannedOnuMgmtModeLocal.equals(ONUConstants.USE_VOMCI)) {
                isVomci = true;
            } else if (plannedOnuMgmtModeLocal.equals(ONUConstants.USE_EOMCI)) {
                isVomci = false;
            }
        } else {
            //use the global mode as default when planned-onu-management-in-this-olt is null or not defined;
            LOGGER.info(String.format("Planned ONU management mode(Local) is null, using value of global "
                    + "planned-onu-management-mode: %s", plannedOnuMgmtModeGlobal));
            if (plannedOnuMgmtModeGlobal != null) {
                if (plannedOnuMgmtModeGlobal.equals(ONUConstants.USE_VOMCI)) {
                    isVomci = true;
                } else if (plannedOnuMgmtModeGlobal.equals(ONUConstants.USE_EOMCI)) {
                    isVomci = false;
                }
            }
        }

        return isVomci;
    }

    public static boolean isOnuAuthenticatedByOLTAndHasNoErrors(String onuState) {
        Boolean onuAuthenticatedAndHasNoError = false;
        if (onuState != null && !onuState.isEmpty()) {
            if (onuState.equals(ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION)) {
                // ONU is authenticated by OLT and has no Error
                onuAuthenticatedAndHasNoError = true;
            } else if (onuState.equals(ONU_PRESENT_AND_V_ANI_KNOW_BUT_INTENDED_CT_UNKNOWN)
                    || onuState.equals(ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_NO_ONU_ID)
                    || onuState.equals(ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_UNDEFINED)) {
                // ONU is authnticated by OLT, but ONU is unservicable
                onuAuthenticatedAndHasNoError = false;
            }
        }
        return onuAuthenticatedAndHasNoError;
    }

    public static String getChannelPartitionName(Device onuDevice, PmaRegistry pmaRegistry) {
        OnuStateInfo onuStateInfo = onuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo();
        String channelPartitionName = "";
        if (onuStateInfo != null) {
            String oltName = onuStateInfo.getActualAttachmentPoint().getOltName();
            String channelTermRef = onuStateInfo.getActualAttachmentPoint()
                    .getChannelTerminationRef();

            if (oltName != null && channelTermRef != null) {
                channelPartitionName = getChannelPartition(oltName, channelTermRef, pmaRegistry);
            }
        }
        return channelPartitionName;
    }

    public static void setMessageId(AbstractNetconfRequest request, AtomicLong messageId) {
        final String currentMessageId = String.valueOf(messageId.addAndGet(1));
        request.setMessageId(currentMessageId);
    }

    public static void removeActualAttachmentPointInfo(Device onuDevice, TxService txService, DeviceManager deviceManager) {
        txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
            deviceManager.updateOnuStateInfo(onuDevice.getDeviceName(), null);
            return null;
        });
    }

    public static void sendGetRequest(ONUNotification onuNotification, MessageFormatter messageFormatter, TxService txService,
                                      DeviceDao deviceDao, NetworkFunctionDao networkFunctionDao,
                                      OnuKafkaProducer onuKafkaProducer) {
        String serialNumber = onuNotification.getSerialNo();
        String vendor = serialNumber.substring(0, 4);
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", vendor);
        NotificationRequest request = new NotificationRequest(null, onuNotification.getOltDeviceName(), onuNotification.getChannelTermRef(),
                onuNotification.getOnuId(), ONUConstants.REQUEST_EVENT, labels);
        LOGGER.info(String.format("Sending internal get request to UnknownONU [olt-name:%s, channel-termination-ref:%s, onu-id:%s] ",
                onuNotification.getOltDeviceName(), onuNotification.getChannelTermRef(), onuNotification.getOnuId()));
        sendInternalGetRequestOnKafka(request, messageFormatter, txService, deviceDao, networkFunctionDao,
                onuKafkaProducer);
    }

    private static Future<NetConfResponse> prepareGetRequestForVaniAndExecute(Device oltDevice, String vaniName,
                                                                              NetconfConnectionManager connectionManager) {
        Future<NetConfResponse> netConfResponseFuture = null;
        GetRequest request = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(oltDevice.getDeviceName(), vaniName);
        setMessageId(request, MESSAGE_ID);
        try {
            netConfResponseFuture = connectionManager.executeNetconf(oltDevice, request);
        } catch (ExecutionException e) {
            LOGGER.error(String.format("Failed to execute Get request:", e));
        }
        m_responseFutureAndMessageIdMap.put(netConfResponseFuture, request.getMessageId());
        return netConfResponseFuture;
    }

    private static String getOnuNameFromResponseFuture(NetConfResponse netConfResponse, String messageId) {
        String onuName = null;
        if (netConfResponse != null && netConfResponse.getMessageId().equals(messageId)) {
            NodeList onuNameList = netConfResponse.getData().getElementsByTagName(ONU_NAME_TAG);
            if (onuNameList.item(0) != null) {
                onuName = onuNameList.item(0).getFirstChild().getNodeValue();
            }
        }
        return onuName;
    }

    public static void sendKafkaMessage(Object kafkaMessage, String networkFunctionName,
                                        TxService txService, NetworkFunctionDao networkFunctionDao,
                                        OnuKafkaProducer onuKafkaProducer) {

        AtomicReference<HashSet<String>> kafkaTopicNames = new AtomicReference<>(new HashSet<String>());
        txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            final HashSet<String> kafkaTopicNamesFinal = networkFunctionDao.getKafkaTopicNames(networkFunctionName,
                    KafkaTopicPurpose.VOMCI_REQUEST);
            if (kafkaTopicNamesFinal != null && !kafkaTopicNamesFinal.isEmpty()) {
                kafkaTopicNames.set(kafkaTopicNamesFinal);
            } else {
                LOGGER.error(String.format("Topic Name for the Network Function %s and Topic Purpose %s was not found",
                        networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST));
            }
            return null;
        });
        if (!kafkaTopicNames.get().isEmpty()) {
            for (String topicName : kafkaTopicNames.get()) {
                try {
                    onuKafkaProducer.sendNotification(topicName, kafkaMessage);
                } catch (MessageFormatterException e) {
                    LOGGER.error(String.format("Failed to send kafka message: %s to topic :%s", kafkaMessage.toString(), topicName, e));
                }
            }
        }
    }
}
