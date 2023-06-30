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

package org.broadband_forum.obbaa.ipfix.entities.message;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.ipfix.entities.IpfixDecodable;
import org.broadband_forum.obbaa.ipfix.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.header.IpfixSetHeader;
import org.broadband_forum.obbaa.ipfix.entities.message.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.entities.message.set.IpfixOptionTemplateSet;
import org.broadband_forum.obbaa.ipfix.entities.message.set.IpfixTemplateSet;
import org.broadband_forum.obbaa.ipfix.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

//+----------------------------------------------------+
//| Message Header                                     |
//+----------------------------------------------------+
//| Set                                                |
//+----------------------------------------------------+
//| Set                                                |
//+----------------------------------------------------+
//...
//+----------------------------------------------------+
//| Set                                                |
//+----------------------------------------------------+
public class IpfixMessage extends IpfixEntity implements IpfixDecodable {

    private static final int HEADER_LENGTH = IpfixMessageHeader.TOTAL_LENGTH;
    private static final int SET_HEADER_LENGTH = IpfixSetHeader.TOTAL_LENGTH;

    private IpfixMessageHeader m_header;
    private List<IpfixTemplateSet> m_templateSets;
    private List<IpfixOptionTemplateSet> m_optionTemplateSets;
    private List<IpfixDataSet> m_dataSets;

    public IpfixMessage(byte[] data) throws NotEnoughBytesException {
        super(data);
        try {
            decodeToIpfixEntity(data);
        } catch (IllegalArgumentException e) {
            throw new NotEnoughBytesException(e.getMessage(), e);
        }
    }

    public IpfixMessageHeader getHeader() {
        return m_header;
    }

    public void setHeader(IpfixMessageHeader header) {
        m_header = header;
    }

    public List<IpfixTemplateSet> getTemplateSets() {
        return m_templateSets;
    }

    public void setTemplateSets(List<IpfixTemplateSet> templateSets) {
        m_templateSets = templateSets;
    }

    public void addTemplateSet(IpfixTemplateSet templateSet) {
        if (m_templateSets == null) {
            m_templateSets = new ArrayList<IpfixTemplateSet>();
        }
        m_templateSets.add(templateSet);
    }

    public List<IpfixOptionTemplateSet> getOptionTemplateSets() {
        return m_optionTemplateSets;
    }

    public void setOptionTemplateSets(List<IpfixOptionTemplateSet> optionTemplateSets) {
        m_optionTemplateSets = optionTemplateSets;
    }

    public void addOptionTemplateSet(IpfixOptionTemplateSet optionTemplateSet) {
        if (m_optionTemplateSets == null) {
            m_optionTemplateSets = new ArrayList<IpfixOptionTemplateSet>();
        }
        m_optionTemplateSets.add(optionTemplateSet);
    }

    public List<IpfixDataSet> getDataSets() {
        return m_dataSets;
    }

    public void setDataSets(List<IpfixDataSet> dataSets) {
        m_dataSets = dataSets;
    }

    public void addDataSet(IpfixDataSet dataSet) {
        if (m_dataSets == null) {
            m_dataSets = new ArrayList<IpfixDataSet>();
        }
        m_dataSets.add(dataSet);
    }

    @Override
    public void decodeToIpfixEntity(byte[] data) throws NotEnoughBytesException {
        int start = 0;

        byte[] header = IpfixUtilities.copyByteArray(data, start, HEADER_LENGTH);
        this.setHeader(new IpfixMessageHeader(header));
        start += HEADER_LENGTH;
        int messageLength = this.getHeader().getLength();

        while (start < messageLength) {
            byte[] setData = IpfixUtilities.copyByteArray(data, start, messageLength - start);
            byte[] setHeaderData = IpfixUtilities.copyByteArray(data, start, SET_HEADER_LENGTH);
            IpfixSetHeader setHeader = new IpfixSetHeader(setHeaderData);
            int setId = setHeader.getId();

            if (setId == IpfixTemplateSet.SET_ID) {
                IpfixTemplateSet set = parseTemplateSet(setData);
                if (set == null) {
                    break;
                } else {
                    start += set.getPresentation().length;
                    this.addTemplateSet(set);
                }
            } else if (setId == IpfixOptionTemplateSet.SET_ID) {
                IpfixOptionTemplateSet set = parseOptionTemplateSet(setData);
                if (set == null) {
                    break;
                } else {
                    start += set.getPresentation().length;
                    this.addOptionTemplateSet(set);
                }
            } else if (setId >= IpfixDataSet.SET_ID_MIN) {
                IpfixDataSet set = parseDataSet(setData);
                if (set == null) {
                    break;
                } else {
                    start += set.getPresentation().length;
                    this.addDataSet(set);
                }
            } else {
                break;
            }
        }

        byte[] presentation = IpfixUtilities.copyByteArray(data, 0, start);
        this.setPresentation(presentation);

    }

    private IpfixTemplateSet parseTemplateSet(byte[] setData) {
        try {
            IpfixTemplateSet set = new IpfixTemplateSet(setData);
            return set;
        } catch (NotEnoughBytesException e) {
            return null;
        }
    }

    private IpfixOptionTemplateSet parseOptionTemplateSet(byte[] setData) {
        try {
            IpfixOptionTemplateSet set = new IpfixOptionTemplateSet(setData);
            return set;
        } catch (NotEnoughBytesException e) {
            return null;
        }
    }

    private IpfixDataSet parseDataSet(byte[] setData) {
        try {
            IpfixDataSet set = new IpfixDataSet(setData);
            return set;
        } catch (NotEnoughBytesException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "IpfixMessage{"
                + "m_header=" + m_header
                + ", m_templateSets=" + m_templateSets
                + ", m_optionTemplateSets=" + m_optionTemplateSets
                + ", m_dataSets=" + m_dataSets
                + ", data= " + getPresentationAsString()
                + '}';
    }
}
