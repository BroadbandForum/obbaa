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

package org.broadband_forum.obbaa.device.adapter.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.broadband_forum.obbaa.device.adapter.YangLibraryModulePojo;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ietfParamsXmlNsYangIetfYangLibrary.ModulesStateDocument.ModulesState;
import ietfParamsXmlNsYangIetfYangLibrary.ModulesStateDocument.ModulesState.Module;
import ietfParamsXmlNsYangIetfYangLibrary.ModulesStateDocument.ModulesState.Module.Deviation;
import ietfParamsXmlNsYangIetfYangLibrary.ModulesStateDocument.ModulesState.Module.Submodule;
import x0.ietfParamsXmlNsNetconfBase1.RpcReplyDocument;


public final class YangLibraryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(YangLibraryUtils.class);

    private YangLibraryUtils() {
    }

    public static YangLibraryModulePojo getYangModulePojo(String yangLibraryPath) throws Exception {
        Path path = Paths.get(yangLibraryPath);
        File yangLibraryFile = path.toFile();
        return getVariantYangModulePojo(yangLibraryFile, null);
    }

    public static YangLibraryModulePojo getVariantYangModulePojo(File yangLibraryFile, String variant) throws Exception {
        RpcReplyDocument moduleStateDoc = RpcReplyDocument.Factory.parse(yangLibraryFile);
        ModulesState moduleState = moduleStateDoc.getRpcReply().getData().getModulesState();
        List<Module> moduleList = moduleState.getModuleList();
        List<QName> modules = new ArrayList<>();
        List<QName> supportedFeatures = new ArrayList<>();
        Map<QName, List<QName>> supportedDeviations = new HashMap<>();
        Map<QName, List<Pair<String, String>>> affectedModuleDeviations = new HashMap<>();

        for (Module module : moduleList) {
            String name = module.getName();
            String moduleRevison = module.getRevision();
            String revision = module.getRevision() == null ? "" : moduleRevison;
            String namespace = module.getNamespace();
            QName moduleQName = getQName(name, revision, namespace);
            modules.add(moduleQName);

            fillSubModuleList(modules, module, namespace);

            fillSupportedFeaturesList(supportedFeatures, module, revision, namespace);

            fillSupportedDeviationsList(affectedModuleDeviations, module, moduleQName);
        }
        Set<String> missingDeviationsList = new HashSet<>();
        fillModuleToAffectedDeviations(modules, supportedDeviations, affectedModuleDeviations, missingDeviationsList);
        if (!missingDeviationsList.isEmpty()) {
            String delimitedString = StringUtils.join(missingDeviationsList, ',');
            LOGGER.error("Following Deviation modules {} are not listed as a module in the yang-library module list", delimitedString);
            throw new Exception("Following Deviation modules " + delimitedString
                    + " are not listed as a module in the yang-library module list");
        }
        return new YangLibraryModulePojo(modules, supportedFeatures, supportedDeviations, variant);
    }

    private static void fillModuleToAffectedDeviations(List<QName> modules, Map<QName, List<QName>> supportedDeviations,
                                                       Map<QName, List<Pair<String, String>>> affectedModuleDeviations,
                                                       Set<String> missingDeviationsList) {
        for (Entry<QName, List<Pair<String, String>>> entry : affectedModuleDeviations.entrySet()) {
            List<QName> affectedDeviations = new ArrayList<>();
            for (Pair<String, String> pair : entry.getValue()) {
                boolean found = false;
                for (QName module : modules) {
                    if (module.getLocalName().equals(pair.getFirst())) {
                        Optional<Revision> revision = module.getRevision();
                        if ((!revision.isPresent() && pair.getSecond().trim().isEmpty())
                                || (revision.get().toString().equals(pair.getSecond()))) {
                            affectedDeviations.add(module);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    String missingDeviationModule = pair.getFirst() + "@" + pair.getSecond();
                    missingDeviationsList.add(missingDeviationModule);
                }
            }
            supportedDeviations.put(entry.getKey(), affectedDeviations);
        }
    }

    private static void fillSupportedDeviationsList(Map<QName, List<Pair<String, String>>> affectedModuleDeviations, Module module,
                                                    QName moduleQName) {
        List<Deviation> deviationList = module.getDeviationList();
        List<Pair<String, String>> deviationPairList = new ArrayList<>();
        for (Deviation deviation : deviationList) {
            String deviationModuleName = deviation.getName();
            String deviationRevision = deviation.getRevision();
            String deviationModuleRevision = deviationRevision == null ? "" : deviationRevision;
            Pair<String, String> deviationPair = new Pair<String, String>(deviationModuleName, deviationModuleRevision);
            deviationPairList.add(deviationPair);
        }
        if (!deviationPairList.isEmpty()) {
            affectedModuleDeviations.put(moduleQName, deviationPairList);
        }
    }

    private static void fillSupportedFeaturesList(List<QName> supportedFeatures, Module module, String revision,
                                                  String namespace) {
        List<String> featuresList = module.getFeatureList();
        for (String featureName : featuresList) {
            supportedFeatures.add(getQName(featureName, revision, namespace));
        }
    }

    private static void fillSubModuleList(List<QName> modules, Module module,
                                          String namespace) {
        List<Submodule> submoduleList = module.getSubmoduleList();
        for (Submodule subModule : submoduleList) {
            String subModuleName = subModule.getName();
            String subModuleRevision = subModule.getRevision();
            String revision = subModuleRevision == null ? "" : subModuleRevision;
            QName subModuleQName = getQName(subModuleName, revision, namespace);
            modules.add(subModuleQName);
        }
    }

    private static QName getQName(String name, String revision, String namespace) {
        QName module;
        if (revision != null && !revision.trim().isEmpty()) {
            module = QName.create(namespace, revision, name);
        } else {
            module = QName.create(namespace, name);
        }
        return module;
    }
}
