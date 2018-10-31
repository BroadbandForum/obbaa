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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.util.AbstractUtilTxTest;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;
import org.junit.Before;
import org.junit.Test;

public class DeviceDaoImplTest extends AbstractUtilTxTest {

    public static final String DEVICE_A = "DeviceA";
    public static final String DEVICE_B = "DeviceB";
    public static final String CONTAINER_MANAGED_DEVICES = "/container=managed-devices";
    public static final String DEVICE_C = "DeviceC";
    public static final String HSQL = "testhsql";
    private DeviceDao m_deviceDao;
    private PersistenceManagerUtil m_persistenceMgrUtil;
    private JPAEntityDataStoreManager m_emSpy;

    @Before
    public void setup() {
        JPAEntityManagerFactory factory = new JPAEntityManagerFactory(HSQL);
        m_persistenceMgrUtil = new ThreadLocalPersistenceManagerUtil(factory) {
            protected EntityDataStoreManager createEntityDataStoreManager() {
                JPAEntityDataStoreManager jpaEntityDataStoreManager = new JPAEntityDataStoreManager(getEntityManagerFactory());
                m_emSpy = spy(jpaEntityDataStoreManager);
                return m_emSpy;
            }
        };
        m_deviceDao = new DeviceDaoImpl(m_persistenceMgrUtil);
    }

    private void createTwoDevices() {
        Device device = new Device();
        device.setDeviceName(DEVICE_A);
        device.setParentId(CONTAINER_MANAGED_DEVICES);

        m_deviceDao.create(device);

        device = new Device();
        device.setDeviceName(DEVICE_B);
        device.setParentId(CONTAINER_MANAGED_DEVICES);

        m_deviceDao.create(device);
    }

    @Test
    public void createTwoDeviceAndAssert() throws Exception {
        beginTx();
        createTwoDevices();

        List<Device> deviceList = m_deviceDao.findAll();
        assertEquals(2, deviceList.size());

        assertEquals(DEVICE_A, m_deviceDao.getDeviceByName(DEVICE_A).getDeviceName());
        assertEquals(DEVICE_B, m_deviceDao.getDeviceByName(DEVICE_B).getDeviceName());
        assertNull(m_deviceDao.getDeviceByName(DEVICE_C));
        m_deviceDao.deleteAll();
        closeTx();
    }

    @Test
    public void deleteOneDeviceAndAssert() throws Exception {
        beginTx();
        createTwoDevices();


        List<Device> deviceList = m_deviceDao.findAll();
        assertEquals(2, deviceList.size());

        Device deviceA = m_deviceDao.getDeviceByName(DEVICE_A);
        Device deviceB = m_deviceDao.getDeviceByName(DEVICE_B);

        assertEquals(DEVICE_A, deviceA.getDeviceName());
        assertEquals(DEVICE_B, deviceB.getDeviceName());
        assertNull(m_deviceDao.getDeviceByName(DEVICE_C));

        m_deviceDao.delete(deviceA);

        deviceList = m_deviceDao.findAll();
        assertEquals(1, deviceList.size());
        deviceA = m_deviceDao.getDeviceByName(DEVICE_A);
        deviceB = m_deviceDao.getDeviceByName(DEVICE_B);
        assertNull(deviceA);
        assertEquals(DEVICE_B, deviceB.getDeviceName());
        m_deviceDao.deleteAll();
        closeTx();
    }

    @Override
    protected PersistenceManagerUtil getPersistenceManagerUtil() {
        return m_persistenceMgrUtil;
    }
}