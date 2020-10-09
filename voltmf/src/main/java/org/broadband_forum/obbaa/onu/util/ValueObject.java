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

import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class ValueObject {

    private final Object m_value;

    public ValueObject(Object value) {
        m_value = value;
    }

    private static boolean isInstanceIdentifier(TypeDefinition<?> type, String restconfValue,
                                                SchemaRegistry schemaRegistry, DataSchemaNode leafOrLeafListNode) {
        if (type instanceof InstanceIdentifierTypeDefinition) {
            return true;
        } else if (type instanceof UnionTypeDefinition) {
            List<TypeDefinition<?>> derivedTypes = ((UnionTypeDefinition) type).getTypes();
            for (TypeDefinition<?> derivedType : derivedTypes) {
                if (derivedType instanceof InstanceIdentifierTypeDefinition) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addAllDerivedIdentities(Set<IdentitySchemaNode> identities, Set<IdentitySchemaNode> allIdentitiesSet) {
        for (IdentitySchemaNode identity : identities) {
            addAllDerivedIdentities(identity.getDerivedIdentities(), allIdentitiesSet);
            allIdentitiesSet.add(identity);
        }
    }

    private static TypeDefinition<?> getNodeType(DataSchemaNode leafOrLeafListNode) {
        TypeDefinition<?> type;
        if (leafOrLeafListNode instanceof LeafSchemaNode) {
            type = ((LeafSchemaNode) leafOrLeafListNode).getType();
        }
        else if (leafOrLeafListNode instanceof LeafListSchemaNode) {
            type = ((LeafListSchemaNode) leafOrLeafListNode).getType();
        }
        else {
            throw new RuntimeException("Unexpected node type: " + leafOrLeafListNode.getClass());
        }
        return type;
    }

    public Object getValue() {
        return m_value;
    }

    public String getStringValue() {
        if (m_value instanceof String) {
            return (String)m_value;
        } else if (m_value instanceof List<?>) {
            StringBuilder builder = new StringBuilder();
            @SuppressWarnings("unchecked")
            List<PathNodeValue> pnvList = (List<PathNodeValue>) m_value;
            for (PathNodeValue pnv : pnvList) {
                builder.append(pnv.getQualifiedName());
            }
            return builder.toString();
        } else {
            throw new RuntimeException("Unhandled key or leaf list value type: " + m_value.getClass());
        }
    }

    public String toString() {
        return getStringValue();
    }
}
