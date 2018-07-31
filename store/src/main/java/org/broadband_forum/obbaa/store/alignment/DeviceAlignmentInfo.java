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

package org.broadband_forum.obbaa.store.alignment;

import org.broadband_forum.obbaa.store.Value;

public class DeviceAlignmentInfo implements Value<String> {
    public static final String ALIGNED = "Aligned";
    public static final String IN_ERROR = "In Error";
    public static final String NEVER_ALIGNED = "Never Aligned";
    private String m_deviceName;
    private String m_verdict = NEVER_ALIGNED;

    public DeviceAlignmentInfo() {
    }

    public DeviceAlignmentInfo(String deviceName) {
        m_deviceName = deviceName;
    }

    @Override
    public String getKey() {
        return m_deviceName;
    }

    public void setKey(String deviceName) {
        m_deviceName = deviceName;
    }

    public String getVerdict() {
        return m_verdict;
    }

    public void setVerdict(String verdict) {
        m_verdict = verdict;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceAlignmentInfo that = (DeviceAlignmentInfo) other;

        return m_deviceName != null ? m_deviceName.equals(that.m_deviceName) : that.m_deviceName == null;
    }

    @Override
    public int hashCode() {
        return m_deviceName != null ? m_deviceName.hashCode() : 0;
    }

    public boolean isNeverAligned() {
        return NEVER_ALIGNED.equals(getVerdict());
    }

    public boolean isInError() {
        return getVerdict().startsWith(IN_ERROR);
    }

    public boolean isAligned() {
        return ALIGNED.equals(getVerdict()) && !isInError();
    }

    public String getName() {
        return m_deviceName;
    }
}
