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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getChildElements;
import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil.getSchemaNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

public class DeviceSubsystemResponseUtil {
    private final SchemaRegistry m_schemaRegistry;

    public DeviceSubsystemResponseUtil(SchemaRegistry schemaRegistry) {
        m_schemaRegistry = schemaRegistry;
    }

    public Map<ModelNodeId, List<Element>> getStateResponse(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes, Element
            dataElement) {
        Map<ModelNodeId, List<Element>> response = new HashMap<>();
        for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()) {
            List<Element> childElements = getChildElements(dataElement);
            Element matchingNode = null;
            ModelNodeId nodeId = entry.getKey();
            for (Element rootNode : childElements) {
                matchingNode = getMatchingNode(nodeId, rootNode);
                if (matchingNode != null) {
                    break;
                }
            }
            if (matchingNode != null) {
                fillResponseForNode(entry, matchingNode, response);
            }

        }
        return response;
    }

    private void fillResponseForNode(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry, Element matchingNode, Map
            <ModelNodeId, List<Element>> response) {
        List<Element> responseElements = new ArrayList<>();
        List<QName> stateLeaves = entry.getValue().getFirst();
        for (QName stateLeaf : stateLeaves) {
            List<Element> children = getChildElements(matchingNode, stateLeaf.getLocalName(), stateLeaf.getNamespace().toString());
            responseElements.addAll(children);
        }

        List<FilterNode> filterNodes = entry.getValue().getSecond();
        for (FilterNode filterNode : filterNodes) {
            List<Element> children = getChildElements(matchingNode, filterNode.getNodeName(), filterNode.getNamespace());
            responseElements.addAll(children);
        }
        response.put(entry.getKey(), responseElements);
    }

    private Element getMatchingNode(ModelNodeId nodeId, Element rootNode) {
        List<ModelNodeRdn> rdns = nodeId.getRdns();
        ModelNodeId subNodeId = new ModelNodeId();
        Element currentNode = rootNode;
        MutableIndex currentRdnIndex = new MutableIndex(0);
        return getMatchingNode(rdns, currentRdnIndex, subNodeId, currentNode);
    }

    private Element getMatchingNode(List<ModelNodeRdn> rdns, MutableIndex currentRdnIndex, ModelNodeId subNodeId, Element currentNode) {
        Element matchingNode = null;
        int rdnsLength = rdns.size();
        while (currentRdnIndex.getIndex() < rdnsLength) {
            ModelNodeRdn rdn = rdns.get(currentRdnIndex.getIndex());
            currentRdnIndex.increment();
            subNodeId.addRdn(rdn);
            if (nodesMatch(currentNode, rdn)) {
                DataSchemaNode sn = getSchemaNode(m_schemaRegistry, subNodeId);
                if (sn instanceof ListSchemaNode) {
                    boolean keysMatch = doKeysMatch(rdns, currentRdnIndex, (ListSchemaNode) sn, currentNode);
                    if (!keysMatch) {
                        return null;
                    }
                }
                matchingNode = currentNode;
            } else {
                break;
            }
            if (currentRdnIndex.getIndex() < rdnsLength) {
                List<Element> currentNodeChildren = getChildElements(currentNode);
                for (Element currentNodeChild : currentNodeChildren) {
                    MutableIndex currentRdnIndexCopy = new MutableIndex(currentRdnIndex);
                    ModelNodeId subNodeIdCopy = new ModelNodeId(subNodeId);
                    matchingNode = getMatchingNode(rdns, currentRdnIndexCopy, subNodeIdCopy, currentNodeChild);
                    if (matchingNode != null) {
                        currentRdnIndex = currentRdnIndexCopy;
                        subNodeId = subNodeIdCopy;
                        break;
                    }
                }
            }
        }
        return matchingNode;
    }

    private boolean doKeysMatch(List<ModelNodeRdn> rdns, MutableIndex currentRdnIndex, ListSchemaNode listNode, Element listDataNode) {
        int keyLength = listNode.getKeyDefinition().size();
        for (int i = 0; i < keyLength; i++) {
            if (currentRdnIndex.getIndex() < rdns.size()) {
                ModelNodeRdn keyRdn = rdns.get(currentRdnIndex.getIndex());
                currentRdnIndex.increment();
                List<Element> possibleKeys = getChildElements(listDataNode, keyRdn.getRdnName(), keyRdn.getNamespace());
                if (!possibleKeys.isEmpty()) {
                    Element key = possibleKeys.get(0);
                    if (!keyMatches(keyRdn, key)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean keyMatches(ModelNodeRdn keyRdn, Element key) {
        return keyRdn.getRdnValue().equals(key.getTextContent());
    }


    private boolean nodesMatch(Element node, ModelNodeRdn modelNodeRdn) {
        return node.getNamespaceURI().equals(modelNodeRdn.getNamespace()) && node.getLocalName().equals(modelNodeRdn.getRdnValue());
    }

    public class MutableIndex {
        int m_index;

        public MutableIndex(MutableIndex index) {
            m_index = index.getIndex();
        }

        public MutableIndex(int index) {
            m_index = index;
        }

        public int getIndex() {
            return m_index;
        }

        public int increment() {
            return m_index++;
        }

        @Override
        public String toString() {
            return String.valueOf(m_index);

        }
    }
}
