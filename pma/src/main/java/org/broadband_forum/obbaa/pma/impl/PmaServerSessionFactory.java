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

import static org.broadband_forum.obbaa.device.adapter.AdapterUtils.getAdapterContext;
import static org.broadband_forum.obbaa.device.adapter.AdapterUtils.getStandardAdapterContext;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.broadband_forum.obbaa.device.adapter.AdapterBuilder;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.SingleXmlObjectDSM;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionManager;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.NetconfNetworkFunctionAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO obbaa-366 create methods and attributes for networkFunctions
public class PmaServerSessionFactory extends PmaSessionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PmaServerSessionFactory.class);
    private final DeviceManager m_deviceManager;
    private final EntityRegistry m_entityRegistry;
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeDSMRegistry m_modelNodeDsmRegistry;
    private final String m_deviceFileBaseDirPath;
    private final NetConfServerImpl m_netconfServer;
    private final PersistenceManagerUtil m_persistenceMgrUtil = new DummyPersistenceUtil();
    private final NetconfDeviceAlignmentService m_das;
    private AdapterManager m_adapterManager;
    private final NetworkFunctionManager m_networkFunctionManager;
    private final NetconfNetworkFunctionAlignmentService m_nas;
    private final String m_networkFunctionFileBaseDirPath;

    public PmaServerSessionFactory(String deviceFileBaseDirPath, DeviceManager deviceManager, NetConfServerImpl netconfServer,
                                   NetconfDeviceAlignmentService das, EntityRegistry entityRegistry, SchemaRegistry schemaRegistry,
                                   ModelNodeDSMRegistry modelNodeDsmRegistry, AdapterManager adapterManager,
                                   NetworkFunctionManager networkFunctionManager, NetconfNetworkFunctionAlignmentService nas) {
        m_das = das;
        m_entityRegistry = entityRegistry;
        m_schemaRegistry = schemaRegistry;
        m_modelNodeDsmRegistry = modelNodeDsmRegistry;
        m_deviceManager = deviceManager;
        m_netconfServer = netconfServer;
        m_deviceFileBaseDirPath = deviceFileBaseDirPath;
        m_adapterManager = adapterManager;

        m_networkFunctionManager = networkFunctionManager;
        m_nas = nas;
        m_networkFunctionFileBaseDirPath = deviceFileBaseDirPath;
    }

    public void init() throws AnnotationAnalysisException {
        File directory = new File(m_deviceFileBaseDirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            throw new RuntimeException(m_deviceFileBaseDirPath + " is not a directory");
        }
        EntityRegistryBuilder.updateEntityRegistry(PmaRegistryImpl.COMPONENT_ID, Arrays.asList(DeviceXmlStore.class),
                m_entityRegistry, m_schemaRegistry, null, m_modelNodeDsmRegistry);
    }

    public void destroy() {
        m_entityRegistry.undeploy(PmaRegistryImpl.COMPONENT_ID);
    }

    @Override
    public PmaSession create(PmaResourceId resourceId) {
        if (resourceId.getResourceType() == PmaResourceId.Type.DEVICE) {
            Device device = m_deviceManager.getDevice(resourceId.getResourceName());
            PmaServer pmaServer = createPmaServer(device);
            return new PmaServerSession(device, pmaServer, m_das);
        }

        if (resourceId.getResourceType() == PmaResourceId.Type.NETWORK_FUNCTION) {
            NetworkFunction networkFunction = m_networkFunctionManager.getNetworkFunction(resourceId.getResourceName());
            PmaServer pmaServer = createPmaServer(networkFunction);
            return new PmaServerSession(networkFunction,pmaServer,m_nas);
        }
        return null;
    }

    @Override
    public boolean validateObject(PmaResourceId key, PooledObject<PmaSession> session) {
        return ((PmaServerSession) session.getObject()).isActive();
    }

    @Override
    public PooledObject<PmaSession> wrap(PmaSession value) {
        return new DefaultPooledObject<>(value);
    }

    @NotNull
    private PmaServerImpl createPmaServer(Device device) {
        device.getDeviceManagement().setNetconf(AdapterUtils.getAdapter(device, m_adapterManager).getNetconf());
        return new PmaServerImpl(device, m_netconfServer, getDsmAndStore(device), getAdapterContext(device, m_adapterManager),
                getStandardAdapterContext(m_adapterManager,
                        AdapterUtils.getAdapter(device, m_adapterManager)), m_deviceFileBaseDirPath);
    }

    //TODO obbaa-366 NetworkFunction has no getNetworkFunctionManagement() method (see createPmaServer above ^)
    private PmaServer createPmaServer(NetworkFunction networkFunction) {
        String adapterType;
        String adapterVersion = "1.0";
        switch (networkFunction.getType()) {
            case "bbf-nf-types:vomci-function-type":
                adapterType = "nf-vomci";
                adapterVersion = "2.0";
                break;
            case "bbf-nf-types:vomci-proxy-type":
                adapterType = "nf-vproxy";
                adapterVersion = "2.0";
                break;
            case "bbf-d-olt-nft:d-olt-pppoeia":
                adapterType = "nf-d-olt-pppoe-ia";
                break;
            default:
                //unsupported type
                LOGGER.error("Unsupported type \"{}\"", networkFunction.getType());
                return null;
        }
        //TODO obbaa-366 create network function specific adapters, for now, create one along side the device adapters
        DeviceAdapter deviceAdapter = AdapterBuilder.createAdapterBuilder()
                // As part of OBBAA-656, only 2.0 version of std adapters(for nw functions) are available from R6.0 onwards.
                .setDeviceAdapterId(new DeviceAdapterId(adapterType, adapterVersion, "standard", "BBF"))
                .build();
        return new PmaServerImplNf(networkFunction, m_netconfServer, getNsmAndStore(networkFunction, deviceAdapter),
                getStandardAdapterContext(m_adapterManager,
                        deviceAdapter), m_networkFunctionFileBaseDirPath);
    }

    private Pair<ModelNodeDataStoreManager, DeviceXmlStore> getDsmAndStore(Device device) {
        DeviceXmlStore deviceStore = new DeviceXmlStore(getDeviceStoreFilePath(device.getDeviceName()));
        AdapterContext adapterContext = getStandardAdapterContext(m_adapterManager,
                AdapterUtils.getAdapter(device, m_adapterManager));
        SingleXmlObjectDSM<DeviceXmlStore> dsm = new SingleXmlObjectDSM<>(deviceStore, m_persistenceMgrUtil,
                m_entityRegistry, adapterContext.getSchemaRegistry(), adapterContext.getModelNodeHelperRegistry(),
                adapterContext.getSubSystemRegistry(), adapterContext.getDsmRegistry());
        return new Pair<>(dsm, deviceStore);
    }

    private Pair<ModelNodeDataStoreManager, DeviceXmlStore> getNsmAndStore(NetworkFunction networkFunction, DeviceAdapter deviceAdapter) {
        DeviceXmlStore deviceStore = new DeviceXmlStore(getNetFuncStoreFilePath(networkFunction.getNetworkFunctionName()));

        AdapterContext adapterContext = getStandardAdapterContext(m_adapterManager,
                deviceAdapter);
        SingleXmlObjectDSM<DeviceXmlStore> dsm = new SingleXmlObjectDSM<>(deviceStore, m_persistenceMgrUtil,
                m_entityRegistry, adapterContext.getSchemaRegistry(), adapterContext.getModelNodeHelperRegistry(),
                adapterContext.getSubSystemRegistry(), adapterContext.getDsmRegistry());
        return new Pair<>(dsm, deviceStore);
    }

    @NotNull
    private String getDeviceStoreFilePath(String deviceName) {
        return m_deviceFileBaseDirPath + File.separator + deviceName + "_running_ds.xml";
    }

    @Override
    public void deviceDeleted(String deviceName) {
        deleteStoreFile(deviceName);
    }

    //TODO obbaa-366 should use same store file as devices or create a new one?
    @Override
    public void networkFunctionDeleted(String networkFunctionName) {
        deleteNetFuncStoreFile(networkFunctionName);
    }

    private void deleteNetFuncStoreFile(String networkFunctionName) {
        String filePath = getNetFuncStoreFilePath(networkFunctionName);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @NotNull
    private String getNetFuncStoreFilePath(String networkFunctionName) {
        return m_networkFunctionFileBaseDirPath + File.separator + networkFunctionName + "_running_ds_nf.xml";
    }

    private void deleteStoreFile(String deviceName) {
        String filePath = getDeviceStoreFilePath(deviceName);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private class DummyPersistenceUtil implements PersistenceManagerUtil {
        @Override
        public EntityDataStoreManager getEntityDataStoreManager() {
            return null;
        }

        @Override
        public void closePersistenceManager() {
        }

    }
}
