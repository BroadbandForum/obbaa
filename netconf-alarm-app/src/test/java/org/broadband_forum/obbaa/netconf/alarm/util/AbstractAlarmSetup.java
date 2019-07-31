package org.broadband_forum.obbaa.netconf.alarm.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmTestConstants;
import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;
import org.junit.Before;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@SuppressWarnings("deprecation")
public class AbstractAlarmSetup {

    protected static final String ALARM_TYPE_ID = "{baa-network-manager}(http://www.example.com/network-manager/managed-devices)device-alarm";
    protected static final String ALARM_TYPE_QUALIFIER = "{network-manager}(http://www.example.com/network-manager/managed-devices?revision=2016-07-04)fast-ftu-r-loss-of-margin";
    protected static final String ALARM_TEXT = "A Critical Alarm";
    protected static final String UPDATE_ALARM_TEXT = "A Update Alarm";
    protected static final ModelNodeId MODEL_NODE_ID = new ModelNodeId()
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "managed-devices")
            .addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    protected static final ModelNodeId MODEL_NODE_ID2 = new ModelNodeId()
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "managed-devices")
            .addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT2");
    protected static final ModelNodeId DEVICE_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("device-id", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    protected static final String DEVICE_ID = "R1.S1.LT1.P1.ONT1";
    protected static final String DEVICE_ID2 = "R1.S1.LT1.P1.ONT2";
    protected static final String MOUNT_KEY = "G.FAST|1.0";
    protected static final String RESOURCETYPE_STRING = "ams:mobject-manager/prefix=XDSL Port/ne=myNE/agent=IACM/relative=R1.S1.LT1.P1";
    protected static final String RESOURCETYPE_STRING_2 = "ams:mobject-manager/prefix=XDSL Port/ne=myNE/agent=IACM/relative=R1.S1.LT1.P2";
    protected static final String IETF_INTERFACE = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String REVISION = "2018-07-27";
    private static final QName NETWORK_MGR_QNAME = QName.create(AlarmTestConstants.BAA_NAMESPACE, REVISION, "network-manager");
    private static final QName DEVICE_QNAME = QName.create(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, REVISION, "device");
    private static final QName ROOT_QNAME = QName.create(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, REVISION, "root");
    protected static final SchemaPath DEVICE_SPECIFIC_DATA_PATH = SchemaPath.create(true, NETWORK_MGR_QNAME, DEVICE_QNAME, ROOT_QNAME);
    protected static PersistenceManagerUtil m_persistenceManager;
    protected SchemaRegistry m_schemaRegistry;
    protected ModelNodeDataStoreManager m_dsm;
    protected List<String> m_ignoreElements;

    public static List<Alarm> createAlarms(Timestamp timestamp, int count) {
        // create & persist alarm
        List<Alarm> alarmsList = new ArrayList<>();
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();

        for (int i = 0; i < count; i++) {
            AlarmInfo info = buildAlarmInfo(i + 1);
            Alarm alarm = AlarmUtil.convertToAlarmEntity(info, generateDBID(i + 1));
            alarm.setRaisedTime(timestamp);
            manager.create(alarm);
            alarmsList.add(alarm);
        }
        manager.commitTransaction();
        return alarmsList;
    }

    public static void destroyAlarms() {
        EntityDataStoreManager manager = m_persistenceManager.getEntityDataStoreManager();
        manager.beginTransaction();
        manager.deleteAll(Alarm.class);
        manager.commitTransaction();
    }

    public static String generateDBID(int count) {
        return "/network-manager/managed-devices[name='OLT-2345']/device[name='R1.S1.LT1.P1.ONT" + count + "']";
    }

    public static AlarmInfo buildAlarmInfo() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, MODEL_NODE_ID, time, AlarmSeverity.CRITICAL,
                ALARM_TEXT, DEVICE_ID);
        return alarmInfo;
    }

    public static AlarmInfo buildAlarmInfoForDevice2() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, MODEL_NODE_ID2, time, AlarmSeverity.CRITICAL,
                ALARM_TEXT, DEVICE_ID2);
        return alarmInfo;
    }

    public static AlarmInfo buildAlarmInfoWithResourceTypeString() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, null, time, AlarmSeverity.CRITICAL,
                ALARM_TEXT, DEVICE_ID, RESOURCETYPE_STRING);
        return alarmInfo;
    }

    public static AlarmInfo buildAlarmInfoWithResourceTypeString_Device2() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, null, time, AlarmSeverity.CRITICAL,
                ALARM_TEXT, DEVICE_ID2, RESOURCETYPE_STRING_2);
        return alarmInfo;
    }

    /**
     * count should be used to generate the Alarm object with unique device ID
     */
    public static AlarmInfo buildAlarmInfo(int count) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, MODEL_NODE_ID, time, AlarmSeverity.CRITICAL,
                ALARM_TEXT, "R1.S1.LT1.P1.ONT" + count);
        return alarmInfo;
    }

    public static AlarmInfo buildAlarmInfoWithoutAlarmText() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, MODEL_NODE_ID, time, AlarmSeverity.CRITICAL,
                null, DEVICE_ID);
        return alarmInfo;
    }

    public static AlarmInfo buildUpdateAlarmInfo() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, MODEL_NODE_ID, time, AlarmSeverity.MAJOR,
                UPDATE_ALARM_TEXT, DEVICE_ID);
        return alarmInfo;
    }

    public static AlarmInfo buildClearedAlarmInfo() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        AlarmInfo alarmInfo = new AlarmInfo(ALARM_TYPE_ID, ALARM_TYPE_QUALIFIER, MODEL_NODE_ID, time, AlarmSeverity.CLEAR,
                ALARM_TEXT, DEVICE_ID);
        return alarmInfo;
    }

    @Before
    public void setup() {
        m_schemaRegistry = mock(SchemaRegistry.class);
        m_dsm = mock(ModelNodeDataStoreManager.class);
        EMFactory emf = new JPAEntityManagerFactory("netconf-alarm-app");
        m_persistenceManager = new ThreadLocalPersistenceManagerUtil(emf);
        when(m_schemaRegistry.getNamespaceURI("baa")).thenReturn(AlarmTestConstants.BAA_NAMESPACE);
        when(m_schemaRegistry.getNamespaceURI("baa-network-manager")).thenReturn(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE);
        when(m_schemaRegistry.getNamespaceURI("alarms")).thenReturn(AlarmConstants.ALARM_NAMESPACE);

        Module baaModule = mock(Module.class);
        when(baaModule.getPrefix()).thenReturn("baa");
        when(baaModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn(baaModule);
        Module deviceHolderModule = mock(Module.class);
        when(deviceHolderModule.getPrefix()).thenReturn("baa-network-manager");
        when(deviceHolderModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(deviceHolderModule);

        when(m_schemaRegistry.getNamespaceOfModule("baa")).thenReturn(AlarmTestConstants.BAA_NAMESPACE);
        when(m_schemaRegistry.getNamespaceOfModule("baa-network-manager")).thenReturn(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE);
        when(m_schemaRegistry.getPrefix(AlarmTestConstants.BAA_NAMESPACE)).thenReturn("baa");
        when(m_schemaRegistry.getPrefix(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn("baa-network-manager");
        when(m_schemaRegistry.getPrefix(AlarmConstants.ALARM_NAMESPACE)).thenReturn("alarms");


        m_ignoreElements = new ArrayList<String>();
        m_ignoreElements.add("baa-network-manager:time");
        m_ignoreElements.add("alarms:time");
    }

}
