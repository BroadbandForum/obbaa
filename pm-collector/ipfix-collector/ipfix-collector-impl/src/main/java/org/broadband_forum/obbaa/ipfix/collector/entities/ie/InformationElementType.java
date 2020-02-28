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

package org.broadband_forum.obbaa.ipfix.collector.entities.ie;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.broadband_forum.obbaa.ipfix.collector.exception.MissingAbstractDataTypeException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;

import com.google.common.base.Strings;

public enum InformationElementType {
    UNSIGNED8("unsigned8", 1, Integer.class, 1, Uint8TypeDefinition.class),
    UNSIGNED16("unsigned16", 2, Integer.class, 2, Uint16TypeDefinition.class),
    UNSIGNED32("unsigned32", 4, Long.class, 3, Uint32TypeDefinition.class),
    UNSIGNED64("unsigned64", 8, Long.class, 4, Uint64TypeDefinition.class),
    SIGNED8("signed8", 1, Integer.class, 5, Int8TypeDefinition.class),
    SIGNED16("signed16", 2, Integer.class, 6, Int16TypeDefinition.class),
    SIGNED32("signed32", 4, Long.class, 7, Int32TypeDefinition.class),
    SIGNED64("signed64", 8, Long.class, 8, Int64TypeDefinition.class),
    FLOAT64("float64", 8, Long.class, 10, DecimalTypeDefinition.class),
    BOOLEAN("boolean", 1, Boolean.class, 11, BooleanTypeDefinition.class),
    MACADDRESS("macAddress", 6, String.class, 12),
    STRING("string", 4, String.class, 13, StringTypeDefinition.class),
    DATATIMESECONDS("dateTimeSeconds", 4, Date.class, 14),
    IPV4ADDRESS("ipv4Address", 4, String.class, 18),
    IPV6ADDRESS("ipv6Address", 16, String.class, 19),
    ENUMERATION("enumeration", 4, String.class, 254, EnumTypeDefinition.class),
    UNION("union", 5, Object.class, 255, UnionTypeDefinition.class),
    OCTETARRAY("octetArray", 5, String.class, 0),
    BASICLIST("basicList", 5, String.class, 20),
    SUBTEMPLATELIST("subTemplateList", 5, String.class, 21),
    SUBTEMPLATEMULTILIST("subTemplateMultiList", 5, String.class, 22);

    private String m_key;
    private int m_numberOfOctets;
    private Class<?> m_type;
    private Class<? extends TypeDefinition> m_typeDefinition;
    private int m_ianaDataTypeCode;

    InformationElementType(String key, int numberOfOctets, Class<?> type, int ianaDataTypeCode) {
        m_key = key;
        m_numberOfOctets = numberOfOctets;
        m_type = type;
        m_ianaDataTypeCode = ianaDataTypeCode;
    }

    InformationElementType(String key, int numberOfOctets, Class<?> type, int ianaDataTypeCode,
                           Class<? extends TypeDefinition> typeDefinition) {
        this(key, numberOfOctets, type, ianaDataTypeCode);
        this.m_typeDefinition = typeDefinition;
    }

    public static InformationElementType getEnum(String key) throws MissingAbstractDataTypeException {
        if (!Strings.isNullOrEmpty(key)) {
            return Arrays.stream(values()).filter(ieType -> ieType.getKey().equals(key)).findFirst()
                    .orElseThrow(() -> new MissingAbstractDataTypeException("Cannot found abstract data type"));
        }
        throw new MissingAbstractDataTypeException("Cannot found abstract data type");
    }

    public static Optional<InformationElementType> find(Object value) {
        if (Objects.nonNull(value)) {
            if (value instanceof String) {
                return Arrays.stream(values()).filter(ieType -> ieType.getType().equals(value)).findFirst();
            }
            if (value instanceof TypeDefinition) {
                return Arrays.stream(values()).filter(ieType -> ieType.getTypeDefinition().isInstance(value)).findFirst();
            }
        }
        return Optional.empty();
    }


    public String getKey() {
        return m_key;
    }

    public void setKey(String key) {
        this.m_key = key;
    }

    public int getNumberOfOctets() {
        return m_numberOfOctets;
    }

    public void setNumberOfOctets(int numberOfOctets) {
        this.m_numberOfOctets = numberOfOctets;
    }

    public Class<?> getType() {
        return m_type;
    }

    public void setType(Class<?> type) {
        this.m_type = type;
    }

    public Class<? extends TypeDefinition> getTypeDefinition() {
        if (Objects.isNull(m_typeDefinition)) {
            return UnknownTypeDefinition.class;
        }
        return m_typeDefinition;
    }

    public void setTypeDefinition(Class<? extends TypeDefinition> typeDefinition) {
        this.m_typeDefinition = typeDefinition;
    }

    public int getIanaDataTypeCode() {
        return m_ianaDataTypeCode;
    }

    public void setIanaDataTypeCode(int ianaDataTypeCode) {
        this.m_ianaDataTypeCode = ianaDataTypeCode;
    }
}
