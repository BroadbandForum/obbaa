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
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DevicePK;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.AbstractDao;

@Transactional(Transactional.TxType.MANDATORY)
public class DeviceDaoImpl extends AbstractDao<Device, DevicePK> implements DeviceDao {

    public static final String DEVICE_NAME = "deviceName";
    private static final String DUID = "duid";

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
        deviceState.setConfigAlignmentState(verdict);
    }


}
