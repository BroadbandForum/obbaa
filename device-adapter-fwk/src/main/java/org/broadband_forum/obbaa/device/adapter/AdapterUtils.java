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

package org.broadband_forum.obbaa.device.adapter;

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.BBF;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DPU;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.OLT;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STANDARD;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STD_ADAPTER_VERSION;

import org.broadband_forum.obbaa.device.adapter.util.SystemProperty;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.joda.time.DateTime;

public final class AdapterUtils {

    private AdapterUtils() {

    }

    public static AdapterContext getAdapterContext(Device device, AdapterManager adapterManager) {
        return adapterManager.getAdapterContext(new DeviceAdapterId(
                device.getDeviceManagement().getDeviceType(), device.getDeviceManagement().getDeviceInterfaceVersion(),
                device.getDeviceManagement().getDeviceModel(), device.getDeviceManagement().getDeviceVendor()));
    }

    public static AdapterContext getStandardAdapterContext(Device dev, AdapterManager adapterManager) {
        if (dev.getDeviceManagement().getDeviceType().equalsIgnoreCase(DPU)) {
            AdapterContext adapterContext = adapterManager.getAdapterContext(new DeviceAdapterId(DPU, STD_ADAPTER_VERSION, STANDARD, BBF));
            if (adapterContext == null) {
                throw new RuntimeException("no standard adapterContext deployed for : " + dev.getDeviceManagement().getDeviceType());
            }
            return adapterContext;
        } else if (dev.getDeviceManagement().getDeviceType().equalsIgnoreCase(OLT)) {
            AdapterContext adapterContext = adapterManager.getAdapterContext(new DeviceAdapterId(OLT, STD_ADAPTER_VERSION, STANDARD, BBF));
            if (adapterContext == null) {
                throw new RuntimeException("no standard adapterContext deployed for : " + dev.getDeviceManagement().getDeviceType());
            }
            return adapterContext;
        } else {
            throw new RuntimeException("no standard adapter found for this type of device : " + dev.getDeviceManagement().getDeviceType());
        } //Clean this up later for generic
    }

    public static String updateAdapterLastUpdateTime(String key, DateTime now) {
        String result = now.toString();
        SystemProperty systemProperty = SystemProperty.getInstance();
        String persistResult = systemProperty.get(key);
        if (persistResult != null) {
            result = persistResult;
        } else {
            systemProperty.set(key, result);
        }

        return result;
    }

    public static void removeAdapterLastUpdateTime(String key) {
        SystemProperty systemProperty = SystemProperty.getInstance();
        String persistResult = systemProperty.get(key);
        if (persistResult != null) {
            systemProperty.remove(key);
        }
    }

    public static DeviceAdapter getAdapter(Device device, AdapterManager adapterManager) {
        return adapterManager.getDeviceAdapter(new DeviceAdapterId(
                device.getDeviceManagement().getDeviceType(), device.getDeviceManagement().getDeviceInterfaceVersion(),
                device.getDeviceManagement().getDeviceModel(), device.getDeviceManagement().getDeviceVendor()));
    }
}
