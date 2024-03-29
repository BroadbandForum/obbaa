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

package org.broadband_forum.obbaa.nm.devicemanager;

import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;

/**
 * <p>
 * Provides APIs to do CRUD on a device.
 * Once a device is created via DeviceManager, it is called a "Managed Device".
 * </p>
 * Created by kbhatk on 29/9/17.
 */
public interface DeviceManager {

    Device getDevice(String deviceName);

    Device getDeviceWithSerialNumber(String serialNumber);

    Device getDeviceWithRegistrationId(String registrationId);

    List<Device> getAllDevices();

    void removeDeviceStateProvider(DeviceStateProvider stateProvider);

    void addDeviceStateProvider(DeviceStateProvider stateProvider);

    void deviceAdded(String deviceName);

    void deviceRemoved(String deviceName);

    void devicePropertyChanged(String deviceName);

    void updateConfigAlignmentState(String deviceName, String verdict);

    void updateOnuStateInfo(String deviceName, OnuStateInfo onuStateInfo);

    void updateEquipmentIdInOnuStateInfo(String deviceName, String equipmentId);

    void updateSoftwareImageInOnuStateInfo(String deviceName, Set<SoftwareImage> softwareImageSet);

    String getEndpointName(String deviceName, String terminationPoint, String networkFunctionName);
}
