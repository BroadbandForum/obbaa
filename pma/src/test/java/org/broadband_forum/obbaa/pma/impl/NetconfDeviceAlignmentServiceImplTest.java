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
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEVER_ALIGNED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.impl.NcCompliantAdapterDeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NetconfDeviceAlignmentServiceImplTest {
    public static final String EDIT_REQ_STR = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
        "  <edit-config>\n" +
        "    <target>\n" +
        "      <running />\n" +
        "    </target>\n" +
        "    <test-option>set</test-option>\n" +
        "    <config>\n" +
        "      <some-config xmlns=\"some:ns\"/>\n" +
        "    </config>\n" +
        "  </edit-config>\n" +
        "</rpc>";
    private static final String CC_REQ_STR = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
        "  <copy-config>\n" +
        "    <source>blah<source>\n" +
        "\t<target>\n" +
        "      <running />\n" +
        "    </target>\n" +
        "    <test-option>set</test-option>\n" +
        "    <config>\n" +
        "      <some-config xmlns=\"some:ns\"/>\n" +
        "    </config>\n" +
        "  </copy-config>\n" +
        "</rpc>";

    //windows has different line feed
    public static final String EDIT_REQ_STR_WINDOWS = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\r\n" +
            "  <edit-config>\r\n" +
            "    <target>\r\n" +
            "      <running />\r\n" +
            "    </target>\r\n" +
            "    <test-option>set</test-option>\r\n" +
            "    <config>\r\n" +
            "      <some-config xmlns=\"some:ns\"/>\r\n" +
            "    </config>\r\n" +
            "  </edit-config>\r\n" +
            "</rpc>";
    private static final String CC_REQ_STR_WINDOWS = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\r\n" +
            "  <copy-config>\r\n" +
            "    <source>blah<source>\r\n" +
            "\t<target>\r\n" +
            "      <running />\r\n" +
            "    </target>\r\n" +
            "    <test-option>set</test-option>\r\n" +
            "    <config>\r\n" +
            "      <some-config xmlns=\"some:ns\"/>\r\n" +
            "    </config>\r\n" +
            "  </copy-config>\r\n" +
            "</rpc>";

    NetconfDeviceAlignmentServiceImpl m_das;
    @Mock
    private EditConfigRequest m_edit1;
    @Mock
    private EditConfigRequest m_edit2;
    @Mock
    private EditConfigRequest m_edit3;
    @Mock
    private EditConfigRequest m_edit4;
    @Mock
    private NetconfConnectionManager m_ncm;
    @Mock
    private Future<NetConfResponse> m_responseFutureObject;
    private NetConfResponse m_okResponse;
    @Mock
    private Future<NetConfResponse> m_notOkResponseFuture;
    private NetConfResponse m_notOkResponse;
    @Mock
    private EditConfigRequest m_edit5;
    @Mock
    private CopyConfigRequest m_cc;
    @Mock
    private DeviceManager m_dm;
    private Device m_device1;
    private Device m_device2;
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
    private AdapterManager m_manager;
    @Mock
    private AdapterContext m_context;
    private DeviceInterface m_deviceInterface;
    @Mock
    private NetConfResponse m_getResponse;
    private NetConfResponse m_pmaDsGetConfigResponse;

    @Before
    public void setUp() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        MockitoAnnotations.initMocks(this);
        m_txService = new TxService();
        when(m_persistenceMgrUtil.getEntityDataStoreManager()).thenReturn(entityDSM);
        when(entityDSM.getEntityManager()).thenReturn(entityMgr);
        when(entityMgr.getTransaction()).thenReturn(entityTx);
        when(entityTx.isActive()).thenReturn(true);
        if (isWindowsSys()) {
            when(m_edit1.requestToString()).thenReturn(EDIT_REQ_STR_WINDOWS);
            when(m_edit2.requestToString()).thenReturn(EDIT_REQ_STR_WINDOWS);
            when(m_edit3.requestToString()).thenReturn(EDIT_REQ_STR_WINDOWS);
            when(m_edit4.requestToString()).thenReturn(EDIT_REQ_STR_WINDOWS);
            when(m_edit5.requestToString()).thenReturn(EDIT_REQ_STR_WINDOWS);
            when(m_cc.requestToString()).thenReturn(CC_REQ_STR_WINDOWS);
        }
        else {
            when(m_edit1.requestToString()).thenReturn(EDIT_REQ_STR);
            when(m_edit2.requestToString()).thenReturn(EDIT_REQ_STR);
            when(m_edit3.requestToString()).thenReturn(EDIT_REQ_STR);
            when(m_edit4.requestToString()).thenReturn(EDIT_REQ_STR);
            when(m_edit5.requestToString()).thenReturn(EDIT_REQ_STR);
            when(m_cc.requestToString()).thenReturn(CC_REQ_STR);
        }

        m_deviceInterface = mock(NcCompliantAdapterDeviceInterface.class);
        when(m_manager.getAdapterContext(any())).thenReturn(m_context);
        when(m_context.getDeviceInterface()).thenReturn(m_deviceInterface);
        when(m_deviceInterface.forceAlign(any(), any())).thenReturn(new Pair<>(m_cc, m_responseFutureObject));
        when(m_deviceInterface.align(any(), any(), any())).thenReturn(m_responseFutureObject);
        when(m_ncm.executeNetconf(anyString(), anyObject())).thenReturn(m_responseFutureObject);
        m_okResponse = new NetConfResponse().setOk(true);
        m_okResponse.setMessageId("1");
        when(m_responseFutureObject.get()).thenReturn(m_okResponse);
        m_notOkResponse = new NetConfResponse().setOk(false);
        m_notOkResponse.addError(NetconfRpcError.getApplicationError("Something went wrong"));
        m_notOkResponse.setMessageId("1");
        when(m_notOkResponseFuture.get()).thenReturn(m_notOkResponse);
        m_das = new NetconfDeviceAlignmentServiceImpl(m_dm, m_ncm, m_manager);
        m_das.setTxService(m_txService);
        m_device1 = createDevice("device1");
        m_device2 = createDevice("device2");
        when(m_dm.getDevice("device1")).thenReturn(m_device1);
        when(m_dm.getDevice("device2")).thenReturn(m_device2);
        m_das.deviceAdded(m_device1.getDeviceName());
        m_das.deviceAdded(m_device2.getDeviceName());
        m_das.forceAlign(m_device1, mock(NetConfResponse.class));
        verify(m_dm).updateConfigAlignmentState(m_device1.getDeviceName() , ALIGNED);
        updateAlignment(m_device1.getDeviceName(), ALIGNED);
        m_das.forceAlign(m_device2, mock(NetConfResponse.class));
        verify(m_dm).updateConfigAlignmentState(m_device2.getDeviceName() , ALIGNED);
        updateAlignment(m_device2.getDeviceName(), ALIGNED);
        m_das.queueEdit(m_device1.getDeviceName(), m_edit1);
        m_das.queueEdit(m_device2.getDeviceName(), m_edit2);
        m_das.queueEdit(m_device1.getDeviceName(), m_edit3);
        m_das.queueEdit(m_device2.getDeviceName(), m_edit4);
    }

    private void updateAlignment(String deviceName , String verdict) {
        m_dm.getDevice(deviceName).getDeviceManagement().getDeviceState().setConfigAlignmentState(verdict);
    }

    private Device createDevice(String device1) {
        Device device = new Device();
        device.setDeviceName(device1);
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        device.setDeviceManagement(deviceMgmt);
        return device;
    }

    @Test
    public void testDASQueuesRequestsInOrder() {
        List<EditConfigRequest> edits = new ArrayList<>();

        edits.add(m_edit1);
        edits.add(m_edit3);
        assertEquals(edits, m_das.getEditQueue(m_device1.getDeviceName()));
        edits.clear();

        edits.add(m_edit2);
        edits.add(m_edit4);
        assertEquals(edits, m_das.getEditQueue(m_device2.getDeviceName()));
    }

    @Test
    public void testDASContactsNCMForFlushing() throws ExecutionException {
        m_das.alignAllDevices();
        assertEquals(0, m_das.getEditQueue(m_device1.getDeviceName()).size());
        assertEquals(0, m_das.getEditQueue(m_device2.getDeviceName()).size());
        verify(m_deviceInterface).align(m_device1, m_edit1, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device2, m_edit2, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device1, m_edit3, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device2, m_edit4, m_pmaDsGetConfigResponse);
    }

    @Test
    public void makeSureErrorResponseClearsRestOfEdits() throws ExecutionException, NetconfMessageBuilderException {
        makeDeviceError(m_edit1);
        assertEquals(0, m_das.getEditQueue(m_device1.getDeviceName()).size());
        assertEquals(0, m_das.getEditQueue(m_device2.getDeviceName()).size());
        verify(m_deviceInterface).align(m_device1, m_edit1, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device2, m_edit2, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface, never()).align(m_device1, m_edit3, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device2, m_edit4, m_pmaDsGetConfigResponse);
    }

    @Test
    public void makeSureTimedoutResponseClearsRestOfEdits() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        makeDeviceTimeout(m_edit1);
        assertEquals(0, m_das.getEditQueue(m_device1.getDeviceName()).size());
        assertEquals(0, m_das.getEditQueue(m_device2.getDeviceName()).size());
        verify(m_deviceInterface).align(m_device1, m_edit1, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device2, m_edit2, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface, never()).align(m_device1, m_edit3, m_pmaDsGetConfigResponse);
        verify(m_deviceInterface).align(m_device2, m_edit4, m_pmaDsGetConfigResponse);
    }

    @Test
    public void testDeviceState() {
        assertEquals("2 Edit(s) Pending", m_das.getAlignmentState(m_device1.getDeviceName()));
        m_das.queueEdit(m_device1.getDeviceName(), m_edit1);
        assertEquals("3 Edit(s) Pending", m_das.getAlignmentState(m_device1.getDeviceName()));
    }

    @Test
    public void testAlignedDeviceState() {
        m_das.alignAllDevices();
        assertEquals("Aligned", m_das.getAlignmentState(m_device1.getDeviceName()));
    }

    @Test
    public void testNeverAlignedDeviceState() {
        Device deviceX = createDevice("X");

        m_das.deviceAdded(deviceX.getDeviceName());
        Assert.assertEquals(NEVER_ALIGNED, deviceX.getDeviceManagement().getDeviceState().getConfigAlignmentState());
    }

    @Test
    public void testErrorDeviceState() throws ExecutionException, NetconfMessageBuilderException {
        makeDeviceError(m_edit1);
        String expected;

        if (isWindowsSys()) {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\r\n" +
                    "  <edit-config>\r\n" +
                    "    <target>\r\n" +
                    "      <running />\r\n" +
                    "    </target>\r\n" +
                    "    <test-option>set</test-option>\r\n" +
                    "    <config>\r\n" +
                    "      <some-config xmlns=\"some:ns\"/>\r\n" +
                    "    </config>\r\n" +
                    "  </edit-config>\r\n" +
                    "</rpc>,\r\n" +
                    "response received : <rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                    "  <rpc-error>\r\n" +
                    "    <error-type>application</error-type>\r\n" +
                    "    <error-tag>operation-failed</error-tag>\r\n" +
                    "    <error-severity>error</error-severity>\r\n" +
                    "    <error-message>Something went wrong</error-message>\r\n" +
                    "  </rpc-error>\r\n" +
                    "</rpc-reply>\r\n";
        }
        else {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                    "  <edit-config>\n" +
                    "    <target>\n" +
                    "      <running />\n" +
                    "    </target>\n" +
                    "    <test-option>set</test-option>\n" +
                    "    <config>\n" +
                    "      <some-config xmlns=\"some:ns\"/>\n" +
                    "    </config>\n" +
                    "  </edit-config>\n" +
                    "</rpc>,\n" +
                    "response received : <rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-message>Something went wrong</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>\n";
        }

        verify(m_dm).updateConfigAlignmentState(m_device1.getDeviceName(),expected);
    }

    @Test
    public void testErrorDeviceStateDueToTimeout() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        makeDeviceTimeout(m_edit1);
        String expected;

        if (isWindowsSys()) {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\r\n" +
                    "  <edit-config>\r\n" +
                    "    <target>\r\n" +
                    "      <running />\r\n" +
                    "    </target>\r\n" +
                    "    <test-option>set</test-option>\r\n" +
                    "    <config>\r\n" +
                    "      <some-config xmlns=\"some:ns\"/>\r\n" +
                    "    </config>\r\n" +
                    "  </edit-config>\r\n" +
                    "</rpc>,\r\n" +
                    "response received : request timed out";
        }
        else {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                    "  <edit-config>\n" +
                    "    <target>\n" +
                    "      <running />\n" +
                    "    </target>\n" +
                    "    <test-option>set</test-option>\n" +
                    "    <config>\n" +
                    "      <some-config xmlns=\"some:ns\"/>\n" +
                    "    </config>\n" +
                    "  </edit-config>\n" +
                    "</rpc>,\n" +
                    "response received : request timed out";
        }

        verify(m_dm).updateConfigAlignmentState(m_device1.getDeviceName(),expected);
    }

    @Test
    public void makeSureEditsAreNotQueuedAfterError() throws ExecutionException, NetconfMessageBuilderException {
        makeDeviceError(m_edit1);
        updateAlignment(m_device1.getDeviceName(),IN_ERROR);
        m_das.queueEdit(m_device1.getDeviceName(), m_edit5);
        m_das.alignAllDevices();
        verify(m_ncm, never()).executeNetconf(m_device1.getDeviceName(), m_edit5);
    }

    @Test
    public void testFullResyncExecutesCopyConfig() throws ExecutionException, NetconfMessageBuilderException {
        m_das.forceAlign(m_device1, m_getResponse);
        verify(m_deviceInterface).forceAlign(m_device1, m_getResponse);
        assertEquals("Aligned", m_das.getAlignmentState(m_device1.getDeviceName()));
    }

    @Test
    public void testFullResyncError() throws ExecutionException, NetconfMessageBuilderException {
        makeDeviceError(null);
        m_das.forceAlign(m_device1, m_getResponse);
        verify(m_deviceInterface).forceAlign(m_device1, m_getResponse);
        String expected;

        if (isWindowsSys()) {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\r\n" +
                    "  <copy-config>\r\n" +
                    "    <source>blah<source>\r\n" +
                    "\t<target>\r\n" +
                    "      <running />\r\n" +
                    "    </target>\r\n" +
                    "    <test-option>set</test-option>\r\n" +
                    "    <config>\r\n" +
                    "      <some-config xmlns=\"some:ns\"/>\r\n" +
                    "    </config>\r\n" +
                    "  </copy-config>\r\n" +
                    "</rpc>,\r\n" +
                    "response received : <rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                    "  <rpc-error>\r\n" +
                    "    <error-type>application</error-type>\r\n" +
                    "    <error-tag>operation-failed</error-tag>\r\n" +
                    "    <error-severity>error</error-severity>\r\n" +
                    "    <error-message>Something went wrong</error-message>\r\n" +
                    "  </rpc-error>\r\n" +
                    "</rpc-reply>\r\n";
        }
        else {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                    "  <copy-config>\n" +
                    "    <source>blah<source>\n" +
                    "\t<target>\n" +
                    "      <running />\n" +
                    "    </target>\n" +
                    "    <test-option>set</test-option>\n" +
                    "    <config>\n" +
                    "      <some-config xmlns=\"some:ns\"/>\n" +
                    "    </config>\n" +
                    "  </copy-config>\n" +
                    "</rpc>,\n" +
                    "response received : <rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-message>Something went wrong</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>\n";
        }

        verify(m_dm).updateConfigAlignmentState(m_device1.getDeviceName(),expected);

    }

    @Test
    public void testFullResyncTimeout() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        makeDeviceTimeout(null);
        m_das.forceAlign(m_device1, m_getResponse);
        verify(m_deviceInterface).forceAlign(m_device1, m_getResponse);

        String expected;

        if (isWindowsSys()) {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\r\n" +
                    "  <copy-config>\r\n" +
                    "    <source>blah<source>\r\n" +
                    "\t<target>\r\n" +
                    "      <running />\r\n" +
                    "    </target>\r\n" +
                    "    <test-option>set</test-option>\r\n" +
                    "    <config>\r\n" +
                    "      <some-config xmlns=\"some:ns\"/>\r\n" +
                    "    </config>\r\n" +
                    "  </copy-config>\r\n" +
                    "</rpc>,\r\n" +
                    "response received : request timed out";
        }
        else {
             expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                    "  <copy-config>\n" +
                    "    <source>blah<source>\n" +
                    "\t<target>\n" +
                    "      <running />\n" +
                    "    </target>\n" +
                    "    <test-option>set</test-option>\n" +
                    "    <config>\n" +
                    "      <some-config xmlns=\"some:ns\"/>\n" +
                    "    </config>\n" +
                    "  </copy-config>\n" +
                    "</rpc>,\n" +
                    "response received : request timed out";
        }

        verify(m_dm).updateConfigAlignmentState(m_device1.getDeviceName(), expected);
    }

    @Test
    public void testInitRegisterAndDestroyUnregistersStateProvider() {
        verify(m_dm, never()).addDeviceStateProvider(m_das);
        verify(m_dm, never()).removeDeviceStateProvider(m_das);

        m_das.init();
        verify(m_dm).addDeviceStateProvider(m_das);
        verify(m_dm, never()).removeDeviceStateProvider(m_das);

        m_das.destroy();
        verify(m_dm).addDeviceStateProvider(m_das);
        verify(m_dm).removeDeviceStateProvider(m_das);
    }

    @Test
    public void testDeviceAddedAndRemoved() {
        m_das.deviceAdded("deviceX");
        assertTrue(m_das.getQueue().containsKey("deviceX"));
        m_das.deviceRemoved("deviceX");
        assertFalse(m_das.getQueue().containsKey("deviceX"));
    }

    private void makeDeviceTimeout(EditConfigRequest request) throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        when(m_deviceInterface.forceAlign(m_device1, m_getResponse)).thenReturn(new Pair<>(m_cc ,m_notOkResponseFuture));
        when(m_deviceInterface.align(m_device1, request, m_pmaDsGetConfigResponse)).thenReturn(m_notOkResponseFuture);
        when(m_notOkResponseFuture.get()).thenReturn(null);
        m_das.alignAllDevices();
    }

    private void makeDeviceError(EditConfigRequest request) throws ExecutionException, NetconfMessageBuilderException {
        when(m_deviceInterface.forceAlign(m_device1, m_getResponse)).thenReturn(new Pair<>(m_cc ,m_notOkResponseFuture));
        when(m_deviceInterface.align(m_device1, request, m_pmaDsGetConfigResponse)).thenReturn(m_notOkResponseFuture);
        m_das.alignAllDevices();
    }

    private boolean isWindowsSys() {
        return System.getProperty("os.name").startsWith("Windows");
    }

}
