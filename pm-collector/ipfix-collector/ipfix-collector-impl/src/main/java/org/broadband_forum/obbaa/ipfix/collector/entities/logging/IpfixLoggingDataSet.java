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

import org.apache.commons.collections4.CollectionUtils;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixSetHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixDataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IpfixLoggingDataSet extends IpfixLoggingWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixLoggingDataSet.class);

    @Expose
    @SerializedName("TemplateSet")
    private Set<IpfixLoggingTemplateRecord> m_templateRecords = new HashSet<>();

    @Expose
    @SerializedName("Record")
    private Set<IpfixLoggingDataRecord> m_dataRecords = new HashSet<>();

    public IpfixLoggingDataSet(IpfixSetHeader setHeader) {
        super(setHeader.getId(), setHeader.getLength());
    }

    public IpfixLoggingDataSet(int setId, int length, Set<IpfixLoggingTemplateRecord> templateRecords,
                               Set<IpfixLoggingDataRecord> dataRecords) {
        super(setId, length);
        if (templateRecords != null) {
            this.m_templateRecords = templateRecords;
        }
        if (dataRecords != null) {
            this.m_dataRecords = dataRecords;
        }
    }

    public Set<IpfixLoggingTemplateRecord> getTemplateRecords() {
        return m_templateRecords;
    }

    public void setTemplateRecords(List<IpfixFieldSpecifier> templateRecords) {
        if (CollectionUtils.isEmpty(templateRecords)) {
            return;
        }

        int numberOfRecord = templateRecords.size();
        for (int i = 0; i < numberOfRecord; i++) {
            IpfixFieldSpecifier ipfixFieldSpecifier = templateRecords.get(i);
            IpfixLoggingTemplateRecord loggingTemplateRecord = new IpfixLoggingTemplateRecord(i + 1, ipfixFieldSpecifier);
            this.m_templateRecords.add(loggingTemplateRecord);
        }
    }

    public void addTemplateRecord(IpfixLoggingTemplateRecord templateRecord) {
        this.m_templateRecords.add(templateRecord);
    }

    public Set<IpfixLoggingDataRecord> getDataRecords() {
        return m_dataRecords;
    }

    public void setDataRecords(Set<IpfixLoggingDataRecord> dataRecords) {
        this.m_dataRecords = dataRecords;
    }

    public void setDataRecords(List<IpfixDataRecord> dataRecords, boolean isOptionTemplateSet) {
        if (CollectionUtils.isEmpty(dataRecords)) {
            return;
        }

        int numberOfRecord = dataRecords.size();
        for (int i = 0; i < numberOfRecord; i++) {
            IpfixDataRecord dataRecord = dataRecords.get(i);
            String hexValue = dataRecord.getHexValue();
            String decodedValue = dataRecord.getValue();

            IpfixLoggingDataRecord ipfixLoggingDataRecord = new IpfixLoggingDataRecord(i + 1, hexValue, decodedValue);
            m_dataRecords.add(ipfixLoggingDataRecord);
        }
    }
}
