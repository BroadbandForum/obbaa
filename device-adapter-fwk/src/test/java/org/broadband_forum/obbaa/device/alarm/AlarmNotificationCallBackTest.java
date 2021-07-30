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

package org.broadband_forum.obbaa.device.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.alarm.callback.AlarmNotificationCallBack;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmNotification;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmsDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBack;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.w3c.dom.Element;


public class AlarmNotificationCallBackTest {

    private final AlarmService m_alarmService = mock(AlarmService.class);
    private SchemaRegistry m_registry = null;
    private final NotificationContext m_notifContext = new NotificationContext();
    private Device m_device = null;
    private DeviceAdapter m_deviceAdapter = null;
    private DeviceAdapterId m_deviceAdapterId = new DeviceAdapterId("DPU", "1.0", "Standard", "BBF");
    private static final QName TYPE = QName.create(AlarmConstants.ALARM_NAMESPACE, "alarm-notification");
    SchemaRegistry m_mountRegistry = mock(SchemaRegistryImpl.class);
    SchemaRegistry m_dmSchemaRegistry = mock(SchemaRegistryImpl.class);
    DateTime m_date = new DateTime(System.currentTimeMillis());

    /**
     * <notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
     * <eventTime>2016-08-31T12:03:43+05:30</eventTime>
     * <alarms:alarm-notification xmlns:alarms="urn:ietf:params:xml:ns:yang:ietf-alarms">
     * <alarms:resource xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>
     * <alarms:alarm-type-id xmlns:dpu-al="urn:broadband-forum-org:yang:dpu-alarm-types">dpu-al:xdsl-fe-los</alarms:alarm-type-id>
     * <alarms:alarm-type-qualifier/>
     * <alarms:time>2016-11-07T17:45:02.004+05:30</alarms:time>
     * <alarms:perceived-severity>major</alarms:perceived-severity>
     * <alarms:alarm-text>dummyText</alarms:alarm-text>
     * </alarms:alarm-notification>
     * </notification>
     *
     * @throws SchemaBuildException
     * @throws URISyntaxException
     **/

    @Before
    public void setup() throws SchemaBuildException, URISyntaxException {
        List<String> yangs = new ArrayList<>();
        yangs.add("/coreyangs/ietf-yang-types.yang");
        yangs.add("/coreyangs/ietf-inet-types.yang");
        yangs.add("/coreyangs/ietf-netconf-acm.yang");
        yangs.add("/coreyangs/ietf-crypto-types.yang");
        yangs.add("/coreyangs/ietf-yang-library@2016-06-21.yang");
        yangs.add("/coreyangs/ietf-yang-schema-mount.yang");
        yangs.add("/coreyangs/ietf-tcp-common.yang");
        yangs.add("/coreyangs/ietf-tcp-client.yang");
        yangs.add("/coreyangs/ietf-tcp-server.yang");
        yangs.add("/coreyangs/ietf-datastores@2017-08-17.yang");
        yangs.add("/coreyangs/bbf-device-types.yang");
        yangs.add("/coreyangs/bbf-network-function-types.yang");
        yangs.add("/coreyangs/bbf-xpon-types.yang");
        yangs.add("/coreyangs/bbf-yang-types.yang");
        yangs.add("/coreyangs/bbf-vomci-types.yang");
        yangs.add("/coreyangs/bbf-grpc-client.yang");
        yangs.add("/coreyangs/bbf-kafka-agent.yang");
        yangs.add("/coreyangs/bbf-network-function-client.yang");
        yangs.add("/coreyangs/bbf-network-function-server.yang");
        yangs.add("/coreyangs/bbf-obbaa-network-manager.yang");
        m_device = mock(Device.class);
        m_deviceAdapter = mock(DeviceAdapter.class);
        m_registry = SchemaRegistryImpl.buildSchemaRegistry(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());

        Module module = mock(Module.class);
        QNameModule qNameModule = QNameModule.create(new URI("urn:broadband-forum-org:yang:dpu-alarm-types"), Revision.of("2017-10-10"));
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(m_mountRegistry.getModuleByNamespace("urn:broadband-forum-org:yang:dpu-alarm-types")).thenReturn(module);
        when(m_deviceAdapter.getDeviceAdapterId()).thenReturn(m_deviceAdapterId);
        when(m_device.getDeviceName()).thenReturn("R1.S1.LT1.PON1.ONT1");
        m_notifContext.put(Device.class.getSimpleName(), m_device);
        m_notifContext.put(DeviceAdapterId.class.getSimpleName(), m_deviceAdapterId);
    }

    @After
    public void tearDown() {
        RequestScope.resetScope();
    }

    @Test
    public void testRaiseAlarmWithSchemaMount() throws NetconfMessageBuilderException, SchemaBuildException, URISyntaxException {
        testAlarmInfoWithSeverity("major");
    }

