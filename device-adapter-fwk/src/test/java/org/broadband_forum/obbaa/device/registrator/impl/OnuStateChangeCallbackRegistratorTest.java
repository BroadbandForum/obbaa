package org.broadband_forum.obbaa.device.registrator.impl;

import static org.broadband_forum.obbaa.device.registrator.impl.OnuStateChangeCallbackRegistrator.ONU_STATE_CHANGE_NOTIFICATION;
import static org.broadband_forum.obbaa.device.registrator.impl.OnuStateChangeCallbackRegistrator.ONU_STATE_CHANGE_NS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;

public class OnuStateChangeCallbackRegistratorTest {

    @Mock
    private NotificationService m_notificationService;
    private OnuStateChangeCallbackRegistrator m_onuCallbackRegistrator;
    @Mock
    private AdapterContext m_adapterContext;
    @Mock
    private DeviceAdapter m_adapter;
    @Mock
    private Module m_module;
    @Mock
    private SchemaRegistryImpl m_schemaRegistry;
    private QName m_notificationQName;
    @Mock
    private DeviceAdapterId m_deviceAdapterId;

    @Before
    public void setup() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        m_onuCallbackRegistrator = new OnuStateChangeCallbackRegistrator(m_notificationService);
        when(m_module.getNamespace()).thenReturn(new URI(ONU_STATE_CHANGE_NS));
        when(m_module.getName()).thenReturn("bbf-xpon-onu-states");
        QNameModule qNameModule = QNameModule.create(new URI(ONU_STATE_CHANGE_NS), Revision.of("2019-02-25"));
        when(m_module.getQNameModule()).thenReturn(qNameModule);
        m_notificationQName = QName.create(ONU_STATE_CHANGE_NS, ONU_STATE_CHANGE_NOTIFICATION);
        when(m_schemaRegistry.getModuleByNamespace(ONU_STATE_CHANGE_NS)).thenReturn(m_module);
        when(m_adapter.getDeviceAdapterId()).thenReturn(m_deviceAdapterId);
        when(m_adapterContext.getSchemaRegistry()).thenReturn(m_schemaRegistry);
    }

    @After
    public void teardown() {
        reset(m_notificationService);
        RequestScope.resetScope();
    }

    @Test
    public void testDeployAndUndeploy() {
        ArgumentCaptor<List> capture = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<NotificationCallBackInfo> callbackArgCaptor = ArgumentCaptor.forClass(NotificationCallBackInfo.class);
        m_onuCallbackRegistrator.onDeployed(m_adapter, m_adapterContext);
        verify(m_notificationService).registerCallBack(capture.capture());
        List<NotificationCallBackInfo> callbacks = capture.getValue();
        assertEquals(1, callbacks.size());
        assertEquals(m_notificationQName, callbacks.get(0).getNotificationTypes().iterator().next());

        m_onuCallbackRegistrator.onUndeployed(m_adapter, m_adapterContext);
        verify(m_notificationService).unregisterCallBackInfo(callbackArgCaptor.capture());
        assertEquals(m_notificationQName, callbackArgCaptor.getValue().getNotificationTypes().iterator().next());
    }

    @Test
    public void testDeployAndUndeployNoModuleInAdapter() {
        when(m_schemaRegistry.getModuleByNamespace(ONU_STATE_CHANGE_NS)).thenReturn(null);
        m_onuCallbackRegistrator.onDeployed(m_adapter, m_adapterContext);
        verifyZeroInteractions(m_notificationService);

        m_onuCallbackRegistrator.onUndeployed(m_adapter, m_adapterContext);
        verifyZeroInteractions(m_notificationService);
    }
}
