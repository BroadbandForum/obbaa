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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.ipfix.collector.service.InformationElementService;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IEMappingCacheServiceImplTest {

    @Mock
    private DeviceCacheServiceImpl m_deviceFamilyCacheService;
    @Mock
    private InformationElementService m_informationElementService;

    private IEMappingCacheServiceImpl m_ieIdIdMappingCacheService;

    private String m_family;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_ieIdIdMappingCacheService = new IEMappingCacheServiceImpl(m_informationElementService, m_deviceFamilyCacheService);
        m_family = "family";
    }

    @Test
    public void testSyncIEMappingCache() throws Exception {
        when(m_informationElementService.isIECacheAvailable()).thenReturn(false);
        String responseInStr = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <get>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "        <device-adapters xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "          <device-adapter>\n" +
                "            <type/>\n" +
                "            <model/>\n" +
                "            <vendor/>\n" +
                "            <interface-version/>\n" +
                "          </device-adapter>\n" +
                "        </device-adapters>\n" +
                "      </baa-network-manager:network-manager>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>";
        NetConfResponse response = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(responseInStr));
        when(m_deviceFamilyCacheService.getNetConfResponse(any())).thenReturn(response);

        m_ieIdIdMappingCacheService.syncIEMappingCache(m_family);
        verify(m_informationElementService).isIECacheAvailable();
        String requestInString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <get>\n" +
                "      <filter type=\"subtree\">\n" +
                "         <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "            <device-adapters xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "               <device-adapter>\n" +
                "                  <type/>\n" +
                "                  <model/>\n" +
                "                  <vendor/>\n" +
                "                  <interface-version/>\n" +
                "               </device-adapter>\n" +
                "            </device-adapters>\n" +
                "         </baa-network-manager:network-manager>\n" +
                "      </filter>\n" +
                "   </get>\n" +
                "</rpc>\n";
        ArgumentCaptor<AbstractNetconfRequest> captor = new ArgumentCaptor<>();
        verify(m_deviceFamilyCacheService).getNetConfResponse(captor.capture());
        assertEquals(requestInString, captor.getValue().requestToString());
    };
}
