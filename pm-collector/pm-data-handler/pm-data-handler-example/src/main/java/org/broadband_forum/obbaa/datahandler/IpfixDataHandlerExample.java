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

package org.broadband_forum.obbaa.datahandler;

import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpfixDataHandlerExample implements IpfixDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixDataHandlerExample.class);
    private DataHandlerService m_dataHandlerService;

    public IpfixDataHandlerExample(DataHandlerService dataHandlerService) {
        m_dataHandlerService = dataHandlerService;
    }

    public void init() {
        m_dataHandlerService.registerIpfixDataHandler(this);
    }

    public void destroy() {
        m_dataHandlerService.unregisterIpfixDataHandler(this);
    }

    @Override
    public void handleIpfixData(String ipfixMessageJson) {
        LOGGER.info("Decoded data : " + ipfixMessageJson);
    }
}
