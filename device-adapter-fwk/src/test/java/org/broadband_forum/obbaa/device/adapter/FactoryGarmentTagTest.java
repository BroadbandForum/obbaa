package org.broadband_forum.obbaa.device.adapter;

import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.device.adapter.impl.AdapterManagerImpl;
import org.broadband_forum.obbaa.device.registrator.impl.StandardModelRegistrator;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class FactoryGarmentTagTest {
    Map<URL, InputStream> m_moduleStream = new HashMap<>();
    private AdapterManager m_adapterManager;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDataStoreManager;
    private ReadWriteLockService m_readWriteLockService;
    private InputStream m_inputStream;
    private InputStream m_inputStream2;
    private InputStream m_inputStream3;
    private byte[] m_defaultConfig1;
    private byte[] m_defaultConfig2;
    private byte[] m_defaultConfig3;
    private DeviceAdapter m_deviceAdapter1;
    private DeviceAdapter m_deviceAdapter2;
    private DeviceAdapter m_deviceAdapter3;
    private DeviceAdapter m_stdAdapterv2;
    private DeviceAdapter m_stdAdapterv1;
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

    @Mock
    private StandardModelRegistrator m_standardModelRegistrator;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_adapterManager = spy(new AdapterManagerImpl(m_modelNodeDataStoreManager, m_readWriteLockService, m_entityRegistry, m_eventAdmin, m_standardModelRegistrator));
        m_inputStream = getClass().getResourceAsStream("/model/device-adapter1.xml");
        m_inputStream2 = getClass().getResourceAsStream("/model/device-adapter2.xml");
        m_inputStream3 = getClass().getResourceAsStream("/model/device-adapter10.xml");
        m_inputStreamStdAdapterv2 = getClass().getResourceAsStream("/model/device-adapter4.xml");
        m_inputStreamStdAdapterv1 = getClass().getResourceAsStream("/model/device-adapter11.xml");
        m_defaultConfig1 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config1.xml"));
        m_defaultConfig2 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml"));
        m_defaultConfig3 = IOUtils.toByteArray(getClass().getResourceAsStream("/model/default-config2.xml"));
        List<String> caps = new ArrayList<>();
        caps.add("capability1-adapter1");
        caps.add("capability2-adapter1");
        m_stdAdapterv2 = prepareAdapter(caps, m_inputStreamStdAdapterv2);
        m_stdAdapterv2.init();
        m_stdAdapterv1 = prepareAdapter(caps, m_inputStreamStdAdapterv1);
        m_stdAdapterv1.init();
        m_deviceAdapter1 = prepareAdapter(caps, getStreamMap(), m_inputStream, m_defaultConfig1, "model/supported-deviations.txt");
        m_deviceAdapter1.init();
        List<String> cap2 = new ArrayList<>();
        cap2.add("capability1-adapter2");
        cap2.add("capability2-adapter2");
        m_deviceAdapter2 = prepareAdapter(cap2, getStreamMapForAdapter2(), m_inputStream2, m_defaultConfig2, "model/supported-deviations2.txt");
        m_deviceAdapter2.init();
        m_deviceAdapter3 = AdapterBuilder.createAdapterBuilder()
                .setCaps(cap2)
                .setModuleStream(m_moduleStream)
                .setDeviceXml(m_inputStream3)
                .setDefaultxmlBytes(m_defaultConfig3)
                .build();
        m_deviceAdapter3.init();

    }

    private DeviceAdapter prepareAdapter(List<String> caps, Map<URL, InputStream> streamMap, InputStream m_inputStream, byte[] m_defaultConfig1, String s) {
        return AdapterBuilder.createAdapterBuilder()
                .setCaps(caps)
                .setModuleStream(streamMap)
                .setDeviceXml(m_inputStream)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedDeviations(CommonFileUtil.getAdapterDeviationsFromFile(getInputStream(s)))
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
    }

    private DeviceAdapter prepareAdapter(List<String> cap1, InputStream m_inputStreamStdAdapterv2) throws IOException {
        return AdapterBuilder.createAdapterBuilder()
                .setCaps(cap1)
                .setModuleStream(getStreamMapForStd())
                .setDeviceXml(m_inputStreamStdAdapterv2)
                .setDefaultxmlBytes(m_defaultConfig1)
                .setSupportedFeatures(CommonFileUtil.getAdapterFeaturesFromFile(getInputStream("model/supported-features.txt")))
                .build();
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

    private Map<URL, InputStream> getStreamMapForAdapter2() throws IOException {
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

    @Test
    public void testFactoryGarmentTag() {
        m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
        assertNull(m_adapterManager.getFactoryGarmentTag(m_stdAdapterv2));
        m_adapterManager.deploy(m_deviceAdapter1, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(4, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getTotalNumberOfStandardModules());
        assertEquals(6, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getNumberOfModulesOfVendorAdapter());
        assertEquals("[ietf-interfaces]", String.valueOf(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getAugmentedStdModules()));
        assertEquals("[ietf-interfaces]", String.valueOf(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getDeviatedStdModules()));
        assertEquals(100, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getAdherencePercentage());
        assertEquals("25%", m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getPercentageDeviatedStdModules());
        assertEquals("25%", m_adapterManager.getFactoryGarmentTag(m_deviceAdapter1).getPercentageAugmentedStdModules());
    }

    @Test
    public void testFactoryGarmentTagWithoutDeviationAndAugments() {
        m_adapterManager.deploy(m_stdAdapterv1, m_subSystem, getClass(), m_deviceInterface);
        assertNull(m_adapterManager.getFactoryGarmentTag(m_stdAdapterv1));
        m_adapterManager.deploy(m_deviceAdapter2, m_subSystem, getClass(), m_deviceInterface);
        assertEquals(4, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getTotalNumberOfStandardModules());
        assertEquals(4, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getNumberOfModulesOfVendorAdapter());
        assertEquals("[]", String.valueOf(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getAugmentedStdModules()));
        assertEquals("[]", String.valueOf(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getDeviatedStdModules()));
        assertEquals(100, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getAdherencePercentage());
        assertEquals("0%", m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getPercentageDeviatedStdModules());
        assertEquals("0%", m_adapterManager.getFactoryGarmentTag(m_deviceAdapter2).getPercentageAugmentedStdModules());
    }

    @Test
    public void testFactoryGarmentTagWithoutResuedModules() {
        ArrayList<String> dummylist = new ArrayList<String>();
        m_adapterManager.deploy(m_stdAdapterv2, m_subSystem, getClass(), m_deviceInterface);
        assertNull(m_adapterManager.getFactoryGarmentTag(m_stdAdapterv2));
        m_adapterManager.deploy(m_deviceAdapter3, m_subSystem, getClass(), m_deviceInterface);
        when(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3)).thenReturn(new FactoryGarmentTag(4, 6, 0, dummylist, dummylist));
        assertEquals(4, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getTotalNumberOfStandardModules());
        assertEquals(6, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getNumberOfModulesOfVendorAdapter());
        assertEquals("[]", String.valueOf(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getAugmentedStdModules()));
        assertEquals("[]", String.valueOf(m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getDeviatedStdModules()));
        assertEquals(0, m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getAdherencePercentage());
        assertEquals("Not Applicable", m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getPercentageDeviatedStdModules());
        assertEquals("Not Applicable", m_adapterManager.getFactoryGarmentTag(m_deviceAdapter3).getPercentageAugmentedStdModules());
    }
}
