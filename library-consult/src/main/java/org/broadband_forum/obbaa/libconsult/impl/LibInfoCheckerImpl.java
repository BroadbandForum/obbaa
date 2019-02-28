/*
 *   Copyright 2018 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.broadband_forum.obbaa.libconsult.impl;

import static org.broadband_forum.obbaa.libconsult.utils.LibraryConsultUtils.getDevReferredAdapterId;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.libconsult.BaseLibConsult;
import org.broadband_forum.obbaa.libconsult.LibInfoChecker;
import org.broadband_forum.obbaa.libconsult.utils.LibraryConsultUtils;
import org.broadband_forum.obbaa.libconsult.utils.YangReferenceInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LibInfoCheckerImpl extends BaseLibConsult implements LibInfoChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibInfoCheckerImpl.class);
    private PmaRegistry m_pmaRegistry;

    //key: moduleName+revision
    private Map<String, YangReferenceInfo> m_nsMap2UsedYangModule = new ConcurrentHashMap<>();

    public LibInfoCheckerImpl(AdapterManager adapterManager, DeviceManager deviceManager, PmaRegistry pmaRegistry) {
        super(adapterManager, deviceManager);
        this.m_pmaRegistry = pmaRegistry;
    }

    @Override
    public Element getSpecificUsedYangModules(String moduleName, String revisionDate, Document responseDoc) {

        YangReferenceInfo filterModule = new YangReferenceInfo(moduleName, revisionDate);
        return getUsedYangModules(responseDoc, filterModule);
    }

    @Override
    public Element getAllUsedYangModules(Document responseDoc) {

        return getUsedYangModules(responseDoc, null);
    }

    private Element getUsedYangModules(Document responseDoc, YangReferenceInfo filterModule) {

        m_nsMap2UsedYangModule.clear();
        executeWithAllDevices(device -> {
            NetConfResponse response = getAllConfigOfEachDevice(device.getDeviceName());
            collectAllCfgCorrespondingYangModules(response, device, filterModule);
        });

        return buildAllUsedYangModulesWithExtraEl(responseDoc, (parent, yangRefInfo) -> {
            Element associateInfoElement = buildSingleYangAssociateInfo(responseDoc, yangRefInfo);
            parent.appendChild(associateInfoElement);
        });
    }

    @Override
    public Element getUsedYangModulesPerDevice(Document responseDoc, String devName) {
        m_nsMap2UsedYangModule.clear();
        NetConfResponse response = getAllConfigOfEachDevice(devName);

        Device device = getDeviceManager().getDevice(devName);
        collectAllCfgCorrespondingYangModules(response, device, null);
        DeviceAdapterId deviceAdapterId = getDevReferredAdapterId(device);

        Element devUsedYangModulesEl = responseDoc
                .createElementNS(LibraryConsultUtils.NAMESPACE, LibraryConsultUtils.DEVICE_USED_YANG_MODULES);
        Element relateAdapterEl = responseDoc.createElementNS(LibraryConsultUtils.NAMESPACE, LibraryConsultUtils.RELATED_ADAPTER);
        buildAdapterIdElement(responseDoc, deviceAdapterId, relateAdapterEl);
        devUsedYangModulesEl.appendChild(relateAdapterEl);
        devUsedYangModulesEl.appendChild(buildAllUsedYangModulesWithExtraEl(responseDoc, null));

        return devUsedYangModulesEl;
    }

    private Element buildAllUsedYangModulesWithExtraEl(Document responseDoc,
            BiConsumer<Element, YangReferenceInfo> fun) {
        Element usedYangModulesEl = responseDoc
                .createElementNS(LibraryConsultUtils.NAMESPACE, LibraryConsultUtils.USED_YANG_MODULES);
        m_nsMap2UsedYangModule.forEach((key, yangRefInfo) -> {
            Element moduleEl = responseDoc.createElementNS(LibraryConsultUtils.NAMESPACE, LibraryConsultUtils.MODULE);
            appendElement(responseDoc, moduleEl, DeviceManagerNSConstants.NAME, yangRefInfo.getModuleName());
            appendElement(responseDoc, moduleEl, LibraryConsultUtils.REVISION_TAG, yangRefInfo.getRevisionDate());
            if (fun != null) {
                fun.accept(moduleEl, yangRefInfo);
            }
            usedYangModulesEl.appendChild(moduleEl);

        });
        return usedYangModulesEl;
    }

    private Element buildSingleYangAssociateInfo(Document responseDoc, YangReferenceInfo yangReferenceInfo) {
        return buildAllAdaptersInUseEl(responseDoc, yangReferenceInfo.getReferredDevices(),
                LibraryConsultUtils.ASSOCIATE_ADAPTERS);
    }

    private NetConfResponse getAllConfigOfEachDevice(String deviceKey) {
        try {
            return m_pmaRegistry.getAllPersistCfg(deviceKey);
        }
        catch (ExecutionException e) {
            LOGGER.info("error when get all config for device:{}, error:{}", deviceKey, e.getMessage());
        }
        return null;
    }

    private void collectAllCfgCorrespondingYangModules(NetConfResponse configurationRes, Device device,
            YangReferenceInfo filterModule) {

        if ((configurationRes == null) || (configurationRes.getData() == null)) {
            return;
        }

        NodeList nodeList = configurationRes.getData().getElementsByTagName("*");
        if (nodeList == null) {
            return;
        }

        for (int index = 0; index < nodeList.getLength(); index++) {
            Node node = nodeList.item(index);
            if (node.getNamespaceURI() != null) {
                collectYangReferenceByNs(device, node.getNamespaceURI(), filterModule);
            }

            NamedNodeMap namedNodeMap = node.getAttributes();
            if (namedNodeMap == null) {
                continue;
            }
            for (int attrIndex = 0; attrIndex < namedNodeMap.getLength(); attrIndex++) {
                if (namedNodeMap.item(attrIndex).getNodeValue() != null) {
                    collectYangReferenceByNs(device, namedNodeMap.item(attrIndex).getNodeValue(),
                            filterModule);
                }
            }
        }
    }

    private AdapterContext getAdapterContext(Device device) {
        DeviceAdapterId deviceAdapterId = getDevReferredAdapterId(device);
        return getAdapterManager().getAdapterContext(deviceAdapterId);
    }

    private Module getYangModuleFromRelatedAdapter(Device device, String nameSpace) {
        AdapterContext adapterContext = getAdapterContext(device);
        return adapterContext.getSchemaRegistry().getModuleByNamespace(nameSpace);
    }

    private void collectYangReferenceByNs(Device device, String nameSpace, YangReferenceInfo filterModule) {
        Module module = getYangModuleFromRelatedAdapter(device, nameSpace);
        YangReferenceInfo yangTmpInfo = new YangReferenceInfo(module.getName(), module.getRevision().get());

        if ((filterModule != null) && !(filterModule.equals(yangTmpInfo))) {
            return;
        }
        //different adapter may use the same YangModule with different revision
        YangReferenceInfo yangReferenceInfo = m_nsMap2UsedYangModule
                .computeIfAbsent(yangTmpInfo.getModuleName() + yangTmpInfo.getRevisionDate(), s -> yangTmpInfo);

        yangReferenceInfo.updateReferredInfo(device);
    }

    private Element buildAllAdaptersInUseEl(Document response, Map<DeviceAdapterId, Set<Device>> adapter2DevicesMap,
            String parentName) {
        Element usedAdaptersEl = response.createElementNS(LibraryConsultUtils.NAMESPACE, parentName);
        adapter2DevicesMap.forEach((key, value) -> {
            Element adapterInUseEl = buildSingleAdapterInUse(response, key, value);
            usedAdaptersEl.appendChild(adapterInUseEl);
        });

        return usedAdaptersEl;
    }

    private Element buildSingleAdapterInUse(Document response, DeviceAdapterId deviceAdapterId, Set<Device> deviceSet) {

        Element adapterInUseEl = response.createElementNS(LibraryConsultUtils.NAMESPACE, DeviceManagerNSConstants.DEVICE_ADAPTER);
        buildAdapterIdElement(response, deviceAdapterId, adapterInUseEl);

        appendElement(response, adapterInUseEl, LibraryConsultUtils.RELATED_DEVICE_NUM,
                String.valueOf(deviceSet.size()));
        adapterInUseEl.appendChild(buildDeviceList(response, deviceSet));

        return adapterInUseEl;
    }

    private void buildAdapterIdElement(Document response, DeviceAdapterId deviceAdapterId, Element parent) {
        appendElement(response, parent, DeviceManagerNSConstants.VENDOR, deviceAdapterId.getVendor());
        appendElement(response, parent, DeviceManagerNSConstants.MODEL, deviceAdapterId.getModel());
        appendElement(response, parent, DeviceManagerNSConstants.TYPE, deviceAdapterId.getType());
        appendElement(response, parent, DeviceManagerNSConstants.INTERFACE_VERSION,
                deviceAdapterId.getInterfaceVersion());
    }

    private Element buildDeviceList(Document response, Set<Device> deviceSet) {
        Element relDevicesEl = response.createElementNS(LibraryConsultUtils.NAMESPACE, LibraryConsultUtils.RELATED_DEVICES);
        deviceSet.forEach(
            device -> appendElement(response, relDevicesEl, LibraryConsultUtils.DEVICE, device.getDeviceName()));
        return relDevicesEl;
    }

    private Element appendElement(Document document, Element parentElement, String localName, String... textContents) {
        Element lastElement = null;
        for (String textContent : textContents) {
            Element element = document.createElementNS(LibraryConsultUtils.NAMESPACE, localName);
            element.setTextContent(textContent);
            parentElement.appendChild(element);
            lastElement = element;
        }
        return lastElement;
    }
}
