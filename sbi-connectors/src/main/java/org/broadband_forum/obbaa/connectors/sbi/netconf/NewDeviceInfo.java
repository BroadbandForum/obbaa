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

package org.broadband_forum.obbaa.connectors.sbi.netconf;

import java.util.Objects;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;

public class NewDeviceInfo {
    private final String m_duid;
    private final NetconfClientSession m_deviceSession;

    public NewDeviceInfo(String duid, NetconfClientSession deviceSession) {
        m_duid = duid;
        m_deviceSession = deviceSession;
    }

    public String getDuid() {
        return m_duid;
    }

    public Set<String> getCapabilities() {
        return m_deviceSession.getServerCapabilities();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        NewDeviceInfo that = (NewDeviceInfo) other;
        return Objects.equals(m_duid, that.m_duid) && Objects.equals(m_deviceSession, that.m_deviceSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_duid, m_deviceSession);
    }

    public void closeSession() {
        m_deviceSession.closeAsync();
    }
}
