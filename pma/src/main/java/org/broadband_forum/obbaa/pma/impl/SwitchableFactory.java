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

import java.util.Map;

import org.apache.commons.pool2.PooledObject;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;

public class SwitchableFactory extends PmaSessionFactory {
    public static final String PMA_SESSION_FACTORY_TYPE = "PMA_SESSION_FACTORY_TYPE";
    public static final String REGULAR = "REGULAR";
    public static final String TRANSPARENT = "TRANSPARENT";
    private static final Logger LOGGER = Logger.getLogger(SwitchableFactory.class);
    private final Map<String, PmaSessionFactory> m_factories;
    private final PmaSessionFactory m_currentFactory;

    public SwitchableFactory(Map<String, PmaSessionFactory> factories) {
        m_factories = factories;
        m_currentFactory = m_factories.get(getFactoryName());
        LOGGER.info(String.format("Using PmaSesssionFactory of type %s", m_currentFactory.getClass()));
    }

    private String getFactoryName() {
        String factoryName = System.getenv(PMA_SESSION_FACTORY_TYPE);
        if (factoryName == null) {
            factoryName = System.getProperty(PMA_SESSION_FACTORY_TYPE, REGULAR);
        }
        LOGGER.info("Configured PmaSessionFactory Name " + factoryName);
        return factoryName;
    }

    @Override
    public PmaSession create(PmaResourceId resourceId) throws Exception {
        return m_currentFactory.create(resourceId);
    }

    @Override
    public PooledObject<PmaSession> wrap(PmaSession value) {
        return m_currentFactory.wrap(value);
    }

    @Override
    public boolean validateObject(PmaResourceId resourceId, PooledObject<PmaSession> session) {
        return m_currentFactory.validateObject(resourceId, session);
    }

    @Override
    public void deviceDeleted(String deviceName) {
        m_currentFactory.deviceDeleted(deviceName);
    }

    @Override
    public void networkFunctionDeleted(String networkFunctionName) {
        m_currentFactory.networkFunctionDeleted(networkFunctionName);
    }
}
