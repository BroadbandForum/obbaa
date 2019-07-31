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

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.registrator.api.StandardAdapterModelRegistrator;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;

public class StandardModelRegistrator {

    private Set<StandardAdapterModelRegistrator> m_standardModelRegistrators = new HashSet<>();

    public StandardModelRegistrator(NotificationService notificationService, AlarmService alarmService,
                                    SchemaRegistry dmSchemaRegistry) {
        AlarmManagementCallbackRegistrator alarmCallbackRegistrator = new AlarmManagementCallbackRegistrator(
                notificationService, alarmService, dmSchemaRegistry);
        OnuStateChangeCallbackRegistrator onuStateChangeCallbackRegistrator = new OnuStateChangeCallbackRegistrator(notificationService);
        m_standardModelRegistrators.add(alarmCallbackRegistrator);
        m_standardModelRegistrators.add(onuStateChangeCallbackRegistrator);
    }

    public void onDeployed(DeviceAdapter adapter, AdapterContext adapterContext) {
        for (StandardAdapterModelRegistrator registrator : m_standardModelRegistrators) {
            registrator.onDeployed(adapter, adapterContext);
        }
    }

    public void onUndeployed(DeviceAdapter adapter, AdapterContext adapterContext) {
        for (StandardAdapterModelRegistrator registrator : m_standardModelRegistrators) {
            registrator.onUndeployed(adapter, adapterContext);
        }
    }

    public Set<StandardAdapterModelRegistrator> getStandardPlugRegistrators() {
        return m_standardModelRegistrators;
    }

    public void setOldIdentities(Set<IdentitySchemaNode> oldIdentities) {
        for (StandardAdapterModelRegistrator registrator : m_standardModelRegistrators) {
            if (registrator instanceof AlarmManagementCallbackRegistrator) {
                ((AlarmManagementCallbackRegistrator) registrator).setOldIdentities(oldIdentities);
            }
        }
    }

}
