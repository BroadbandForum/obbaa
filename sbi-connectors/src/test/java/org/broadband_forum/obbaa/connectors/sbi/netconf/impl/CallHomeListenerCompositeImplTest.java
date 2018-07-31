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

package org.broadband_forum.obbaa.connectors.sbi.netconf.impl;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.security.cert.X509Certificate;

import org.broadband_forum.obbaa.connectors.sbi.netconf.CallHomeListenerComposite;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CallHomeListenerCompositeImplTest {
    private CallHomeListenerComposite m_composite;
    @Mock
    private CallHomeListener m_listener1;
    @Mock
    private CallHomeListener m_listener2;
    @Mock
    private NetconfClientSession m_session;
    @Mock
    private NetconfLoginProvider m_loginProvider;
    @Mock
    private X509Certificate m_certificate;
    private boolean m_isSelfSigned = false;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_composite = new CallHomeListenerCompositeImpl();
        m_composite.addListener(m_listener1);
        m_composite.addListener(m_listener2);
    }

    @Test
    public void testListenersAreCalled() {
        verifyListenersAreNeverCalled();

        m_composite.connectionEstablished(m_session, m_loginProvider, m_certificate, m_isSelfSigned);

        verifyListenersAreCalled();
    }

    @Test
    public void testRemovedListenersAreNotCalled() {
        verifyListenersAreNeverCalled();

        m_composite.removeListener(m_listener1);

        verifyListenersAreNeverCalled();

        m_composite.connectionEstablished(m_session, m_loginProvider, m_certificate, m_isSelfSigned);

        verifyZeroInteractions(m_listener1);
        verifyListenerIsCalled(m_listener2);
    }

    @Test
    public void testListenersThrowingExceptionsDoNotDropCallsToOtherListeners() {
        makeListenersThrowExceptions();
        verifyListenersAreNeverCalled();

        m_composite.connectionEstablished(m_session, m_loginProvider, m_certificate, m_isSelfSigned);
        verifyListenersAreCalled();
    }

    private void makeListenersThrowExceptions() {
        doThrow(new RuntimeException("Some error during listener callback")).when(m_listener1).connectionEstablished
                (anyObject(), anyObject(), anyObject(), anyBoolean());
        doThrow(new RuntimeException("Some error during listener callback")).when(m_listener2).connectionEstablished
                (anyObject(), anyObject(), anyObject(), anyBoolean());
    }

    private void verifyListenersAreNeverCalled() {
        verifyZeroInteractions(m_listener1);
        verifyZeroInteractions(m_listener2);
    }

    private void verifyListenersAreCalled() {
        verifyListenerIsCalled(m_listener1);
        verifyListenerIsCalled(m_listener2);
    }

    private void verifyListenerIsCalled(CallHomeListener listener) {
        verify(listener).connectionEstablished(m_session, m_loginProvider, m_certificate, m_isSelfSigned);
    }
}
