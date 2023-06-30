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

package org.broadband_forum.obbaa.ipfix.entities.message.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.broadband_forum.obbaa.ipfix.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.header.IpfixSetHeader;
import org.broadband_forum.obbaa.ipfix.entities.record.IpfixTemplateRecord;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

public class IpfixTemplateSet extends IpfixEntity implements IpfixDecodable {

    public static final int SET_ID = 2;
    private static final int HEADER_LENGTH = IpfixSetHeader.TOTAL_LENGTH;

    private IpfixSetHeader m_header;
    private List<IpfixTemplateRecord> m_records;
    private byte[] m_padding;

    public IpfixTemplateSet(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public IpfixSetHeader getHeader() {
        return m_header;
    }

    public void setHeader(IpfixSetHeader header) {
        m_header = header;
    }

    public List<IpfixTemplateRecord> getRecords() {
        return m_records;
    }

    public void setRecords(List<IpfixTemplateRecord> records) {
        m_records = records;
    }

    public void addRecord(IpfixTemplateRecord record) {
        if (m_records == null) {
            m_records = new ArrayList<IpfixTemplateRecord>();
        }
        m_records.add(record);
    }

    public byte[] getPadding() {
        return m_padding;
    }

    public void setPadding(byte[] padding) {
        m_padding = padding;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        int start = 0;

        byte[] header = IpfixUtilities.copyByteArray(data, start, HEADER_LENGTH);
        this.setHeader(new IpfixSetHeader(header));
        start += HEADER_LENGTH;

        int setLength = this.getHeader().getLength();
        while (start < setLength) {
            byte[] record = IpfixUtilities.copyByteArray(data, start, setLength - start);
            IpfixTemplateRecord templateRecord = parseTemplateRecord(record);
            if (templateRecord == null) {
                break;
            } else {
                this.addRecord(templateRecord);
                start += templateRecord.getPresentation().length;
            }
        }

        byte[] padding = IpfixUtilities.copyByteArray(data, start, setLength - start);
        start += padding.length;
        this.setPadding(padding);

        byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
        this.setPresentation(presentation);
    }

    private IpfixTemplateRecord parseTemplateRecord(byte[] data) {
        try {
            IpfixTemplateRecord tr = new IpfixTemplateRecord(data);
            return tr;
        } catch (NotEnoughBytesException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "IpfixTemplateSet{"
                + "m_header=" + m_header
                + ", m_records=" + m_records
                + ", m_padding=" + Arrays.toString(m_padding)
                + ", data= " + getPresentationAsString()
                + '}';
    }
}
