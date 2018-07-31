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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.pma.DeviceModelDeployer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class DeviceModelDeployerImpl implements DeviceModelDeployer {
    private final String m_deployDirPath;
    private final File m_deployDir;
    private final ModelServiceDeployer m_modelServicesDeployer;
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeDataStoreManager m_dsm;
    public static final String COMPONENT_ID = DeviceModelDeployerImpl.class.getName();
    private List<ModelService> m_modelServices;
    private final EntityRegistry m_entityRegistry;
    private final SubSystem m_subSystem;

    public DeviceModelDeployerImpl(String deployDirPath, ModelServiceDeployer modelServicesDeployer,
                                   ModelNodeDataStoreManager dsm, SubSystem subSystem, SchemaRegistry schemaRegistry, EntityRegistry
                                           entityRegistry) {
        m_deployDirPath = deployDirPath;
        m_modelServicesDeployer = modelServicesDeployer;
        m_subSystem = subSystem;
        m_schemaRegistry = schemaRegistry;
        m_entityRegistry = entityRegistry;
        m_deployDir = new File(m_deployDirPath);
        m_modelServices = new ArrayList<>();
        m_dsm = dsm;
    }

    public void init() {
        if (!m_deployDir.exists()) {
            m_deployDir.mkdirs();
        } else if (!m_deployDir.isDirectory()) {
            throw new RuntimeException(m_deployDirPath + " is not a directory");
        }
        deploy();
    }

    public void destroy() {
        undeploy();
    }


    @Override
    public List<String> redeploy() {
        undeploy();
        deploy();
        return getDeployedModules();
    }

    @Override
    public List<String> getDeployedModules() {
        List<String> deployedModuleNames = new ArrayList<>();
        for (ModelService service : m_modelServices) {
            deployedModuleNames.add(service.getName());
        }
        return deployedModuleNames;
    }

    @Override
    public void undeploy() {
        unloadSchemaRegistry();
        undeployModelServices();
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        return m_schemaRegistry.getAllModuleIdentifiers();
    }

    @Override
    public void deploy() {
        buildSchemaRegistry();
        deployModelServices();
        updateEntityRegistry();
    }

    private void updateEntityRegistry() {
        Map<SchemaPath, Class> rootNodeMap = getRootNodeMap();
        m_entityRegistry.addSchemaPaths(COMPONENT_ID, rootNodeMap);
    }

    private Map<SchemaPath, Class> getRootNodeMap() {
        Map<SchemaPath, Class> rootNodeMap = new HashMap<>();
        for (DataSchemaNode rootNode : m_schemaRegistry.getRootDataSchemaNodes()) {
            rootNodeMap.put(rootNode.getPath(), DeviceXmlStore.class);
        }
        return rootNodeMap;
    }

    private void deployModelServices() {
        List<ModelService> services;
        try {
            services = prepareModelServices();
            m_modelServicesDeployer.deploy(services);
            m_modelServices = services;
        } catch (ModelServiceDeployerException e) {
            throw new RuntimeException("Could not deploy model services", e);
        }
    }

    private void undeployModelServices() {
        try {
            m_modelServicesDeployer.undeploy(m_modelServices);
            m_modelServices.clear();
        } catch (ModelServiceDeployerException e) {
            throw new RuntimeException("Could not undeploy model services", e);
        }
    }

    private List<ModelService> prepareModelServices() {
        List<ModelService> modelServices = new ArrayList<>();
        for (ModuleIdentifier moduleId : m_schemaRegistry.getAllModuleIdentifiers()) {
            ModelService service = new ModelService();
            service.setModuleName(moduleId.getName());
            service.setModuleRevision(moduleId.getQNameModule().getFormattedRevision());
            service.setModelNodeDSM(m_dsm);
            service.setDefaultSubsystem(m_subSystem);
            modelServices.add(service);
        }
        return modelServices;
    }

    private void buildSchemaRegistry() {
        try {
            m_schemaRegistry.loadSchemaContext(COMPONENT_ID, getYangTextSources(), null, null);
        } catch (SchemaBuildException | MalformedURLException e) {
            throw new RuntimeException("Could not redeploy yangs into schema registry", e);
        }
    }

    private void unloadSchemaRegistry() {
        try {
            m_schemaRegistry.unloadSchemaContext(COMPONENT_ID, null);
        } catch (SchemaBuildException e) {
            throw new RuntimeException("Could not undeploy yangs from schema registry", e);
        }
    }

    private List<YangTextSchemaSource> getYangTextSources() throws MalformedURLException {
        List<YangTextSchemaSource> yangTextSchemaSources = new ArrayList<>();
        for (File yangFile : m_deployDir.listFiles()) {
            if (yangFile.getAbsolutePath().endsWith(".yang")) {
                yangTextSchemaSources.add(YangParserUtil.getYangSource(yangFile.toURL()));
            }
        }
        return yangTextSchemaSources;
    }

}
