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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.pma.DeviceModelDeployer;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 8/10/17.
 */
public class PmaRegistryImplTest {
    private static final String OK_RESPONSE = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <ok/>\n" +
            "</rpc-reply>\n";
    private PmaRegistryImpl m_pmaRegistry;
    @Mock
    private NetconfConnectionManager m_cm;
    @Mock
    private DeviceManager m_dm;
    @Mock
    private DeviceInfo m_device1Meta;
    @Mock
    private List<String> m_moduleList;
    @Mock
    private DeviceModelDeployer m_modelDeployer;
    private NetConfResponse m_response = new NetConfResponse().setOk(true).setMessageId("1");

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_pmaRegistry = new PmaRegistryImpl(m_dm, m_cm,
                m_modelDeployer, new TransparentPmaSessionFactory(m_cm, m_dm));
        when(m_cm.isConnected(m_device1Meta)).thenReturn(true);
        when(m_dm.getDevice("device1")).thenReturn(m_device1Meta);
        Future<NetConfResponse> futureObject = mock(Future.class);
        when(futureObject.get()).thenReturn(m_response);
        when(m_cm.executeNetconf((DeviceInfo) anyObject(), anyObject())).thenReturn(futureObject);
        when(m_modelDeployer.redeploy()).thenReturn(m_moduleList);
    }

    @Test
    public void testExceptionIsThrownWhenDeviceNotManaged() throws ExecutionException {
        try{
            m_pmaRegistry.executeNC("device2", "");
            fail("Expected an exception here");
        }catch (IllegalArgumentException e){
            assertEquals("Device not managed : device2", e.getMessage());
        }
    }

    @Ignore("This is applicable only in transparent mode")
    @Test
    public void testExceptionIsThrownWhenNoConnectionToDevice() throws ExecutionException {
        when(m_cm.isConnected(m_device1Meta)).thenReturn(false);
        try{
            m_pmaRegistry.executeNC("device1", "");
            fail("Expected an exception here");
        }catch (IllegalStateException e){
            assertEquals("Device not connected : device1", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithPmaSession() throws ExecutionException {
        String response = m_pmaRegistry.executeWithPmaSession("device1", session -> "response");
        assertEquals("response", response);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFullResyncContactsDAS() throws ExecutionException {
        m_pmaRegistry.forceAlign("device1");
    }

    @Test
    public void testDeviceModelReloadContactsDeviceModelDeployer(){
        assertEquals(m_moduleList, m_pmaRegistry.reloadDeviceModel());
        verify(m_modelDeployer).redeploy();
    }

}
