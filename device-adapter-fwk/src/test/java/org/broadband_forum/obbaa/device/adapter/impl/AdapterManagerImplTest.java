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

package org.broadband_forum.obbaa.device.adapter.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdapterManagerImplTest {

    private AdapterManager m_adapterManager;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    @Mock
    private SubSystemRegistry m_subSystemRegistry;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDataStoreManager;
    @Mock
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    @Mock
    private ModelNodeHelperDeployer m_modelNodeHelperDeployer;
    private ReadWriteLockService m_readWriteLockService;
    private InputStream m_inputStream;
    Map<URL, InputStream> m_moduleStream = new HashMap<>();
    private InputStream m_inputStream2;
    private DeviceAdapter m_deviceAdapter1;
    private DeviceAdapter m_deviceAdapter2;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private SubSystem m_subSystem;

    public AdapterManagerImplTest() {
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_readWriteLockService =  spy(new ReadWriteLockServiceImpl());
        m_adapterManager = spy(new AdapterManagerImpl(m_modelNodeDataStoreManager, m_readWriteLockService, m_entityRegistry));
        m_inputStream = getClass().getResourceAsStream("/model/adapter1.xml");
        m_inputStream2 = getClass().getResourceAsStream("/model/adapter2.xml");
        List<String> cap1 = new ArrayList<>();
        cap1.add("capability1-adapter1");
        cap1.add("capability2-adapter1");
        m_deviceAdapter1 =  new DeviceAdapter("ADAPTER1", "1.0", "4LT", "VENDOR1", cap1, m_inputStream , m_moduleStream);
        List<String> cap2 = new ArrayList<>();
        cap2.add("capability1-adapter2");
        cap2.add("capability2-adapter2");
        m_deviceAdapter2 =  new DeviceAdapter("ADAPTER2", "2.0", "8LT", "VENDOR2", cap2, m_inputStream2 , m_moduleStream);

    }

    @Test
    public void testAdapterCount() {
        m_adapterManager.deploy(m_deviceAdapter1, null, null);
        m_adapterManager.deploy(m_deviceAdapter2, null, null);
        assertEquals(2, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testAddAdapters() {
        m_adapterManager.deploy(m_deviceAdapter1, null, null);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapter2, null, null);
        assertEquals(2, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testRemoveAdapter() {
        m_adapterManager.deploy(m_deviceAdapter1, null, null);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.undeploy(m_deviceAdapter1);
        assertEquals(0, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testUndeployedCalledRemoveContext(){
        m_adapterManager.deploy(m_deviceAdapter1, null, null);
        DeviceAdapterId deviceAdapterId = new DeviceAdapterId(m_deviceAdapter1.getType(),
            m_deviceAdapter1.getInterfaceVersion(), m_deviceAdapter1.getModel(), m_deviceAdapter1.getVendor());
        AdapterContext adapterContext = spy(m_adapterManager.getAdapterContext(deviceAdapterId));
        when(m_adapterManager.getAdapterContext(deviceAdapterId)).thenReturn(adapterContext);
        verify(adapterContext , never()).undeployed();
        m_adapterManager.undeploy(m_deviceAdapter1);
        verify(adapterContext).undeployed();
    }
}
