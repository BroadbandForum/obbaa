package org.broadband_forum.obbaa.device.alarm.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.w3c.dom.Element;

public class AlarmNotificationUtilTest {

    private static final Revision REV = Revision.of("2018-07-30");
    private SchemaRegistry m_registry = mock(SchemaRegistry.class);
    private Module m_module = mock(Module.class);

    private static final String BAA_NAMESPACE = "http://www.example.com/network-manager/baa";
    private static final String NETWORK_MANAGER_NAMESPACE = "http://www.example.com/network-manager/managed-devices";
    private static final String IF_INTF_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String BBF_ALARM_TYPES_NS = "urn:broadband-forum-org:yang:dpu-alarm-types";

    private static final ModelNodeId MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, NETWORK_MANAGER_NAMESPACE, "device").addRdn("name", NETWORK_MANAGER_NAMESPACE, "'R1.S1.LT1.PON1.ONT1'").addRdn(ModelNodeRdn.CONTAINER, IF_INTF_NS, "interfaces").addRdn(ModelNodeRdn.CONTAINER, IF_INTF_NS, "interface").addRdn(ModelNodeRdn.NAME, IF_INTF_NS, "'xdsl-line:1/1/1/1'");
    private static final ModelNodeId PARTIAL_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, NETWORK_MANAGER_NAMESPACE, "device").addRdn("name", NETWORK_MANAGER_NAMESPACE, "'R1.S1.LT1.PON1.ONT1'");
    private Device m_device = null;
    private static final DeviceAdapterId m_deviceAdapterId = new DeviceAdapterId("DPU", "1.0", "Standard", "BBF");
    private DeviceAdapter m_deviceAdapter = null;

    @Before
    public void setup() {
        m_device = mock(Device.class);
        m_deviceAdapter = mock(DeviceAdapter.class);
        when(m_deviceAdapter.getDeviceAdapterId()).thenReturn(m_deviceAdapterId);
    }

    @Test
    public void testextractAndCreateAlarmEntryFromResponseVersion1() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmListStringVersion1()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromResponse(notificationElement, m_registry, m_device, m_deviceAdapterId, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromResponseMissingNamespace() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmListStringMissingNamespace()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromResponse(notificationElement, m_registry, m_device, m_deviceAdapterId, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText, Error processing AN alarm: Namespace is missing for prefix 'if' in resource", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("", info.getAlarmTypeQualifier());
        assertEquals(PARTIAL_MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromResponseVersion2() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmListStringVersion2()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromResponse(notificationElement, m_registry, m_device, m_deviceAdapterId, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromResponseVersion3() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmListStringVersion3()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromResponse(notificationElement, m_registry, m_device, m_deviceAdapterId, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("CommunicationLoss", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromResponseVersion4() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmListStringVersion4()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromResponse(notificationElement, m_registry, m_device, m_deviceAdapterId, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("CommunicationLoss", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromNotificationVersion1() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmNotificationStringVersion1()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(notificationElement, m_registry, m_device, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromNotificationVersion2() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmNotificationStringVersion2()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(notificationElement, m_registry, m_device, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromNotificationVersion3() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmNotificationStringVersion3()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(notificationElement, m_registry, m_device, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("CommLoss", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    @Test
    public void testextractAndCreateAlarmEntryFromNotificationVersion4() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmNotificationStringVersion4()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(notificationElement, m_registry, m_device, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("CommLoss", info.getAlarmTypeQualifier());
        assertEquals(MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }


    @Test
    public void testextractAndCreateAlarmEntryFromNotificationMissingNamespace() throws NetconfMessageBuilderException, URISyntaxException {
        QNameModule qNameModule = QNameModule.create(new URI(BBF_ALARM_TYPES_NS), REV);
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        when(m_registry.getModuleByNamespace(BBF_ALARM_TYPES_NS)).thenReturn(m_module);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        Element notificationElement = DocumentUtils.stringToDocument(getAlarmNotificationStringMissingNamespace()).getDocumentElement();
        List<AlarmInfo> infoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(notificationElement, m_registry, m_device, AlarmConstants.ALARM_NAMESPACE);
        assertEquals(1, infoList.size());
        AlarmInfo info = infoList.get(0);
        assertEquals("dummyText, Error processing AN alarm: Namespace is missing for prefix 'if' in resource", info.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, info.getSeverity());
        Optional<Revision> revision = qNameModule.getRevision();
        StringBuffer fullAlarmTypeId = new StringBuffer();
        fullAlarmTypeId.append("{dpu-al}(").append(BBF_ALARM_TYPES_NS);
        if (revision.isPresent()) {
            fullAlarmTypeId.append("?revision=").append(revision.get());
        }
        fullAlarmTypeId.append(")").append("xdsl-fe-los");
        assertEquals(fullAlarmTypeId.toString(), info.getAlarmTypeId());
        assertEquals("", info.getAlarmTypeQualifier());
        assertEquals(PARTIAL_MODEL_NODE_ID, info.getSourceObject());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
    }

    private String getAlarmNotificationStringVersion1() {
        String alarmNotification =
                "<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarms:resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                        + "  <alarms:alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarms:alarm-type-id>"
                        + "  <alarms:alarm-type-qualifier/>"
                        + "  <alarms:time>2016-11-07T17:45:02.004+05:30</alarms:time>"
                        + "  <alarms:perceived-severity>major</alarms:perceived-severity>"
                        + "  <alarms:alarm-text>dummyText</alarms:alarm-text>"
                        + "</alarms:alarm-notification>";
        return alarmNotification;
    }

    private String getAlarmNotificationStringVersion2() {
        String alarmNotification =
                "<alarm-notification xmlns=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\"  xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</resource>"
                        + "  <alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarm-type-id>"
                        + "  <alarm-type-qualifier/>"
                        + "  <time>2016-11-07T17:45:02.004+05:30</time>"
                        + "  <perceived-severity>major</perceived-severity>"
                        + "  <alarm-text>dummyText</alarm-text>"
                        + "</alarm-notification>";
        return alarmNotification;
    }

    //for non-empty alarm-type-Qualifier
    private String getAlarmNotificationStringVersion3() {
        String alarmNotification =
                "<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarms:resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                        + "  <alarms:alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarms:alarm-type-id>"
                        + "  <alarms:alarm-type-qualifier>CommLoss</alarms:alarm-type-qualifier>"
                        + "  <alarms:time>2016-11-07T17:45:02.004+05:30</alarms:time>"
                        + "  <alarms:perceived-severity>major</alarms:perceived-severity>"
                        + "  <alarms:alarm-text>dummyText</alarms:alarm-text>"
                        + "</alarms:alarm-notification>";
        return alarmNotification;
    }

    //for non-empty alarm-type-Qualifier
    private String getAlarmNotificationStringVersion4() {
        String alarmNotification =
                "<alarm-notification xmlns=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\"  xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</resource>"
                        + "  <alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarm-type-id>"
                        + "  <alarm-type-qualifier>CommLoss</alarm-type-qualifier>"
                        + "  <time>2016-11-07T17:45:02.004+05:30</time>"
                        + "  <perceived-severity>major</perceived-severity>"
                        + "  <alarm-text>dummyText</alarm-text>"
                        + "</alarm-notification>";
        return alarmNotification;
    }

    private String getAlarmNotificationStringMissingNamespace() {
        String alarmNotification =
                "<alarm-notification xmlns=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</resource>"
                        + "  <alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarm-type-id>"
                        + "  <alarm-type-qualifier/>"
                        + "  <time>2016-11-07T17:45:02.004+05:30</time>"
                        + "  <perceived-severity>major</perceived-severity>"
                        + "  <alarm-text>dummyText</alarm-text>"
                        + "</alarm-notification>";
        return alarmNotification;
    }

    private String getAlarmListStringVersion1() {
        String alarmNotification =
                "<alarms:alarms xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarms:alarm-list>"
                        + "   <alarms:alarms>"
                        + "    <alarms:resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                        + "    <alarms:alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarms:alarm-type-id>"
                        + "    <alarms:alarm-type-qualifier/>"
                        + "    <alarms:time>2016-11-07T17:45:02.004+05:30</alarms:time>"
                        + "    <alarms:perceived-severity>major</alarms:perceived-severity>"
                        + "    <alarms:alarm-text>dummyText</alarms:alarm-text>"
                        + "   </alarms:alarms>"
                        + "  </alarms:alarm-list>"
                        + "</alarms:alarms>";
        return alarmNotification;
    }

    private String getAlarmListStringVersion2() {
        String alarmNotification =
                "<alarms xmlns=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarm-list>"
                        + "   <alarm>"
                        + "    <resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\"  xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</resource>"
                        + "    <alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarm-type-id>"
                        + "    <alarm-type-qualifier/>"
                        + "    <time>2016-11-07T17:45:02.004+05:30</time>"
                        + "    <perceived-severity>major</perceived-severity>"
                        + "    <alarm-text>dummyText</alarm-text>"
                        + "   </alarm>"
                        + "  </alarm-list>"
                        + "</alarms>";
        return alarmNotification;
    }

    //for non-empty alarm-type-Qualifier
    private String getAlarmListStringVersion3() {
        String alarmNotification =
                "<alarms:alarms xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarms:alarm-list>"
                        + "   <alarms:alarms>"
                        + "    <alarms:resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                        + "    <alarms:alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarms:alarm-type-id>"
                        + "    <alarms:alarm-type-qualifier>CommunicationLoss</alarms:alarm-type-qualifier>"
                        + "    <alarms:time>2016-11-07T17:45:02.004+05:30</alarms:time>"
                        + "    <alarms:perceived-severity>major</alarms:perceived-severity>"
                        + "    <alarms:alarm-text>dummyText</alarms:alarm-text>"
                        + "   </alarms:alarms>"
                        + "  </alarms:alarm-list>"
                        + "</alarms:alarms>";
        return alarmNotification;
    }

    //for non-empty alarm-type-Qualifier
    private String getAlarmListStringVersion4() {
        String alarmNotification =
                "<alarms xmlns=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarm-list>"
                        + "   <alarm>"
                        + "    <resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\"  xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</resource>"
                        + "    <alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarm-type-id>"
                        + "    <alarm-type-qualifier>CommunicationLoss</alarm-type-qualifier>"
                        + "    <time>2016-11-07T17:45:02.004+05:30</time>"
                        + "    <perceived-severity>major</perceived-severity>"
                        + "    <alarm-text>dummyText</alarm-text>"
                        + "   </alarm>"
                        + "  </alarm-list>"
                        + "</alarms>";
        return alarmNotification;
    }

    private String getAlarmListStringMissingNamespace() {
        String alarmNotification =
                "<alarms xmlns=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                        + "  <alarm-list>"
                        + "   <alarm>"
                        + "    <resource xmlns:adh=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\">/baa:network-manager/adh:device[adh:name='R1.S1.LT1.PON1.ONT1']/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</resource>"
                        + "    <alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarm-type-id>"
                        + "    <alarm-type-qualifier/>"
                        + "    <time>2016-11-07T17:45:02.004+05:30</time>"
                        + "    <perceived-severity>major</perceived-severity>"
                        + "    <alarm-text>dummyText</alarm-text>"
                        + "   </alarm>"
                        + "  </alarm-list>"
                        + "</alarms>";
        return alarmNotification;
    }
}
