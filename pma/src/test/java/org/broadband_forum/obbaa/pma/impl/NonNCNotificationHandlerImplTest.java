package org.broadband_forum.obbaa.pma.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Authentication;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceConnection;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.PasswordAuth;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.pma.DeviceNotificationListenerService;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NonNCNotificationHandlerImplTest {

    @Mock
    private NotificationService m_notificationService;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private PmaRegistry m_pmaRegistry;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private Device m_device;
    @Mock
    private DeviceNotificationListener m_deviceNotificationListener;
    @Mock
    private DeviceNotificationListenerService m_deviceNotificationClientService;
    private NonNCNotificationHandlerImpl m_nonNetconfDeviceAdapter;
    private String ip_address;
    private String port;
    private Notification notification;
    private DeviceMgmt m_deviceMgmt;

    private List<Device> m_devices = new ArrayList<>();
    private DeviceConnection m_deviceConnection = new DeviceConnection();
    private PasswordAuth m_passwordAuth = new PasswordAuth();
    private Authentication m_authentication = new Authentication();



    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ip_address = "192.0.0.1";
        port = "9008";
        notification = new NetconfNotification(DocumentUtils.stringToDocument(getNotificationString()));
        m_nonNetconfDeviceAdapter = new NonNCNotificationHandlerImpl(m_notificationService, m_adapterManager,
                m_pmaRegistry, m_deviceManager, m_deviceNotificationClientService);
        m_deviceMgmt = new DeviceMgmt();
        m_deviceMgmt.setDeviceType("OLT");
        m_deviceMgmt.setDeviceInterfaceVersion("1.0");
        m_deviceMgmt.setDeviceModel("example");
        m_deviceMgmt.setDeviceVendor("UT");
        m_deviceMgmt.setDeviceConnection(m_deviceConnection);
        m_deviceConnection.setConnectionModel("direct");
        m_deviceConnection.setPasswordAuth(m_passwordAuth);
        m_passwordAuth.setAuthentication(m_authentication);
        m_authentication.setAddress(ip_address);
        m_authentication.setManagementPort(port);
        m_device = new Device();
        m_device.setDeviceName("UT-device");
        m_devices.add(m_device);
        m_device.setDeviceManagement(m_deviceMgmt);
        when(m_deviceManager.getAllDevices()).thenReturn(m_devices);
    }

    @Test
    public void testHandleNotificationNewDevice() {
        Notification notification = mock(Notification.class);
        AdapterContext adapterContext = mock(AdapterContext.class);
        DeviceInterface deviceInterface = mock(DeviceInterface.class);
        when(m_adapterManager.getAdapterContext(any(DeviceAdapterId.class))).thenReturn(adapterContext);
        when(adapterContext.getDeviceInterface()).thenReturn(deviceInterface);
        when(m_deviceNotificationClientService.getDeviceNotificationClientListeners()).thenReturn(new ArrayList<>());
        m_nonNetconfDeviceAdapter.handleNotification(ip_address, port, notification);
        verify(notification).notificationToString();
    }

    @Test
    public void testHandleNotificationForExistingDevice() {
        m_nonNetconfDeviceAdapter.addListener("UT-device", m_deviceNotificationListener);
        m_nonNetconfDeviceAdapter.handleNotification(ip_address, port, notification);
        verify(m_deviceNotificationListener).notificationReceived(notification);
    }

    @Test
    public void testHandleNotificationForNonExistingDevice() {
        m_nonNetconfDeviceAdapter.addListener("UT-device", m_deviceNotificationListener);
        m_nonNetconfDeviceAdapter.handleNotification(ip_address, "9009", notification);
        verifyZeroInteractions(m_deviceNotificationListener);
    }

    private String getNotificationString() {
        String notificationStr = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "</notification>";
        return notificationStr;
    }

    @After
    public void tearDown() throws Exception {
        m_nonNetconfDeviceAdapter = null;
        m_deviceMgmt = null;
        m_device = null;
    }
}