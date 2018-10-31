package org.broadband_forum.obbaa.aggregator.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfProtocol;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.api.YangLibraryMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.DataStores;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.Module;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.ModuleSet;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.ModuleSets;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.Modules;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.YangLibrary;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.state.ModulesState;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IetfYangLibrary implements GlobalRequestProcessor {
    private Aggregator m_aggregator;

    private static final String MODULE_ALL = "all";
    private static final Logger LOGGER = LoggerFactory.getLogger(IetfYangLibrary.class);
    private static final String IETF_YANG_LIBRARY = "urn:ietf:params:xml:ns:yang:ietf-yang-library";

    private IetfYangLibrary() {
        //Hide
    }

    public IetfYangLibrary(Aggregator aggregator) {
        this.m_aggregator = aggregator;
    }

    public void init() {
        try {
            m_aggregator.addProcessor(buildModuleIdentifiers(), this);
        }
        catch (DispatchException ex) {
            LOGGER.error("IetfYangLibrary init failed");
        }
    }

    public void destroy() {
        try {
            m_aggregator.removeProcessor(this);
        }
        catch (DispatchException ex) {
            LOGGER.error("IetfYangLibrary destroy failed");
        }
    }

    private Set<ModuleIdentifier> buildModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier("ietf-yang-library",
                IETF_YANG_LIBRARY, "2017-10-30");
        moduleIdentifiers.add(moduleIdentifier);

        return moduleIdentifiers;
    }

    @Override
    public String processRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        LOGGER.info(netconfRequest);
        try {
            YangLibraryMessage yangLibraryMessage = new YangLibraryMessage(netconfRequest);
            buildPayload(yangLibraryMessage);
            return yangLibraryMessage.buildRpcReplyResponse();
        }
        catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    /**
     * Build payload of YANG library. The root container can not be null in sub-tree of filter.
     *
     * @param yangLibraryMessage YANG library message
     */
    private void buildPayload(YangLibraryMessage yangLibraryMessage) {
        NetconfProtocol.buildNonEmpty(yangLibraryMessage, () -> {
            buildYangLibrary(yangLibraryMessage.getYangLibrary());
            buildModulesState(yangLibraryMessage.getModulesState());
        });
    }

    private void buildYangLibrary(YangLibrary yangLibrary) {
        NetconfProtocol.buildNonEmpty(yangLibrary, () -> {
            prepareYangLibrary(yangLibrary);

            buildModules(yangLibrary.getModules());
            buildModuleSets(yangLibrary.getModuleSets());
            buildDataStores(yangLibrary.getDataStores());
            buildCheckSum(yangLibrary.getCheckSum());
        });
    }

    private void prepareYangLibrary(YangLibrary yangLibrary) {
        if (JaxbUtils.isObjectContainNull(yangLibrary)) {
            createYangLibraryData(yangLibrary);
        }
    }

    private void createYangLibraryData(YangLibrary yangLibrary) {
        yangLibrary.setModules(new Modules());
        yangLibrary.setModuleSets(new ModuleSets());
        yangLibrary.setDataStores(new DataStores());
        yangLibrary.setCheckSum(new String());
    }

    private void buildModules(Modules modules) {
        NetconfProtocol.buildNonEmpty(modules, () -> {
            Set<ModuleIdentifier> moduleIdentifiers = getAggregator().getModuleIdentifiers();
            if ((modules.getModules() == null) || modules.getModules().isEmpty()) {
                buildModulesAll(modules, moduleIdentifiers);
            }
            else {
                buildModulesFiltered(modules, moduleIdentifiers);
            }
        });
    }

    private void buildModulesAll(Modules modules, Set<ModuleIdentifier> moduleIdentifiers) {
        List<Module> moduleList = new ArrayList<>();

        for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
            moduleList.add(buildModule(moduleIdentifier));
        }

        modules.setModules(moduleList);
    }

    private void buildModuleValue(Module module, ModuleIdentifier moduleIdentifier) {
        module.setId(buildModuleId(moduleIdentifier));
        module.setName(moduleIdentifier.getName());
        module.setRevision(moduleIdentifier.getQNameModule().getFormattedRevision());
        module.setNamespace(moduleIdentifier.getNamespace().toString());
    }

    private Module buildModule(ModuleIdentifier moduleIdentifier) {
        Module module = new Module();
        buildModuleValue(module, moduleIdentifier);
        return module;
    }

    private String buildModuleId(ModuleIdentifier moduleIdentifier) {
        return NetconfProtocol.buildCapability(moduleIdentifier);
    }

    private void buildModulesFiltered(Modules modules, Set<ModuleIdentifier> moduleIdentifiers) {
        for (Module module : modules.getModules()) {
            buildModuleFiltered(module, moduleIdentifiers);
        }
    }

    private void buildModuleFiltered(Module module, Set<ModuleIdentifier> moduleIdentifiers) {
        for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
            buildModuleFiltered(module, moduleIdentifier);
        }
    }

    private void buildModuleFiltered(Module module, ModuleIdentifier moduleIdentifier) {
        if (isModuleFiltered(module, moduleIdentifier)) {
            return;
        }

        buildModuleValue(module, moduleIdentifier);
    }

    private boolean isModuleFiltered(Module module, ModuleIdentifier moduleIdentifier) {
        // Just support filtered main value
        if ((isFiltered(module.getId(), buildModuleId(moduleIdentifier)))
                || (isFiltered(module.getName(), moduleIdentifier.getName()))
                || (isFiltered(module.getNamespace(), moduleIdentifier.getNamespace().toString()))
                || (isFiltered(module.getRevision(), moduleIdentifier.getQNameModule().getFormattedRevision()))) {
            return true;
        }

        return true;
    }

    private boolean isFiltered(String input, String source) {
        if ((input != null) && (input.equals(source))) {
            return true;
        }

        return false;
    }

    private void buildModuleSets(ModuleSets moduleSets) {
        NetconfProtocol.buildNonEmpty(moduleSets, () -> {
            Set<ProcessorCapability> capabilities = getAggregator().getProcessorCapabilities();
            if ((moduleSets.getModuleSets() == null) || moduleSets.getModuleSets().isEmpty()) {
                buildModuleSetsAll(moduleSets, capabilities);
            }
            else {
                buildModuleSetsFiltered(moduleSets, capabilities);
            }
        });
    }

    private void buildModuleSetsAll(ModuleSets moduleSets, Set<ProcessorCapability> capabilities) {
        List<ModuleSet> moduleSetList = new ArrayList<>();

        for (ProcessorCapability processorCapability : capabilities) {
            moduleSetList.add(buildModuleSet(processorCapability));
        }

        moduleSets.setModuleSets(moduleSetList);
    }

    private ModuleSet buildModuleSet(ProcessorCapability processorCapability) {
        ModuleSet moduleSet = new ModuleSet();
        buildModuleSetValue(moduleSet, processorCapability);
        return moduleSet;
    }

    private void buildModuleSetValue(ModuleSet moduleSet, ProcessorCapability processorCapability) {
        moduleSet.setId(processorCapability.getDeviceType());
        List<String> modules = new ArrayList<>();
        Set<ModuleIdentifier> moduleIdentifiers = processorCapability.getModuleIdentifiers();
        for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
            modules.add(buildModuleId(moduleIdentifier));
        }

        moduleSet.setModule(modules);
    }

    private void buildModuleSetsFiltered(ModuleSets moduleSets, Set<ProcessorCapability> capabilities) {
        List<ModuleSet> moduleSetList = moduleSets.getModuleSets();
        for (ModuleSet moduleSet : moduleSetList) {
            for (ProcessorCapability processorCapability : capabilities) {
                buildModuleSetFiltered(moduleSet, processorCapability);
            }
        }
    }

    private void buildModuleSetFiltered(ModuleSet moduleSet, ProcessorCapability processorCapability) {
        if (isModuleSetFiltered(moduleSet, processorCapability)) {
            return;
        }

        buildModuleSetValue(moduleSet, processorCapability);
    }

    private boolean isModuleSetFiltered(ModuleSet moduleSet, ProcessorCapability processorCapability) {
        if ((moduleSet.getId() != null) && (moduleSet.getId().equals(processorCapability.getDeviceType()))) {
            return false;
        }

        return true;
    }

    private void buildDataStores(DataStores dataStores) {
        NetconfProtocol.buildNonEmpty(dataStores, () -> {
            //TODO : don't support in the version
        });
    }

    private void buildCheckSum(String checkSum) {
        NetconfProtocol.buildNonEmpty(checkSum, () -> {
            //TODO : don't support in the version
        });
    }

    private void buildModulesState(ModulesState modulesState) {
        NetconfProtocol.buildNonEmpty(modulesState, () -> {
            buildModuleSetId(modulesState);
            buildModulesForState(modulesState);
        });
    }

    private void buildModuleSetId(ModulesState modulesState) {
        if ((modulesState.getModuleSetId() == null) || modulesState.getModuleSetId().isEmpty()) {
            modulesState.setModuleSetId(MODULE_ALL);
        }
    }

    private void buildModulesForState(ModulesState modulesState) {
        if (modulesState.getModuleSetId().equals(MODULE_ALL)) {
            buildModulesAllSets(modulesState);
        }
        else {
            buildModulesByDeviceType(modulesState.getModuleSetId(), modulesState);
        }

        invalidIdForState(modulesState);
    }

    private void invalidIdForState(ModulesState modulesState) {
        if (modulesState.getModules() == null) {
            return;
        }

        for (Module module : modulesState.getModules()) {
            module.setId(null);
        }
    }

    private void buildModulesAllSets(ModulesState modulesState) {
        buildModulesInState(modulesState, getAggregator().getModuleIdentifiers());
    }

    private Set<ModuleIdentifier> getModuleIdentifiersByDeviceType(String deviceType) {
        Set<ProcessorCapability> capabilities = getAggregator().getProcessorCapabilities();
        Iterator<ProcessorCapability> iterator = capabilities.iterator();
        while (iterator.hasNext()) {
            ProcessorCapability capability = iterator.next();
            if (capability.getDeviceType().equals(deviceType)) {
                return capability.getModuleIdentifiers();
            }
        }

        return new HashSet<>();
    }

    private void buildModulesByDeviceType(String moduleSetId, ModulesState modulesState) {
        buildModulesInState(modulesState, getModuleIdentifiersByDeviceType(moduleSetId));
    }

    private void buildModulesInState(ModulesState modulesState, Set<ModuleIdentifier> identifiers) {
        Modules modules = new Modules();

        List<Module> modulesInput = modulesState.getModules();
        if (modulesInput == null || modulesInput.isEmpty()) {
            buildModulesAll(modules, identifiers);
        }
        else {
            modules.setModules(modulesInput);
            buildModulesFiltered(modules, identifiers);
        }

        modulesState.setModules(modules.getModules());
    }

    public Aggregator getAggregator() {
        return m_aggregator;
    }
}
