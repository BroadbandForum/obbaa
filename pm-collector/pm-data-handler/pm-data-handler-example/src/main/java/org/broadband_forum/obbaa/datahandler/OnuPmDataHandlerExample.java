/*
 * Copyright 2023 Broadband Forum
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
import org.broadband_forum.obbaa.pm.service.OnuPMDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnuPmDataHandlerExample implements OnuPMDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnuPmDataHandlerExample.class);
    private DataHandlerService m_dataHandlerService;

    public OnuPmDataHandlerExample(DataHandlerService dataHandlerService) {
        m_dataHandlerService = dataHandlerService;
    }

    public void init() {
        //m_dataHandlerService.registerOnuPmDataHandler(this);
    }

    public void destroy() {

        //m_dataHandlerService.unregisterOnuPmDataHandler(this);
    }

    @Override
    public void handleOnuPMData(String msgID, String senderName, String recipientName, String objectType, String objectName, String data) {
        LOGGER.info("ONU telemetry data is : " + data);
    }
}
