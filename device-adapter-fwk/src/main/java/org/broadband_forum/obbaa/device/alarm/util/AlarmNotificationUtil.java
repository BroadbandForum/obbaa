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

package org.broadband_forum.obbaa.device.alarm.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmProcessingException;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class AlarmNotificationUtil {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AlarmNotificationUtil.class,
            LogAppNames.NETCONF_ALARM);

    private static final String ALARM_TYPE_ID_TAG = AlarmConstants.ALARM_TYPE_ID;
    private static final String ALARMLIST_TAG = "alarm-list";
    private static final String RESOURCE_TAG = "resource";
    private static final String DATE_TIME_FORMAT1 = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_TIME_FORMAT2 = "yyyy-MM-dd HH:mm:ss";
    private static final String COLON = ":";
    private static final String OPEN_PARANTHESIS = "(";
    private static final String CLOSE_PARANTHESIS = ")";
    private static final String OPEN_CURLY_BRACES = "{";
    private static final String CLOSE_CURLY_BRACES = "}";
    private static final String QUESTION_MARK = "?";
    private static final String REVISION = "revision=";

    private AlarmNotificationUtil() {
    }

    private static AlarmInfo getAlarmInfoFromAlarmElement(SchemaRegistry schemaRegistry, Device device, Element nodeEle,
                                                          String modifiedIETFAlarmNs) {
        QName modifiedLastPerceivedSeverityQName = QName.create(modifiedIETFAlarmNs, AlarmConstants.ALARM_PERCEIVED_SEVERITY);
        QName modifiedLastStatusChangeQName = QName.create(modifiedIETFAlarmNs, AlarmConstants.TIME);
        QName modifiedLastAlarmTextQName = QName.create(modifiedIETFAlarmNs, AlarmConstants.ALARM_TEXT);
        return createAlarmInfo(schemaRegistry, device, nodeEle, modifiedIETFAlarmNs, modifiedLastPerceivedSeverityQName,
                modifiedLastStatusChangeQName, modifiedLastAlarmTextQName);
    }

    private static AlarmInfo getAlarmInfoFromAlarmNotificationElement(SchemaRegistry schemaRegistry, Device device,
                                                                      Element nodeEle, String modifiedAlarmNs) {
        QName modifiedSeverityQName = QName.create(modifiedAlarmNs, AlarmConstants.ALARM_PERCEIVED_SEVERITY);
        QName modifiedEventTimeQName = QName.create(modifiedAlarmNs, AlarmConstants.TIME);
        QName modifiedAlarmTextQName = QName.create(modifiedAlarmNs, AlarmConstants.ALARM_TEXT);
        return createAlarmInfo(schemaRegistry, device, nodeEle, modifiedAlarmNs, modifiedSeverityQName, modifiedEventTimeQName,
                modifiedAlarmTextQName);
    }

    private static AlarmInfo createAlarmInfo(SchemaRegistry schemaRegistry, Device device, Element nodeEle,
                                             String modifiedIETFAlarmNs, QName modifiedSeverityQName, QName modifiedEventTimeQName,
                                             QName modifiedAlarmTextQName) {
        QName modifiedResourceQName = QName.create(modifiedIETFAlarmNs, RESOURCE_TAG);
        QName modifiedAlarmTypeIdQName = QName.create(modifiedIETFAlarmNs, ALARM_TYPE_ID_TAG);
        QName modifiedAlarmTypeQualifier = QName.create(modifiedIETFAlarmNs, AlarmConstants.ALARM_TYPE_QUALIFIER);
        NodeList list = nodeEle.getChildNodes();
        String alarmTypeId = null;
        String fullAlarmTypeId = null;
        String time = null;
        String severity = null;
        String alarmText = null;
        ModelNodeId resourceId = null;
        String alarmTypeQualifier = null;
        String resource = null;
        String resourceNamespaces = null;
        List<String> alarmProcessingErrors = new ArrayList<>();
        for (int index = 0; index < list.getLength(); index++) {
            Node innerNode = list.item(index);
            if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element innerNodeEle = (Element) innerNode;
                String innerNodeName = innerNodeEle.getLocalName();
                QName attributeQName = QName.create(innerNodeEle.getNamespaceURI(), innerNodeName);
                try {
                    if (attributeQName.equals(modifiedResourceQName)) {
                        resource = innerNodeEle.getTextContent().trim();
                        if (!resource.startsWith("/")) {
                            throw new RuntimeException("Resource '" + resource + "' is not an instance-identifier, this is"
                                    + " not supported yet by BAA");
                        }
                        resourceId = AlarmUtil.toNodeIdUsingNodeElement(resource, innerNodeEle);
                        String resourceElement = DocumentUtils.documentToPrettyString(innerNodeEle).replaceAll(
                                "xmlns:alarms=\"" + modifiedIETFAlarmNs + "\"", "");
                        resourceNamespaces = resourceElement.substring((resourceElement.indexOf(innerNodeEle.getTagName())
                                + innerNodeEle.getTagName().length()) + 1, resourceElement.indexOf(">")).trim();
                    } else if (attributeQName.equals(modifiedAlarmTypeIdQName)) {
                        alarmTypeId = innerNodeEle.getTextContent().trim();
                        if (alarmTypeId != null) {
                            fullAlarmTypeId = getFullAlarmType(alarmTypeId, innerNodeEle, schemaRegistry);
                        }
                    } else if (attributeQName.equals(modifiedAlarmTypeQualifier)) {
                        alarmTypeQualifier = innerNodeEle.getTextContent().trim();
                    } else if (attributeQName.equals(modifiedEventTimeQName)) {
                        time = innerNodeEle.getTextContent().trim();
                    } else if (attributeQName.equals(modifiedSeverityQName)) {
                        severity = innerNodeEle.getTextContent().trim();
                    } else if (attributeQName.equals(modifiedAlarmTextQName)) {
                        alarmText = innerNodeEle.getTextContent().trim();
                    }
                } catch (AlarmProcessingException e) {
                    alarmProcessingErrors.add(e.getMessage());
                    if (resourceId == null) {
                        resourceId = e.getPartialModelNodeId();
                    }
                } catch (NetconfMessageBuilderException e) {
                    alarmProcessingErrors.add(e.getMessage());
                }
            }
        }
        if (!alarmProcessingErrors.isEmpty()) {
            if (alarmText == null) {
                alarmText = "";
            } else {
                alarmText += ", ";
            }
            alarmText += "Error processing AN alarm: " + String.join(", ", alarmProcessingErrors);
        }
        if (resourceId != null && alarmTypeId != null && time != null && severity != null
                && alarmText != null) {
            return new AlarmInfo(fullAlarmTypeId, alarmTypeQualifier, resourceId, resourceNamespaces,
                    parseTimestampValue(time), AlarmSeverity.getAlarmSeverity(severity), alarmText,
                    device.getDeviceName(), resource);
        }
        return null;
    }

    private static String getFullAlarmType(String id, Element element, SchemaRegistry schemaRegistry) {
        if (id.contains(COLON)) {
            String[] splits = id.split(COLON);
            String prefix = splits[0];
            String ns = element.lookupNamespaceURI(prefix);
            Module module = schemaRegistry.getModuleByNamespace(ns);
            Optional<Revision> revision = module.getQNameModule().getRevision();
            StringBuffer sb = new StringBuffer();
            sb.append(OPEN_CURLY_BRACES).append(prefix).append(CLOSE_CURLY_BRACES);
            sb.append(OPEN_PARANTHESIS).append(ns);
            if (revision.isPresent()) {
                sb.append(QUESTION_MARK).append(REVISION).append(revision.get());
            }
            String typeId = splits[1];
            sb.append(CLOSE_PARANTHESIS).append(typeId);
            return sb.toString();
        } else {
            return id;
        }
    }

    public static Timestamp parseTimestampValue(String dateInString) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT1);
        try {
            return new Timestamp(formatter.parse(dateInString).getTime());

        } catch (ParseException e) {
            SimpleDateFormat formatter2 = new SimpleDateFormat(DATE_TIME_FORMAT2);
            return getTimeStamp(dateInString, formatter2);
        }
    }

    private static Timestamp getTimeStamp(String dateInString, SimpleDateFormat formatter) {
        try {
            return new Timestamp(formatter.parse(dateInString).getTime());

        } catch (ParseException e) {
            throw new RuntimeException("Error while parsing to date format for the time : " + dateInString, e);
        }
    }

    public static List<AlarmInfo> extractAndCreateAlarmEntryFromResponse(Element outputElement, SchemaRegistry schemaRegistry,
                                                                         Device device, DeviceAdapterId deviceAdapterId,
                                                                         String ietfAlarmNs) {
        List<AlarmInfo> alarmInfoList = new ArrayList<>();
        try {
            NodeList baselist = outputElement.getChildNodes();
            for (int i = 0; i < baselist.getLength(); i++) {
                Node node = baselist.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeEle = (Element) node;
                    String nodeName = nodeEle.getLocalName();
                    QName expectedAlarmListQName = QName.create(ietfAlarmNs, ALARMLIST_TAG);
                    QName qname = QName.create(nodeEle.getNamespaceURI(), nodeName);
                    AlarmInfo info = null;
                    if (qname.equals(expectedAlarmListQName)) {
                        NodeList alarmListBaseListNode = nodeEle.getChildNodes();
                        for (int j = 0; j < alarmListBaseListNode.getLength(); j++) {
                            Node alarmListBaseNode = alarmListBaseListNode.item(j);
                            if (alarmListBaseNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element alarmListEle = (Element) alarmListBaseNode;
                                try {
                                    info = getAlarmInfoFromAlarmElement(schemaRegistry, device, alarmListEle, ietfAlarmNs);
                                    if (info != null) {
                                        alarmInfoList.add(info);
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Could not process alarm due to exception on unexpected response, "
                                            + "more details in debug", e);
                                    LOGGER.debug("Exception while processing alarm:", e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not process alarm due to exception on unexpected response, more details in debug", e);
            LOGGER.debug("Exception while processing alarm:", e);
        }
        return alarmInfoList;
    }

    public static List<AlarmInfo> extractAndCreateAlarmEntryFromNotification(Element outputElement, SchemaRegistry schemaRegistry,
                                                                             Device device, String alarmNs) {
        List<AlarmInfo> alarmInfoList = new ArrayList<>();
        try {
            QName expectedAlarmListQName = QName.create(alarmNs, AlarmConstants.ALARM_NOTIFICATION);
            QName qname = QName.create(outputElement.getNamespaceURI(), outputElement.getLocalName());
            AlarmInfo info = null;
            if (qname.equals(expectedAlarmListQName)) {
                info = getAlarmInfoFromAlarmNotificationElement(schemaRegistry, device, outputElement, alarmNs);
                if (info != null) {
                    alarmInfoList.add(info);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Could not process alarm: %s due to exception %s", outputElement, e));
            throw new RuntimeException("Exception while processing alarm", e);
        }
        return alarmInfoList;
    }

}
