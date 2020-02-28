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

public class IpfixDataRecord implements Serializable {

    private String m_value;

    private String m_hexValue;

    // DataRecord will not be decode when decoding ipfix message!
    // It will be decode in collecting process
    public IpfixDataRecord(String value, String hexValue) {
        m_value = value;
        m_hexValue = hexValue;
    }

    public IpfixDataRecord(String hexValue) {
        m_value = hexValue;
        m_hexValue = hexValue;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        this.m_value = value;
    }

    public String getHexValue() {
        return m_hexValue;
    }

    public void setHexValue(String hexValue) {
        this.m_hexValue = hexValue;
    }

    @Override
    public String toString() {
        return "IpfixDataRecord{"
                + "m_value='" + m_value + '\''
                + ", m_hexValue='" + m_hexValue + '\''
                + '}';
    }
}
