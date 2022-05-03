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

@Entity
public class OnuStateInfo {
    @Id
    @Column(name = "onu_state_info_id")
    private String onuStateInfoId;

    @Column(name = "onuState")
    private String onuState;

    @Column(name = "vendorId")
    private String vendorId;

    @Column(name = "equipmentId")
    private String equipmentId;


    @YangAttribute(name = DeviceManagerNSConstants.DETERMINED_ONU_MANAGEMENT_MODE,
            attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    @Column(name = "determinedOnuManagementMode")
    private String determinedOnuManagementMode;

    @YangAttributeNS(belongsToAttribute = DeviceManagerNSConstants.DETERMINED_ONU_MANAGEMENT_MODE,
            attributeNamespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
            attributeRevision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
    @Column(name = "determinedOnuManagementModeNs")
    private String determinedOnuManagementModeNs;

    @Column(name = "detectedSerialNumber")
    private String detectedSerialNumber;

    @Column(name = "detectedRegistrationId")
    private String detectedRegistrationId;

    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private ActualAttachmentPoint actualAttachmentPoint;

    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private SoftwareImages softwareImages;

    public SoftwareImages getSoftwareImages() {
        return softwareImages;
    }

    public void setSoftwareImages(SoftwareImages softwareImages) {
        this.softwareImages = softwareImages;
    }

    public String getOnuStateInfoId() {
        return onuStateInfoId;
    }

    public void setOnuStateInfoId(String onuStateId) {
        this.onuStateInfoId = onuStateId;
    }

    public String getOnuState() {
        return onuState;
    }

    public void setOnuState(String onuState) {
        this.onuState = onuState;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }


    public String getDetermineOnuManagementMode() {
        return determinedOnuManagementMode;
    }

    public void setDetermineOnuManagementMode(String determineOnuManagementMode) {
        this.determinedOnuManagementMode = determineOnuManagementMode;
    }

    public String getDetectedSerialNumber() {
        return detectedSerialNumber;
    }

    public void setDetectedSerialNumber(String detectedSerialNumber) {
        this.detectedSerialNumber = detectedSerialNumber;
    }

    public String getDetectedRegistrationId() {
        return detectedRegistrationId;
    }

    public void setDetectedRegistrationId(String detectedRegistrationId) {
        this.detectedRegistrationId = detectedRegistrationId;
    }

    public ActualAttachmentPoint getActualAttachmentPoint() {
        return actualAttachmentPoint;
    }

    public void setActualAttachmentPoint(ActualAttachmentPoint actualAttachmentPoint) {
        this.actualAttachmentPoint = actualAttachmentPoint;
    }

    public String getDetermineOnuManagementModeNs() {
        return determinedOnuManagementModeNs;
    }

    public void setDetermineOnuManagementModeNs(String determineOnuManagementModeNs) {
        this.determinedOnuManagementModeNs = determineOnuManagementModeNs;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        OnuStateInfo that = (OnuStateInfo) other;

        if (onuStateInfoId != null ? !onuStateInfoId.equals(that.onuStateInfoId) : that.onuStateInfoId != null) {
            return false;
        }
        if (onuState != null ? !onuState.equals(that.onuState) : that.onuState != null) {
            return false;
        }
        if (vendorId != null ? !vendorId.equals(that.vendorId) : that.vendorId != null) {
            return false;
        }
        if (equipmentId != null ? !equipmentId.equals(that.equipmentId) : that.equipmentId != null) {
            return false;
        }
        if (determinedOnuManagementMode != null ? !determinedOnuManagementMode.equals(that.determinedOnuManagementMode) :
                that.determinedOnuManagementMode != null) {
            return false;
        }
        if (detectedSerialNumber != null ? !detectedSerialNumber.equals(that.detectedSerialNumber) : that.detectedSerialNumber != null) {
            return false;
        }
        if (detectedRegistrationId != null ? !detectedRegistrationId.equals(that.detectedRegistrationId) :
                that.detectedRegistrationId != null) {
            return false;
        }
        return actualAttachmentPoint != null ? !actualAttachmentPoint.equals(that.actualAttachmentPoint)
                : that.actualAttachmentPoint != null;

    }

    @Override
    public int hashCode() {
        int result = onuStateInfoId != null ? onuStateInfoId.hashCode() : 0;
        result = 31 * result + (onuState != null ? onuState.hashCode() : 0);
        result = 31 * result + (vendorId != null ? vendorId.hashCode() : 0);
        result = 31 * result + (equipmentId != null ? equipmentId.hashCode() : 0);
        result = 31 * result + (determinedOnuManagementMode != null ? determinedOnuManagementMode.hashCode() : 0);
        result = 31 * result + (detectedSerialNumber != null ? detectedSerialNumber.hashCode() : 0);
        result = 31 * result + (detectedRegistrationId != null ? detectedRegistrationId.hashCode() : 0);
        result = 31 * result + (actualAttachmentPoint != null ? actualAttachmentPoint.hashCode() : 0);
        return result;
    }
}
