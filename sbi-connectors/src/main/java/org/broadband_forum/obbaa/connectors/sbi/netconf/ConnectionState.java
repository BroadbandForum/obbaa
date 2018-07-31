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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

/**
 * Created by kbhatk on 10/30/17.
 */
public class ConnectionState {
    public static final ZoneId UTC = ZoneId.of("UTC");
    boolean m_isConnected = false;
    Date m_creationTime = NetconfResources.parseDateTime("1970-01-01T00:00:00+00:00").toDate();
    Set<String> m_capabilities = Collections.emptySet();

    public boolean isConnected() {
        return m_isConnected;
    }

    public void setConnected(boolean connected) {
        m_isConnected = connected;
    }

    public Date getCreationTime() {
        return m_creationTime;
    }

    public void setCreationTime(Date creationTime) {
        m_creationTime = creationTime;
    }

    public Set<String> getCapabilities() {
        return m_capabilities;
    }

    public void setCapabilities(Set<String> capabilities) {
        m_capabilities = capabilities;
    }

    public String getFormattedCreationTime() {
        return  DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofInstant(m_creationTime.toInstant(), UTC));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ConnectionState that = (ConnectionState) other;
        return m_isConnected == that.m_isConnected && Objects.equals(m_creationTime, that.m_creationTime)
                && Objects.equals(m_capabilities, that.m_capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_isConnected, m_creationTime, m_capabilities);
    }

    @Override
    public String toString() {
        return "ConnectionState{"
                + "m_isConnected=" + m_isConnected
                + ", m_creationTime=" + m_creationTime
                + '}';
    }
}
