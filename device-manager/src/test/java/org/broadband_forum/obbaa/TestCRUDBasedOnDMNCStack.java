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

package org.broadband_forum.obbaa;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dm.DeviceManagementSubsystem;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.interceptor.DataStoreTransactionInterceptor;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestCRUDBasedOnDMNCStack {

    private static final Logger LOGGER = Logger.getLogger(TestCRUDBasedOnDMNCStack.class);
    private AbstractApplicationContext m_applicationContext;
    private NetconfServer m_netconfServer;
    @Mock
    private NetconfClientInfo m_clientInfo;
    private EditConfigRequest m_createDevice;
    private GetConfigRequest m_getConfig;
    private EditConfigRequest m_deleteDevice;
    @Mock
    private DeviceManager m_deviceManager;
    private DeviceManagementSubsystem m_deviceMgmtS;
    private PersistenceManagerUtil m_pmUtil;

    @Before
    public void setUp() throws NetconfMessageBuilderException {
        MockitoAnnotations.initMocks(this);
        m_applicationContext = new ClassPathXmlApplicationContext("/test-application-context.xml");
        m_netconfServer = (NetconfServer) m_applicationContext.getBean("netconfServer");
        TxService txService = (TxService) m_applicationContext.getBean("txService");
        m_deviceMgmtS = (DeviceManagementSubsystem) m_applicationContext.getBean("deviceManagementSubSystem");
        m_pmUtil = (PersistenceManagerUtil) m_applicationContext.getBean("persistenceMgrUtil");
        DataStoreTransactionInterceptor dataStoreTransactionInterceptor = (DataStoreTransactionInterceptor) m_applicationContext.getBean("dataStoreTransactionInterceptor");
        dataStoreTransactionInterceptor.setTxService(txService);
        m_deviceMgmtS.setDeviceManager(m_deviceManager);
        m_createDevice = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(load("/createDevice.xml")));
        m_deleteDevice = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(load("/deleteDevice.xml")));
        m_getConfig = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument(load("/getConfig.xml")));
    }

    @Test
    @Ignore
    public void testDeviceNotPresentToDeleteDevice() {
        m_pmUtil.getEntityDataStoreManager().beginTransaction();
        assertEquals(load("/data-missing-error-response.xml"), deleteDevice().responseToString());
        m_pmUtil.getEntityDataStoreManager().commitTransaction();
    }

    @Test
    @Ignore
    public void testDeviceAlreadyExistsErrorDuringCreateDevice() {
        m_pmUtil.getEntityDataStoreManager().beginTransaction();
        assertEquals(load("/ok-response.xml"), createDevice().responseToString());
        assertEquals(load("/device-already-exists-error-response.xml"), createDevice().responseToString());
        m_pmUtil.getEntityDataStoreManager().commitTransaction();
    }

    @Test
    @Ignore
    public void testEmptyGetconfig() {
        m_pmUtil.getEntityDataStoreManager().beginTransaction();
        assertEquals(load("/getConfigNoDeviceResponse.xml"), getConfig().responseToString());
        m_pmUtil.getEntityDataStoreManager().commitTransaction();
    }

    @Test
    @Ignore
    public void testCreateAndDeleteDevice() {
        m_pmUtil.getEntityDataStoreManager().beginTransaction();
        assertEquals(load("/ok-response.xml"), createDevice().responseToString());
        assertEquals(load("/getConfigCreateResponse.xml"), getConfig().responseToString());
        assertEquals(load("/ok-response.xml"), deleteDevice().responseToString());
        m_pmUtil.getEntityDataStoreManager().commitTransaction();
        //assertEquals(load("/getConfigNoDeviceResponse.xml"), getConfig().responseToString());
    }

    private NetConfResponse getConfig() {
        NetConfResponse getConfigResponse = new NetConfResponse();
        m_netconfServer.onGetConfig(m_clientInfo, m_getConfig, getConfigResponse);
        return getConfigResponse;
    }

    private NetConfResponse createDevice() {
        NetConfResponse createResponse = new NetConfResponse();
        m_netconfServer.onEditConfig(m_clientInfo, m_createDevice, createResponse);
        return createResponse;
    }

    private NetConfResponse deleteDevice() {
        NetConfResponse deleteResponse = new NetConfResponse();
        m_netconfServer.onEditConfig(m_clientInfo, m_deleteDevice, deleteResponse);
        return deleteResponse;
    }

    private String load(String name) {
        StringBuffer sb = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TestCRUDBasedOnDMNCStack.class.getResourceAsStream(name)))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
