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

package org.broadband_forum.obbaa.ipfix.entities.header;

import java.io.Serializable;

import org.broadband_forum.obbaa.ipfix.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

//0                   1                   2                   3
//0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|      Template ID (> 255)      |         Field Count           |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
public class IpfixTemplateRecordHeader extends IpfixEntity implements IpfixDecodable, Serializable {

    private static final int ID_FIELD_LENGTH = 2;
    private static final int FIELD_COUNT_FIELD_LENGTH = 2;
    public static final int TOTAL_LENGTH = ID_FIELD_LENGTH + FIELD_COUNT_FIELD_LENGTH;

    private int m_id;
    private int m_fieldCount;

    public IpfixTemplateRecordHeader(byte[] data) throws UtilityException, NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public int getFieldCount() {
        return m_fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        m_fieldCount = fieldCount;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        try {
            int start = 0;

            byte[] id = IpfixUtilities.copyByteArray(data, start, ID_FIELD_LENGTH);
            this.setId(IpfixUtilities.byteToInteger(id, 1, 2));
            start += ID_FIELD_LENGTH;

            byte[] fieldCount = IpfixUtilities.copyByteArray(data, start, FIELD_COUNT_FIELD_LENGTH);
            this.setFieldCount(IpfixUtilities.byteToInteger(fieldCount, 1, 2));
            start += FIELD_COUNT_FIELD_LENGTH;

            byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
            this.setPresentation(presentation);
        } catch (UtilityException e) {
            throw new NotEnoughBytesException("Not enough bytes to decode Template Header");
        }
    }

    @Override
    public String toString() {
        return "IpfixTemplateRecordHeader{"
                + "m_id=" + m_id
                + ", m_fieldCount=" + m_fieldCount
                + ", data= " + getPresentationAsString()
                + '}';
    }
}
