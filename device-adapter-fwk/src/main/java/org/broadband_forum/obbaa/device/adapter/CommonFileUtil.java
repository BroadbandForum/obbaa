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

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.COMMA;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bbf.obbaa.schemas.adapter.x10.Adapter;
import org.bbf.obbaa.schemas.adapter.x10.AdapterDocument;
import org.opendaylight.yangtools.yang.common.QName;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonFileUtil {

    private CommonFileUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonFileUtil.class);

    private static FileSystem createZipFileSystem(String zipFilename)
            throws Exception {
        final Path path = Paths.get(zipFilename);
        return FileSystems.newFileSystem(path, null);
    }

    public static void copySpecificFilefromZip(String zipFileLocation, String targetFilename, Path toCopyFilePath)
            throws Exception {
        try (FileSystem zipFileSystem = createZipFileSystem(zipFileLocation)) {
            final Path targetFilePath = zipFileSystem.getPath(targetFilename);
            if (Files.notExists(targetFilePath)) {
                throw new Exception(targetFilePath.getFileName().toString() + " file does not exist in the specified archive file");
            }
            Files.copy(targetFilePath, toCopyFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void unpackToSpecificDirectory(String zipFilename, String destDirname)
            throws Exception {

        final Path destDir = Paths.get(destDirname);
        //if the destination doesn't exist, create it
        if (Files.notExists(destDir)) {
            LOGGER.info(destDir + " does not exist. Creating...");
            Files.createDirectories(destDir);
        }

        try (FileSystem zipFileSystem = createZipFileSystem(zipFilename)) {
            final Path root = zipFileSystem.getPath(File.separator);

            //walk the zip file tree and copy files to the destination
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException {
                    final Path destFile = Paths.get(destDir.toString(),
                            file.toString());
                    LOGGER.info("Extracting file " + file + "  to " + destFile + "\n");
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) throws IOException {
                    final Path dirToCreate = Paths.get(destDir.toString(),
                            dir.toString());
                    if (Files.notExists(dirToCreate)) {
                        LOGGER.info("Creating directory : \n" + dirToCreate);
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error occured while unpacking ", e);
            throw e;
        }
    }

    public static DeviceAdapterId parseAdapterXMLFile(String zipFilename, String deviceXMLPath) throws Exception {
        Path tempFile = null;
        try {
            // create a temp xml file to copy the device.xml file from ZIP
            tempFile = Files.createTempFile("temp", Paths.get(deviceXMLPath).getFileName().toString());
            Path zipFilePath = Paths.get(zipFilename);
            // throw an error if specified zip file is not exists
            if (Files.notExists(zipFilePath)) {
                throw new Exception(AdapterSpecificConstants.ADAPTER_DEFINITION_ARCHIVE_NOT_FOUND_ERROR);
            }
            copySpecificFilefromZip(zipFilename, deviceXMLPath, tempFile);
            AdapterDocument deviceDocument = AdapterDocument.Factory.parse(tempFile.toFile());
            Adapter adapter = deviceDocument.getAdapter();
            if (adapter == null || adapter.getType() == null || adapter.getInterfaceVersion() == null
                    || adapter.getModel() == null || adapter.getVendor() == null) {
                throw new Exception(AdapterSpecificConstants.UNABLE_TO_GET_ADAPTER_DETAILS_TYPE);
            }
            return new DeviceAdapterId(adapter.getType(), adapter.getInterfaceVersion(), adapter.getModel(), adapter.getVendor());
        } finally {
            // delete temp file if it exists
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    public static DeviceAdapterId parseAdapterXMLFile(String adapterXmlPath, Bundle bundle) throws Exception {
        Path tempFile = null;
        try {
            URL sourceAdapterXml = bundle.getResource(adapterXmlPath);
            // create a temp xml file to copy the device.xml file from ZIP
            tempFile = Files.createTempFile("temp", Paths.get(sourceAdapterXml.getFile()).getFileName().toString());
            Files.copy(sourceAdapterXml.openStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            AdapterDocument deviceDocument = AdapterDocument.Factory.parse(tempFile.toFile());
            Adapter adapter = deviceDocument.getAdapter();
            if (adapter == null || adapter.getType() == null || adapter.getInterfaceVersion() == null
                    || adapter.getModel() == null || adapter.getVendor() == null) {
                throw new Exception(AdapterSpecificConstants.UNABLE_TO_GET_ADAPTER_DETAILS_TYPE);
            }
            return new DeviceAdapterId(adapter.getType(), adapter.getInterfaceVersion(), adapter.getModel(), adapter.getVendor());
        } finally {
            // delete temp file if it exists
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    public static Map<URL, InputStream> buildModuleStreamMap(String destDirname, List<String> modulePaths) throws IOException {
        String yangPath = destDirname + AdapterSpecificConstants.YANG_PATH;
        Map<URL, InputStream> yangFilePath = new HashMap<URL, InputStream>();
        String[] extensions = new String[] {"yang"};
        Iterator<File> iterator = FileUtils.iterateFiles(new File(yangPath), extensions, true);

        while (iterator.hasNext()) {
            File yangFile = iterator.next();
            URL url = yangFile.toURI().toURL();
            yangFilePath.put(url, url.openStream());
            modulePaths.add(url.getPath());
        }
        Collections.sort(modulePaths);
        return yangFilePath;
    }

    public static Set<QName> getAdapterFeaturesFromFile(InputStream inp) {
        Set<QName> supportedFeatures = new HashSet<>();
        List<String> qnameStrings;
        try {
            qnameStrings = IOUtils.readLines(inp, Charset.defaultCharset());
            for (String qnameStr : qnameStrings) {
                supportedFeatures.add(QName.create(qnameStr));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading features from supported-features.txt", e);
        }
        return supportedFeatures;
    }

    public static Map<QName, Set<QName>> getAdapterDeviationsFromFile(InputStream deviationsFile) {
        Map<QName, Set<QName>> supportedDeviations = new HashMap<>();
        List<String> qnameStrings;
        int iter;
        try {
            qnameStrings = IOUtils.readLines(deviationsFile, Charset.defaultCharset());
            for (String qnameStr : qnameStrings) {
                Set<QName> deviationQNames = new HashSet<>();
                String[] moduleAndDeviations = qnameStr.split(COMMA);
                /*
                 * In each line : eg.
                 * (urn:broadband-forum-org:yang:bbf-xdsl?revision=2016-01-25)bbf-xdsl,(urn:xxxxx-org:yang:nokia-xdsl-dev?revision=2017-07-
                 * 05)nokia-xdsl-dev 1. First string will always be module name. 2. From second string it will be the deviations for the
                 * module
                 */
                QName moduleQName = QName.create(moduleAndDeviations[0]);
                if (moduleQName != null) {
                    for (iter = 1; iter < moduleAndDeviations.length; iter++) {
                        QName deviationQName = QName.create(moduleAndDeviations[iter]);
                        deviationQNames.add(deviationQName);
                    }
                    supportedDeviations.put(moduleQName, deviationQNames);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading deviations from supported-deviations.txt", e);
        }
        return supportedDeviations;
    }

}
