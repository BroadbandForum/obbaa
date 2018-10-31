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

package org.broadband_forum.obbaa.connectors.sbi.netconf.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class TimerService {
    private static final Logger LOGGER = Logger.getLogger(TimerService.class);
    private final NetconfConnectionManagerImpl m_connectionManager;
    private final Timer m_timer;

    public TimerService(NetconfConnectionManagerImpl connectionManager) {
        m_connectionManager = connectionManager;
        m_timer = new Timer();
    }

    public void init() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (m_connectionManager.getDeviceDao() != null && m_connectionManager.getTxService() != null) {
                        m_connectionManager.auditConnections();
                    } else {
                        LOGGER.error("Audit connection not running yet");
                    }
                } catch (Exception e) {
                    LOGGER.error("Something wrong happened during connection audit" , e);
                }
            }
        }, 1000L, 10000L);
    }

    public void destroy() {
        m_timer.cancel();
    }

}
