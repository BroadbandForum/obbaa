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

package org.broadband_forum.obbaa.device.adapter;

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.ADAPTER_XML_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_DEVIATIONS_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_FEATURES_PATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;

public class CodedServiceImplTest {

    private CodedAdapterService m_codedAdapterService;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private Bundle m_bundle;
    @Mock
    private SubSystem m_subsystem;
    @Mock
    private DeviceInterface m_deviceInterface;
    private URL m_adapterUrl;
    private URL m_featuresUrl;
    private URL m_deviationsUrl;
    @Mock
    private DeviceAdapter m_dummyAdapter;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        m_codedAdapterService = new CodedAdapterServiceImpl(m_adapterManager, m_bundle, this.getClass(), m_subsystem, m_deviceInterface);
        m_adapterUrl = getClass().getResource(ADAPTER_XML_PATH);
        when(m_bundle.getResource(ADAPTER_XML_PATH)).thenReturn(m_adapterUrl);
        m_featuresUrl = getClass().getResource(DEFAULT_FEATURES_PATH);
        when(m_bundle.getResource(DEFAULT_FEATURES_PATH)).thenReturn(m_featuresUrl);
        m_deviationsUrl = getClass().getResource(DEFAULT_DEVIATIONS_PATH);
        when(m_bundle.getResource(DEFAULT_DEVIATIONS_PATH)).thenReturn(m_deviationsUrl);
    }

    @Test
    public void testDeployAdapter() throws Exception {
        ArgumentCaptor<DeviceAdapter> adapter = ArgumentCaptor.forClass(DeviceAdapter.class);
        m_codedAdapterService.deployAdapter();
        verify(m_adapterManager).deploy(adapter.capture(), eq(m_subsystem), eq(this.getClass()), eq(m_deviceInterface));
        DeviceAdapter actualAdapter = adapter.getValue();
        DeviceAdapterId expectedId = new DeviceAdapterId(actualAdapter.getType(), actualAdapter.getInterfaceVersion(),
                actualAdapter.getModel(), actualAdapter.getVendor());
        assertEquals(expectedId, actualAdapter.getDeviceAdapterId());
        assertEquals(4, actualAdapter.getSupportedFeatures().size());
        assertEquals(1, actualAdapter.getSupportedDevations().size());
        assertEquals(5, actualAdapter.getCapabilities().size());
        assertEquals(2, actualAdapter.getRevisions().size());
        assertEquals("Developer for this test case", actualAdapter.getDeveloper());
    }

    @Test
    public void testUndeployAdapter() throws Exception {
        when(m_adapterManager.getDeviceAdapter(new DeviceAdapterId("ADAPTER1", "1.0", "4LT", "VENDOR1" )))
                .thenReturn(m_dummyAdapter);
        m_codedAdapterService.unDeployAdapter();
        verify(m_adapterManager).undeploy(m_dummyAdapter);
    }
}
