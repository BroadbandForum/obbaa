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

package org.broadband_forum.obbaa.store.dm;

import java.io.Serializable;

public class SshConnectionInfo implements Serializable {
    private String m_ip;
    private int m_port;
    private String m_username;
    private String m_password;

    public SshConnectionInfo() {
    }

    public SshConnectionInfo(String ip, int port, String username, String password) {
        m_ip = ip;
        m_port = port;
        m_username = username;
        m_password = password;
    }


    public String getIp() {
        return m_ip;
    }

    public void setIp(String ip) {
        m_ip = ip;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        SshConnectionInfo that = (SshConnectionInfo) other;

        if (m_port != that.m_port) {
            return false;
        }
        if (m_ip != null ? !m_ip.equals(that.m_ip) : that.m_ip != null) {
            return false;
        }
        if (m_username != null ? !m_username.equals(that.m_username) : that.m_username != null) {
            return false;
        }
        return m_password != null ? m_password.equals(that.m_password) : that.m_password == null;
    }

    @Override
    public int hashCode() {
        int result = m_ip != null ? m_ip.hashCode() : 0;
        result = 31 * result + m_port;
        result = 31 * result + (m_username != null ? m_username.hashCode() : 0);
        result = 31 * result + (m_password != null ? m_password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshConnectionInfo{");
        sb.append("m_ip='").append(m_ip).append('\'');
        sb.append(", m_port=").append(m_port);
        sb.append(", m_username='").append(m_username).append('\'');
        sb.append(", m_password='").append(m_password).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
