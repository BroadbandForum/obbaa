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
import java.util.Objects;

public class SoftwareImagePK implements Serializable {
    private String parentId;
    private int id;

    public SoftwareImagePK() {
    }

    public SoftwareImagePK(String parentId, int id) {
        this.parentId = parentId;
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        SoftwareImagePK softwareImagePK = (SoftwareImagePK) other;

        if (parentId != null ? !parentId.equals(softwareImagePK.parentId) : softwareImagePK.parentId != null) {
            return false;
        }
        return Objects.equals(id, softwareImagePK.id);
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (String.valueOf(id) != null ? String.valueOf(id).hashCode() : 0);
        return result;
    }
}
