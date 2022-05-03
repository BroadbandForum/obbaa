/*
 * Copyright 2020 Broadband Forum
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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = DeviceManagerNSConstants.ONU_CONFIG_INFO, namespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
        revision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
public class OnuConfigInfo {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = DeviceManagerNSConstants.EXPECTED_SERIAL_NUMBER)
    @Column(name = "expectedSerialNumber")
    private String expectedSerialNumber;

    @YangAttribute(name = DeviceManagerNSConstants.EXPECTED_REGISTRATION_ID)
    @Column(name = "expectedRegistrationId")
    private String expectedRegistrationId;

    @YangAttribute(name = DeviceManagerNSConstants.VENDOR_NAME)
    @Column(name = "vendorName")
    private String vendorName;

    @YangAttribute(name = DeviceManagerNSConstants.XPON_TECHNOLOGY, attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    @Column(name = "xponTechnology")
    private String xponTechnology;

    @YangAttributeNS(belongsToAttribute = DeviceManagerNSConstants.XPON_TECHNOLOGY,
            attributeNamespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
            attributeRevision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
    @Column(name = "xponTechnology_ns")
    private String xponTechnologyNs;

    @YangAttribute(name = DeviceManagerNSConstants.PLANNED_ONU_MANAGEMENT_MODE, attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    @Column(name = "plannedOnuManagementMode")
    private String plannedOnuManagementMode;

    @YangAttributeNS(belongsToAttribute = DeviceManagerNSConstants.PLANNED_ONU_MANAGEMENT_MODE,
            attributeNamespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
            attributeRevision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
    @Column(name = "plannedOnuManagementMode_ns")
    private String plannedOnuManagementModeNs;

    @YangChild
    @OneToOne(targetEntity = ExpectedAttachmentPoints.class, cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private ExpectedAttachmentPoints expectedAttachmentPoints;

    @YangChild
    @OneToOne(targetEntity = VomciOnuManagement.class, cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private VomciOnuManagement vomciOnuManagement;

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

    public String getExpectedSerialNumber() {
        return expectedSerialNumber;
    }

    public void setExpectedSerialNumber(String serialNumber) {
        this.expectedSerialNumber = serialNumber;
    }

    public String getExpectedRegistrationId() {
        return expectedRegistrationId;
    }

    public void setExpectedRegistrationId(String registrationId) {
        this.expectedRegistrationId = registrationId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public ExpectedAttachmentPoints getExpectedAttachmentPoints() {
        return expectedAttachmentPoints;
    }

    public void setExpectedAttachmentPoints(ExpectedAttachmentPoints expectedAttachmentPoints) {
        this.expectedAttachmentPoints = expectedAttachmentPoints;
    }


    public String getPlannedOnuManagementMode() {
        return plannedOnuManagementMode;
    }

    public void setPlannedOnuManagementMode(String plannedOnuManagementMode) {
        this.plannedOnuManagementMode = plannedOnuManagementMode;
    }

    public String getXponTechnology() {
        return xponTechnology;
    }

    public void setXponTechnology(String xponTechnology) {
        this.xponTechnology = xponTechnology;
    }

    public VomciOnuManagement getVomciOnuManagement() {
        return vomciOnuManagement;
    }

    public void setVomciOnuManagement(VomciOnuManagement vomciOnuManagement) {
        this.vomciOnuManagement = vomciOnuManagement;
    }

    public String getXponTechnologyNs() {
        return xponTechnologyNs;
    }

    public void setXponTechnologyNs(String xponTechnologyNs) {
        this.xponTechnologyNs = xponTechnologyNs;
    }

    public String getPlannedOnuManagementModeNs() {
        return plannedOnuManagementModeNs;
    }

    public void setPlannedOnuManagementModeNs(String plannedOnuManagementModeNs) {
        this.plannedOnuManagementModeNs = plannedOnuManagementModeNs;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        OnuConfigInfo that = (OnuConfigInfo) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) {
            return false;
        }
        if (expectedSerialNumber != null ? !expectedSerialNumber.equals(that.expectedSerialNumber) : that.expectedSerialNumber != null) {
            return false;
        }
        if (vendorName != null ? !vendorName.equals(that.vendorName) : that.vendorName != null) {
            return false;
        }
        if (xponTechnology != null ? !xponTechnology.equals(that.xponTechnology) : that.xponTechnology != null) {
            return false;
        }
        if (xponTechnologyNs != null ? !xponTechnologyNs.equals(that.xponTechnologyNs) : that.xponTechnologyNs != null) {
            return false;
        }
        if (vomciOnuManagement != null ? !vomciOnuManagement.equals(that.vomciOnuManagement) : that.vomciOnuManagement != null) {
            return false;
        }
        if (plannedOnuManagementMode != null ? !plannedOnuManagementMode.equals(that.plannedOnuManagementMode) :
                that.plannedOnuManagementMode != null) {
            return false;
        }
        return expectedRegistrationId != null
                ? expectedRegistrationId.equals(that.expectedRegistrationId) : that.expectedRegistrationId == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (expectedSerialNumber != null ? expectedSerialNumber.hashCode() : 0);
        result = 31 * result + (expectedRegistrationId != null ? expectedRegistrationId.hashCode() : 0);
        result = 31 * result + (vendorName != null ? vendorName.hashCode() : 0);
        result = 31 * result + (xponTechnology != null ? xponTechnology.hashCode() : 0);
        result = 31 * result + (xponTechnologyNs != null ? xponTechnologyNs.hashCode() : 0);
        result = 31 * result + (vomciOnuManagement != null ? vomciOnuManagement.hashCode() : 0);
        result = 31 * result + (plannedOnuManagementMode != null ? plannedOnuManagementMode.hashCode() : 0);
        result = 31 * result + (plannedOnuManagementModeNs != null ? plannedOnuManagementModeNs.hashCode() : 0);
        return result;
    }
}
