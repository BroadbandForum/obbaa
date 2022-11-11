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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.device.adapter.AdapterBuilder;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.util.SystemProperty;
import org.broadband_forum.obbaa.device.registrator.impl.StandardModelRegistrator;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.EventAdmin;

public class AdapterManagerImplTest {

    Map<URL, InputStream> m_moduleStream = new HashMap<>();
    private AdapterManager m_adapterManager;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDataStoreManager;
    private ReadWriteLockService m_readWriteLockService;
    private InputStream m_inputStream;
    private InputStream m_inputStream2;
    private InputStream m_inputStream3;
    private InputStream m_inputStreamThirdType;
    private byte[] m_defaultConfig1;
    private byte[] m_defaultConfig2;
    private byte[] m_defaultConfig3;
    private DeviceAdapter m_deviceAdapter1;
    private DeviceAdapter m_deviceAdapter2;
    private DeviceAdapter m_deviceAdapter3;
    private DeviceAdapter m_stdAdapterv2;
    private DeviceAdapter m_stdAdapterv1;
    private DeviceAdapter m_codedStdAdapterv1;
    private DeviceAdapter m_deviceAdapterThirdType;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private EventAdmin m_eventAdmin;
    @Mock
    private SubSystem m_subSystem;
    @Mock
    private DeviceInterface m_deviceInterface;
    private InputStream m_inputStreamStdAdapterv2;
    private InputStream m_inputStreamStdAdapterv1;
    private InputStream m_inputStreamCodedStdAdapterv1;
    @Mock
    private DeviceDao m_deviceDao;
    @Mock
    private DeviceAdapterId m_deviceAdapterId;
    private TxService m_txService;
    @Mock
    private StandardModelRegistrator m_standardModelRegistrator;

    private final String TYPE_OLT = "OLT";
    private final String TYPE_DPU = "DPU";
    private final String VENDOR_BBF = "BBF";
    private final String MODEL = "model";
    private final String VERSION_1 = "1.0";
    private final String VERSION_2 = "2.0";


