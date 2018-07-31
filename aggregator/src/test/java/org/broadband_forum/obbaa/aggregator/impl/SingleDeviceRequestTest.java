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

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SingleDeviceRequestTest {
    private SingleDeviceRequest m_singleDeviceRequest;
    private Document m_document;

    private static String TEST_NS = "urn:ietf:params:xml:ns:yang:ietf-system";
    private static String TEST_REQUEST =
                    "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\r\n" +
                            "  <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\r\n" +
                            "    <system xmlns=\"urn:ietf:params:xml:ns:yang:ietf-system\">\r\n" +
                            "      <device-name>A</device-name>\r\n" +
                            "      <device-type>AA</device-type>\r\n" +
                            "      <restart/>\r\n" +
                            "    </system>\r\n" +
                            "  </action>\r\n" +
                            "</rpc>\r\n";

    private static String DEVICE_NAME = "deviceA";
    private static String DEVICE_TYPE = "deviceDPU";

    @Before
    public void setUp() throws Exception {
        m_document = DocumentUtils.stringToDocument(TEST_REQUEST);
        m_singleDeviceRequest = new SingleDeviceRequest(DEVICE_NAME, null, m_document);
        m_singleDeviceRequest.setDeviceType(DEVICE_TYPE);
    }

    @Test
    public void getDocumentTest() throws Exception {
        assertEquals(m_document, m_singleDeviceRequest.getDocument());
    }

    @Test
    public void getDeviceNameTest() throws Exception {
        assertEquals(DEVICE_NAME, m_singleDeviceRequest.getDeviceName());
    }

    @Test
    public void getDeviceTypeTest() throws Exception {
        assertEquals(DEVICE_TYPE, m_singleDeviceRequest.getDeviceType());
    }

    @Test
    public void getNameSpaceTest() throws Exception {
        String namespace = m_singleDeviceRequest.getNamespace();
        assertEquals(TEST_NS, namespace);
    }

    @Test
    public void toStringTest() throws Exception {
        String message = m_singleDeviceRequest.toString();
        assertTrue(message.contains("device-name"));
    }
}