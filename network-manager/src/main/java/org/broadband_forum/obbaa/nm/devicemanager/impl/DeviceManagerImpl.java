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

package org.broadband_forum.obbaa.nm.devicemanager.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.dao.SoftwareImageDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceStateProvider;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceManagerImpl implements DeviceManager {
    private DeviceDao m_deviceDao;
    private SoftwareImageDao m_softwareImageDao;
    private Set<DeviceStateProvider> m_deviceStateProviders = new LinkedHashSet<>();

    public DeviceManagerImpl(DeviceDao deviceDao, SoftwareImageDao softwareImageDao) {
        m_deviceDao = deviceDao;
        m_softwareImageDao = softwareImageDao;
    }

    public void setDeviceDao(DeviceDao deviceDao) {
        m_deviceDao = deviceDao;
    }

    public void setSoftwareImageDao(SoftwareImageDao softwareImageDao) {
        m_softwareImageDao = softwareImageDao;
    }

    @Override
    public void deviceAdded(String deviceName) {
        for (DeviceStateProvider provider : m_deviceStateProviders) {
            provider.deviceAdded(deviceName);
        }
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public Device getDevice(String deviceName) {
        Device device = m_deviceDao.getDeviceByName(deviceName);
        if (device == null) {
            throw new IllegalArgumentException("Device " + deviceName + " does not exist");
        }
        return device;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public Device getDeviceWithSerialNumber(String serialNumber) {
        Device device = m_deviceDao.findDeviceWithSerialNumber(serialNumber);
        if (device == null) {
            throw new IllegalArgumentException("Device with serial-number " + serialNumber + " does not exist");
        }
        return device;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public Device getDeviceWithRegistrationId(String registrationId) {
        Device device = m_deviceDao.findDeviceWithRegistrationId(registrationId);
        if (device == null) {
            throw new IllegalArgumentException("Device with registration id " + registrationId + " does not exist");
        }
        return device;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public List<Device> getAllDevices() {
        List<Device> allDevices = m_deviceDao.findAllDevices();
        return allDevices;
    }

    @Override
    public void deviceRemoved(String deviceName) {
        for (DeviceStateProvider provider : m_deviceStateProviders) {
            provider.deviceRemoved(deviceName);
        }
    }

    @Override
    public void devicePropertyChanged(String deviceName) {
        deviceRemoved(deviceName);
        deviceAdded(deviceName);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public void updateConfigAlignmentState(String deviceName, String verdict) {
        m_deviceDao.updateDeviceAlignmentState(deviceName, verdict);
    }

    @Override
    public void updateOnuStateInfo(String deviceName, OnuStateInfo onuStateInfo) {
        m_deviceDao.updateOnuStateInfo(deviceName, onuStateInfo);
    }

    @Override
    public void removeDeviceStateProvider(DeviceStateProvider stateProvider) {
        m_deviceStateProviders.remove(stateProvider);
    }

    @Override
    public void addDeviceStateProvider(DeviceStateProvider stateProvider) {
        m_deviceStateProviders.add(stateProvider);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public void updateEquipmentIdInOnuStateInfo(String deviceName, String equipmentId) {
        String existingEqptId = m_deviceDao.getDeviceState(deviceName).getOnuStateInfo().getEquipmentId();
        if (!equipmentId.equals(existingEqptId)) {
            m_deviceDao.getDeviceState(deviceName).getOnuStateInfo().setEquipmentId(equipmentId);
        }
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public void updateSoftwareImageInOnuStateInfo(String deviceName, Set<SoftwareImage> softwareImageSet) {
        m_softwareImageDao.updateSWImageInOnuStateInfo(m_deviceDao.getDeviceState(deviceName), softwareImageSet);

    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public String getEndpointName(String deviceName, String terminationPoint, String networkFunctionName) {
        String endpointName = null;
        if (m_deviceDao.getDeviceByName(deviceName).isMediatedSession()) {
            endpointName = m_deviceDao.getRemoteEndpointName(deviceName, terminationPoint, networkFunctionName);
        }
        return endpointName;
    }


}
