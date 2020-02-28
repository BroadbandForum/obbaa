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

package org.broadband_forum.obbaa.ipfix.collector.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.ipfix.collector.service.IEMappingCacheService;
import org.broadband_forum.obbaa.ipfix.collector.service.InformationElementService;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixConstants;
import org.broadband_forum.obbaa.ipfix.ncclient.api.NetConfClientException;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IEMappingCacheServiceImpl implements IEMappingCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IEMappingCacheServiceImpl.class);
    private static final String NETWORK_MANAGER_NAMESPACE = "urn:bbf:yang:obbaa:network-manager";
    private static final String NETWORK_MANAGER = "baa-network-manager:network-manager";
    private static final String DEVICE_ADAPTERS = "device-adapters";
    private static final String DEVICE_ADAPTER = "device-adapter";
    private static final String DEVICE_TYPE = "type";
    private static final String DEVICE_MODEL = "model";
    private static final String DEVICE_VENDOR = "vendor";
    private static final String DEVICE_INTERFACE_VERSION = "interface-version";
    private static final String DEFAULT_MESSAGEID = "1";

    private InformationElementService m_informationElementService;
    private DeviceCacheServiceImpl m_deviceFamilyCacheService;

    public IEMappingCacheServiceImpl(InformationElementService informationElementService, DeviceCacheServiceImpl deviceFamilyCacheService) {
        m_informationElementService = informationElementService;
        m_informationElementService.setIeMappingCacheService(this);
        m_deviceFamilyCacheService = deviceFamilyCacheService;
    }

    @Override
    public void syncIEMappingCache(String family) {
        Set<String> families = new HashSet<>();
        families.add(family);
        if (!m_informationElementService.isIECacheAvailable()) {
            families = retrieveAllFamilies();
        }
        families.forEach(this::updateIEMapping);
    }

    private void updateIEMapping(String deviceFamily) {
        LOGGER.debug(String.format("Updating IE mapping for deviceFamily: %s ", deviceFamily));
        String filePath = downloadFileFromFileServer(deviceFamily);
        buildIEIdCache(deviceFamily, filePath);
    }

    private void removeIEMapping(String deviceFamily) {
        LOGGER.debug(String.format("Removing IE mapping for deviceFamily: {}", deviceFamily));
        if (!m_informationElementService.removeInformationElementMapping(deviceFamily)) {
            return;
        }
        LOGGER.debug(String.format("The IEId mapping of device adapter %s is removed", deviceFamily));
        removeCsvFile(deviceFamily);
    }

    private String downloadFileFromFileServer(String deviceFamily) {
        String ieMappingCsv = IpfixConstants.IEID_CSV_FILE;
        String ieMappingPath = IpfixConstants.FE_IE_MAPPING_DIR + IpfixConstants.SLASH + deviceFamily + IpfixConstants.SLASH;
        return ieMappingPath + ieMappingCsv;
    }

    private void buildIEIdCache(String deviceFamily, String csvFilePath) {
        File csvFile = new File(csvFilePath);
        m_informationElementService.loadInformationElementMapping(deviceFamily, csvFile);
    }

    private void removeCsvFile(String deviceFamily) {
        String ieMappingPath = IpfixConstants.FE_IE_MAPPING_DIR + IpfixConstants.SLASH + deviceFamily + IpfixConstants.SLASH;
        try {
            FileUtils.deleteDirectory(new File(ieMappingPath));
        } catch (IOException e) {
            LOGGER.error(String.format("Error while removing csv file for device family: %s", deviceFamily), e);
        }
    }

    private Set<String> retrieveAllFamilies() {
        try {
            GetRequest request = createDevicePlugsRequest();
            NetConfResponse response = m_deviceFamilyCacheService.getNetConfResponse(request);
            return extractFamiliesFromNetconfResponse(response);
        } catch (ParserConfigurationException | ExecutionException | NetConfClientException e) {
            LOGGER.error(String.format("Error while retrieving all device families"), e);
        }
        return Collections.emptySet();
    }

    private Set<String> extractFamiliesFromNetconfResponse(NetConfResponse response) {
        if (Objects.isNull(response)) {
            return Collections.emptySet();
        }
        LOGGER.debug("Extracting device families from the netconf response: " + response.responseToString());
        Element deviceAdaptersElement = DocumentUtils.getChildElement(response.getData(), DEVICE_ADAPTERS);
        List<Element> deviceAdapters = DocumentUtils.getDirectChildElements(deviceAdaptersElement, DEVICE_ADAPTER,
                NETWORK_MANAGER_NAMESPACE);
        if (deviceAdapters.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> deviceFamilies = new HashSet<>();
        for (Element deviceAdapter : deviceAdapters) {
            String deviceFamily;
            String type = getNodeValue(deviceAdapter, DEVICE_TYPE, NETWORK_MANAGER_NAMESPACE);
            String interfaceVersion = getNodeValue(deviceAdapter, DEVICE_INTERFACE_VERSION, NETWORK_MANAGER_NAMESPACE);
            String model = getNodeValue(deviceAdapter, DEVICE_MODEL, NETWORK_MANAGER_NAMESPACE);
            String vendor = getNodeValue(deviceAdapter, DEVICE_VENDOR, NETWORK_MANAGER_NAMESPACE);
            if (model != null && vendor != null && type != null && interfaceVersion != null) {
                deviceFamily =  vendor + "-" + type + "-" + model + "-" + interfaceVersion;
                deviceFamilies.add(deviceFamily);
            }
        }
        LOGGER.debug(String.format("Extracted families: %s from the netconf response: %s", deviceFamilies, response.responseToString()));
        return deviceFamilies;
    }

    private String getNodeValue(Element parent, String name, String namespace) {
        Element node =  DocumentUtils.getDirectChildElement(parent, name, namespace);
        if (node != null) {
            return node.getTextContent();
        }
        return null;
    }

    private GetRequest createDevicePlugsRequest() throws ParserConfigurationException {
        Document document = DocumentUtils.getNewDocument();
        Element networkManager = document.createElementNS(NETWORK_MANAGER_NAMESPACE, NETWORK_MANAGER);
        document.appendChild(networkManager);
        final Element type = document.createElement(DEVICE_TYPE);
        final Element model = document.createElement(DEVICE_MODEL);
        final Element vendor = document.createElement(DEVICE_VENDOR);
        final Element interfaceVersion = document.createElement(DEVICE_INTERFACE_VERSION);
        Element deviceAdapter = document.createElementNS(NETWORK_MANAGER_NAMESPACE, DEVICE_ADAPTER);
        deviceAdapter.appendChild(type);
        deviceAdapter.appendChild(model);
        deviceAdapter.appendChild(vendor);
        deviceAdapter.appendChild(interfaceVersion);
        Element deviceAdapters = document.createElementNS(NETWORK_MANAGER_NAMESPACE, DEVICE_ADAPTERS);
        deviceAdapters.appendChild(deviceAdapter);
        networkManager.appendChild(deviceAdapters);
        NetconfFilter requestFilter = new NetconfFilter();
        requestFilter.setType(NetconfResources.SUBTREE_FILTER);
        requestFilter.addXmlFilter(document.getDocumentElement());
        GetRequest getRequest = new GetRequest();
        getRequest.setMessageId(DEFAULT_MESSAGEID);
        getRequest.setFilter(requestFilter);
        getRequest.setReplyTimeout(10000);
        return getRequest;
    }
}
