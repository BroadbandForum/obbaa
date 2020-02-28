package org.broadband_forum.obbaa.device.registrator.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;

public class AlarmManagementCallbackRegistratorTest {

    private NotificationService m_notificationService = mock(NotificationService.class);
    private AlarmService m_alarmService = mock(AlarmService.class);
    private AdapterManager m_adapterManager = mock(AdapterManager.class);
    private DeviceAdapter m_adapter = mock(DeviceAdapter.class);
    private DeviceAdapterId m_deviceAdapterId = mock(DeviceAdapterId.class);
    private AdapterContext m_adapterContext = mock(AdapterContext.class);

    @After
    public void tearDown() {
        RequestScope.resetScope();
    }

    @Test
    public void testOnDeploy() throws URISyntaxException {
        SchemaRegistryImpl mountRegistry = mock(SchemaRegistryImpl.class);
        Module module = mock(Module.class);
        String alarmNS = "urn:ietf:params:xml:ns:yang:ietf-alarms";
        String alarmNotification = "alarm-notification";
        String moduleCapabilityString = "urn:ietf:params:xml:ns:yang:ietf-alarms?module=ietf-alarms&revision=2019-09-11";
        QName alarmNotificationQName = QName.create(alarmNS, alarmNotification);
        QNameModule qNameModule = QNameModule.create(new URI("urn:ietf:params:xml:ns:yang:ietf-alarms"), Revision.of("2019-09-11"));
        when(module.getNamespace()).thenReturn(new URI(alarmNS));
        when(module.getName()).thenReturn("ietf-alarms");
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(mountRegistry.getModuleByNamespace(alarmNS)).thenReturn(module);
        when(m_adapter.getDeviceAdapterId()).thenReturn(m_deviceAdapterId);
        when(m_adapterManager.getAdapterContext(m_deviceAdapterId)).thenReturn(m_adapterContext);
        when(m_notificationService.isNotificationCallbackRegistered(alarmNotificationQName, moduleCapabilityString)).thenReturn(false);
        when(m_adapterContext.getSchemaRegistry()).thenReturn(mountRegistry);
        AlarmManagementCallbackRegistrator registrator = new AlarmManagementCallbackRegistrator(m_notificationService, m_alarmService, null);

        @SuppressWarnings({"rawtypes", "unchecked"})
        Class<List<NotificationCallBackInfo>> listClass = (Class<List<NotificationCallBackInfo>>) (Class) List.class;
        ArgumentCaptor<List<NotificationCallBackInfo>> capture = ArgumentCaptor.forClass(listClass);
        registrator.onDeployed(m_adapter, m_adapterContext);
        verify(m_notificationService).registerCallBack(capture.capture());
        List<NotificationCallBackInfo> callbacks = capture.getValue();
        assertEquals(alarmNotificationQName, callbacks.get(0).getNotificationTypes().iterator().next());
    }
}
