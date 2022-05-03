/* * Copyright 2021 Broadband Forum
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
 * limitations under the License.*/


package org.broadband_forum.obbaa.dmyang.entities;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMEMT_CHAIN;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;


@Entity
@IdClass(OnuManagementChainPK.class)
@YangList(name = ONU_MANAGEMEMT_CHAIN, namespace = ONU_MANAGEMENT_NS, revision = ONU_MANAGEMENT_REVISION)
public class OnuManagementChain implements Comparable<OnuManagementChain> {

    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @Column(name = "nfName")
    @YangAttribute(name = DeviceManagerNSConstants.NETWORK_FUNCTION_NAME)
    private String nfName;

    @Id
    @YangAttribute(name = DeviceManagerNSConstants.NETWORK_FUNCTION_TYPE)
    @Column(name = "nfType")
    private String nfType;

    @Column
    @YangOrderByUser
    private Integer insertOrder;

    public String getNfName() {
        return nfName;
    }

    public void setNfName(String nfName) {
        this.nfName = nfName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public Integer getInsertOrder() {
        return insertOrder;
    }

    public void setInsertOrder(Integer insertOrder) {
        this.insertOrder = insertOrder;
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
        OnuManagementChain that = (OnuManagementChain) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(that.parentId) : that.schemaPath != null) {
            return false;
        }
        if (insertOrder != null ? !insertOrder.equals(that.insertOrder) : that.insertOrder != null) {
            return false;
        }
        return nfName != null ? nfName.equals(that.nfName) : that.nfName == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (nfName != null ? nfName.hashCode() : 0);
        result = 31 * result + (insertOrder != null ? insertOrder.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(OnuManagementChain other) {
        return nfName.compareTo(other.getNfName());
    }
}
