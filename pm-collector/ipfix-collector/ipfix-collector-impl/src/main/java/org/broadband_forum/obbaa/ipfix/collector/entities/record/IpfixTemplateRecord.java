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
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

public class IpfixTemplateRecord extends AbstractTemplateRecord implements IpfixDecodable, Serializable {

    private static final int HEADER_LENGTH = IpfixTemplateRecordHeader.TOTAL_LENGTH;

    private IpfixTemplateRecordHeader m_header;

    public IpfixTemplateRecord(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public IpfixTemplateRecordHeader getHeader() {
        return m_header;
    }

    public void setHeader(IpfixTemplateRecordHeader header) {
        m_header = header;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        try {
            int start = 0;

            byte[] header = IpfixUtilities.copyByteArray(data, start, HEADER_LENGTH);
            this.setHeader(new IpfixTemplateRecordHeader(header));
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
        } catch (UtilityException | NotEnoughBytesException e) {
            throw new NotEnoughBytesException("Not enough bytes to decode Template Record");
        }
    }

    @Override
    public String toString() {
        return "IpfixTemplateRecord{"
                + "m_header=" + m_header
                + ", data= " + getPresentationAsString()
                + '}';
    }
}
