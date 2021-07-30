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
 * Primary Key class for Network function
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public class NetworkFunctionPK implements Serializable {
    private String parentId;
    private String networkFunctionName;

    public NetworkFunctionPK() {
    }

    public NetworkFunctionPK(String parentId, String networkFunctionName) {
        this.parentId = parentId;
        this.networkFunctionName = networkFunctionName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getNetworkFunctionName() {
        return networkFunctionName;
    }

    public void setNetworkFunctionName(String networkFunctionName) {
        this.networkFunctionName = networkFunctionName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        NetworkFunctionPK networkFunctionPK = (NetworkFunctionPK) other;

        if (parentId != null ? !parentId.equals(networkFunctionPK.parentId) : networkFunctionPK.parentId != null) {
            return false;
        }
        return networkFunctionName != null ? networkFunctionName.equals(networkFunctionPK.networkFunctionName)
                : networkFunctionPK.networkFunctionName == null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (networkFunctionName != null ? networkFunctionName.hashCode() : 0);
        return result;
    }
}
