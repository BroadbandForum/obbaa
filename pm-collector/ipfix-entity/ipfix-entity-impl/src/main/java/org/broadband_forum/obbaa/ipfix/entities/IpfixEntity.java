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

package org.broadband_forum.obbaa.ipfix.entities;

import java.io.Serializable;

import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

public class IpfixEntity implements Serializable {

    private byte[] m_presentation;

    public IpfixEntity(byte[] data) {
        this.setPresentation(data);
    }

    public byte[] getPresentation() {
        return m_presentation;
    }

    public void setPresentation(byte[] presentation) {
        this.m_presentation = presentation;
    }

    public String getPresentationAsString() {
        return IpfixUtilities.bytesToHex(m_presentation);
    }
}
