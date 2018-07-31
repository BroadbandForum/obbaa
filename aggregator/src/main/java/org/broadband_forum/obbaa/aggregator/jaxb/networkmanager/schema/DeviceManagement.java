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

package org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "device-management")
public class DeviceManagement {
    private String deviceType;
    private String deviceSoftwareVersion;
    private String deviceModel;
    private String deviceVendor;
    private String deviceState;

    private DeviceConnection deviceConnection;

    @XmlElement(name = "device-type")
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @XmlElement(name = "device-software-version")
    public String getDeviceSoftwareVersion() {
        return deviceSoftwareVersion;
    }

    public void setDeviceSoftwareVersion(String deviceSoftwareVersion) {
        this.deviceSoftwareVersion = deviceSoftwareVersion;
    }

    @XmlElement(name = "device-model")
    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    @XmlElement(name = "device-vendor")
    public String getDeviceVendor() {
        return deviceVendor;
    }

    public void setDeviceVendor(String deviceVendor) {
        this.deviceVendor = deviceVendor;
    }

    @XmlElement(name = "device-state")
    public String getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }

    @XmlElement
    public DeviceConnection getDeviceConnection() {
        return deviceConnection;
    }

    public void setDeviceConnection(DeviceConnection deviceConnection) {
        this.deviceConnection = deviceConnection;
    }
}