    @Test
    public void testClearAlarmWithSchemaMount() throws NetconfMessageBuilderException, SchemaBuildException, URISyntaxException {
        testAlarmInfoWithSeverity("cleared");
    }


    @Test
    public void testExceptionIsThrownWhenDeviceTypeVersionIsNull() throws NetconfMessageBuilderException {
        SBINotification sbiNotification = new SBINotification(m_registry);
        String alarmNotification = "<baa-network-manager>"
                + "</baa-network-manager>";
        Element notificationElement = DocumentUtils.stringToDocument(alarmNotification).getDocumentElement();
        sbiNotification.setNotificationElement(notificationElement);
        NotificationCallBack callback = new AlarmNotificationCallBack(m_mountRegistry, m_alarmService, AlarmConstants.ALARM_NAMESPACE, m_registry);
        try {
            callback.onNotificationReceived(sbiNotification, m_notifContext, m_date);
            fail("Should have thrown an exception when device details is missing");
        } catch (Exception e) {
            assertEquals("Exception while processing alarm", e.getMessage());
        }
    }

    private Element getNotificationElement(String severity) throws NetconfMessageBuilderException {
        String alarmNotification = "<alarms:alarm-notification xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                + "<alarms:resource xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                + "<alarms:alarm-type-id xmlns:dpu-al=\"urn:broadband-forum-org:yang:dpu-alarm-types\">dpu-al:xdsl-fe-los</alarms:alarm-type-id>"
                + "<alarms:time>2016-11-07T17:45:02.004+05:30</alarms:time>"
                + "<alarms:alarm-type-qualifier/>"
                + "<alarms:perceived-severity>" + severity + "</alarms:perceived-severity>"
                + "<alarms:alarm-text>dummyText</alarms:alarm-text>"
                + "</alarms:alarm-notification>";
        Element notificationElement = DocumentUtils.stringToDocument(alarmNotification).getDocumentElement();
        return notificationElement;
    }

    private void testAlarmInfoWithSeverity(String severity) throws NetconfMessageBuilderException {
        Element notificationElement = getNotificationElement(severity);
        AlarmSeverity alarmSeverity = severity.equals("cleared") ? AlarmSeverity.CLEAR : AlarmSeverity.MAJOR;
        SBINotification sbiNotification = new SBINotification(m_registry);
        sbiNotification.setNotificationElement(notificationElement);
        NotificationCallBack callback = new AlarmNotificationCallBack(m_mountRegistry, m_alarmService, AlarmConstants.ALARM_NAMESPACE, m_registry);
        callback.onNotificationReceived(sbiNotification, m_notifContext, m_date);
        ArgumentCaptor<AlarmInfo> capture = ArgumentCaptor.forClass(AlarmInfo.class);
        if (severity.equals("cleared")) {
            verify(m_alarmService).clearAlarm(capture.capture());
        } else {
            verify(m_alarmService).raiseAlarm(capture.capture());
        }
        AlarmInfo info = capture.getValue();
        assertEquals("{dpu-al}(urn:broadband-forum-org:yang:dpu-alarm-types?revision=2017-10-10)xdsl-fe-los", info.getAlarmTypeId());
        assertEquals("ModelNodeId[/container=network-manager/container=managed-devices/container=device/name='R1.S1.LT1.PON1.ONT1'/container=root/container=interfaces/container=interface/name='xdsl-line:1/1/1/1']", info.getSourceObject().toString());
        assertEquals(alarmSeverity, info.getSeverity());
        assertEquals("R1.S1.LT1.PON1.ONT1", info.getDeviceName());
        assertEquals("dummyText", info.getAlarmText());
        assertEquals("", info.getAlarmTypeQualifier());
    }

    private class SBINotification extends NetconfNotification {

        private static final String SBI_IETF_ALARM_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-alarms";

        private AlarmNotification m_alarm;

        private SchemaRegistry m_schemaRegistry;

        private SBINotification(SchemaRegistry schemaRegistry) {
            m_schemaRegistry = schemaRegistry;
        }

        public void setNotificationElement(Element notificationElement) {
            super.setNotificationElement(notificationElement);
        }

        @Override
        public Element getNotificationElement() {
            Element alarmNotificationElement = super.getNotificationElement();
            if (alarmNotificationElement == null) {
                alarmNotificationElement = getAlarmNotificationElement();
                setNotificationElement(alarmNotificationElement);
            }
            return alarmNotificationElement;
        }

        private Element getAlarmNotificationElement() {
            try {
                AlarmsDocumentTransformer transformer = new AlarmsDocumentTransformer(m_schemaRegistry, null);
                return transformer.buildAlarmNotification(m_alarm, SBI_IETF_ALARM_NAMESPACE);
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException("Error while getting alarm notification element ", e);
            }
        }

        @Override
        public QName getType() {
            return TYPE;
        }

        @Override
        public String toString() {
            return "SBINotification [alarm=" + m_alarm + "]";
        }

    }
}
