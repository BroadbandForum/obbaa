package org.broadband_forum.obbaa.device.registrator.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.alarm.callback.OnuStateChangeCallback;
import org.broadband_forum.obbaa.device.registrator.api.StandardAdapterModelRegistrator;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationApplicableCheck;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBack;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnuStateChangeCallbackRegistrator implements StandardAdapterModelRegistrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnuStateChangeCallbackRegistrator.class);
    public static final String ONU_STATE_CHANGE_NS = "urn:bbf:yang:bbf-xpon-onu-states";
    public static final String ONU_STATE_CHANGE_NOTIFICATION = "onu-state-change";
    private NotificationService m_notificationService;
    private static final String BBF_ONU_STATE_CAP_FORMAT = "%s?module=%s&revision=%s";
    private static final String BBF_ONU_STATE_CAP_FORMAT_NO_REV = "%s?module=%s";
    private Map<DeviceAdapterId, List<NotificationCallBackInfo>> m_callbackMapPerAdapter = new ConcurrentHashMap<>();

    public OnuStateChangeCallbackRegistrator(NotificationService notificationService) {
        m_notificationService = notificationService;
    }

    @Override
    public void onDeployed(DeviceAdapter adapter, AdapterContext adapterContext) {
        DeviceAdapterId deviceAdapterId = adapter.getDeviceAdapterId();
        LOGGER.debug("Invoked deploy to register Onu State change callback for deviceAdapterId: " + deviceAdapterId);
        Module module = getMatchingNs(adapterContext);
        if (module != null) {
            String moduleNS = module.getNamespace().toString();
            String moduleName = module.getName();
            Optional<Revision> dpuModuleRevision = module.getQNameModule().getRevision();
            LOGGER.debug(String.format("Registering onuStateChangeCallBack if not already registered while adapter %s deployment",
                    deviceAdapterId));
            if (m_callbackMapPerAdapter.get(deviceAdapterId) == null) {
                List<NotificationCallBackInfo> notificationCallBackInfos = createNotificationCallBackInfo(moduleNS,
                        moduleName, dpuModuleRevision, adapter, adapterContext);
                m_notificationService.registerCallBack(notificationCallBackInfos);
                LOGGER.info(String.format("notification callback is registered for the notification namespace %s for adapter %s",
                        moduleNS, deviceAdapterId));
            }
        } else {
            LOGGER.warn("No matching bbf-xpon-onu-states module found for adapter: " + deviceAdapterId);
        }
    }

    private Module getMatchingNs(AdapterContext adapterContext) {
        Set<String> namespaceList = new HashSet<>();
        namespaceList.add(ONU_STATE_CHANGE_NS);
        for (String namespace : namespaceList) {
            Module module = adapterContext.getSchemaRegistry().getModuleByNamespace(namespace);
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    @Override
    public void onUndeployed(DeviceAdapter adapter, AdapterContext adapterContext) {
        List<NotificationCallBackInfo> adapterCallBacks = m_callbackMapPerAdapter.get(adapter.getDeviceAdapterId());
        if (adapterCallBacks != null) {
            for (NotificationCallBackInfo notificationCallBackInfo : adapterCallBacks) {
                m_notificationService.unregisterCallBackInfo(notificationCallBackInfo);
                LOGGER.info("notification callback/s is unregistered for notification type names %s for deviceAdatperId %s ",
                        getOnuStateChangeNotificationQName(ONU_STATE_CHANGE_NS), adapter.getDeviceAdapterId());
            }
        }
        m_callbackMapPerAdapter.remove(adapter.getDeviceAdapterId());
    }

    private List<NotificationCallBackInfo> createNotificationCallBackInfo(String moduleNs, String moduleName,
                                                                          Optional<Revision> moduleRevision, DeviceAdapter adapter,
                                                                          AdapterContext adapterContext) {
        NotificationCallBackInfo cbInfo = new NotificationCallBackInfo();

        //This check is used to verify that whether the NotificationCallBack is applicable for this plug or not
        NotificationApplicableCheck notificationApplicableCheck = new NotificationApplicableCheck() {
            @Override
            public boolean isApplicable(NotificationContext context) {
                DeviceAdapterId deviceAdapterId = adapter.getDeviceAdapterId();
                return (context.get(DeviceAdapterId.class.getSimpleName()).equals(deviceAdapterId));
            }
        };

        cbInfo.setNotificationApplicableCheck(notificationApplicableCheck);

        Set<String> capabilitiesString = new HashSet<>();
        String capsString = getModuleCapabilityString(moduleNs, moduleName, moduleRevision);
        capabilitiesString.add(capsString);
        cbInfo.setCapabilities(capabilitiesString);
        Set<QName> notificationTypes = new HashSet<>();
        NotificationCallBack callback = new OnuStateChangeCallback(m_notificationService, ONU_STATE_CHANGE_NS);
        notificationTypes.add(getOnuStateChangeNotificationQName(moduleNs));
        cbInfo.setNotificationTypes(notificationTypes);
        cbInfo.setCallBack(callback);
        List<NotificationCallBackInfo> notificationCBInfoList = new ArrayList<>();
        notificationCBInfoList.add(cbInfo);
        m_callbackMapPerAdapter.put(adapter.getDeviceAdapterId(), notificationCBInfoList);
        return notificationCBInfoList;
    }

    private QName getOnuStateChangeNotificationQName(String moduleNs) {
        QName onuStateChangeNotificationQName = QName.create(moduleNs, ONU_STATE_CHANGE_NOTIFICATION);
        return onuStateChangeNotificationQName;
    }

    private String getModuleCapabilityString(String moduleNs, String moduleName, Optional<Revision> revision) {
        String capsString;
        if (revision.isPresent()) {
            capsString = String.format(BBF_ONU_STATE_CAP_FORMAT, moduleNs, moduleName, revision.get());
        } else {
            capsString = String.format(BBF_ONU_STATE_CAP_FORMAT_NO_REV, moduleNs, moduleName);
        }
        return capsString;
    }
}
