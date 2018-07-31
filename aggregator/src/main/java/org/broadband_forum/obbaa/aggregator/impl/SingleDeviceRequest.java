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

package org.broadband_forum.obbaa.aggregator.impl;

import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Request of one device mounted to the network-manager.
 */
public class SingleDeviceRequest {
    private Document m_requestToBeProcessed;
    private String m_deviceName;
    private String m_deviceType;
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleDeviceRequest.class);

    public SingleDeviceRequest(String deviceName, String deviceType, Document requestToBeProcessed) {
        m_deviceName = deviceName;
        m_deviceType = deviceType;
        m_requestToBeProcessed = requestToBeProcessed;

        LOGGER.info("SingleDeviceRequest: {}-{}-{}", deviceName, deviceType, requestToBeProcessed);
    }

    /**
     * Get document of request dispatched to the processors.
     *
     * @return Document of message
     */
    Document getDocument() {
        return m_requestToBeProcessed;
    }

    /**
     * Get device name of the request.
     *
     * @return Device name
     */
    String getDeviceName() {
        return m_deviceName;
    }

    /**
     * Set the device name of the request.
     *
     * @param deviceType Device type
     */
    public void setDeviceType(String deviceType) {
        m_deviceType = deviceType;
    }

    /**
     * Get type of the device.
     *
     * @return Device type
     */
    public String getDeviceType() {
        return m_deviceType;
    }

    /**
     * Get namespace of the request.
     *
     * @return Namespace
     */
    String getNamespace() {
        String namespace = NetconfMessageUtil.getParentXmlns(m_requestToBeProcessed);
        LOGGER.info("getNamespace: {}", namespace);
        return namespace;
    }

    /**
     * Get message will be dispatched to the request processor.
     *
     * @return Message
     */
    String getMessage() {
        String message = PojoToDocumentTransformer.requestToString(m_requestToBeProcessed);
        return message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
