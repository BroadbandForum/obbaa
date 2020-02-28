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
import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixEntity;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

public class AbstractTemplateRecord extends IpfixEntity implements Serializable {

    private List<IpfixFieldSpecifier> m_fieldSpecifiers = new ArrayList<>();

    public AbstractTemplateRecord(byte[] data) {
        super(data);
    }

    public List<IpfixFieldSpecifier> getFieldSpecifiers() {
        return m_fieldSpecifiers;
    }

    public void setFieldSpecifiers(List<IpfixFieldSpecifier> fieldSpecifiers) {
        m_fieldSpecifiers = fieldSpecifiers;
    }

    public void addFieldSpecifier(IpfixFieldSpecifier fieldSpecifier) {
        if (m_fieldSpecifiers == null) {
            m_fieldSpecifiers = new ArrayList<IpfixFieldSpecifier>();
        }
        m_fieldSpecifiers.add(fieldSpecifier);
    }

    protected boolean isEnterpriseField(byte[] data) {
        return IpfixUtilities.getBit(data, 0) == 1;
    }

    @Override
    public String toString() {
        return "AbstractTemplateRecord{"
                + "m_fieldSpecifiers=" + m_fieldSpecifiers
                + ",data=" + IpfixUtilities.bytesToHex(getPresentation()) + '}';
    }
}
