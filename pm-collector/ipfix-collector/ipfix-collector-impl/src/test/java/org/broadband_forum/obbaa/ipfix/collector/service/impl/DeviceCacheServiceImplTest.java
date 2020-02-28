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

package org.broadband_forum.obbaa.ipfix.collector.service.impl;

import org.broadband_forum.obbaa.ipfix.ncclient.api.NcClientService;
import org.broadband_forum.obbaa.ipfix.ncclient.api.NetConfClientException;
import org.broadband_forum.obbaa.ipfix.ncclient.app.NcClientServiceImpl;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getRpcRequest;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeviceCacheServiceImplTest {

    private DeviceCacheServiceImpl m_deviceFamilyCacheService;

    @Before
    public void setup() throws NetconfMessageBuilderException {
        MockitoAnnotations.initMocks(this);
        //NetconfRpcRequest request = generateGetAllDevicesRequest();
        NetConfResponse response = generateGetAllDevicesResponse();
        m_deviceFamilyCacheService = new DeviceCacheServiceImpl() {
            @Override
            public NetConfResponse getNetConfResponse(AbstractNetconfRequest request) throws ExecutionException, NetConfClientException {
                return response;
            }
        };
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testGetDeviceFamilyInCache() throws NetconfMessageBuilderException, ExecutionException, NetConfClientException, InterruptedException {

        Map<String, String> cache = new HashMap<String, String>();
        cache.put("DPU1", "sample-DPU-modeltls-1.0");
        cache.put("OLT1", "sample-OLT-modeltls-1.0");

        NcClientService ncClientService = mock(NcClientServiceImpl.class);

        String family1 = m_deviceFamilyCacheService.getDeviceFamily("DPU1");
        assertEquals("sample-DPU-modeltls-1.0", family1);
        assertEquals(m_deviceFamilyCacheService.getDeviceFamilyCacheSize(), 2);

        String family2 = m_deviceFamilyCacheService.getDeviceFamily("OLT1");
        assertEquals("sample-OLT-modeltls-1.0", family2);
        assertEquals(m_deviceFamilyCacheService.getDeviceFamilyCacheSize(), 2);
    }

    private NetconfRpcRequest generateGetAllDevicesRequest() throws NetconfMessageBuilderException {
        String rpcString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"11\">\n" +
                "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <filter type=\"subtree\">\n" +
                "    <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "      <managed-devices>\n" +
                "      </managed-devices>\n" +
                "    </network-manager>\n" +
                "  </filter>\n" +
                "</get>\n" +
                "</rpc>";
        return getRpcRequest(stringToDocument(rpcString));
    }

    private NetConfResponse generateGetAllDevicesResponse() throws NetconfMessageBuilderException {
        String responseString = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"11\">\n" +
                "  <data>\n" +
                "    <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "      <baa-network-manager:managed-devices>\n" +
                "        <baa-network-manager:device>\n" +
                "          <baa-network-manager:name>DPU1</baa-network-manager:name>\n" +
                "          <baa-network-manager:device-management>            \n" +
                "            <baa-network-manager:interface-version>1.0</baa-network-manager:interface-version>\n" +
                "            <baa-network-manager:model>modeltls</baa-network-manager:model>\n" +
                "            <baa-network-manager:push-pma-configuration-to-device>true</baa-network-manager:push-pma-configuration-to-device>\n" +
                "            <baa-network-manager:type>DPU</baa-network-manager:type>\n" +
                "            <baa-network-manager:vendor>sample</baa-network-manager:vendor>\n" +
                "            <baa-network-manager:device-connection>\n" +
                "              <baa-network-manager:connection-model>direct</baa-network-manager:connection-model>              \n" +
                "            </baa-network-manager:device-connection>\n" +
                "          </baa-network-manager:device-management>\n" +
                "        </baa-network-manager:device>\n" +
                "        <baa-network-manager:device>\n" +
                "          <baa-network-manager:name>OLT1</baa-network-manager:name>\n" +
                "          <baa-network-manager:device-management>            \n" +
                "            <baa-network-manager:interface-version>1.0</baa-network-manager:interface-version>\n" +
                "            <baa-network-manager:model>modeltls</baa-network-manager:model>\n" +
                "            <baa-network-manager:push-pma-configuration-to-device>true</baa-network-manager:push-pma-configuration-to-device>\n" +
                "            <baa-network-manager:type>OLT</baa-network-manager:type>\n" +
                "            <baa-network-manager:vendor>sample</baa-network-manager:vendor>\n" +
                "            <baa-network-manager:device-connection>\n" +
                "              <baa-network-manager:connection-model>direct</baa-network-manager:connection-model>              \n" +
                "            </baa-network-manager:device-connection>\n" +
                "          </baa-network-manager:device-management>\n" +
                "        </baa-network-manager:device>\n" +
                "      </baa-network-manager:managed-devices>\n" +
                "    </baa-network-manager:network-manager>\n" +
                "  </data>\n" +
                "</rpc-reply>";
        return DocumentToPojoTransformer.getNetconfResponse(stringToDocument(responseString));
    }
}
