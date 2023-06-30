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
package org.broadband_forum.obbaa.onu.pm.datahandler.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.pm.datahandler.OnuPerformanceManagement;
import org.broadband_forum.obbaa.onu.pm.kafka.OnuKafkaNotificationCallback;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Msg;
import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.OnuPMDataHandler;


public class OnuPerformanceManagementImpl implements OnuPerformanceManagement {

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaNotificationCallback.class);
    private final DataHandlerService m_dataHandlerService;

    public OnuPerformanceManagementImpl(DataHandlerService dataHandlerService) {
        m_dataHandlerService = dataHandlerService;
    }

    public void processOnuPmData(Object obj) {
        LOGGER.info("Received data from vomci");
        if (obj != null && !obj.toString().equals("")) {
            String msgId = ((Msg) obj).getHeader().getMsgId();
            String senderName = ((Msg) obj).getHeader().getSenderName();
            String recipientName = ((Msg) obj).getHeader().getRecipientName();
            String objectType = ((Msg) obj).getHeader().getObjectType().name();
            String objectName = ((Msg) obj).getHeader().getObjectName();
            String data = ((Msg) obj).getBody().getNotification().getData().toStringUtf8();

            List<OnuPMDataHandler> dataHandlers = m_dataHandlerService.getOnuPmDataHandlers();
            for (OnuPMDataHandler dataHandler : dataHandlers) {
                dataHandler.handleOnuPMData(msgId, senderName, recipientName, objectType, objectName, data);
            }
        } else {
            LOGGER.debug("Kafka received with null value");
        }
    }
}
