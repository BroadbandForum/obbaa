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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ImpactNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNodes;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.TreeImpactNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
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

    public static void clearRegistry() {
        c_registry.remove();
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
    public void buildSchemaContext(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures,
                                   Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported)
            throws SchemaBuildException {
        getRegistry().buildSchemaContext(coreYangModelFiles, supportedFeatures, supportedDeviations, isYangLibNotificationSupported);
    }

    @Override
    public Collection<DataSchemaNode> getRootDataSchemaNodes() {
        return getRegistry().getRootDataSchemaNodes();
    }

    @Override
    public Optional<Module> getModule(String name, Revision revision) {
        return getRegistry().getModule(name, revision);
    }

    @Override
    public Optional<Module> getModule(String name) {
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
    public void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures, Map<QName,
            Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        getRegistry().loadSchemaContext(componentId, yangModelFiles, supportedFeatures, supportedDeviations,
                isYangLibNotificationSupported);
    }

    @Override
    public void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures, Map<QName,
            Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported,
                                  boolean isYangLibraryIgnored) throws SchemaBuildException {
        getRegistry().loadSchemaContext(componentId, yangModelFiles, supportedFeatures, supportedDeviations,
                isYangLibNotificationSupported, isYangLibraryIgnored);
    }

    @Override
    public void unloadSchemaContext(String componentId, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations)
            throws SchemaBuildException {
        getRegistry().unloadSchemaContext(componentId, supportedFeatures, supportedDeviations);
    }

    @Override
    public void unloadSchemaContext(String componentId, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations,
                                    boolean isYangLibNotificationSupported) throws SchemaBuildException {
        getRegistry().unloadSchemaContext(componentId, supportedFeatures, supportedDeviations, isYangLibNotificationSupported);
    }

    @Override
    public void unloadSchemaContext(String componentId, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations,
                                    boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException {
        getRegistry().unloadSchemaContext(componentId, supportedFeatures,  supportedDeviations, isYangLibNotificationSupported,
                isYangLibraryIgnored);

    }

    @Override
    public Map<SchemaPath, DataSchemaNode> getSchemaNodes() {
        return getRegistry().getSchemaNodes();
    }

    @Override
    public ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> getImpactNodeMap() {
        return getRegistry().getImpactNodeMap();
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
    public ActionDefinition getActionDefinitionNode(DataPath actionDataPath) {
        return getRegistry().getActionDefinitionNode(actionDataPath);
    }

    @Override
    public NotificationDefinition getNotificationDefinitionNode(List<QName> path) {
        return getRegistry().getNotificationDefinitionNode(path);
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
    public String getComponentIdByNamespace(String namespace) {
        return getRegistry().getComponentIdByNamespace(namespace);
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
    public Optional<Module> findModuleByNamespaceAndRevision(URI namespace, Revision revision) {
        return getRegistry().findModuleByNamespaceAndRevision(namespace, revision);
    }

    @Override
    public void registerNodesReferredInConstraints(String componentId, ReferringNode referringNode) {
        getRegistry().registerNodesReferredInConstraints(componentId, referringNode);
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
    public ReferringNodes getReferringNodesForSchemaPath(SchemaPath schemaPath) {
        return getRegistry().getReferringNodesForSchemaPath(schemaPath);
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
    public void addToChildBigList(SchemaPath schemaPath) {
        getRegistry().addToChildBigList(schemaPath);
    }

    @Override
    public boolean isChildBigList(SchemaPath schemaPath) {
        return getRegistry().isChildBigList(schemaPath);
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
    public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, Expression expression) {
        return getRegistry().getExpressionWithModuleNameInPrefix(schemaPath, expression);
    }

    @Override
    public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression) {
        return getRegistry().getExpressionWithModuleNameInPrefix(schemaPath, expression);
    }

    @Override
    public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression, String expressionWithPrefix) {
        getRegistry().registerExpressionWithModuleNameInPrefix(schemaPath, expression, expressionWithPrefix);
    }

    @Override
    public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath, Expression expression, String expressionWithPrefix) {
        getRegistry().registerExpressionWithModuleNameInPrefix(schemaPath, expression, expressionWithPrefix);
    }

    @Override
    public Set<String> getAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath) {
        return getRegistry().getAttributesWithSameLocalNameDifferentNameSpace(schemaPath);
    }

    @Override
    public void registerAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath,
                                                                      Set<String> attributesWithSameLocalNameDifferentNameSpace) {
        getRegistry().registerAttributesWithSameLocalNameDifferentNameSpace(schemaPath,attributesWithSameLocalNameDifferentNameSpace);
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
    public Set<ActionDefinition> retrieveAllActionDefinitions() {
        return getRegistry().retrieveAllActionDefinitions();
    }

    @Override
    public Set<NotificationDefinition> retrieveAllNotificationDefinitions() {
        return getRegistry().retrieveAllNotificationDefinitions();
    }

    @Override
    public Map<ModuleIdentifier, Set<QName>> getSupportedDeviations() {
        return getRegistry().getSupportedDeviations();
    }

    @Override
    public Map<ModuleIdentifier, Set<QName>> getSupportedFeatures() {
        return getRegistry().getSupportedFeatures();
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
    public SchemaMountRegistry getMountRegistry() {
        return getRegistry().getMountRegistry();
    }

    @Override
    public SchemaPath getMountPath() {
        return getRegistry().getMountPath();
    }

    @Override
    public void setMountPath(SchemaPath mountPath) {
        getRegistry().setMountPath(mountPath);
    }

    @Override
    public SchemaPath stripRevisions(SchemaPath schemaPath) {
        return getRegistry().stripRevisions(schemaPath);
    }

    @Override
    public SchemaPath addRevisions(SchemaPath schemaPath) {
        return getRegistry().addRevisions(schemaPath);
    }

    @Override
    public void registerMountPointSchemaPath(String componentId, DataSchemaNode schemaNode) {
        getRegistry().registerMountPointSchemaPath(componentId, schemaNode);
    }

    @Override
    public void unregisterMountPointSchemaPath(String componentId) {
        getRegistry().unregisterMountPointSchemaPath(componentId);
    }

    @Override
    public Set<QName> retrieveAllMountPointsPath() {
        return getRegistry().retrieveAllMountPointsPath();
    }

    @Override
    public Collection<DataSchemaNode> retrieveAllNodesWithMountPointExtension() {
        return getRegistry().retrieveAllNodesWithMountPointExtension();
    }

    @Override
    public SchemaRegistry getParentRegistry() {
        return getRegistry().getParentRegistry();
    }

    @Override
    public void setParentRegistry(SchemaRegistry parent) {
        getRegistry().setParentRegistry(parent);
    }

    @Override
    public void putValidator(TypeDefinition<?> type, TypeValidator typeValidator) {
        getRegistry().putValidator(type, typeValidator);
    }

    @Override
    public TypeValidator getValidator(TypeDefinition<?> type) {
        return getRegistry().getValidator(type);
    }

    @Override
    public SchemaNodeConstraintParser getSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode) {
        return getRegistry().getSchemaNodeConstraintParser(dataSchemaNode);
    }

    @Override
    public void putSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode, SchemaNodeConstraintParser schemaNodeConstraintParser) {
        getRegistry().putSchemaNodeConstraintParser(dataSchemaNode, schemaNodeConstraintParser);
    }

    @Override
    public void setName(String schemaRegistryName) {
        getRegistry().setName(schemaRegistryName);
    }

    @Override
    public String getName() {
        return getRegistry().getName();
    }

    @Override
    public ReferringNodes addChildImpactPaths(SchemaPath schemaPath, Set<QName> skipImmediateChildQNames) {
        return getRegistry().addChildImpactPaths(schemaPath, skipImmediateChildQNames);
    }

    @Override
    public void registerWhenReferringNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath,
                                           String accessPath) {
        getRegistry().registerWhenReferringNodes(componentId, referencedSchemaPath, nodeSchemaPath, accessPath);
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodes(SchemaPath nodeSchemaPath) {
        return getRegistry().getWhenReferringNodes(nodeSchemaPath);
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodes(String componentId, SchemaPath nodeSchemaPath) {
        return getRegistry().getWhenReferringNodes(componentId, nodeSchemaPath);
    }

    @Override
    public void registerWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath,
                                                            SchemaPath nodeSchemaPath, String accessPath) {
        getRegistry().registerWhenReferringNodesForAllSchemaNodes(componentId, referencedSchemaPath, nodeSchemaPath, accessPath);
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath) {
        return getRegistry().getWhenReferringNodesForAllSchemaNodes(componentId, nodeSchemaPath);
    }

    @Override
    public void registerMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath,
                                                            SchemaPath nodeSchemaPath, String accessPath) {
        getRegistry().registerMustReferringNodesForAllSchemaNodes(componentId, referencedSchemaPath, nodeSchemaPath, accessPath);
    }

    @Override
    public Map<SchemaPath, Expression> getMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath) {
        return getRegistry().getMustReferringNodesForAllSchemaNodes(componentId, nodeSchemaPath);
    }

    @Override
    public String getShortPath(SchemaPath path) {
        return getRegistry().getShortPath(path);
    }

    @Override
    public void addToSkipValidationPaths(SchemaPath schemaPath, String constraintXpath) {
        getRegistry().addToSkipValidationPaths(schemaPath, constraintXpath);
    }

    @Override
    public boolean isSkipValidationBySchemaPathWithConstraintXpath(SchemaPath schemaPath, String constraintXpath) {
        return getRegistry().isSkipValidationBySchemaPathWithConstraintXpath(schemaPath, constraintXpath);
    }

    @Override
    public boolean isSkipValidationPath(SchemaPath schemaPath) {
        return getRegistry().isSkipValidationPath(schemaPath);
    }

    @Override
    public void addExpressionsWithoutKeysInList(String fullExpression, LocationPath expressionWithListAtLowerLevel) {
        getRegistry().addExpressionsWithoutKeysInList(fullExpression, expressionWithListAtLowerLevel);
    }

    @Override
    public Set<LocationPath> getExpressionsWithoutKeysInList(String fullExpression) {
        return getRegistry().getExpressionsWithoutKeysInList(fullExpression);
    }

    @Override
    public void printReferringNodes() {
        getRegistry().printReferringNodes();
    }

    @Override
    public void addImpactNodeForChild(SchemaPath schemaPath, ReferringNodes impactedPaths, Set<QName> skipImmediateChildQNames) {
        getRegistry().addImpactNodeForChild(schemaPath, impactedPaths, skipImmediateChildQNames);
    }

    @Override
    public Map<SchemaPath, TreeImpactNode<ImpactNode>> getReferringNodes() {
        return getRegistry().getReferringNodes();
    }

    @Override
    public void registerNodeConstraintDefinedModule(DataSchemaNode schemaNode, String constraint, Module module) {
        getRegistry().registerNodeConstraintDefinedModule(schemaNode, constraint, module);
    }

    @Override
    public Map<Expression, Module> getNodeConstraintDefinedModule(DataSchemaNode schemaNode) {
        return getRegistry().getNodeConstraintDefinedModule(schemaNode);
    }

    @Override
    public YinAnnotationService getYinAnnotationService() {
        return getRegistry().getYinAnnotationService();
    }

    @Override
    public DataSchemaNode findChild(DataSchemaNode currentNode, QName qname) {
        return getRegistry().findChild(currentNode, qname);
    }

    @Override
    public void clear() {
        getRegistry().clear();
    }

    @Override
    public Set<ActionDefinition> getActionDefinitionNodesWithListAndLeafRef() {
        return getRegistry().getActionDefinitionNodesWithListAndLeafRef();
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

    @Override
    public SchemaRegistry unwrap() {
        return getRegistry().unwrap();
    }
}
