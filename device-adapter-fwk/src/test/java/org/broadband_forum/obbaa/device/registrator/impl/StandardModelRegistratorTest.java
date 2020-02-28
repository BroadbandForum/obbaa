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

package org.broadband_forum.obbaa.device.registrator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.alarm.service.AlarmQueue;
import org.broadband_forum.obbaa.netconf.alarm.service.AlarmServiceImpl;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class StandardModelRegistratorTest {

    private static final AlarmService m_alarmService = mock(AlarmService.class);
    private final SchemaRegistryImpl m_mountRegistry = mock(SchemaRegistryImpl.class);
    private SchemaRegistry m_registry;
    private static final NotificationService m_notificationService = mock(NotificationService.class);
    private static final DeviceAdapter m_deviceAdapter = mock(DeviceAdapter.class);
    private static final DeviceAdapter m_deviceAdapter1 = mock(DeviceAdapter.class);
    private StandardModelRegistrator m_standardModelRegistrator;
    private static final DeviceAdapterId m_deviceAdapterId = new DeviceAdapterId("DPU", "1.0", "Standard", "BBF");
    private YangTextSchemaSource m_ietfAlarmYang = YangParserUtil.getYangSource(StandardModelRegistratorTest.class.getResource("/yang/ietf-alarms1@2019-09-11.yang"));
    private YangTextSchemaSource m_ietfAlarm1Yang = YangParserUtil.getYangSource(StandardModelRegistratorTest.class.getResource("/yang/ietf-alarms4@2019-09-11.yang"));
    private YangTextSchemaSource m_ietfYangTypesYang = YangParserUtil.getYangSource(StandardModelRegistratorTest.class.getResource("/yang/ietf-yang-types.yang"));
    private YangTextSchemaSource m_ietfYangTypesCoreYang = YangParserUtil.getYangSource(StandardModelRegistratorTest.class.getResource("/coreyangs/ietf-yang-types.yang"));
    private YangTextSchemaSource m_ietfInetTypesYang = YangParserUtil.getYangSource(StandardModelRegistratorTest.class.getResource("/coreyangs/ietf-inet-types.yang"));
    private static final String IETF_ALARM_NS1 = "urn:ietf:params:xml:ns:yang:ietf-alarms";
    private static final String ALARM_NOTIFICATION = "alarm-notification";
    private static final String IETF_ALARM_MGMT_MODULE1 = "ietf-alarms1";
    private static final String IETF_ALARM_MGMT_MODULE4 = "ietf-alarms4";
    private String m_adapterTypeVersion;
    private AdapterContext m_adapterContext = mock(AdapterContext.class);

    @Before
    public void setup() throws Exception {
        RequestScope.resetScope();
        m_registry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        m_standardModelRegistrator = new StandardModelRegistrator(m_notificationService, m_alarmService, m_registry);
        when(m_deviceAdapter.getDeviceAdapterId()).thenReturn(m_deviceAdapterId);
        List<YangTextSchemaSource> yangFiles = getYangFiles();
        when(m_deviceAdapter.getModuleByteSources()).thenReturn(yangFiles);
        when(m_adapterContext.getSchemaRegistry()).thenReturn((SchemaRegistryImpl) m_registry);
        List<YangTextSchemaSource> yangFiles1 = getYangFiles();
        yangFiles1.add(m_ietfAlarm1Yang);
        when(m_deviceAdapter1.getModuleByteSources()).thenReturn(yangFiles1);
        Module module = mock(Module.class);
        when(module.getNamespace()).thenReturn(new URI(IETF_ALARM_NS1));
        when(module.getName()).thenReturn(IETF_ALARM_MGMT_MODULE1);
        QNameModule qNameModule = QNameModule.create(new URI(IETF_ALARM_NS1), Revision.of("2019-09-11"));
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(m_mountRegistry.getModuleByNamespace(IETF_ALARM_NS1)).thenReturn(module);

        Module module1 = mock(Module.class);
        when(module1.getNamespace()).thenReturn(new URI(IETF_ALARM_NS1));
        when(module1.getName()).thenReturn(IETF_ALARM_MGMT_MODULE4);
        QNameModule qNameModule1 = QNameModule.create(new URI(IETF_ALARM_NS1), Revision.of("2019-09-11"));
        when(module1.getQNameModule()).thenReturn(qNameModule1);
        when(m_mountRegistry.getModuleByNamespace(IETF_ALARM_NS1)).thenReturn(module1);
        m_adapterTypeVersion = m_deviceAdapterId.getType() + "." + m_deviceAdapterId.getInterfaceVersion() + "." + m_deviceAdapterId.getModel() + "." + m_deviceAdapterId.getVendor();
    }

    @After
    public void teardown() {
        reset(m_notificationService);
        RequestScope.resetScope();
    }

    private List<YangTextSchemaSource> getYangFiles() {
        List<YangTextSchemaSource> yangFiles = new ArrayList<>();
        yangFiles.add(m_ietfAlarmYang);
        yangFiles.add(m_ietfYangTypesYang);
        yangFiles.add(m_ietfYangTypesCoreYang);
        yangFiles.add(m_ietfInetTypesYang);
        return yangFiles;
    }

    @Test
    public void testInitialRegistratorCount() throws SchemaBuildException {
        assertEquals(2, m_standardModelRegistrator.getStandardPlugRegistrators().size());
    }

    @Test
    public void testOnDeployAndUndeploy() throws SchemaBuildException {
        m_registry.buildSchemaContext(getYangFiles(), Collections.emptySet(), Collections.emptyMap());
        m_standardModelRegistrator.onDeployed(m_deviceAdapter, m_adapterContext);
        ArgumentCaptor<List> infoCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_notificationService).registerCallBack(infoCaptor.capture());
        assertEquals(1, infoCaptor.getValue().size());
        NotificationCallBackInfo callbackInfo = (NotificationCallBackInfo) infoCaptor.getValue().get(0);
        QName bbfAlarmNotificationQName = QName.create(IETF_ALARM_NS1, ALARM_NOTIFICATION);
        Set<QName> notificationTypes = new HashSet<>();
        notificationTypes.add(bbfAlarmNotificationQName);
        assertEquals(notificationTypes, callbackInfo.getNotificationTypes());
        verify(m_alarmService).updateAlarmNotificationQNameToAdapterTypeVersion(bbfAlarmNotificationQName, m_adapterTypeVersion);
        List<NotificationCallBackInfo> notificationCallBackInfos = new ArrayList<>();
        notificationCallBackInfos.add(callbackInfo);
        when(m_notificationService.getNotificationCallBacks()).thenReturn(notificationCallBackInfos);
        m_standardModelRegistrator.onUndeployed(m_deviceAdapter, m_adapterContext);
        verify(m_notificationService).unregisterCallBackInfo(callbackInfo);
    }

    @Test
    public void testOnDeployAndUnDeployForUnknownModule() throws IOException, SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = getYangFiles();
        yangFiles.remove(m_ietfAlarmYang);
        when(m_mountRegistry.getModuleByNamespace(IETF_ALARM_NS1)).thenReturn(null);
        m_registry.buildSchemaContext(yangFiles, Collections.emptySet(), Collections.emptyMap());
        when(m_deviceAdapter.getModuleByteSources()).thenReturn(yangFiles);
        m_standardModelRegistrator.onDeployed(m_deviceAdapter, m_adapterContext);
        when(m_adapterContext.getSchemaRegistry()).thenReturn(m_mountRegistry);
        verify(m_notificationService, never()).registerCallBack(anyList());

        m_standardModelRegistrator.onUndeployed(m_deviceAdapter, m_adapterContext);
        verify(m_alarmService, never()).removeAlarmNotificationQNameToAdapterTypeVersion(null, m_adapterTypeVersion);
    }

    @Test
    public void testOnDeployForSameModuleNS() throws SchemaBuildException {
        Set<String> typeVersions = new HashSet<>();
        typeVersions.add("DPU.1.0.Standard.BBF");
        m_registry.buildSchemaContext(getYangFiles(), Collections.emptySet(), Collections.emptyMap());
        AlarmService alarmService = new AlarmServiceImpl(mock(PersistenceManagerUtil.class), mock(AlarmQueue.class));
        m_standardModelRegistrator = new StandardModelRegistrator(m_notificationService, alarmService, m_registry);
        m_standardModelRegistrator.onDeployed(m_deviceAdapter, m_adapterContext);

        ArgumentCaptor<List> infoCaptor = ArgumentCaptor.forClass(List.class);
        verify(m_notificationService, times(1)).registerCallBack(infoCaptor.capture());
        NotificationCallBackInfo callbackInfo1 = (NotificationCallBackInfo) infoCaptor.getValue().get(0);
        QName bbfAlarmNotificationQName = QName.create(IETF_ALARM_NS1, ALARM_NOTIFICATION);
        assertEquals(typeVersions, alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(bbfAlarmNotificationQName));

        List<YangTextSchemaSource> yangFiles = getYangFiles();
        m_registry.buildSchemaContext(yangFiles, Collections.emptySet(), Collections.emptyMap());
        when(m_notificationService.isNotificationCallbackRegistered(any(QName.class), anyString())).thenReturn(true);
        DeviceAdapterId deviceAdapterId = new DeviceAdapterId("DPU", "1.1", "Standard", "BBF");
        when(m_deviceAdapter1.getDeviceAdapterId()).thenReturn(deviceAdapterId);
        m_standardModelRegistrator.onDeployed(m_deviceAdapter1, m_adapterContext);
        verify(m_notificationService, times(2)).registerCallBack(infoCaptor.capture());
        NotificationCallBackInfo callbackInfo2 = (NotificationCallBackInfo) infoCaptor.getValue().get(0);
        typeVersions.add("DPU.1.1.Standard.BBF");
        assertEquals(typeVersions, alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(bbfAlarmNotificationQName));

        typeVersions.remove("DPU.1.0.Standard.BBF");
        Set<QName> notificationTypes = new HashSet<>();
        notificationTypes.add(bbfAlarmNotificationQName);
        List<NotificationCallBackInfo> notificationCallBackInfos = new ArrayList<>();
        notificationCallBackInfos.add(callbackInfo1);
        notificationCallBackInfos.add(callbackInfo2);
        when(m_notificationService.getNotificationCallBacks()).thenReturn(notificationCallBackInfos);
        m_standardModelRegistrator.onUndeployed(m_deviceAdapter, m_adapterContext);
        assertEquals(typeVersions, alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(bbfAlarmNotificationQName));
        verify(m_notificationService, times(1)).unregisterCallBackInfo(callbackInfo1);

        typeVersions.remove("DPU.1.1.Standard.BBF");
        m_standardModelRegistrator.onUndeployed(m_deviceAdapter1, m_adapterContext);
        assertEquals(typeVersions, alarmService.retrieveAdapterTypeVersionsFromAlarmNotificationQName(bbfAlarmNotificationQName));
        verify(m_notificationService, times(1)).unregisterCallBackInfo(callbackInfo2);
    }

    @Test
    public void testMultiplePlugDeploymentAndUndeployment() throws Exception {
        ArgumentCaptor<List> infoCaptor = ArgumentCaptor.forClass(List.class);
        NotificationContext context = new NotificationContext();

        // Here m_deviceAdapter refers to adapter 1 and m_deviceAdapter1 refers to adapter 2
        // deploy adapter 1
        m_registry.buildSchemaContext(getYangFiles(), Collections.emptySet(), Collections.emptyMap());
        AlarmService alarmService = new AlarmServiceImpl(mock(PersistenceManagerUtil.class), mock(AlarmQueue.class));
        m_standardModelRegistrator = new StandardModelRegistrator(m_notificationService, alarmService, m_registry);
        m_standardModelRegistrator.onDeployed(m_deviceAdapter, m_adapterContext);
        verify(m_notificationService, times(1)).registerCallBack(infoCaptor.capture());
        NotificationCallBackInfo callbackInfo1 = (NotificationCallBackInfo) infoCaptor.getValue().get(0);
        context.put(DeviceAdapterId.class.getSimpleName(), m_deviceAdapterId);
        assertTrue(callbackInfo1.getNotificationApplicableCheck().isApplicable(context));

        // deploy adapter 2
        List<YangTextSchemaSource> yangFiles = getYangFiles();
        //yangFiles.add(m_ietfAlarm1Yang);
        m_registry.buildSchemaContext(yangFiles, Collections.emptySet(), Collections.emptyMap());
        DeviceAdapterId deviceAdapterId = new DeviceAdapterId("DPU", "1.1", "Standard", "BBF");
        when(m_deviceAdapter1.getDeviceAdapterId()).thenReturn(deviceAdapterId);
        m_standardModelRegistrator.onDeployed(m_deviceAdapter1, m_adapterContext);
        verify(m_notificationService, times(2)).registerCallBack(infoCaptor.capture());
        NotificationCallBackInfo callbackInfo2 = (NotificationCallBackInfo) infoCaptor.getValue().get(0);
        // adapter 1 m_adapterTypeVersion should be not registered in callBackInfo of adapter 2
        assertFalse(callbackInfo2.getNotificationApplicableCheck().isApplicable(context));
        context.put(DeviceAdapterId.class.getSimpleName(), deviceAdapterId);
        assertTrue(callbackInfo2.getNotificationApplicableCheck().isApplicable(context));

        List<NotificationCallBackInfo> notificationCallBackInfos = new ArrayList<>();
        notificationCallBackInfos.add(callbackInfo1);
        notificationCallBackInfos.add(callbackInfo2);
        when(m_notificationService.getNotificationCallBacks()).thenReturn(notificationCallBackInfos);

        // undeploy adapter 1 : it should not unregister callBack for adapter 2
        m_standardModelRegistrator.onUndeployed(m_deviceAdapter, m_adapterContext);
        verify(m_notificationService, times(1)).unregisterCallBackInfo(callbackInfo1);
        verify(m_notificationService, never()).unregisterCallBackInfo(callbackInfo2);

        // undeploy adapter 2
        m_standardModelRegistrator.onUndeployed(m_deviceAdapter1, m_adapterContext);
        verify(m_notificationService, times(1)).unregisterCallBackInfo(callbackInfo2);
    }

    @Test
    public void testDeploymentOfSamePlugTwice() throws Exception {
        ArgumentCaptor<List> infoCaptor = ArgumentCaptor.forClass(List.class);
        NotificationContext context = new NotificationContext();

        // Here m_deviceAdapter refers to adapter 1
        // deploy adapter 1
        m_registry.buildSchemaContext(getYangFiles(), Collections.emptySet(), Collections.emptyMap());
        AlarmService alarmService = new AlarmServiceImpl(mock(PersistenceManagerUtil.class), mock(AlarmQueue.class));
        m_standardModelRegistrator = new StandardModelRegistrator(m_notificationService, alarmService, m_registry);
        m_standardModelRegistrator.onDeployed(m_deviceAdapter, m_adapterContext);
        verify(m_notificationService, times(1)).registerCallBack(infoCaptor.capture());
        NotificationCallBackInfo callbackInfo1 = (NotificationCallBackInfo) infoCaptor.getValue().get(0);
        context.put(DeviceAdapterId.class.getSimpleName(), m_deviceAdapterId);
        assertTrue(callbackInfo1.getNotificationApplicableCheck().isApplicable(context));
        List<NotificationCallBackInfo> notificationCallBackInfos = new ArrayList<>();
        notificationCallBackInfos.add(callbackInfo1);
        when(m_notificationService.getNotificationCallBacks()).thenReturn(notificationCallBackInfos);

        // deploy adapter 1 again
        List<YangTextSchemaSource> yangFiles = getYangFiles();
        //yangFiles.add(m_ietfAlarm1Yang);
        m_registry.buildSchemaContext(yangFiles, Collections.emptySet(), Collections.emptyMap());
        m_standardModelRegistrator.onDeployed(m_deviceAdapter, m_adapterContext);
        // registerCallBack should not be called again , count should remain 1
        // only
        verify(m_notificationService, times(1)).registerCallBack(infoCaptor.capture());
    }
}