    public AdapterManagerImplTest() {
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        System.setProperty("MAXIMUM_ALLOWED_ADAPTER_VERSIONS", "1");
        m_readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_txService = new TxService();
        m_adapterManager = spy(new AdapterManagerImpl(m_modelNodeDataStoreManager, m_readWriteLockService, m_entityRegistry, m_eventAdmin, m_standardModelRegistrator, m_deviceDao, m_txService));
        m_inputStream = getClass().getResourceAsStream("/model/device-adapter1.xml");
        m_inputStream2 = getClass().getResourceAsStream("/model/device-adapter2.xml");
        m_inputStream3 = getClass().getResourceAsStream("/model/device-adapter10.xml");
        m_inputStreamThirdType = getClass().getResourceAsStream("/model/device-adapter13.xml");
        m_inputStreamStdAdapterv2 = getClass().getResourceAsStream("/model/device-adapter4.xml");
        m_inputStreamStdAdapterv1 = getClass().getResourceAsStream("/model/device-adapter11.xml");
        m_inputStreamCodedStdAdapterv1 = getClass().getResourceAsStream("/model/device-adapter12.xml");
        m_defaultConfig1 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config1.xml"));
        m_defaultConfig2 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml"));
        m_defaultConfig3 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml"));
        List<String> cap1 = new ArrayList<>();
        cap1.add("capability1-adapter1");
        cap1.add("capability2-adapter1");
        m_stdAdapterv2 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap1)
                .setModuleStream(getStreamMapForStd())
                .setDeviceXml(m_inputStreamStdAdapterv2)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        m_stdAdapterv2.init();
        m_stdAdapterv1 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap1)
                .setModuleStream(getStreamMapForStd())
                .setDeviceXml(m_inputStreamStdAdapterv1)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        m_stdAdapterv1.init();
        m_codedStdAdapterv1 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap1)
                .setModuleStream(getStreamMapForStd())
                .setDeviceXml(m_inputStreamCodedStdAdapterv1)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        m_codedStdAdapterv1.init();
        m_deviceAdapter1 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap1)
                .setModuleStream(getStreamMap())
                .setDeviceXml(m_inputStream)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedDeviations(CommonFileUtil.getAdapterDeviationsFromFile(getInputStream("model/supported-deviations.txt")))
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        m_deviceAdapter1.init();
        List<String> cap2 = new ArrayList<>();
        cap2.add("capability1-adapter2");
        cap2.add("capability2-adapter2");
        m_deviceAdapter2 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap2)
                .setModuleStream(m_moduleStream)
                .setDeviceXml(m_inputStream2)
                .setDefaultxmlBytes(m_defaultConfig2)
                .build();
        m_deviceAdapter2.init();
        m_deviceAdapter3 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap2)
                .setModuleStream(m_moduleStream)
                .setDeviceXml(m_inputStream3)
                .setDefaultxmlBytes(m_defaultConfig3)
                .build();
        m_deviceAdapter3.init();
        m_deviceAdapterThirdType = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap2)
                .setModuleStream(m_moduleStream)
                .setDeviceXml(m_inputStreamThirdType)
                .setDefaultxmlBytes(m_defaultConfig1)
                .build();
        m_deviceAdapterThirdType.init();

    }

    @Test
    public void testAdapterCount() {
        assertNull(m_stdAdapterv2.getLastUpdateTime());
        assertNull(m_deviceAdapter1.getLastUpdateTime());
        assertNull(m_deviceAdapter2.getLastUpdateTime());
        m_adapterManager.deploy(m_stdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_deviceAdapter1, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(4, m_adapterManager.getAdapterSize());
        assertNotNull(m_stdAdapterv2.getLastUpdateTime());
        assertNotNull(m_deviceAdapter1.getLastUpdateTime());
        assertNotNull(m_deviceAdapter2.getLastUpdateTime());
        m_adapterManager.undeploy(m_deviceAdapter2);
        assertEquals(3, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testAddAdapters() {
        assertEquals(0, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_stdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(2, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapter1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(3, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(4, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testAddCodedStdAdapters() {
        assertEquals(0, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_codedStdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapterThirdType, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(2, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testRemoveThirdTypeStdAdapter() throws Exception {
        assertEquals(0, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_codedStdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.undeploy(m_codedStdAdapterv1);
        assertEquals(0, m_adapterManager.getAdapterSize());
        try {
            m_adapterManager.deploy(m_deviceAdapterThirdType, m_subSystem, getClass(), m_deviceInterface);
            fail("Expected an Exception");
        } catch (Exception e) {
            String expectedMessage = "no standard adapter found for this type of device : THIRDTYPE";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void testRemoveStdAdapter() throws Exception {
        assertEquals(0, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_stdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.undeploy(m_stdAdapterv1);
        assertEquals(0, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testRemoveThirdTypeStdAdapterInUse() throws Exception {
        assertEquals(0, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_codedStdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapterThirdType, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(2, m_adapterManager.getAdapterSize());
        try {
            m_adapterManager.undeploy(m_codedStdAdapterv1);
            fail("Expected an Exception");
        } catch (Exception e) {
            String expectedMessage = "Given standard adapter is referenced by other coded adapter(s), undeploy operation is not allowed";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }
    }

    @Test
    public void testDeployWhenMaxAllowedExceeds() throws Exception {
        when(m_adapterManager.getAllDeviceAdapters()).thenReturn(Collections.singletonList(m_deviceAdapter1));
        try {
            m_adapterManager.deploy(m_deviceAdapter3, m_subSystem, getClass(), m_deviceInterface);
            fail("Expected an Exception");
        } catch (Exception e) {
            String expectedMessage = "Adapter deployment failed!! Reason : maximum allowed versions(1) reached for the specified adapter(DeviceAdapterId{m_type='DPU, m_interfaceVersion='2.0, m_model='4LT, m_vendor='sample}), Uninstall any older version to proceed";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }

    }

    @Test
    public void testRemoveAdapter() {
        m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.undeploy(m_stdAdapterv2);
        assertEquals(0, m_adapterManager.getAdapterSize());
        assertNull(SystemProperty.getInstance().get(m_stdAdapterv2.genAdapterLastUpdateTimeKey()));
    }

    @Test
    public void testUndeployedCalledRemoveContext() {
        m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
        DeviceAdapterId deviceAdapterId = new DeviceAdapterId(m_stdAdapterv2.getType(),
                m_stdAdapterv2.getInterfaceVersion(), m_stdAdapterv2.getModel(), m_stdAdapterv2.getVendor());
        AdapterContext adapterContext = spy(m_adapterManager.getAdapterContext(deviceAdapterId));
        when(m_adapterManager.getAdapterContext(deviceAdapterId)).thenReturn(adapterContext);
        verify(adapterContext, never()).undeployed();
        m_adapterManager.undeploy(m_stdAdapterv2);
        verify(adapterContext).undeployed();
    }

    @Test
    public void testGetEditReqForDefaultConfig() throws IOException, NetconfMessageBuilderException {
        m_adapterManager.deploy(m_stdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(IOUtils.toString(getInputStream("expectedEditReq.xml")), DocumentUtils.documentToPrettyString
                (m_adapterManager.getEditRequestForAdapter(m_stdAdapterv1.getDeviceAdapterId()).getConfigElement().getXmlElement()));
        assertNull(m_adapterManager.getEditRequestForAdapter(m_deviceAdapter2.getDeviceAdapterId()));
        //after undeploy verify that the values dont exist in the map
        m_adapterManager.undeploy(m_deviceAdapter2);
        assertNull(m_adapterManager.getEditRequestForAdapter(m_deviceAdapter2.getDeviceAdapterId()));
        m_adapterManager.undeploy(m_stdAdapterv1);
        assertNull(m_adapterManager.getEditRequestForAdapter(m_stdAdapterv2.getDeviceAdapterId()));
    }

    @Test
    public void testIsAdapterInUseTrue() {
        when(m_deviceDao.findAllDevices()).thenReturn(prepareDeviceList());
        when(m_deviceAdapterId.getInterfaceVersion()).thenReturn(VERSION_1);
        when(m_deviceAdapterId.getType()).thenReturn(TYPE_OLT);
        when(m_deviceAdapterId.getModel()).thenReturn(MODEL);
        when(m_deviceAdapterId.getVendor()).thenReturn(VENDOR_BBF);
        Boolean isAdapterInUse = m_adapterManager.isAdapterInUse(m_deviceAdapterId);
        verify(m_deviceAdapterId, times(1)).getInterfaceVersion();
        verify(m_deviceAdapterId, times(1)).getModel();
        verify(m_deviceAdapterId, times(1)).getVendor();
        verify(m_deviceAdapterId, times(1)).getType();
        assertTrue(isAdapterInUse);
    }

    @Test
    public void testIsAdapterInUseWhenNoDevicesAdded() {
        List<Device> devices = Collections.<Device>emptyList();
        when(m_deviceDao.findAllDevices()).thenReturn(devices);
        when(m_deviceAdapterId.getInterfaceVersion()).thenReturn(VERSION_1);
        when(m_deviceAdapterId.getType()).thenReturn(TYPE_OLT);
        when(m_deviceAdapterId.getModel()).thenReturn(MODEL);
        when(m_deviceAdapterId.getVendor()).thenReturn(VENDOR_BBF);
        Boolean isAdapterInUse = m_adapterManager.isAdapterInUse(m_deviceAdapterId);
        verify(m_deviceAdapterId, never()).getInterfaceVersion();
        verify(m_deviceAdapterId, never()).getModel();
        verify(m_deviceAdapterId, never()).getVendor();
        verify(m_deviceAdapterId, never()).getType();
        assertFalse(isAdapterInUse);
    }

    @Test
    public void testIsAdapterInUseFalse() {
        when(m_deviceDao.findAllDevices()).thenReturn(prepareDeviceList());
        when(m_deviceAdapterId.getInterfaceVersion()).thenReturn(VERSION_2);
        when(m_deviceAdapterId.getType()).thenReturn(TYPE_DPU);
        when(m_deviceAdapterId.getModel()).thenReturn(MODEL);
        when(m_deviceAdapterId.getVendor()).thenReturn(VENDOR_BBF);
        Boolean isAdapterInUse = m_adapterManager.isAdapterInUse(m_deviceAdapterId);
        verify(m_deviceAdapterId, times(1)).getModel();
        verify(m_deviceAdapterId, times(1)).getVendor();
        verify(m_deviceAdapterId, times(1)).getType();
        assertFalse(isAdapterInUse);
    }

    private List<Device> prepareDeviceList() {
        List<Device> devices = new ArrayList<>();
        Device device = new Device();
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        device.setDeviceManagement(deviceMgmt);
        device.getDeviceManagement().setDeviceModel(MODEL);
        device.getDeviceManagement().setDeviceType(TYPE_OLT);
        device.getDeviceManagement().setDeviceInterfaceVersion(VERSION_1);
        device.getDeviceManagement().setDeviceVendor(VENDOR_BBF);
        devices.add(device);
        return devices;
    }

    @Test
    public void testScenarioWhenDeafultConfigIncorrect() throws IOException {
        //Verify error in validating a leaf which is not present in the schema registry
        List<String> cap = new ArrayList<>();
        cap.add("capability1-adapter2");
        cap.add("capability2-adapter2");
        byte[] m_defaultConfigIncorrect = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config-incorrect.xml"));
        DeviceAdapter adapter = AdapterBuilder.createAdapterBuilder()
                .setDeviceAdapterId(new DeviceAdapterId("DPU", "1.0", "4LT", "VENDOR"))
                .setModuleStream(getStreamMap())
                .setCaps(cap)
                .setDeviceXml(m_inputStream)
                .setDefaultxmlBytes(m_defaultConfigIncorrect)
                .setSupportedDeviations(CommonFileUtil.getAdapterDeviationsFromFile(getInputStream("model/supported-deviations.txt")))
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        adapter.setStdAdapterIntVersion("2.0");
        try {
            m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
            m_adapterManager.deploy(adapter, m_subSystem, getClass(), m_deviceInterface);
        } catch (Exception e) {
            assertEquals("Error while deploying adapter", e.getMessage());
            String message = e.getCause().getMessage();
            assertTrue(message.contains("ValidationException"));
            assertTrue(message.contains("errorMessage=An unexpected element 'leaf-not-present' is present"));
            assertTrue(message.contains("errorTag=unknown-element"));
            assertTrue(message.contains("errorSeverity=error"));
            assertTrue(message.contains("errorType=application"));
        }

        m_defaultConfigIncorrect = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config-incorrect2.xml"));
        adapter = AdapterBuilder.createAdapterBuilder()
                .setDeviceAdapterId(new DeviceAdapterId("DPU", "1.0", "4LT", "VENDOR"))
                .setModuleStream(getStreamMap())
                .setCaps(cap)
                .setDeviceXml(m_inputStream)
                .setDefaultxmlBytes(m_defaultConfigIncorrect)
                .setSupportedDeviations(CommonFileUtil.getAdapterDeviationsFromFile(getInputStream("model/supported-deviations.txt")))
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        adapter.setStdAdapterIntVersion("2.0");
        try {
            m_adapterManager.deploy(adapter, m_subSystem, getClass(), m_deviceInterface);
        } catch (Exception e) {
            assertEquals("Error while deploying adapter", e.getMessage());
            String message = e.getCause().getMessage();
            assertTrue(message.contains("Failed to parse default-config.xml from the adapter"));
            assertTrue(message.contains("The element type \"if:leaf-not-present\" must be terminated by the matching end-tag"));
        }
    }

    private Map<URL, InputStream> getStreamMap() throws IOException {
        Map<URL, InputStream> moduleStream = new HashMap<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        List<String> files = new ArrayList<>();
        files.add("noncodedadapter/yang/iana-if-type.yang");
        files.add("noncodedadapter/yang/ietf-inet-types.yang");
        files.add("noncodedadapter/yang/ietf-interfaces.yang");
        files.add("noncodedadapter/yang/ietf-yang-types.yang");
        files.add("noncodedadapter/yang/sample-ietf-interfaces-dev.yang");
        files.add("noncodedadapter/yang/sample-ietf-interfaces-aug.yang");
        for (String file : files) {
            URL fileUrl = cl.getResource(file);
            if (System.getProperty("os.name").startsWith("Windows")) {
                fileUrl = revisePathForWindow(fileUrl);
            }
            moduleStream.put(fileUrl, fileUrl != null ? fileUrl.openStream() : null);
        }
        return moduleStream;
    }

    private Map<URL, InputStream> getStreamMapForStd() throws IOException {
        Map<URL, InputStream> moduleStream = new HashMap<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        List<String> files = new ArrayList<>();
        files.add("noncodedadapter/yang/iana-if-type.yang");
        files.add("noncodedadapter/yang/ietf-inet-types.yang");
        files.add("noncodedadapter/yang/ietf-interfaces.yang");
        files.add("noncodedadapter/yang/ietf-yang-types.yang");
        for (String file : files) {
            URL fileUrl = cl.getResource(file);
            if (System.getProperty("os.name").startsWith("Windows")) {
                fileUrl = revisePathForWindow(fileUrl);
            }
            moduleStream.put(fileUrl, fileUrl != null ? fileUrl.openStream() : null);
        }
        return moduleStream;
    }

    private URL revisePathForWindow(URL orginalUrl) throws IOException {
        return new URL("file:" + orginalUrl.getPath().substring(1));
    }

    private InputStream getInputStream(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (System.getProperty("os.name").startsWith("Windows")) {
            return cl.getResourceAsStream(path.substring(1));
        }
        return cl.getResourceAsStream(path);
    }
}
