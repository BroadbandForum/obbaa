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

package org.broadband_forum.obbaa.dmyang.entities;

import java.io.Serializable;

public class OnuManagementChainPK implements Serializable {
    private String parentId;
    private String nfName;
    private String nfType;

    public OnuManagementChainPK() {
    }

    public OnuManagementChainPK(String parentId, String nfName, String nfType) {
        this.parentId = parentId;
        this.nfName = nfName;
        this.nfType = nfType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getNfName() {
        return nfName;
    }

    public void setNfName(String nfName) {
        this.nfName = nfName;
    }

    public String getNfType() {
        return nfType;
    }

    public void setNfType(String nfType) {
        this.nfType = nfType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        OnuManagementChainPK onuManagementChainPK = (OnuManagementChainPK) other;

        if (parentId != null ? !parentId.equals(onuManagementChainPK.parentId) : onuManagementChainPK.parentId != null) {
            return false;
        }
        if (nfName != null ? !nfName.equals(onuManagementChainPK.nfName) : onuManagementChainPK.nfName != null) {
            return false;
        }
        return nfType != null ? nfType.equals(onuManagementChainPK.nfType)
                : onuManagementChainPK.nfType == null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (nfName != null ? nfName.hashCode() : 0);
        result = 31 * result + (nfType != null ? nfType.hashCode() : 0);
        return result;
    }
}
