package org.broadband_forum.obbaa.pma.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.pma.NonNCNotificationHandler;
import org.broadband_forum.obbaa.pma.PmaRegistry;

public class NonNCNotificationHandlerImpl implements NonNCNotificationHandler {
    private static final Logger LOGGER = Logger.getLogger(NonNCNotificationHandlerImpl.class);
    private final NotificationService m_notificationService;
    private final AdapterManager m_adapterManager;
    private final PmaRegistry m_pmaRegistry;
    private Map<String, DeviceNotificationListener> m_deviceNotificationListenerMap;
    private DeviceManager m_deviceManager;

    public NonNCNotificationHandlerImpl(NotificationService notificationService, AdapterManager adapterManager,
                                        PmaRegistry pmaRegistry, DeviceManager deviceManager) {
        this.m_notificationService = notificationService;
        this.m_adapterManager = adapterManager;
        this.m_pmaRegistry = pmaRegistry;
        this.m_deviceManager = deviceManager;
        this.m_deviceNotificationListenerMap = new HashMap<>();
    }

    public void handleNotification(String ip, String port, Notification notification) {
        Device device = getDeviceFromIpAndPort(ip, port);
        if (device != null) {
            DeviceNotificationListener deviceListener;
            DeviceAdapterId deviceAdapterId = new DeviceAdapterId(device.getDeviceManagement().getDeviceType(),
                    device.getDeviceManagement().getDeviceInterfaceVersion(), device.getDeviceManagement().getDeviceModel(),
                    device.getDeviceManagement().getDeviceVendor());
            String deviceName = device.getDeviceName();
            if (m_deviceNotificationListenerMap.get(deviceName) != null) {
                deviceListener = m_deviceNotificationListenerMap.get(deviceName);
            } else {
                AdapterContext adapterContext = AdapterUtils.getAdapterContext(device, m_adapterManager);
                deviceListener = new DeviceNotificationListener(device, deviceAdapterId, m_notificationService,
                        adapterContext, m_pmaRegistry);
                m_deviceNotificationListenerMap.put(deviceName, deviceListener);
            }
            deviceListener.notificationReceived(notification);
        } else {
            LOGGER.info(String.format("No device found corresponding to ip: %s and port: %s", ip, port));
        }
    }

    private Device getDeviceFromIpAndPort(String ip, String port) {
        List<Device> devices = m_deviceManager.getAllDevices();
        for (Device device : devices) {
            if (!device.isCallhome()) {
                if ((device.getDeviceManagement().getDeviceConnection().getPasswordAuth().getAuthentication().getAddress().equals(ip))
                        && (device.getDeviceManagement().getDeviceConnection().getPasswordAuth().getAuthentication()
                        .getManagementPort().equals(port))) {
                    return device;
                }
            }
        }
        return null;
    }

    //Only for UT
    protected void addListener(String deviceName, DeviceNotificationListener listener) {
        m_deviceNotificationListenerMap.put(deviceName, listener);
    }
}
