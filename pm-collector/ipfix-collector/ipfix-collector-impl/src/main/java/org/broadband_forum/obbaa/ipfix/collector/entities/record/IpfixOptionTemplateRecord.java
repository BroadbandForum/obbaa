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

package org.broadband_forum.obbaa.ipfix.collector.entities.record;

import java.io.Serializable;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixOptionTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

//+--------------------------------------------------+
//| Options Template Record Header                   |
//+--------------------------------------------------+
//| Field Specifier                                  |
//+--------------------------------------------------+
//| Field Specifier                                  |
//+--------------------------------------------------+
//...
//+--------------------------------------------------+
//| Field Specifier                                  |
//+--------------------------------------------------+
public class IpfixOptionTemplateRecord extends AbstractTemplateRecord implements IpfixDecodable, Serializable {

    private static final int HEADER_LENGTH = IpfixOptionTemplateRecordHeader.TOTAL_LENGTH;

    private IpfixOptionTemplateRecordHeader m_header;

    public IpfixOptionTemplateRecord(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public IpfixOptionTemplateRecordHeader getHeader() {
        return m_header;
    }

    public void setHeader(IpfixOptionTemplateRecordHeader header) {
        m_header = header;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        try {
            int start = 0;

            byte[] header = IpfixUtilities.copyByteArray(data, start, HEADER_LENGTH);
            this.setHeader(new IpfixOptionTemplateRecordHeader(header));
            start += HEADER_LENGTH;

            int count = this.getHeader().getFieldCount();

            for (int i = 0; i < count; i++) {
                byte[] id = IpfixUtilities.copyByteArray(data, start, IpfixFieldSpecifier.IEID_FIELD_LENGTH);
                if (isEnterpriseField(id)) {
                    byte[] field = IpfixUtilities.copyByteArray(data, start, IpfixFieldSpecifier.ENTERPRISE_LENGTH);
                    IpfixFieldSpecifier fieldSpecifier = new IpfixFieldSpecifier(field);
                    this.addFieldSpecifier(fieldSpecifier);
                    start += IpfixFieldSpecifier.ENTERPRISE_LENGTH;
                } else {
                    byte[] field = IpfixUtilities.copyByteArray(data, start, IpfixFieldSpecifier.NON_ENTERPRISE_LENGTH);
                    IpfixFieldSpecifier fieldSpecifier = new IpfixFieldSpecifier(field);
                    this.addFieldSpecifier(fieldSpecifier);
                    start += IpfixFieldSpecifier.NON_ENTERPRISE_LENGTH;
                }
            }

            byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
            this.setPresentation(presentation);
        } catch (NotEnoughBytesException e) {
            throw new NotEnoughBytesException("Not enough bytes to decode Option Template Record");
        }
    }

    @Override
    public String toString() {
        return "IpfixOptionTemplateRecord{"
                + "m_header=" + m_header
                + ", data= " + getPresentationAsString() + "}";
    }
}