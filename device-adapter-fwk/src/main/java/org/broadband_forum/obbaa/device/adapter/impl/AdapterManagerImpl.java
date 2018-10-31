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

package org.broadband_forum.obbaa.device.adapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.WriteLockTemplate;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterManagerImpl implements AdapterManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterManagerImpl.class);


    private ModelNodeDataStoreManager m_dataStoreManager;
    private ReadWriteLockService m_readWriteLockService;
    private EntityRegistry m_entityRegistry;
    private Map<DeviceAdapterId, AdapterContext> m_adapterContextRegistry = new ConcurrentHashMap<>();
    private Map<DeviceAdapterId, DeviceAdapter> m_adapterMap = new HashMap<DeviceAdapterId, DeviceAdapter>();

    public AdapterManagerImpl(ModelNodeDataStoreManager dataStoreManager,
                              ReadWriteLockService readWriteLockService, EntityRegistry entityRegistry) {
        m_dataStoreManager = dataStoreManager;
        m_readWriteLockService = readWriteLockService;
        m_entityRegistry = entityRegistry;
    }

    @Override
    public void deploy(DeviceAdapter adapter, SubSystem subSystem, Class klass) {
        LOGGER.info("Deploying the adapter :" + adapter);
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    DeviceAdapterId adapterId = new DeviceAdapterId(adapter.getType(), adapter.getInterfaceVersion(),
                            adapter.getModel(), adapter.getVendor());
                    String componentId = getComponentId(adapter);
                    AdapterContext adapterContext = new AdapterContext(m_readWriteLockService);
                    try {
                        deployAdapter(adapter, adapterContext, componentId, subSystem, klass);
                        m_adapterContextRegistry.putIfAbsent(adapterId, adapterContext);
                        m_adapterMap.putIfAbsent(adapterId, adapter);
                    } catch (Exception e) {
                        LOGGER.error("Error while deploying adapter: " + adapter, e);
                        throw new LockServiceException(e);
                    }

                    return null;
                }
            });
        } catch (LockServiceException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void undeploy(DeviceAdapter adapter) {
        LOGGER.info("Undeploying device adapter: " + adapter);
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    DeviceAdapterId deviceAdapterId = new DeviceAdapterId(adapter.getType(),
                            adapter.getInterfaceVersion(), adapter.getModel(), adapter.getVendor());
                    try {
                        unDeployAdapter(adapter);
                    } catch (Exception e) {
                        LOGGER.error("Error while undeploying adapter: " + adapter, e);
                        throw new LockServiceException(e);
                    }
                    m_adapterMap.remove(deviceAdapterId);
                    m_adapterContextRegistry.remove(deviceAdapterId);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void unDeployAdapter(DeviceAdapter adapter) throws ModelNodeInitException {
        LOGGER.info("undeploying device plug: " + adapter);
        String componentId = getComponentId(adapter);
        AdapterContext context = getAdapterContext(new DeviceAdapterId(adapter.getType(),
                adapter.getInterfaceVersion(), adapter.getModel(), adapter.getVendor()));
        context.getDsmRegistry().undeploy(componentId);
        context.getModelNodeHelperRegistry().undeploy(componentId);
        try {
            context.getSchemaRegistry().unloadSchemaContext(componentId, null);
        } catch (Exception e) {
            LOGGER.error("Error while undeploying device adapter " + adapter, e);
            throw new ModelNodeInitException("Error while undeploying device adapter " + adapter, e);
        }
        context.undeployed();
    }

    private void deployAdapter(DeviceAdapter adapter, AdapterContext adapterContext, String componentId, SubSystem subSystem, Class klass)
            throws ModelNodeInitException, IOException {
        SchemaRegistry schemaRegistry = adapterContext.getSchemaRegistry();
        SubSystemRegistry subSystemRegistry = adapterContext.getSubSystemRegistry();
        ModelNodeDSMRegistry dsmRegistry = adapterContext.getDsmRegistry();
        ModelNodeHelperRegistry helperRegistry = adapterContext.getModelNodeHelperRegistry();
        ModelNodeHelperDeployer modelNodeHelperDeployer = new EntityModelNodeHelperDeployer(helperRegistry, schemaRegistry,
                m_dataStoreManager, m_entityRegistry, subSystemRegistry);
        buildSchemaRegistry(schemaRegistry, componentId, adapter);

        RootModelNodeAggregatorImpl rootModelNodeAggregator = new RootModelNodeAggregatorImpl(schemaRegistry, helperRegistry,
                m_dataStoreManager, subSystemRegistry);
        adapterContext.setRootModelNodeAggregator(rootModelNodeAggregator);

        ModelServiceDeployerImpl serviceDeployer = new ModelServiceDeployerImpl(dsmRegistry,
                helperRegistry, subSystemRegistry, new RpcRequestHandlerRegistryImpl(), modelNodeHelperDeployer,
                schemaRegistry, m_readWriteLockService);
        serviceDeployer.setRootModelNodeAggregator(rootModelNodeAggregator);
        deployModelServices(serviceDeployer, schemaRegistry, subSystem, adapter);
        updateEntityRegistry(componentId, schemaRegistry, klass);
    }

    private String getComponentId(DeviceAdapter adapter) {
        return adapter.getType() + "-" + adapter.getInterfaceVersion() + "-" + adapter.getModel() + "-" + adapter.getVendor();
    }

    @Override
    public int getAdapterSize() {
        return m_adapterMap.size();
    }

    @Override
    public DeviceAdapter getDeviceAdapter(DeviceAdapterId adapterId) {
        return m_adapterMap.get(adapterId);
    }

    @Override
    public AdapterContext getAdapterContext(DeviceAdapterId adapterId) {
        return m_adapterContextRegistry.get(adapterId);
    }

    @Override
    public Collection<DeviceAdapter> getAllDeviceAdapters() {
        return m_adapterMap.values();
    }


    private void buildSchemaRegistry(SchemaRegistry schemaRegistry, String componentId, DeviceAdapter deviceAdapter) throws IOException {
        try {
            schemaRegistry.loadSchemaContext(componentId, deviceAdapter.getModuleByteSources(), null, null);
        } catch (SchemaBuildException | MalformedURLException e) {
            throw new RuntimeException("Could not redeploy yangs into schema registry", e);
        }
    }

    private void deployModelServices(ModelServiceDeployerImpl modelServiceDeployer, SchemaRegistry schemaRegistry,
                                     SubSystem subSystem, DeviceAdapter adapter) {
        List<ModelService> services;
        try {
            services = prepareModelServices(schemaRegistry, subSystem, adapter);
            modelServiceDeployer.deploy(services);
        } catch (ModelServiceDeployerException e) {
            throw new RuntimeException("Could not deploy model services", e);
        }
    }

    private List<ModelService> prepareModelServices(SchemaRegistry schemaRegistry, SubSystem subSystem, DeviceAdapter adapter) {
        List<ModelService> modelServices = new ArrayList<>();
        for (ModuleIdentifier moduleId : schemaRegistry.getAllModuleIdentifiers()) {
            ModelService service = new ModelService();
            service.setModuleName(moduleId.getName());
            service.setModuleRevision(moduleId.getQNameModule().getFormattedRevision());
            service.setModelNodeDSM(m_dataStoreManager);
            service.setDefaultSubsystem(subSystem);
            adapter.addCapability(schemaRegistry.getCapability(moduleId));
            modelServices.add(service);
        }
        return modelServices;
    }

    private void updateEntityRegistry(String componentId, SchemaRegistry schemaRegistry, Class klass) {
        Map<SchemaPath, Class> rootNodeMap = getRootNodeMap(schemaRegistry, klass);
        m_entityRegistry.addSchemaPaths(componentId, rootNodeMap);
    }

    private Map<SchemaPath, Class> getRootNodeMap(SchemaRegistry schemaRegistry, Class klass) {
        Map<SchemaPath, Class> rootNodeMap = new HashMap<>();
        for (DataSchemaNode rootNode : schemaRegistry.getRootDataSchemaNodes()) {
            rootNodeMap.put(rootNode.getPath(), klass);
        }
        return rootNodeMap;
    }
}
