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
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
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
    private DeviceInfo m_deviceInfo;
    @Mock
    private NetconfDeviceAlignmentService m_das;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_tempDir = Files.createTempDir();
        String deviceFileBaseDir = m_tempDir.getAbsolutePath();
        m_factory = new PmaServerSessionFactory(deviceFileBaseDir, m_dm, m_netconfServer, m_das, m_entityRegistry, m_schemaRegistry,
                m_modelNodeHelperRegistry, m_subsystemRegistry, m_modelNodeDsmRegistry);
        when(m_dm.getDevice(m_deviceKey)).thenReturn(m_deviceInfo);
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
                    m_modelNodeHelperRegistry, m_subsystemRegistry, m_modelNodeDsmRegistry).init();
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
