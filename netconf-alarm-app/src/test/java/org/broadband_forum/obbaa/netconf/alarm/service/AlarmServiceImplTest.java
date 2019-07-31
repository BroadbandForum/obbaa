package org.broadband_forum.obbaa.netconf.alarm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmCondition;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmNotification;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmTestConstants;
import org.broadband_forum.obbaa.netconf.alarm.api.DefaultAlarmStateChangeNotification;
import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.alarm.service.AlarmServiceImpl.AlarmQueueThread;
import org.broadband_forum.obbaa.netconf.alarm.util.AbstractAlarmSetup;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.w3c.dom.Element;

public class AlarmServiceImplTest extends AbstractAlarmSetup {

    private static final String RESOURCE_FORMAT = "/device-manager/device[name='%s']/";
    private static final String resourceString = "/baa:device-manager/network-manager:device[network-manager:name='R1.S1.LT1.P1.ONT1']/network-manager:device-specific-data/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']";
    private static final String TEST_DEVICE_ID = "R1.S1.LT1.P1.ONT1";
    private static final ModelNodeId INTF_MODEL_NODE_ID1 = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("name", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1").addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-specific-data").addRdn(ModelNodeRdn.CONTAINER, IETF_INTERFACE, "interfaces").addRdn(ModelNodeRdn.CONTAINER, IETF_INTERFACE, "interface").addRdn(ModelNodeRdn.NAME, IETF_INTERFACE, "xdsl-line:1/1/1/1");
    private static final ModelNodeId INTF_MODEL_NODE_ID2 = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("name", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1").addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-specific-data").addRdn(ModelNodeRdn.CONTAINER, IETF_INTERFACE, "interfaces").addRdn(ModelNodeRdn.CONTAINER, IETF_INTERFACE, "interface").addRdn(ModelNodeRdn.NAME, IETF_INTERFACE, "xdsl-line:1/1/1/2");
    private static final ModelNodeId UNREACHABLE_MODELNODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    private static final QName ALARMNOTIFICATION_QNAME = QName.create("test-namespace", "alarm-notification");
    NotificationService m_notificationService;
    private AlarmServiceImpl m_alarmService;
    private DefaultInternalAlarmServiceImpl m_internalAlarmService;
    private AlarmQueue m_alarmQueue;
    private SchemaMountRegistry m_schemaMountRegistry;
    private SchemaMountRegistryProvider m_schemaMountRegProvider;
    private ModelNode m_modelNode;
    private SchemaRegistry m_mountRegistry;
    private ModelServiceDeployer m_modelServiceDeployer = mock(ModelServiceDeployer.class);

    private List<Alarm> getAlarmsFor(int startRow, int maxRow) {
        PagingInput pagingInput = new PagingInput(startRow, maxRow);
        List<Alarm> alarms = m_alarmService.getAlarms(pagingInput);
        assertEquals(-1, pagingInput.getFirstResult());
        return alarms;
    }

    private List<Alarm> getAlarmsFor(int startRow, int maxRow, String deviceId) {
        PagingInput pagingInput = new PagingInput(startRow, maxRow);
        List<Alarm> alarms = m_alarmService.getAlarms(pagingInput, deviceId);
        assertEquals(-1, pagingInput.getFirstResult());
        return alarms;
    }

    private void assertMultiRecords(int maxAlarms, int firstRow, int maxRow) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        createAlarms(timestamp, maxAlarms);

        // start fetch and validate
        PagingInput paging = new PagingInput(firstRow, maxRow);

        /**
         * loop through the alarms for the maximum alarm entry, incrementing by
         * "maxRow" counts
         */
        for (int i = firstRow; i < maxAlarms; i = i + maxRow) {
            m_persistenceManager.getEntityDataStoreManager().beginTransaction();
            List<Alarm> alarms = m_alarmService.getAlarms(paging);
            m_persistenceManager.getEntityDataStoreManager().commitTransaction();

            /*
             * if
             * 		the current fetch start index and maximum requested row index
             * 		are less than maximum element count then
             * 		validate
             * 		1) if maximum row is fetched
             * 		2) if the next start index is incremented by maxRow
             * else
             * 		1) validate if the difference between last start
             * index and maxAlarms count is retrieved
             */
            if (i + maxRow < maxAlarms) {
                assertEquals(maxRow, alarms.size());
                assertEquals(i + maxRow, paging.getFirstResult());
            } else {
                assertEquals(maxAlarms - i, alarms.size());
            }
        }

