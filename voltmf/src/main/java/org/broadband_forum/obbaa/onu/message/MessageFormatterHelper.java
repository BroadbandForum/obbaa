/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceConfigBackup;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterSyncException;
import org.broadband_forum.obbaa.onu.util.DeviceJsonUtils;
import org.broadband_forum.obbaa.onu.util.JsonUtil;
import org.json.JSONObject;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

/**
 * <p>
 * Utility class to help MessageFormatter
 * </p>
 * Created by Filipe Cláudio (Altice Labs) on 19/05/2021.
 */
final class MessageFormatterHelper {

    private static final Logger LOGGER = Logger.getLogger(MessageFormatterHelper.class);

    private MessageFormatterHelper() {}

    static String getJsonForDeltaConfig(EditConfigRequest request, Device onuDevice, AdapterManager adapterManager,
                                        ModelNodeDataStoreManager modelNodeDsm) {
        EditConfigElement configElement = request.getConfigElement();
        List<String> configElementsJsonList = new ArrayList<>();
        if (configElement != null) {
            SchemaRegistry deviceSchemaRegistry = AdapterUtils.getAdapterContext(onuDevice, adapterManager).getSchemaRegistry();
            DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(deviceSchemaRegistry, modelNodeDsm);
            for (Element element : configElement.getConfigElementContents()) {
                DataSchemaNode deviceRootSchemaNode =
                        deviceJsonUtils.getDeviceRootSchemaNode(element.getLocalName(), element.getNamespaceURI());
                String configElementJsonString = JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(Arrays.asList(element),
                        deviceRootSchemaNode, deviceSchemaRegistry, modelNodeDsm);
                configElementsJsonList.add(configElementJsonString.substring(1, configElementJsonString.length() - 1));
                LOGGER.debug("JSON converted string for the edit-config request of the filter element is " + configElementJsonString);
            }
        }
        return "{" + String.join(",", configElementsJsonList) + "}";
    }

    static String getFilterElementsJsonListForGetRequest(GetRequest request, SchemaRegistry schemaRegistry, Device onuDevice,
                                                         AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm) {
        SchemaRegistry deviceSchemaRegistry;
        String deviceLeaf;
        if (request.getMessageId().equals(ONUConstants.DEFAULT_MESSAGE_ID)) {
            deviceLeaf = ONUConstants.DEVICE_MANAGEMENT;
            deviceSchemaRegistry = schemaRegistry;
        } else {
            deviceLeaf = ONUConstants.ROOT;
            deviceSchemaRegistry = AdapterUtils.getAdapterContext(onuDevice, adapterManager).getSchemaRegistry();
        }
        DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(deviceSchemaRegistry, modelNodeDsm);

        List<String> filterElementsJsonList = new ArrayList<>();
        for (Element element : request.getFilter().getXmlFilterElements()) {
            DataSchemaNode deviceRootSchemaNode = deviceJsonUtils.getDeviceRootSchemaNode(element.getLocalName(),
                    element.getNamespaceURI());
            String filterElementJsonString = JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(Arrays.asList(element),
                    deviceRootSchemaNode, deviceSchemaRegistry, modelNodeDsm);
            if (request.getMessageId().equals(ONUConstants.DEFAULT_MESSAGE_ID)) {
                //retrieve only DeviceState attributes from filterElementJsonString
                JSONObject jsonObject = new JSONObject(filterElementJsonString);
                filterElementJsonString = jsonObject.getJSONObject(ONUConstants.OBBAA_NETWORK_MANAGER)
                        .getJSONObject(ONUConstants.MANAGED_DEVICES).getJSONArray(ONUConstants.DEVICE).getJSONObject(0)
                        .getJSONObject(ONUConstants.DEVICE_MANAGEMENT).toString();
            }
            filterElementsJsonList.add(filterElementJsonString.substring(1, filterElementJsonString.length() - 1));
            LOGGER.info("JSON converted string for the GET request of the filter element is " + filterElementJsonString);
        }
        return "{\"" + ONUConstants.NETWORK_MANAGER + ONUConstants.COLON + deviceLeaf + "\""
               + ONUConstants.COLON + "{" + String.join(",", filterElementsJsonList) + "}}";
    }

    static void validateBackupDatastore(String onuDeviceName, String messageId, DeviceConfigBackup backupDatastore)
            throws MessageFormatterException {
        if (backupDatastore == null) {
            LOGGER.error(String.format("No backup datastore found for edit-config with message-id %s found for device %s",
                    messageId, onuDeviceName));
            throw new MessageFormatterException("Error while processing edit-config. No backup datastore found for message-id "
                                                + messageId);
        }
    }

    static void validateConfig(String currentConfig, String postConfig, String onuDeviceName) throws MessageFormatterException {
        if (isInvalidConfig(currentConfig) || isInvalidConfig(postConfig)) {
            LOGGER.error(String.format("Unable to fetch pre/post backup configuration for %s request for device %s",
                    ONUConstants.ONU_EDIT_OPERATION, onuDeviceName));
            throw new MessageFormatterException("Unable to fetch pre/post backup configuration for "
                                                + ONUConstants.ONU_EDIT_OPERATION + " request for device " + onuDeviceName);
        } else if (preConfigEqualsPostConfig(currentConfig, postConfig)) {
            LOGGER.info(String.format("Config already is in sync (same config in cache) - skipping %s request for device %s",
                    ONUConstants.ONU_EDIT_OPERATION, onuDeviceName));
            throw new MessageFormatterSyncException(ONUConstants.EDIT_CONFIG_SYNCED);
        }
    }

    private static boolean isInvalidConfig(String config) {
        return config == null || config.isEmpty();
    }

    private static boolean preConfigEqualsPostConfig(String preConfig, String postConfig) {
        return preConfig.equals(postConfig);
    }
}
