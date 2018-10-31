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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bbf.obbaa.schemas.adapter.x10.Adapter;
import org.bbf.obbaa.schemas.adapter.x10.AdapterDocument;
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

    public static DeviceAdapterId parseAdapterDeviceXMLFile(String zipFilename, String deviceXMLPath) throws Exception {
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

}
