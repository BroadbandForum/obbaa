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
import org.broadband_forum.obbaa.ipfix.entities.record.IpfixOptionTemplateRecord;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

//+--------------------------------------------------+
//| Set Header                                       |
//+--------------------------------------------------+
//| record                                           |
//+--------------------------------------------------+
//| record                                           |
//+--------------------------------------------------+
//...
//+--------------------------------------------------+
//| record                                           |
//+--------------------------------------------------+
//| Padding (opt.)                                   |
//+--------------------------------------------------+
public class IpfixOptionTemplateSet extends IpfixEntity implements IpfixDecodable {

    public static final int SET_ID = 3;
    private static final int HEADER_LENGTH = IpfixSetHeader.TOTAL_LENGTH;

    private IpfixSetHeader m_header;
    private List<IpfixOptionTemplateRecord> m_records;
    private byte[] m_padding;

    public IpfixOptionTemplateSet(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public IpfixSetHeader getHeader() {
        return m_header;
    }

    public void setHeader(IpfixSetHeader header) {
        m_header = header;
    }

    public List<IpfixOptionTemplateRecord> getRecords() {
        return m_records;
    }

    public void setRecords(List<IpfixOptionTemplateRecord> records) {
        m_records = records;
    }

    public void addRecord(IpfixOptionTemplateRecord record) {
        if (m_records == null) {
            m_records = new ArrayList<IpfixOptionTemplateRecord>();
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
            IpfixOptionTemplateRecord optionTemplateRecord = parseOptionTemplateRecord(record);
            if (optionTemplateRecord == null) {
                break;
            } else {
                this.addRecord(optionTemplateRecord);
                start += optionTemplateRecord.getPresentation().length;
            }
        }

        byte[] padding = IpfixUtilities.copyByteArray(data, start, setLength - start);
        start += padding.length;
        this.setPadding(padding);

        byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
        this.setPresentation(presentation);
    }

    private IpfixOptionTemplateRecord parseOptionTemplateRecord(byte[] data) {
        try {
            IpfixOptionTemplateRecord otr = new IpfixOptionTemplateRecord(data);
            return otr;
        } catch (NotEnoughBytesException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "IpfixOptionTemplateSet{"
                + "m_header=" + m_header
                + ", m_records=" + m_records
                + ", m_padding=" + Arrays.toString(m_padding)
                + ", data= " + getPresentationAsString()
                + '}';
    }
}
