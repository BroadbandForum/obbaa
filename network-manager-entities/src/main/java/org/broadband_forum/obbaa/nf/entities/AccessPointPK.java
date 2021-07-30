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
 * Primary Key class for Access Point
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public class AccessPointPK implements Serializable {
    private String parentId;
    private String accessPointName;

    public AccessPointPK() {
    }

    public AccessPointPK(String parentId, String accessPointName) {
        this.parentId = parentId;
        this.accessPointName = accessPointName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getAccessPointName() {
        return accessPointName;
    }

    public void setAccessPointName(String accessPointName) {
        this.accessPointName = accessPointName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        AccessPointPK accessPointPK = (AccessPointPK) other;

        if (parentId != null ? !parentId.equals(accessPointPK.parentId) : accessPointPK.parentId != null) {
            return false;
        }
        return accessPointName != null ? accessPointName.equals(accessPointPK.accessPointName) : accessPointPK.accessPointName == null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (accessPointName != null ? accessPointName.hashCode() : 0);
        return result;
    }
}
