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

package org.broadband_forum.obbaa.pma.impl;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.io.Files;

public class PmaServerSessionFactoryTest {
    private PmaServerSessionFactory m_factory;
    @Mock
    private DeviceManager m_dm;
    @Mock
    private NetConfServerImpl m_netconfServer;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    @Mock
    private SubSystemRegistry m_subsystemRegistry;
    @Mock
    private ModelNodeDSMRegistry m_modelNodeDsmRegistry;
    private File m_tempDir;
    private File m_tempFile;
    private String m_deviceKey = "ONT1";
    @Mock
    private Device m_device;
    @Mock
    private NetconfDeviceAlignmentService m_das;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private DeviceMgmt m_deviceMgmt;
    @Mock
    private AdapterContext m_adapterContext;
    @Mock
    DeviceAdapter m_deviceAdapter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_tempDir = Files.createTempDir();
        String deviceFileBaseDir = m_tempDir.getAbsolutePath();
        m_factory = new PmaServerSessionFactory(deviceFileBaseDir, m_dm, m_netconfServer, m_das, m_entityRegistry, m_schemaRegistry,
                m_modelNodeDsmRegistry, m_adapterManager);
        when(m_dm.getDevice(m_deviceKey)).thenReturn(m_device);
        when(m_device.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_device.getDeviceManagement().getDeviceType()).thenReturn("dpu");
        when(m_device.getDeviceManagement().getDeviceInterfaceVersion()).thenReturn("1.0");
        when(m_device.getDeviceManagement().getDeviceModel()).thenReturn("4LT");
        when(m_device.getDeviceManagement().getDeviceVendor()).thenReturn("Vendor1");
        when(m_adapterManager.getAdapterContext(any())).thenReturn(m_adapterContext);
        when(m_adapterManager.getDeviceAdapter(any())).thenReturn(m_deviceAdapter);
    }

    @After
    public void tearDown(){
        deleteIfExists(m_tempDir);
        deleteIfExists(m_tempFile);
    }

    private void deleteIfExists(File file) {
        if(file != null && file.exists()){
            file.delete();
        }
    }

    @Test
    public void testRegistryThrowsExceptionIfPathIsNatADirectory() throws IOException {
        m_tempFile = File.createTempFile("tempfile", ".txt");
        String deviceFileBaseDir = m_tempFile.getAbsolutePath();
        try {
            new PmaServerSessionFactory(deviceFileBaseDir, m_dm, m_netconfServer, m_das, m_entityRegistry, m_schemaRegistry,
                    m_modelNodeDsmRegistry, m_adapterManager).init();
            fail("Expected an exception to be thrown here");
        }catch (Exception e){
            assertTrue(e instanceof RuntimeException);
            assertEquals(m_tempFile.getAbsolutePath()+" is not a directory", e.getMessage());
        }
    }

    @Test
    public void testFacoryCreatesPmaServerSessions() throws Exception {
        PmaSession pmaServer = m_factory.create(m_deviceKey);
        assertTrue(pmaServer instanceof PmaServerSession);

    }
}
