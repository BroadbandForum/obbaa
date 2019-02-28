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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.io.Files;

public class PmaRegistryImplTest {

    private PmaRegistryImpl m_pmaRegistry;
    @Mock
    private NetconfConnectionManager m_cm;
    @Mock
    private DeviceManager m_dm;
    @Mock
    private Device m_device1Meta;

    private NetConfResponse m_response = new NetConfResponse().setOk(true).setMessageId("1");
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private NetConfServerImpl m_netconfServer;
    @Mock
    private NetconfDeviceAlignmentService m_das;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private SchemaRegistry m_schemaReg;
    @Mock
    private ModelNodeHelperRegistry m_modelNodeHelperReg;
    @Mock
    private SubSystemRegistry m_subsystemReg;
    @Mock
    private ModelNodeDSMRegistry m_modelNodeDsmReg;
    @Mock
    private AdapterContext m_adapterContext;
    @Mock
    private DeviceMgmt m_devMgmt;
    @Mock
    private DataStore m_dataStore;
    @Mock
    private DeviceAdapter m_deviceAdapter;
    private PmaRegistryImpl m_pmaRegistryTransparentSF;
    private File m_tempDir;
    private String m_dir;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_tempDir = Files.createTempDir();
        m_dir = m_tempDir.getAbsolutePath();
        m_pmaRegistry = new PmaRegistryImpl(m_dm, new PmaServerSessionFactory(m_dir, m_dm, m_netconfServer, m_das, m_entityRegistry,
                m_schemaReg, m_modelNodeDsmReg, m_adapterManager));
        m_pmaRegistryTransparentSF = new PmaRegistryImpl(m_dm, new TransparentPmaSessionFactory(m_cm, m_dm));
        when(m_cm.isConnected(m_device1Meta)).thenReturn(true);
        when(m_dm.getDevice("device1")).thenReturn(m_device1Meta);
        when(m_device1Meta.getDeviceManagement()).thenReturn(m_devMgmt);
        when(m_device1Meta.getDeviceName()).thenReturn("device1");
        when(m_devMgmt.getDeviceInterfaceVersion()).thenReturn("1.0");
        when(m_devMgmt.getDeviceVendor()).thenReturn("vendor1");
        when(m_devMgmt.getDeviceModel()).thenReturn("model1");
        when(m_devMgmt.getDeviceType()).thenReturn("dpu");
        Future<NetConfResponse> futureObject = mock(Future.class);
        when(futureObject.get()).thenReturn(m_response);
        when(m_cm.executeNetconf((Device) anyObject(), anyObject())).thenReturn(futureObject);
        when(m_adapterManager.getAdapterContext(any())).thenReturn(m_adapterContext);
        when(m_adapterManager.getDeviceAdapter(any())).thenReturn(m_deviceAdapter);
        when(m_netconfServer.getDataStore("running")).thenReturn(m_dataStore);
    }

    @Test
    public void testExceptionIsThrownWhenDeviceNotManaged() throws ExecutionException {
        try{
            m_pmaRegistry.executeNC("device2", "");
            fail("Expected an exception here");
        }catch (IllegalArgumentException e){
            assertEquals("Device not managed : device2", e.getMessage());
        }
    }

    @Ignore("This is applicable only in transparent mode")
    @Test
    public void testExceptionIsThrownWhenNoConnectionToDevice() throws ExecutionException {
        when(m_cm.isConnected(m_device1Meta)).thenReturn(false);
        try{
            m_pmaRegistry.executeNC("device1", "");
            fail("Expected an exception here");
        }catch (IllegalStateException e){
            assertEquals("Device not connected : device1", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithPmaSession() throws ExecutionException {
        String response = m_pmaRegistry.executeWithPmaSession("device1", session -> "response");
        assertEquals("response", response);
    }

    @Test
    public void testExecuteWithPmaSessionException() throws ExecutionException {
        when(m_adapterManager.getAdapterContext(any())).thenReturn(null);
        try {
            m_pmaRegistry.executeWithPmaSession("device1", session -> "response");
        } catch (RuntimeException e){
            assertEquals("Could not get session to Pma device1", e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFullResyncContactsDASTransparentSessionFactory() throws ExecutionException {
        m_pmaRegistryTransparentSF.forceAlign("device1");
    }

    @Test
    public void testFullResyncContactsDAS() throws ExecutionException {
        m_pmaRegistry.forceAlign("device1");
        verify(m_das).forceAlign(any(), any());
    }

    @Test
    public void testsyncContactsDAS() throws ExecutionException {
        m_pmaRegistry.align("device1");
        verify(m_das).align(m_device1Meta);
    }

    @Test
    public void testDeviceRemoved() throws ExecutionException {
        m_pmaRegistry.deviceRemoved("device1");
        m_pmaRegistry.executeWithPmaSession("device1", session -> "response");

    }

    @After
    public void teardown() throws Exception{
        deleteIfExists(m_tempDir);
    }

    private void deleteIfExists(File file) {
        if(file != null && file.exists()){
            file.delete();
        }
    }

}
