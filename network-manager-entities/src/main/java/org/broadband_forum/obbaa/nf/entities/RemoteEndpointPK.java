/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.nf.entities;

import java.io.Serializable;

/**
 * <p>
 * Primary Key class for Remote Endpoint
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public class RemoteEndpointPK implements Serializable {
    private String parentId;
    private String remoteEndpointName;

    public RemoteEndpointPK() {
    }

    public RemoteEndpointPK(String parentId, String remoteEndpointName) {
        this.parentId = parentId;
        this.remoteEndpointName = remoteEndpointName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRemoteEndpointName() {
        return remoteEndpointName;
    }

    public void setRemoteEndpointName(String remoteEndpointName) {
        this.remoteEndpointName = remoteEndpointName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        RemoteEndpointPK remoteEndpointPK = (RemoteEndpointPK) other;

        if (parentId != null ? !parentId.equals(remoteEndpointPK.parentId) : remoteEndpointPK.parentId != null) {
            return false;
        }
        return remoteEndpointName != null ? remoteEndpointName.equals(remoteEndpointPK.remoteEndpointName)
                : remoteEndpointPK.remoteEndpointName == null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (remoteEndpointName != null ? remoteEndpointName.hashCode() : 0);
        return result;
    }
}
