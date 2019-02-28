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

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CopyConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public class ThreadLocalRootModelNodeAggregator implements RootModelNodeAggregator {

    private static ThreadLocal<RootModelNodeAggregatorImpl> c_rootAggregator = new ThreadLocal<>();

    public static void setRootAggregator(RootModelNodeAggregatorImpl registry) {
        c_rootAggregator.set(registry);
    }

    public static RootModelNodeAggregatorImpl getRootAggregator() {
        return c_rootAggregator.get();
    }

    @Override
    public RootModelNodeAggregator addModelServiceRoot(String componentId, ModelNode modelNode) {
        return getRootAggregator().addModelServiceRoot(componentId, modelNode);
    }

    @Override
    public void addModelServiceRoots(String componentId, List<ModelNode> modelNodes) {
        getRootAggregator().addModelServiceRoots(componentId, modelNodes);
    }

    @Override
    public List<ModelNode> getModelServiceRoots() {
        return getRootAggregator().getModelServiceRoots();
    }

    @Override
    public List<ModelNode> getModelServiceRootsForEdit(EditConfigRequest request) {
        return getRootAggregator().getModelServiceRootsForEdit(request);
    }

    @Override
    public List<ModelNode> getModuleRootFromHelpers(String requiredElementNamespace, String requiredElementLocalName) {
        return getRootAggregator().getModuleRootFromHelpers(requiredElementNamespace,requiredElementLocalName);
    }

    @Override
    public List<Element> get(GetContext getContext, NetconfQueryParams params) throws GetException {
        return getRootAggregator().get(getContext, params);
    }

    @Override
    public List<Element> action(ActionRequest actionRequest) throws ActionException {
        return getRootAggregator().action(actionRequest);
    }

    @Override
    public List<Element> getConfig(GetConfigContext getConfigContext, NetconfQueryParams params) throws GetException {
        return getRootAggregator().getConfig(getConfigContext, params);
    }

    @Override
    public List<EditContainmentNode> editConfig(EditConfigRequest request, NotificationContext notificationContext)
            throws EditConfigException {
        return getRootAggregator().editConfig(request, notificationContext);
    }

    @Override
    public void copyConfig(List<Element> configElements) throws CopyConfigException {
        getRootAggregator().copyConfig(configElements);
    }

    @Override
    public void addModelServiceRootHelper(SchemaPath rootNodeSchemaPath, ChildContainerHelper rootNodeHelper) {
        getRootAggregator().addModelServiceRootHelper(rootNodeSchemaPath, rootNodeHelper);
    }

    @Override
    public void addModelServiceRootHelper(SchemaPath rootNodeSchemaPath, ChildListHelper rootNodeHelper) {
        getRootAggregator().addModelServiceRootHelper(rootNodeSchemaPath, rootNodeHelper);
    }

    @Override
    public void removeModelServiceRootHelpers(SchemaPath rootSchemaPath) {
        getRootAggregator().removeModelServiceRootHelpers(rootSchemaPath);
    }

    @Override
    public void removeModelServiceRoot(String componentId) {
        getRootAggregator().removeModelServiceRoot(componentId);
    }

    @Override
    public List<Pair<String, String>> getRootNodeNsLocalNamePairs() {
        return getRootAggregator().getRootNodeNsLocalNamePairs();
    }

    public static void clearRootAggregator() {
        c_rootAggregator.remove();
    }
}
