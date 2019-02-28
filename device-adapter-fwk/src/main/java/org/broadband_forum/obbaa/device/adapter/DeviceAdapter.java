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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlOptions;
import org.bbf.obbaa.schemas.adapter.x10.Adapter;
import org.bbf.obbaa.schemas.adapter.x10.AdapterDocument;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceAdapter implements Serializable {

    private Map<QName, Set<QName>> m_supportedDeviations = new HashMap<>();
    private DeviceAdapterId m_deviceAdapterId;
    private List<String> m_capabilities;
    private Map<URL, InputStream> m_moduleStream = new HashMap<URL, InputStream>();
    private InputStream m_deviceXml;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceAdapter.class);
    private Set<QName> m_supportedFeatures = Collections.emptySet();
    private String m_lastUpdateTime;
    private String m_developer;
    private List<Date> m_revisions = new ArrayList<>();
    private Boolean m_isNetconf = true;

    private static final String ADAPT_LAST_UPDATE_TIME_KEY_POSTFIX = ":last-update-time";

    protected DeviceAdapter(DeviceAdapterId deviceAdapterId, List<String> capabilities,
                            InputStream deviceXml, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations,
                            Map<URL, InputStream> moduleStream) {
        m_deviceAdapterId = deviceAdapterId;
        m_capabilities = capabilities;
        m_moduleStream = moduleStream;
        m_deviceXml = deviceXml;
        m_supportedFeatures = supportedFeatures;
        m_supportedDeviations = supportedDeviations;
    }

    public void init() {
        processDeviceXmlFile(m_deviceXml);
    }

    public DeviceAdapterId getDeviceAdapterId() {
        return m_deviceAdapterId;
    }

    public void setDeviceAdapterId(DeviceAdapterId deviceAdapterId) {
        m_deviceAdapterId = deviceAdapterId;
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

    public InputStream getDeviceXml() {
        return m_deviceXml;
    }

    public void setDeviceXml(InputStream deviceXml) {
        m_deviceXml = deviceXml;
    }

    public String getType() {
        return m_deviceAdapterId.getType();
    }

    public String getInterfaceVersion() {
        return m_deviceAdapterId.getInterfaceVersion();
    }

    public String getModel() {
        return m_deviceAdapterId.getModel();
    }

    public String getVendor() {
        return m_deviceAdapterId.getVendor();
    }

    public Set<QName> getSupportedFeatures() {
        return m_supportedFeatures;
    }

    public void setSupportedFeatures(Set<QName> supportedFeatures) {
        m_supportedFeatures = supportedFeatures;
    }

    public Map<QName, Set<QName>> getSupportedDevations() {
        return m_supportedDeviations;
    }

    public void setSupportedDevations(Map<QName, Set<QName>> supportedDeviations) {
        this.m_supportedDeviations = supportedDeviations;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceAdapter adapter = (DeviceAdapter) other;

        if (m_deviceAdapterId != null ? !m_deviceAdapterId.equals(adapter.m_deviceAdapterId) : adapter.m_deviceAdapterId != null) {
            return false;
        }
        if (m_capabilities != null ? !m_capabilities.equals(adapter.m_capabilities) : adapter.m_capabilities != null) {
            return false;
        }
        if (m_moduleStream != null ? !m_moduleStream.equals(adapter.m_moduleStream) : adapter.m_moduleStream != null) {
            return false;
        }
        if (m_deviceXml != null ? !m_deviceXml.equals(adapter.m_deviceXml) : adapter.m_deviceXml != null) {
            return false;
        }
        if (m_supportedDeviations != null ? !m_supportedDeviations.equals(adapter.m_supportedDeviations) :
                adapter.m_supportedDeviations != null) {
            return false;
        }
        return m_supportedFeatures != null ? m_supportedFeatures.equals(adapter.m_supportedFeatures) : adapter.m_supportedFeatures == null;

    }

    @Override
    public int hashCode() {
        int result = m_deviceAdapterId != null ? m_deviceAdapterId.hashCode() : 0;
        result = 31 * result + (m_capabilities != null ? m_capabilities.hashCode() : 0);
        result = 31 * result + (m_moduleStream != null ? m_moduleStream.hashCode() : 0);
        result = 31 * result + (m_deviceXml != null ? m_deviceXml.hashCode() : 0);
        result = 31 * result + (m_supportedFeatures != null ? m_supportedFeatures.hashCode() : 0);
        result = 31 * result + (m_supportedDeviations != null ? m_supportedDeviations.hashCode() : 0);
        return result;
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
            m_deviceAdapterId.setType(adapter.getType());
            m_deviceAdapterId.setInterfaceVersion(adapter.getInterfaceVersion());
            m_deviceAdapterId.setModel(adapter.getModel());
            m_deviceAdapterId.setVendor(adapter.getVendor());
            setCapabilities(adapter.getCapabilities().getValueList());
            if (adapter.xgetDeveloper() != null) {
                setDeveloper(adapter.getDeveloper());
            }
            if (adapter.getRevisions() != null) {
                setRevisions(adapter.getRevisions().getRevisionList());
            }
            if (adapter.xgetIsNetconf() != null) {
                setNetconf(adapter.getIsNetconf());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading device-adapter.xml", e);
        }
    }

    private void checkValidity(AdapterDocument adapterDocument) {
        XmlOptions options = new XmlOptions();
        List errorList = new ArrayList();
        options.setErrorListener(errorList);
        if (!adapterDocument.validate(options)) {
            String message = "Error loading device-adapter.xml: " + errorList;
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

    public String getLastUpdateTime() {
        return m_lastUpdateTime;
    }

    public void setLastUpdateTime(DateTime lastUpdateTime) {
        m_lastUpdateTime = AdapterUtils.updateAdapterLastUpdateTime(genAdapterLastUpdateTimeKey(), lastUpdateTime);
    }

    public void setDeveloper(String developer) {
        m_developer = developer;
    }

    public String getDeveloper() {
        return m_developer;
    }

    public void setRevisions(List<Calendar> revisions) {
        for (Calendar revision : revisions) {
            m_revisions.add(revision.getTime());
        }
    }

    public List<Date> getRevisions() {
        return m_revisions;
    }

    public String genAdapterLastUpdateTimeKey() {
        return new StringBuilder(getModel()).append(getInterfaceVersion()).append(getVendor()).append(getType())
                .append(ADAPT_LAST_UPDATE_TIME_KEY_POSTFIX).toString();
    }

    public Boolean getNetconf() {
        return m_isNetconf;
    }

    public void setNetconf(Boolean netconf) {
        m_isNetconf = netconf;
    }
}

