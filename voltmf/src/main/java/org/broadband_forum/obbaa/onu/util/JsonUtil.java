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

package org.broadband_forum.obbaa.onu.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;

/**
 * <p>
 * Utility to convert XML to JSON objects
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public final class JsonUtil {

    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class);
    private static final String XMLNS = "xmlns:";
    private static final String COLON = ":";
    private static final String IETF_NETCONF = "ietf-netconf";
    private static final String IETF_RESTCONF = "ietf-restconf";
    private static final String DATA_NODE = "data";
    private static final String INTEGER_TYPE = "^[+-]?(0x)?[0-9.]*$";
    private static final String HEX_INT_TYPE = "^(0x) {1}([0-9]*([a-f]*[A-F]*))+$";

    private static ThreadLocal<Boolean> c_ignoreEmptyLeaves = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static ThreadLocal<Boolean> c_appendActionInput = ThreadLocal.withInitial((Supplier<Boolean>) () -> Boolean.TRUE);

    private JsonUtil() {
        //Not called
    }

    public static String convertFromXmlToJson(List<Element> xmlElements, SchemaNode node, SchemaRegistry schemaRegistry,
                                              ModelNodeDataStoreManager modelNodeDSM) {
        boolean isOutputTagRequired = node instanceof ActionDefinition;
        String currentModuleName = null;
        if (isOutputTagRequired) {
            currentModuleName = getModuleName(node, schemaRegistry);
        }
        JSONObject json = new JSONObject();
        for (Element xml:xmlElements) {
            convertToJSONObject(json, xml, node, schemaRegistry, currentModuleName, modelNodeDSM);
        }

        if (!xmlElements.isEmpty() && isOutputTagRequired) {
            json = appendOutputTag(json, node, schemaRegistry);
        }
        return json.toString();
    }

    public static String convertFromXmlToJsonIgnoreEmptyLeaves(List<Element> xmlElements, SchemaNode node, SchemaRegistry schemaRegistry,
                                                               ModelNodeDataStoreManager modelNodeDSM) {
        return withIgnoreEmptyLeaves(new WithIgnoreEmptyLeavesTemplate<String>() {
            @Override
            protected String execute() {
                return convertFromXmlToJson(xmlElements, node, schemaRegistry, modelNodeDSM);
            }
        });
    }

    public static <RT> RT withIgnoreEmptyLeaves(WithIgnoreEmptyLeavesTemplate<RT> leavesTemplate) {
        return leavesTemplate.executeInternal();
    }

    private static JSONObject appendOutputTag(JSONObject json, SchemaNode node, SchemaRegistry schemaRegistry) {
        JSONObject output = new JSONObject();
        String outputKey = getOutputTag(node, schemaRegistry);
        output.put(outputKey, json);
        return output;
    }

    private static String getOutputTag(SchemaNode node, SchemaRegistry schemaRegistry) {
        String namespace = node.getQName().getNamespace().toString();
        schemaRegistry.getPrefix(namespace);
        String outputKey = getJsonNameAndModuleName(namespace, "output", null, schemaRegistry).getFirst();
        return outputKey;
    }

    private static Pair<String, String> getJsonNameAndModuleName(Node xmlNode, String currentModuleName, SchemaRegistry schemaRegistry) {
        String namespaceString = xmlNode.getNamespaceURI();
        String jsonName = xmlNode.getLocalName();
        return getJsonNameAndModuleName(namespaceString, jsonName, currentModuleName, schemaRegistry);
    }

    private static Pair<String, String> getJsonNameAndModuleName(String namespace, String localName, String currentModuleName,
                                                                 SchemaRegistry schemaRegistry) {
        String newModuleName = schemaRegistry.getModuleNameByNamespace(namespace);
        if (newModuleName == null) {
            LOGGER.warn("Module not found in SchemaRegistry for namespace " + namespace + ", while converting xml to json");
        } else if (!newModuleName.equals(currentModuleName)) {
            localName = newModuleName + COLON + localName;
        }
        return new Pair<>(localName, newModuleName);
    }

    public static String getModuleName(SchemaNode node, SchemaRegistry schemaRegistry) {
        String namespace = node.getQName().getNamespace().toString();
        return schemaRegistry.getModuleByNamespace(namespace).getName();
    }

    public static String getErrorPathJson(SchemaRegistry schemaRegistry, Element errorPathElement) {
        StringBuilder errorPath = new StringBuilder();
        String errorPathString = errorPathElement.getTextContent();
        String[] errorPathElements = errorPathString.split("/");
        int iter = 0;
        errorPathString = errorPathString.trim();
        if (errorPathString.startsWith("/")) {
            iter = 1;
        }
        String prefix = null;
        String moduleName = null;
        while (iter < errorPathElements.length) {
            errorPath.append("/");
            String errorPathValue = errorPathElements[iter];
            if (errorPathValue.contains(COLON)) {
                prefix = errorPathValue.split(COLON,2)[0];
                errorPathValue = errorPathValue.split(COLON,2)[1];
                String namespace = errorPathElement.lookupNamespaceURI(prefix);
                if (namespace != null) {
                    moduleName = schemaRegistry.getModuleNameByNamespace(namespace);
                    if (moduleName == null) {
                        SchemaRegistry parentRegistry = schemaRegistry.getParentRegistry();
                        if (parentRegistry != null) {
                            moduleName = parentRegistry.getModuleNameByNamespace(namespace);
                        }
                    }
                }
                if (errorPathValue.contains("[")) {
                    StringBuilder jsonListKeyBuilder = new StringBuilder();
                    String[] listAndKeyString = errorPathValue.split("[\\[\\]]");
                    jsonListKeyBuilder.append(listAndKeyString[0]);
                    int j = 1;
                    while (j < listAndKeyString.length) {
                        String key = listAndKeyString[j];
                        if (key.contains(COLON)) {
                            String keyPrefix = key.split(COLON)[0];
                            String keyValue = key.split(COLON)[1];
                            if (keyPrefix != null && !keyPrefix.equals(prefix)) {
                                namespace = errorPathElement.lookupNamespaceURI(prefix);
                                if (namespace != null) {
                                    moduleName = schemaRegistry.getModuleNameByNamespace(namespace);
                                    if (moduleName == null) {
                                        SchemaRegistry parentRegistry = schemaRegistry.getParentRegistry();
                                        if (parentRegistry != null) {
                                            moduleName = parentRegistry.getModuleNameByNamespace(namespace);
                                        }
                                    }
                                }

                            }
                            key = moduleName + COLON + keyValue;
                        }
                        jsonListKeyBuilder.append("[" + key + "]");
                        j = j + 2;
                    }
                    errorPathValue = jsonListKeyBuilder.toString();
                }
            }
            errorPath.append(moduleName + COLON + errorPathValue);
            iter++;
        }
        return errorPath.toString();
    }

    public static void convertToJSONObject(JSONObject parentJsonObject, Element xml, SchemaNode schemaNode,
                                           SchemaRegistry schemaRegistry, String currentModuleName,
                                           ModelNodeDataStoreManager modelNodeDSM) {
        boolean isTopLevel = (currentModuleName == null);
        Pair<String, String> jsonNameAndModuleName = getJsonNameAndModuleName(xml, currentModuleName, schemaRegistry);
        String jsonName = jsonNameAndModuleName.getFirst();
        if (jsonNameAndModuleName.getSecond() != null) {
            currentModuleName = jsonNameAndModuleName.getSecond();
            boolean isMountPointNode = false;
            if (schemaNode != null && AnvExtensions.MOUNT_POINT.isExtensionIn(schemaRegistry.getDataSchemaNode(schemaNode.getPath()))) {
                SchemaRegistry mountRegistry = lookupSchemaRegistry(schemaRegistry, xml, schemaNode.getPath());
                if (mountRegistry != null) {
                    isMountPointNode = true;
                    schemaRegistry = mountRegistry;
                }
            }
            if (currentModuleName.equals(IETF_NETCONF) && DATA_NODE.equals(xml.getLocalName())) {
                // top level
                currentModuleName = IETF_RESTCONF;
                JSONObject containerJSON = new JSONObject();
                parentJsonObject.put(currentModuleName + COLON + xml.getLocalName(), containerJSON);
                for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
                    Element currentElement = (Element)xml.getChildNodes().item(i);
                    QName currentElementQName = schemaRegistry.lookupQName(currentElement.getNamespaceURI(), currentElement.getLocalName());
                    convertToJSONObject(containerJSON, currentElement, ((DataNodeContainer)schemaNode)
                            .findDataChildByName(currentElementQName).orElse(null),
                            schemaRegistry, null, modelNodeDSM);
                }
            } else if (schemaNode instanceof ContainerSchemaNode) {
                JSONObject containerJSON = new JSONObject();
                parentJsonObject.put(jsonName, containerJSON);
                handleChildNodes(containerJSON, xml, schemaNode, schemaRegistry, currentModuleName, isMountPointNode, modelNodeDSM);
            } else if (schemaNode instanceof ActionDefinition) {
                if (c_appendActionInput.get()) {
                    ActionDefinition actionDef = (ActionDefinition) schemaNode;
                    ContainerSchemaNode actionInput = actionDef.getInput();
                    if (actionInput != null) {
                        JSONObject containerJSON = new JSONObject();
                        handleChildNodes(containerJSON, xml, actionInput, schemaRegistry, currentModuleName,
                                isMountPointNode, modelNodeDSM);
                        parentJsonObject.put(jsonName, containerJSON);
                    }
                }
            } else if (schemaNode instanceof ListSchemaNode) {
                // at top level we should not have []
                JSONObject listEltJSON = new JSONObject();
                boolean hasChildren = hasElementChildren(xml);
                if ((isTopLevel && !((ListSchemaNode) schemaNode).getKeyDefinition().isEmpty()) || !(hasChildren)) {
                    parentJsonObject.put(jsonName, listEltJSON);
                } else {
                    JSONArray listJSON = getOrCreateJSONArray(parentJsonObject, jsonName);
                    listJSON.put(listEltJSON);
                }
                handleChildNodes(listEltJSON, xml, schemaNode, schemaRegistry, currentModuleName, isMountPointNode, modelNodeDSM);
            } else if (schemaNode instanceof LeafSchemaNode) {
                parentJsonObject.put(jsonName, getValueForJson(xml, ((LeafSchemaNode)schemaNode).getType(), schemaRegistry,
                        currentModuleName, modelNodeDSM));
            } else if (schemaNode instanceof LeafListSchemaNode) {
                JSONArray leafListJSON = getOrCreateJSONArray(parentJsonObject, jsonName);
                leafListJSON.put(getValueForJson(xml, ((LeafListSchemaNode)schemaNode).getType(), schemaRegistry, currentModuleName,
                        modelNodeDSM));
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                handleChoiceChildNodes(parentJsonObject, xml, (ChoiceSchemaNode)schemaNode, schemaRegistry, currentModuleName,
                        modelNodeDSM);
            } else if (schemaNode instanceof AnyXmlSchemaNode) {
                handleAnyXmlSchemaNode(parentJsonObject, xml, jsonName);
            } else if (schemaNode instanceof RpcDefinition) {
                ContainerSchemaNode rpcInput = ((RpcDefinition)schemaNode).getInput();
                if (rpcInput != null) {
                    JSONObject containerJSON = new JSONObject();
                    handleChildNodes(containerJSON, xml, rpcInput, schemaRegistry, currentModuleName,
                            isMountPointNode, modelNodeDSM);
                    parentJsonObject.put(jsonName, containerJSON);
                }
            } else {
                LOGGER.warn(String.format("Missing handler for schemaNode %s, while converting xml to json", schemaNode));
            }
        }
    }

    public static SchemaRegistry lookupSchemaRegistry(SchemaRegistry registry, Element node, SchemaPath path) {
        if (registry.getParentRegistry() != null) {
            registry = registry.getParentRegistry();
        }
        SchemaMountRegistry mountRegistry = registry.getMountRegistry();
        SchemaMountRegistryProvider provider = mountRegistry.getProvider(path);
        return provider.getSchemaRegistry(node);
    }

    private static SchemaRegistry lookupSchemaRegistry(ModelNodeDataStoreManager dsm, String moduleName, SchemaNode urlTarget,
                                                       SchemaRegistry schemaRegistry, List<PathNodeValue> pnvs, Map<QName,
            Map<String, String>> nodeLeafs) {
        Optional<Module> module = schemaRegistry.getModule(moduleName);
        if (module == null || !module.isPresent()) {
            if (schemaRegistry.getParentRegistry() != null && pnvs.isEmpty()) {
                schemaRegistry = schemaRegistry.getParentRegistry();
            } else {
                SchemaRegistry mountRegistry = getMountRegistryForTheSchemaNodeKey(schemaRegistry, pnvs, nodeLeafs);
                if (mountRegistry != null) {
                    pnvs.clear();
                    schemaRegistry = mountRegistry;
                } else {
                    schemaRegistry = lookupMountedRegistry(dsm, pnvs, schemaRegistry);
                }
            }
        }
        return schemaRegistry;
    }

    private static boolean hasElementChildren(Element xml) {
        NodeList childNodes = xml.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    public static JSONArray getOrCreateJSONArray(JSONObject parentJsonObject, String jsonName) {
        JSONArray listJSON = (JSONArray)parentJsonObject.opt(jsonName);
        if (listJSON == null) {
            listJSON = new JSONArray();
            parentJsonObject.put(jsonName, listJSON);
        }
        return listJSON;
    }

    private static void handleChildNodes(JSONObject parentJsonObject, Element xml, SchemaNode schemaNode, SchemaRegistry schemaRegistry,
                                         String currentModuleName, boolean isMountPointNode, ModelNodeDataStoreManager modelNodeDSM) {
        NodeList childNodes = xml.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;
                SchemaNode childDSNode = SchemaRegistryUtil.getChildSchemaNode(childElement, schemaNode.getPath(), schemaRegistry);
                if (childDSNode == null) {
                    childDSNode = getActionNode(childElement, schemaNode.getPath(), schemaRegistry);
                }
                if (isMountPointNode && childDSNode == null) {
                    childDSNode = getRootNode(childElement, schemaRegistry);
                }
                convertToJSONObject(parentJsonObject, childElement, childDSNode, schemaRegistry, currentModuleName, modelNodeDSM);
            }
        }
    }

    private static SchemaNode getActionNode(Element childElement, SchemaPath path, SchemaRegistry schemaRegistry) {
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(path);
        if (dataSchemaNode instanceof ContainerSchemaNode) {
            for (ActionDefinition action : ((ContainerSchemaNode) dataSchemaNode).getActions()) {
                QName childQname = action.getQName();

                if (childElement.getLocalName().equals(childQname.getLocalName()) && childElement.getNamespaceURI()
                        .equals(childQname.getNamespace().toString())) {
                    return action;
                }
            }
        } else if (dataSchemaNode instanceof ListSchemaNode) {
            for (ActionDefinition action : ((ListSchemaNode) dataSchemaNode).getActions()) {
                QName childQname = action.getQName();
                if (childElement.getLocalName().equals(childQname.getLocalName()) && childElement.getNamespaceURI()
                        .equals(childQname.getNamespace().toString())) {
                    return action;
                }
            }
        }
        return null;
    }

    private static DataSchemaNode getRootNode(Element childElement, SchemaRegistry schemaRegistry) {
        for (DataSchemaNode rootNode : schemaRegistry.getRootDataSchemaNodes()) {
            if (rootNode.getQName().getNamespace().toString().equals(childElement.getNamespaceURI()) && rootNode.getQName()
                    .getLocalName().equals(childElement.getLocalName())) {
                return rootNode;
            }
        }
        return null;
    }

    private static Object getValueForJson(Element xml, TypeDefinition<?> type, SchemaRegistry schemaRegistry,
                                          String currentModuleName, ModelNodeDataStoreManager modelNodeDSM) {
        String textContent = getTextContent(xml);
        Object returnValue = getValueForJson(xml, textContent, type, schemaRegistry, currentModuleName, modelNodeDSM);
        if (returnValue == null) {
            returnValue = textContent;
        }
        return returnValue;
    }

    private static Object getValueForJson(Element xml, String textContent, TypeDefinition<?> type, SchemaRegistry schemaRegistry,
                                          String currentModuleName, ModelNodeDataStoreManager modelNodeDSM) {
        if (type instanceof Int8TypeDefinition
                || type instanceof Int16TypeDefinition
                || type instanceof Int32TypeDefinition
                || type instanceof Int64TypeDefinition
                || type instanceof Uint8TypeDefinition
                || type instanceof Uint16TypeDefinition
                || type instanceof Uint32TypeDefinition
                || type instanceof Uint64TypeDefinition
        ) {
            return getIntegerValueForJson(textContent, type, schemaRegistry, currentModuleName);
        } else if (type instanceof IdentityrefTypeDefinition) {
            String localName = textContent;
            String prefix = null;
            if (textContent.contains(COLON)) {
                String[] splitted = textContent.split(COLON);
                prefix = splitted[0];
                localName = splitted[1];
            }
            String namespace = xml.lookupNamespaceURI(prefix);
            Pair<String, String> jsonNameAndModuleName = getJsonNameAndModuleName(namespace, localName, currentModuleName, schemaRegistry);
            String localNameFirst = jsonNameAndModuleName.getFirst();
            if (!Strings.isNullOrEmpty(localNameFirst) && !localNameFirst.contains(COLON) && !Strings.isNullOrEmpty(prefix)) {
                return currentModuleName + COLON + localNameFirst;
            }
            return localNameFirst;
        } else if (type instanceof BooleanTypeDefinition) {
            Boolean value =  Boolean.parseBoolean(textContent);
            if (value != null && !value && textContent.equalsIgnoreCase("false")) {
                return value;
            } else if (value != null && value) {
                return value;
            }
        } else if (type instanceof UnionTypeDefinition) {
            List<TypeDefinition<?>> typeDefinitions = ((UnionTypeDefinition)type).getTypes();
            for (TypeDefinition<?> possibleType:typeDefinitions) {
                Object returnValue = getValueForJson(xml, textContent, possibleType, schemaRegistry, currentModuleName, modelNodeDSM);
                if (returnValue != null) {
                    return returnValue;
                }
            }
        } else if ((type instanceof InstanceIdentifierTypeDefinition) || (type instanceof SchemaNode)) {
            if (textContent.startsWith("/")) {
                List<PathNodeValue> pnvList = new ArrayList<>();
                String[] splitBySlash = textContent.split("/(?=[^\\]]*(\\[|$))");
                StringBuilder returnValue = new StringBuilder();
                StringBuilder prefix = new StringBuilder();
                for (int i = 0; i < splitBySlash.length; i++) {
                    if (splitBySlash[i].contains(COLON)) {
                        returnValue.append("/");
                        if (splitBySlash[i].contains("[")) {
                            String splitKeys[] = splitBySlash[i].split("[\\[\\]]");
                            splitBySlash[i] = splitKeys[0];
                            schemaRegistry = addContentAndGetSchemaRegistry(splitKeys[0], prefix, schemaRegistry, xml, pnvList,
                                    returnValue, false, modelNodeDSM);
                            int j = 1;
                            while (j < splitKeys.length) {
                                returnValue.append("[");
                                schemaRegistry = addContentAndGetSchemaRegistry(splitKeys[j], prefix, schemaRegistry, xml,
                                        pnvList, returnValue, true, modelNodeDSM);
                                returnValue.append("]");
                                j = j + 2;
                            }

                        } else {
                            schemaRegistry = addContentAndGetSchemaRegistry(splitBySlash[i], prefix, schemaRegistry, xml,
                                    pnvList, returnValue, false, modelNodeDSM);
                        }

                    }
                    else {
                        returnValue.append(splitBySlash[i]);
                    }
                }
                return returnValue;
            } else if (type instanceof SchemaNode) {
                StringBuilder returnValue = new StringBuilder();
                StringBuilder prefix = new StringBuilder();
                List<PathNodeValue> pnvList = new ArrayList<>();
                schemaRegistry = addContentAndGetSchemaRegistry(textContent, prefix, schemaRegistry, xml,
                        pnvList, returnValue, false, modelNodeDSM);
                return returnValue;
            }
        } else if (type instanceof EmptyTypeDefinition) {
            JSONArray array = new JSONArray();
            array.put(JSONObject.NULL);
            return array;
        }
        return null;
    }

    private static SchemaRegistry addContentAndGetSchemaRegistry(String content, StringBuilder prefix, SchemaRegistry schemaRegistry,
                                                                 Element xml, List<PathNodeValue> pnvList, StringBuilder returnValue,
                                                                 boolean isKey, ModelNodeDataStoreManager modelNodeDSM) {
        if (content.contains(":")) {
            String currentPrefix = "";
            String localName = "";
            String keyValue = "";
            if (!isKey) {
                currentPrefix = content.split(":")[0];
                localName = content.split(":")[1];
            } else {
                keyValue = content.split("=", -1)[1];
                localName = content.split("=", -1)[0];
                if (localName.contains(":")) {
                    currentPrefix = localName.split(":")[0];
                    localName = localName.split(":")[1];
                } else {
                    currentPrefix = prefix.toString();
                }
            }
            if (!currentPrefix.equals(prefix.toString())) {
                prefix.setLength(0);
                prefix.append(currentPrefix);
                String namespace = xml.lookupNamespaceURI(prefix.toString());
                Module module = schemaRegistry.getModuleByNamespace(namespace);
                if (module == null) {
                    if (schemaRegistry.getParentRegistry() == null) {
                        schemaRegistry = lookupMountedRegistry(modelNodeDSM, pnvList, schemaRegistry);
                        module = schemaRegistry.getModuleByNamespace(namespace);

                    } else {
                        module = schemaRegistry.getParentRegistry().getModuleByNamespace(namespace);

                    }
                }
                if (module != null) {
                    addPnv(namespace, module, prefix.toString(), pnvList, isKey, keyValue, localName);
                    returnValue.append(module.getName() + COLON + content.split(":")[1]);
                } else {
                    //workaround: ietf-interfaces is not part of the vOMCI NF adapter
                    if (namespace.equals(ONUConstants.IETF_INTERFACES_NS)) {
                        LOGGER.info("Using default mapping of " + namespace + " to module name: "
                                + ONUConstants.IETF_INTERFACES);
                        returnValue.append(ONUConstants.IETF_INTERFACES + COLON + content.split(":")[1]);
                    } else {
                        LOGGER.error("Could not find module for namespace: " + namespace);
                    }
                }

            } else {
                addPnvForChild(content, localName, keyValue, pnvList, isKey, returnValue);
            }
        } else {
            if (content.contains("=")) {
                String keyValue = content.split("=", -1)[1];
                String keyName = content.split("=", -1)[0];
                addPnvForChild(content, keyName, keyValue, pnvList, isKey, returnValue);
            }
            else {
                returnValue.append(content);
            }
        }
        return schemaRegistry;
    }

    private static void addPnv(String namespace, Module module, String prefix, List<PathNodeValue> pnvList,
                               boolean isKey, String keyValue, String name) {
        QName qname = QName.create(namespace, name, module.getRevision().get());
        if (!isKey) {
            QNameWithModuleInfo qnameWithPrefix = new QNameWithModuleInfo(qname, prefix, module.getName(), module.getRevision()
                    .get().toString());
            PathNodeValue pnv = new PathNodeValue(qnameWithPrefix);
            pnvList.add(pnv);
        } else if (!pnvList.isEmpty()) {
            PathNodeValue pnv = pnvList.get(pnvList.size() - 1);
            pnv.addPathKey(qname, new ValueObject(keyValue));
        }

    }

    private static void addPnvForChild(String content, String keyName, String keyValue, List<PathNodeValue> pnvList,
                                       boolean isKey, StringBuilder returnValue) {
        PathNodeValue pnv = pnvList.get(pnvList.size() - 1);
        QNameWithModuleInfo parentQnameWithPrefix = pnv.getQNameWithPrefix();
        if (!isKey) {
            returnValue.append(content.split(":")[1]);
            QNameWithModuleInfo qnameWithPrefix = new QNameWithModuleInfo(
                    QName.create(parentQnameWithPrefix.getNamespace().toString(),parentQnameWithPrefix.getRevision(),
                            content.split(":")[1]), parentQnameWithPrefix.getModulePrefix(),
                    pnv.getQualifiedName(), parentQnameWithPrefix.getRevision());
            pnv = new PathNodeValue(qnameWithPrefix);
            pnvList.add(pnv);
        } else {
            returnValue.append(keyName + "=" + keyValue);
            if (keyValue.endsWith("'") && keyValue.startsWith("'")) {
                keyValue = keyValue.substring(1, keyValue.length() - 1);
            }
            pnv.addPathKey(QName.create(parentQnameWithPrefix.getNamespace().toString(),
                    parentQnameWithPrefix.getRevision(), keyName),
                    new ValueObject(keyValue));

        }
    }

    private static boolean isEmptyLeafToBeIgnored(String textContent) {
        if (textContent == null || textContent.isEmpty()) {
            return c_ignoreEmptyLeaves.get();
        }
        return false;
    }

    private static Object getIntegerValueForJson(String textContent, TypeDefinition<?> type, SchemaRegistry schemaRegistry,
                                                 String currentModuleName) {
        if (isEmptyLeafToBeIgnored(textContent)) {
            return null;
        }
        if (!textContent.matches(INTEGER_TYPE) && !textContent.matches(HEX_INT_TYPE)) {
            return null;
        }

        if (textContent.startsWith("0x") || textContent.startsWith("-0x")) {
            textContent = textContent.replaceFirst("0x", "");
            return Long.valueOf(textContent, 16);
        }
        else if ((textContent.startsWith("0") && textContent.length() > 1)
                || (textContent.startsWith("-0") && textContent.length() > 2)) {
            textContent = textContent.replaceFirst("0", "");
            return Long.valueOf(textContent, 8);
        }
        else {
            return Long.valueOf(textContent);
        }
    }

    private static void handleChoiceChildNodes(JSONObject parentJsonObject, Element dataNode, ChoiceSchemaNode node,
                                               SchemaRegistry schemaRegistry, String currentModuleName,
                                               ModelNodeDataStoreManager modelNodeDSM) {
        for (CaseSchemaNode caseNode : node.getCases().values()) {
            for (DataSchemaNode childSchemaNode : caseNode.getChildNodes()) {
                QName childQname = childSchemaNode.getQName();
                if (dataNode.getLocalName().equals(childQname.getLocalName()) && dataNode.getNamespaceURI()
                        .equals(childQname.getNamespace().toString())) {
                    convertToJSONObject(parentJsonObject, dataNode, childSchemaNode, schemaRegistry, currentModuleName, modelNodeDSM);
                }
            }

        }
    }

    private static void handleAnyXmlSchemaNode(JSONObject parentJsonObject, Element xml, String jsonName) {
        try {
            List<Element> children = getElementChildren(xml);
            if (children.size() > 1) {
                JSONArray anyXmlJson = new JSONArray();
                parentJsonObject.put(jsonName, anyXmlJson);
                for (Element childElement: children) {
                    anyXmlJson.put(DocumentUtils.documentToString(childElement));
                }
            }
            else if (children.size() == 1) {
                parentJsonObject.put(jsonName, DocumentUtils.documentToString(children.get(0)));
            }
            else {
                parentJsonObject.put(jsonName, getTextContent(xml));
            }
        } catch (Exception e) {
            LOGGER.warn("Error converting anyxml node to a string", e);
        }
    }

    private static String getTextContent(Element xml) {
        Node firstChild = xml.getFirstChild();
        return firstChild == null ? "" : firstChild.getTextContent();
    }

    private static List<Element> getElementChildren(Element xml) {
        List<Element> children = new ArrayList<>();
        NodeList childNodes = xml.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element)childNode);
            }
        }
        return children;
    }

    public static Element convertFromJsonToXml(ModelNodeDataStoreManager dsm, Pair<String, OperationDefinition> pair,
                                               List<PathNodeValue> pnvs, String jsonInput, SchemaRegistry schemaRegistry,
                                               String namespace, String prefix) throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.createDocument();
        JSONObject obj = new JSONObject(jsonInput);
        List<String> orderedKeys = getOrderedKeys(obj, new ArrayList<>());
        appendChildren(dsm, pair, new LinkedList<>(pnvs), orderedKeys, obj, doc, schemaRegistry, null, namespace,
                prefix, new HashMap<>());
        return doc.getDocumentElement();
    }

    private static List<String> getOrderedKeys(JSONObject obj, List<String> yangOrderedKeys) {
        List<String> keys = new LinkedList<>();
        for (String key : yangOrderedKeys) {
            keys.add(key);
        }
        List<JsonKey> jsonKeys = new ArrayList<>();
        for (String key : obj.keySet()) {
            if (!keys.contains(key)) {
                jsonKeys.add(new JsonKey(key, obj.get(key)));
            }
        }
        Collections.sort(jsonKeys);
        for (JsonKey jsonKey : jsonKeys) {
            keys.add(jsonKey.getJsonKey());
        }
        return keys;
    }

    private static void appendChildren(ModelNodeDataStoreManager dsm, Pair<String, OperationDefinition> pair, List<PathNodeValue> pnvs,
                                       List<String> keys, JSONObject jsonObj, Document doc, SchemaRegistry schemaRegistry,
                                       Element parent, String namespace, String prefix, Map<QName, Map<String, String>> nodeLeafs) {
        for (String key : keys) {
            String actualPrefix = prefix;
            String originalNamespace = namespace;
            if (key.contains(COLON)) {
                String moduleName = key.split(COLON)[0];
                if (schemaRegistry.getNamespaceOfModule(moduleName) == null) {
                    schemaRegistry = lookupSchemaRegistry(dsm, moduleName, null, schemaRegistry, pnvs, nodeLeafs);
                }
            }
            Module module = schemaRegistry.getModuleByNamespace(namespace);
            String elementName = null;
            String localName = null;
            if (key.contains(COLON)) {
                String[] keyStrings = key.split(COLON);
                String moduleName = keyStrings[0];
                namespace = schemaRegistry.getNamespaceOfModule(moduleName);
                if (namespace != null) {
                    module = schemaRegistry.getModuleByNamespace(namespace);
                    prefix = schemaRegistry.getPrefix(namespace);
                }
                localName = keyStrings[1];
                elementName = prefix + COLON + keyStrings[1];
            } else {
                localName = key;
                elementName = prefix + COLON + key;
            }
            if (jsonObj.keySet().contains(key)) {
                Object child = jsonObj.get(key);
                if (child instanceof JSONArray) {
                    List<PathNodeValue> orderedPNVs = new LinkedList<>(pnvs);
                    JSONArray jsonArray = (JSONArray) child;
                    for (int index = 0; index < jsonArray.length(); index++) {
                        Object obj = jsonArray.get(index);
                        Element childElement = createChild(doc, parent, elementName, namespace);
                        if (obj instanceof JSONObject) {
                            appendOrderedJsonChildElements(dsm, pair, doc, schemaRegistry, namespace, prefix, module,
                                    localName, obj, orderedPNVs, childElement, nodeLeafs);
                        } else {
                            if (!JSONObject.NULL.equals(obj)) {
                                String value = obj.toString();
                                appendJsonValueElement(doc, schemaRegistry, namespace, elementName, value, childElement);
                            }
                        }
                    }
                } else if (child instanceof JSONObject) {
                    List<PathNodeValue> orderedPNVs = new LinkedList<>(pnvs);
                    Element childElement = createChild(doc, parent, elementName, namespace);
                    appendOrderedJsonChildElements(dsm, pair, doc, schemaRegistry, namespace, prefix, module, localName,
                            child, orderedPNVs, childElement, nodeLeafs);
                } else {
                    if (jsonObj.isNull(key)) {
                        createChild(doc, parent, elementName, namespace);
                    } else {
                        String value = jsonObj.get(key).toString();
                        if (! pnvs.isEmpty()) {
                            PathNodeValue lastPnv = ((LinkedList<PathNodeValue>)pnvs).getLast();
                            QName listKey = QName.create(namespace, localName);
                            Map<QName, ValueObject> pathKeys = lastPnv.getPathKeys();
                            for (Map.Entry<QName, ValueObject> entry : pathKeys.entrySet()) {
                                QName pathKey = entry.getKey();
                                if (pathKey.getNamespace().equals(listKey.getNamespace())
                                        && pathKey.getLocalName().equals(listKey.getLocalName())) {
                                    entry.setValue(new ValueObject(value));
                                }

                            }
                        }
                        if (parent != null) {
                            String nodeName = parent.getLocalName();
                            String nameNs = parent.getNamespaceURI();
                            QName nodeQName = QName.create(nameNs, nodeName);
                            Map<String, String> leafsMap = nodeLeafs.get(nodeQName);
                            if (leafsMap == null) {
                                leafsMap = new HashMap<>();
                                nodeLeafs.put(nodeQName, leafsMap);
                            }
                            leafsMap.put(key, value);
                        }
                        Element childElement = createChild(doc, parent, elementName, namespace);
                        appendJsonValueElement(doc, schemaRegistry, namespace, elementName, value, childElement);
                    }

                }
            }
            prefix = actualPrefix;
            namespace = originalNamespace;
        }
    }

    private static void appendOrderedJsonChildElements(ModelNodeDataStoreManager dsm, Pair<String, OperationDefinition> pair, Document doc,
                                                       SchemaRegistry schemaRegistry, String namespace, String prefix, Module module,
                                                       String localName, Object child, List<PathNodeValue> orderedPNVs,
                                                       Element childElement, Map<QName, Map<String, String>> nodeLeafs) {
        JSONObject childJsonObj = (JSONObject) child;
        QNameWithModuleInfo qname = createQNameWithPrefix(module, localName);
        DataSchemaNode node = null;
        if (node == null) {
            node = fillPNVAndRetrieveSchemaNode(pair, schemaRegistry, orderedPNVs, qname);
        }
        appendInnerChildren(dsm, pair, doc, schemaRegistry, namespace, prefix, localName, orderedPNVs,
                childElement, childJsonObj, node, nodeLeafs);
    }

    private static DataSchemaNode fillPNVAndRetrieveSchemaNode(Pair<String, OperationDefinition> pair, SchemaRegistry schemaRegistry,
                                                               List<PathNodeValue> orderedPNVs, QNameWithModuleInfo qname) {
        DataSchemaNode node = null;
        if (pair.getSecond() != null) {
            OperationDefinition operationDef = pair.getSecond();
            if (operationDef.getQName().equals(qname.getQName())) {
                node = operationDef.getInput();
            }
        } else {
            if (isQNameAChild(orderedPNVs, qname, schemaRegistry)) {
                PathNodeValue pnv = new PathNodeValue(qname);
                orderedPNVs.add(pnv);
            }
            node = getDataSchemaNode(orderedPNVs, schemaRegistry);
        }
        return node;
    }

    private static boolean isQNameAChild(List<PathNodeValue> orderedPNVs, QNameWithModuleInfo qname,
                                         SchemaRegistry schemaRegistry) {
        if (orderedPNVs.isEmpty()) {
            return true;
        } else {
            DataSchemaNode node = getDataSchemaNode(orderedPNVs, schemaRegistry);
            if (node instanceof ContainerSchemaNode || node instanceof ListSchemaNode) {
                List<DataSchemaNode> childList = new ArrayList<DataSchemaNode>();
                childList.addAll(schemaRegistry.getChildren(node.getPath()));
                for (DataSchemaNode childNode : childList) {
                    if (childNode.getQName().equals(qname.getQName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static DataSchemaNode getDataSchemaNode(List<PathNodeValue> pnvs, SchemaRegistry schemaRegistry) {
        List<QName> paths = getQNamesFromPnvs(pnvs);
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(paths);
        return node;
    }

    private static List<QName> getQNamesFromPnvs(List<PathNodeValue> pnvs) {
        List<QName> paths = new ArrayList<>();
        for (PathNodeValue pathNodeValue : pnvs) {
            paths.add(pathNodeValue.getQNameWithPrefix().getQName());
        }
        return paths;
    }

    private static void appendInnerChildren(ModelNodeDataStoreManager dsm, Pair<String, OperationDefinition> pair, Document doc,
                                            SchemaRegistry schemaRegistry, String namespace, String prefix, String localName,
                                            List<PathNodeValue> orderedPNVs, Element childElement, JSONObject childJsonObj,
                                            DataSchemaNode node, Map<QName, Map<String, String>> nodeLeafs) {
        if (node == null) {
            throw new RuntimeException(NetconfRpcErrorTag.UNKNOWN_ELEMENT.value());
        }
        if (node instanceof ListSchemaNode) {
            List<QName> keyDefinition = ((ListSchemaNode) node).getKeyDefinition();
            if (! orderedPNVs.isEmpty()) {
                PathNodeValue lastPnv = ((LinkedList<PathNodeValue>)orderedPNVs).getLast();
                for (QName key : keyDefinition) {
                    if (! lastPnv.getPathKeys().containsKey(key)) {
                        lastPnv.addPathKey(key, null);
                    }
                }
            }
            appendChildren(dsm, pair, orderedPNVs, getOrderedKeys(childJsonObj, getKeysFromQName(keyDefinition)), childJsonObj,
                    doc, schemaRegistry, childElement, namespace, prefix, nodeLeafs);
        } else {
            appendChildren(dsm, pair, orderedPNVs, getOrderedKeys(childJsonObj, new ArrayList<>()), childJsonObj, doc, schemaRegistry,
                    childElement, namespace, prefix, nodeLeafs);
        }
    }

    private static List<String> getKeysFromQName(List<QName> keyDefs) {
        List<String> keys = new LinkedList<>();
        for (QName qname : keyDefs) {
            keys.add(qname.getLocalName());
        }
        return keys;
    }

    public static QNameWithModuleInfo createQNameWithPrefix(Module module, String localName) {
        String revision = null;
        if (module != null && module.getQNameModule().getRevision().isPresent()) {
            revision = module.getQNameModule().getRevision().get().toString();
        }
        return QNameWithModuleInfo.create(module.getNamespace().toString(), revision, localName,
                module.getPrefix(), module.getName());
    }

    protected static Element createChild(Document doc, Element parent, String elementName, String ns) {
        Element childElement = doc.createElementNS(ns, elementName);
        if (parent == null) {
            doc.appendChild(childElement);
        } else {
            parent.appendChild(childElement);
        }
        return childElement;
    }

    private static SchemaRegistry getMountRegistryForTheSchemaNodeKey(SchemaRegistry schemaRegistry, List<PathNodeValue> pnvs,
                                                                      Map<QName, Map<String, String>> nodeLeafs) {
        List<QName> paths = new ArrayList<>();
        SchemaPath path = null;
        for (PathNodeValue pathNodeValue : pnvs) {
            paths.add(pathNodeValue.getQNameWithPrefix().getQName());
            path = SchemaPath.create(paths, true);
        }
        SchemaMountRegistry schemaMountRegistry = schemaRegistry.getMountRegistry();
        if (schemaMountRegistry != null) {
            SchemaMountRegistryProvider provider = schemaMountRegistry.getProvider(path);
            if (provider != null) {
                SchemaMountKey schemaMountKey = provider.getSchemaMountKey();
                QName qName = schemaMountKey.getNodeQName();
                for (Map.Entry<QName, Map<String, String>> entry : nodeLeafs.entrySet()) {
                    QName key = entry.getKey();
                    if (key.getNamespace().toString().equals(qName.getNamespace().toString())
                            && key.getLocalName().equals(qName.getLocalName())) {
                        Map<String, String> leafValues = nodeLeafs.get(key);
                        if (leafValues != null) {
                            SchemaRegistry mountedRegistry = provider.getSchemaRegistry(leafValues);
                            if (mountedRegistry != null) {
                                pnvs.clear();
                            }
                            return mountedRegistry;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static SchemaRegistry lookupMountedRegistry(ModelNodeDataStoreManager dsm, List<PathNodeValue> pnvs,
                                                       SchemaRegistry globalRegistry) {
        ModelNodeId parentId = new ModelNodeId();
        List<QName> paths = new ArrayList<>();
        SchemaPath path = null;
        ModelNode modelNode = null;
        for (PathNodeValue pathNodeValue : pnvs) {
            paths.add(pathNodeValue.getQNameWithPrefix().getQName());
            path = SchemaPath.create(paths, true);
            ModelNodeKeyBuilder modelNodeKeyBuilder = new ModelNodeKeyBuilder();
            for (Map.Entry<QName, ValueObject> entry : pathNodeValue.getPathKeys().entrySet()) {
                modelNodeKeyBuilder.appendKey(entry.getKey(), entry.getValue().getStringValue());
            }
            modelNode = dsm.findNode(path, modelNodeKeyBuilder.build(), parentId, globalRegistry);
            parentId = modelNode.getModelNodeId();
        }

        SchemaRegistry mountedRegistry = globalRegistry;
        SchemaMountRegistry schemaMountRegistry = globalRegistry.getMountRegistry();
        if (schemaMountRegistry != null) {
            SchemaMountRegistryProvider mountedPathProvider = schemaMountRegistry.getProvider(path);
            if (mountedPathProvider != null) {
                if (modelNode != null) {
                    mountedRegistry = mountedPathProvider.getSchemaRegistry(modelNode.getModelNodeId());
                    if (mountedRegistry != null) {
                        pnvs.clear();
                    }
                }
            }
        }
        return mountedRegistry;
    }

    private static void appendJsonValueElement(Document doc, SchemaRegistry schemaRegistry, String namespace,
                                               String elementName, String value, Element childElement) {
        Pair<Module,String> moduleValuePair = checkAndGetModuleValuePairForIdentity(schemaRegistry, value, namespace, elementName);
        setElementNSForIdentity(namespace, childElement, moduleValuePair);
        childElement.appendChild(doc.createTextNode(moduleValuePair.getSecond()));
    }

    private static void  setElementNSForIdentity(String namespace, Element childElement,
                                                 Pair<Module, String> moduleValuePair) {
        Module module = moduleValuePair.getFirst();
        if (module != null) {
            childElement.setAttributeNS(PojoToDocumentTransformer.XMLNS_NAMESPACE, XMLNS + module.getPrefix(),
                    module.getNamespace().toString());
        } else if (moduleValuePair.getSecond().contains(COLON)) {
            if (moduleValuePair.getSecond().split(COLON).length > 0) {
                childElement.setAttributeNS(PojoToDocumentTransformer.XMLNS_NAMESPACE, XMLNS
                        + moduleValuePair.getSecond().split(COLON)[0], namespace);
            }
        }
    }

    private static Pair<Module,String> checkAndGetModuleValuePairForIdentity(SchemaRegistry schemaRegistry, String value,
                                                                             String namespace, String elementName) {
        Module module = null;
        if (value.contains(COLON)) {
            String values[] = value.split(COLON);
            if (values.length > 0) {
                String moduleName = values[0];
                Optional<Module> optModule = schemaRegistry.getModule(moduleName);
                if (optModule == null || !optModule.isPresent()) {
                    module = schemaRegistry.getModuleByNamespace(namespace);
                } else {
                    module = optModule.get();
                    if (values.length > 1) {
                        value = optModule.get().getPrefix() + COLON + values[1];
                    } else {
                        value = optModule.get().getPrefix() + COLON;
                    }
                }
            }
        } else {
            module = schemaRegistry.getModuleByNamespace(namespace);
            if (elementName.contains(COLON)) {
                elementName = elementName.split(COLON)[1];
            }
            for (IdentitySchemaNode identityRef : module.getIdentities()) {
                if (identityRef.getQName().getNamespace().toString().equals(namespace)
                        && value.equals(identityRef.getQName().getLocalName())) {
                    value = module.getPrefix() + COLON + value;

                    module = null;
                }
            }
        }
        return new Pair<Module, String>(module, value);
    }

    public abstract static class WithIgnoreEmptyLeavesTemplate<RT> {
        protected abstract RT execute();

        final RT executeInternal() throws RequestScope.RsTemplate.RequestScopeExecutionException {
            c_ignoreEmptyLeaves.set(true);
            try {
                return execute();
            } finally {
                c_ignoreEmptyLeaves.set(false);
            }
        }
    }

}