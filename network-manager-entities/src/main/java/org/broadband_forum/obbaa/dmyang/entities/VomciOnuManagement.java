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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = DeviceManagerNSConstants.VOMCI_ONU_MANAGEMENT, namespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
        revision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
public class VomciOnuManagement {

    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = DeviceManagerNSConstants.USE_VOMCI_MANAGEMENT)
    @Column(name = "useVomciManagement")
    private String useVomciManagement;

    @YangAttribute(name = DeviceManagerNSConstants.VOMCI_FUNCTION)
    @Column(name = "vomciFunction")
    private String vomciFunction;

    @YangLeafList(name = DeviceManagerNSConstants.ONU_MANAGEMEMT_CHAIN)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<OnuManagementChain> onuManagementChains = new LinkedHashSet<>();

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private NetworkFunctionLinks networkFunctionLinks;

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

    public String getVomciFunction() {
        return vomciFunction;
    }

    public void setVomciFunction(String vomciFunction) {
        this.vomciFunction = vomciFunction;
    }


    public String getUseVomciManagement() {
        return useVomciManagement;
    }

    public void setUseVomciManagement(String useVomciManagement) {
        this.useVomciManagement = useVomciManagement;
    }


    public Set<OnuManagementChain> getOnuManagementChains() {
        return onuManagementChains;
    }

    public void setOnuManagementChains(Set<OnuManagementChain> onuManagementChains) {
        this.onuManagementChains = onuManagementChains;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        VomciOnuManagement that = (VomciOnuManagement) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }

        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) {
            return false;
        }
        if (useVomciManagement != null ? !useVomciManagement.equals(that.useVomciManagement) : that.useVomciManagement != null) {
            return false;
        }
        if (vomciFunction != null ? !vomciFunction.equals(that.vomciFunction) : that.vomciFunction != null) {
            return false;
        }
        return onuManagementChains != null ? onuManagementChains.equals(that.onuManagementChains) :
                that.onuManagementChains == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (useVomciManagement != null ? useVomciManagement.hashCode() : 0);
        result = 31 * result + (vomciFunction != null ? vomciFunction.hashCode() : 0);
        result = 31 * result + (onuManagementChains != null ? onuManagementChains.hashCode() : 0);
        return result;
    }

    public NetworkFunctionLinks getNetworkFunctionLinks() {
        return networkFunctionLinks;
    }

    public void setNetworkFunctionLinks(NetworkFunctionLinks networkFunctionLinks) {
        this.networkFunctionLinks = networkFunctionLinks;
    }
}
