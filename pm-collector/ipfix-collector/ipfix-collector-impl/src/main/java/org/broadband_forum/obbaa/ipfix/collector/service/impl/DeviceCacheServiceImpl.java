package org.broadband_forum.obbaa.ipfix.collector.service.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.ipfix.collector.service.DeviceCacheService;
import org.broadband_forum.obbaa.ipfix.ncclient.api.NetConfClientException;
import org.broadband_forum.obbaa.ipfix.ncclient.app.NcClientServiceImpl;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DeviceCacheServiceImpl implements DeviceCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCacheServiceImpl.class);
    private static final String DEFAULT_MESSAGEID = "1";
    private static final String DEVICE_TAG = "device";
    private static final String DEVICE_NAME_TAG = "name";
    private static final String DEVICE_TYPE = "type";
    private static final String DEVICE_MODEL = "model";
    private static final String DEVICE_VENDOR = "vendor";
    private static final String DEVICE_INTERFACE_VERSION = "interface-version";
    private static final String DEVICE_MANAGEMENT = "baa-network-manager:device-management";
    private static final String NETWORK_MANAGER_NAMESPACE = "urn:bbf:yang:obbaa:network-manager";
    private static final String NETWORK_MANAGER = "baa-network-manager:network-manager";
    private static final String MANAGED_DEVICES = "baa-network-manager:managed-devices";
    NcClientServiceImpl m_ncClientService;

    private ConcurrentHashMap<String, String> m_deviceFamilyCache;

    public DeviceCacheServiceImpl() {
        m_deviceFamilyCache = new ConcurrentHashMap<String, String>();
    }

    @Override
    public void deleteDeviceFamilyCache(String deviceID) {
        m_deviceFamilyCache.remove(deviceID);
    }

    @Override
    public String getDeviceFamily(String deviceName) {
        //only one thread can do fullSyncDeviceFamilyCache
        if (!m_deviceFamilyCache.containsKey(deviceName)) {
            synchronized (this) {
                if (!m_deviceFamilyCache.containsKey(deviceName)) {
                    fullSyncDeviceFamilyCache();
                }
            }
        }
        String deviceFamily = m_deviceFamilyCache.get(deviceName);
        if (deviceFamily == null) {
            // Invalidate in case cannot getting device family by deviceName from altiplano
            // The stored family cache value: {deviceName : null}. Need to invalidate this
            m_deviceFamilyCache.remove(deviceName);
        }
        return deviceFamily;
    }

    private void fullSyncDeviceFamilyCache() {
        try {
            GetRequest request = createGetAllDevicesRequest();
            NetConfResponse response = getNetConfResponse(request);
            updateDeviceFamilyCache(response);
        } catch (Exception e) {
            LOGGER.error("Syncing of device family cache failed due to exception", e);
        }
    }

    private GetRequest createGetAllDevicesRequest() {
        Document document = DocumentUtils.createDocument();
        Element networkManager = document.createElementNS(NETWORK_MANAGER_NAMESPACE, NETWORK_MANAGER);
        Element managedDevices = document.createElement(MANAGED_DEVICES);
        networkManager.appendChild(managedDevices);
        document.appendChild(networkManager);
        NetconfFilter requestFilter = new NetconfFilter();
        requestFilter.setType(NetconfResources.SUBTREE_FILTER);
        requestFilter.addXmlFilter(document.getDocumentElement());
        GetRequest getRequest = new GetRequest();
        getRequest.setMessageId(DEFAULT_MESSAGEID);
        getRequest.setFilter(requestFilter);
        getRequest.setReplyTimeout(10000);
        return getRequest;
    }

    private void updateDeviceFamilyCache(NetConfResponse response) throws NetconfMessageBuilderException {
        if (response != null) {
            Document document = response.getResponseDocument();
            if (document != null) {
                Element rpcReplyElement = document.getDocumentElement();
                Element managedDevices = DocumentUtils.getChildElement(rpcReplyElement, MANAGED_DEVICES);
                List<Element> deviceList =  DocumentUtils.getDirectChildElements(managedDevices, DEVICE_TAG, NETWORK_MANAGER_NAMESPACE);
                for (Element device : deviceList) {
                    String deviceName =  getNodeValue(device, DEVICE_NAME_TAG, NETWORK_MANAGER_NAMESPACE);
                    Element deviceManagement = DocumentUtils.getChildElement(device,DEVICE_MANAGEMENT);
                    String family = extractDeviceFamilyFromElement(deviceManagement);
                    if (deviceName != null && family != null) {
                        m_deviceFamilyCache.put(deviceName, family);
                    }
                }
            }
        }
    }

    private String getNodeValue(Element parent, String name, String namespace) {
        Element node =  DocumentUtils.getDirectChildElement(parent, name, namespace);
        if (node != null) {
            return node.getTextContent();
        }
        return null;
    }

    private String extractDeviceFamilyFromElement(Element element) {
        String type = getNodeValue(element, DEVICE_TYPE, NETWORK_MANAGER_NAMESPACE);
        String interfaceVersion = getNodeValue(element, DEVICE_INTERFACE_VERSION, NETWORK_MANAGER_NAMESPACE);
        String model = getNodeValue(element, DEVICE_MODEL, NETWORK_MANAGER_NAMESPACE);
        String vendor = getNodeValue(element, DEVICE_VENDOR, NETWORK_MANAGER_NAMESPACE);
        if (type != null && interfaceVersion != null) {
            return vendor + "-" + type + "-" + model + "-" + interfaceVersion;
        }
        return null;
    }

    public NetConfResponse getNetConfResponse(AbstractNetconfRequest request) throws ExecutionException, NetConfClientException {
        try {
            if (m_ncClientService == null) {
                m_ncClientService = new NcClientServiceImpl();
            }
            return m_ncClientService.performNcRequest(request);
        } catch (NetConfClientException e) {
            LOGGER.error("Error while creating Netconf session", e);
            throw e;
        } catch (Exception e) {
            if (!(e instanceof ExecutionException)) {
                throw new ExecutionException(e);
            }
        }
        return null;
    }

    protected int getDeviceFamilyCacheSize() {
        return m_deviceFamilyCache.size();
    }
}
