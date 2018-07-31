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
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.SingleXmlObjectDSM;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.jetbrains.annotations.NotNull;

public class PmaServerSessionFactory extends PmaSessionFactory {
    private final DeviceManager m_deviceManager;
    private final EntityRegistry m_entityRegistry;
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final SubSystemRegistry m_subsystemRegistry;
    private final ModelNodeDSMRegistry m_modelNodeDsmRegistry;
    private final String m_deviceFileBaseDirPath;
    private final NetConfServerImpl m_netconfServer;
    private final PersistenceManagerUtil m_persistenceMgrUtil = new DummyPersistenceUtil();
    private ConcurrentHashMap<DeviceInfo, ModelNodeDataStoreManager> m_dsms = new ConcurrentHashMap<>();
    private final NetconfDeviceAlignmentService m_das;

    public PmaServerSessionFactory(String deviceFileBaseDirPath, DeviceManager deviceManager, NetConfServerImpl
            netconfServer,
                                   NetconfDeviceAlignmentService das, EntityRegistry entityRegistry, SchemaRegistry
                                           schemaRegistry,
                                   ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegistry,
                                   ModelNodeDSMRegistry modelNodeDsmRegistry) {
        m_das = das;
        m_entityRegistry = entityRegistry;
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subsystemRegistry = subsystemRegistry;
        m_modelNodeDsmRegistry = modelNodeDsmRegistry;
        m_deviceManager = deviceManager;
        m_netconfServer = netconfServer;
        m_deviceFileBaseDirPath = deviceFileBaseDirPath;
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
    public PmaSession create(String key) {
        DeviceInfo deviceInfo = m_deviceManager.getDevice(key);
        PmaServer pmaServer = createPmaServer(deviceInfo);
        return new PmaServerSession(deviceInfo, pmaServer, m_das);
    }

    @Override
    public PooledObject<PmaSession> wrap(PmaSession value) {
        return new DefaultPooledObject<>(value);
    }

    @NotNull
    private PmaServerImpl createPmaServer(DeviceInfo deviceInfo) {
        return new PmaServerImpl(deviceInfo, m_netconfServer, getDsm(deviceInfo));
    }

    private ModelNodeDataStoreManager getDsm(DeviceInfo deviceInfo) {
        if (!m_dsms.containsKey(deviceInfo)) {
            DeviceXmlStore deviceStore = new DeviceXmlStore(getDeviceStoreFilePath(deviceInfo.getKey()));
            SingleXmlObjectDSM<DeviceXmlStore> dsm = new SingleXmlObjectDSM<>(deviceStore, m_persistenceMgrUtil,
                    m_entityRegistry, m_schemaRegistry, m_modelNodeHelperRegistry, m_subsystemRegistry,
                    m_modelNodeDsmRegistry);
            m_dsms.putIfAbsent(deviceInfo, dsm);
        }
        return m_dsms.get(deviceInfo);
    }

    @NotNull
    private String getDeviceStoreFilePath(String deviceName) {
        return m_deviceFileBaseDirPath + File.separator + deviceName + "_running_ds.xml";
    }

    @Override
    public void deviceDeleted(String deviceName) {
        DeviceInfo deviceInfo = getDsmWithDeviceName(deviceName);
        deleteStoreFile(deviceName);
        if (deviceInfo != null) {
            m_dsms.remove(deviceInfo);
        }
    }

    private void deleteStoreFile(String deviceName) {
        String filePath = getDeviceStoreFilePath(deviceName);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private DeviceInfo getDsmWithDeviceName(String deviceName) {
        for (DeviceInfo info : m_dsms.keySet()) {
            if (deviceName.equals(info.getKey())) {
                return info;
            }
        }
        return null;
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
