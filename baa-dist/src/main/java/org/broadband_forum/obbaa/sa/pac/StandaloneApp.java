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

package org.broadband_forum.obbaa.sa.pac;

import org.broadband_forum.obbaa.aggregator.processor.DeviceManagerAdapter;
import org.broadband_forum.obbaa.connectors.sbi.netconf.impl.NetconfConnectionManagerImpl;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.interceptor.DataStoreTransactionInterceptor;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManagementSubsystem;
import org.broadband_forum.obbaa.nm.devicemanager.impl.DeviceManagerImpl;
import org.broadband_forum.obbaa.pma.impl.AlignmentTimer;
import org.broadband_forum.obbaa.pma.impl.NetconfDeviceAlignmentServiceImpl;
import org.broadband_forum.obbaa.pma.impl.NetconfNetworkFunctionAlignmentServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by kbhatk on 3/10/17.
 */
@SpringBootApplication
public class StandaloneApp {

    private static AbstractApplicationContext m_fullApplicationContext;
    private static AbstractApplicationContext m_dmStackApplicationContext;
    private static NetconfServer m_dmNetconfServer;
    private static NetconfConnectionManagerImpl m_connMgr;
    private static DeviceManagementSubsystem m_deviceMgmtSubSystem;
    private static DeviceManagerAdapter m_deviceMgrAdapter;
    private static DeviceManagerImpl m_deviceMgr;
    private static DeviceDao m_deviceDao;
    private static NetworkFunctionDao m_networkFunctionDao;
    private static AlignmentTimer m_alignmentTimer;
    private static TxService m_txService;
    private static NetconfDeviceAlignmentServiceImpl m_deviceAlignSvc;
    private static NetconfNetworkFunctionAlignmentServiceImpl m_networkFunctionAlignSvc;
    private static DataStoreTransactionInterceptor m_dmStoreInterceptor;
    private static DataStoreTransactionInterceptor m_storeInterceptor;


    public static void main(String[] args) {
        m_fullApplicationContext = new ClassPathXmlApplicationContext("/application-context.xml");
        m_dmStackApplicationContext = new ClassPathXmlApplicationContext("/dm-stack-application-context.xml");

        m_connMgr = (NetconfConnectionManagerImpl) m_fullApplicationContext.getBean("ncConnectionMgr");
        m_deviceMgrAdapter = (DeviceManagerAdapter) m_fullApplicationContext.getBean("deviceManagerAdapter");
        m_deviceMgr = (DeviceManagerImpl) m_fullApplicationContext.getBean("deviceManager");
        m_alignmentTimer = (AlignmentTimer) m_fullApplicationContext.getBean("aligmentTimer");
        m_deviceAlignSvc = (NetconfDeviceAlignmentServiceImpl) m_fullApplicationContext.getBean("deviceAlignmentService");
        m_networkFunctionAlignSvc = (NetconfNetworkFunctionAlignmentServiceImpl) m_fullApplicationContext.getBean(
                "networkFunctionAlignmentService");
        m_storeInterceptor = (DataStoreTransactionInterceptor)m_fullApplicationContext.getBean("dataStoreTransactionInterceptor");
        m_dmNetconfServer = (NetconfServer) m_dmStackApplicationContext.getBean("netconfServer");
        m_deviceMgmtSubSystem = (DeviceManagementSubsystem) m_dmStackApplicationContext.getBean("deviceManagementSubSystem");
        m_deviceDao = (DeviceDao) m_dmStackApplicationContext.getBean("deviceDao");
        m_networkFunctionDao = (NetworkFunctionDao) m_dmStackApplicationContext.getBean("networkFunctionDao");
        m_txService = (TxService) m_dmStackApplicationContext.getBean("txService");
        m_dmStoreInterceptor = (DataStoreTransactionInterceptor)m_dmStackApplicationContext.getBean("dataStoreTransactionInterceptor");

        //cross wire
        m_storeInterceptor.setTxService(m_txService);
        m_dmStoreInterceptor.setTxService(m_txService);
        m_alignmentTimer.setDeviceDao(m_deviceDao);
        m_alignmentTimer.setTxService(m_txService);
        m_connMgr.setDeviceDao(m_deviceDao);
        m_connMgr.setNetworkFunctionDao(m_networkFunctionDao);
        m_connMgr.setTxService(m_txService);
        m_deviceMgr.setDeviceDao(m_deviceDao);
        m_deviceAlignSvc.setTxService(m_txService);
        m_networkFunctionAlignSvc.setTxService(m_txService);
        m_deviceMgmtSubSystem.setConnectionManager(m_connMgr);
        m_deviceMgmtSubSystem.setDeviceManager(m_deviceMgr);
        m_deviceMgrAdapter.setDmNetconfServer(m_dmNetconfServer);

        SpringApplication.run(StandaloneApp.class, args);
    }
}
