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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.broadband_forum.obbaa.store.Value;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceInfo implements Value<String> {
    private CallHomeInfo m_deviceCallHomeInfo;
    private String m_key;
    private SshConnectionInfo m_deviceConnectionInfo;
    private Map<String, Object> m_deviceState = new LinkedHashMap<>();

    public DeviceInfo() {
    }

    public DeviceInfo(String key, SshConnectionInfo deviceConnectionInfo) {
        m_key = key;
        m_deviceConnectionInfo = deviceConnectionInfo;
    }

    public DeviceInfo(String key, CallHomeInfo deviceCallHomeInfo) {
        m_key = key;
        m_deviceCallHomeInfo = deviceCallHomeInfo;
    }

    public SshConnectionInfo getDeviceConnectionInfo() {
        return m_deviceConnectionInfo;
    }

    public void setDeviceConnectionInfo(SshConnectionInfo deviceConnenctionInfo) {
        m_deviceConnectionInfo = deviceConnenctionInfo;
        m_deviceCallHomeInfo = null;
    }

    public CallHomeInfo getDeviceCallHomeInfo() {
        return m_deviceCallHomeInfo;
    }

    public void setDeviceCallHomeInfo(CallHomeInfo deviceCallHomeInfo) {
        m_deviceCallHomeInfo = deviceCallHomeInfo;
        m_deviceConnectionInfo = null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DeviceInfo that = (DeviceInfo) other;
        return Objects.equals(m_deviceCallHomeInfo, that.m_deviceCallHomeInfo)
                && Objects.equals(m_key, that.m_key)
                && Objects.equals(m_deviceConnectionInfo, that.m_deviceConnectionInfo);
    }

    @Override
    public int hashCode() {

        return Objects.hash(m_deviceCallHomeInfo, m_key, m_deviceConnectionInfo, m_deviceState);
    }

    @Override
    public String toString() {
        return "DeviceInfo{"
                + "m_deviceCallHomeInfo=" + m_deviceCallHomeInfo
                + ", m_key='" + m_key + '\''
                + ", m_deviceConnectionInfo="
                + m_deviceConnectionInfo
                + '}';
    }

    public Map<String, Object> getDeviceState() {
        return m_deviceState;
    }

    @Override
    public String getKey() {
        return m_key;
    }

    public void setKey(String key) {
        m_key = key;
    }

    public boolean isCallHome() {
        return m_deviceCallHomeInfo != null;
    }
}
