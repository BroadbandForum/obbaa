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

import org.broadband_forum.obbaa.ipfix.entities.IpfixFieldSpecifier;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IpfixLoggingTemplateRecord {

    @Expose
    @SerializedName("FieldID")
    private int m_fieldId;

    @Expose
    @SerializedName("EnterpriseBit")
    private int m_enterPriseBit;

    @Expose
    @SerializedName("IEID")
    private int m_ieId;

    @Expose
    @SerializedName("FieldLength")
    private int m_fieldLength;

    @Expose
    @SerializedName("EnterpriseNumber")
    private long m_enterpriseNumber;

    public IpfixLoggingTemplateRecord() {
    }

    public IpfixLoggingTemplateRecord(int fieldId, int enterPriseBit, int ieId,
                                      int fieldLength, long enterpriseNumber) {
        this.m_fieldId = fieldId;
        this.m_enterPriseBit = enterPriseBit;
        this.m_ieId = ieId;
        this.m_fieldLength = fieldLength;
        this.m_enterpriseNumber = enterpriseNumber;
    }

    public IpfixLoggingTemplateRecord(int fieldId, IpfixFieldSpecifier ipfixFieldSpecifier) {
        this.m_fieldId = fieldId;
        this.m_enterPriseBit = ipfixFieldSpecifier.isEnterprise() ? 1 : 0;
        this.m_ieId = ipfixFieldSpecifier.getInformationElementId();
        this.m_fieldLength = ipfixFieldSpecifier.getFieldLength();
        this.m_enterpriseNumber = ipfixFieldSpecifier.getEnterpriseNumber();
    }

    public int getFieldId() {
        return m_fieldId;
    }

    public void setFieldId(int fieldId) {
        this.m_fieldId = fieldId;
    }

    public int getEnterPriseBit() {
        return m_enterPriseBit;
    }

    public void setEnterPriseBit(int enterPriseBit) {
        this.m_enterPriseBit = enterPriseBit;
    }

    public int getIeId() {
        return m_ieId;
    }

    public void setIeId(int ieId) {
        this.m_ieId = ieId;
    }

    public int getFieldLength() {
        return m_fieldLength;
    }

    public void setFieldLength(int fieldLength) {
        this.m_fieldLength = fieldLength;
    }

    public long getEnterpriseNumber() {
        return m_enterpriseNumber;
    }

    public void setEnterpriseNumber(long enterpriseNumber) {
        this.m_enterpriseNumber = enterpriseNumber;
    }
}
