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

package org.broadband_forum.obbaa.dmyang.entities;


import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEVER_ALIGNED;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DeviceState {
    @Id
    @Column(name = "device_node_id")
    private String deviceNodeId;

    @Column(name = "configuration_alignment_state", length = 100000)
    private String configAlignmentState = NEVER_ALIGNED;

    public String getDeviceNodeId() {
        return deviceNodeId;
    }

    public void setDeviceNodeId(String deviceNodeId) {
        this.deviceNodeId = deviceNodeId;
    }

    public String getConfigAlignmentState() {
        return configAlignmentState;
    }

    public void setConfigAlignmentState(String configAlignmentState) {
        this.configAlignmentState = configAlignmentState;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceState that = (DeviceState) other;

        if (deviceNodeId != null ? !deviceNodeId.equals(that.deviceNodeId) : that.deviceNodeId != null) {
            return false;
        }
        return configAlignmentState != null ? configAlignmentState.equals(that.configAlignmentState) : that.configAlignmentState == null;

    }

    @Override
    public int hashCode() {
        int result = deviceNodeId != null ? deviceNodeId.hashCode() : 0;
        result = 31 * result + (configAlignmentState != null ? configAlignmentState.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeviceState{");
        sb.append("deviceNodeId='").append(deviceNodeId).append('\'');
        sb.append(", configAlignmentState='").append(configAlignmentState).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
