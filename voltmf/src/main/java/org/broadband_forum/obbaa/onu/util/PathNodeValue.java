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

import java.util.LinkedHashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class PathNodeValue {

    private static final String SLASH = "/";
    private static final String EQUAL = "=";
    private static final String COLON = ":";
    private static final String QUOTE = "'";

    private static final String OPEN = "[";
    private static final String CLOSE = "]";
    private final QNameWithModuleInfo m_qname;
    private final Map<QName, ValueObject> m_pathKeys = new LinkedHashMap<>();
    private ValueObject m_leafListValue;
    private ModelNodeId m_modelNodeId;
    private SchemaNode m_schemaNode;

    public PathNodeValue(QNameWithModuleInfo qname) {
        m_qname = qname;
    }

    public QNameWithModuleInfo getQNameWithPrefix() {
        return m_qname;
    }

    public Map<QName, ValueObject> getPathKeys() {
        return m_pathKeys;
    }

    public ValueObject getLeafListValue() {
        return m_leafListValue;
    }

    public void setLeafListValue(ValueObject leafListValue) {
        m_leafListValue = leafListValue;
    }

    public void addPathKey(QName qname, ValueObject value) {
        m_pathKeys.put(qname, value);
    }

    public ModelNodeId getModelNodeId() {
        return m_modelNodeId;
    }

    public void setModelNodeId(ModelNodeId modelNodeId) {
        this.m_modelNodeId = modelNodeId;
    }

    public SchemaNode getSchemaNode() {
        return m_schemaNode;
    }

    public void setSchemaNode(SchemaNode schemaNode) {
        this.m_schemaNode = schemaNode;
    }

    @Override
    public String toString() {
        return "PathNodeValue [m_qname=" + m_qname + ", m_pathKeys=" + m_pathKeys + ", m_leafListValue=" + m_leafListValue + "]";
    }

    public String getQualifiedName() {
        StringBuilder builder = new StringBuilder();
        builder.append(SLASH + getQNameWithPrefix().getQualifiedName());
        if (! this.getPathKeys().isEmpty()) {
            for (Map.Entry<QName, ValueObject> entry :  this.getPathKeys().entrySet()) {
                builder.append(OPEN);
                builder.append(getQNameWithPrefix().getModulePrefix() + COLON + entry.getKey().getLocalName());
                builder.append(EQUAL);
                builder.append(QUOTE + entry.getValue().getStringValue() + QUOTE);
                builder.append(CLOSE);
            }
        }
        return builder.toString();
    }
}
