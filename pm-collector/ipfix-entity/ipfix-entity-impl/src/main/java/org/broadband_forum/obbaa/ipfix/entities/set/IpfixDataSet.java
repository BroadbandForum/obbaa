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

package org.broadband_forum.obbaa.ipfix.entities.set;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.ipfix.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.header.IpfixSetHeader;
import org.broadband_forum.obbaa.ipfix.entities.record.IpfixDataRecord;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

public class IpfixDataSet extends IpfixEntity implements IpfixDecodable, Serializable {

    public static final int SET_ID_MIN = 256;
    private static final int HEADER_LENGTH = IpfixSetHeader.TOTAL_LENGTH;

    private IpfixSetHeader m_header;
    private byte[] m_rawData;
    private List<IpfixDataRecord> m_records;

    public IpfixDataSet(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public IpfixSetHeader getHeader() {
        return m_header;
    }

    public void setHeader(IpfixSetHeader header) {
        m_header = header;
    }

    public byte[] getRawData() {
        return m_rawData;
    }

    public void setRawData(byte[] rawData) {
        m_rawData = rawData;
    }

    public List<IpfixDataRecord> getRecords() {
        return m_records;
    }

    public void setRecords(List<IpfixDataRecord> records) {
        m_records = records;
    }

    public void addRecord(IpfixDataRecord record) {
        if (m_records == null) {
            m_records = new ArrayList<IpfixDataRecord>();
        }
        m_records.add(record);
    }

    @Override
    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        int start = 0;
        byte[] header = IpfixUtilities.copyByteArray(data, start, HEADER_LENGTH);
        this.setHeader(new IpfixSetHeader(header));
        start += HEADER_LENGTH;
        int setLength = this.getHeader().getLength();

        byte[] rawData = IpfixUtilities.copyByteArray(data, start, setLength - start);
        this.setRawData(rawData);
        start += rawData.length;

        byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
        this.setPresentation(presentation);
    }

    @Override
    public String toString() {
        return "IpfixDataSet{"
                + "m_header=" + m_header
                + ", m_rawData=" + IpfixUtilities.bytesToHex(m_rawData)
                + ", m_records=" + m_records
                + ", data= " + getPresentationAsString()
                + '}';
    }
}
