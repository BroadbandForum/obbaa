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

package org.broadband_forum.obbaa.ipfix.entities.message.logging;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

public class IpfixLoggingDataRecord {

    @Expose
    @SerializedName("FieldID")
    private int m_fieldId;

    //When generate JSON value if the version is 1.5
    // or higher, it will include m_rawValue
    //FieldValue: 44534c31
    @Since(1.5)
    @Expose
    @SerializedName("FieldValue")
    private String m_rawValue;

    //When generate JSON value if the version is
    // lower than 1.5, it will include m_decodedValue
    //FieldValue: 44534c31 (DSL1)
    @Until(1.5)
    @Expose
    @SerializedName("FieldValue")
    private String m_decodedValue;

    public IpfixLoggingDataRecord(int fieldId, String rawValue, String decodedValue) {
        this.m_fieldId = fieldId;
        this.m_rawValue = rawValue;
        this.m_decodedValue = m_rawValue + " (" + decodedValue + ")";
    }

    public int getFieldId() {
        return m_fieldId;
    }

    public void setFieldId(int fieldId) {
        this.m_fieldId = fieldId;
    }

    public String getRawValue() {
        return m_rawValue;
    }

    public void setRawValue(String rawValue) {
        this.m_rawValue = rawValue;
    }

    public String getDecodedValue() {
        return m_decodedValue;
    }

    public void setDecodedValue(String decodedValue) {
        this.m_decodedValue = decodedValue;
    }
}
