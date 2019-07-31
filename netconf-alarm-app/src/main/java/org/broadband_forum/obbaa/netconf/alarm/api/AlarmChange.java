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

package org.broadband_forum.obbaa.netconf.alarm.api;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public final class AlarmChange {

    private final AlarmChangeType m_type;
    private AlarmInfo m_info;
    private ModelNodeId m_subtree;
    private boolean m_toggling = false;
    private String m_mountKey;
    private String m_deviceId;

    private AlarmChange(AlarmChangeType type, AlarmInfo info, ModelNodeId subtree, String mountKey, String deviceId) {
        m_type = type;
        m_info = info;
        m_subtree = subtree;
        m_mountKey = mountKey;
        m_deviceId = deviceId;
    }

    public static AlarmChange createFromAlarmInfo(AlarmChangeType type, AlarmInfo info) {
        return new AlarmChange(type, info, null, null, null);
    }

    public static AlarmChange createForClearSubtree(ModelNodeId subtree, String mountKey) {
        return new AlarmChange(AlarmChangeType.CLEAR_SUBTREE, null, subtree, mountKey, null);
    }

    public static AlarmChange createForClearSubtree(String deviceId) {
        return new AlarmChange(AlarmChangeType.CLEAR_SUBTREE, null, null, null, deviceId);
    }

    public AlarmChangeType getType() {
        return m_type;
    }

    public AlarmInfo getAlarmInfo() {
        return m_info;
    }

    public ModelNodeId getSubtree() {
        return m_subtree;
    }


    public String getMountKey() {
        return m_mountKey;
    }

    public void setMountKey(String mountKey) {
        this.m_mountKey = mountKey;
    }

    public boolean isToggling() {
        return m_toggling;
    }

    public void setToggling(boolean toggling) {
        m_toggling = toggling;
    }

    public String getDeviceId() {
        return m_deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.m_deviceId = deviceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_info == null) ? 0 : m_info.hashCode());
        result = prime * result + ((m_subtree == null) ? 0 : m_subtree.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
        result = prime * result + ((m_deviceId == null) ? 0 : m_deviceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AlarmChange other = (AlarmChange) obj;
        if (m_info == null) {
            if (other.m_info != null) {
                return false;
            }
        } else if (!m_info.equals(other.m_info)) {
            return false;
        }
        if (m_subtree == null) {
            if (other.m_subtree != null) {
                return false;
            }
        } else if (!m_subtree.equals(other.m_subtree)) {
            return false;
        }
        if (m_type != other.m_type) {
            return false;
        }
        if (m_deviceId == null) {
            if (other.m_deviceId != null) {
                return false;
            }
        } else if (!m_deviceId.equals(other.m_deviceId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AlarmChange [m_type=" + m_type + ", m_info=" + m_info + ", m_subtree=" + m_subtree + ", m_deviceId=" + m_deviceId + "]";
    }
}
