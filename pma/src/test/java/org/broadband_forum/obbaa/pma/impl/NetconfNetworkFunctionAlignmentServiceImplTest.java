/*
 * Copyright 2022 Broadband Forum
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

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.dmyang.entities.PmaResource;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ALIGNED;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.IN_ERROR;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEVER_ALIGNED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NetconfNetworkFunctionAlignmentServiceImplTest {

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

    private static final String NETWORK_FUNCTION_1 = "networkFunction1";
    private static final String NETWORK_FUNCTION_2 = "networkFunction2";


    NetconfNetworkFunctionAlignmentServiceImpl m_nas;
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
    private Future<NetConfResponse> m_okResponseFuture;
    private NetConfResponse m_okResponse;
    @Mock
    private Future<NetConfResponse> m_notOkResponseFuture;
    private NetConfResponse m_notOkResponse;
    @Mock
    private EditConfigRequest m_edit5;
    @Mock
    private CopyConfigRequest m_cc;
    @Mock
    private NetworkFunctionManager m_nfm;

    private NetworkFunction m_networkFunction1;

    private NetworkFunction m_networkFunction2;
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
    @Mock
    private NetConfResponse m_getResponse;

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

        when(m_manager.getAdapterContext(any())).thenReturn(m_context);
        when(m_ncm.executeNetconf(any(PmaResource.class), anyObject())).thenReturn(m_okResponseFuture);
        m_okResponse = new NetConfResponse().setOk(true);
        m_okResponse.setMessageId("1");
        when(m_okResponseFuture.get()).thenReturn(m_okResponse);
        m_notOkResponse = new NetConfResponse().setOk(false);
        m_notOkResponse.addError(NetconfRpcError.getApplicationError("Something went wrong"));
        m_notOkResponse.setMessageId("1");
        when(m_notOkResponseFuture.get()).thenReturn(m_notOkResponse);
        m_nas = new NetconfNetworkFunctionAlignmentServiceImpl(m_nfm, m_ncm);
        m_nas.setTxService(m_txService);
        m_networkFunction1 = createNetworkFunction(NETWORK_FUNCTION_1);
        m_networkFunction2 = createNetworkFunction(NETWORK_FUNCTION_2);
        when(m_nfm.getNetworkFunction(NETWORK_FUNCTION_1)).thenReturn(m_networkFunction1);
        when(m_nfm.getNetworkFunction(NETWORK_FUNCTION_2)).thenReturn(m_networkFunction2);
        when(m_ncm.isConnected(any(PmaResource.class))).thenReturn(true);
        m_nas.networkFunctionAdded(m_networkFunction2.getNetworkFunctionName());
        m_nas.networkFunctionAdded(m_networkFunction1.getNetworkFunctionName());

    }

    private void updateAlignment(String networkFunctionName , String verdict) {
        m_nfm.getNetworkFunction(networkFunctionName).setAlignmentState(verdict);
    }

    private NetworkFunction createNetworkFunction(String networkFunctionName) {
        NetworkFunction networkFunction = new NetworkFunction();
        networkFunction.setNetworkFunctionName(networkFunctionName);
        return networkFunction;
    }

    @Test
    public void testForceAlignUpdatesAlignment() {
        m_nas.forceAlign(m_networkFunction1, mock(NetConfResponse.class));
        verify(m_nfm).updateConfigAlignmentState(m_networkFunction1.getNetworkFunctionName() , ALIGNED);
        m_nas.forceAlign(m_networkFunction2, mock(NetConfResponse.class));
        verify(m_nfm).updateConfigAlignmentState(m_networkFunction2.getNetworkFunctionName() , ALIGNED);
    }

    @Test
    public void testNASQueuesRequestsInOrder() throws ExecutionException {
        List<EditConfigRequest> edits = new ArrayList<>();
        makeOk();

        updateAlignment(m_networkFunction1.getNetworkFunctionName(), ALIGNED);
        updateAlignment(m_networkFunction2.getNetworkFunctionName(), ALIGNED);

        m_nas.queueEdit(m_networkFunction1.getNetworkFunctionName(), m_edit1);
        m_nas.queueEdit(m_networkFunction2.getNetworkFunctionName(), m_edit2);
        m_nas.queueEdit(m_networkFunction1.getNetworkFunctionName(), m_edit3);
        m_nas.queueEdit(m_networkFunction2.getNetworkFunctionName(), m_edit4);

        edits.add(m_edit1);
        edits.add(m_edit3);
        assertEquals(edits, m_nas.getEditQueue(m_networkFunction1.getNetworkFunctionName()));
        edits.clear();

        edits.add(m_edit2);
        edits.add(m_edit4);
        assertEquals(edits, m_nas.getEditQueue(m_networkFunction2.getNetworkFunctionName()));
    }

    @Test
    public void testNASFlush() throws ExecutionException {
        m_nas.alignAllNetworkFunctions();
        assertEquals(0, m_nas.getEditQueue(m_networkFunction1.getNetworkFunctionName()).size());
        assertEquals(0, m_nas.getEditQueue(m_networkFunction2.getNetworkFunctionName()).size());
        verify(m_nfm).updateConfigAlignmentState(m_networkFunction2.getNetworkFunctionName(), ALIGNED);
        verify(m_nfm).updateConfigAlignmentState(m_networkFunction1.getNetworkFunctionName(), ALIGNED);

    }

    @Test
    public void makeSureErrorResponseClearsRestOfEdits() throws ExecutionException, NetconfMessageBuilderException {
        makeError();
        m_nas.alignAllNetworkFunctions();
        assertEquals(0, m_nas.getEditQueue(m_networkFunction1.getNetworkFunctionName()).size());
        assertEquals(0, m_nas.getEditQueue(m_networkFunction2.getNetworkFunctionName()).size());
    }

    @Test
    public void makeSureTimedoutResponseClearsRestOfEdits() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        makeTimeout();
        m_nas.alignAllNetworkFunctions();
        assertEquals(0, m_nas.getEditQueue(m_networkFunction1.getNetworkFunctionName()).size());
        assertEquals(0, m_nas.getEditQueue(m_networkFunction2.getNetworkFunctionName()).size());
    }

    @Test
    public void testDeviceState() throws ExecutionException {
        makeOk();
        String networkFunctionName = m_networkFunction1.getNetworkFunctionName();
        m_nas.alignAllNetworkFunctions();
        updateAlignment(networkFunctionName,ALIGNED);
        m_nas.getQueue().get(networkFunctionName).clear();
        m_nas.queueEdit(networkFunctionName,m_edit1);
        assertEquals("1 Edits Pending", m_nas.getAlignmentState(networkFunctionName));
        m_nas.queueEdit(networkFunctionName, m_edit2);
        assertEquals("2 Edits Pending", m_nas.getAlignmentState(networkFunctionName));
    }

    @Test
    public void testAlignedDeviceState() throws ExecutionException {
        makeOk();
        m_nas.alignAllNetworkFunctions();
        assertEquals(ALIGNED, m_nas.getAlignmentState(m_networkFunction1.getNetworkFunctionName()));
        assertEquals(ALIGNED, m_nas.getAlignmentState(m_networkFunction2.getNetworkFunctionName()));
    }

    @Test
    public void testNeverAlignedDeviceState() {
        NetworkFunction networkFunctionX = createNetworkFunction("X");

        m_nas.networkFunctionAdded(networkFunctionX.getNetworkFunctionName());
        Assert.assertEquals(NEVER_ALIGNED, networkFunctionX.getAlignmentState());
    }

    @Test
    public void testErrorDeviceState() throws ExecutionException, NetconfMessageBuilderException {
        makeError();
        //ensures align makes network function be marked as in error
        m_nas.getQueue().get(m_networkFunction1.getNetworkFunctionName()).add(m_edit1);
        m_nas.align(m_networkFunction1,m_getResponse);
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

        verify(m_nfm).updateConfigAlignmentState(m_networkFunction1.getNetworkFunctionName(),expected);
    }

    @Test
    public void testErrorDeviceStateDueToTimeout() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        makeTimeout();
        //ensures align makes network function be marked as in error
        m_nas.getQueue().get(m_networkFunction1.getNetworkFunctionName()).add(m_edit1);
        m_nas.align(m_networkFunction1,m_getResponse);

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
        verify(m_nfm).updateConfigAlignmentState(m_networkFunction1.getNetworkFunctionName(),expected);
    }

    @Test
    public void makeSureEditsAreNotQueuedAfterError() throws ExecutionException, NetconfMessageBuilderException {
        makeError();
        String networkFunctionName = m_networkFunction1.getNetworkFunctionName();
        m_nas.alignAllNetworkFunctions();
        m_nas.queueEdit(networkFunctionName, m_edit1);
        assertEquals(0,m_nas.getEditQueue(networkFunctionName).size());
        verify(m_ncm, never()).executeNetconf(networkFunctionName, m_edit1);
    }

    @Test
    public void testFullResyncExecutesCopyConfig() throws ExecutionException, NetconfMessageBuilderException {
        m_nas.forceAlign(m_networkFunction1, m_getResponse);
        assertEquals("Aligned", m_nas.getAlignmentState(m_networkFunction1.getNetworkFunctionName()));
    }

    @Test
    public void testFullResyncError() throws ExecutionException, NetconfMessageBuilderException {
        makeError();
        m_nas.forceAlign(m_networkFunction1, m_getResponse);
        String expected;

        if (isWindowsSys()) {
            expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                    "  <copy-config>\r\n" +
                    "    <target>\r\n" +
                    "      <running/>\r\n" +
                    "    </target>\r\n" +
                    "    <source>\r\n" +
                    "      <config/>\r\n" +
                    "    </source>\r\n" +
                    "  </copy-config>\r\n" +
                    "</rpc>\r\n,\r\n" +
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
            expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <copy-config>\n" +
                    "    <target>\n" +
                    "      <running/>\n" +
                    "    </target>\n" +
                    "    <source>\n" +
                    "      <config/>\n" +
                    "    </source>\n" +
                    "  </copy-config>\n" +
                    "</rpc>\n,\n" +
                    "response received : <rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>application</error-type>\n" +
                    "    <error-tag>operation-failed</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-message>Something went wrong</error-message>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply>\n";
        }

        verify(m_nfm).updateConfigAlignmentState(m_networkFunction1.getNetworkFunctionName(),expected);

    }

    @Test
    public void testFullResyncTimeout() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        makeTimeout();
        m_nas.forceAlign(m_networkFunction1, m_getResponse);
        String expected;

        if (isWindowsSys()) {
            expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\r\n" +
                    "  <copy-config>\r\n" +
                    "    <target>\r\n" +
                    "      <running/>\r\n" +
                    "    </target>\r\n" +
                    "    <source>\r\n" +
                    "      <config/>\r\n" +
                    "    </source>\r\n" +
                    "  </copy-config>\r\n" +
                    "</rpc>\r\n" +
                    ",\r\n" +
                    "response received : request timed out";
        }
        else {
            expected = "In Error, request sent : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <copy-config>\n" +
                    "    <target>\n" +
                    "      <running/>\n" +
                    "    </target>\n" +
                    "    <source>\n" +
                    "      <config/>\n" +
                    "    </source>\n" +
                    "  </copy-config>\n" +
                    "</rpc>\n" +
                    ",\n" +
                    "response received : request timed out";
        }

        verify(m_nfm).updateConfigAlignmentState(m_networkFunction1.getNetworkFunctionName(), expected);
    }

    @Test
    public void testInitRegisterAndDestroyUnregistersStateProvider() {
        //init and destroy currently do nothing
    }

    @Test
    public void testDeviceAddedAndRemoved() {
        String networkFunctionName = "nfx";
        m_nas.networkFunctionAdded(networkFunctionName);
        assertTrue(m_nas.getQueue().containsKey(networkFunctionName));
        m_nas.networkFunctionRemoved(networkFunctionName);
        assertFalse(m_nas.getQueue().containsKey(networkFunctionName));
    }

    private void makeTimeout() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        when(m_ncm.executeNetconf(any(PmaResource.class),any())).thenReturn(m_notOkResponseFuture);
        when(m_notOkResponseFuture.get()).thenReturn(null);
    }

    private void makeError() throws ExecutionException, NetconfMessageBuilderException {
        when(m_ncm.executeNetconf(any(PmaResource.class),any())).thenReturn(m_notOkResponseFuture);
    }

    private void makeOk() throws ExecutionException {
        when(m_ncm.executeNetconf(any(PmaResource.class),any())).thenReturn(m_okResponseFuture);
    }

    private boolean isWindowsSys() {
        return System.getProperty("os.name").startsWith("Windows");
    }

}
