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

package org.broadband_forum.obbaa.ipfix.collector.threadpool;

import java.util.concurrent.ExecutorService;

import org.broadband_forum.obbaa.ipfix.collector.service.CollectingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IpfixCollectorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixCollectorService.class);
    private CollectingService m_collectingService;
    private IpfixCollectorServer m_server;
    private ExecutorService m_executorService;
    private ExecutorService m_msgProcessingExecutor;

    public IpfixCollectorService(CollectingService collectingService,
                                 ExecutorService executorService, ExecutorService msgProcessingExecutor) {
        this.m_collectingService = collectingService;
        this.m_executorService = executorService;
        m_msgProcessingExecutor = msgProcessingExecutor;
    }

    public void init() {
        LOGGER.info("Start IPFIX Collector");
        m_server = new IpfixCollectorServer(m_collectingService, m_executorService, m_msgProcessingExecutor);
        m_executorService.execute(() -> m_server.startServer());
    }

    public void destroy() {
        if (m_server != null) {
            m_server.closeServer();
        }
    }
}
