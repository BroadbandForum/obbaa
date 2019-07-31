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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.EventAdmin;

public class AdapterManagerImplTest {

    private AdapterManager m_adapterManager;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDataStoreManager;
    private ReadWriteLockService m_readWriteLockService;
    private InputStream m_inputStream;
    Map<URL, InputStream> m_moduleStream = new HashMap<>();
    private InputStream m_inputStream2;
    private InputStream m_inputStream3;
    private byte[] m_defaultConfig1;
    private byte[] m_defaultConfig2;
    private byte[] m_defaultConfig3;
    private DeviceAdapter m_deviceAdapter1;
    private DeviceAdapter m_deviceAdapter2;
    private DeviceAdapter m_deviceAdapter3;
    private DeviceAdapter m_stdAdapter;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private EventAdmin m_eventAdmin;
    @Mock
    private SubSystem m_subSystem;
    @Mock
    private DeviceInterface m_deviceInterface;
    private InputStream m_inputStreamStdAdapter;

    @Mock
    private StandardModelRegistrator m_standardModelRegistrator;

    public AdapterManagerImplTest() {
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        System.setProperty("MAXIMUM_ALLOWED_ADAPTER_VERSIONS", "1");
        m_readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_adapterManager = spy(new AdapterManagerImpl(m_modelNodeDataStoreManager, m_readWriteLockService, m_entityRegistry, m_eventAdmin, m_standardModelRegistrator));
        m_inputStream = getClass().getResourceAsStream("/model/device-adapter1.xml");
        m_inputStream2 = getClass().getResourceAsStream("/model/device-adapter2.xml");
        m_inputStream3 = getClass().getResourceAsStream("/model/device-adapter10.xml");
        m_inputStreamStdAdapter = getClass().getResourceAsStream("/model/device-adapter4.xml");
        m_defaultConfig1 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config1.xml"));
        m_defaultConfig2 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml"));
        m_defaultConfig3 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml"));
        List<String> cap1 = new ArrayList<>();
        cap1.add("capability1-adapter1");
        cap1.add("capability2-adapter1");
        m_stdAdapter =  AdapterBuilder.createAdapterBuilder()
                .setCaps(cap1)
                .setModuleStream(getStreamMapForStd())
                .setDeviceXml(m_inputStreamStdAdapter)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
        m_stdAdapter.init();
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

    }

    @Test
    public void testAdapterCount() {
        assertNull(m_stdAdapter.getLastUpdateTime());
        assertNull(m_deviceAdapter1.getLastUpdateTime());
        assertNull(m_deviceAdapter2.getLastUpdateTime());
        m_adapterManager.deploy(m_stdAdapter, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_deviceAdapter1, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(3, m_adapterManager.getAdapterSize());
        assertNotNull(m_stdAdapter.getLastUpdateTime());
        assertNotNull(m_deviceAdapter1.getLastUpdateTime());
        assertNotNull(m_deviceAdapter2.getLastUpdateTime());
    }

    @Test
    public void testAddAdapters() {
        assertEquals(0, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_stdAdapter, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapter1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(2, m_adapterManager.getAdapterSize());
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(3, m_adapterManager.getAdapterSize());
    }

    @Test
    public void testDeployWhenMaxAllowedExceeds() throws Exception {
        when(m_adapterManager.getAllDeviceAdapters()).thenReturn(Collections.singletonList(m_deviceAdapter1));
        try {
            m_adapterManager.deploy(m_deviceAdapter3, m_subSystem, getClass(), m_deviceInterface);
            fail("Expected an Exception");
        } catch (Exception e) {
            String expectedMessage = "Adapter deployment failed!! Reason : maximum allowed versions(1) reached for the specified adapter(DeviceAdapterId{m_type='DPU, m_interfaceVersion='2.0, m_model='4LT, m_vendor='VENDOR1}), Uninstall any older version to proceed";
            assertEquals(expectedMessage, e.getCause().getMessage());
        }

    }

    @Test
    public void testRemoveAdapter() {
        m_adapterManager.deploy(m_stdAdapter, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(1, m_adapterManager.getAdapterSize());
        m_adapterManager.undeploy(m_stdAdapter);
        assertEquals(0, m_adapterManager.getAdapterSize());
        assertNull(SystemProperty.getInstance().get(m_stdAdapter.genAdapterLastUpdateTimeKey()));
    }

    @Test
    public void testUndeployedCalledRemoveContext() {
        m_adapterManager.deploy(m_stdAdapter, m_subSystem, getClass(), m_deviceInterface);
        DeviceAdapterId deviceAdapterId = new DeviceAdapterId(m_stdAdapter.getType(),
                m_stdAdapter.getInterfaceVersion(), m_stdAdapter.getModel(), m_stdAdapter.getVendor());
        AdapterContext adapterContext = spy(m_adapterManager.getAdapterContext(deviceAdapterId));
        when(m_adapterManager.getAdapterContext(deviceAdapterId)).thenReturn(adapterContext);
        verify(adapterContext, never()).undeployed();
        m_adapterManager.undeploy(m_stdAdapter);
        verify(adapterContext).undeployed();
    }

    @Test
    public void testGetEditReqForDefaultConfig() throws IOException, NetconfMessageBuilderException {
        m_adapterManager.deploy(m_stdAdapter, m_subSystem, getClass(), m_deviceInterface);
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(IOUtils.toString(getInputStream("expectedEditReq.xml")), DocumentUtils.documentToPrettyString
                (m_adapterManager.getEditRequestForAdapter(m_stdAdapter.getDeviceAdapterId()).getConfigElement().getXmlElement()));
        assertNull(m_adapterManager.getEditRequestForAdapter(m_deviceAdapter2.getDeviceAdapterId()));
        //after undeploy verify that the values dont exist in the map
        m_adapterManager.undeploy(m_stdAdapter);
        assertNull(m_adapterManager.getEditRequestForAdapter(m_stdAdapter.getDeviceAdapterId()));
        m_adapterManager.undeploy(m_deviceAdapter2);
        assertNull(m_adapterManager.getEditRequestForAdapter(m_deviceAdapter2.getDeviceAdapterId()));
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
            m_adapterManager.deploy(m_stdAdapter, m_subSystem, getClass(), m_deviceInterface);
            m_adapterManager.deploy(adapter, m_subSystem, getClass(), m_deviceInterface);
        } catch (Exception e) {
            assertEquals("Error while deploying adapter", e.getMessage());
            String message = e.getCause().getMessage();
            assertTrue(message.contains("ValidationException"));
            assertTrue(message.contains("errorMessage=An unexpected element leaf-not-present is present"));
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
