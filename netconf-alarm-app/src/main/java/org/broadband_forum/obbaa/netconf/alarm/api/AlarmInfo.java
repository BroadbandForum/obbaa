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
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class AlarmInfo {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AlarmInfo.class, LogAppNames.NETCONF_ALARM);

    private static final int ADDITIONAL_INFO_COLUMN_SIZE = 1000;

    private String m_alarmTypeId;

    private String m_alarmTypeQualifier;

    private ModelNodeId m_sourceObject;

    private Timestamp m_time;

    private AlarmSeverity m_severity;

    private String m_alarmText;

    private String m_deviceName;

    private String m_sourceObjectString;

    private String m_sourceObjectNamespaces;

    public AlarmInfo(String alarmTypeId, ModelNodeId sourceObject, Timestamp time, AlarmSeverity severity,
                     String alarmText, String deviceName) {
        this(alarmTypeId, "", sourceObject, time, severity, alarmText, deviceName);
    }

    public AlarmInfo(String alarmTypeId, String alarmTypeQualifier, ModelNodeId sourceObject, Timestamp time, AlarmSeverity severity,
                     String alarmText, String deviceName) {
        this(alarmTypeId, alarmTypeQualifier, sourceObject, time, severity, alarmText, deviceName, null);
    }

    public AlarmInfo(String alarmTypeId, String alarmTypeQualifier, ModelNodeId sourceObject, String sourceObjectNamespaces,
                     Timestamp eventTime, AlarmSeverity severity, String alarmText, String deviceName, String resourceString) {
        this(alarmTypeId, alarmTypeQualifier, sourceObject, eventTime, severity, alarmText, deviceName, resourceString);
        this.m_sourceObjectNamespaces = sourceObjectNamespaces;
    }

    public AlarmInfo(String alarmTypeId, String alarmTypeQualifier, String resource, Timestamp time, AlarmSeverity severity,
                     String alarmText, String deviceName) {
        this(alarmTypeId, alarmTypeQualifier, null, time, severity, alarmText, deviceName, resource);
    }

    public AlarmInfo(String alarmTypeId, String alarmTypeQualifier, ModelNodeId sourceObject, Timestamp time, AlarmSeverity severity,
                     String alarmText, String deviceName, String sourceObjectString) {
        this.m_alarmTypeId = alarmTypeId;
        this.m_alarmTypeQualifier = alarmTypeQualifier;
        this.m_sourceObject = sourceObject;
        this.m_time = time;
        this.m_severity = severity;
        if (alarmText != null) {
            if (alarmText.length() > ADDITIONAL_INFO_COLUMN_SIZE) {
                LOGGER.info(null,
                        "Additional Info size exceeded. Trimmed to expected size. Original Text:" + alarmText);
                alarmText = alarmText.substring(0, ADDITIONAL_INFO_COLUMN_SIZE);
            }
        }
        this.m_alarmText = alarmText;
        this.m_deviceName = deviceName;
        this.m_sourceObjectString = sourceObjectString;
    }

    public String getAlarmTypeId() {
        return m_alarmTypeId;
    }

    public String getAlarmTypeQualifier() {
        return m_alarmTypeQualifier;
    }

    public ModelNodeId getSourceObject() {
        return m_sourceObject;
    }

    public void setSourceObject(ModelNodeId sourceObject) {
        this.m_sourceObject = sourceObject;
    }

    public Timestamp getTime() {
        return m_time;
    }

    public AlarmSeverity getSeverity() {
        return m_severity;
    }

    public String getAlarmText() {
        return m_alarmText;
    }

    public String getDeviceName() {
        return m_deviceName;
    }

    public String getSourceObjectString() {
        return m_sourceObjectString;
    }

    public void setSourceObjectString(String sourceObjectString) {
        this.m_sourceObjectString = sourceObjectString;
    }

    public String getSourceObjectNamespaces() {
        return m_sourceObjectNamespaces;
    }

    public void setSourceObjectNamespaces(String sourceObjectNamespaces) {
        this.m_sourceObjectNamespaces = sourceObjectNamespaces;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alarmText == null) ? 0 : m_alarmText.hashCode());
        result = prime * result + ((m_alarmTypeId == null) ? 0 : m_alarmTypeId.hashCode());
        result = prime * result + ((m_alarmTypeQualifier == null) ? 0 : m_alarmTypeQualifier.hashCode());
        result = prime * result + ((m_time == null) ? 0 : m_time.hashCode());
        result = prime * result + ((m_severity == null) ? 0 : m_severity.hashCode());
        result = prime * result + ((m_sourceObject == null) ? 0 : m_sourceObject.hashCode());
        result = prime * result + ((m_deviceName == null) ? 0 : m_deviceName.hashCode());
        result = prime * result + ((m_sourceObjectString == null) ? 0 : m_sourceObjectString.hashCode());
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
        AlarmInfo other = (AlarmInfo) obj;
        if (m_alarmText == null) {
            if (other.m_alarmText != null) {
                return false;
            }
        } else if (!m_alarmText.equals(other.m_alarmText)) {
            return false;
        }
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
        if (m_time == null) {
            if (other.m_time != null) {
                return false;
            }
        } else if (!m_time.equals(other.m_time)) {
            return false;
        }
        if (m_severity != other.m_severity) {
            return false;
        }
        if (m_sourceObject == null) {
            if (other.m_sourceObject != null) {
                return false;
            }
        } else if (!m_sourceObject.equals(other.m_sourceObject)) {
            return false;
        }
        if (m_deviceName == null) {
            if (other.m_deviceName != null) {
                return false;
            }
        } else if (!m_deviceName.equals(other.m_deviceName)) {
            return false;
        }
        if (m_sourceObjectString == null) {
            if (other.m_sourceObjectString != null) {
                return false;
            }
        } else if (!m_sourceObjectString.equals(other.m_sourceObjectString)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AlarmInfo [alarmTypeId=" + m_alarmTypeId + ", alarmTypeQualifier=" + m_alarmTypeQualifier
                + ", sourceObject=" + m_sourceObject + ", eventTime=" + m_time + ", severity=" + m_severity
                + ", alarmText=" + m_alarmText + ", deviceName=" + m_deviceName
                + ", sourceObjectString=" + m_sourceObjectString
                + ", sourceObjectNamespaces=" + m_sourceObjectNamespaces + "]";
    }

}
