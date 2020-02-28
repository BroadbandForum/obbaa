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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

//0                   1                   2                   3
//0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|       Version Number          |            Length             |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|                           Export Time                         |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|                       Sequence Number                         |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//|                    Observation Domain ID                      |
//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
public class IpfixMessageHeader extends IpfixEntity implements IpfixDecodable {

    public static final int VERSION_NUBMER_FIELD_LENGTH = 2;
    public static final int LENGTH_FIELD_LENGTH = 2;
    private static final int EXPORT_TIME_FIELD_LENGTH = 4;
    private static final int SEQUENCE_NUMBER_FIELD_LENGTH = 4;
    private static final int OBSV_DOMAIN_ID_FIELD_LENGTH = 4;
    public static final int TOTAL_LENGTH = VERSION_NUBMER_FIELD_LENGTH + LENGTH_FIELD_LENGTH + EXPORT_TIME_FIELD_LENGTH
            + SEQUENCE_NUMBER_FIELD_LENGTH + OBSV_DOMAIN_ID_FIELD_LENGTH;
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String TIMEZONE = "UTC";

    private int m_versionNumber;
    private int m_length;
    private String m_exportTime;
    private long m_sequenceNumber;
    private long m_observationDomainId;

    public IpfixMessageHeader(byte[] data) throws NotEnoughBytesException {
        super(data);
        decodeToIpfixEntity(data);
    }

    public int getVersionNumber() {
        return m_versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        m_versionNumber = versionNumber;
    }

    public int getLength() {
        return m_length;
    }

    public void setLength(int length) {
        m_length = length;
    }

    public String getExportTime() {
        return m_exportTime;
    }

    public void setExportTime(String exportTime) {
        m_exportTime = exportTime;
    }

    public long getSequenceNumber() {
        return m_sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        m_sequenceNumber = sequenceNumber;
    }

    public long getObservationDomainId() {
        return m_observationDomainId;
    }

    public void setObservationDomainId(long observationDomainId) {
        m_observationDomainId = observationDomainId;
    }

    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        try {
            int start = 0;

            byte[] versionNumber = IpfixUtilities.copyByteArray(data, start, VERSION_NUBMER_FIELD_LENGTH);
            this.setVersionNumber(IpfixUtilities.byteToInteger(versionNumber, 1, 2));
            start += VERSION_NUBMER_FIELD_LENGTH;

            byte[] messageLength = IpfixUtilities.copyByteArray(data, start, LENGTH_FIELD_LENGTH);
            this.setLength(IpfixUtilities.byteToInteger(messageLength, 1, 2));
            start += LENGTH_FIELD_LENGTH;

            byte[] exportTimeData = IpfixUtilities.copyByteArray(data, start, EXPORT_TIME_FIELD_LENGTH);
            Long millisecondSinceEpoch = IpfixUtilities.bytesToLong(exportTimeData, 1, 4) * 1000;
            Date date = new Date(millisecondSinceEpoch);
            DateFormat df = new SimpleDateFormat(PATTERN);
            df.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
            String exportTime = df.format(date);
            this.setExportTime(exportTime);
            start += EXPORT_TIME_FIELD_LENGTH;

            byte[] sequenceNumber = IpfixUtilities.copyByteArray(data, start, SEQUENCE_NUMBER_FIELD_LENGTH);
            this.setSequenceNumber(IpfixUtilities.bytesToLong(sequenceNumber, 1, 4));
            start += SEQUENCE_NUMBER_FIELD_LENGTH;

            byte[] obsvDomainId = IpfixUtilities.copyByteArray(data, start, OBSV_DOMAIN_ID_FIELD_LENGTH);
            this.setObservationDomainId(IpfixUtilities.bytesToLong(obsvDomainId, 1, 4));
            start += OBSV_DOMAIN_ID_FIELD_LENGTH;

            byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
            this.setPresentation(presentation);
        } catch (UtilityException e) {
            throw new NotEnoughBytesException("Not enough bytes to decode Message Header");
        }

    }

    @Override
    public String toString() {
        return "IpfixMessageHeader{"
                + "versionNumber=" + m_versionNumber
                + ", length=" + m_length
                + ", exportTime='" + m_exportTime + '\''
                + ", sequenceNumber=" + m_sequenceNumber
                + ", observationDomainId=" + m_observationDomainId
                + ", data= " + IpfixUtilities.bytesToHex(getPresentation()) + "}";
    }
}
