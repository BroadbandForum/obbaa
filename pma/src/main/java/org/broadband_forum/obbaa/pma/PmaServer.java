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

package org.broadband_forum.obbaa.pma;

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.PmaResource;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;

public interface PmaServer {
    ThreadLocal<PmaResource> CURRENT_DEVICE = new ThreadLocal<>();
    ThreadLocal<DeviceXmlStore> CURRENT_DEVICE_XML_STORE = new ThreadLocal<>();
    ThreadLocal<DeviceXmlStore> BACKUP_DEVICE_XML_STORE = new ThreadLocal<>();
    NetconfClientInfo PMA_USER = new NetconfClientInfo("PMA_USER", 1);

    Map<NetConfResponse, List<Notification>> executeNetconf(AbstractNetconfRequest request);

    static void setCurrentDevice(PmaResource device) {
        CURRENT_DEVICE.set(device);
    }

    static void clearCurrentDevice() {
        CURRENT_DEVICE.remove();
    }

    static Device getCurrentDevice() {
        if (CURRENT_DEVICE.get() instanceof Device) {
            return (Device) CURRENT_DEVICE.get();
        }
        return null;
    }

    static void setCurrentDeviceXmlStore(DeviceXmlStore deviceXmlStore) {
        CURRENT_DEVICE_XML_STORE.set(deviceXmlStore);
    }

    static void clearCurrentDeviceXmlStore() {
        CURRENT_DEVICE_XML_STORE.remove();
    }

    static DeviceXmlStore getCurrentDeviceXmlStore() {
        return CURRENT_DEVICE_XML_STORE.get();
    }

    static void setBackupDeviceXmlStore(DeviceXmlStore deviceXmlStore) {
        BACKUP_DEVICE_XML_STORE.set(deviceXmlStore);
    }

    static void clearBackupDeviceXmlStore() {
        BACKUP_DEVICE_XML_STORE.remove();
    }

    static DeviceXmlStore getBackupDeviceXmlStore() {
        return BACKUP_DEVICE_XML_STORE.get();
    }

    //methods for network functions
    /*TODO obbaa-366 missing attributes for network functions, currently
    *  using device attributes*/
    static void setCurrentNetworkFunction(PmaResource networkFunction) {
        CURRENT_DEVICE.set(networkFunction);
    }

    static NetworkFunction getCurrentNetworkFunction() {
        PmaResource resource = CURRENT_DEVICE.get();
        if (resource instanceof NetworkFunction) {
            return (NetworkFunction) resource;
        }
        return null;
    }

    boolean isActive();
}
