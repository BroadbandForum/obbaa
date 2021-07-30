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

package org.broadband_forum.obbaa.nm.devicemanager.rest;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 3/10/17.
 */
public class DeviceManagerRestEPTest {

    private DeviceManagerRestEP m_dm;
    @Mock
    private DeviceManager m_managerInternal;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        m_dm = spy(new DeviceManagerRestEP(m_managerInternal));
    }
    @Test
    public void testThatEPDeligatesOnAllMethods(){

        m_dm.getDevice("device 2");
        verify(m_managerInternal).getDevice("device 2");

        m_dm.getAllDevices();
        verify(m_managerInternal).getAllDevices();
    }

}
