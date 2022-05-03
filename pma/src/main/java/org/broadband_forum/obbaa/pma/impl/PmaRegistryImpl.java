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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.transaction.Transactional;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionManager;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;
import org.broadband_forum.obbaa.pma.PmaSessionTemplate;

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
    private final NetworkFunctionManager m_networkFunctionManager;
    private GenericKeyedObjectPool<PmaResourceId, PmaSession> m_pmaSessionPool;
    private PmaSessionFactory m_factory;

    public PmaRegistryImpl(DeviceManager deviceManager, NetworkFunctionManager networkFunctionManager,
                           PmaSessionFactory factory) {
        m_deviceManager = deviceManager;
        m_networkFunctionManager = networkFunctionManager;
        m_factory = factory;
        setupPmaSessionPool();
    }

    private void setupPmaSessionPool() {
        m_pmaSessionPool = new GenericKeyedObjectPool<PmaResourceId, PmaSession>(m_factory);
        m_pmaSessionPool.setTimeBetweenEvictionRunsMillis(10000); //evict old pmasessions every 10 secs
        m_pmaSessionPool.setMaxIdlePerKey(1);
        m_pmaSessionPool.setMinIdlePerKey(1);
        m_pmaSessionPool.setTestOnBorrow(true);
        m_pmaSessionPool.setTestOnReturn(true);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class, Exception.class})
    public Map<NetConfResponse, List<Notification>> executeNC(PmaResourceId resourceId, String netconfRequest)
            throws IllegalArgumentException, IllegalStateException, ExecutionException {
        return executeWithPmaSession(resourceId, session -> session.executeNC(netconfRequest));
    }

    @Override
    public <RT> RT executeWithPmaSession(PmaResourceId resourceId, PmaSessionTemplate<RT> sessionTemplate)
            throws IllegalArgumentException, IllegalStateException, ExecutionException {
        PmaSession pmaSession = getPmaSession(resourceId);
        try {
            return sessionTemplate.execute(pmaSession);
        } finally {
            if (pmaSession != null) {
                m_pmaSessionPool.returnObject(resourceId, pmaSession);
            }
        }
    }

    @Override
    public void forceAlign(PmaResourceId resourceId) throws ExecutionException {
        executeWithPmaSession(resourceId, (PmaSessionTemplate<Void>) session -> {
            session.forceAlign();
            return null;
        });
    }

    @Override
    public void align(PmaResourceId resourceId) throws ExecutionException {
        executeWithPmaSession(resourceId, (PmaSessionTemplate<Void>) session -> {
            session.align();
            return null;
        });
    }

    private PmaSession getPmaSession(PmaResourceId resourceId) throws IllegalArgumentException, IllegalStateException {

        PmaSession session;
        switch (resourceId.getResourceType()) {
            case DEVICE:
                Device device = m_deviceManager.getDevice(resourceId.getResourceName());
                if (device == null) {
                    throw new IllegalArgumentException(String.format("Device not managed : %s", resourceId.getResourceName()));
                }

       /* if (!m_connectionManager.isConnected(deviceInfo)) {
            throw new IllegalStateException(String.format("Device not connected : %s", deviceName));
        }*/
                try {
                    session = m_pmaSessionPool.borrowObject(resourceId);
                    return session;
                } catch (Exception e) {
                    LOGGER.error(String.format("Could not get session to Pma %s", resourceId.getResourceName()));
                    throw new RuntimeException(String.format("Could not get session to Pma %s", resourceId.getResourceName()), e);
                }
            case NETWORK_FUNCTION:
                NetworkFunction networkFunction = m_networkFunctionManager.getNetworkFunction(resourceId.getResourceName());
                if (networkFunction == null) {
                    throw new IllegalArgumentException(String.format("Network Function not managed : %s", resourceId.getResourceName()));
                }
                try {
                    session = m_pmaSessionPool.borrowObject(resourceId);
                    return session;
                }
                catch (Exception e) {
                    LOGGER.error(String.format("Could not get session to Pma %s", resourceId.getResourceName()));
                    throw new RuntimeException(String.format("Could not get session to Pma %s", resourceId.getResourceName()), e);
                }
            default:
                throw new IllegalArgumentException(String.format("Resource type %s not implemented", resourceId.getResourceType()));
        }

    }

    public void deviceRemoved(String deviceName) {
        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.DEVICE,deviceName);
        m_pmaSessionPool.clear(resourceId);
        m_factory.deviceDeleted(deviceName);
    }

    @Override
    public NetConfResponse getAllPersistCfg(String deviceName) throws ExecutionException {
        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.DEVICE,deviceName);
        return executeWithPmaSession(resourceId, PmaSession::getAllPersistCfg);
    }

    @Override
    public void networkFunctionRemoved(String networkFunctionName) {
        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.NETWORK_FUNCTION,networkFunctionName);
        m_pmaSessionPool.clear(resourceId);
        m_factory.networkFunctionDeleted(networkFunctionName);
    }

}
