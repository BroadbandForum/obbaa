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

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;

public class ConnectionState {
    private String connectionNodeId;

    private boolean connected = false;

    private Date connectionCreationTime = NetconfResources.parseDateTime("1970-01-01T00:00:00+00:00").toDate();

    private Set<String> deviceCapability = Collections.emptySet();

    public String getConnectionNodeId() {
        return connectionNodeId;
    }

    public void setConnectionNodeId(String connectionNodeId) {
        this.connectionNodeId = connectionNodeId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Date getConnectionCreationTime() {
        return connectionCreationTime;
    }

    public String getConnectionCreationTimeFormat() {
        return NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(new DateTime(connectionCreationTime));
    }

    public void setConnectionCreationTime(Date connectionCreationTime) {
        this.connectionCreationTime = connectionCreationTime;
    }

    public Set<String> getDeviceCapability() {
        return deviceCapability;
    }

    public void setDeviceCapability(Set<String> deviceCapability) {
        this.deviceCapability = deviceCapability;
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

        if (connected != that.connected) {
            return false;
        }
        if (connectionNodeId != null ? !connectionNodeId.equals(that.connectionNodeId) : that.connectionNodeId != null) {
            return false;
        }
        if (connectionCreationTime != null
            ? !connectionCreationTime.equals(that.connectionCreationTime) : that.connectionCreationTime != null) {
            return false;
        }
        return deviceCapability != null ? deviceCapability.equals(that.deviceCapability) : that.deviceCapability == null;

    }

    @Override
    public int hashCode() {
        int result = connectionNodeId != null ? connectionNodeId.hashCode() : 0;
        result = 31 * result + (connected ? 1 : 0);
        result = 31 * result + (connectionCreationTime != null ? connectionCreationTime.hashCode() : 0);
        result = 31 * result + (deviceCapability != null ? deviceCapability.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionState{");
        sb.append("connectionNodeId='").append(connectionNodeId).append('\'');
        sb.append(", connected=").append(connected);
        sb.append(", connectionCreationTime=").append(connectionCreationTime);
        sb.append(", deviceCapability=").append(deviceCapability);
        sb.append('}');
        return sb.toString();
    }
}
