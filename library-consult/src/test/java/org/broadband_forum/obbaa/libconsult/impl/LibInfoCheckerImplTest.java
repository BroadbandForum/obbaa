package org.broadband_forum.obbaa.libconsult.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.broadband_forum.obbaa.device.adapter.AdapterBuilder;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.impl.AdapterManagerImpl;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dm.impl.DeviceManagerImpl;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.impl.PmaRegistryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LibInfoCheckerImplTest {

    private static DeviceManager m_deviceManager;
    private static AdapterManager m_adapterManager;
    private static PmaRegistry m_pmaRegistry;
    private static LibInfoCheckerImpl libInfoChecker;
    private static SchemaRegistryImpl m_schemaRegistry;

    private static final String DEVICE_A = "DeviceA";
    private static final String DEVICE_B = "DeviceB";
    private static final String DEVICE_C = "DeviceC";
    private static final String BBF_FAST_NS = "urn:bbf:yang:bbf-fast";
    private static final String BBF_FIBER_NS = "urn:bbf:yang:bbf-fiber";

    @BeforeClass
    public static void setUp() throws Exception {

        m_deviceManager = mock(DeviceManagerImpl.class);
        m_adapterManager = mock(AdapterManagerImpl.class);
        m_pmaRegistry = mock(PmaRegistryImpl.class);
        AdapterContext m_adapterContext = mock(AdapterContext.class);
        m_schemaRegistry = mock(SchemaRegistryImpl.class);
        when(m_adapterManager.getAdapterContext(any())).thenReturn(m_adapterContext);
        when(m_adapterContext.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        libInfoChecker = new LibInfoCheckerImpl(m_adapterManager, m_deviceManager, m_pmaRegistry);

        prepareTestModule();
        prepareGetRunningCfgResponse();
        prepareAdapters();
        prepareDevicesForAdapterInUseTest();
    }

    private static void prepareGetRunningCfgResponse() throws Exception{
        Document allRunningOltCfgDoc = DocumentUtils.loadXmlDocument(
                LibInfoCheckerImplTest.class.getResourceAsStream("/get-device-OLT-all-running-config-response.xml"));
        NetConfResponse ntcfOltResponse = new NetConfResponse().setMessageId("test123");
        ntcfOltResponse.addDataContent(allRunningOltCfgDoc.getDocumentElement());

        Document allRunningDpuCfgDoc = DocumentUtils.loadXmlDocument(
                LibInfoCheckerImplTest.class.getResourceAsStream("/get-device-DPU-all-running-config-response.xml"));
        NetConfResponse ntcfDpuResponse = new NetConfResponse().setMessageId("test123");
        ntcfDpuResponse.addDataContent(allRunningDpuCfgDoc.getDocumentElement());
        when(m_pmaRegistry.getAllPersistCfg(DEVICE_A)).thenReturn(ntcfOltResponse);
        when(m_pmaRegistry.getAllPersistCfg(DEVICE_B)).thenReturn(ntcfDpuResponse);
        when(m_pmaRegistry.getAllPersistCfg(DEVICE_C)).thenReturn(ntcfDpuResponse);
    }

    private static void prepareTestModule() {
        Module moduleFiber = mock(Module.class);
        when(moduleFiber.getName()).thenReturn("bbf-fiber");
        when(moduleFiber.getRevision()).thenReturn(Optional.of(Revision.of("2016-09-08")));

        Module moduleFast = mock(Module.class);
        when(moduleFast.getName()).thenReturn("bbf-fast");
        when(moduleFast.getRevision()).thenReturn(Optional.of(Revision.of("2017-03-13")));

        when(m_schemaRegistry.getModuleByNamespace(BBF_FIBER_NS)).thenReturn(moduleFiber);
        when(m_schemaRegistry.getModuleByNamespace(BBF_FAST_NS)).thenReturn(moduleFast);
    }

    private static void prepareAdapters() {
        DeviceAdapterId oltDevAdapterId = new DeviceAdapterId("OLT", "1.0",
                "MA5800-X17", "Huawei");
        AdapterBuilder oltAdapterBuilder = new AdapterBuilder();
        oltAdapterBuilder.setDeviceAdapterId(oltDevAdapterId);

        DeviceAdapterId dpuDevAdapterId = new DeviceAdapterId("DPU", "1.0",
                "MA5811s-AE08", "Huawei");
        AdapterBuilder dpuAdapterBuilder = new AdapterBuilder();
        dpuAdapterBuilder.setDeviceAdapterId(dpuDevAdapterId);

        List<DeviceAdapter> adapters = new ArrayList<>(2);
        adapters.add(dpuAdapterBuilder.build());
        adapters.add(oltAdapterBuilder.build());
        when(m_adapterManager.getAllDeviceAdapters()).thenReturn(adapters);
    }

    private static Device createDirectDevicesForAdapterInUseTest(String devName, String intfVersion, String model, String type,
            String vendor) {
        Device directDevice = new Device();
        directDevice.setDeviceName(devName);
        DeviceMgmt devicemgmt = new DeviceMgmt();
        devicemgmt.setDeviceInterfaceVersion(intfVersion);
        devicemgmt.setDeviceModel(model);
        devicemgmt.setDeviceType(type);
        devicemgmt.setDeviceVendor(vendor);
        directDevice.setDeviceManagement(devicemgmt);
        return directDevice;
    }

    private static void prepareDevicesForAdapterInUseTest() {
        List<Device> deviceList = new ArrayList<>();
        Device deviceA = createDirectDevicesForAdapterInUseTest(DEVICE_A, "1.0", "MA5800-X17", "OLT", "Huawei");
        Device deviceB = createDirectDevicesForAdapterInUseTest(DEVICE_B, "1.0", "MA5811s-AE08", "DPU", "Huawei");
        Device deviceC = createDirectDevicesForAdapterInUseTest(DEVICE_C, "1.0", "MA5811s-AE08", "DPU", "Huawei");
        deviceList.add(deviceA);
        deviceList.add(deviceB);
        deviceList.add(deviceC);
        when(m_deviceManager.getAllDevices()).thenReturn(deviceList);
        when(m_deviceManager.getDevice(DEVICE_A)).thenReturn(deviceA);
        when(m_deviceManager.getDevice(DEVICE_B)).thenReturn(deviceB);
        when(m_deviceManager.getDevice(DEVICE_C)).thenReturn(deviceC);
    }

    @Test
    public void getSpecificUsedYangModuleBbfFiber() throws Exception {
        executeLibCheckTest("get-specific-used-yang-module-bbf-fiber-response.xml",
                document -> libInfoChecker.getSpecificUsedYangModules(
                        "bbf-fiber", "2016-09-08",  document));
    }

    @Test
    public void getSpecificUsedYangModuleBbfFast() throws Exception {
        executeLibCheckTest("get-specific-used-yang-module-bbf-fast-response.xml",
                document -> libInfoChecker.getSpecificUsedYangModules(
                        "bbf-fast", "2017-03-13",  document));
    }

    @Test
    public void getSpecificUsedYangModuleWithInvalidYang() throws Exception {
        executeLibCheckTest("get-used-yang-module-empty-response.xml",
                document -> libInfoChecker.getSpecificUsedYangModules(
                        "invalid-yang", "2016-09-08", document));
    }

    @Test
    public void getAllUsedYangModules() throws Exception {

        executeLibCheckTest("get-all-used-yang-modules-response.xml",
                document -> libInfoChecker.getAllUsedYangModules(document));
    }

    @Test
    public void getUsedYangModulesByDeviceA() throws Exception {
        executeLibCheckTest("get-deviceA-used-yang-modules-response.xml",
                document -> libInfoChecker.getUsedYangModulesPerDevice(document, DEVICE_A));
    }

    @Test
    public void getUsedYangModulesByDeviceB() throws Exception {

        executeLibCheckTest("get-deviceB-used-yang-modules-response.xml",
                document -> libInfoChecker.getUsedYangModulesPerDevice(document, DEVICE_B));
    }

    private void executeLibCheckTest(String expectXml, Function<Document, Element> testFunction) throws Exception{
        NetConfResponse netConfResponse = new NetConfResponse().setMessageId("test123");
        Document responseDocument = netConfResponse.getResponseDocument();

        Element expectEl = DocumentUtils.loadXmlDocument(
                LibInfoCheckerImplTest.class.getResourceAsStream("/" + expectXml))
                .getDocumentElement();
        Element actualElement = testFunction.apply(responseDocument);

        netConfResponse.addDataContent(actualElement);
        System.out.println(netConfResponse.responseToString());
        TestUtil.assertXMLEquals(expectEl, actualElement);
    }

}