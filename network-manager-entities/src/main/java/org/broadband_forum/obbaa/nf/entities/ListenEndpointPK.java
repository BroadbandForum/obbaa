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
 * Primary Key class for Listen Endpoint
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public class ListenEndpointPK implements Serializable {
    private String parentId;
    private String listenEndpointName;

    public ListenEndpointPK() {
    }

    public ListenEndpointPK(String parentId, String listenEndpointName) {
        this.parentId = parentId;
        this.listenEndpointName = listenEndpointName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getListenEndpointName() {
        return listenEndpointName;
    }

    public void setListenEndpointName(String listenEndpointName) {
        this.listenEndpointName = listenEndpointName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ListenEndpointPK listenEndpointPK = (ListenEndpointPK) other;

        if (parentId != null ? !parentId.equals(listenEndpointPK.parentId) : listenEndpointPK.parentId != null) {
            return false;
        }
        return listenEndpointName != null ? listenEndpointName.equals(listenEndpointPK.listenEndpointName)
                : listenEndpointPK.listenEndpointName == null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (listenEndpointName != null ? listenEndpointName.hashCode() : 0);
        return result;
    }
}
