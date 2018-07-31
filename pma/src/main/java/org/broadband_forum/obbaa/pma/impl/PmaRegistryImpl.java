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

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.pma.DeviceModelDeployer;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;
import org.broadband_forum.obbaa.pma.PmaSessionTemplate;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;

/**
 * <p>
 * Acts as a "pass through" Persistent Management Agent.
 * Will execute the requests directly on the device and return the response.
 * If the device is not connected, throws an exception.
 * </p>
 * Created by kbhatk on 8/10/17.
 */
public class PmaRegistryImpl implements PmaRegistry {
    private static final Logger LOGGER = Logger.getLogger(PmaRegistryImpl.class);
    public static final String COMPONENT_ID = "pma";
    private final DeviceManager m_deviceManager;
    private final DeviceModelDeployer m_dmd;
    private GenericKeyedObjectPool<String, PmaSession> m_pmaSessionPool;
    private PmaSessionFactory m_factory;

    public PmaRegistryImpl(DeviceManager deviceManager, NetconfConnectionManager connectionManager,
                           DeviceModelDeployer dmd, PmaSessionFactory factory) {
        m_deviceManager = deviceManager;
        m_dmd = dmd;
        m_factory = factory;
        setupPmaSessionPool();
    }

    private void setupPmaSessionPool() {
        m_pmaSessionPool = new GenericKeyedObjectPool<>(m_factory);
        m_pmaSessionPool.setTimeBetweenEvictionRunsMillis(10000); //evict old pmasessions every 10 secs
        m_pmaSessionPool.setMaxIdlePerKey(1);
        m_pmaSessionPool.setMinIdlePerKey(1);
    }

    @Override
    public String executeNC(String deviceName, String netconfRequest) throws IllegalArgumentException,
            IllegalStateException, ExecutionException {
        return executeWithPmaSession(deviceName, session -> {
            return session.executeNC(netconfRequest);
        });
    }

    @Override
    public <RT> RT executeWithPmaSession(String deviceName, PmaSessionTemplate<RT> sessionTemplate)
            throws IllegalArgumentException, IllegalStateException, ExecutionException {
        PmaSession pmaSession = getPmaSession(deviceName);
        try {
            return sessionTemplate.execute(pmaSession);
        } finally {
            if (pmaSession != null) {
                m_pmaSessionPool.returnObject(deviceName, pmaSession);
            }
        }
    }

    @Override
    public void forceAlign(String deviceName) throws ExecutionException {
        executeWithPmaSession(deviceName, (PmaSessionTemplate<Void>) session -> {
            session.forceAlign();
            return null;
        });
    }

    @Override
    public void align(String deviceName) throws ExecutionException {
        executeWithPmaSession(deviceName, (PmaSessionTemplate<Void>) session -> {
            session.align();
            return null;
        });
    }

    @Override
    public List<String> reloadDeviceModel() {
        return m_dmd.redeploy();
    }

    private PmaSession getPmaSession(String deviceName) throws IllegalArgumentException, IllegalStateException {
        DeviceInfo deviceInfo = m_deviceManager.getDevice(deviceName);
        if (deviceInfo == null) {
            throw new IllegalArgumentException(String.format("Device not managed : %s", deviceName));
        }

       /* if (!m_connectionManager.isConnected(deviceInfo)) {
            throw new IllegalStateException(String.format("Device not connected : %s", deviceName));
        }*/
        PmaSession session = null;
        try {
            session = m_pmaSessionPool.borrowObject(deviceName);
            return session;
        } catch (Exception e) {
            LOGGER.error(String.format("Could not get session to Pma %s", deviceName));
            throw new RuntimeException(String.format("Could not get session to Pma %s", deviceName), e);
        }
    }

    public void deviceRemoved(String deviceName) {
        m_pmaSessionPool.clear(deviceName);
        m_factory.deviceDeleted(deviceName);
    }
}
