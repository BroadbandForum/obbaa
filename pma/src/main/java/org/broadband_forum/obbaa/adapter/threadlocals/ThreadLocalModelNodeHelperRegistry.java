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

package org.broadband_forum.obbaa.adapter.threadlocals;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ThreadLocalModelNodeHelperRegistry implements ModelNodeHelperRegistry {

    private static ThreadLocal<ModelNodeHelperRegistryImpl> c_registry = new ThreadLocal<>();

    public static void setModelNodeHelperRegistry(ModelNodeHelperRegistryImpl registry) {
        c_registry.set(registry);
    }

    public static ModelNodeHelperRegistryImpl getHelperRegistry() {
        return c_registry.get();
    }

    @Override
    public boolean isRegistered(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().isRegistered(modelNodeSchemaPath);
    }

    @Override
    public boolean registrationComplete(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().registrationComplete(modelNodeSchemaPath);
    }

    @Override
    public Map<QName, ChildListHelper> getChildListHelpers(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().getChildListHelpers(modelNodeSchemaPath);
    }

    @Override
    public void registerChildListHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ChildListHelper helper) {
        getHelperRegistry().registerChildListHelper(componentId, modelNodeSchemaPath, name, helper);
    }

    @Override
    public ChildListHelper getChildListHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
        return getHelperRegistry().getChildListHelper(modelNodeSchemaPath, helperName);
    }

    @Override
    public void registerChildContainerHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name,
                                             ChildContainerHelper helper) {
        getHelperRegistry().registerChildContainerHelper(componentId, modelNodeSchemaPath, name, helper);
    }

    @Override
    public ChildContainerHelper getChildContainerHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
        return getHelperRegistry().getChildContainerHelper(modelNodeSchemaPath, helperName);
    }

    @Override
    public Map<QName, ChildContainerHelper> getChildContainerHelpers(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().getChildContainerHelpers(modelNodeSchemaPath);
    }

    @Override
    public void registerNaturalKeyHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name,
                                         ConfigAttributeHelper helper) {
        getHelperRegistry().registerNaturalKeyHelper(componentId, modelNodeSchemaPath, name, helper);
    }

    @Override
    public ConfigAttributeHelper getNaturalKeyHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
        return getHelperRegistry().getNaturalKeyHelper(modelNodeSchemaPath, helperName);
    }

    @Override
    public void registerConfigAttributeHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name,
                                              ConfigAttributeHelper helper) {
        getHelperRegistry().registerConfigAttributeHelper(componentId, modelNodeSchemaPath, name, helper);
    }

    @Override
    public ConfigAttributeHelper getConfigAttributeHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
        return getHelperRegistry().getConfigAttributeHelper(modelNodeSchemaPath, helperName);
    }

    @Override
    public Map<QName, ConfigAttributeHelper> getConfigAttributeHelpers(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().getConfigAttributeHelpers(modelNodeSchemaPath);
    }

    @Override
    public Map<QName, ConfigAttributeHelper> getNaturalKeyHelpers(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().getNaturalKeyHelpers(modelNodeSchemaPath);
    }

    @Override
    public ChildLeafListHelper getConfigLeafListHelper(SchemaPath modelNodeSchemaPath, QName helperName) {
        return getHelperRegistry().getConfigLeafListHelper(modelNodeSchemaPath, helperName);
    }

    @Override
    public Map<QName, ChildLeafListHelper> getConfigLeafListHelpers(SchemaPath modelNodeSchemaPath) {
        return getHelperRegistry().getConfigLeafListHelpers(modelNodeSchemaPath);
    }

    @Override
    public void registerConfigLeafListHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name,
                                             ChildLeafListHelper childLeafListHelper) {
        getHelperRegistry().registerConfigLeafListHelper(componentId, modelNodeSchemaPath, name, childLeafListHelper);
    }

    @Override
    public DefaultCapabilityCommandInterceptor getDefaultCapabilityCommandInterceptor() {
        return getHelperRegistry().getDefaultCapabilityCommandInterceptor();
    }

    @Override
    public void setDefaultCapabilityCommandInterceptor(DefaultCapabilityCommandInterceptor defaultCapabilityCommandInterceptor) {
        getHelperRegistry().setDefaultCapabilityCommandInterceptor(defaultCapabilityCommandInterceptor);
    }

    @Override
    public void undeploy(String componentId) {
        getHelperRegistry().undeploy(componentId);
    }

    @Override
    public void resetDefaultCapabilityCommandInterceptor() {
        getHelperRegistry().resetDefaultCapabilityCommandInterceptor();
    }

    @Override
    public void clear() {
        getHelperRegistry().clear();
    }

    public static void clearRegistry() {
        c_registry.remove();
    }

    @Override
    public ModelNodeHelperRegistry unwrap() {
        return getHelperRegistry().unwrap();
    }
}
