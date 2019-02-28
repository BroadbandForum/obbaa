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


import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_MANAGEMENT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.INTERFACE_VERSION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MODEL;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.PUSH_PMA_CONFIGURATION_TO_DEVICE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.TYPE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.VENDOR;

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
@YangContainer(name = DEVICE_MANAGEMENT, namespace = NS)
public class DeviceMgmt {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = TYPE)
    @Column(name = "type")
    private String deviceType;

    @YangAttribute(name = INTERFACE_VERSION)
    @Column(name = "interface_version")
    private String deviceInterfaceVersion;

    @YangAttribute(name = MODEL)
    @Column(name = "model")
    private String deviceModel;

    @YangAttribute(name = VENDOR)
    @Column(name = "vendor")
    private String deviceVendor;

    @YangAttribute(name = PUSH_PMA_CONFIGURATION_TO_DEVICE)
    @Column(name = "push_pma_configuration_to_device")
    private String pushPmaConfigurationToDevice = "true";

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private DeviceConnection deviceConnection;

    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private DeviceState deviceState = new DeviceState();

    public Boolean isNetconf = true;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
        this.deviceState.setDeviceNodeId(parentId);
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceInterfaceVersion() {
        return deviceInterfaceVersion;
    }

    public void setDeviceInterfaceVersion(String deviceInterfaceVersion) {
        this.deviceInterfaceVersion = deviceInterfaceVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceVendor() {
        return deviceVendor;
    }

    public void setDeviceVendor(String deviceVendor) {
        this.deviceVendor = deviceVendor;
    }

    public String getPushPmaConfigurationToDevice() {
        return pushPmaConfigurationToDevice;
    }

    public void setPushPmaConfigurationToDevice(String pushPmaConfigurationToDevice) {
        this.pushPmaConfigurationToDevice = pushPmaConfigurationToDevice;
    }

    public DeviceConnection getDeviceConnection() {
        return deviceConnection;
    }

    public void setDeviceConnection(DeviceConnection deviceConnection) {
        this.deviceConnection = deviceConnection;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public boolean isNetconf() {
        return isNetconf;
    }

    public void setNetconf(boolean netconf) {
        isNetconf = netconf;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceMgmt that = (DeviceMgmt) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) {
            return false;
        }
        if (deviceType != null ? !deviceType.equals(that.deviceType) : that.deviceType != null) {
            return false;
        }
        if (deviceInterfaceVersion != null ? !deviceInterfaceVersion.equals(that.deviceInterfaceVersion)
            : that.deviceInterfaceVersion != null) {
            return false;
        }
        if (deviceModel != null ? !deviceModel.equals(that.deviceModel) : that.deviceModel != null) {
            return false;
        }
        if (deviceVendor != null ? !deviceVendor.equals(that.deviceVendor) : that.deviceVendor != null) {
            return false;
        }
        return pushPmaConfigurationToDevice != null
            ? pushPmaConfigurationToDevice.equals(that.pushPmaConfigurationToDevice)
            : that.pushPmaConfigurationToDevice == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (deviceType != null ? deviceType.hashCode() : 0);
        result = 31 * result + (deviceInterfaceVersion != null ? deviceInterfaceVersion.hashCode() : 0);
        result = 31 * result + (deviceModel != null ? deviceModel.hashCode() : 0);
        result = 31 * result + (deviceVendor != null ? deviceVendor.hashCode() : 0);
        result = 31 * result + (pushPmaConfigurationToDevice != null ? pushPmaConfigurationToDevice.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeviceMgmt{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append(", deviceType='").append(deviceType).append('\'');
        sb.append(", deviceInterfaceVersion='").append(deviceInterfaceVersion).append('\'');
        sb.append(", deviceModel='").append(deviceModel).append('\'');
        sb.append(", deviceVendor='").append(deviceVendor).append('\'');
        sb.append(", pushPmaConfigurationToDevice='").append(pushPmaConfigurationToDevice).append('\'');
        sb.append(", deviceConnection=").append(deviceConnection);
        sb.append(", deviceState=").append(deviceState);
        sb.append('}');
        return sb.toString();
    }
}
