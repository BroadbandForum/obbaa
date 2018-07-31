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

package org.broadband_forum.obbaa.store.dm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.store.dm.CallHomeInfo;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.broadband_forum.obbaa.store.dm.SshConnectionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceAdminStoreImplTest {
    private DeviceAdminStoreImpl m_adminStore;
    private File m_dbFile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_dbFile = File.createTempFile("DeviceAdminStoreImplTest", "DeviceAdminStoreImplTest");
        m_adminStore = new DeviceAdminStoreImpl(m_dbFile.getPath());
        m_dbFile.delete();
        m_adminStore.init();
    }

    @After
    public void tearDown() {
        m_adminStore.destroy();
        m_dbFile.delete();
    }

    @Test
    public void testCreatedAndRetrieve() {
        DeviceInfo deviceInfo = createDevice("Bangalore-south");
        assertEquals(deviceInfo, m_adminStore.get("Bangalore-south"));
    }

    private DeviceInfo createDevice(String deviceName) {
        DeviceInfo deviceInfo = new DeviceInfo(deviceName, new SshConnectionInfo("10.1.1.1", 1234, "user", "pass"));
        m_adminStore.create(deviceInfo);
        return deviceInfo;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTwiceTthrowsException() {
        createDevice("Bangalore-south");
        createDevice("Bangalore-south");
    }

    @Test
    public void testUpdate() {
        DeviceInfo firstMeta = createDevice("Bangalore-south");
        DeviceInfo updatedMeta = new DeviceInfo("Bangalore-south", new SshConnectionInfo("10.1.1.3", 1234, "user", "pass"));
        assertNotEquals(firstMeta, updatedMeta);
        m_adminStore.update(updatedMeta);
        assertEquals(updatedMeta, m_adminStore.get("Bangalore-south"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeviceMetaUpdateThrowsExceptionWhenDeviceDoesIsNotCreated() {
        DeviceInfo deviceInfo = new DeviceInfo("Bangalore-south", new SshConnectionInfo("10.1.1.1", 1234, "user", "pass"));
        m_adminStore.update(deviceInfo);
    }

    @Test
    public void testDelete() {
        createDevice("Bangalore-south");
        m_adminStore.delete("Bangalore-south");
        assertNull(m_adminStore.get("Bangalore-south"));
    }

    @Test
    public void testGetAllDevicesReturnsAllDevices() {
        DeviceInfo meta1 = createDevice("Bangalore-south");
        DeviceInfo meta2 = createDevice("Bangalore-north");
        DeviceInfo meta3 = createDevice("Chennai-east");
        DeviceInfo meta4 = createDevice("Chennai-west");
        Set<DeviceInfo> expectedDevices = new HashSet<>();
        expectedDevices.add(meta1);
        expectedDevices.add(meta2);
        expectedDevices.add(meta3);
        expectedDevices.add(meta4);

        Set<DeviceInfo> actualDevices = m_adminStore.getAllEntries();
        assertEquals(expectedDevices, actualDevices);
    }

    @Test
    public void testGetCallHomeDeviceWithDuid() {
        DeviceInfo callHomeDevice = createCallHomeDevice("Bangalore-south");
        assertEquals(callHomeDevice, m_adminStore.getCallHomeDeviceWithDuid("Bangalore-south"));
    }

    private DeviceInfo createCallHomeDevice(String deviceName) {
        DeviceInfo deviceInfo = new DeviceInfo(deviceName, new CallHomeInfo(deviceName));
        m_adminStore.create(deviceInfo);
        return deviceInfo;
    }
}