        // validate if the end is indicated
        assertEquals(-1, paging.getFirstResult());

        destroyAlarms();
    }


    @Before
    public void setup() {
        super.setup();
        m_schemaMountRegistry = mock(SchemaMountRegistry.class);
        m_schemaMountRegProvider = mock(SchemaMountRegistryProvider.class);
        m_modelNode = mock(ModelNode.class);
        m_mountRegistry = mock(SchemaRegistry.class);
        when(m_schemaRegistry.getMountRegistry()).thenReturn(m_schemaMountRegistry);
        when(m_schemaMountRegistry.getProvider(DEVICE_SPECIFIC_DATA_PATH)).thenReturn(m_schemaMountRegProvider);
        when(m_dsm.findNode(DEVICE_SPECIFIC_DATA_PATH, ModelNodeKey.EMPTY_KEY, DEVICE_MODEL_NODE_ID)).thenReturn(m_modelNode);
        when(m_schemaMountRegProvider.getSchemaRegistry(m_modelNode)).thenReturn(m_mountRegistry);
        Module ifModule = mock(Module.class);
        when(m_mountRegistry.getModuleByNamespace(IETF_INTERFACE)).thenReturn(ifModule);
        when(ifModule.getPrefix()).thenReturn("if");
        when(ifModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_mountRegistry.getPrefix(IETF_INTERFACE)).thenReturn("if");
        when(m_mountRegistry.getModuleNameByNamespace(IETF_INTERFACE)).thenReturn("ietf-interfaces");
        when(m_mountRegistry.getNamespaceOfModule("ietf-interfaces")).thenReturn(IETF_INTERFACE);
        when(m_mountRegistry.getNamespaceURI("if")).thenReturn(IETF_INTERFACE);
        when(m_schemaRegistry.getPrefix(IETF_INTERFACE)).thenReturn("if");
        when(m_schemaRegistry.getModuleNameByNamespace(IETF_INTERFACE)).thenReturn("ietf-interfaces");
        when(m_schemaRegistry.getNamespaceOfModule("ietf-interfaces")).thenReturn(IETF_INTERFACE);
        when(m_schemaRegistry.getNamespaceURI("if")).thenReturn(IETF_INTERFACE);
        when(m_schemaRegistry.getModuleByNamespace(IETF_INTERFACE)).thenReturn(ifModule);
        m_notificationService = mock(NotificationService.class);
        m_internalAlarmService = new DefaultInternalAlarmServiceImpl(m_schemaRegistry, m_persistenceManager, m_dsm, m_notificationService, new TxService());
        m_alarmQueue = new AlarmQueue(m_internalAlarmService);
        m_alarmService = new AlarmServiceImpl(m_persistenceManager, m_alarmQueue);
    }

    @After
    public void teardown() {
        reset(m_mountRegistry);
    }

    @Test
    public void testRaisedClearedAlarmList() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        // Build AlarmInfo
        AlarmInfo alarmInfo1 = buildAlarmInfo1();
        AlarmInfo alarmInfo2 = buildAlarmInfo2();
        List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
        alarmInfoList.add(alarmInfo1);
        alarmInfoList.add(alarmInfo2);

        // Raise Alarm using List<AlarmInfo>
        m_alarmService.raiseAlarms(alarmInfoList);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        // Verify Alarm is raised
        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        verify(m_notificationService).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));
        assertEquals(2, m_internalAlarmService.getRaisedAlarms());
        assertEquals(2, alarms.size());

        // Clear Alarm using List<AlarmInfo>
        m_alarmService.clearAlarms(alarmInfoList);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        // Verify Alarm is cleared
        manager.beginTransaction();
        alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(2, m_internalAlarmService.getClearedAlarms());
        assertEquals(0, alarms.size());
    }

    @Test
    public void testTogglingAlarm() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();
        AlarmInfo alarmInfo = buildAlarmInfo();
        m_alarmService.raiseAlarm(alarmInfo);
        m_alarmService.raiseAlarm(alarmInfo);
        m_alarmService.clearAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(1, m_internalAlarmService.getTogglingAlarms());

    }

    @Test
    public void testActiveAlarms() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();
        AlarmInfo alarmInfo = buildAlarmInfo();
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(1, m_internalAlarmService.getActiveAlarms());

    }

    @Test
    public void testAlarm() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        // Build AlarmInfo
        AlarmInfo alarmInfo = buildAlarmInfo();

        // Raise Alarm
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(1, m_internalAlarmService.getRaisedAlarms());
        // verify send notification is called
        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));

        // verify the notification
        DefaultAlarmStateChangeNotification notification = m_internalAlarmService.getNotification();
        List<AlarmNotification> alarmNotifications = notification.getAlarmNotification();
        assertEquals(1, alarmNotifications.size());
        AlarmNotification alarmNotification = alarmNotifications.get(0);
        assertEquals(AlarmCondition.ALARM_ON, alarmNotification.getAlarmCondition());
        assertEquals(alarmInfo.getTime(), alarmNotification.getLastStatusChange());

        // Verify Alarm is raised
        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(1, alarms.size());

        // Verify the raised alarm
        Alarm actualAlarm = alarms.get(0);
        assertEquals(generateDBID(1), actualAlarm.getSourceObject());
        assertEquals(ALARM_TEXT, actualAlarm.getAlarmText());
        assertEquals(alarmInfo.getTime(), actualAlarm.getRaisedTime());
        assertEquals(AlarmSeverity.CRITICAL, actualAlarm.getSeverity());
        assertEquals(DEVICE_ID, actualAlarm.getDeviceId());

        reset(m_notificationService);
        // Case 2: Raise the same alarm again
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        assertEquals(1, m_internalAlarmService.getRaisedAlarms());

        verify(m_notificationService, times(0)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));

        alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        // Expected Result: Alarm should not raise
        assertEquals(1, alarms.size());

        //update alarm
        AlarmInfo updateAlarmInfo = buildUpdateAlarmInfo();
        reset(m_notificationService);
        m_alarmService.raiseAlarm(updateAlarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(1, m_internalAlarmService.getRaisedAlarms());
        assertEquals(1, m_internalAlarmService.getUpdatedAlarms());

        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));

        manager.beginTransaction();
        List<Alarm> alarmList = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(1, alarmList.size());

        // Verify the update alarm
        Alarm updateAlarm = alarmList.get(0);
        assertEquals(generateDBID(1), updateAlarm.getSourceObject());
        assertEquals(UPDATE_ALARM_TEXT, updateAlarm.getAlarmText());
        assertEquals(AlarmSeverity.MAJOR, updateAlarm.getSeverity());


        // Clear the raised alarm
        reset(m_notificationService);
        alarmInfo = buildClearedAlarmInfo();
        m_alarmService.clearAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        assertEquals(1, m_internalAlarmService.getClearedAlarms());

        // verify send notification is called
        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));

        // verify the notification
        notification = m_internalAlarmService.getNotification();
        alarmNotifications = notification.getAlarmNotification();
        assertEquals(1, alarmNotifications.size());
        alarmNotification = alarmNotifications.get(0);
        assertEquals(AlarmCondition.ALARM_OFF, alarmNotification.getAlarmCondition());
        assertEquals(alarmInfo.getTime(), alarmNotification.getLastStatusChange());

        manager.beginTransaction();
        alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertTrue(alarms.isEmpty());
    }

    @Test
    public void testAlarmResync() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        AlarmInfo alarmInfo1 = buildAlarmInfo1();
        AlarmInfo alarmInfo2 = buildAlarmInfo2();

        m_alarmService.raiseAlarm(alarmInfo1);
        m_alarmService.raiseAlarm(alarmInfo2);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        verify(m_notificationService).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));
        assertEquals(2, m_internalAlarmService.getRaisedAlarms());

        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(2, alarms.size());

        reset(m_notificationService);
        m_alarmService.updateAlarmsForResync(getNewRaiseAlarmList(), getUpdateAlarmList(), getClearAlarmList());
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        verify(m_notificationService).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));
        assertEquals(3, m_internalAlarmService.getRaisedAlarms());
        assertEquals(1, m_internalAlarmService.getUpdatedAlarms());
        assertEquals(1, m_internalAlarmService.getClearedAlarms());

        manager.beginTransaction();
        alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(2, alarms.size());

        // Clear the new raised alarm
        reset(m_notificationService);
        m_alarmService.clearAlarm(getNewRaiseAlarmList().get(0));
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));
        assertEquals(2, m_internalAlarmService.getClearedAlarms());

        // Clear the updated alarm
        reset(m_notificationService);
        m_alarmService.clearAlarm(getUpdateAlarmList().get(0));
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));
        assertEquals(3, m_internalAlarmService.getClearedAlarms());
    }

    @Test
    public void testGetExistingAlarmsByResource() throws Exception {
        when(m_schemaRegistry.getMountRegistry()).thenReturn(m_schemaMountRegistry);
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        AlarmInfo alarmInfo1 = buildAlarmInfo3();
        m_alarmService.raiseAlarm(alarmInfo1);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));

        // verify the notification
        DefaultAlarmStateChangeNotification notification = m_internalAlarmService.getNotification();
        ModelNodeDataStoreManager actualDsm = notification.getModelNodeDSM();
        assertNotNull(actualDsm);
        assertEquals(m_dsm, actualDsm);
        Element actualNotificationElement = notification.getNotificationElement();
        TestUtil.assertXMLEquals(getExpectedNotificationElement(), actualNotificationElement, m_ignoreElements);
        Element alarmElement = (Element) actualNotificationElement.getElementsByTagNameNS(AlarmConstants.ALARM_NAMESPACE, AlarmConstants.ALARM_ELEMENT).item(0);
        Element resourceElement = (Element) alarmElement.getElementsByTagNameNS(AlarmConstants.ALARM_NAMESPACE, AlarmConstants.ALARM_RESOURCE).item(0);
        assertEquals(resourceString, resourceElement.getTextContent());
        assertEquals(AlarmTestConstants.BAA_NAMESPACE, resourceElement.lookupNamespaceURI("baa"));
        assertEquals(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, resourceElement.lookupNamespaceURI("baa-network-manager"));
        assertEquals(IETF_INTERFACE, resourceElement.lookupNamespaceURI("if"));

        AlarmInfo alarmInfo2 = buildUnReachableAlarm();
        m_alarmService.raiseAlarm(alarmInfo2);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        verify(m_notificationService, times(2)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));

        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(2, alarms.size());

        AlarmInfo alarmInfo3 = buildAlarmInfo2();
        m_alarmService.raiseAlarm(alarmInfo3);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        manager.beginTransaction();
        alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(3, alarms.size());

        String resourceMatchLike = String.format(RESOURCE_FORMAT, "R1.S1.LT1.P1.ONT1");
        manager.beginTransaction();
        List<Alarm> existingAlarms = m_alarmService.getAlarmsUnderResource(resourceMatchLike);
        manager.commitTransaction();
        assertEquals(2, existingAlarms.size());
    }

    private Element getExpectedNotificationElement() {
        String notificationString = "<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                + "<alarms:alarm>"
                + "<alarms:resource xmlns:network-manager=\"http://www.example.com/network-manager/managed-devices\"  xmlns:baa=\"http://www.example.com/network-manager/baa\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/baa:device-manager/network-manager:device[network-manager:name='R1.S1.LT1.P1.ONT1']/network-manager:device-specific-data/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                + "<alarms:alarm-type-id xmlns:baa-network-manager=\"http://www.example.com/network-manager/managed-devices\">baa-network-manager:device-alarm</alarms:alarm-type-id>"
                + "<alarms:alarm-type-qualifier xmlns:baa-network-manager=\"http://www.example.com/network-manager/managed-devices\">baa-network-manager:fast-ftu-r-loss-of-margin</alarms:alarm-type-qualifier>"
                + "<alarms:time>2018-11-08T17:08:19.151+05:30</alarms:time>"
                + "<alarms:perceived-severity>critical</alarms:perceived-severity>"
                + "<alarms:alarm-text>text1</alarms:alarm-text>"
                + "</alarms:alarm>"
                + "</alarms:alarm-notification>";
        return TestUtil.transformToElement(notificationString);
    }

    private AlarmInfo buildUnReachableAlarm() {
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo("TestId1", "Test_AlarmType_Qualifier", UNREACHABLE_MODELNODE_ID, raisedTime, AlarmSeverity.CRITICAL,
                "device is disconnected", TEST_DEVICE_ID);
        return alarmInfo;
    }

    private AlarmInfo buildAlarmInfo1() {
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo("TestId1", "Test_AlarmType_Qualifier", INTF_MODEL_NODE_ID1, raisedTime, AlarmSeverity.CRITICAL,
                "text1", TEST_DEVICE_ID);
        return alarmInfo;
    }

    private AlarmInfo buildAlarmInfo2() {
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo("TestId2", "Test_AlarmType_Qualifier", INTF_MODEL_NODE_ID2, raisedTime, AlarmSeverity.MAJOR,
                "text2", TEST_DEVICE_ID);
        return alarmInfo;
    }

    private AlarmInfo buildAlarmInfo3() {
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, INTF_MODEL_NODE_ID1, raisedTime, AlarmSeverity.CRITICAL,
                "text1", TEST_DEVICE_ID);
        alarmInfo.setSourceObjectNamespaces("xmlns:baa-network-manager=\"http://www.example.com/network-manager/managed-devices\" xmlns:baa=\"http://www.example.com/network-manager/baa\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\"");
        alarmInfo.setSourceObjectString(resourceString);
        return alarmInfo;
    }

    private List<AlarmInfo> getNewRaiseAlarmList() {
        List<AlarmInfo> raiseAlarmList = new ArrayList<>();
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo("TestId3", "Test_AlarmType_Qualifier", INTF_MODEL_NODE_ID1, raisedTime, AlarmSeverity.MAJOR,
                "text3", TEST_DEVICE_ID);
        raiseAlarmList.add(alarmInfo);
        return raiseAlarmList;
    }

    private List<AlarmInfo> getUpdateAlarmList() {
        List<AlarmInfo> updateAlarmList = new ArrayList<>();
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo("TestId1", "Test_AlarmType_Qualifier", INTF_MODEL_NODE_ID1, raisedTime, AlarmSeverity.MAJOR,
                "updateAlarmText", TEST_DEVICE_ID);
        updateAlarmList.add(alarmInfo);
        return updateAlarmList;
    }

    private List<AlarmInfo> getClearAlarmList() {
        List<AlarmInfo> clearAlarmList = new ArrayList<>();
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo("TestId2", "Test_AlarmType_Qualifier", INTF_MODEL_NODE_ID2, raisedTime, AlarmSeverity.MAJOR,
                "text2", TEST_DEVICE_ID);
        clearAlarmList.add(alarmInfo);
        return clearAlarmList;
    }

    private List<Alarm> getAlarms() {
        List<Alarm> alarms = new ArrayList<Alarm>();

        Alarm alarm1 = new Alarm();
        alarm1.setAlarmTypeId("TestId1");
        alarm1.setAlarmTypeQualifier("Test_AlarmType_Qualifier");

        Alarm alarm2 = new Alarm();
        alarm2.setAlarmTypeId("TestId2");
        alarm2.setAlarmTypeQualifier("Test_AlarmType_Qualifier");

        Alarm alarm3 = new Alarm();
        alarm3.setAlarmTypeId("TestId3");
        alarm3.setAlarmTypeQualifier("Test_AlarmType_Qualifier");

        Alarm alarm4 = new Alarm();
        alarm4.setAlarmTypeId(ALARM_TYPE_ID);
        alarm4.setAlarmTypeQualifier(ALARM_TYPE_QUALIFIER);

        alarms.add(alarm1);
        alarms.add(alarm2);
        alarms.add(alarm3);
        alarms.add(alarm4);
        return alarms;
    }

    @Test
    public void testRaiseUnknownAlarm() {
        // Build AlarmInfo
        AlarmInfo alarmInfo = buildAlarmInfo();
        // Raise Alarm - this should not give an exception, only log an error
        m_alarmService.raiseAlarm(alarmInfo);
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        m_alarmQueue.clearQueue();
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();
    }

    @Test
    public void testClearUnknownAlarm() {
        // Build AlarmInfo
        AlarmInfo alarmInfo = buildAlarmInfo();
        // Clear Alarm - this should not give an exception, only log an error
        m_alarmService.clearAlarm(alarmInfo);
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        m_alarmQueue.clearQueue();
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();
    }

    @Test
    public void testAlarmWithEmptyAlarmText() {
        AlarmInfo alarmInfo = buildAlarmInfoWithoutAlarmText();
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        m_alarmService.raiseAlarm(alarmInfo);
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        m_alarmQueue.clearQueue();
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();

        m_alarmService.clearAlarm(alarmInfo);
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        m_alarmQueue.clearQueue();
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();
    }

    @Test
    public void testGetAlarms() {
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        List<Alarm> alarms = getAlarmsFor(0, 0);
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();
        assertEquals(0, alarms.size());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        createAlarms(timestamp, 1);

        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        // now validate different cases
        alarms = getAlarmsFor(0, 1);
        assertEquals(1, alarms.size());
        assertEquals(timestamp, alarms.get(0).getRaisedTime());

        alarms = getAlarmsFor(0, 100);
        assertEquals(1, alarms.size());
        assertEquals(timestamp, alarms.get(0).getRaisedTime());

        alarms = getAlarmsFor(1, 100);
        assertEquals(0, alarms.size());

        alarms = getAlarmsFor(1, 1);
        assertEquals(0, alarms.size());

        alarms = getAlarmsFor(0, 1);
        assertEquals(1, alarms.size());
        assertEquals(timestamp, alarms.get(0).getRaisedTime());

        m_persistenceManager.getEntityDataStoreManager().commitTransaction();

        destroyAlarms();
    }

    @Test
    public void testGetAlarms_By_DeviceID() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        createAlarms(timestamp, 5);

        m_persistenceManager.getEntityDataStoreManager().beginTransaction();

        List<Alarm> alarms = getAlarmsFor(0, 5);
        assertEquals(5, alarms.size());

        alarms = getAlarmsFor(0, 5, null);
        assertEquals(5, alarms.size());

        alarms = getAlarmsFor(0, 5, DEVICE_ID);
        assertEquals(1, alarms.size());
        assertEquals(timestamp, alarms.get(0).getRaisedTime());
        assertEquals(DEVICE_ID, alarms.get(0).getDeviceId());

        alarms = getAlarmsFor(0, 1, DEVICE_ID2);
        assertEquals(1, alarms.size());

        alarms = getAlarmsFor(0, 1, DEVICE_ID + "1");
        assertEquals(0, alarms.size());

        m_persistenceManager.getEntityDataStoreManager().commitTransaction();

        destroyAlarms();
    }

    @Test
    public void testMultiAlarms() {
        // check various cases of fetch
        assertMultiRecords(5, 0, 1);
        assertMultiRecords(100, 0, 9);
        assertMultiRecords(1000, 0, 99);
        assertMultiRecords(100, 0, 45);
        assertMultiRecords(100, 0, 49);
        assertMultiRecords(100, 0, 13);
        assertMultiRecords(1000, 0, 123);
        assertMultiRecords(1000, 100, 100);
        assertMultiRecords(100, 35, 20);
    }

    @Test
    public void testClearAlarmsOnNodeAndSubtree() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        createAlarms(timestamp, 1);
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        List<Alarm> alarms = getAlarmsFor(0, 1);
        assertEquals(1, alarms.size());
        m_alarmService.clearAlarmsOnNodeAndSubtree(MODEL_NODE_ID, MOUNT_KEY);
        m_alarmQueue.clearQueue();
        alarms = getAlarmsFor(0, 1);
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();
        assertEquals(0, alarms.size());
        // verify send notification is called
        verify(m_notificationService, times(1)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME), any(DefaultAlarmStateChangeNotification.class));
        // verify the notification
        DefaultAlarmStateChangeNotification notification = m_internalAlarmService.getNotification();
        ModelNodeDataStoreManager actualDsm = notification.getModelNodeDSM();
        assertNotNull(actualDsm);
        assertEquals(m_dsm, actualDsm);
        List<AlarmNotification> alarmNotifications = notification.getAlarmNotification();
        assertEquals(1, alarmNotifications.size());
        AlarmNotification alarmNotification = alarmNotifications.get(0);
        assertEquals(AlarmCondition.ALARM_OFF, alarmNotification.getAlarmCondition());

        destroyAlarms();
    }

    @Test
    public void testClearAlarmsOnNodeAndSubtreeForEmptyAlarmList() {
        PersistenceManagerUtil persistenceManagerUtil = mock(PersistenceManagerUtil.class);
        EntityDataStoreManager entityDataStoreManager = mock(EntityDataStoreManager.class);
        AlarmServiceImpl alarmService = new AlarmServiceImpl(persistenceManagerUtil, m_alarmQueue);
        Map<String, String> matchValue = new HashMap<String, String>();
        List<Alarm> alarms = new ArrayList<Alarm>();

        when(persistenceManagerUtil.getEntityDataStoreManager()).thenReturn(entityDataStoreManager);

        // when empty alarm list is returned
        when(entityDataStoreManager.findLike(Alarm.class, matchValue)).thenReturn(alarms);
        alarmService.clearAlarmsOnNodeAndSubtree(MODEL_NODE_ID, MOUNT_KEY);
        verify(m_notificationService, times(0)).sendNotification(eq(AlarmConstants.ALARM_STREAM_NAME),
                any(DefaultAlarmStateChangeNotification.class));
    }

    @Test
    public void testAlarmQueueThreadInitialization() {
        m_alarmService.init();
        assertNotNull(m_alarmService.getAlarmQueueThread());
        m_alarmService.close();
    }

    @Test
    public void testAlarmQueueThread() {

        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        AlarmQueueThread alarmQueueThread = new AlarmQueueThread(m_alarmService.getAlarmQueue());

        AlarmInfo alarmInfo = buildAlarmInfo();
        m_alarmService.raiseAlarm(alarmInfo);

        manager.beginTransaction();
        alarmQueueThread.doOneRun();
        manager.commitTransaction();

        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(1, alarms.size());

        // Verify the raised alarm
        Alarm actualAlarm = alarms.get(0);
        assertEquals(generateDBID(1), actualAlarm.getSourceObject());
        assertEquals(ALARM_TEXT, actualAlarm.getAlarmText());
        assertEquals(alarmInfo.getTime(), actualAlarm.getRaisedTime());
        assertEquals(AlarmSeverity.CRITICAL, actualAlarm.getSeverity());
    }

    @Test
    public void testAlarmQueueThreadError() {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        AlarmQueue alarmQueue = mock(AlarmQueue.class);
        AlarmQueueThread alarmQueueThread = new AlarmQueueThread(alarmQueue);

        AlarmInfo alarmInfo = buildAlarmInfo();
        m_alarmService.raiseAlarm(alarmInfo);

        doThrow(new RuntimeException("error")).when(alarmQueue).clearQueue();

        // should not throw an exception, only log
        manager.beginTransaction();
        alarmQueueThread.doOneRun();
        manager.commitTransaction();

        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(0, alarms.size());
    }

    @Test
    public void testRetrieveTypeVersionsFromAlarmNotifQName() {
        Set<String> typeVersions = new HashSet<>();
        typeVersions.add("TEST.1.0");
        m_alarmService.updateAlarmNotificationQNameToAdapterTypeVersion(ALARMNOTIFICATION_QNAME, "TEST.1.0");
        assertEquals(typeVersions, m_alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(ALARMNOTIFICATION_QNAME));

        typeVersions.add("TEST1.1.0");
        m_alarmService.updateAlarmNotificationQNameToAdapterTypeVersion(ALARMNOTIFICATION_QNAME, "TEST1.1.0");
        assertEquals(typeVersions, m_alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(ALARMNOTIFICATION_QNAME));

        typeVersions.remove("TEST.1.0");
        m_alarmService.removeAlarmNotificationQNameToAdapterTypeVersion(ALARMNOTIFICATION_QNAME, "TEST.1.0");
        assertEquals(typeVersions, m_alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(ALARMNOTIFICATION_QNAME));

        typeVersions.remove("TEST1.1.0");
        m_alarmService.removeAlarmNotificationQNameToAdapterTypeVersion(ALARMNOTIFICATION_QNAME, "TEST1.1.0");
        assertEquals(typeVersions, m_alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(ALARMNOTIFICATION_QNAME));
    }


    /**
     * Below UT's are cover resourceObject type as String
     */

    @Test
    public void testGetActiveAlarms() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        // Raise an alarm with resourceObject as string and verify active alarms
        AlarmInfo alarmInfo = buildAlarmInfoWithResourceTypeString();
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(1, m_internalAlarmService.getActiveAlarms());

        //Raise an alarm with resourceobject as modelnodeid
        alarmInfo = buildAlarmInfo();
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(2, m_internalAlarmService.getActiveAlarms());

    }

    @Test
    public void testClearAlarms_AlarmResourceTypeAsString() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        AlarmInfo alarmInfo = buildAlarmInfoWithResourceTypeString();
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(1, m_internalAlarmService.getActiveAlarms());

        // Verify raised active alarms
        manager.beginTransaction();
        List<Alarm> alarms = manager.findAll(Alarm.class);
        manager.commitTransaction();
        assertEquals(1, alarms.size());
        Alarm alarm = alarms.get(0);
        assertEquals(RESOURCETYPE_STRING, alarm.getSourceObject());
        assertTrue(!alarm.isYangResource());

        // Clear Alarm with ResourceType as String
        m_alarmService.clearAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();
        assertEquals(0, m_internalAlarmService.getActiveAlarms());
    }

    @Test
    public void testClearActiveAlarms_BothResourceObjectAsStringAndModelNodeId() throws Exception {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
        AlarmInfo alarmInfo1 = buildAlarmInfoWithResourceTypeString();
        AlarmInfo alarmInfo2 = buildAlarmInfo();
        alarmInfoList.add(alarmInfo1);
        alarmInfoList.add(alarmInfo2);

        // Raise alarms
        m_alarmService.raiseAlarms(alarmInfoList);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        //Verify active alarms
        assertEquals(2, m_internalAlarmService.getRaisedAlarms());
        assertEquals(2, m_internalAlarmService.getActiveAlarms());

        // Clear Alarm which have resource object as string
        m_alarmService.clearAlarm(alarmInfo1);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        //Verify cleared and active alarm's count
        assertEquals(1, m_internalAlarmService.getClearedAlarms());
        assertEquals(1, m_internalAlarmService.getActiveAlarms());

        // Clear all active alarms using List<AlarmInfo>
        m_alarmService.clearAlarms(alarmInfoList);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        assertEquals(2, m_internalAlarmService.getClearedAlarms());
        assertEquals(0, m_internalAlarmService.getActiveAlarms());
    }

    @Test
    public void testGetAllAlarmsAndClearAllAlarmsByDeviceID() {
        // create 5 alarms for 5 different devices
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        createAlarms(timestamp, 5);

        // raise an alarm for resource object as string
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();
        AlarmInfo alarmInfo = buildAlarmInfoWithResourceTypeString();
        m_alarmService.raiseAlarm(alarmInfo);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        // Verify Get all active alarms by deviceId with paging input
        m_persistenceManager.getEntityDataStoreManager().beginTransaction();
        List<Alarm> alarms = getAlarmsFor(0, 10);
        assertEquals(6, alarms.size());

        alarms = getAlarmsFor(0, 10, null);
        assertEquals(6, alarms.size());

        // Get the alarms by device-id and validate alarm info
        alarms = getAlarmsFor(0, 5, DEVICE_ID);
        assertEquals(2, alarms.size());
        assertEquals(timestamp, alarms.get(0).getRaisedTime());
        assertEquals(DEVICE_ID, alarms.get(0).getDeviceId());
        assertEquals(generateDBID(1), alarms.get(0).getSourceObject());

        assertEquals(DEVICE_ID, alarms.get(1).getDeviceId());
        assertEquals(RESOURCETYPE_STRING, alarms.get(1).getSourceObject());

        alarms = getAlarmsFor(0, 1, DEVICE_ID2);
        assertEquals(1, alarms.size());

        alarms = getAlarmsFor(0, 1, DEVICE_ID + "1");
        assertEquals(0, alarms.size());

        // Verify clear active alarms by deviceID
        m_alarmService.clearAlarmsOnDevice(DEVICE_ID);
        m_alarmQueue.clearQueue();

        alarms = getAlarmsFor(0, 5, DEVICE_ID);
        assertEquals(0, alarms.size());

        alarms = getAlarmsFor(0, 10);
        assertEquals(4, alarms.size());
        m_persistenceManager.getEntityDataStoreManager().commitTransaction();

        destroyAlarms();
    }

    @Test
    public void testClearAlarmsOnDevice() {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();
        // Raise an alarms with resource type as string as well as modelnode
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        createAlarms(timestamp, 3);
        AlarmInfo alarm1 = buildAlarmInfoWithResourceTypeString();
        AlarmInfo alarm2 = buildAlarmInfoWithResourceTypeString_Device2();
        m_alarmService.raiseAlarm(alarm1);
        m_alarmService.raiseAlarm(alarm2);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        // Verify the count of active alarms
        List<Alarm> alarms = getAlarmsFor(0, 10);
        assertEquals(5, alarms.size());

        // clear alarms with different ways for device1 and device2
        m_alarmService.clearAlarm(alarm1);
        m_alarmService.clearAlarm(alarm2);

        m_alarmService.clearAlarmsOnNodeAndSubtree(MODEL_NODE_ID, MOUNT_KEY);
        m_alarmService.clearAlarmsOnNodeAndSubtree(MODEL_NODE_ID2, MOUNT_KEY);

        m_alarmService.clearAlarmsOnDevice(DEVICE_ID);
        m_alarmService.clearAlarmsOnNodeAndSubtree(MODEL_NODE_ID2, MOUNT_KEY);
        m_alarmService.clearAlarmsOnDevice(DEVICE_ID2);
        m_alarmService.clearAlarmsOnDevice(DEVICE_ID);
        manager.beginTransaction();
        m_alarmQueue.clearQueue();
        manager.commitTransaction();

        // All the alarms cleared which was belongs to device1 and device2
        alarms = getAlarmsFor(0, 10);
        assertEquals(1, alarms.size());
    }

}
