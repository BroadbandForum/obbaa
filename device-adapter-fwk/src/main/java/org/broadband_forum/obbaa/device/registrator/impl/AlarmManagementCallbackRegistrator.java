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
import org.broadband_forum.obbaa.device.alarm.callback.AlarmNotificationCallBack;
import org.broadband_forum.obbaa.device.registrator.api.StandardAdapterModelRegistrator;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationApplicableCheck;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBack;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmManagementCallbackRegistrator implements StandardAdapterModelRegistrator {
    private static final String IETF_ALARM_CAP_FORMAT = "%s?module=%s&revision=%s";
    private static final String IETF_ALARM_CAP_FORMAT_NO_REV = "%s?module=%s";
    private static final String ALARM_NOTIFICATION = "alarm-notification";
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmManagementCallbackRegistrator.class);
    private Map<DeviceAdapterId, List<NotificationCallBackInfo>> m_callbackMapPerAdapter = new ConcurrentHashMap<>();

    private final NotificationService m_notificationService;
    private AlarmService m_alarmService;
    private QName m_alarmNotificationQName;
    private Set<IdentitySchemaNode> m_oldIdentities;
    private SchemaRegistry m_dmSchemaRegistry;

    public AlarmManagementCallbackRegistrator(NotificationService notificationService, AlarmService alarmService,
                                              SchemaRegistry dmSchemaRegistry) {
        m_alarmService = alarmService;
        m_notificationService = notificationService;
        m_dmSchemaRegistry = dmSchemaRegistry;
    }

    public void onDeployed(DeviceAdapter adapter, AdapterContext adapterContext) {
        DeviceAdapterId deviceAdapterId = adapter.getDeviceAdapterId();
        LOGGER.debug("Invoked deploy to register alarm callback for deviceAdapterId: " + deviceAdapterId);
        Module module = getMatchedAlarmModule(adapterContext);
        if (module != null) {
            String alarmNS = module.getNamespace().toString();
            m_alarmNotificationQName = getAlarmNotificationQName(alarmNS);
            m_alarmService.updateAlarmNotificationQNameToAdapterTypeVersion(m_alarmNotificationQName,
                    deviceAdapterId.getType() + "." + deviceAdapterId.getInterfaceVersion() + "."
                            + deviceAdapterId.getModel() + "." + deviceAdapterId.getVendor());
            LOGGER.debug(String.format("Going to register alarmCallBack if not already registered during deployment of adapter %s ",
                    deviceAdapterId));
            String moduleName = module.getName();
            Optional<Revision> moduleRevision = module.getQNameModule().getRevision();
            if (m_callbackMapPerAdapter.get(deviceAdapterId) == null) {
                List<NotificationCallBackInfo> notificationCallBackInfos = createNotificationCallBackInfo(alarmNS,
                        moduleName, moduleRevision, adapter, adapterContext);
                m_notificationService.registerCallBack(notificationCallBackInfos);
                LOGGER.info(String.format("notification callback is registered for the notification namespace %s for adapter %s",
                        alarmNS, deviceAdapterId));
            }
        } else {
            LOGGER.error("No matching alarm module found for adapter: " + deviceAdapterId);
        }
    }

    public void onUndeployed(DeviceAdapter adapter, AdapterContext adapterContext) {
        // alarm and notification change support for schema mount changes will be covered in separate US
        if (m_alarmNotificationQName != null) {
            DeviceAdapterId deviceAdapterId = adapter.getDeviceAdapterId();
            Set<QName> notificationTypeNames = new HashSet<>();
            notificationTypeNames.add(m_alarmNotificationQName);
            LOGGER.info("Invoked undeploy to unregister alarm callback %s", notificationTypeNames);
            m_alarmService.removeAlarmNotificationQNameToAdapterTypeVersion(m_alarmNotificationQName,
                    deviceAdapterId.getType() + "." + deviceAdapterId.getInterfaceVersion() + "."
                            + deviceAdapterId.getModel() + "." + deviceAdapterId.getVendor());
            LOGGER.info("Registering alarmCallBack if not already registered while adapter %s deployment", deviceAdapterId);
            List<NotificationCallBackInfo> adapterCallbacks = m_callbackMapPerAdapter.get(deviceAdapterId);
            if (adapterCallbacks != null) {
                for (NotificationCallBackInfo notificationCallBackInfo : adapterCallbacks) {
                    m_notificationService.unregisterCallBackInfo(notificationCallBackInfo);
                    LOGGER.info("notification callback is unregistered for notification type names %s for deviceAdatperId %s ",
                            notificationTypeNames, deviceAdapterId);
                }
            }
            m_callbackMapPerAdapter.remove(deviceAdapterId);
        }
    }

    public void setOldIdentities(Set<IdentitySchemaNode> oldIdentities) {
        m_oldIdentities = oldIdentities;
    }


    private Module getMatchedAlarmModule(AdapterContext adapterContext) {
        Set<String> modifiedIETFAlarmNSList = new HashSet<>();
        modifiedIETFAlarmNSList.add(AlarmConstants.ALARM_NAMESPACE);
        for (String modifiedAlarmNS : modifiedIETFAlarmNSList) {
            Module module = adapterContext.getSchemaRegistry().getModuleByNamespace(modifiedAlarmNS);
            if (module != null) {
                return module;
            }
        }
        return null;
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
        NotificationCallBack callback = new AlarmNotificationCallBack(adapterContext.getSchemaRegistry(),
                m_alarmService, moduleNs, m_dmSchemaRegistry);
        notificationTypes.add(m_alarmNotificationQName);
        cbInfo.setNotificationTypes(notificationTypes);
        cbInfo.setCallBack(callback);
        List<NotificationCallBackInfo> notificationCBInfoList = new ArrayList<>();
        notificationCBInfoList.add(cbInfo);
        m_callbackMapPerAdapter.put(adapter.getDeviceAdapterId(), notificationCBInfoList);
        return notificationCBInfoList;
    }

    private QName getAlarmNotificationQName(String moduleNs) {
        QName alarmNotificationQName = QName.create(moduleNs, ALARM_NOTIFICATION);
        return alarmNotificationQName;
    }

    private String getModuleCapabilityString(String moduleNs, String moduleName, Optional<Revision> revision) {
        String capsString;
        if (revision.isPresent()) {
            capsString = String.format(IETF_ALARM_CAP_FORMAT, moduleNs, moduleName, revision.get());
        } else {
            capsString = String.format(IETF_ALARM_CAP_FORMAT_NO_REV, moduleNs, moduleName);
        }
        return capsString;
    }

}