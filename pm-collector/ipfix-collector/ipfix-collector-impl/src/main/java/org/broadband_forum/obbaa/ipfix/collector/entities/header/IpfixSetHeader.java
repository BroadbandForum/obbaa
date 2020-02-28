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

package org.broadband_forum.obbaa.ipfix.collector.entities.header;

import java.io.Serializable;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

//0                   1                   2                   3
//0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|          Set ID               |          Length               |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
public class IpfixSetHeader extends IpfixEntity implements IpfixDecodable, Serializable {

    private static final int ID_FIELD_LENGTH = 2;
    private static final int LENGTH_FIELD_LENGTH = 2;
    public static final int TOTAL_LENGTH = ID_FIELD_LENGTH + LENGTH_FIELD_LENGTH;

    private int m_id;
    private int m_length;

    public IpfixSetHeader(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public int getLength() {
        return m_length;
    }

    public void setLength(int length) {
        m_length = length;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        try {
            int start = 0;

            byte[] id = IpfixUtilities.copyByteArray(data, start, ID_FIELD_LENGTH);
            this.setId(IpfixUtilities.byteToInteger(id, 1, 2));
            start += ID_FIELD_LENGTH;

            byte[] length = IpfixUtilities.copyByteArray(data, start, LENGTH_FIELD_LENGTH);
            this.setLength(IpfixUtilities.byteToInteger(length, 1, 2));
            start += LENGTH_FIELD_LENGTH;

            byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
            this.setPresentation(presentation);
        } catch (UtilityException e) {
            throw new NotEnoughBytesException("Not enough bytes to decode Set Header");
        }

    }

    @Override
    public String toString() {
        return "IpfixSetHeader{"
                + "m_id=" + m_id
                + ", m_length=" + m_length
                + ", data: " + getPresentationAsString() + "}";
    }
}
