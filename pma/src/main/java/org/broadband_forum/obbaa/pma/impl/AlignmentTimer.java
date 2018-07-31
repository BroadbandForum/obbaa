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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentInfo;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentStore;
import org.broadband_forum.obbaa.store.dm.DeviceAdminStore;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;

public class AlignmentTimer {
    private static final Logger LOGGER = Logger.getLogger(AlignmentTimer.class);
    private final PmaRegistry m_pmaRegistry;
    private final DeviceAlignmentStore m_alignmentStore;
    private final Timer m_timer;
    private final NetconfConnectionManager m_connMgr;
    private final DeviceAdminStore m_adminStore;

    public AlignmentTimer(DeviceAlignmentStore alignmentStore, PmaRegistry pmaRegistry, DeviceAdminStore adminStore,
                          NetconfConnectionManager connMgr) {
        m_alignmentStore = alignmentStore;
        m_pmaRegistry = pmaRegistry;
        m_adminStore = adminStore;
        m_connMgr = connMgr;
        m_timer = new Timer();
    }

    public void init() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runAlignment();
            }
        }, 10000L, 10000L);
    }

    public void destroy() {
        m_timer.cancel();
    }

    public void runAlignment() {
        Set<DeviceAlignmentInfo> devices = m_alignmentStore.getAllEntries();
        for (DeviceAlignmentInfo device : devices) {
            if (deviceIsConnected(device)) {
                try {
                    if (device.isNeverAligned()) {
                        LOGGER.debug(String.format("Device %s is " + DeviceAlignmentInfo.NEVER_ALIGNED, device));

                        if (deviceHasConfigurations(device)) {
                            m_pmaRegistry.forceAlign(device.getName());
                        }

                    } else if (!device.isAligned()) {
                        m_pmaRegistry.align(device.getName());
                    }
                } catch (ExecutionException e) {
                    LOGGER.error(String.format("Could not align device %s", device), e);
                }
            }
        }
    }

    private boolean deviceIsConnected(DeviceAlignmentInfo device) {
        DeviceInfo deviceInfo = m_adminStore.get(device.getKey());
        return m_connMgr.isConnected(deviceInfo);
    }

    private boolean deviceHasConfigurations(DeviceAlignmentInfo device) throws ExecutionException {
        return m_pmaRegistry.executeWithPmaSession(device.getName(), session -> {
            String responseStr = session.executeNC(new GetConfigRequest().setSourceRunning().setMessageId("xx")
                    .requestToString());
            try {
                NetConfResponse response = getNetconfResponse(DocumentUtils.stringToDocument(responseStr));
                return !response.getDataContent().isEmpty();
            } catch (NetconfMessageBuilderException e) {
                throw new ExecutionException(e);
            }
        });
    }
}
