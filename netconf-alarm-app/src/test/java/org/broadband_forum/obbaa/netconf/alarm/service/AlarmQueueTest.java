package org.broadband_forum.obbaa.netconf.alarm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChange;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChangeType;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlarmQueueTest {

    private static final String NS = "http://www.example.com/some.namespace";

    private static final ModelNodeId NODEID11 = new ModelNodeId("/device-holder=a/device=d1/childlevel=foo", NS);
    private static final ModelNodeId NODEID12 = new ModelNodeId("/device-holder=a/device=d1/childlevel=bar", NS);
    private static final ModelNodeId NODEID21 = new ModelNodeId("/device-holder=a/device=d2/childlevel=fom", NS);
    private static final ModelNodeId SUBTREE1 = new ModelNodeId("/device-holder=a/device=d1", NS);

    private static final String ALARMTYPE1 = "alarm-type-1";
    private static final String ALARMTYPE2 = "alarm-type-2";
    private static final String MOUNT_KEY = "G.FAST|1.0";

    @Mock
    private InternalAlarmService m_alarmService;

    @InjectMocks
    private AlarmQueue m_alarmQueue;

    @Test
    public void testAddAlarmChanges() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12));
        changes.add(createAlarmChange(AlarmChangeType.UPDATE, ALARMTYPE1, NODEID21));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE2, NODEID21));
        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        verify(m_alarmService).processChanges(changes);
    }

    @Test
    public void testAddEmptyAlarmChanges() {
        List<AlarmChange> changes = new ArrayList<>();
        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        verify(m_alarmService, never()).processChanges(anyListOf(AlarmChange.class));
    }

    @Test
    public void testNoAlarmChanges() {
        m_alarmQueue.clearQueue();

        verify(m_alarmService, never()).processChanges(anyListOf(AlarmChange.class));
    }

    @Test
    public void testQueueOverflow() {
        m_alarmQueue.configureMaxQueueSize("2");

        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12));
        changes.add(createAlarmChange(AlarmChangeType.UPDATE, ALARMTYPE1, NODEID21));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE2, NODEID21));
        m_alarmQueue.addAlarmChanges(changes);
        assertEquals(2, m_alarmQueue.getDroppedAlarms());
        assertEquals(2, m_alarmQueue.getAlarmQueueSize());

        m_alarmQueue.clearQueue();

        List<AlarmChange> changeSubset = changes.subList(0, 2);
        verify(m_alarmService).processChanges(changeSubset);
        assertEquals(2, m_alarmQueue.getDroppedAlarms());
        assertEquals(0, m_alarmQueue.getAlarmQueueSize());
    }

    @Test
    public void testCleanupForClearSubtree() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12));
        changes.add(createAlarmChange(AlarmChangeType.UPDATE, ALARMTYPE1, NODEID21));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE2, NODEID21));
        changes.add(AlarmChange.createForClearSubtree(SUBTREE1, MOUNT_KEY));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID12));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(2));
        expectedChanges.add(changes.get(3));
        expectedChanges.add(changes.get(4));
        expectedChanges.add(changes.get(5));
        expectedChanges.add(changes.get(6));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        verify(m_alarmService).processChanges(expectedChanges);
    }

    @Test
    public void testCleanupForTogglingAlarms() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE2, NODEID12));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(3));
        expectedChanges.add(changes.get(4));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        ArgumentCaptor<List> changesCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_alarmService).processChanges(changesCaptor.capture());

        List<AlarmChange> actualChanges = changesCaptor.getValue();
        assertEquals(expectedChanges, actualChanges);
        assertTrue(actualChanges.get(0).isToggling());
        assertTrue(actualChanges.get(1).isToggling());
    }

    @Test
    public void testCleanupForTogglingAlarmsMultipleClear() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(1));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        ArgumentCaptor<List> changesCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_alarmService).processChanges(changesCaptor.capture());

        List<AlarmChange> actualChanges = changesCaptor.getValue();
        assertEquals(expectedChanges, actualChanges);
        assertFalse(actualChanges.get(0).isToggling());
    }

    @Test
    public void testCleanupForTogglingAlarmsMultipleRaise() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(1));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        ArgumentCaptor<List> changesCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_alarmService).processChanges(changesCaptor.capture());

        List<AlarmChange> actualChanges = changesCaptor.getValue();
        assertEquals(expectedChanges, actualChanges);
        assertFalse(actualChanges.get(0).isToggling());
    }

    @Test
    public void testCleanupForTogglingAlarmsMultipleRaiseAndClear() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(3));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        ArgumentCaptor<List> changesCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_alarmService).processChanges(changesCaptor.capture());

        List<AlarmChange> actualChanges = changesCaptor.getValue();
        assertEquals(expectedChanges, actualChanges);
        assertTrue(actualChanges.get(0).isToggling());
    }

    @Test
    public void testCleanupForTogglingAlarmsMultipleClearAndRaise() {
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.CLEAR, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));
        changes.add(createAlarmChange(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(3));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        ArgumentCaptor<List> changesCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_alarmService).processChanges(changesCaptor.capture());

        List<AlarmChange> actualChanges = changesCaptor.getValue();
        assertEquals(expectedChanges, actualChanges);
        assertTrue(actualChanges.get(0).isToggling());
    }

    private AlarmChange createAlarmChange(AlarmChangeType type, String alarmType, ModelNodeId modelNodeId) {
        Timestamp ts = new Timestamp(new Date().getTime());
        AlarmInfo info = new AlarmInfo(alarmType, modelNodeId, ts, AlarmSeverity.MAJOR, "", null);
        return AlarmChange.createFromAlarmInfo(type, info);
    }

    @Test
    public void testCleanupForTogglingAlarmsWithSameAlarmTypeId() {

        /*
         * This UT will test the case when two or more alarms have same modelNodeId &
         * alarmTypeId but different alarmTypeQualifier
         *
         */
        List<AlarmChange> changes = new ArrayList<>();
        changes.add(
                createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11, "testQualifier2"));
        changes.add(
                createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE, ALARMTYPE1, NODEID11, "testQualifier3"));
        changes.add(
                createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12, "testQualifier2"));
        changes.add(
                createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE, ALARMTYPE2, NODEID12, "testQualifier3"));
        changes.add(createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE1, NODEID11,
                "testQualifier2"));
        changes.add(createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE1, NODEID11,
                "testQualifier3"));
        changes.add(createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE2, NODEID12,
                "testQualifier2"));
        changes.add(createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType.RAISE_OR_UPDATE, ALARMTYPE2, NODEID12,
                "testQualifier3"));

        List<AlarmChange> expectedChanges = new ArrayList<>();
        expectedChanges.add(changes.get(4));
        expectedChanges.add(changes.get(5));
        expectedChanges.add(changes.get(6));
        expectedChanges.add(changes.get(7));

        m_alarmQueue.addAlarmChanges(changes);

        m_alarmQueue.clearQueue();

        verify(m_alarmService).processChanges(expectedChanges);

    }

    @Test
    public void testConfigureMaxQueueSize_UsingDefaultValue() {
        String invalidQueueSize = "abc";
        int defaultQueueSize = 1000;

        m_alarmQueue.configureMaxQueueSize(invalidQueueSize);

        assertEquals(defaultQueueSize, m_alarmQueue.getMaxQueueSize());
    }

    private AlarmChange createAlarmChangeWithAlarmTypeQualifier(AlarmChangeType type, String alarmType,
                                                                ModelNodeId modelNodeId, String alarmTypeQualifier) {
        Timestamp ts = new Timestamp(new Date().getTime());
        AlarmInfo info = new AlarmInfo(alarmType, alarmTypeQualifier, modelNodeId, ts, AlarmSeverity.MAJOR, "", null);
        return AlarmChange.createFromAlarmInfo(type, info);
    }

}
