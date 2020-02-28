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

package org.broadband_forum.obbaa.ipfix.collector.entities;

import java.io.Serializable;

import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

//0                   1                   2                   3
//0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|E|  Information Element ident. |        Field Length           |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|                      Enterprise Number                        |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
public class IpfixFieldSpecifier extends IpfixEntity implements IpfixDecodable, Serializable {

    public static final int IEID_FIELD_LENGTH = 2;
    private static final int LENGTH_FIELD_LENGTH = 2;
    public static final int NON_ENTERPRISE_LENGTH = IEID_FIELD_LENGTH + LENGTH_FIELD_LENGTH;
    private static final int ENTERPRISE_FIELD_LENGTH = 4;
    public static final int ENTERPRISE_LENGTH = IEID_FIELD_LENGTH + LENGTH_FIELD_LENGTH + ENTERPRISE_FIELD_LENGTH;

    private int m_fieldId;

    private boolean m_isEnterprise;

    private int m_informationElementId;

    private int m_fieldLength;

    private long m_enterpriseNumber;

    public IpfixFieldSpecifier(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public boolean isEnterprise() {
        return m_isEnterprise;
    }

    public void setEnterprise(boolean isEnterprise) {
        m_isEnterprise = isEnterprise;
    }

    public int getInformationElementId() {
        return m_informationElementId;
    }

    public void setInformationElementId(int informationElementId) {
        m_informationElementId = informationElementId;
    }

    public int getFieldLength() {
        return m_fieldLength;
    }

    public void setFieldLength(int fieldLength) {
        m_fieldLength = fieldLength;
    }

    public long getEnterpriseNumber() {
        return m_enterpriseNumber;
    }

    public void setEnterpriseNumber(long enterpriseNumber) {
        m_enterpriseNumber = enterpriseNumber;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        try {
            int start = 0;

            byte[] id = IpfixUtilities.copyByteArray(data, start, IEID_FIELD_LENGTH);
            this.setEnterprise(IpfixUtilities.getBit(id, 0) == 1);
            this.setInformationElementId(decodeInformationElementId(id));
            start += IEID_FIELD_LENGTH;

            byte[] fielLength = IpfixUtilities.copyByteArray(data, start, LENGTH_FIELD_LENGTH);
            this.setFieldLength(IpfixUtilities.byteToInteger(fielLength, 1, 2));
            start += LENGTH_FIELD_LENGTH;

            if (this.isEnterprise()) {
                byte[] enterpriseNumber = IpfixUtilities.copyByteArray(data, start, ENTERPRISE_FIELD_LENGTH);
                this.setEnterpriseNumber(IpfixUtilities.bytesToLong(enterpriseNumber, 1, 4));
            }

        } catch (UtilityException e) {
            throw new NotEnoughBytesException("Not enough bytes to decode Field Specifier");
        }
    }

    private int decodeInformationElementId(byte[] data) throws UtilityException {
        data[0] = (byte) (data[0] & 0x7f);
        return IpfixUtilities.byteToInteger(data, 1, 2);
    }

    @Override
    public String toString() {
        return "IpfixFieldSpecifier{"
                + "m_isEnterprise=" + m_isEnterprise
                + ", m_informationElementId=" + m_informationElementId
                + ", m_fieldLength=" + m_fieldLength
                + ", m_enterpriseNumber=" + m_enterpriseNumber
                + '}';
    }
}
