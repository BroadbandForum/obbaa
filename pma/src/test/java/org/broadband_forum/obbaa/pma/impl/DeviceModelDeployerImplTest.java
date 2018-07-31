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

import static junit.framework.TestCase.fail;
import static org.broadband_forum.obbaa.pma.impl.DeviceModelDeployerImpl.COMPONENT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class DeviceModelDeployerImplTest {

    private static String m_deployDirPath;
    private static File m_yangSourceDir;

    @Captor
    private ArgumentCaptor<List<ModelService>> m_serviceCaptor;
    @Captor
    private ArgumentCaptor<List<YangTextSchemaSource>> m_yangTextSources;
    @Mock
    private ModelServiceDeployer m_msd;
    @Mock
    private ModelNodeDataStoreManager m_dsm;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private SubSystem m_subSystem;

    private SchemaRegistryImpl m_schemaRegistry;
    DeviceModelDeployerImpl m_deployer;

    @BeforeClass
    public static void createYANGFiles() throws IOException {
        m_deployDirPath = Files.createTempDirectory(DeviceModelDeployerImplTest.class.getName()).toFile().getAbsolutePath();
        m_yangSourceDir = new File(DeviceModelDeployerImplTest.class.getResource("/devicemodeldeployerimpltest").getFile());
        writeYangFiles();
    }

    @Before
    public void setUp() throws IOException, SchemaBuildException {
        MockitoAnnotations.initMocks(this);
        m_schemaRegistry = spy(new SchemaRegistryImpl(Collections.emptyList(), new NoLockService()));
        m_deployer = new DeviceModelDeployerImpl(m_deployDirPath, m_msd, m_dsm, m_subSystem, m_schemaRegistry, m_entityRegistry);

    }

    private static void writeYangFiles() throws IOException {
        for(File yangSrcFile : m_yangSourceDir.listFiles()){
            FileUtils.copyFileToDirectory(yangSrcFile, new File(m_deployDirPath));
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(m_deployDirPath));
    }

    @Test
    public void testDeployerDeploysModelServices() throws Exception {
        List<String> modulesDeployed = m_deployer.redeploy();
        verify(m_schemaRegistry).loadSchemaContext(eq(COMPONENT_ID), m_yangTextSources.capture(), eq(null), eq(null));
        verify(m_schemaRegistry).unloadSchemaContext(COMPONENT_ID, null);
        assertAllYangFilesPresent(m_yangTextSources.getValue());
        verify(m_msd).undeploy(Collections.emptyList());
        verify(m_msd).deploy(m_serviceCaptor.capture());
        List<ModelService> services = m_serviceCaptor.getValue();
        checkServicesHaveFieldsSet(services);
        assertEquals(19, services.size());
        assertEquals(19, modulesDeployed.size());

        //redeploy again
        modulesDeployed = m_deployer.redeploy();
        verify(m_msd, times(2)).undeploy(m_serviceCaptor.getValue());
        verify(m_msd, times(2)).deploy(m_serviceCaptor.capture());
        services = m_serviceCaptor.getValue();
        checkServicesHaveFieldsSet(services);
        assertEquals(19, services.size());
        assertEquals(19, modulesDeployed.size());
    }

    @Test
    public void testDeployUpdatesEntityRegistry(){
        m_deployer.redeploy();
        verifyEntityRegistryIsUpdated();
    }

    private void verifyEntityRegistryIsUpdated() {
        verify(m_entityRegistry).addSchemaPaths(COMPONENT_ID, getRootNodeMap());
    }

    private Map<SchemaPath, Class> getRootNodeMap() {
        Map<SchemaPath, Class> rootNodeMap = new HashMap<>();
        for(DataSchemaNode rootNode : m_schemaRegistry.getRootDataSchemaNodes()){
            rootNodeMap.put(rootNode.getPath(), DeviceXmlStore.class);
        }
        return rootNodeMap;
    }


    @Test
    public void testInitCallsDeploy(){
        m_deployer = Mockito.spy(m_deployer);
        m_deployer.init();
        verify(m_deployer).deploy();
    }

    @Test
    public void testDestroyCallsUndeploy(){
        m_deployer = Mockito.spy(m_deployer);
        m_deployer.destroy();
        verify(m_deployer).undeploy();
    }

    private void checkServicesHaveFieldsSet(List<ModelService> services) {
        for(ModelService service : services){
            assertNotNull(service.getModuleName());
            assertNotNull(service.getModuleRevision());
            assertEquals(m_dsm, service.getModelNodeDSM());
            assertEquals(m_subSystem, service.getDefaultSubsystem());
        }

    }

    private void assertAllYangFilesPresent(List<YangTextSchemaSource> actualSources) {
        List<YangTextSchemaSource> expectedSources = new ArrayList<>();
        for (File yangSrcFile : m_yangSourceDir.listFiles()) {
            expectedSources.add(YangTextSchemaSource.forFile(yangSrcFile));
        }
        assertEquals(expectedSources.size(), actualSources.size());
        for (YangTextSchemaSource expectedSource : expectedSources) {
            boolean sourceFound = false;
            for (YangTextSchemaSource actualSource : actualSources) {
                try {
                    if (expectedSource.contentEquals(actualSource)) {
                        sourceFound = true;
                        break;
                    }
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
            if(!sourceFound){
                fail("Could not find source"+expectedSource);
            }
        }
    }


}
