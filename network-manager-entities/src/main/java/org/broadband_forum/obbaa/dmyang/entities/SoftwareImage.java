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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;

@Entity
@IdClass(SoftwareImagePK.class)
@YangList
public class SoftwareImage implements Comparable<SoftwareImage> {

    @Id
    @Column(name = "parent_id")
    String parentId;

    @Id
    @YangListKey(name = DeviceManagerNSConstants.SOFTWARE_IMAGE_ID)
    @Column(name = "id")
    private int id;

    @YangAttribute(name = DeviceManagerNSConstants.SOFWARE_IMAGE_VERSION)
    @Column(name = "version")
    private String version;

    @YangAttribute(name = DeviceManagerNSConstants.SOFTWARE_IMAGE_IS_COMMITTED)
    @Column(name = "is_committed")
    private Boolean isCommitted;

    @YangAttribute(name = DeviceManagerNSConstants.SOFTWARE_IMAGE_IS_ACTIVE)
    @Column(name = "is_active")
    private Boolean isActive;

    @YangAttribute(name = DeviceManagerNSConstants.SOFTWARE_IMAGE_IS_VALID)
    @Column(name = "is_valid")
    private Boolean isValid;

    @YangAttribute(name = DeviceManagerNSConstants.SOFTWARE_IMAGE_PRODUCT_CODE)
    @Column(name = "product_code")
    private String productCode;

    @YangAttribute(name = DeviceManagerNSConstants.SOFTWARE_IMAGE_HASH)
    @Column(name = "hash")
    private String hash;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getIsCommitted() {
        return isCommitted;
    }

    public void setIsCommitted(Boolean committed) {
        isCommitted = committed;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "SoftwareImage{"
                + "parentId='" + parentId + '\''
                + ", id=" + id
                + ", version='" + version + '\''
                + ", isCommitted=" + isCommitted
                + ", isActive=" + isActive
                + ", isValid=" + isValid
                + ", productCode='" + productCode + '\''
                + ", hash='" + hash + '\''
                + '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SoftwareImage that = (SoftwareImage) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        if (isCommitted != null ? !isCommitted.equals(that.isCommitted) : that.isCommitted != null) {
            return false;
        }
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) {
            return false;
        }
        if (isValid != null ? !isValid.equals(that.isValid) : that.isValid != null) {
            return false;
        }

        if (productCode != null ? !productCode.equals(that.productCode) : that.productCode != null) {
            return false;
        }

        if (hash != null ? !hash.equals(that.hash) : that.hash != null) {
            return false;
        }
        return id == that.id;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (productCode != null ? productCode.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (isCommitted != null ? isCommitted.hashCode() : 0);
        result = 31 * result + (isValid != null ? isValid.hashCode() : 0);
        result = 31 * result + (id);
        return result;
    }

    @Override
    public int compareTo(SoftwareImage other) {
        return String.valueOf(id).compareTo(String.valueOf(other.getId()));
    }

}
