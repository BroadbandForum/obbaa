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

package org.broadband_forum.obbaa.ipfix.entities.message.logging;


import org.broadband_forum.obbaa.ipfix.entities.header.IpfixSetHeader;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IpfixLoggingWrapper {

    @Expose
    @SerializedName("SetID")
    private int m_setId;

    @Expose
    @SerializedName("Length")
    private int m_length;

    public IpfixLoggingWrapper() {
    }

    ;

    public IpfixLoggingWrapper(int setId, int length) {
        this.m_setId = setId;
        this.m_length = length;
    }

    public IpfixLoggingWrapper(IpfixSetHeader setHeader) {
        this.m_setId = setHeader.getId();
        this.m_length = setHeader.getLength();
    }

    public int getSetId() {
        return m_setId;
    }

    public void setSetId(int setId) {
        this.m_setId = setId;
    }

    public int getLength() {
        return m_length;
    }

    public void setLength(int length) {
        this.m_length = length;
    }
}
