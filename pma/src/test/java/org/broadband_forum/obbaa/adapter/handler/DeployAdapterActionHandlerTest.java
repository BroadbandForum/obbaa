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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.karaf.kar.KarService;
import org.broadband_forum.obbaa.adapter.AdapterDeployer;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
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
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private DeviceAdapterId m_deviceAdapterId;

    @Before
    public void setUp() throws BundleException, URISyntaxException {
        initMocks(this);
        m_stagingArea = "/home";
        m_url = new URI("file:///home/test/adapter.kar");
        m_adapterActionHandler = new DeviceAdapterActionHandlerImpl(m_stagingArea, m_karService, m_adapterManager);

    }


    @Test
    public void testDeployCodedAdapter() throws Exception {
        m_adapterActionHandler.deployAdapter("bbf-dpu-4lt-1.0.kar");
        //  verify(m_karService).install(m_url);

        //the url in windows will be : "file:///D:/home/test/adapter.kar", which will cause the case fail
        verify(m_karService).install(any());
    }

    @Test
    public void testDeployCodedAdapterWrongPattenForVendor() throws Exception {
        String expectedMessage;
        expectedMessage = "File name is not in the expected pattern";
        try {
            m_adapterActionHandler.deployAdapter("b123b!@#$f)(*&-dpu-4lt-1.0.kar");
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertTrue("File name is not in the expected pattern(vendor-type-model-interfaceVersion.kar)", e.getMessage().contains(expectedMessage));

        }
    }

    @Test
    public void testDeployCodedAdapterWrongPattenForType() throws Exception {
        String expectedMessage;
        expectedMessage = "File name is not in the expected pattern";
        try {
            m_adapterActionHandler.deployAdapter("bbf-123!@#$-4lt-10.kar");
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertTrue("File name is not in the expected pattern(vendor-type-model-interfaceVersion.kar)", e.getMessage().contains(expectedMessage));

        }
    }

    @Test
    public void testDeployCodedAdapterWrongPattenForModel() throws Exception {
        String expectedMessage;
        expectedMessage = "File name is not in the expected pattern";
        try {
            m_adapterActionHandler.deployAdapter("bbf-olt-4.!lt-1.0.kar");
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertTrue("File name is not in the expected pattern(vendor-type-model-interfaceVersion.kar)", e.getMessage().contains(expectedMessage));

        }
    }

    @Test
    public void testDeployCodedAdapterWrongPattenForIfversion() throws Exception {
        String expectedMessage;
        expectedMessage = "File name is not in the expected pattern";
        try {
            m_adapterActionHandler.deployAdapter("bbf-olt-8lt-1234.kar");
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertTrue("File name is not in the expected pattern(vendor-type-model-interfaceVersion.kar)", e.getMessage().contains(expectedMessage));

        }
    }

    @Test
    public void testDeployCodedAdapterWrongPatten() throws Exception {
        String expectedMessage;
        expectedMessage = "File name is not in the expected pattern";
        try {
            m_adapterActionHandler.deployAdapter("bbfolt4lt1.0.kar");
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertTrue("File name is not in the expected pattern(vendor-type-model-interfaceVersion.kar)", e.getMessage().contains(expectedMessage));

        }
    }

    @Test
    public void testDeployAdapterInstallingKarThrowsError() throws Exception {
        doThrow(new RuntimeException("Install error")).when(m_karService).install(m_url);
        try {
            m_adapterActionHandler.deployAdapter("bbf-dpu-4lt-1.0.kar");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Install error"));
        }
    }

    @Test
    public void testUndeployKar() throws Exception {
        m_adapterActionHandler.undeployAdapter("bbf-olt-8lt-1.0.kar");
        verify(m_karService).uninstall("bbf-olt-8lt-1.0");
    }

    @Test
    public void testUndeployKarwhenWrongPattern() throws Exception {
        String expectedMessage;
        expectedMessage = "File name is not in the expected pattern";
        try {
            m_adapterActionHandler.undeployAdapter("bbfolt4lt1.0.kar");
            fail("Expected a runtimeException");
        } catch (Exception e) {
            assertTrue("File name is not in the expected pattern(vendor-type-model-interfaceVersion.kar)", e.getMessage().contains(expectedMessage));

        }
    }

    @Test
    public void testUndeployKarWhichIsNotInstalled() throws Exception {
        doThrow(new RuntimeException("uninstall error")).when(m_karService).uninstall("bbf-olt-8lt-1.0");
        try {
            m_adapterActionHandler.undeployAdapter("bbf-olt-8lt-1.0.kar");
            fail("Should have failed while uninstalling kar");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("uninstall error"));
        }
    }

    @Test
    public void testUndeployKarWhenAdapterInUse() throws Exception {
        when(m_adapterManager.isAdapterInUse(any())).thenReturn(true);
        try {
            m_adapterActionHandler.undeployAdapter("bbf-olt-8lt-1.0.kar");
            fail("Should have failed while uninstalling kar");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Adapter in Use, Undeploy operation not allowed"));
        }
    }

    @Test
    public void testUndeployKarWhenNotInUse() throws Exception {
        when(m_adapterManager.isAdapterInUse(any())).thenReturn(false);
        m_adapterActionHandler.undeployAdapter("bbf-olt-8lt-1.0.kar");
        verify(m_karService).uninstall("bbf-olt-8lt-1.0");
    }

}
