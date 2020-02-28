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

package org.broadband_forum.obbaa.netconf.alarm.util;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmNotification;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmParameters;
import org.broadband_forum.obbaa.netconf.alarm.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.joda.time.DateTime;
import org.w3c.dom.Element;

public class AlarmsDocumentTransformer extends PojoToDocumentTransformer {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AlarmsDocumentTransformer.class,
            LogAppNames.NETCONF_ALARM);

    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    private static final String OPEN_CURLY_BRACKETS = "{";
    private static final String CLOSE_CURLY_BRACKETS = "}";
    private static final String COLON = ":";
    private static final String QUESTION_MARK = "?";
    private static final String UTF8 = "UTF-8";

    protected SchemaRegistry m_schemaRegistry;

    private ModelNodeDataStoreManager m_dsm;

    public AlarmsDocumentTransformer(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager dsm) {
        m_schemaRegistry = schemaRegistry;
        m_dsm = dsm;
    }

    protected void appendChild(Element parent, String ns, String name, String value) {
        Element child = m_doc.createElementNS(ns, name);
        child.setTextContent(value);
        parent.appendChild(child);
    }

    protected String buildLocalName(String prefix, String localName) {
        if (prefix == null) {
            return localName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(":").append(localName);
        return sb.toString();
    }

    public Element buildAlarmParameters(String namespace, String prefix, AlarmParameters alarmParameters)
            throws NetconfMessageBuilderException {
        Element alarm = m_doc.createElementNS(namespace, buildLocalName(prefix, AlarmConstants.ALARM_ELEMENT));
        buildAlarmParameters(namespace, prefix, alarmParameters, alarm);
        return alarm;
    }

    protected void buildAlarmParameters(String namespace, String prefix, AlarmParameters alarmParameters, Element alarm)
            throws NetconfMessageBuilderException {
        // append alarm source/resource
        if (m_schemaRegistry != null) {
            ModelNodeId resourceId = alarmParameters.getResource();
            Map<String, Pair<String, SchemaRegistry>> prefixToNsMap = AlarmUtil.getPrefixToNsMap(resourceId, m_dsm, m_schemaRegistry,
                    alarmParameters.getMountKey());

            // add resource element
            if (alarmParameters.getResource() != null) {
                if (!prefixToNsMap.isEmpty()) {
                    Element resourceElement = m_doc.createElementNS(namespace, buildLocalName(prefix,
                            getAlarmElementName(AlarmConstants.ALARM_RESOURCE)));
                    for (String prefixVal : prefixToNsMap.keySet()) {
                        String resourceNamespace = prefixToNsMap.get(prefixVal).getFirst();
                        if (!resourceNamespace.equals(namespace)) {
                            resourceElement.setAttributeNS(XMLNS_NAMESPACE, XMLNS + prefixVal, resourceNamespace);
                        }
                    }
                    resourceElement.setTextContent(AlarmUtil.xPathString(prefixToNsMap, m_schemaRegistry, resourceId));
                    alarm.appendChild(resourceElement);
                }
            } else if (alarmParameters.getResourceNamespaces() != null) {
                Element resourceElement = m_doc.createElementNS(namespace, buildLocalName(prefix,
                        getAlarmElementName(AlarmConstants.ALARM_RESOURCE)));
                String resource = alarmParameters.getResourceNamespaces();
                String[] resourceNamespaces = resource.split(" ");
                for (String resourceNamespace : resourceNamespaces) {
                    String[] prefixNamespace = resourceNamespace.split("=");
                    String namespaceUri = prefixNamespace[1].split("\"")[1];
                    resourceElement.setAttributeNS(XMLNS_NAMESPACE, prefixNamespace[0], namespaceUri);
                }
                resourceElement.setTextContent(alarmParameters.getResourceObjectString());
                alarm.appendChild(resourceElement);
            } else {
                appendChild(alarm, namespace, buildLocalName(prefix, getAlarmElementName(AlarmConstants.ALARM_RESOURCE)),
                        alarmParameters.getResourceObjectString());
            }

            // add alarm type id
            Element alarmTypeIdElement = m_doc.createElementNS(namespace, buildLocalName(prefix,
                    getAlarmElementName(AlarmConstants.ALARM_TYPE_ID)));
            String alarmType = alarmParameters.getAlarmTypeId();
            String alarmTypeId = replaceNamespaceToPrefix(alarmType);

            if (alarmTypeId != null) {
                String alarmTypeNamespace = getNamespace(alarmType);
                String nsPrefix = getPrefix(alarmTypeNamespace, alarmType);

                if (nsPrefix != null && alarmTypeNamespace != null) {
                    alarmTypeIdElement.setAttributeNS(XMLNS_NAMESPACE, XMLNS + nsPrefix, alarmTypeNamespace);
                }
            }
            alarmTypeIdElement.setTextContent(alarmTypeId);
            alarm.appendChild(alarmTypeIdElement);

            // Add alarm-type-qualifier
            Element alarmTypeQualifierElement = m_doc.createElementNS(namespace, buildLocalName(prefix,
                    getAlarmElementName(AlarmConstants.ALARM_TYPE_QUALIFIER)));
            String qualifierText = alarmParameters.getAlarmTypeQualifier();
            String alarmTypeQualifier = replaceNamespaceToPrefix(qualifierText);

            if (alarmTypeQualifier != null) {
                String qualifierNamespace = getNamespace(qualifierText);
                String nsPrefix = getPrefix(qualifierNamespace, qualifierText);

                if (nsPrefix != null && qualifierNamespace != null) {
                    alarmTypeQualifierElement.setAttributeNS(XMLNS_NAMESPACE, XMLNS + nsPrefix, qualifierNamespace);
                }
            }
            alarmTypeQualifierElement.setTextContent(alarmTypeQualifier);
            alarm.appendChild(alarmTypeQualifierElement);
        } else {
            String resource = alarmParameters.getResource().xPathString(m_schemaRegistry);
            appendChild(alarm, namespace, buildLocalName(prefix, getAlarmElementName(AlarmConstants.ALARM_RESOURCE)), resource);
            appendChild(alarm, namespace, buildLocalName(prefix, getAlarmElementName(AlarmConstants.ALARM_TYPE_ID)),
                    alarmParameters.getAlarmTypeId());
        }


        // append time stamp - last status change
        Timestamp timestamp = alarmParameters.getLastStatusChange();
        DateTime dateTime = new DateTime(timestamp.getTime());
        appendChild(alarm, namespace, buildLocalName(prefix, getAlarmElementName(AlarmConstants.TIME)), dateTime.toString());

        // append last perceived severity
        appendChild(alarm, namespace, buildLocalName(prefix, getAlarmElementName(AlarmConstants.ALARM_PERCEIVED_SEVERITY)),
                alarmParameters.getLastPerceivedSeverity().toString());

        // append last alarm text
        appendChild(alarm, namespace, buildLocalName(prefix, getAlarmElementName(AlarmConstants.ALARM_TEXT)),
                alarmParameters.getLastAlarmText());
    }

    protected String getAlarmElementName(String name) {
        return name;
    }

    private String replaceNamespaceToPrefix(String alarmTypeId) {
        if (alarmTypeId != null) {
            String namespace = getNamespace(alarmTypeId);
            String nsPrefix = getPrefix(namespace, alarmTypeId);
            if (nsPrefix != null) {
                alarmTypeId = nsPrefix + COLON + alarmTypeId.substring(alarmTypeId.indexOf(CLOSE_BRACKET) + 1);
            }
        }
        return alarmTypeId;
    }

    private String replaceNamespaceToModuleName(String alarmTypeId) {
        String namespace = getNamespace(alarmTypeId);
        String nsModuleName = m_schemaRegistry.getModuleNameByNamespace(namespace);
        if (nsModuleName != null) {
            alarmTypeId = nsModuleName + COLON + alarmTypeId.substring(alarmTypeId.indexOf(CLOSE_BRACKET) + 1);
        }
        return alarmTypeId;
    }

    private String replaceQualifierNamespaceToModuleName(String qualifier, Map<String, Pair<String, SchemaRegistry>> prefixToNsMap) {
        String namespace = getNamespace(qualifier);
        for (String prefixVal : prefixToNsMap.keySet()) {
            SchemaRegistry schemaRegistry = prefixToNsMap.get(prefixVal).getSecond();
            String moduleName = schemaRegistry.getModuleNameByNamespace(namespace);
            if (moduleName != null) {
                qualifier = moduleName + COLON + qualifier.substring(qualifier.indexOf(CLOSE_BRACKET) + 1);
                break;
            }
        }
        return qualifier;
    }

    private String getPrefix(String namespace, String text) {
        String prefix = m_schemaRegistry.getPrefix(namespace);
        if (prefix == null && text != null) {
            if (text.contains(OPEN_CURLY_BRACKETS)) {
                prefix = text.substring(text.indexOf(OPEN_CURLY_BRACKETS) + 1, text.indexOf(CLOSE_CURLY_BRACKETS));
            }
        }
        return prefix;
    }

    private String getNamespace(String alarmTypeId) {
        if (alarmTypeId != null) {
            if (alarmTypeId.contains(OPEN_BRACKET) && alarmTypeId.contains(QUESTION_MARK)) {
                return alarmTypeId.substring(alarmTypeId.indexOf(OPEN_BRACKET) + 1, alarmTypeId.indexOf(QUESTION_MARK));
            } else if (alarmTypeId.contains(OPEN_BRACKET)) {
                return alarmTypeId.substring(alarmTypeId.indexOf(OPEN_BRACKET) + 1, alarmTypeId.indexOf(CLOSE_BRACKET));
            }
        }
        return alarmTypeId;
    }

    /**
     * Build a document of format
     * <alarm-notification>
     * <resource/>
     * <alarm-type-id/>
     * <last-status-change/>
     * <last-perceived-severity/>
     * <last-alarm-text/>
     * <last-alarm-condition/>
     * </alarm-notification>
     *
     * @param alarmNotif An alarm notification
     * @return org.w3c.dom.Element
     * @throws NetconfMessageBuilderException : Exception while building netconf message
     */
    public Element getAlarmNotificationElement(AlarmNotification alarmNotif) throws NetconfMessageBuilderException {
        return buildAlarmNotification(alarmNotif, AlarmConstants.ALARM_NAMESPACE);
    }

    public Element buildAlarmNotification(AlarmNotification alarmNotif, String alarmNS) throws NetconfMessageBuilderException {
        try {
            m_doc = DocumentUtils.getNewDocument();
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }

        String prefix = null;
        if (m_schemaRegistry != null) {
            prefix = m_schemaRegistry.getPrefix(alarmNS);
            if (prefix == null) {
                prefix = AlarmConstants.ALARM_PREFIX;
            }
        }

        Element alarmNotificationElement = m_doc.createElementNS(alarmNS,
                buildLocalName(prefix, AlarmConstants.ALARM_NOTIFICATION));

        buildAlarmParameters(alarmNS, prefix, alarmNotif, alarmNotificationElement);
        LOGGER.debug(null, "Processed alarmNotification-%s ", alarmNotif.toString());

        return alarmNotificationElement;
    }

    public Element getAlarmRpcOutputElement(String namespace, String offsetValue, List<AlarmParameters> alarmParameters)
            throws NetconfMessageBuilderException {
        String prefix = null;
        if (m_schemaRegistry != null) {
            prefix = getPrefix(namespace, null);
        }

        try {
            m_doc = DocumentUtils.getNewDocument();
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }

        Element alarmRpcOutputElement = m_doc.createElementNS(namespace, buildLocalName(prefix, AlarmConstants.ACTIVE_ALARMS));

        if (offsetValue != null) {
            appendChild(alarmRpcOutputElement, namespace, buildLocalName(prefix, AlarmConstants.OFFSET), offsetValue);
        }

        for (AlarmParameters alarmParameter : alarmParameters) {
            Element alarmElement = buildAlarmParameters(namespace, prefix, alarmParameter);
            alarmRpcOutputElement.appendChild(alarmElement);
        }
        return alarmRpcOutputElement;
    }
}
