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

package org.broadband_forum.obbaa.pma.rest;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 9/10/17.
 */
public class PmaRestEPTest {
    private PmaRestEP m_pmaRestEP;
    @Mock
    private PmaRegistry m_pmaRegistry;
    private static final String RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ><ok/></rpc-reply>";

    @Before
    public void setUp() throws ExecutionException {
        MockitoAnnotations.initMocks(this);
        when(m_pmaRegistry.executeNC(anyString(), anyString())).thenReturn(RESPONSE);
        m_pmaRestEP = new PmaRestEP(m_pmaRegistry);
    }

    @Test
    public void testThatEPDeligatesToRegistry() throws ExecutionException {
        assertEquals(RESPONSE, m_pmaRestEP.executeNC("device1", "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><get/></rpc>"));
        verify(m_pmaRegistry).executeNC("device1", "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><get/></rpc>");
        m_pmaRestEP.forceAlign("device1");
        verify(m_pmaRegistry).forceAlign("device1");
    }

}
