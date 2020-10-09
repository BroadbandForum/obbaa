/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.onu.kafka.OnuKafkaProducer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Unit tests that tests session for Mediated devices to handle netconf requests and responses
 * <p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class MediatedDeviceNetconfSessionTest {
    @Mock
    private Device m_mediatedDevice;

    @Mock
    private OnuKafkaProducer m_kafkaProducer;

    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;

    @Mock
    private AdapterManager m_adapterManager;

    @Mock
    public static GetRequest m_getRequestOne;

    @Mock
    private EditConfigRequest m_editConfigRequest;
    @Mock
    private AdapterContext m_adapterContext;
    @Mock
    private NetconfFilter m_filter;
    @Mock
    private ThreadPoolExecutor m_kafkaCommunicationPool;

    private String m_oltDeviceName;
    private String m_onuId;
    private String m_channelTermRef;
    private HashMap<String, String> m_labels;
    private String getRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <get>\n" +
            "    <filter type=\"subtree\">\n" +
            "      <interfaces-state xmlns=\"urn:ietf:ns\">\n" +
            "        <leaf1/>\n" +
            "        <leaf2/>\n" +
            "      </interfaces-state>\n" +
            "    </filter>\n" +
            "  </get>\n" +
            "</rpc>";
    @Mock
    private DeviceMgmt m_deviceMgmt;


    @Before
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        m_oltDeviceName = "pOLT";
        m_onuId = "1";
        m_channelTermRef = "CT_1";
        m_labels = new HashMap<>();
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getMessageId()).thenReturn(ONUConstants.ONU_GET_OPERATION + "-1");
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(getRequest));
        when(m_editConfigRequest.getMessageId()).thenReturn(ONUConstants.ONU_EDIT_OPERATION);
        when(m_adapterManager.getAdapterContext(any(DeviceAdapterId.class))).thenReturn(m_adapterContext);
        when(m_mediatedDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn("OLT");
        when(m_deviceMgmt.getDeviceModel()).thenReturn("standard");
        when(m_deviceMgmt.getDeviceVendor()).thenReturn("BBF");
        when(m_deviceMgmt.getDeviceInterfaceVersion()).thenReturn("1.0");
    }

    @Test
    public void testNoSessionRequests() {
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();

        // Ensure kafka communication pool is never invoked
        //verify(m_kafkaCommunicationPool, never()).offer(anyString(), (Runnable) anyObject());
        assertEquals(0, session.getNumberOfPendingRequests());
        // Verify integrity of the session
        verifySessionIntegrity(session, null, null, null);
    }

    private MediatedDeviceNetconfSession getNewMDNSessionForNewDevice() {
        return new MediatedDeviceNetconfSession(m_mediatedDevice, m_oltDeviceName, m_onuId, m_channelTermRef, m_labels,
                1, m_kafkaProducer, m_modelNodeDSM, m_adapterManager, m_kafkaCommunicationPool);
    }

    private void verifySessionIntegrity(MediatedDeviceNetconfSession session, String messageIdOne, String messageIdTwo,
                                        String messageIdThree) {
        assertTrue(session instanceof NetconfClientSession);
        assertTrue(session.isOpen());
        int count = 0;
        long timeStamp = 0;
        Map<String, TimestampFutureResponse> requestMap = session.getMapOfRequests();
        for (Map.Entry<String, TimestampFutureResponse> entry : requestMap.entrySet()) {
            switch (count) {
                case 0:
                    assertEquals(entry.getKey(), messageIdOne);
                    break;
                case 1:
                    assertEquals(entry.getKey(), messageIdTwo);
                    break;
                case 2:
                    assertEquals(entry.getKey(), messageIdThree);
                    break;
            }
            assertTrue(timeStamp <= entry.getValue().getTimeStamp());
            timeStamp = entry.getValue().getTimeStamp();
            count++;
        }
    }
}
