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

import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixOptionTemplateSet;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixTemplateSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IpfixLogging {

    private GsonBuilder m_gson;

    @Expose
    @SerializedName("Version")
    private int m_versionNumber;

    @Expose
    @SerializedName("Length")
    private int m_length;

    @Expose
    @SerializedName("ExportTime")
    private String m_exportTime;

    @Expose
    @SerializedName("SequenceNumber")
    private long m_sequenceNumber;

    @Expose
    @SerializedName("ObservationDomainID")
    private long m_observationDomainId;

    @Expose
    @SerializedName("Set")
    private Set<IpfixLoggingWrapper> m_setMessages = new HashSet<>();

    public IpfixLogging() {
        this.m_gson = new GsonBuilder();
    }

    public int getVersionNumber() {
        return m_versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.m_versionNumber = versionNumber;
    }

    public int getLength() {
        return m_length;
    }

    public void setLength(int length) {
        this.m_length = length;
    }

    public String getExportTime() {
        return m_exportTime;
    }

    public void setExportTime(String exportTime) {
        this.m_exportTime = exportTime;
    }

    public long getSequenceNumber() {
        return m_sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.m_sequenceNumber = sequenceNumber;
    }

    public long getObservationDomainId() {
        return m_observationDomainId;
    }

    public void setObservationDomainId(long observationDomainId) {
        this.m_observationDomainId = observationDomainId;
    }

    public Set<IpfixLoggingWrapper> getSetMessages() {
        return m_setMessages;
    }

    public void setSetMessages(Set<IpfixLoggingWrapper> set) {
        this.m_setMessages = set;
    }

    public void addToSetMessages(IpfixLoggingWrapper ipfixSetMessage) {
        this.m_setMessages.add(ipfixSetMessage);
    }

    public void addTemplateSet(List<IpfixTemplateSet> templateSets) {
        for (IpfixTemplateSet templateSet : templateSets) {
            IpfixLoggingTemplateSet ipfixLoggingTemplateSet = new IpfixLoggingTemplateSet(templateSet);
            this.m_setMessages.add(ipfixLoggingTemplateSet);
        }
    }

    public void addOptionTemplateSet(List<IpfixOptionTemplateSet> optionTemplateSets) {
        for (IpfixOptionTemplateSet templateSet : optionTemplateSets) {
            IpfixLoggingTemplateSet ipfixOptionTemplateSet = new IpfixLoggingTemplateSet(templateSet);
            this.m_setMessages.add(ipfixOptionTemplateSet);
        }
    }

    public void setMessageHeader(IpfixMessageHeader messageHeader) {
        this.m_versionNumber = messageHeader.getVersionNumber();
        this.m_length = messageHeader.getLength();
        this.m_exportTime = messageHeader.getExportTime();
        this.m_sequenceNumber = messageHeader.getSequenceNumber();
        this.m_observationDomainId = messageHeader.getObservationDomainId();
    }

    public String convertToLog() {
        //Set version 1.0 to include the decoded value in IpfixLoggingDataRecord for NBI logging
        //FieldValue: 44534c31 (DSL1)
        Gson gson = m_gson.setVersion(1.0).excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
