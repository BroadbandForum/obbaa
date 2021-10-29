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

package org.broadband_forum.obbaa.device.adapter;

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.BBF;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DPU;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.OLT;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STANDARD;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STANDARD_ADAPTER_OLDEST_VERSION;
import static org.broadband_forum.obbaa.device.adapter.AdapterUtils.getStandardAdapterContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.device.adapter.impl.AdapterManagerImpl;
import org.broadband_forum.obbaa.device.registrator.impl.StandardModelRegistrator;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.EventAdmin;

public class AdapterUtilsTest {

    private AdapterManager m_adapterManager;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDataStoreManager;
    @Mock
    private EventAdmin m_eventAdmin;
    @Mock
    private ReadWriteLockService m_readWriteLockService;
    private InputStream m_inputStream;
    private DeviceAdapter m_deviceAdapter;
    private Device m_device;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private SubSystem m_subSystem;
    @Mock
    private DeviceInterface m_deviceInterface;
    @Mock
    private StandardModelRegistrator m_standardModelRegistrator;
    @Mock
    private DeviceDao m_deviceDao;
    private TxService m_txService;

    public AdapterUtilsTest() {

    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_readWriteLockService = spy(new ReadWriteLockServiceImpl());
        Map<DeviceAdapter, FactoryGarmentTag> garmentTagMap = new HashMap<DeviceAdapter, FactoryGarmentTag>();
        ArrayList<String> deviatedStdModules = new ArrayList<>();
        deviatedStdModules.add(0, "ietf-interfaces");
        ArrayList<String> augmentedStdModules = new ArrayList<>();
        augmentedStdModules.add(0, "iana-interfaces");
        m_adapterManager = spy(new AdapterManagerImpl(m_modelNodeDataStoreManager, m_readWriteLockService, m_entityRegistry, m_eventAdmin, m_standardModelRegistrator, m_deviceDao, m_txService) {
            @Override
            protected void computeFactoryGarmentTag(DeviceAdapter adapter, AdapterContext vendorAdapterContext) {
                garmentTagMap.put(adapter, new FactoryGarmentTag(10, 3, 2, deviatedStdModules, augmentedStdModules));
            }
        });
        //install standard dpu adapters
        createAndDeployAdapter("/model/device-adapter11.xml");
        createAndDeployAdapter("/model/device-adapter4.xml");
        //install standard olt adapters
        createAndDeployAdapter("/model/device-adapter7.xml");
        createAndDeployAdapter("/model/device-adapter8.xml");
        //install other adapters
        createAndDeployAdapter("/model/device-adapter1.xml");
        createAndDeployAdapter("/model/device-adapter2.xml");
        createAndDeployAdapter("/model/device-adapter5.xml");
        createAndDeployAdapter("/model/device-adapter6.xml");
        createAndDeployAdapter("/model/device-adapter3.xml");
        createAndDeployAdapter("/model/device-adapter9.xml");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetStandardAdapterContextForDpuWhenStdAdapterIntVersionIsNotNull() {
        //when stdAdapterIntVersion != null i.e 2.0
        m_device = setDeviceAttributes("DPU", "4LT", "1.0", "sample");
        getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
        verifyCorrectStdAdapter(m_device, DPU, "2.0", STANDARD, BBF);
    }


    @Test
    public void testGetStdAdapterContextForDpuWhenStdAdapterIntVersionIsNull() {
        //when stdAdapterIntVersion == null i.e pick oldest version 1.0
        m_device = setDeviceAttributes("DPU", "8LT", "2.0", "VENDOR2");
        getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
        verifyCorrectStdAdapter(m_device, DPU, STANDARD_ADAPTER_OLDEST_VERSION, STANDARD, BBF);
    }

    @Test
    public void testGetStdAdapterContextForOltWhenStdAdapterIntVersionIsNotNull() {
        //when stdAdapterIntVersion != null i.e 2.0
        m_device = setDeviceAttributes("OLT", "4LT", "1.0", "VENDOR1");
        getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
        verifyCorrectStdAdapter(m_device, OLT, "2.0", STANDARD, BBF);
    }

    @Test
    public void testGetStdAdapterContextForOltWhenStdAdapterIntVersionIsNull() {
        //when stdAdapterIntVersion == null i.e pick oldest version 1.0
        m_device = setDeviceAttributes("OLT", "8LT", "2.0", "VENDOR2");
        getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
        verifyCorrectStdAdapter(m_device, OLT, STANDARD_ADAPTER_OLDEST_VERSION, STANDARD, BBF);
    }

    @Test
    public void testGetStdAdapterContextForStdOltWhenStdAdapterIntVersionIsNull() {
        //when adapter is standard, it picks up the standard adapter version
        m_device = setDeviceAttributes("OLT", "standard", "1.0", "BBF");
        getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
        verifyCorrectStdAdapter(m_device, OLT, "1.0", STANDARD, BBF);
    }

    @Test
    public void testGetStdAdapterContextForStdDpuWhenStdAdapterIntVersionIsNull() {
        //when adapter is standard, it picks up the standard adapter version
        m_device = setDeviceAttributes("DPU", "standard", "1.0", "BBF");
        getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
        verifyCorrectStdAdapter(m_device, DPU, "1.0", STANDARD, BBF);
    }

    @Test
    public void testGetStdAdapterContextForNonDpuOlt() {
        //Error case when device type != OLT || DPU
        m_device = setDeviceAttributes("TEST", "8LT", "2.0", "VENDOR2");
        try {
            getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertEquals("no standard adapter found for this type of device : TEST", e.getMessage());
        }
    }

    @Test
    @Ignore
    public void testGetStdAdapterContextWhenStdAdapterContextIsNull() {
        //Error case when no standard adapter installed for the device type
        m_device = setDeviceAttributes("DPU", "8LT", "2.0", "VENDOR2");
        try {
            m_adapterManager.undeploy(m_deviceAdapter);
            getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
            fail("Expected an Exception");
        } catch (Exception e) {
            assertEquals("no standard adapterContext deployed for : DPU", e.getMessage());
        }
    }

    @Test
    public void testGetStdAdapterContextWhenGivenAdapterUndeployed() {
        //Error case when no adapter installed for the device type
        m_device = setDeviceAttributes("OLT", "dummy", "1.0", "sample");
        try {
            createAndDeployAdapter("/model/device-adapter14.xml");
            m_adapterManager.undeploy(m_deviceAdapter);
            getStandardAdapterContext(m_adapterManager, AdapterUtils.getAdapter(m_device, m_adapterManager));
            fail("Expected an Exception");
        } catch (Exception e) {
            assertEquals("Given device adapter is not installed", e.getMessage());
        }
    }


    private Device setDeviceAttributes(String type, String model, String ifVersion, String vendor) {
        m_device = new Device();
        m_device.setDeviceName("UT-Device");
        DeviceMgmt devMgmt = new DeviceMgmt();
        devMgmt.setDeviceType(type);
        devMgmt.setDeviceModel(model);
        devMgmt.setDeviceInterfaceVersion(ifVersion);
        devMgmt.setDeviceVendor(vendor);
        m_device.setDeviceManagement(devMgmt);
        return m_device;
    }

    private void createAndDeployAdapter(String deviceAdapterXml) throws IOException {
        List<String> cap = new ArrayList<>();
        cap.add("sample-capability1");
        cap.add("sample-capability2");
        m_inputStream = getClass().getResourceAsStream(deviceAdapterXml);
        Map<URL, InputStream> m_moduleStream = new HashMap<>();
        m_deviceAdapter = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap)
                .setModuleStream(m_moduleStream)
                .setDeviceXml(m_inputStream)
                .setDefaultxmlBytes(IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml")))
                .build();
        m_deviceAdapter.init();
        m_adapterManager.deploy(m_deviceAdapter, m_subSystem, getClass(), m_deviceInterface);
    }

    private void verifyCorrectStdAdapter(Device givenDevice, String expectedType, String expectedInterfaceVersion, String expectedModel, String expectedVendor) {
        DeviceAdapter adapter = AdapterUtils.getAdapter(givenDevice, m_adapterManager);
        if (!adapter.getModel().equalsIgnoreCase(BBF) && adapter.getStdAdapterIntVersion() != null) {
            assertEquals(expectedType, adapter.getType());
            assertEquals(expectedInterfaceVersion, adapter.getStdAdapterIntVersion());
        } else {
            assertEquals(expectedType, adapter.getType());
            assertEquals(expectedInterfaceVersion, STANDARD_ADAPTER_OLDEST_VERSION);
        }
    }

}