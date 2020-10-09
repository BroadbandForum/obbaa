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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.AUTHENTICATION_METHOD;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_CONFIG_INFO;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.REGISTRATION_ID;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SERIAL_NUMBER;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.VENDOR_NAME;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.XPON_TECHNOLOGY;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = ONU_CONFIG_INFO, namespace = ONU_MANAGEMENT_NS, revision = ONU_MANAGEMENT_REVISION)
public class OnuConfigInfo {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = SERIAL_NUMBER)
    @Column(name = "serialNumber")
    private String serialNumber;

    @YangAttribute(name = REGISTRATION_ID)
    @Column(name = "registrationId")
    private String registrationId;

    @YangAttribute(name = VENDOR_NAME)
    @Column(name = "vendorName")
    private String vendorName;

    @YangAttribute(name = AUTHENTICATION_METHOD)
    @Column(name = "authenticationMethod")
    private String authenticationMethod;

    @YangAttribute(name = XPON_TECHNOLOGY)
    @Column(name = "xponTechnology")
    private String xponTechnology;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private AttachmentPoint attachmentPoint;

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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public AttachmentPoint getAttachmentPoint() {
        return attachmentPoint;
    }

    public void setAttachmentPoint(AttachmentPoint attachmentPoint) {
        this.attachmentPoint = attachmentPoint;
    }

    public String getXponTechnology() {
        return xponTechnology;
    }

    public void setXponTechnology(String xponTechnology) {
        this.xponTechnology = xponTechnology;
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
        if (serialNumber != null ? !serialNumber.equals(that.serialNumber) : that.serialNumber != null) {
            return false;
        }
        if (attachmentPoint != null ? !attachmentPoint.equals(that.attachmentPoint) : that.attachmentPoint != null) {
            return false;
        }
        if (vendorName != null ? !vendorName.equals(that.vendorName) : that.vendorName != null) {
            return false;
        }
        if (authenticationMethod != null ? !authenticationMethod.equals(that.authenticationMethod) : that.authenticationMethod != null) {
            return false;
        }
        if (xponTechnology != null ? !xponTechnology.equals(that.xponTechnology) : that.xponTechnology != null) {
            return false;
        }
        return registrationId != null ? registrationId.equals(that.registrationId) : that.registrationId == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
        result = 31 * result + (registrationId != null ? registrationId.hashCode() : 0);
        result = 31 * result + (attachmentPoint != null ? attachmentPoint.hashCode() : 0);
        result = 31 * result + (vendorName != null ? vendorName.hashCode() : 0);
        result = 31 * result + (authenticationMethod != null ? authenticationMethod.hashCode() : 0);
        result = 31 * result + (xponTechnology != null ? xponTechnology.hashCode() : 0);
        return result;
    }
}
