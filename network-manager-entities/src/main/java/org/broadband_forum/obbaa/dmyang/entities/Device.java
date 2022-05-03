/*
 * Copyright 2018 Broadband Forum
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
import javax.persistence.IdClass;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Entity
@IdClass(DevicePK.class)
@YangList(name = "device", namespace = DeviceManagerNSConstants.NS)
public class Device implements Comparable<Device>, PmaResource {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;
    @Id
    @YangListKey(name = DeviceManagerNSConstants.NAME)
    @Column(name = "name")
    private String deviceName;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private DeviceMgmt deviceManagement;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private DeviceNotification deviceNotification;

    @YangParentSchemaPath
    public static final SchemaPath getParentSchemaPath() {
        return DeviceManagerNSConstants.MANAGED_DEVICES_SP;
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceMgmt getDeviceManagement() {
        return deviceManagement;
    }

    public void setDeviceManagement(DeviceMgmt deviceManagement) {
        this.deviceManagement = deviceManagement;
    }

    public DeviceNotification getDeviceNotification() {
        return deviceNotification;
    }

    public void setDeviceNotification(DeviceNotification deviceNotification) {
        this.deviceNotification = deviceNotification;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Device device = (Device) other;

        if (parentId != null ? !parentId.equals(device.parentId) : device.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(device.schemaPath) : device.schemaPath != null) {
            return false;
        }
        return deviceName != null ? deviceName.equals(device.deviceName) : device.deviceName == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Device{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append(", deviceName='").append(deviceName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Device other) {
        return deviceName.compareTo(other.getDeviceName());
    }

    public boolean isNeverAligned() {
        return DeviceManagerNSConstants.NEVER_ALIGNED.equals(deviceManagement.getDeviceState().getConfigAlignmentState());
    }

    public boolean isAligned() {
        return DeviceManagerNSConstants.ALIGNED.equals(deviceManagement.getDeviceState().getConfigAlignmentState()) && !isInError();
    }

    public boolean isInError() {
        return deviceManagement.getDeviceState().getConfigAlignmentState().startsWith(DeviceManagerNSConstants.IN_ERROR);
    }

    public boolean isAlignmentUnknown() {
        return DeviceManagerNSConstants.ALIGNMENT_UNKNOWN.equals(deviceManagement.getDeviceState().getConfigAlignmentState());
    }

    public boolean isCallhome() {
        return deviceManagement.getDeviceConnection().getConnectionModel().equals(DeviceManagerNSConstants.CALL_HOME);
    }

    public boolean isSnmp() {
        return deviceManagement.getDeviceConnection().getConnectionModel().equals(DeviceManagerNSConstants.SNMP);
    }

    public boolean isMediatedSession() {
        return deviceManagement.getDeviceConnection().getConnectionModel().equals(DeviceManagerNSConstants.MEDIATED_SESSION);
    }

    public AlignmentOption getAlignmentOption() {
        if ("true".equals(deviceManagement.getPushPmaConfigurationToDevice())) {
            return AlignmentOption.PUSH;
        } else {
            return AlignmentOption.PULL;
        }
    }
}
