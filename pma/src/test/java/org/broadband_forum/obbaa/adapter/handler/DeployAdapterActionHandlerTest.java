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

package org.broadband_forum.obbaa.adapter.handler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.karaf.kar.KarService;
import org.broadband_forum.obbaa.adapter.AdapterDeployer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.BundleException;


public class DeployAdapterActionHandlerTest {

    private String m_stagingArea;
    private AdapterDeployer m_adapterActionHandler;
    @Mock
    private KarService m_karService;
    private URI m_url;

    @Before
    public void setUp() throws BundleException, URISyntaxException {
        initMocks(this);
        m_stagingArea = "/home";
        m_url = new URI("file:///home/test/adapter.kar");
        m_adapterActionHandler = new DeviceAdapterActionHandlerImpl(m_stagingArea, m_karService);
    }

    @Test
    public void testDeployCodedAdapter() throws Exception {
        m_adapterActionHandler.deployAdapter("test/adapter.kar");
        verify(m_karService).install(m_url);
    }

    @Test
    public void testDeployAdapterInstallingKarThrowsError() throws Exception {
        doThrow(new RuntimeException("Install error")).when(m_karService).install(m_url);
        try {
            m_adapterActionHandler.deployAdapter("test/adapter.kar");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Install error"));
        }
    }

    @Test
    public void testUndeployKar() throws Exception {
        m_adapterActionHandler.undeployAdapter("adapter.kar");
        verify(m_karService).uninstall("adapter");
    }

    @Test
    public void testUndeployKarWhichIsNotInstalled() throws Exception {
        doThrow(new RuntimeException("uninstall error")).when(m_karService).uninstall("adapter");
        try {
            m_adapterActionHandler.undeployAdapter("adapter.kar");
            fail("Should have failed while uninstalling kar");
        } catch (RuntimeException e) {
            assertTrue( e.getMessage().contains("uninstall error"));
        }
    }

}
