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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentInfo;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
    private DeviceAlignmentInfo m_device1Info;
    @Mock
    private DeviceAlignmentInfo m_device2Info;
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
    private DeviceAlignmentStore m_alignmentStore;
    @Mock
    private DeviceManager m_dm;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        MockitoAnnotations.initMocks(this);
        when(m_edit1.requestToString()).thenReturn(EDIT_REQ_STR);
        when(m_edit2.requestToString()).thenReturn(EDIT_REQ_STR);
        when(m_edit3.requestToString()).thenReturn(EDIT_REQ_STR);
        when(m_edit4.requestToString()).thenReturn(EDIT_REQ_STR);
        when(m_edit5.requestToString()).thenReturn(EDIT_REQ_STR);
        when(m_cc.requestToString()).thenReturn(CC_REQ_STR);
        when(m_ncm.executeNetconf(anyString(), anyObject())).thenReturn(m_responseFutureObject);
        m_okResponse = new NetConfResponse().setOk(true);
        m_okResponse.setMessageId("1");
        when(m_responseFutureObject.get()).thenReturn(m_okResponse);
        m_notOkResponse = new NetConfResponse().setOk(false);
        m_notOkResponse.addError(NetconfRpcError.getApplicationError("Something went wrong"));
        m_notOkResponse.setMessageId("1");
        when(m_notOkResponseFuture.get()).thenReturn(m_notOkResponse);
        m_das = new NetconfDeviceAlignmentServiceImpl(m_dm, m_ncm, m_alignmentStore);
        mockNameKeyCalls(m_device1Info, "device1");
        mockNameKeyCalls(m_device2Info, "device2");
        when(m_alignmentStore.get("device1")).thenReturn(m_device1Info);
        when(m_alignmentStore.get("device2")).thenReturn(m_device2Info);
        m_das.deviceAdded(m_device1Info.getName());
        m_das.deviceAdded(m_device2Info.getName());
        m_das.forceAlign(m_device1Info.getName(), mock(CopyConfigRequest.class));
        m_das.forceAlign(m_device2Info.getName(), mock(CopyConfigRequest.class));
        m_das.queueEdit(m_device1Info.getName(), m_edit1);
        m_das.queueEdit(m_device2Info.getName(), m_edit2);
        m_das.queueEdit(m_device1Info.getName(), m_edit3);
        m_das.queueEdit(m_device2Info.getName(), m_edit4);
    }

    private void mockNameKeyCalls(DeviceAlignmentInfo deviceInfo, String deviceName) {
        when(deviceInfo.getName()).thenReturn(deviceName);
        when(deviceInfo.getKey()).thenReturn(deviceName);
    }

    @Test
    public void testDASQueuesRequestsInOrder(){
        List<EditConfigRequest> edits = new ArrayList<>();

        edits.add(m_edit1);
        edits.add(m_edit3);
        assertEquals(edits, m_das.getEditQueue(m_device1Info.getName()));
        edits.clear();

        edits.add(m_edit2);
        edits.add(m_edit4);
        assertEquals(edits, m_das.getEditQueue(m_device2Info.getName()));
    }

    @Test
    public void testDASContactsNCMForFlushing() throws ExecutionException {
        m_das.alignAllDevices();
        assertEquals(0, m_das.getEditQueue(m_device1Info.getName()).size());
        assertEquals(0, m_das.getEditQueue(m_device2Info.getName()).size());
        verify(m_ncm).executeNetconf(m_device1Info.getName(), m_edit1);
        verify(m_ncm).executeNetconf(m_device2Info.getName(), m_edit2);
        verify(m_ncm).executeNetconf(m_device1Info.getName(), m_edit3);
        verify(m_ncm).executeNetconf(m_device2Info.getName(), m_edit4);
    }

    @Test
    public void makeSureErrorResponseClearsRestOfEdits() throws ExecutionException {
        makeDeviceError(m_edit1);
        assertEquals(0, m_das.getEditQueue(m_device1Info.getName()).size());
        assertEquals(0, m_das.getEditQueue(m_device2Info.getName()).size());
        verify(m_ncm).executeNetconf(m_device1Info.getName(), m_edit1);
        verify(m_ncm).executeNetconf(m_device2Info.getName(), m_edit2);
        verify(m_ncm, never()).executeNetconf(m_device1Info.getName(), m_edit3);
        verify(m_ncm).executeNetconf(m_device2Info.getName(), m_edit4);
    }

    @Test
    public void makeSureTimedoutResponseClearsRestOfEdits() throws ExecutionException, InterruptedException {
        makeDeviceTimeout(m_edit1);
        assertEquals(0, m_das.getEditQueue(m_device1Info.getName()).size());
        assertEquals(0, m_das.getEditQueue(m_device2Info.getName()).size());
        verify(m_ncm).executeNetconf(m_device1Info.getName(), m_edit1);
        verify(m_ncm).executeNetconf(m_device2Info.getName(), m_edit2);
        verify(m_ncm, never()).executeNetconf(m_device1Info.getName(), m_edit3);
        verify(m_ncm).executeNetconf(m_device2Info.getName(), m_edit4);
    }

    @Test
    public void testDeviceState(){
        assertEquals("2 Edit(s) Pending", m_das.getAlignmentState(m_device1Info.getName()));
        m_das.queueEdit(m_device1Info.getName(), m_edit1);
        assertEquals("3 Edit(s) Pending", m_das.getAlignmentState(m_device1Info.getName()));
    }

    @Test
    public void testAlignedDeviceState() {
        m_das.alignAllDevices();
        assertEquals("Aligned", m_das.getAlignmentState(m_device1Info.getName()));
    }

    @Test
    public void testNeverAlignedDeviceState() {
        DeviceAlignmentInfo deviceInfo = new DeviceAlignmentInfo("X");
        when(m_alignmentStore.get("X")).thenReturn(deviceInfo);
        m_das.deviceAdded(deviceInfo.getName());
        Assert.assertEquals(DeviceAlignmentInfo.NEVER_ALIGNED, deviceInfo.getVerdict());
        verify(m_alignmentStore).create(deviceInfo);
    }

    @Test
    public void testErrorDeviceState() throws ExecutionException {
        makeDeviceError(m_edit1);
        verify(m_device1Info).setVerdict("In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
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
                "</rpc-reply>\n");
    }

    @Test
    public void testErrorDeviceStateDueToTimeout() throws ExecutionException, InterruptedException {
        makeDeviceTimeout(m_edit1);
        verify(m_device1Info).setVerdict("In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
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
                "response received : request timed out");
    }

    @Test
    public void makeSureEditsAreNotQueuedAfterError() throws ExecutionException {
        makeDeviceError(m_edit1);
        m_das.queueEdit(m_device1Info.getName(), m_edit5);
        m_das.alignAllDevices();
        verify(m_ncm, never()).executeNetconf(m_device1Info.getKey(), m_edit5);
    }

    @Test
    public void testFullResyncExecutesCopyConfig() throws ExecutionException {
        m_das.forceAlign(m_device1Info.getName(), m_cc);
        verify(m_ncm).executeNetconf(m_device1Info.getKey(), m_cc);
        assertEquals("Aligned", m_das.getAlignmentState(m_device1Info.getName()));
    }

    @Test
    public void testFullResyncError() throws ExecutionException {
        makeDeviceError(m_cc);
        m_das.forceAlign(m_device1Info.getName(), m_cc);
        verify(m_ncm).executeNetconf(m_device1Info.getKey(), m_cc);
        verify(m_device1Info).setVerdict("In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
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
                "</rpc-reply>\n");
    }

    @Test
    public void testFullResyncTimeout() throws ExecutionException, InterruptedException {
        makeDeviceTimeout(m_cc);
        m_das.forceAlign(m_device1Info.getName(), m_cc);
        verify(m_ncm).executeNetconf(m_device1Info.getKey(), m_cc);
        verify(m_device1Info).setVerdict("In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
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
                "response received : request timed out");
    }

    @Test
    public void testInitRegisterAndDestroyUnregistersStateProvider(){
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
    public void testDeviceAddedAndRemoved(){
        m_das.deviceAdded("deviceX");
        ArgumentCaptor<DeviceAlignmentInfo> captor = ArgumentCaptor.forClass(DeviceAlignmentInfo.class);
        verify(m_alignmentStore, atLeastOnce()).create(captor.capture());
        assertEquals("deviceX", captor.getValue().getName());
        Assert.assertEquals(DeviceAlignmentInfo.NEVER_ALIGNED, captor.getValue().getVerdict());

        m_das.deviceRemoved("deviceX");
        verify(m_alignmentStore).delete("deviceX");
    }

    private void makeDeviceTimeout(AbstractNetconfRequest request) throws ExecutionException, InterruptedException {
        when(m_ncm.executeNetconf(m_device1Info.getKey(), request)).thenReturn(m_notOkResponseFuture);
        when(m_notOkResponseFuture.get()).thenReturn(null);
        m_das.alignAllDevices();
    }

    private void makeDeviceError(AbstractNetconfRequest request) throws ExecutionException {
        when(m_ncm.executeNetconf(m_device1Info.getKey(), request)).thenReturn(m_notOkResponseFuture);
        when(m_device1Info.isInError()).thenReturn(true);
        m_das.alignAllDevices();
    }
}
