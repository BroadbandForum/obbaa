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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;

public class AdapterContext {

    private final ReadWriteLockService m_readWriteLockService;
    private SubSystemRegistryImpl m_subSystemRegistry;
    private SchemaRegistryImpl m_schemaRegistry;
    private ModelNodeDSMRegistryImpl m_dsmRegistry;
    private ModelNodeHelperRegistryImpl m_modelNodeHelperRegistry;
    private RootModelNodeAggregatorImpl m_rootModelNodeAggregator;
    private List<AdapterListener> m_contextListeners = new ArrayList<>();

    public AdapterContext(ReadWriteLockService readWriteLockService) {
        m_readWriteLockService = readWriteLockService;
        m_subSystemRegistry = new SubSystemRegistryImpl();
        try {
            m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(),
                    Collections.emptyMap(), m_readWriteLockService);
        } catch (SchemaBuildException e) {
            throw new RuntimeException("Unable to get mount schema registry",e);
        }
        m_dsmRegistry = new ModelNodeDSMRegistryImpl();
        m_modelNodeHelperRegistry = createModelNodeHelperRegistry();
    }

    public SubSystemRegistryImpl getSubSystemRegistry() {
        return m_subSystemRegistry;
    }

    public SchemaRegistryImpl getSchemaRegistry() {
        return m_schemaRegistry;
    }

    public ModelNodeDSMRegistryImpl getDsmRegistry() {
        return m_dsmRegistry;
    }

    public ModelNodeHelperRegistryImpl getModelNodeHelperRegistry()  {
        return m_modelNodeHelperRegistry;
    }

    public ModelNodeHelperRegistryImpl createModelNodeHelperRegistry() {
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(getSchemaRegistry());
        DSExpressionValidator validator = new DSExpressionValidator(getSchemaRegistry(), m_modelNodeHelperRegistry);
        AddDefaultDataInterceptor interceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry,
                getSchemaRegistry(), validator);
        interceptor.init();
        m_modelNodeHelperRegistry.setDefaultCapabilityCommandInterceptor(interceptor);
        return m_modelNodeHelperRegistry;
    }

    public RootModelNodeAggregatorImpl getRootModelNodeAggregator() {
        return m_rootModelNodeAggregator;
    }

    public void setRootModelNodeAggregator(RootModelNodeAggregatorImpl rootModelNodeAggregator) {
        m_rootModelNodeAggregator = rootModelNodeAggregator;
    }

    public void undeployed() {
        m_contextListeners.forEach(adapterListener -> adapterListener.undeployed());
        m_contextListeners.clear();
    }

    public void addListener(AdapterListener listener) {
        m_contextListeners.add(listener);
    }
}
