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

package org.broadband_forum.obbaa.ipfix.collector.entities.logging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixOptionTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixOptionTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixOptionTemplateSet;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixTemplateSet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IpfixLoggingTemplateSet extends IpfixLoggingWrapper {

    @Expose
    @SerializedName("TemplateID")
    private int m_templateId;

    @Expose
    @SerializedName("FieldCount")
    private int m_fieldCount;

    @Expose
    @SerializedName("Padding")
    private byte[] m_padding;

    @Expose
    @SerializedName("Field")
    private Set<IpfixLoggingTemplateRecord> m_templateRecords = new HashSet<>();

    public IpfixLoggingTemplateSet() {
    }

    ;

    public IpfixLoggingTemplateSet(int setId, int length, int templateId, int fieldCount,
                                   byte[] padding, Set<IpfixLoggingTemplateRecord> templateRecords) {
        super(setId, length);
        this.m_templateId = templateId;
        this.m_fieldCount = fieldCount;
        this.m_padding = padding;
        if (templateRecords != null) {
            this.m_templateRecords = templateRecords;
        }
    }

    public IpfixLoggingTemplateSet(IpfixTemplateSet templateSet) {
        super(templateSet.getHeader());
        this.m_padding = templateSet.getPadding();
        List<IpfixTemplateRecord> ipfixTemplateRecords = templateSet.getRecords();
        if (ipfixTemplateRecords.size() > 0) {
            IpfixTemplateRecord templateRecord = ipfixTemplateRecords.get(0);
            IpfixTemplateRecordHeader templateRecordHeader = templateRecord.getHeader();
            this.m_templateId = templateRecordHeader.getId();
            this.m_fieldCount = templateRecordHeader.getFieldCount();

            List<IpfixFieldSpecifier> ipfixFieldSpecifiers = templateRecord.getFieldSpecifiers();
            setTemplateRecords(ipfixFieldSpecifiers);
        }
    }

    public IpfixLoggingTemplateSet(IpfixOptionTemplateSet templateSet) {
        super(templateSet.getHeader());
        this.m_padding = templateSet.getPadding();
        List<IpfixOptionTemplateRecord> ipfixOptionTemplateRecords = templateSet.getRecords();
        if (ipfixOptionTemplateRecords.size() > 0) {
            IpfixOptionTemplateRecord templateRecord = ipfixOptionTemplateRecords.get(0);
            IpfixOptionTemplateRecordHeader templateRecordHeader = templateRecord.getHeader();
            this.m_templateId = templateRecordHeader.getId();
            this.m_fieldCount = templateRecordHeader.getFieldCount();

            List<IpfixFieldSpecifier> ipfixFieldSpecifiers = templateRecord.getFieldSpecifiers();
            setTemplateRecords(ipfixFieldSpecifiers);
        }
    }

    public int getTemplateId() {
        return m_templateId;
    }

    public void setTemplateId(int templateId) {
        this.m_templateId = templateId;
    }

    public int getFieldCount() {
        return m_fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.m_fieldCount = fieldCount;
    }

    public byte[] getPadding() {
        return m_padding;
    }

    public void setPadding(byte[] padding) {
        this.m_padding = padding;
    }

    public Set<IpfixLoggingTemplateRecord> getTemplateRecords() {
        return m_templateRecords;
    }

    public void setTemplateRecords(List<IpfixFieldSpecifier> ipfixFieldSpecifiers) {
        int numberOfField = ipfixFieldSpecifiers.size();
        for (int i = 0; i < numberOfField; i++) {
            IpfixFieldSpecifier ipfixFieldSpecifier = ipfixFieldSpecifiers.get(i);
            IpfixLoggingTemplateRecord loggingTemplateRecord = new IpfixLoggingTemplateRecord(i + 1, ipfixFieldSpecifier);
            m_templateRecords.add(loggingTemplateRecord);
        }
    }

    public void setTemplateRecords(Set<IpfixLoggingTemplateRecord> templateRecords) {
        this.m_templateRecords = templateRecords;
    }
}
