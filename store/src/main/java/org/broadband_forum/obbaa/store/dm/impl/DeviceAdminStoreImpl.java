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

package org.broadband_forum.obbaa.store.dm.impl;

import java.util.Objects;
import java.util.Set;

import org.broadband_forum.obbaa.store.AbstractStore;
import org.broadband_forum.obbaa.store.dm.DeviceAdminStore;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceAdminStoreImpl extends AbstractStore<String, DeviceInfo> implements DeviceAdminStore {
    public DeviceAdminStoreImpl(String mapDbFilePath) {
        super(mapDbFilePath, "devices", DeviceInfo.class.getSimpleName());
    }

    @Override
    public DeviceInfo getCallHomeDeviceWithDuid(String duid) {
        Objects.requireNonNull(duid);
        Set<DeviceInfo> allDevices = getAllEntries();
        for (DeviceInfo deviceInfo : allDevices) {
            if (deviceInfo.isCallHome()) {
                if (duid.equals(deviceInfo.getDeviceCallHomeInfo().getDuid())) {
                    return deviceInfo;
                }
            }
        }
        return null;
    }
}
