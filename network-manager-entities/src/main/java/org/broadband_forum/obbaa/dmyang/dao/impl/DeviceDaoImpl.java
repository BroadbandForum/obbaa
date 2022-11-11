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

package org.broadband_forum.obbaa.dmyang.dao.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.entities.DevicePK;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.NetworkFunctionLink;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.TerminationPointA;
import org.broadband_forum.obbaa.dmyang.entities.TerminationPointB;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.AbstractDao;

@Transactional(Transactional.TxType.MANDATORY)
public class DeviceDaoImpl extends AbstractDao<Device, DevicePK> implements DeviceDao {

    private static final Logger LOGGER = Logger.getLogger(DeviceDaoImpl.class);

    public static final String DEVICE_NAME = "deviceName";
    private static final String DUID = "duid";
    private static final String FUNCTION_NAME = "functionName";

    public DeviceDaoImpl(PersistenceManagerUtil persistenceManagerUtil) {
        super(persistenceManagerUtil, Device.class);
    }

    @Override
    public List<Device> findAllDevices() {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        return entityDataStoreManager.findAll(Device.class);
    }

    @Override
    public Device getDeviceByName(String deviceName) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        Map<String, Object> matchedValues = new HashMap<String, Object>();
        matchedValues.put(JPAEntityDataStoreManager.buildQueryPath(DEVICE_NAME), deviceName);
        List<Device> devices = entityDataStoreManager.findByMatchValue(Device.class, matchedValues);
        if (devices != null && !devices.isEmpty()) {
            return devices.get(0);
        }
        return null;
    }

    @Override
    public Device findDeviceWithDuid(String duid) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        List<Device> devices = entityDataStoreManager.findAll(Device.class);

        for (Device device : devices) {
            if (device.isCallhome()) {
                if (device.getDeviceManagement().getDeviceConnection().getDuid().equals(duid)) {
                    return device;
                }
            }
        }
        return null;
    }

    @Override
    public Device findDeviceWithSerialNumber(String serialNumber) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        List<Device> devices = entityDataStoreManager.findAll(Device.class);

        for (Device device : devices) {
            if (device.getDeviceManagement().getDeviceType().equals(DeviceManagerNSConstants.DEVICE_TYPE_ONU)) {
                if (device.getDeviceManagement().getOnuConfigInfo().getExpectedSerialNumber().equals(serialNumber)) {
                    return device;
                }
            }
        }
        return null;
    }

    @Override
    public Device findDeviceWithRegistrationId(String registrationId) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        List<Device> devices = entityDataStoreManager.findAll(Device.class);

        for (Device device : devices) {
            if (device.getDeviceManagement().getDeviceType().equals(DeviceManagerNSConstants.DEVICE_TYPE_ONU)) {
                if (device.getDeviceManagement().getOnuConfigInfo().getExpectedRegistrationId().equals(registrationId)) {
                    return device;
                }
            }
        }
        return null;
    }

    @Override
    public DeviceState getDeviceState(String deviceName) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        if (deviceName != null) {
            String deviceNodeId = "/container=network-manager/container=managed-devices/container=device/name=" + deviceName;
            return entityDataStoreManager.findById(DeviceState.class, deviceNodeId, LockModeType.PESSIMISTIC_WRITE);
        }
        return null;
    }

    @Override
    public void updateDeviceAlignmentState(String deviceName, String verdict) {
        DeviceState deviceState = getDeviceState(deviceName);
        if (deviceState != null) {
            deviceState.setConfigAlignmentState(verdict);
        } else {
            LOGGER.error(String.format("Device %s doesn't exist " , deviceName));
        }
    }

    @Override
    public void updateOnuStateInfo(String deviceName, OnuStateInfo onuStateInfo) {
        DeviceState deviceState = getDeviceState(deviceName);
        if (deviceState != null) {
            deviceState.setOnuStateInfo(onuStateInfo);
        } else {
            LOGGER.error(String.format("Device %s doesn't exist " , deviceName));
        }
    }

    @Override
    public String getVomciFunctionName(String deviceName) {
        Device device = getDeviceByName(deviceName);
        String vomciFunction = null;
        if (device != null && device.getDeviceManagement().getDeviceType().equals(DeviceManagerNSConstants.DEVICE_TYPE_ONU)) {
            vomciFunction = device.getDeviceManagement().getOnuConfigInfo().getVomciOnuManagement().getVomciFunction();
        }
        return vomciFunction;
    }

    @Override
    public OnuManagementChain[] getOnuManagementChains(String deviceName) {
        Integer minInsertOrderVal = -1;
        Device device = getDeviceByName(deviceName);
        Set<OnuManagementChain> onuManagementChainSet = new LinkedHashSet<>();
        if (device != null && device.getDeviceManagement().getDeviceType().equals(DeviceManagerNSConstants.DEVICE_TYPE_ONU)) {
            onuManagementChainSet = device.getDeviceManagement().getOnuConfigInfo().getVomciOnuManagement().getOnuManagementChains();
        }

        OnuManagementChain[] onuManagementChainArray = new OnuManagementChain[onuManagementChainSet.size()];
        //insert order is being incremented globally for all ONUs
        for (OnuManagementChain managementChain : onuManagementChainSet) {
            if ((minInsertOrderVal < 0) || (managementChain.getInsertOrder() < minInsertOrderVal)) {
                minInsertOrderVal = managementChain.getInsertOrder();
            }
        }
        for (OnuManagementChain managementChain : onuManagementChainSet) {
            onuManagementChainArray[managementChain.getInsertOrder() - minInsertOrderVal] = managementChain;
        }
        return onuManagementChainArray;
    }

    @Override
    public String getRemoteEndpointName(String deviceName, String terminationPoint, String networkFunctionName) {
        String remoteEndpointName = null;
        Device device = getDeviceByName(deviceName);
        if (device != null) {
            Set<NetworkFunctionLink> networkFunctionLinks = device.getDeviceManagement().getOnuConfigInfo().getVomciOnuManagement()
                    .getNetworkFunctionLinks().getNetworkFunctionLink();
            for (NetworkFunctionLink networkFnLink : networkFunctionLinks) {
                if (terminationPoint.equals(DeviceManagerNSConstants.TERMINATION_POINT_A)) {
                    TerminationPointA terminationPointa = networkFnLink.getTerminationPointA();
                    if (terminationPointa.getFunctionName().equals(networkFunctionName)) {
                        remoteEndpointName = terminationPointa.getLocalEndpointName();
                    }
                } else {
                    TerminationPointB terminationPointB = networkFnLink.getTerminationPointB();
                    if (terminationPointB.getFunctionName().equals(networkFunctionName)) {
                        remoteEndpointName = terminationPointB.getLocalEndpointName();
                    }
                }
            }
        }
        return remoteEndpointName;
    }
}
