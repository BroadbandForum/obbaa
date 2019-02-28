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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.YangLibraryModulePojo;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public final class VariantFileUtils {

    private VariantFileUtils() {
    }

    private static final String NEW_LINE = "\n";

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantFileUtils.class);


    public static void createAdapterOutputDirs(List<String> variants, String outputDir) {
        final File yangDir = new File(outputDir, AdapterSpecificConstants.YANG);
        final File finalDir = new File(outputDir, AdapterSpecificConstants.FINAL_FOLDER);
        final File variantsDir = new File(outputDir, AdapterSpecificConstants.VARIANTS);
        final File modelDir = new File(outputDir, AdapterSpecificConstants.MODEL);
        yangDir.mkdirs();
        finalDir.mkdirs();
        variantsDir.mkdirs();
        modelDir.mkdirs();
    }

    public static String getYangLibPath(String variant) {
        StringBuilder builder = new StringBuilder(AdapterSpecificConstants.VARIANTS);
        builder.append(File.separator);
        builder.append(variant);
        builder.append(File.separator);
        builder.append(AdapterSpecificConstants.YANG_LIB_FILE);
        return builder.toString();
    }

    /**
     * Fetch the supported features and deviations from yang-library.xml file and create supported-deviations.txt,
     * supported-features.txt files
     *
     * @param yangLibPath - location of yang-library.xml file
     * @param targetPath  - location to be copied
     * @throws IOException - Exception if any
     */
    public static void copyYangLibFiles(String yangLibPath, String targetPath) throws Exception {
        YangLibraryModulePojo yangModulePojo = YangLibraryUtils.getYangModulePojo(yangLibPath);
        copyYangLibSupportingFiles(targetPath, yangModulePojo);

    }

    public static void validateAndCopyYangLibFiles(String yangLibPath, String targetAdapterModelDir, String srcYangDir,
                                                   String filteredYangDir, boolean isPyangValidationEnabled) throws Exception {
        YangLibraryModulePojo yangModulePojo = YangLibraryUtils.getYangModulePojo(yangLibPath);

        String srcYangPathTemp = srcYangDir + "temp";
        FileUtils.copyDirectoryToDirectory(new File(srcYangDir), new File(srcYangPathTemp));
        List<QName> moduleQNames = yangModulePojo.getModules();
        File filteredYangFiles = new File(filteredYangDir);
        try {
            doYangLibValidation(moduleQNames, new File(srcYangPathTemp), filteredYangFiles);
            if (isPyangValidationEnabled) {
                doPyangValidation(filteredYangDir);
            }
        } finally {
            //delete once the yang validation is done
            if (filteredYangFiles.exists()) {
                FileUtils.deleteDirectory(filteredYangFiles);
            }
        }
        copyYangLibSupportingFiles(targetAdapterModelDir, yangModulePojo);
    }

    private static void copyYangLibSupportingFiles(String targetAdapterModelDir, YangLibraryModulePojo yangModulePojo)
            throws IOException {
        File adapterModelTargetDir = new File(targetAdapterModelDir);
        copySupportingFeaturesFiles(adapterModelTargetDir, yangModulePojo.getSupportedFeatures(),
                AdapterSpecificConstants.SUPPORTED_FEATURES_FILE);
        copySupportingDeviationsFiles(adapterModelTargetDir, yangModulePojo.getSupportedDeviations(),
                AdapterSpecificConstants.SUPPORTED_DEVIATIONS_FILE);
    }

    private static void doPyangValidation(String filteredYangsPath) throws Exception {
        String pyangRunnableOption = "-E WBAD_MODULE_NAME -E WBAD_REVISION -E FILENAME_BAD_MODULE_NAME -E FILENAME_BAD_REVISION -W none";
        doPyangValidation(filteredYangsPath, pyangRunnableOption);
    }

    private static void doPyangValidation(String filteredYangsPath, String pyangRunnableOption) throws Exception {
        String pyangcmd = String.format("pyang %s -p %s %s/*", pyangRunnableOption, filteredYangsPath, filteredYangsPath);
        List<String> errorStreamList = PyangValidatorUtil.runPyang(pyangcmd);
        if (errorStreamList != null && !errorStreamList.isEmpty()) {
            String delimtedErrorString = StringUtils.join(errorStreamList, '\n');
            LOGGER.error("Error during Pyang validation, {}", delimtedErrorString);
            throw new Exception("Error during Pyang validation, " + delimtedErrorString);
        }
    }

    private static void doYangLibValidation(List<QName> moduleQNames, File sourceYangDir, File targetFilteredYangDir) throws Exception {
        try {
            String[] extensions = new String[]{"yang"};
            Collection<File> srcYangFiles = FileUtils.listFiles(sourceYangDir, extensions, true);
            Set<String> srcYangFileNames = new HashSet<String>();
            Map<String, File> moduleToFiles = new HashMap<>();
            Set<String> missingModuleFiles = new HashSet<>();
            for (File srcYangFile : srcYangFiles) {
                srcYangFileNames.add(srcYangFile.getName());
                moduleToFiles.put(srcYangFile.getName(), srcYangFile);
            }
            for (QName moduleQname : moduleQNames) {
                String fileNameWithoutRevision = moduleQname.getLocalName() + AdapterSpecificConstants.YANG_EXTN;
                String fileNameWithRevision = null;
                if (moduleQname.getRevision().isPresent()) {
                    String date = moduleQname.getRevision().get().toString();
                    fileNameWithRevision = moduleQname.getLocalName() + "@" + date + AdapterSpecificConstants.YANG_EXTN;
                }
                if (srcYangFileNames.contains(fileNameWithoutRevision)) {
                    File srcFile = moduleToFiles.get(fileNameWithoutRevision);
                    // copy with fileName with yang-library revision so pyang will detect the revision mismatch later
                    File destFile = new File(targetFilteredYangDir,
                            fileNameWithRevision == null ? fileNameWithoutRevision : fileNameWithRevision);
                    File destFileWithoutRevision = new File(targetFilteredYangDir, fileNameWithoutRevision);
                    if (!destFileWithoutRevision.exists() && !destFile.exists()) {
                        moveFiles(srcFile, destFile);
                    }
                } else if (fileNameWithRevision != null && srcYangFileNames.contains(fileNameWithRevision)) {
                    File srcFile = moduleToFiles.get(fileNameWithRevision);
                    File destFile = new File(targetFilteredYangDir, fileNameWithRevision);
                    File destFileWithoutRevision = new File(targetFilteredYangDir, fileNameWithoutRevision);
                    if (!destFileWithoutRevision.exists() && !destFile.exists()) {
                        moveFiles(srcFile, destFile);
                    }
                } else {
                    missingModuleFiles.add(fileNameWithoutRevision);
                }
            }
            if (!missingModuleFiles.isEmpty()) {
                String delimitedString = StringUtils.join(missingModuleFiles, ',');
                LOGGER.error("Modules {} mentioned in the yang-library.xml file are missing in the yang directory", delimitedString);
                throw new Exception("Modules " + delimitedString
                        + " mentioned in the yang-library.xml file are missing in the yang directory");
            }
            Collection<File> remainingYangFiles = FileUtils.listFiles(sourceYangDir, extensions, true);
            if (!remainingYangFiles.isEmpty()) {
                String delimitedString = StringUtils.join(remainingYangFiles, ',');
                LOGGER.warn("There are still some yang files {} present in the yang directory but missing in the yang-library.xml file",
                        delimitedString);
            }
        } finally {
            //delete the temp src directory
            if (sourceYangDir.exists()) {
                FileUtils.deleteDirectory(sourceYangDir);
            }
        }

    }

    private static void moveFiles(File srcFile, File destFile) throws Exception {
        try {
            FileUtils.moveFile(srcFile, destFile);
        } catch (IOException e) {
            throw new Exception("Error while moving yang file " + srcFile.getAbsolutePath() + " to destination directory ", e);
        }
    }

    private static void copySupportingFeaturesFiles(File variantModelTargetDir, List<QName> moduleNames,
                                                    String supportingFileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (QName module : moduleNames) {
            builder.append(module.toString());
            builder.append(NEW_LINE);
        }

        File supportingFile = new File(variantModelTargetDir, supportingFileName);
        supportingFile.createNewFile();
        writeToFile(builder, supportingFile);
    }

    private static void writeToFile(StringBuilder builder, File supportingFile) throws IOException {
        if (builder.length() > 0) {
            String data = builder.toString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(supportingFile))) {
                writer.write(data);
            }
        }
    }

    private static void copySupportingDeviationsFiles(File variantModelTargetDir, Map<QName, List<QName>> deviations,
                                                      String supportingFileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Entry<QName, List<QName>> entry : deviations.entrySet()) {
            builder.append(entry.getKey());
            builder.append(AdapterSpecificConstants.COMMA);
            List<QName> affectedDeviationsList = entry.getValue();
            builder.append(Joiner.on(AdapterSpecificConstants.COMMA).join(affectedDeviationsList));
            builder.append(NEW_LINE);

        }
        File supportingFile = new File(variantModelTargetDir, supportingFileName);
        supportingFile.createNewFile();
        writeToFile(builder, supportingFile);
    }

    public static File getResource(String resourceName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(resourceName).getFile());
    }
}
