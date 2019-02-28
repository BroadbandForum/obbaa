package org.broadband_forum.obbaa.device.adapter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * util class to manage some key-value properties.
 */
public final class SystemProperty {
    private static final String CGF_FILE_FOLDER = File.separator + "baa" + File.separator + "stores" + File.separator;
    private static final String CGF_FILE = "system.properties";
    private static final String CGF_FILE_PATH = CGF_FILE_FOLDER + File.separator + CGF_FILE;

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemProperty.class);

    private Properties m_properties = new Properties();

    private static SystemProperty INSTANCE = new SystemProperty();

    private SystemProperty() {
        init();
    }

    public static SystemProperty getInstance() {
        return INSTANCE;
    }

    private void init() {
        try {
            createDirectory();
            createPropFile();
            readFromPath(CGF_FILE_PATH);
        }
        catch (IOException e) {
            LOGGER.info("error happened when system property init {}", e.getMessage());
        }
    }

    private void persistSysProperty() {
        try {
            try (FileOutputStream fileOutputStream = new FileOutputStream(CGF_FILE_PATH, false)) {
                m_properties.store(fileOutputStream, null);
            }
        }
        catch (IOException e) {
            LOGGER.info("exception happened when persist {}", e.getMessage());
        }
    }

    private void createDirectory() throws IOException {
        final Path propertyDirectory = Paths.get(CGF_FILE_FOLDER);
        if (!propertyDirectory.toFile().exists()) {
            Files.createDirectories(propertyDirectory);
        }
    }

    private void createPropFile() throws IOException {
        File propFile = new File(CGF_FILE_PATH);
        if (!propFile.exists()) {
            Boolean result = propFile.createNewFile();
            LOGGER.info("create file:{} result {}", CGF_FILE, result);
        }
    }

    private void readFromPath(String path) throws IOException {
        readFromFile(new File(path));
    }

    private void readFromFile(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            readFrom(stream);
        }
    }

    private void readFrom(InputStream stream) throws IOException {
        m_properties.load(stream);
    }

    public void set(String key, String value) {
        m_properties.setProperty(key, value);
        persistSysProperty();
    }

    public String get(String key) {
        return (String) m_properties.get(key);
    }

    public void remove(String key) {
        m_properties.remove(key);
        persistSysProperty();
    }
}