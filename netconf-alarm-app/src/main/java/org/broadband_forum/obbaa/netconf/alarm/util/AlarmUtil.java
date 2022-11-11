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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public final class AlarmUtil {

    private static final String CLOSE_BRACKET = "]";
    private static final String COLON = ":";
    private static final String OPEN_BRACKET = "[";
    private static final String SLASH = "/";
    private static final String SINGLE_QUOTES = "\'";
    private static final String EQUAL_TO = "=";

    private AlarmUtil() {
    }

    /**
     * Split xPath string using JXPath  parser and return array of Step objects
     * Input: /baa:device-manager/adh:device-holder[adh:name= 'OLT-2345']/adh:device[adh:ID='R1.S1.LT1.P1.ONT1']
     * Output: { baa:device-manager, adh:device-holder[adh:name='OLT-2345'], adh:device[adh:ID='R1.S1.LT1.P1.ONT1']}
     *
     * @param xPath - xPath string
     * @return Step[] - Array of JXPath
     */
    public static Step[] splitXpathByJXPath(String xPath) {
        LocationPath locationPath = buildLocationPath(xPath);
        return locationPath.getSteps();
    }


    public static Map<String, Pair<String, SchemaRegistry>> getPrefixToNsMap(ModelNodeId modelNodeId, ModelNodeDataStoreManager dsm,
                                                                             SchemaRegistry schemaRegistry, String mountKey) {
        Map<String, Pair<String, SchemaRegistry>> prefixToNsMap = new HashMap<>();
        List<org.opendaylight.yangtools.yang.common.QName> qNameList = new ArrayList<>();
        ModelNodeId mountModelNodeId = new ModelNodeId();
        if (modelNodeId != null) {
            for (ModelNodeRdn rdn : modelNodeId.getRdns()) {
                String ns = rdn.getNamespace();
                Module module = schemaRegistry.getModuleByNamespace(ns);
                if (module != null) {
                    if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName())) {
                        qNameList.add(org.opendaylight.yangtools.yang.common.QName.create(ns, rdn.getRdnValue(),
                                module.getRevision().orElse(null)));
                    }
                    ModelNodeRdn mountrdn = new ModelNodeRdn(rdn.getRdnName(), rdn.getNamespace(), removeSingleQuotes(rdn.getRdnValue()));
                    mountModelNodeId.addRdn(mountrdn);
                } else {
                    SchemaPath schemaPath = SchemaPath.create(qNameList, true);
                    SchemaRegistry mountRegistry = getMountRegistry(schemaRegistry, dsm, schemaPath, mountModelNodeId.getParentId(),
                            mountKey);
                    if (mountRegistry != null) {
                        schemaRegistry = mountRegistry;
                        module = schemaRegistry.getModuleByNamespace(ns);
                    }
                }
                if (module != null) {
                    Pair<String, SchemaRegistry> nsAndhSR = new Pair<>(ns, schemaRegistry);
                    prefixToNsMap.put(module.getPrefix(), nsAndhSR);
                }
            }
        }
        return prefixToNsMap;
    }

    private static String removeSingleQuotes(String rdnValue) {
        if (rdnValue != null && rdnValue.startsWith(SINGLE_QUOTES) && rdnValue.endsWith(SINGLE_QUOTES)) {
            return rdnValue.substring(1, rdnValue.length() - 1);
        }
        return rdnValue;
    }

    private static SchemaRegistry getMountRegistry(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager dsm, SchemaPath schemaPath,
                                                   ModelNodeId modelNodeId, String mountKey) {
        SchemaMountRegistry mountRegistry = schemaRegistry.getMountRegistry();
        if (mountRegistry != null && dsm != null) {
            SchemaMountRegistryProvider provider = mountRegistry.getProvider(schemaPath);
            ModelNode modelNode = dsm.findNode(schemaPath, ModelNodeKey.EMPTY_KEY, modelNodeId, schemaRegistry);
            if (modelNode != null) {
                return provider.getSchemaRegistry(modelNode.getModelNodeId());
            } else {
                return provider.getSchemaRegistry(mountKey);
            }
        }
        return null;
    }

    /**
     * Parse the xPath string using JXPath compiler. If xPath string does not have proper single quotes then
     * JXPathInvalidSyntaxException will be thrown.
     *
     * @param xPath xPath value
     * @return LocationPath : LocationPath of the xpath using JXPath compiler
     */
    private static LocationPath buildLocationPath(String xPath) {
        return (LocationPath) JXPathUtils.getExpression((JXPathCompiledExpression) JXPathContext.compile(xPath));
    }

    /**
     * Parse the dbId string using JXPath and covert it to ModelNodeId object.
     * 'dbId' must have proper single quotes, otherwise JXpath compiler will be thrown JXPathInvalidSyntaxException
     *
     * @param dbId    : dbId string to be parsed
     * @param element : node element
     * @return ModelNodeId : modelNodeId of dbId
     */
    public static ModelNodeId toNodeIdUsingNodeElement(String dbId, Element element) {
        ModelNodeId modelNodeId = new ModelNodeId();
        String moduleNamespace = null;
        // Parse the dbId string using JXPath compiler
        LocationPath locationPath = AlarmUtil.buildLocationPath(dbId);
        // steps:  [baa:device-manager, baa-device-holders:device-holder[name = 'OLT-2345'], device[ID = 'R1.S1.LT1.P1.ONT1']]
        Step[] steps = locationPath.getSteps();
        for (Step stepNode : steps) {
            moduleNamespace = buildNodeIdUsingNodeElement(element, null, modelNodeId, stepNode, moduleNamespace);
            // Identify predicates (get the expression by '['  ']' pattern)
            Expression[] predicates = stepNode.getPredicates();
            if (predicates != null) {
                for (int predicateCount = 0; predicateCount < predicates.length; predicateCount++) {
                    moduleNamespace = buildNodeIdUsingNodeElement(element, null, modelNodeId, predicates[predicateCount],
                            moduleNamespace);
                }
            }
        }
        return modelNodeId;
    }

    public static ModelNodeId toNodeIdUsingRegistry(String dbId, SchemaRegistry registry) {
        ModelNodeId modelNodeId = new ModelNodeId();
        String moduleNamespace = null;
        // Parse the dbId string using JXPath compiler
        LocationPath locationPath = AlarmUtil.buildLocationPath(dbId);
        // steps:  [baa:device-manager, baa-device-holders:device-holder[name = 'OLT-2345'], device[ID = 'R1.S1.LT1.P1.ONT1']]
        Step[] steps = locationPath.getSteps();
        for (Step stepNode : steps) {
            moduleNamespace = buildNodeIdUsingNodeElement(null, registry, modelNodeId, stepNode, moduleNamespace);
            // Identify predicates (get the expression by '['  ']' pattern)
            Expression[] predicates = stepNode.getPredicates();
            if (predicates != null) {
                for (int predicateCount = 0; predicateCount < predicates.length; predicateCount++) {
                    moduleNamespace = buildNodeIdUsingNodeElement(null, registry, modelNodeId, predicates[predicateCount],
                            moduleNamespace);
                }
            }
        }
        return modelNodeId;
    }


    private static String buildNodeIdUsingNodeElement(Element element, SchemaRegistry registry, ModelNodeId modelNodeId,
                                                      Expression expression, String moduleNamespace) {
        if (expression instanceof CoreOperationEqual) {
            CoreOperationEqual operationEqual = (CoreOperationEqual) expression;
            Expression[] operations = operationEqual.getArguments();
            Expression attribute = operations[0];
            Expression attributeValue = operations[1];
            String attributeValueStr = attributeValue.toString();// 'OLT-2345'
            if (attribute instanceof LocationPath) {
                LocationPath path = (LocationPath) attribute; // attribute : name
                moduleNamespace = buildNodeIdUsingNodeElement(element, registry, modelNodeId, path.getSteps()[0], attributeValueStr,
                        moduleNamespace);
            }
        }
        return moduleNamespace;
    }

    private static String buildNodeIdUsingNodeElement(Element element, SchemaRegistry registry, ModelNodeId modelNodeId, Step stepNode,
                                                      String rdnValue, String moduleNamespace) {
        String prefix = null;
        String rdnName = null;

        QName qName = getNodeName(stepNode);
        if (qName != null) {
            prefix = qName.getPrefix();
            rdnName = qName.getName();
        }

        /**
         * Resolve namespace from module name
         */
        if (element != null) {
            if (prefix != null) {
                moduleNamespace = element.lookupNamespaceURI(prefix);
            }
        } else {
            moduleNamespace = registry.getNamespaceURI(prefix);
        }

        if (moduleNamespace == null) {
            throw new AlarmProcessingException("Namespace is missing for prefix '" + prefix + "' in resource", modelNodeId);
        }

        if (!moduleNamespace.isEmpty() && rdnName != null && !rdnName.isEmpty()) {
            modelNodeId.addRdn(rdnName, moduleNamespace, rdnValue);
        }
        return moduleNamespace;
    }

    private static String buildNodeIdUsingNodeElement(Element element, SchemaRegistry registry, ModelNodeId modelNodeId,
                                                      Step stepNode, String moduleNamespace) {
        String rdnName = ModelNodeRdn.CONTAINER;
        String prefix = null;
        String rdnValue = null;

        QName qName = getNodeName(stepNode);

        if (qName != null) {
            prefix = qName.getPrefix();
            rdnValue = qName.getName();
        }
        /**
         * Resolve namespace from module name
         */
        if (element != null) {
            if (prefix != null) {
                moduleNamespace = element.lookupNamespaceURI(prefix);
            }
        } else {
            moduleNamespace = registry.getNamespaceURI(prefix);
        }

        if (moduleNamespace == null) {
            throw new AlarmProcessingException("Namespace is missing for prefix '" + prefix + "' in resource", modelNodeId);
        }

        if (!moduleNamespace.isEmpty() && rdnValue != null && !rdnValue.isEmpty()) {
            modelNodeId.addRdn(rdnName, moduleNamespace, rdnValue);
        }
        return moduleNamespace;
    }

    public static Alarm convertToAlarmEntity(AlarmInfo alarmInfo, String dbId) {
        Alarm alarmEntity = new Alarm();
        if (alarmInfo.getSourceObject() != null) {
            alarmEntity.setSourceObject(dbId);
            alarmEntity.setYangResource(true);
            String sourceObjectNamespaces = alarmInfo.getSourceObjectNamespaces();
            if (sourceObjectNamespaces != null) {
                alarmEntity.setResourceNamespaces(sourceObjectNamespaces);
                alarmEntity.setResourceName(alarmInfo.getSourceObjectString());
            }
        } else {
            alarmEntity.setSourceObject(alarmInfo.getSourceObjectString());
            alarmEntity.setYangResource(false);
        }
        alarmEntity.setAlarmTypeId(alarmInfo.getAlarmTypeId());
        alarmEntity.setAlarmTypeQualifier(alarmInfo.getAlarmTypeQualifier());
        alarmEntity.setRaisedTime(alarmInfo.getTime());
        alarmEntity.setSeverity(alarmInfo.getSeverity());
        alarmEntity.setAlarmText(alarmInfo.getAlarmText());
        alarmEntity.setDeviceId(alarmInfo.getDeviceName());
        return alarmEntity;
    }

    public static List<AlarmInfo> convertAlarmToAlarmInfoList(List<Alarm> existingBaaAlarms, SchemaRegistry schemaRegistry,
                                                              ModelNodeDataStoreManager dsm) {
        List<AlarmInfo> existingAlarmInfoList = new ArrayList<>();
        for (Alarm alarm : existingBaaAlarms) {
            String alarmTypeId = alarm.getAlarmTypeId();
            ModelNodeId modelNodeId = alarm.isYangResource() ? toNodeId(schemaRegistry, dsm, alarm.getSourceObject()) : null;
            String resourceObjectString = alarm.isYangResource() ? null : alarm.getSourceObject();
            AlarmInfo alarmInfo = new AlarmInfo(alarmTypeId, alarm.getAlarmTypeQualifier(), modelNodeId,
                    alarm.getRaisedTime(), alarm.getSeverity(), alarm.getAlarmText(), alarm.getDeviceId(), resourceObjectString);
            existingAlarmInfoList.add(alarmInfo);
        }
        return existingAlarmInfoList;
    }

    /**
     * Build xPath string with single quotes from ModelNodeId object.
     */
    public static String xPathAlarmString(Map<String, Pair<String, SchemaRegistry>> prefixToNsMap, ModelNodeId modelNodeId) {
        StringBuilder sb = new StringBuilder();
        List<ModelNodeRdn> rdns = modelNodeId.getRdns();
        if (!rdns.isEmpty()) {
            for (ModelNodeRdn rdn : rdns) {
                String prefix = "";
                if (prefixToNsMap != null) {
                    prefix = getPrefixFromNs(prefixToNsMap, rdn.getNamespace());
                    if (prefix == null) {
                        prefix = "";
                    } else {
                        prefix += COLON;
                    }
                }
                if (rdn.getRdnName().equals(ModelNodeRdn.CONTAINER)) {
                    sb.append(SLASH).append(prefix).append(rdn.getRdnValue());
                } else {
                    sb.append(OPEN_BRACKET).append(prefix).append(rdn.getRdnName()).append(EQUAL_TO);
                    if (!rdn.getRdnValue().startsWith(SINGLE_QUOTES)) {
                        sb.append(SINGLE_QUOTES);
                    }
                    sb.append(rdn.getRdnValue());
                    if (!rdn.getRdnValue().endsWith(SINGLE_QUOTES)) {
                        sb.append(SINGLE_QUOTES);
                    }
                    sb.append(CLOSE_BRACKET);
                }
            }
        } else {
            sb.append(SLASH);
        }
        return sb.toString();
    }

    private static String getPrefixFromNs(Map<String, Pair<String, SchemaRegistry>> prefixToNsMap, String namespace) {
        for (Entry<String, Pair<String, SchemaRegistry>> entry : prefixToNsMap.entrySet()) {
            String prefix = entry.getKey();
            String ns = entry.getValue().getFirst();
            if (namespace.equals(ns)) {
                return prefix;
            }
        }
        return null;
    }

    private static String getPrefixFromNsMap(Map<String, String> prefixToNsMap, String namespace) {
        for (Entry<String, String> entry : prefixToNsMap.entrySet()) {
            String prefix = entry.getKey();
            String ns = entry.getValue();
            if (namespace.equals(ns)) {
                return prefix;
            }
        }
        return null;
    }


    /**
     * Transforms modelNodeId (instance-identifier) to a short String called dbId for persisting to DB.
     * This method convert to 'dbId' string from ModelNodeId object using JXPath parsing.
     * Input : ModelNodeId[/container=device-manager/container=device-holder/name='OLT-2345'/container=device/ID='R1.S1.LT1.P1.ONT1'] <br>
     * Output: /baa:device-manager/baa-device-holders:device-holder[name='OLT-2345']/device[ID='R1.S1.LT1.P1.ONT1']
     *
     * @param schemaRegistry : schemaRegistry to use
     * @param dsm            : dataStoreManager
     * @param modelNodeId    : modelNodeId fir wich nodeId string is to be built
     * @return DBId value
     */
    public static String toDbId(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager dsm, ModelNodeId modelNodeId,
                                Map<String, Pair<String, SchemaRegistry>> prefixToNsMap) {

        /**
         * build nodeId string from modelNodeId, also append single quotes if it does not exists in modelnodeId
         * Example : ModelNodeId[/container=device-manager/container=device-holder/name ='OLT-2345'/container=device/ID='R1.S1.LT1.P1.ONT1']
         */
        String nodeId = AlarmUtil.xPathAlarmString(prefixToNsMap, modelNodeId);
        List<String> moduleNamesList = new ArrayList<String>();
        StringBuilder dbId = new StringBuilder();
        /**
         * Split the nodeId using JXPath parser (based on '/' character)
         * nodeId: /baa:device-manager/adh:device-holder[adh:name='OLT-2345']/adh:device[adh:ID='R1.S1.LT1.P1.ONT1']
         * splitSteps: {baa:device-manager, adh:device-holder[adh:name = 'OLT-2345'], adh:device[adh:ID = 'R1.S1.LT1.P1.ONT1']}
         *
         */
        LocationPath jxPath = AlarmUtil.buildLocationPath(nodeId);
        Step[] splitSteps = jxPath.getSteps();
        if (splitSteps != null && splitSteps.length > 0) {

            dbId.append(SLASH);

            /**
             * Iterate each Step object and build dbId string using jxPath parsing
             *
             * splitSteps : {baa:device-manager, adh:device-holder[adh:name = 'OLT-2345'], adh:device[adh:ID = 'R1.S1.LT1.P1.ONT1']}
             *
             * dbId: /baa:device-manager/baa-device-holders:device-holder[name='OLT-2345']/device[ID='R1.S1.LT1.P1.ONT1']
             *
             */
            for (Step stepNode : splitSteps) {

                SchemaRegistry mountRegistry = buildDbId(stepNode, schemaRegistry, prefixToNsMap, moduleNamesList, dbId);
                if (mountRegistry != null) { // From this step mount registry will get into the place
                    schemaRegistry = mountRegistry;
                }
                // If stepNode has any predicates (found '['  and ']' pattern)t hen parse each predicates {adh:name = 'OLT-2345'}
                Expression[] predicates = stepNode.getPredicates();
                if (predicates != null && predicates.length > 0) {
                    for (int predicateCount = 0; predicateCount < predicates.length; predicateCount++) {
                        // predicates[0]: adh:name = 'OLT-2345'
                        buildDbId(predicates[predicateCount], schemaRegistry, prefixToNsMap, moduleNamesList, dbId);
                    }
                }
                dbId.append(SLASH);
            }
        }
        dbId = dbId.deleteCharAt(dbId.length() - 1);
        return dbId.toString();
    }

    /**
     * Convert QName object from StepNode. QName object contains prefix and local name
     *
     * @param stepNode : stepNode from which QName has to be obtained
     * @return QName : QName obtained from stepNode containing prefix and localName
     */
    public static QName getNodeName(Step stepNode) {
        NodeTest nodeTest = stepNode.getNodeTest();
        if (nodeTest instanceof NodeNameTest) {
            NodeNameTest nameTest = (NodeNameTest) stepNode.getNodeTest();
            return nameTest.getNodeName();
        }
        return null;
    }

    private static SchemaRegistry buildDbId(Step stepNode, SchemaRegistry schemaRegistry,
                                            Map<String, Pair<String, SchemaRegistry>> prefixToNsMap, List<String> moduleNamesList,
                                            StringBuilder dbId) {
        String prefix = null;
        String localName = null;

        // stepNode baa:device-manager
        QName qName = getNodeName(stepNode);
        if (qName != null) {
            prefix = qName.getPrefix();
            localName = qName.getName();
        }
        Pair<String, SchemaRegistry> pair = prefixToNsMap.get(prefix);
        String namespace = pair.getFirst();
        SchemaRegistry mountRegistry = pair.getSecond();
        String moduleName = mountRegistry.getModuleNameByNamespace(namespace);
        if (moduleName != null && !moduleNamesList.contains(moduleName)) {
            moduleNamesList.add(moduleName);
            dbId.append(moduleName).append(COLON).append(localName);
        } else {
            dbId.append(localName);
        }
        return mountRegistry;
    }

    /**
     * Parse the predicate expression if it matches with '=' pattern.
     *
     * @param expression      : adh:name = 'OLT-2345'
     * @param schemaRegistry  : schemaRegistry to use to parse
     * @param prefixToNsMap   : map of prefix to namespace
     * @param moduleNamesList : List of moduleNames
     * @param dbId            : String dbId
     */
    private static void buildDbId(Expression expression, SchemaRegistry schemaRegistry, Map<String, Pair<String,
            SchemaRegistry>> prefixToNsMap, List<String> moduleNamesList, StringBuilder dbId) {
        /*
         *  expression: adh:name = 'OLT-2345'
         *  If an expression matches with '=' pattern, then split the expression
         */

        if (expression instanceof CoreOperationEqual) {
            CoreOperationEqual operationEqual = (CoreOperationEqual) expression;
            // Split the expression based on '='
            Expression[] operations = operationEqual.getArguments();
            // attribute name  adh:name
            Expression attribute = operations[0]; // adh:name
            // attribute value 'OLT-2345'
            Expression attributeValue = operations[1];
            String attributeValueStr = attributeValue.toString();// 'OLT-2345'
            if (attribute instanceof LocationPath) {
                LocationPath path = (LocationPath) attribute; // operation[0] : adh:name
                // again split the expression by '/'
                buildDbId(path.getSteps()[0], schemaRegistry, prefixToNsMap, moduleNamesList, dbId,
                        attributeValueStr);
            }

        }
    }

    /**
     * This method used to build the DbId string.
     */
    private static void buildDbId(Step stepNode, SchemaRegistry schemaRegistry, Map<String, Pair<String, SchemaRegistry>> prefixToNsMap,
                                  List<String> moduleNamesList, StringBuilder dbId, String attributeValue) {
        String prefix = null;
        String localName = null;

        // Get prefix and localname from QName object
        QName qName = getNodeName(stepNode);
        if (qName != null) {
            prefix = qName.getPrefix(); // adh
            localName = qName.getName(); // name
        }
        String namespace = prefixToNsMap.get(prefix).getFirst();
        String moduleName = schemaRegistry.getModuleNameByNamespace(namespace);
        dbId.append(OPEN_BRACKET);
        if (moduleName != null && !moduleNamesList.contains(moduleName)) {
            moduleNamesList.add(moduleName);
            dbId.append(moduleName).append(COLON).append(localName);
        } else {
            dbId.append(localName + EQUAL_TO + attributeValue); // [name='OLT-2345']
        }
        dbId.append(CLOSE_BRACKET);
    }

    /**
     * Transforms dbId to modelNodeId using JXPath parsing
     * 1) By replacing module names by containers & key fields
     * Input: /baa:device-manager/baa-device-holders:device-holder[name='OLT-2345']/device[ID='R1.S1.LT1.P1.ONT1']
     * Output: ModelNodeId[/container=device-manager/container=device-holder/name ='OLT-2345'/container=device/ID='R1.S1.LT1.P1.ONT1']
     */
    public static ModelNodeId toNodeId(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager dsm, String dbId,
                                       String mountRegistryKey) {
        ModelNodeId modelNodeId = new ModelNodeId();
        String moduleNamespace = null;
        LocationPath locationPath = AlarmUtil.buildLocationPath(dbId);
        /**
         *  steps : [baa:device-manager, baa-device-holders:device-holder[name = 'OLT-2345'], device[ID = 'R1.S1.LT1.P1.ONT1']]
         */
        Step[] steps = locationPath.getSteps();
        // Iterate each Step expression and build modelNodeId
        List<org.opendaylight.yangtools.yang.common.QName> qNameList = new ArrayList<>();
        for (Step stepNode : steps) {
            moduleNamespace = buildNodeId(schemaRegistry, modelNodeId, stepNode, moduleNamespace, qNameList);
            if (moduleNamespace == null) {
                SchemaPath schemaPath = SchemaPath.create(qNameList, true);
                ModelNodeId mountModelNodeId = new ModelNodeId();
                for (ModelNodeRdn rdn : modelNodeId.getRdns()) {
                    ModelNodeRdn mountrdn = new ModelNodeRdn(rdn.getRdnName(), rdn.getNamespace(), removeSingleQuotes(rdn.getRdnValue()));
                    mountModelNodeId.addRdn(mountrdn);
                }
                SchemaRegistry mountRegistry = getMountRegistry(schemaRegistry, dsm, schemaPath, mountModelNodeId.getParentId(),
                        mountRegistryKey);
                if (mountRegistry != null) {
                    schemaRegistry = mountRegistry;
                    moduleNamespace = buildNodeId(schemaRegistry, modelNodeId, stepNode, moduleNamespace, qNameList);
                }
            }
            Expression[] predicates = stepNode.getPredicates();
            if (predicates != null) {
                for (int predicateCount = 0; predicateCount < predicates.length; predicateCount++) {
                    moduleNamespace = buildNodeId(schemaRegistry, modelNodeId, predicates[predicateCount],
                            moduleNamespace);
                }
            }
        }
        return modelNodeId;
    }

    public static ModelNodeId toNodeId(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager dsm, String dbId) {
        return toNodeId(schemaRegistry, dsm, dbId, null);
    }

    public static String xPathString(Map<String, Pair<String, SchemaRegistry>> prefixToNsMap, NamespaceContext nsContext,
                                     ModelNodeId modelNodeId) {
        StringBuilder sb = new StringBuilder();
        if (!modelNodeId.getRdns().isEmpty()) {
            for (ModelNodeRdn rdn : modelNodeId.getRdns()) {
                String prefix = "";
                if (nsContext != null) {
                    prefix = nsContext.getPrefix(rdn.getNamespace());
                    if (prefix == null) {
                        prefix = getPrefixFromNs(prefixToNsMap, rdn.getNamespace());
                    }
                    if (prefix == null) {
                        prefix = "";
                    } else {
                        prefix += ":";
                    }
                }

                if (rdn.getRdnName().equals(ModelNodeRdn.CONTAINER)) {
                    sb.append("/").append(prefix).append(rdn.getRdnValue());
                } else {
                    sb.append("[").append(prefix).append(rdn.getRdnName()).append("=").append(rdn.getRdnValue()).append("]");
                }
            }
        } else {
            sb.append("/");
        }
        return sb.toString();
    }

    private static String buildNodeId(SchemaRegistry schemaRegistry, ModelNodeId modelNodeId, Step stepNode,
                                      String rdnValue, String moduleNamespace) {
        String moduleName = null;
        String rdnName = null;

        QName qName = getNodeName(stepNode);
        if (qName != null) {
            moduleName = qName.getPrefix();
            rdnName = qName.getName();
        }

        /**
         * Resolve namespace from module name
         */
        if (moduleName != null) {
            moduleNamespace = schemaRegistry.getNamespaceOfModule(moduleName);
        }
        if (moduleNamespace != null && !moduleNamespace.isEmpty() && rdnName != null && !rdnName.isEmpty()) {
            modelNodeId.addRdn(rdnName, moduleNamespace, rdnValue);
        }
        return moduleNamespace;
    }

    /**
     * This method used to build the ModelNodeId for predicates.
     */
    private static String buildNodeId(SchemaRegistry schemaRegistry, ModelNodeId modelNodeIdJXPath,
                                      Expression expression, String moduleNamespace) {
        if (expression instanceof CoreOperationEqual) {
            CoreOperationEqual operationEqual = (CoreOperationEqual) expression;
            Expression[] operations = operationEqual.getArguments();
            Expression attribute = operations[0];
            Expression attributeValue = operations[1];
            String attributeValueStr = attributeValue.toString();// 'OLT-2345'
            if (attribute instanceof LocationPath) {
                LocationPath path = (LocationPath) attribute;
                moduleNamespace = buildNodeId(schemaRegistry, modelNodeIdJXPath, path.getSteps()[0], attributeValueStr,
                        moduleNamespace);
            }
        }
        return moduleNamespace;

    }

    private static String buildNodeId(SchemaRegistry schemaRegistry, ModelNodeId modelNodeId, Step stepNode,
                                      String moduleNamespace, List<org.opendaylight.yangtools.yang.common.QName> qNameList) {
        String rdnName = ModelNodeRdn.CONTAINER;
        String moduleName = null;
        String rdnValue = null;

        QName qName = getNodeName(stepNode);
        if (qName != null) {
            moduleName = qName.getPrefix();
            rdnValue = qName.getName();
        }

        /**
         * Resolve namespace from module name
         */
        if (moduleName != null) {
            moduleNamespace = schemaRegistry.getNamespaceOfModule(moduleName);
        }
        if (moduleNamespace != null) {
            Module module = schemaRegistry.getModuleByNamespace(moduleNamespace);
            if (module != null) {
                qNameList.add(org.opendaylight.yangtools.yang.common.QName.create(moduleNamespace, rdnValue,
                        module.getRevision().orElse(null)));
            }
        }
        if (moduleNamespace != null && !moduleNamespace.isEmpty() && rdnValue != null && !rdnValue.isEmpty()) {
            modelNodeId.addRdn(rdnName, moduleNamespace, rdnValue);
        }
        return moduleNamespace;
    }
}
