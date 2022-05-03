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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SwitchableFactoryTest {

    private SwitchableFactory m_factory;
    private Map<String, PmaSessionFactory> m_factories;
    @Mock
    PmaSessionFactory m_regularFactory;
    @Mock
    PmaSessionFactory m_transparentFactory;
    @Mock
    private PmaSession m_pmaSession;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_factories = new HashMap<>();
        m_factories.put(SwitchableFactory.REGULAR, m_regularFactory);
        m_factories.put(SwitchableFactory.TRANSPARENT, m_transparentFactory);
        setUpRegularFactory();
    }

    private void setUpRegularFactory() {
        System.setProperty(SwitchableFactory.PMA_SESSION_FACTORY_TYPE, SwitchableFactory.REGULAR);
        m_factory = new SwitchableFactory(m_factories);
    }

    @Test
    public void testFactoryDelegatesToRegularFactory() throws Exception {
        testRegularFactoryIsInvoked();
    }

    @Test
    public void testFactoryDelegatesToTransparentFactory() throws Exception {
        setUpTransparentFactory();
        testTransparentFactoryIsInvoked();
    }


    @Test
    public void testDefaultFactoryIsRegular() throws Exception {
        m_factory = new SwitchableFactory(m_factories);
        testRegularFactoryIsInvoked();
    }

    private void setUpTransparentFactory() {
        System.setProperty(SwitchableFactory.PMA_SESSION_FACTORY_TYPE, SwitchableFactory.TRANSPARENT);
        m_factory = new SwitchableFactory(m_factories);
    }

    private void testTransparentFactoryIsInvoked() throws Exception {
        testFactoryIsInvoked(m_transparentFactory, m_regularFactory);
    }

    private void testRegularFactoryIsInvoked() throws Exception {
        testFactoryIsInvoked(m_regularFactory, m_transparentFactory);
    }

    private void testFactoryIsInvoked(BaseKeyedPooledObjectFactory factoryToBeUses, BaseKeyedPooledObjectFactory factoryNotToBeUsed)
            throws Exception {
        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.DEVICE,"blah");
        verify(factoryToBeUses, never()).create(anyString());
        m_factory.create(resourceId);
        verify(factoryToBeUses).create(resourceId);

        verify(factoryToBeUses, never()).wrap(anyObject());
        m_factory.wrap(m_pmaSession);
        verify(factoryToBeUses).wrap(m_pmaSession);

        verifyZeroInteractions(factoryNotToBeUsed);
    }
}
