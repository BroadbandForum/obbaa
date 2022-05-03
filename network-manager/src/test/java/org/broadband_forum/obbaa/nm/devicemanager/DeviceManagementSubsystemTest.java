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

package org.broadband_forum.obbaa.nm.devicemanager;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.AUTHENTICATION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CONTAINER_DEVICE_TEMPLATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_MANAGEMENT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MANAGED_DEVICES;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MANAGEMENT_PORT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NETWORK_MANAGER;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NETWORK_MANAGER_ID_TEMPLATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlCalendar;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.device.adapter.AdapterBuilder;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.FactoryGarmentTag;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DeviceManagementSubsystemTest {

    public static final String DEVICE_A = "deviceA";
    public static final String DEVICE_B = "deviceB";
    public static final String ONU_DEVICE = "onuDevice";
    public static final String OLT = "OLT";
    public static final String DPU = "DPU";
    public static final String ONU = "ONU";
    private static final ModelNodeId DEVICE_A_ID_TEMPLATE = new ModelNodeId("/container=" + NETWORK_MANAGER + "/container=" + MANAGED_DEVICES
            + CONTAINER_DEVICE_TEMPLATE + DEVICE_A + "/container=" + DEVICE_MANAGEMENT, NS);
    private static final ModelNodeId DEVICE_B_ID_TEMPLATE = new ModelNodeId("/container=" + NETWORK_MANAGER + "/container=" + MANAGED_DEVICES
            + CONTAINER_DEVICE_TEMPLATE + DEVICE_B + "/container=" + DEVICE_MANAGEMENT, NS);

    private static final ModelNodeId DEVICE_ONU_ID_TEMPLATE = new ModelNodeId("/container=" + NETWORK_MANAGER + "/container=" + MANAGED_DEVICES
            + CONTAINER_DEVICE_TEMPLATE + ONU_DEVICE + "/container=" + DEVICE_MANAGEMENT, NS);
    Date m_now = new Date();
    private DeviceManagementSubsystem m_deviceManagementSubsystem;
    private SchemaRegistryImpl m_schemaRegistry;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private NetconfConnectionManager m_connectionManager;
    @Mock
    private DeviceInterface m_devInterface;
    @Mock
    private NetconfClientSession m_newDeviceSession1;
    @Mock
    private NetconfClientSession m_newDeviceSession2;
    private List<NewDeviceInfo> m_newDevices;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private AdapterContext m_context;
    private List<String> m_ignoreElements;

    @Before
    public void setup() throws SchemaBuildException {
        MockitoAnnotations.initMocks(this);
        m_schemaRegistry = getSchemaRegistry();
        m_deviceManagementSubsystem = new DeviceManagementSubsystem(m_schemaRegistry, m_adapterManager);
        m_deviceManagementSubsystem.setDeviceManager(m_deviceManager);
        m_deviceManagementSubsystem.setConnectionManager(m_connectionManager);
        setupDirectDevice(DEVICE_A, DPU);
        setupDirectDevice(DEVICE_B, OLT);
        setupONUDevice(ONU_DEVICE, ONU);
        setupNewDevices();
        setupAdapters();
        when(m_adapterManager.getAdapterContext(any())).thenReturn(m_context);
        when(m_context.getDeviceInterface()).thenReturn(m_devInterface);
        when(m_context.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        m_ignoreElements = new ArrayList<String>();
        m_ignoreElements.add("revision");
    }

    private void setupNewDevices() {
        Set<String> m_newDeviceCaps1 = new LinkedHashSet<>();
        m_newDeviceCaps1.add("caps1");
        m_newDeviceCaps1.add("caps2");

        Set<String> m_newDeviceCaps2 = new LinkedHashSet<>();
        m_newDeviceCaps2.add("caps3");
        m_newDeviceCaps2.add("caps4");

        when(m_newDeviceSession1.getServerCapabilities()).thenReturn(m_newDeviceCaps1);
        when(m_newDeviceSession2.getServerCapabilities()).thenReturn(m_newDeviceCaps2);
        m_newDevices = new ArrayList<>();
        m_newDevices.add(new NewDeviceInfo("duid-1234", m_newDeviceSession1));
        m_newDevices.add(new NewDeviceInfo("duid-5678", m_newDeviceSession2));
        when(m_connectionManager.getNewDevices()).thenReturn(m_newDevices);
    }

    private void setupDirectDevice(String name, String type) {
        DeviceState deviceState = new DeviceState();
        deviceState.setConfigAlignmentState("Aligned");
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnectionCreationTime(m_now);
        connectionState.setConnected(true);
        Set<String> deviceCaps = new LinkedHashSet<>();
        deviceCaps.add(name + "caps1");
        deviceCaps.add(name + "caps2");
        connectionState.setDeviceCapability(deviceCaps);
        Device directDevice = new Device();
        directDevice.setDeviceName(name);
        DeviceMgmt devicemgmt = new DeviceMgmt();
        devicemgmt.setDeviceState(deviceState);
        devicemgmt.setDeviceType(type);
        directDevice.setDeviceManagement(devicemgmt);
        when(m_deviceManager.getDevice(name)).thenReturn(directDevice);
        when(m_devInterface.getConnectionState(directDevice)).thenReturn(connectionState);
    }

    private void setupONUDevice(String name, String type) {
        DeviceState deviceState = new DeviceState();
        deviceState.setConfigAlignmentState("Aligned");
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnectionCreationTime(m_now);
        connectionState.setConnected(true);
        Set<String> deviceCaps = new LinkedHashSet<>();
        deviceCaps.add(name + "caps1");
        deviceCaps.add(name + "caps2");
        connectionState.setDeviceCapability(deviceCaps);
        Device onuDevice = new Device();
        onuDevice.setDeviceName(name);
        DeviceMgmt devicemgmt = new DeviceMgmt();
        devicemgmt.setDeviceState(deviceState);
        devicemgmt.setDeviceType(type);
        onuDevice.setDeviceManagement(devicemgmt);
        when(m_deviceManager.getDevice(name)).thenReturn(onuDevice);
        when(m_devInterface.getConnectionState(onuDevice)).thenReturn(connectionState);

        OnuStateInfo onuStateInfo= new OnuStateInfo();
        onuStateInfo.setEquipmentId("eqptId1");
        SoftwareImages softwareImages = new SoftwareImages();
        softwareImages.setSoftwareImagesId(ONU_DEVICE);
        softwareImages.setSoftwareImage(prepareSwImageSet(ONU_DEVICE));
        onuStateInfo.setSoftwareImages(softwareImages);
        deviceState.setOnuStateInfo(onuStateInfo);
    }

    private Set<SoftwareImage> prepareSwImageSet(String parentId) {
        Set<SoftwareImage> softwareImageSet = new HashSet<>();
        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setId(0);
        softwareImage0.setParentId(parentId);
        softwareImage0.setHash("1020");
        softwareImage0.setProductCode("test");
        softwareImage0.setVersion("1.0");
        softwareImage0.setIsValid(true);
        softwareImage0.setIsCommitted(true);
        softwareImage0.setIsActive(true);
        softwareImageSet.add(softwareImage0);
        return softwareImageSet;
    }
    private void setupAdapters() {
        when(m_adapterManager.getAdapterSize()).thenReturn(2);
        Collection<DeviceAdapter> adapters = new ArrayList<>();
        DeviceAdapter adapter1 = AdapterBuilder.createAdapterBuilder()
                .setDeviceAdapterId(new DeviceAdapterId("dpu", "interface1", "model1", "vendor1"))
                .build();
        adapter1.setNetconf(false);
        DeviceAdapter adapter2 = AdapterBuilder.createAdapterBuilder()
                .setDeviceAdapterId(new DeviceAdapterId("olt", "interface2", "model2", "vendor2"))
                .build();
        adapter2.setDeveloper("test");
        adapter2.setLastUpdateTime(DateTime.parse("2019-01-01T00:00:00Z"));
        List revisionList = new ArrayList<Calendar>();
        revisionList.add(new XmlCalendar("2019-01-02T08:00:00Z"));
        adapter2.setRevisions(revisionList);
        adapters.add(adapter1);
        adapters.add(adapter2);
        when(m_adapterManager.getAllDeviceAdapters()).thenReturn(adapters);
        ArrayList<String> adapter1DeviatedStdModules = new ArrayList<>();
        adapter1DeviatedStdModules.add(0, "ietf-hardware");
        adapter1DeviatedStdModules.add(1, "iana-hardware");
        ArrayList<String> adapter2DeviatedStdModules = new ArrayList<>();
        adapter2DeviatedStdModules.add(0, "ietf-interfaces");
        ArrayList<String> adapter1AugmentedStdModules = new ArrayList<>();
        adapter1AugmentedStdModules.add(0, "ietf-hardware");
        adapter1AugmentedStdModules.add(1, "iana-hardware");
        ArrayList<String> adapter2AugmentedStdModules = new ArrayList<>();
        adapter2AugmentedStdModules.add(0, "ietf-interfaces");
        when(m_adapterManager.getFactoryGarmentTag(adapter1)).thenReturn(new FactoryGarmentTag(12, 15, 10, adapter1DeviatedStdModules, adapter1AugmentedStdModules));
        when(m_adapterManager.getFactoryGarmentTag(adapter2)).thenReturn(new FactoryGarmentTag(15, 20, 10, adapter2DeviatedStdModules, adapter2AugmentedStdModules));
    }

    private List<YangTextSchemaSource> getByteSources(List<String> yangFiles) {
        List<YangTextSchemaSource> byteSrsList = new ArrayList<>();
        if (yangFiles != null) {
            for (String yang : yangFiles) {
                byteSrsList.add(getByteSource(yang));
            }
        }
        return byteSrsList;
    }

    private YangTextSchemaSource getByteSource(String file) {
        return YangParserUtil.getYangSource(DeviceManagementSubsystemTest.class.getResource(file));
    }


    private SchemaRegistryImpl getSchemaRegistry() throws SchemaBuildException {
        List<File> avYangFiles = Arrays.asList(new File(DeviceManagementSubsystemTest.class.getResource("/yangs").getFile()).listFiles());
        List<String> avYangFileNames = new ArrayList<>();
        for (File yangFile : avYangFiles) {
            avYangFileNames.add("/yangs/" + yangFile.getName());
        }
        return new SchemaRegistryImpl(getByteSources(avYangFileNames), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
    }

    private Document getDeviceStateDocumentWithConnectionStateFilter(String name) throws Exception {
        return DocumentUtils.stringToDocument(
                "<device-state xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                        "<connection-state>\n" +
                        "<connected>true</connected>\n" +
                        "<connection-creation-time>" + NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(new DateTime(m_now)) + "</connection-creation-time>\n" +
                        "<device-capability>" + name + "caps1</device-capability>\n" +
                        "<device-capability>" + name + "caps2</device-capability>\n" +
                        "</connection-state>\n" +
                        "</device-state>\n");
    }

    private Document getDeviceStateDocument(String name) throws Exception {
        return DocumentUtils.stringToDocument(
                "<device-state xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                        "<configuration-alignment-state>Aligned</configuration-alignment-state>\n" +
                        "<connection-state>\n" +
                        "<connected>true</connected>\n" +
                        "<connection-creation-time>" + NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(new DateTime(m_now)) + "</connection-creation-time>\n" +
                        "<device-capability>" + name + "caps1</device-capability>\n" +
                        "<device-capability>" + name + "caps2</device-capability>\n" +
                        "</connection-state>\n" +
                        "</device-state>\n");
    }

    private Document getOnuStateInfoDocument() throws Exception {
        return DocumentUtils.stringToDocument(
                "<device-state xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                        "<onu-state-info xmlns=\"urn:bbf:yang:obbaa:onu-management\">" +
                        "<equipment-id>eqptId1</equipment-id>" +
                        "<software-images>" +
                        "<software-image>" +
                        "<id>0</id>" +
                        "<version>1.0</version>" +
                                "<is-committed>true</is-committed>" +
                                "<is-active>true</is-active>" +
                                "<is-valid>true</is-valid>" +
                                "<product-code>test</product-code>" +
                                "<hash>1020</hash>" +
                                "</software-image>" +
                                "</software-images>" +
                                "</onu-state-info>" +
                        "</device-state>\n");
    }

    private FilterNode getFilterNode(Document filterDoc) throws Exception {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(filterDoc.getDocumentElement()));
        if (!root.getChildNodes().isEmpty()) {
            return root.getChildNodes().get(0);
        } else {
            return root.getSelectNodes().get(0);
        }
    }

    @Test
    public void retrieveStateAttributesOfDeviceA_DeviceB_Newdevices_DeviceAdapters() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document deviceStateFilter = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-state-request.xml"));
        mapAttributes.put(DEVICE_A_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        mapAttributes.put(DEVICE_B_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        Document filterNewDevicesWithDuid = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-new-devices-with-duid-request.xml"));
        Document filterDeviceAdapterWithType = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-adapter-with-type-request.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterNewDevicesWithDuid), getFilterNode(filterDeviceAdapterWithType))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(3, stateInfo.size());
        List<Element> deviceAState = stateInfo.get(DEVICE_A_ID_TEMPLATE);
        assertEquals(1, deviceAState.size());
        List<Element> deviceBState = stateInfo.get(DEVICE_B_ID_TEMPLATE);
        assertEquals(1, deviceBState.size());

        List<Element> newDeviceAndAdapter = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        assertEquals(2, newDeviceAndAdapter.size());
        Document deviceAStatedocument = getDeviceStateDocument(DEVICE_A);
        Document deviceBStatedocument = getDeviceStateDocument(DEVICE_B);
        TestUtil.assertXMLEquals(deviceAStatedocument.getDocumentElement(), deviceAState.get(0));
        TestUtil.assertXMLEquals(deviceBStatedocument.getDocumentElement(), deviceBState.get(0));
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-new-devices-with-duid-response.xml")).getDocumentElement(), newDeviceAndAdapter.get(0));
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/device-adapters-response.xml")).getDocumentElement(), newDeviceAndAdapter.get(1));
    }

    @Test
    public void retrieveStateAttributesOfDeviceA_DeviceB() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document deviceStateFilter = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-connection-state-request.xml"));
        mapAttributes.put(DEVICE_A_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        mapAttributes.put(DEVICE_B_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(2, stateInfo.size());
        List<Element> deviceAState = stateInfo.get(DEVICE_A_ID_TEMPLATE);
        assertEquals(1, deviceAState.size());
        List<Element> deviceBState = stateInfo.get(DEVICE_B_ID_TEMPLATE);
        assertEquals(1, deviceBState.size());

        Document deviceAStatedocument = getDeviceStateDocumentWithConnectionStateFilter(DEVICE_A);
        Document deviceBStatedocument = getDeviceStateDocumentWithConnectionStateFilter(DEVICE_B);
        TestUtil.assertXMLEquals(deviceAStatedocument.getDocumentElement(), deviceAState.get(0));
        TestUtil.assertXMLEquals(deviceBStatedocument.getDocumentElement(), deviceBState.get(0));
    }

    @Test
    public void retrieveStateAttributesOfDeviceA() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document deviceStateFilter = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-connection-state-request.xml"));
        mapAttributes.put(DEVICE_A_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> deviceAState = stateInfo.get(DEVICE_A_ID_TEMPLATE);
        assertEquals(1, deviceAState.size());
        Document deviceAStatedocument = getDeviceStateDocumentWithConnectionStateFilter(DEVICE_A);
        TestUtil.assertXMLEquals(deviceAStatedocument.getDocumentElement(), deviceAState.get(0));
    }

    @Test
    public void retrieveStateAttributesOfDeviceB() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document deviceStateFilter = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-connection-state-request.xml"));
        mapAttributes.put(DEVICE_B_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> deviceBState = stateInfo.get(DEVICE_B_ID_TEMPLATE);
        assertEquals(1, deviceBState.size());
        Document deviceBStatedocument = getDeviceStateDocumentWithConnectionStateFilter(DEVICE_B);
        TestUtil.assertXMLEquals(deviceBStatedocument.getDocumentElement(), deviceBState.get(0));
    }

    @Test
    public void retrieveStateAttributesOfOnuDevice() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document deviceStateFilter = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-onu-state-info-request.xml"));
        mapAttributes.put(DEVICE_ONU_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> onuDeviceState = stateInfo.get(DEVICE_ONU_ID_TEMPLATE);
        assertEquals(1, onuDeviceState.size());
        Document onuStateInfoDocument = getOnuStateInfoDocument();
        assertEquals(DocumentUtils.documentToPrettyString(onuStateInfoDocument.getDocumentElement()), DocumentUtils.documentToPrettyString(onuDeviceState.get(0)));

    }

    @Test
    public void retrieveStateAttributesNewDevicesAndDeviceAdapters() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document filterNewDevicesWithDuid = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-new-devices-with-duid-request.xml"));
        Document filterDeviceAdapterWithType = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-adapter-with-type-request.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterNewDevicesWithDuid), getFilterNode(filterDeviceAdapterWithType))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> newDeviceAndAdapter = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        assertEquals(2, newDeviceAndAdapter.size());
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-new-devices-with-duid-response.xml")).getDocumentElement(), newDeviceAndAdapter.get(0));
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/device-adapters-response.xml")).getDocumentElement(), newDeviceAndAdapter.get(1));
    }

    @Test
    public void retrieveStateAttributesNewDevice() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document filterNewDevicesWithDuid = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-new-devices-with-duid-request.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterNewDevicesWithDuid))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> newDevice = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        assertEquals(1, newDevice.size());
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-new-devices-with-duid-response.xml")).getDocumentElement(), newDevice.get(0));
    }

    @Test
    public void retrieveStateAttributesDeviceAdapters() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document filterDeviceAdapterWithType = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-adapter-with-type-request.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterDeviceAdapterWithType))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> deviceAdapter = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        assertEquals(1, deviceAdapter.size());
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/device-adapters-response.xml")).getDocumentElement(), deviceAdapter.get(0));
    }

    @Test
    public void testNotifyChangedDeviceCreate() {
        List<ChangeNotification> notifs = new ArrayList<>();
        ModelNodeId nodeId = new ModelNodeId("/container=network-manager/container=" + MANAGED_DEVICES, NS);
        EditContainmentNode editContainmentNode = new EditContainmentNode(QName.create(NS, DEVICE), EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(QName.create(NS, DEVICE), new GenericConfigAttribute("device", NS, DEVICE_A));
        ModelNodeChange change = new ModelNodeChange(ModelNodeChangeType.create, editContainmentNode);
        notifs.add(new EditConfigChangeNotification(nodeId, change, StandardDataStores.RUNNING, mock(ModelNode.class)));
        m_deviceManagementSubsystem.notifyChanged(notifs);
        verify(m_deviceManager).deviceAdded(DEVICE_A);
        verify(m_deviceManager, never()).deviceRemoved(DEVICE_A);
        verify(m_deviceManager, never()).devicePropertyChanged(DEVICE_A);
    }

    @Test
    public void testNotifyChangedDeviceDelete() {
        List<ChangeNotification> notifs = new ArrayList<>();
        ModelNodeId nodeId = new ModelNodeId("/container=network-manager/container=" + MANAGED_DEVICES, NS);
        EditContainmentNode editContainmentNode = new EditContainmentNode(QName.create(NS, DEVICE), EditConfigOperations.DELETE);
        editContainmentNode.addMatchNode(QName.create(NS, DEVICE), new GenericConfigAttribute("device", NS, DEVICE_A));
        ModelNodeChange change = new ModelNodeChange(ModelNodeChangeType.delete, editContainmentNode);
        notifs.add(new EditConfigChangeNotification(nodeId, change, StandardDataStores.RUNNING, mock(ModelNode.class)));
        m_deviceManagementSubsystem.notifyChanged(notifs);
        verify(m_deviceManager).deviceRemoved(DEVICE_A);
        verify(m_deviceManager, never()).deviceAdded(DEVICE_A);
        verify(m_deviceManager, never()).devicePropertyChanged(DEVICE_A);
    }

    @Test
    public void testNotifyChangedDevicePropertyChanged() {
        List<ChangeNotification> notifs = new ArrayList<>();
        ModelNodeId nodeId = new ModelNodeId("/container=network-manager/container=managed-devices"
                + "/container=device/name=" + DEVICE_A
                + "/container=device-management/container=device-connection"
                + "/container=password-auth/container=authentication", NS);
        EditContainmentNode editContainmentNode = new EditContainmentNode(QName.create(NS, AUTHENTICATION), EditConfigOperations.MERGE);
        editContainmentNode.addLeafChangeNode(QName.create(NS, MANAGEMENT_PORT), new GenericConfigAttribute(MANAGEMENT_PORT, NS, String.valueOf(30)));
        ModelNodeChange change = new ModelNodeChange(ModelNodeChangeType.merge, editContainmentNode);
        notifs.add(new EditConfigChangeNotification(nodeId, change, StandardDataStores.RUNNING, mock(ModelNode.class)));
        m_deviceManagementSubsystem.notifyChanged(notifs);
        verify(m_deviceManager, never()).deviceRemoved(DEVICE_A);
        verify(m_deviceManager, never()).deviceAdded(DEVICE_A);
        verify(m_deviceManager).devicePropertyChanged(DEVICE_A);
    }

    private Device createDirectDevicesForAdapterInUseTest(String devName, String intfVersion, String model, String type, String vendor) {
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

    private void prepareDevicesForAdapterInUseTest() {
        List<Device> deviceList = new ArrayList<>();
        Device deviceA = createDirectDevicesForAdapterInUseTest("DeviceA(BasedAdapter1)", "interface1", "model1", "dpu", "vendor1");
        deviceList.add(deviceA);
        when(m_deviceManager.getAllDevices()).thenReturn(deviceList);
    }

    @Test
    public void retrieveStateAttributesDeviceAdaptersWithInUseFilter() throws Exception {
        prepareDevicesForAdapterInUseTest();
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document filterDeviceAdapterWithType = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-adapter-with-inuse.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterDeviceAdapterWithType))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> deviceAdapter = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        assertEquals(1, deviceAdapter.size());
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/device-adapters-inuse-response.xml")).getDocumentElement(), deviceAdapter.get(0), m_ignoreElements);
    }

    @Test
    public void retrieveStateAttributesDeviceAdaptersWithoutFilter() throws Exception {
        prepareDevicesForAdapterInUseTest();
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document filterDeviceAdapterWithType = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-adapter.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterDeviceAdapterWithType))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(1, stateInfo.size());
        List<Element> deviceAdapter = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        assertEquals(1, deviceAdapter.size());
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-adapter-response.xml")).getDocumentElement(), deviceAdapter.get(0), m_ignoreElements);
    }

    @Test
    public void retrieveStateAttributesAllDeviceAdapters() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document filterDeviceAdapterWithType = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/get-device-adapters.xml"));
        mapAttributes.put(NETWORK_MANAGER_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(filterDeviceAdapterWithType))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        List<Element> deviceAdapter = stateInfo.get(NETWORK_MANAGER_ID_TEMPLATE);
        TestUtil.assertXMLEquals(DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/get-device-adapters-response.xml")).getDocumentElement(), deviceAdapter.get(0), m_ignoreElements);
    }

    @Test
    public void retrieveStateAttributesOfDeviceA_DeviceB_WhenAdapterNotdeployed() throws Exception {
        when(m_adapterManager.getAdapterContext(any())).thenReturn(null);
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes = new HashMap<>();
        Document deviceStateFilter = DocumentUtils.loadXmlDocument(DeviceManagementSubsystemTest.class.getResourceAsStream("/filter-device-state-request.xml"));
        mapAttributes.put(DEVICE_A_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        mapAttributes.put(DEVICE_B_ID_TEMPLATE, new Pair<>(Collections.emptyList(), Arrays.asList(getFilterNode(deviceStateFilter))));
        Map<ModelNodeId, List<Element>> stateInfo = m_deviceManagementSubsystem.retrieveStateAttributes(mapAttributes);
        assertEquals(2, stateInfo.size());
        List<Element> deviceAState = stateInfo.get(DEVICE_A_ID_TEMPLATE);
        assertEquals(1, deviceAState.size());
        List<Element> deviceBState = stateInfo.get(DEVICE_B_ID_TEMPLATE);
        assertEquals(1, deviceBState.size());
        String expectedDeviceState = "<device-state xmlns=\"urn:bbf:yang:obbaa:network-manager\"/>";
        assertEquals(expectedDeviceState, DocumentUtils.documentToPrettyString(deviceAState.get(0)).trim());
        assertEquals(expectedDeviceState, DocumentUtils.documentToPrettyString(deviceBState.get(0)).trim());
    }

}