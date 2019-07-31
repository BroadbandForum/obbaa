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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ALIGNED;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.IN_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AlignmentTimerTest {

    private static String SAMPLE_EMPTY_NC_RES = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
        "message-id=\"101012\">\n" +
        "  <data>\n" +
        "  </data>\n" +
        "</rpc-reply>";
    private static String SAMPLE_NON_EMPTY_NC_RES = "<rpc-reply " +
        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
        "message-id=\"101012\">\n" +
        "  <data>\n" +
        "    <root-config xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
        "      <config1>\n" +
        "      </config1>\n" +
        "    </root-config>\n" +
        "  </data>\n" +
        "</rpc-reply>";

    private static String EDIT_CONFIG_REQ = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
        "  <edit-config>\n" +
        "    <target>\n" +
        "      <running/>\n" +
        "    </target>\n" +
        "    <default-operation>merge</default-operation>\n" +
        "    <test-option>set</test-option>\n" +
        "    <error-option>stop-on-error</error-option>\n" +
        "    <config>\n" +
        "      <root-config xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
        "      <config1>\n" +
        "      </config1>\n" +
        "    </root-config>\n" +
        "    </config>\n" +
        "  </edit-config>\n" +
        "</rpc>\n";

    private static String OK_RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
        "  <ok/>\n" +
        "</rpc-reply>";

    private static String ERROR_RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
        "  <rpc-error>\n" +
        "    <error-type>application</error-type>\n" +
        "    <error-tag>operation-failed</error-tag>\n" +
        "    <error-severity>error</error-severity>\n" +
        "  </rpc-error>\n" +
        "</rpc-reply>";
    private AlignmentTimer m_timer;
    private List<Device> m_devices;
    private Device m_newDevice;
    private Device m_alignedDevice;
    private Device m_misalignedDevice;
    private Device m_inErrorDevice;
    @Mock
    private PmaRegistry m_pmaRegistry;
    @Mock
    private PmaSession m_pmaSession;
    @Mock
    private DeviceDao m_deviceDao;
    private TxService m_txService;
    @Mock
    private PersistenceManagerUtil m_persistenceMgrUtil;
    @Mock
    private EntityDataStoreManager entityDSM;
    @Mock
    private EntityManager entityMgr;
    @Mock
    private EntityTransaction entityTx;
    @Mock
    private AdapterManager m_adapterMgr;
    @Mock
    private DeviceInterface m_devInterface;
    @Mock
    private AdapterContext m_context;


    private void reviseStrForWindows() {
         SAMPLE_EMPTY_NC_RES = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"101012\">\r\n" +
                "  <data>\r\n" +
                "  </data>\r\n" +
                "</rpc-reply>";
         SAMPLE_NON_EMPTY_NC_RES = "<rpc-reply " +
                "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"101012\">\r\n" +
                "  <data>\r\n" +
                "    <root-config xmlns=\"urn:bbf:yang:obbaa:network-manager\">\r\n" +
                "      <config1>\r\n" +
                "      </config1>\r\n" +
                "    </root-config>\r\n" +
                "  </data>\r\n" +
                "</rpc-reply>";

         EDIT_CONFIG_REQ = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                "  <edit-config>\r\n" +
                "    <target>\r\n" +
                "      <running/>\r\n" +
                "    </target>\r\n" +
                "    <default-operation>merge</default-operation>\r\n" +
                "    <test-option>set</test-option>\r\n" +
                "    <error-option>stop-on-error</error-option>\r\n" +
                "    <config>\r\n" +
                "      <root-config xmlns=\"urn:bbf:yang:obbaa:network-manager\">\r\n" +
                "      <config1>\r\n" +
                "      </config1>\r\n" +
                "    </root-config>\r\n" +
                "    </config>\r\n" +
                "  </edit-config>\r\n" +
                "</rpc>\r\n";

          OK_RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                "  <ok/>\r\n" +
                "</rpc-reply>";

          ERROR_RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                "  <rpc-error>\r\n" +
                "    <error-type>application</error-type>\r\n" +
                "    <error-tag>operation-failed</error-tag>\r\n" +
                "    <error-severity>error</error-severity>\r\n" +
                "  </rpc-error>\r\n" +
                "</rpc-reply>";
    }
    @Before
    public void setUp() throws ExecutionException, NetconfMessageBuilderException {

        if (System.getProperty("os.name").startsWith("Windows")) {
            reviseStrForWindows();
        }
        MockitoAnnotations.initMocks(this);
        m_txService = new TxService();
        when(m_persistenceMgrUtil.getEntityDataStoreManager()).thenReturn(entityDSM);
        when(entityDSM.getEntityManager()).thenReturn(entityMgr);
        when(entityMgr.getTransaction()).thenReturn(entityTx);
        when(entityTx.isActive()).thenReturn(true);
        m_devices = new ArrayList<>();
        m_newDevice = createDevice("new-device");
        m_devices.add(m_newDevice);
        m_alignedDevice = createDevice("aligned-device");
        m_alignedDevice.getDeviceManagement().getDeviceState().setConfigAlignmentState(ALIGNED);
        m_devices.add(m_alignedDevice);
        m_misalignedDevice = createDevice("misaligned-device");
        m_misalignedDevice.getDeviceManagement().getDeviceState().setConfigAlignmentState("2 Edits pending");
        m_devices.add(m_misalignedDevice);
        m_inErrorDevice = createDevice("in-error");
        m_inErrorDevice.getDeviceManagement().getDeviceState().setConfigAlignmentState(IN_ERROR + ", blah blah");
        m_devices.add(m_inErrorDevice);
        when(m_deviceDao.findAllDevices()).thenReturn(m_devices);
        m_timer = new AlignmentTimer(m_pmaRegistry, m_adapterMgr, m_deviceDao, m_txService);
        when(m_adapterMgr.getAdapterContext(any())).thenReturn(m_context);
        when(m_context.getDeviceInterface()).thenReturn(m_devInterface);
        Map<NetConfResponse, List<Notification>> map = new HashMap<>();
        NetConfResponse netConfResponse = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(SAMPLE_EMPTY_NC_RES));
        map.put(netConfResponse, Collections.emptyList());
        when(m_pmaSession.executeNC(anyString())).thenReturn(map);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return ((PmaSessionTemplate) args[1]).execute(m_pmaSession);
        }).when(m_pmaRegistry).executeWithPmaSession(anyString(), anyObject());
    }

    private Device createDevice(String deviceName) {
        Device device = new Device();
        device.setDeviceName(deviceName);
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        device.setDeviceManagement(deviceMgmt);
        return device;
    }

    @Test
    public void testNewDevicesAreAligned() throws ExecutionException {
        m_timer.runAlignment();
        verify(m_pmaSession, times(2)).align();
        verify(m_pmaSession, never()).forceAlign();
    }

    @Test
    public void testNewDevicesWithConfigurationsAreAligned() throws ExecutionException, NetconfMessageBuilderException {
        Map<NetConfResponse, List<Notification>> map = new HashMap<>();
        NetConfResponse netConfResponse = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(SAMPLE_NON_EMPTY_NC_RES));
        map.put(netConfResponse, Collections.emptyList());
        when(m_pmaSession.executeNC(anyString())).thenReturn(map);
        m_timer.runAlignment();
        verify(m_pmaSession, times(2)).align();
    }

    @Test
    public void testUploadConfig() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        m_newDevice.getDeviceManagement().setPushPmaConfigurationToDevice("false");
        Future<NetConfResponse> responseFuture = mock(Future.class);
        NetConfResponse netConfResponse = null;
        try {
            netConfResponse = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(SAMPLE_NON_EMPTY_NC_RES));
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
        when(responseFuture.get()).thenReturn(netConfResponse);
        when(m_devInterface.getConfig(eq(m_newDevice), anyObject())).thenReturn(responseFuture);
        Map<NetConfResponse, List<Notification>> map = new HashMap<>();
        NetConfResponse netConfRespons = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(OK_RESPONSE));
        map.put(netConfRespons, Collections.emptyList());
        when(m_pmaSession.executeNC(EDIT_CONFIG_REQ)).thenReturn(map);
        m_timer.runAlignment();
        assertEquals("true", m_newDevice.getDeviceManagement().getPushPmaConfigurationToDevice());
        verify(m_deviceDao).updateDeviceAlignmentState("new-device", ALIGNED);
    }

    @Test
    public void testUploadConfigError() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        m_newDevice.getDeviceManagement().setPushPmaConfigurationToDevice("false");
        Future<NetConfResponse> responseFuture = mock(Future.class);
        NetConfResponse netConfResponse = null;
        try {
            netConfResponse = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(SAMPLE_NON_EMPTY_NC_RES));
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
        when(responseFuture.get()).thenReturn(netConfResponse);
        when(m_devInterface.getConfig(eq(m_newDevice), anyObject())).thenReturn(responseFuture);
        Map<NetConfResponse, List<Notification>> map = new HashMap<>();
        NetConfResponse netConfRespons = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(ERROR_RESPONSE));
        map.put(netConfRespons, Collections.emptyList());
        when(m_pmaSession.executeNC(EDIT_CONFIG_REQ)).thenReturn(map);
        m_timer.runAlignment();
        assertEquals("false", m_newDevice.getDeviceManagement().getPushPmaConfigurationToDevice());
        verify(m_deviceDao, never()).updateDeviceAlignmentState(anyString(), anyString());
        assertTrue(m_newDevice.isNeverAligned());
    }

    @Test
    public void testUploadConfigEmptyResponseFromDevice() throws ExecutionException, InterruptedException {
        m_newDevice.getDeviceManagement().setPushPmaConfigurationToDevice("false");
        Future<NetConfResponse> responseFuture = mock(Future.class);
        NetConfResponse netConfResponse = null;
        try {
            netConfResponse = DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument(SAMPLE_EMPTY_NC_RES));
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
        when(responseFuture.get()).thenReturn(netConfResponse);
        when(m_devInterface.getConfig(eq(m_newDevice), anyObject())).thenReturn(responseFuture);
        m_timer.runAlignment();
        assertEquals("true", m_newDevice.getDeviceManagement().getPushPmaConfigurationToDevice());
        verify(m_deviceDao).updateDeviceAlignmentState("new-device", ALIGNED);
    }

}
