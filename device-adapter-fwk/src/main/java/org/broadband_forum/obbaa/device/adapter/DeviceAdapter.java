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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.bbf.obbaa.schemas.adapter.x10.Adapter;
import org.bbf.obbaa.schemas.adapter.x10.AdapterDocument;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceAdapter implements Serializable {

    public static final String DEVICE_ADAPTERS = "device-adapters";
    public static final String DEVICE_ADAPTER = "device-adapter";
    public static final String DEVICE_ADAPTER_COUNT = "device-adapter-count";

    private String m_type;
    private String m_interfaceVersion;
    private String m_model;
    private String m_vendor;
    private List<String> m_capabilities;
    private Map<URL, InputStream> m_moduleStream = new HashMap<URL, InputStream>();
    private InputStream m_deviceXml;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceAdapter.class);

    public DeviceAdapter(String type, String interfaceVersion, String model, String vendor,
                         List<String> capabilities,
                         InputStream deviceXml, Map<URL, InputStream> moduleStream) {
        m_type = type;
        m_interfaceVersion = interfaceVersion;
        m_model = model;
        m_vendor = vendor;
        m_capabilities = capabilities;
        m_moduleStream = moduleStream;
        m_deviceXml = deviceXml;
    }

    public void init() {
        processDeviceXmlFile(m_deviceXml);
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getInterfaceVersion() {
        return m_interfaceVersion;
    }

    public void setInterfaceVersion(String interfaceVersion) {
        m_interfaceVersion = interfaceVersion;
    }

    public String getModel() {
        return m_model;
    }

    public void setModel(String model) {
        m_model = model;
    }

    public String getVendor() {
        return m_vendor;
    }

    public void setVendor(String vendor) {
        m_vendor = vendor;
    }

    public List<String> getCapabilities() {
        return m_capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        m_capabilities = capabilities;
    }

    public Map<URL, InputStream> getModuleStream() {
        return m_moduleStream;
    }

    public void setModuleStream(Map<URL, InputStream> moduleStream) {
        m_moduleStream = moduleStream;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final DeviceAdapter that = (DeviceAdapter) other;

        if (!m_type.equals(that.m_type)) {
            return false;
        }
        if (!m_interfaceVersion.equals(that.m_interfaceVersion)) {
            return false;
        }
        if (!m_model.equals(that.m_model)) {
            return false;
        }
        if (!m_vendor.equals(that.m_vendor)) {
            return false;
        }
        return m_capabilities.equals(that.m_capabilities);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = m_type != null ? m_type.hashCode() : 0;
        result = prime * result + (m_interfaceVersion != null ? m_interfaceVersion.hashCode() : 0);
        result = prime * result + (m_model != null ? m_model.hashCode() : 0);
        result = prime * result + (m_vendor != null ? m_vendor.hashCode() : 0);
        result = prime * result + (m_capabilities != null ? m_capabilities.hashCode() : 0);
        result = prime * result + (m_moduleStream != null ? m_moduleStream.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeviceAdapter{"
                + "m_type='" + m_type
                + ", m_interfaceVersion='" + m_interfaceVersion
                + ", m_model='" + m_model
                + ", m_vendor='" + m_vendor
                + ", m_capabilities=" + m_capabilities
                + '}';
    }

    private void processDeviceXmlFile(InputStream deviceXml) {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();

        if (deviceXml == null) {
            throw new IllegalArgumentException("Couldn't find resource " + AdapterSpecificConstants.ADAPTER_XML_PATH);
        }

        try {
            AdapterDocument adapterDocument = AdapterDocument.Factory.parse(deviceXml, options);
            checkValidity(adapterDocument);
            Adapter adapter = adapterDocument.getAdapter();
            setType(adapter.getType());
            setInterfaceVersion(adapter.getInterfaceVersion());
            setModel(adapter.getModel());
            setVendor(adapter.getVendor());
            setCapabilities(adapter.getCapabilities().getValueList());
        } catch (Exception e) {
            throw new RuntimeException("Error loading adaper", e);
        }
    }

    private void checkValidity(AdapterDocument adapterDocument) {
        XmlOptions options = new XmlOptions();
        List errorList = new ArrayList();
        options.setErrorListener(errorList);
        if (!adapterDocument.validate(options)) {
            String message = "Error loading device.xml: " + errorList;
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    public void addCapability(String capability) {
        m_capabilities.add(capability);
    }

    public void setModuleStreams(Map<URL, InputStream> moduleStream) {
        this.m_moduleStream = moduleStream;
    }

    public Map<URL, InputStream> getModuleStreams() {
        return m_moduleStream;
    }

    public List<YangTextSchemaSource> getModuleByteSources() throws IOException {
        List<YangTextSchemaSource> byteSources = new ArrayList<>();
        for (URL url : m_moduleStream.keySet()) {
            byteSources.add(YangParserUtil.getYangSource(url, m_moduleStream.get(url)));
        }
        return byteSources;
    }
}

