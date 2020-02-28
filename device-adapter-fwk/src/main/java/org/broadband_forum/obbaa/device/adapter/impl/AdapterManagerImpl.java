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

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.BBF;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STANDARD;
import static org.broadband_forum.obbaa.device.adapter.AdapterUtils.getStandardAdapterContext;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.FactoryGarmentTag;
import org.broadband_forum.obbaa.device.registrator.impl.StandardModelRegistrator;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
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
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AdapterManagerImpl implements AdapterManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterManagerImpl.class);
    public static boolean ENABLE_FACTORY_GARMENT_TAG_RETRIEVAL = Boolean.parseBoolean(SystemPropertyUtils.getInstance()
            .getFromEnvOrSysProperty("ENABLE_FACTORY_GARMENT_TAG_RETRIEVAL", "True"));
    private final EventAdmin m_eventAdmin;
    private ModelNodeDataStoreManager m_dataStoreManager;
    private ReadWriteLockService m_readWriteLockService;
    private EntityRegistry m_entityRegistry;
    private Map<DeviceAdapterId, AdapterContext> m_adapterContextRegistry = new ConcurrentHashMap<>();
    private Map<DeviceAdapterId, DeviceAdapter> m_adapterMap = new HashMap<DeviceAdapterId, DeviceAdapter>();
    private Map<DeviceAdapterId, EditConfigRequest> m_defaultConfigReqMap = new HashMap<>();
    private Map<DeviceAdapter, FactoryGarmentTag> m_garmentTagMap = new HashMap<DeviceAdapter, FactoryGarmentTag>();
    private int m_maxAllowedAdapterVersions = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(
            "MAXIMUM_ALLOWED_ADAPTER_VERSIONS", "3"));
    private StandardModelRegistrator m_standardModelRegistrator;

    public AdapterManagerImpl(ModelNodeDataStoreManager dataStoreManager, ReadWriteLockService readWriteLockService,
                              EntityRegistry entityRegistry, EventAdmin eventAdmin, StandardModelRegistrator standardModelRegistrator) {
        m_dataStoreManager = dataStoreManager;
        m_readWriteLockService = readWriteLockService;
        m_entityRegistry = entityRegistry;
        m_eventAdmin = eventAdmin;
        m_standardModelRegistrator = standardModelRegistrator;
    }

    @Override
    public void deploy(DeviceAdapter adapter, SubSystem subSystem, Class klass, DeviceInterface deviceInterface) {
        LOGGER.info("Deploying the adapter: " + adapter.getDeviceAdapterId().toString());
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    DeviceAdapterId adapterId = new DeviceAdapterId(adapter.getType(), adapter.getInterfaceVersion(),
                            adapter.getModel(), adapter.getVendor());
                    String componentId = getComponentId(adapter);
                    AdapterContext adapterContext = new AdapterContext(m_readWriteLockService, deviceInterface);
                    try {
                        verifyIfMaxAllowedAdaptersInstalled(adapterId, adapter);
                        m_standardModelRegistrator.setOldIdentities(SchemaRegistryUtil.getAllIdentities(
                                adapterContext.getSchemaRegistry()));
                        deployAdapter(adapter, adapterContext, componentId, subSystem, klass);
                        if ((ENABLE_FACTORY_GARMENT_TAG_RETRIEVAL)
                                && (!adapter.getModel().equalsIgnoreCase(STANDARD) && !adapter.getVendor().equalsIgnoreCase(BBF))) {
                            computeFactoryGarmentTag(adapter, adapterContext);
                        }
                        m_adapterContextRegistry.putIfAbsent(adapterId, adapterContext);
                        m_adapterMap.putIfAbsent(adapterId, adapter);
                        EditConfigRequest editReq = validateDefaultXmlAndGenerateEditConfig(adapter, adapterContext);
                        m_defaultConfigReqMap.putIfAbsent(adapterId, editReq);
                        adapter.setLastUpdateTime(DateTime.now().toDateTime(DateTimeZone.UTC));
                        m_standardModelRegistrator.onDeployed(adapter, adapterContext);
                    } catch (Exception e) {
                        try {
                            AdapterUtils.logErrorToFile(e.toString(), adapter);
                        } catch (IOException ex) {
                            LOGGER.error(String.format(ex.getMessage()));
                        }
                        //remove keys from the map
                        m_adapterContextRegistry.remove(adapterId);
                        m_adapterMap.remove(adapterId);
                        m_defaultConfigReqMap.remove(adapterId);
                        LOGGER.error("Error while deploying adapter: " + adapter.getDeviceAdapterId().toString(), e);
                        throw new RuntimeException("Error while deploying adapter", e);
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
        LOGGER.info("Undeploying device adapter: " + adapter.getDeviceAdapterId().toString());
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    DeviceAdapterId deviceAdapterId = new DeviceAdapterId(adapter.getType(),
                            adapter.getInterfaceVersion(), adapter.getModel(), adapter.getVendor());
                    try {
                        unDeployAdapter(adapter);
                        AdapterUtils.removeAdapterLastUpdateTime(adapter.genAdapterLastUpdateTimeKey());
                    } catch (Exception e) {
                        LOGGER.error("Error while undeploying adapter: " + adapter, e);
                        throw new RuntimeException(e);
                    }
                    m_adapterMap.remove(deviceAdapterId);
                    m_adapterContextRegistry.remove(deviceAdapterId);
                    m_defaultConfigReqMap.remove(deviceAdapterId);
                    m_standardModelRegistrator.onUndeployed(adapter, getAdapterContext(deviceAdapterId));
                    if (m_garmentTagMap.containsKey(adapter)) {
                        m_garmentTagMap.remove(adapter);
                    }
                    return null;
                }
            });
        } catch (LockServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void unDeployAdapter(DeviceAdapter adapter) throws ModelNodeInitException {
        LOGGER.info("undeploying device adapter: " + adapter.getDeviceAdapterId().toString());
        String componentId = getComponentId(adapter);
        AdapterContext context = getAdapterContext(new DeviceAdapterId(adapter.getType(),
                adapter.getInterfaceVersion(), adapter.getModel(), adapter.getVendor()));
        context.getDsmRegistry().undeploy(componentId);
        context.getModelNodeHelperRegistry().undeploy(componentId);
        try {
            context.getSchemaRegistry().unloadSchemaContext(componentId, adapter.getSupportedFeatures(), adapter.getSupportedDevations());
        } catch (Exception e) {
            LOGGER.error("Error while undeploying device adapter " + adapter, e);
            throw new ModelNodeInitException("Error while undeploying device adapter " + adapter, e);
        }
        context.undeployed();
    }

    private void deployAdapter(DeviceAdapter adapter, AdapterContext adapterContext, String componentId, SubSystem subSystem, Class klass)
            throws IOException {
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

    private EditConfigRequest validateDefaultXmlAndGenerateEditConfig(DeviceAdapter adapter, AdapterContext adapterContext) {
        Document defaultXml = fetchDefaultData(adapter);
        try {
            Element dataElement = defaultXml.getDocumentElement();
            List<Element> childElements = DocumentUtils.getChildElements(dataElement);
            if (childElements.size() > 0) {
                EditConfigRequest request = new EditConfigRequest()
                        .setTargetRunning()
                        .setTestOption(EditConfigTestOptions.SET)
                        .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                        .setConfigElement(new EditConfigElement().setConfigElementContents(childElements));
                request.setMessageId("internal");
                request.setConfigElement(new EditConfigElement().setConfigElementContents(childElements));
                SchemaRegistryImpl stdAdapterSchemaRegistry = getStandardAdapterContext(this, adapter).getSchemaRegistry();
                RpcRequestConstraintParser parser = new RpcRequestConstraintParser(stdAdapterSchemaRegistry, null, null);
                parser.validate(request, RequestType.EDIT_CONFIG);
                //since there is no datastore for adapter, use the defaultXml itself as updated datastore and for old ds, dummy document
                adapterContext.getDeviceInterface().veto(null, request, createDocument(), defaultXml);
                return request;
            } else {
                LOGGER.info(String.format("The adapter %s does not contain default configurations", adapter.getDeviceAdapterId()));
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AdapterContext getStdAdapterContextFromDeviceAdapter(DeviceAdapter adapter) {
        AdapterContext stdAdapterContext = getStandardAdapterContext(this, adapter);
        return stdAdapterContext;
    }

    protected void computeFactoryGarmentTag(DeviceAdapter adapter, AdapterContext vendorAdapterContext) {
        AdapterContext stdAdapterContext = getStdAdapterContextFromDeviceAdapter(adapter);
        SchemaRegistry stdAdapterSchemaRegistry = stdAdapterContext.getSchemaRegistry();
        SchemaRegistry vdaSchemaRegistry = vendorAdapterContext.getSchemaRegistry();
        Set<Module> stdModules = stdAdapterSchemaRegistry.getAllModules();
        Set<Module> vdaModules = vdaSchemaRegistry.getAllModules();
        Set<Module> nonStdVdaModules = new HashSet<>(vdaModules);

        nonStdVdaModules.removeAll(stdModules);
        List<Module> reusedModules = stdModules.stream().filter(stdModule -> vdaModules.contains(stdModule))
                .collect(Collectors.toCollection(ArrayList::new));

        int numberOfStandardModules = stdModules.size();
        int numberOfModulesOfVendorAdapter = vdaModules.size();
        int numberOfReUsedStModules = reusedModules.size();

        List<String> deviatedStdModules = deriveDeviatedStandardModules(vdaSchemaRegistry, reusedModules);
        List<String> augmentedStandardModules = deriveAugmentedStandardModules(nonStdVdaModules, stdAdapterSchemaRegistry);

        m_garmentTagMap.put(adapter, new FactoryGarmentTag(numberOfStandardModules, numberOfModulesOfVendorAdapter,
                numberOfReUsedStModules, deviatedStdModules, augmentedStandardModules));
    }

    private List<String> deriveDeviatedStandardModules(SchemaRegistry vdaSchemaRegistry, List<Module> reusedModules) {
        Map<ModuleIdentifier, Set<QName>> supportedDeviations = vdaSchemaRegistry.getSupportedDeviations();
        Set<ModuleIdentifier> moduleIdentifiers = supportedDeviations.keySet();
        ArrayList<String> deviatedModules = moduleIdentifiers.stream().map(ModuleIdentifier::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        return reusedModules.stream().filter(module -> deviatedModules.contains(module.getName())).map(module -> module.getName())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> deriveAugmentedStandardModules(Set<Module> nonStdVdaModules,
                                                        SchemaRegistry stdAdapterSchemaRegistry) {
        Set<AugmentationSchemaNode> augmentationSchemaNodeSet = new HashSet<AugmentationSchemaNode>();
        Set<URI> augmentedNamespaceUri = new HashSet<URI>();
        for (Module nonStdVdaModule : nonStdVdaModules) {
            augmentationSchemaNodeSet = nonStdVdaModule.getAugmentations();
            augmentationSchemaNodeSet.forEach(augmentationSchemaNode -> {
                augmentedNamespaceUri.add(augmentationSchemaNode.getTargetPath().getLastComponent().getModule().getNamespace());
            });
        }
        ArrayList<String> augmentedStdModules = new ArrayList<>();
        for (URI uri : augmentedNamespaceUri) {
            String module = stdAdapterSchemaRegistry.getModuleNameByNamespace(String.valueOf(uri));
            if (module != null) {
                augmentedStdModules.add(module);
            }
        }
        return augmentedStdModules;
    }

    private Document fetchDefaultData(DeviceAdapter adapter) {
        Document doc;
        try (InputStream defaultXml = new ByteArrayInputStream(adapter.getDefaultXmlBytes())) {
            doc = DocumentUtils.loadXmlDocument(defaultXml);
        } catch (Exception e) {
            LOGGER.error("Failed to parse default-config.xml from the adapter: " + adapter.getDeviceAdapterId(), e);
            throw new RuntimeException("Failed to parse default-config.xml from the adapter " + e);
        }
        return doc;
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

    @Override
    public EditConfigRequest getEditRequestForAdapter(DeviceAdapterId adapterId) {
        return m_defaultConfigReqMap.get(adapterId);
    }

    public FactoryGarmentTag getFactoryGarmentTag(DeviceAdapter adapter) {
        return m_garmentTagMap.get(adapter);
    }

    private void buildSchemaRegistry(SchemaRegistry schemaRegistry, String componentId, DeviceAdapter deviceAdapter) throws IOException {
        try {
            schemaRegistry.loadSchemaContext(componentId, deviceAdapter.getModuleByteSources(), deviceAdapter.getSupportedFeatures(),
                    deviceAdapter.getSupportedDevations());
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
            service.setModuleRevision(moduleId.getRevision().get().toString());
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


    private boolean isMaxAllowedAdaptersInstalled(DeviceAdapter adapter) {
        boolean isMaxAdaptersInstalled = false;
        DeviceAdapterId adapterId = adapter.getDeviceAdapterId();
        Collection<DeviceAdapter> adapters = getAllDeviceAdapters();
        int numberOfInstalledAdapterVersions = 0;
        for (DeviceAdapter deployed : adapters) {
            DeviceAdapterId deployedAdapterId = deployed.getDeviceAdapterId();
            if (compareAdapterDetails(adapterId, deployedAdapterId)) {
                numberOfInstalledAdapterVersions++;
            }
        }
        LOGGER.info(String.format("Number of adapters already installed for the same vendor & model combination:%s",
                numberOfInstalledAdapterVersions));
        if (numberOfInstalledAdapterVersions >= m_maxAllowedAdapterVersions) {
            isMaxAdaptersInstalled = true;
        }
        return isMaxAdaptersInstalled;
    }

    private boolean compareAdapterDetails(DeviceAdapterId adapterId, DeviceAdapterId deployedAdapterId) {
        return !deployedAdapterId.getModel().equals(STANDARD)
                && !deployedAdapterId.getVendor().equals(BBF)
                && deployedAdapterId.getModel().equals(adapterId.getModel()) && deployedAdapterId.getType().equals(adapterId.getType())
                && deployedAdapterId.getVendor().equals(adapterId.getVendor());
    }

    private String prepareArchiveName(DeviceAdapter adapter) {
        return adapter.getVendor() + AdapterSpecificConstants.DASH + adapter.getType() + AdapterSpecificConstants.DASH
                + adapter.getModel() + AdapterSpecificConstants.DASH + adapter.getInterfaceVersion();
    }

    private void sendEvent(String fileName) {
        LOGGER.info("Sending event for Adapter {} ", fileName);
        Dictionary properties = new Hashtable();
        properties.put("FileName", fileName);
        Event event = new Event(EVENT_TOPIC, properties);
        m_eventAdmin.postEvent(event);
    }

    private void verifyIfMaxAllowedAdaptersInstalled(DeviceAdapterId adapterId, DeviceAdapter adapter) {
        if (isMaxAllowedAdaptersInstalled(adapter)) {
            sendEvent(prepareArchiveName(adapter));
            adapter.setLastUpdateTime(DateTime.now().toDateTime(DateTimeZone.UTC));
            throw new RuntimeException(String.format(("Adapter deployment failed!! Reason : maximum allowed versions(%s) "
                            + "reached for the specified adapter(%s), Uninstall any older version to proceed"),
                    m_maxAllowedAdapterVersions, adapterId));
        }
    }


}
