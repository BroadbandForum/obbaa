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

import java.sql.Timestamp;

import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public class AlarmParameters {

    private String m_alarmTypeId;

    private ModelNodeId m_resource;

    private Timestamp m_lastStatusChange;

    private AlarmSeverity m_lastPerceivedSeverity;

    private String m_lastAlarmText;

    private String m_alarmTypeQualifier;

    private String m_mountKey;

    private String m_resourceObjectString;

    private String m_resourceNamespaces;

    public AlarmParameters(String alarmTypeId, ModelNodeId resource, Timestamp lastStatusChange, AlarmSeverity lastPerceivedSeverity,
                           String lastAlarmText) {
        this(alarmTypeId, "", resource, lastStatusChange, lastPerceivedSeverity, lastAlarmText);
    }

    public AlarmParameters(String alarmTypeId, String alarmTypeQualifier, ModelNodeId resource, Timestamp lastStatusChange,
                           AlarmSeverity lastPerceivedSeverity,
                           String lastAlarmText) {
        this(alarmTypeId, alarmTypeQualifier, resource, lastStatusChange, lastPerceivedSeverity, lastAlarmText, null, null);
    }

    public AlarmParameters(String alarmTypeId, String alarmTypeQualifier, ModelNodeId resource, Timestamp lastStatusChange,
                           AlarmSeverity lastPerceivedSeverity,
                           String lastAlarmText, String mountKey, String resourceObjectString) {
        this.m_alarmTypeId = alarmTypeId;
        this.m_alarmTypeQualifier = alarmTypeQualifier;
        this.m_resource = resource;
        this.m_lastStatusChange = lastStatusChange;
        this.m_lastPerceivedSeverity = lastPerceivedSeverity;
        this.m_lastAlarmText = lastAlarmText;
        this.m_mountKey = mountKey;
        this.m_resourceObjectString = resourceObjectString;
    }

    public String getAlarmTypeId() {
        return m_alarmTypeId;
    }

    public String getAlarmTypeQualifier() {
        return m_alarmTypeQualifier;
    }

    public ModelNodeId getResource() {
        return m_resource;
    }

    public Timestamp getLastStatusChange() {
        return m_lastStatusChange;
    }

    public AlarmSeverity getLastPerceivedSeverity() {
        return m_lastPerceivedSeverity;
    }

    public String getLastAlarmText() {
        return m_lastAlarmText;
    }

    public String getMountKey() {
        return m_mountKey;
    }

    public void setMountKey(String mountKey) {
        this.m_mountKey = mountKey;
    }

    public String getResourceObjectString() {
        return m_resourceObjectString;
    }

    public void setResourceObjectString(String resourceObjectString) {
        this.m_resourceObjectString = resourceObjectString;
    }

    public String getResourceNamespaces() {
        return m_resourceNamespaces;
    }

    public void setResourceNamespaces(String resourceNamespaces) {
        this.m_resourceNamespaces = resourceNamespaces;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alarmTypeId == null) ? 0 : m_alarmTypeId.hashCode());
        result = prime * result + ((m_alarmTypeQualifier == null) ? 0 : m_alarmTypeQualifier.hashCode());
        result = prime * result + ((m_lastAlarmText == null) ? 0 : m_lastAlarmText.hashCode());
        result = prime * result + ((m_lastPerceivedSeverity == null) ? 0 : m_lastPerceivedSeverity.hashCode());
        result = prime * result + ((m_lastStatusChange == null) ? 0 : m_lastStatusChange.hashCode());
        result = prime * result + ((m_resource == null) ? 0 : m_resource.hashCode());
        result = prime * result + ((m_resourceObjectString == null) ? 0 : m_resourceObjectString.hashCode());
        result = prime * result + ((m_resourceNamespaces == null) ? 0 : m_resourceNamespaces.hashCode());
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
        AlarmParameters other = (AlarmParameters) obj;
        if (m_alarmTypeId == null) {
            if (other.m_alarmTypeId != null) {
                return false;
            }
        } else if (!m_alarmTypeId.equals(other.m_alarmTypeId)) {
            return false;
        }
        if (m_alarmTypeQualifier == null) {
            if (other.m_alarmTypeQualifier != null) {
                return false;
            }
        } else if (!m_alarmTypeQualifier.equals(other.m_alarmTypeQualifier)) {
            return false;
        }
        if (m_lastAlarmText == null) {
            if (other.m_lastAlarmText != null) {
                return false;
            }
        } else if (!m_lastAlarmText.equals(other.m_lastAlarmText)) {
            return false;
        }
        if (m_lastPerceivedSeverity != other.m_lastPerceivedSeverity) {
            return false;
        }
        if (m_lastStatusChange == null) {
            if (other.m_lastStatusChange != null) {
                return false;
            }
        } else if (!m_lastStatusChange.equals(other.m_lastStatusChange)) {
            return false;
        }
        if (m_resource == null) {
            if (other.m_resource != null) {
                return false;
            }
        } else if (!m_resource.equals(other.m_resource)) {
            return false;
        }
        if (m_resourceObjectString == null) {
            if (other.m_resourceObjectString != null) {
                return false;
            }
        } else if (!m_resourceObjectString.equals(other.m_resourceObjectString)) {
            return false;
        }
        if (m_resourceNamespaces == null) {
            if (other.m_resourceNamespaces != null) {
                return false;
            }
        } else if (!m_resourceNamespaces.equals(other.m_resourceNamespaces)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AlarmParameters [alarmTypeId=" + m_alarmTypeId + ", resource=" + m_resource
                + ", lastStatusChange=" + m_lastStatusChange + ", lastPerceivedSeverity="
                + m_lastPerceivedSeverity + ", lastAlarmText=" + m_lastAlarmText + ", resourceObjectString="
                + m_resourceObjectString + ", resourceNamespaces=" + m_resourceNamespaces + "]";
    }

}
