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

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;

public interface PmaServer {
    ThreadLocal<DeviceInfo> CURRENT_DEVICE = new ThreadLocal<>();
    NetconfClientInfo PMA_USER = new NetconfClientInfo("PMA_USER", 1);

    NetConfResponse executeNetconf(AbstractNetconfRequest request);

    static void setCurrentDevice(DeviceInfo deviceInfo) {
        CURRENT_DEVICE.set(deviceInfo);
    }

    static void clearCurrentDevice() {
        CURRENT_DEVICE.remove();
    }

    static DeviceInfo getCurrentDevice() {
        return CURRENT_DEVICE.get();
    }
}
