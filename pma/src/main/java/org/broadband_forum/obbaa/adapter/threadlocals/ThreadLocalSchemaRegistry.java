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

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class ThreadLocalSchemaRegistry implements SchemaRegistry {

    private static ThreadLocal<SchemaRegistryImpl> c_registry = new ThreadLocal<>();

    public static void setSchemaRegistry(SchemaRegistryImpl registry) {
        c_registry.set(registry);
    }

    public static SchemaRegistryImpl getRegistry() {
        return c_registry.get();
    }

    @Override
    public void buildSchemaContext(List<YangTextSchemaSource> coreYangModelFiles) throws SchemaBuildException {
        getRegistry().buildSchemaContext(coreYangModelFiles);

    }

    @Override
    public void buildSchemaContext(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures,
                                   Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        getRegistry().buildSchemaContext(coreYangModelFiles, supportedFeatures, supportedDeviations);
    }

    @Override
    public Collection<DataSchemaNode> getRootDataSchemaNodes() {
        return getRegistry().getRootDataSchemaNodes();
    }

    @Override
    public Module getModule(String name, String revision) {
        return getRegistry().getModule(name, revision);
    }

    @Override
    public Module getModule(String name, Date revision) {
        return getRegistry().getModule(name, revision);
    }

    @Override
    public Module getModule(String name) {
        return getRegistry().getModule(name);
    }

    @Override
    public Set<SchemaPath> getRootSchemaPaths() {
        return getRegistry().getRootSchemaPaths();
    }

    @Override
    public Collection<RpcDefinition> getRpcDefinitions() {
        return getRegistry().getRpcDefinitions();
    }

    @Override
    public RpcDefinition getRpcDefinition(SchemaPath schemaPath) {
        return getRegistry().getRpcDefinition(schemaPath);
    }

    @Override
    public void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures,
                                  Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        getRegistry().loadSchemaContext(componentId, yangModelFiles, supportedFeatures, supportedDeviations);
    }

    @Override
    public void unloadSchemaContext(String componentId, Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        getRegistry().unloadSchemaContext(componentId, supportedDeviations);
    }

    @Override
    public DataSchemaNode getDataSchemaNode(SchemaPath dataNodeSchemaPath) {
        return getRegistry().getDataSchemaNode(dataNodeSchemaPath);
    }

    @Override
    public DataSchemaNode getDataSchemaNode(List<QName> paths) {
        return getRegistry().getDataSchemaNode(paths);
    }

    @Override
    public SchemaNode getActionDefinitionNode(List<QName> path) {
        return getRegistry().getActionDefinitionNode(path);
    }

    @Override
    public Collection<DataSchemaNode> getChildren(SchemaPath parentSchemaPath) {
        return getRegistry().getChildren(parentSchemaPath);
    }

    @Override
    public DataSchemaNode getChild(SchemaPath pathSchemaPath, QName childQName) {
        return getRegistry().getChild(pathSchemaPath, childQName);
    }

    @Override
    public Collection<DataSchemaNode> getNonChoiceChildren(SchemaPath parentSchemaPath) {
        return getRegistry().getNonChoiceChildren(parentSchemaPath);
    }

    @Override
    public DataSchemaNode getNonChoiceChild(SchemaPath parentSchemaPath, QName name) {
        return getRegistry().getNonChoiceChild(parentSchemaPath, name);
    }

    @Override
    public SchemaPath getDescendantSchemaPath(SchemaPath parentSchemaPath, QName name) {
        return getRegistry().getDescendantSchemaPath(parentSchemaPath, name);
    }

    @Override
    public DataSchemaNode getNonChoiceParent(SchemaPath schemaPath) {
        return getRegistry().getNonChoiceParent(schemaPath);
    }

    @Override
    public boolean isKnownNamespace(String namespaceURI) {
        return getRegistry().isKnownNamespace(namespaceURI);
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        return getRegistry().getAllModuleIdentifiers();
    }

    @Override
    public QName lookupQName(String namespace, String localName) {
        return getRegistry().lookupQName(namespace, localName);
    }

    @Override
    public SchemaContext getSchemaContext() {
        return getRegistry().getSchemaContext();
    }

    @Override
    public String getModuleNameByNamespace(String namespace) {
        return getRegistry().getModuleNameByNamespace(namespace);
    }

    @Override
    public String getNamespaceOfModule(String moduleName) {
        return getRegistry().getNamespaceOfModule(moduleName);
    }

    @Override
    public Module getModuleByNamespace(String namespace) {
        return getRegistry().getModuleByNamespace(namespace);
    }

    @Override
    public Module findModuleByNamespaceAndRevision(URI namespace, Date revision) {
        return getRegistry().findModuleByNamespaceAndRevision(namespace, revision);
    }

    @Override
    public void registerNodesReferencedInConstraints(String componentId, SchemaPath constraintSchemaPath,
                                                     SchemaPath nodeSchemaPath, String accessPath) {
        getRegistry().registerNodesReferencedInConstraints(componentId, constraintSchemaPath, nodeSchemaPath, accessPath);
    }

    @Override
    public void deRegisterNodesReferencedInConstraints(String componentId) {
        getRegistry().deRegisterNodesReferencedInConstraints(componentId);
    }

    @Override
    public Collection<SchemaPath> getSchemaPathsForComponent(String componentId) {
        return getRegistry().getSchemaPathsForComponent(componentId);
    }

    @Override
    public Map<SchemaPath, Expression> getReferencedNodesForSchemaPaths(SchemaPath schemaPath) {
        return getRegistry().getReferencedNodesForSchemaPaths(schemaPath);
    }

    @Override
    public Map<SourceIdentifier, YangTextSchemaSource> getAllYangTextSchemaSources() throws SchemaBuildException {
        return getRegistry().getAllYangTextSchemaSources();
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleAndSubmoduleIdentifiers() {
        return getRegistry().getAllModuleAndSubmoduleIdentifiers();
    }

    @Override
    public void registerAppAllowedAugmentedPath(String componentId, String path, SchemaPath schemaPath) {
        getRegistry().registerAppAllowedAugmentedPath(componentId, path, schemaPath);
    }

    @Override
    public void deRegisterAppAllowedAugmentedPath(String path) {
        getRegistry().deRegisterAppAllowedAugmentedPath(path);
    }

    @Override
    public String getMatchingPath(String path) {
        return getRegistry().getMatchingPath(path);
    }

    @Override
    public void registerRelativePath(String augmentedPath, String relativePath, DataSchemaNode schemaNode) {
        getRegistry().registerRelativePath(augmentedPath, relativePath, schemaNode);
    }

    @Override
    public Expression getRelativePath(String augmentPath, DataSchemaNode dataSchemaNode) {
        return getRegistry().getRelativePath(augmentPath, dataSchemaNode);
    }

    @Override
    public boolean isYangLibrarySupportedInHelloMessage() {
        return getRegistry().isYangLibrarySupportedInHelloMessage();
    }

    @Override
    public Set<String> getModuleCapabilities(boolean forHello) {
        return getRegistry().getModuleCapabilities(forHello);
    }

    @Override
    public String getCapability(ModuleIdentifier moduleId) {
        return getRegistry().getCapability(moduleId);
    }

    @Override
    public String getModuleSetId() {
        return getRegistry().getModuleSetId();
    }

    @Override
    public Set<Module> getAllModules() {
        return getRegistry().getAllModules();
    }

    @Override
    public void registerYangLibraryChangeNotificationListener(YangLibraryChangeNotificationListener listener) {
        getRegistry().registerYangLibraryChangeNotificationListener(listener);
    }

    @Override
    public void unregisterYangLibraryChangeNotificationListener() {
        getRegistry().unregisterYangLibraryChangeNotificationListener();
    }

    @Override
    public Map<SchemaPath, String> retrieveAppAugmentedPathToComponent() {
        return getRegistry().retrieveAppAugmentedPathToComponent();
    }

    @Override
    public List<YangTextSchemaSource> getYangModelByteSourcesOfAPlugin(String componentId) {
        return getRegistry().getYangModelByteSourcesOfAPlugin(componentId);
    }

    @Override
    public void registerActionSchemaNode(String componentId, SchemaPath nodeSchemaPath, Set<ActionDefinition> actionDefinitions) {
        getRegistry().registerActionSchemaNode(componentId, nodeSchemaPath, actionDefinitions);
    }

    @Override
    public void deRegisterActionSchemaNodes(String componentId) {
        getRegistry().deRegisterActionSchemaNodes(componentId);
    }

    @Override
    public Set<ActionDefinition> retrieveAllActionDefinitions() {
        return getRegistry().retrieveAllActionDefinitions();
    }

    @Override
    public Map<ModuleIdentifier, Set<QName>> getSupportedDeviations() {
        return getRegistry().getSupportedDeviations();
    }

    @Override
    public DataSchemaNode getRPCInputChildNode(RpcDefinition rpcDef, List<QName> qnames) {
        return getRegistry().getRPCInputChildNode(rpcDef, qnames);
    }

    @Override
    public Map<QName, DataSchemaNode> getIndexedChildren(SchemaPath parentSchemaPath) {
        return getRegistry().getIndexedChildren(parentSchemaPath);
    }

    @Override
    public String getNamespaceURI(String string) {
        return getRegistry().getNamespaceURI(string);
    }

    @Override
    public String getPrefix(String string) {
        return getRegistry().getPrefix(string);
    }

    @Override
    public Iterator getPrefixes(String string) {
        return getRegistry().getPrefixes(string);
    }

    public static void clearRegistry() {
        c_registry.remove();
    }
}
