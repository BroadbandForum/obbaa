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

package org.broadband_forum.obbaa.onu.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.w3c.dom.Element;

/**
 * <p>
 * XML utility to format from XML to JSON or from JSON Format to XML
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 13/05/2021.
 */
public final class XmlUtil {
    private static final Logger LOGGER = Logger.getLogger(XmlUtil.class);

    private XmlUtil(){

    }

    public static String convertXmlToJson(Device onuDevice, AdapterManager adapterManager,
                                          ModelNodeDataStoreManager modelNodeDsm, String xml) {
        try {
            Element element = DocumentUtils.stringToDocument(xml).getDocumentElement();
            List<Element> deviceRootNodes = DocumentUtils.getChildElements(element);
            SchemaRegistry deviceSchemaRegistry = AdapterUtils.getAdapterContext(onuDevice, adapterManager).getSchemaRegistry();
            return convertElementsToJson(deviceRootNodes, deviceSchemaRegistry, modelNodeDsm);
        } catch (NetconfMessageBuilderException exception) {
            LOGGER.error("Error in convertXmlToJson ", exception);
            return "";
        }
    }

    public static String convertXmlToJson(SchemaRegistry schemaRegistry,
                                          ModelNodeDataStoreManager modelNodeDsm, String xml,
                                          boolean ignoreRoot) {
        try {
            if (ignoreRoot == true) {
                Element element = DocumentUtils.stringToDocument(xml).getDocumentElement();
                return convertElementToJson(element, schemaRegistry, modelNodeDsm);
            } else {
                Element element = DocumentUtils.stringToDocument(xml).getDocumentElement();
                List<Element> deviceRootNodes = DocumentUtils.getChildElements(element);
                return convertElementsToJson(deviceRootNodes, schemaRegistry, modelNodeDsm);
            }
        } catch (NetconfMessageBuilderException exception) {
            LOGGER.error("Error in convertXmlToJson ", exception);
            return "";
        }
    }

    public static String convertXmlToJson(SchemaRegistry schemaRegistry,
                                          ModelNodeDataStoreManager modelNodeDsm, String xml) {
        return convertXmlToJson(schemaRegistry, modelNodeDsm, xml, true);
    }


    private static String convertElementsToJson(List<Element> deviceRootNodes, SchemaRegistry schemaRegistry,
                                         ModelNodeDataStoreManager modelNodeDsm) {

        DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(schemaRegistry, modelNodeDsm);
        List<String> jsonNodes = new ArrayList<>();
        for (Element deviceRootNode : deviceRootNodes) {
            DataSchemaNode schemaNode = deviceJsonUtils.getDeviceRootSchemaNode(deviceRootNode.getLocalName(),
                    deviceRootNode.getNamespaceURI());
            String jsonNode = JsonUtil.convertFromXmlToJson(Arrays.asList(deviceRootNode), schemaNode,
                    schemaRegistry, modelNodeDsm);
            jsonNodes.add(jsonNode.substring(1, jsonNode.length() - 1));
        }
        return "{" + String.join(",", jsonNodes) + "}";
    }

    private static String convertElementToJson(Element element, SchemaRegistry schemaRegistry,
                                                ModelNodeDataStoreManager modelNodeDsm) {

        DeviceJsonUtils jsonUtils = new DeviceJsonUtils(schemaRegistry, modelNodeDsm);
        List<String> jsonNodes = new ArrayList<>();
        SchemaNode schemaNode;
        if (element.getLocalName().equals(ONUConstants.CREATE_ONU)) {
            schemaNode = jsonUtils.getRpcSchemaNode(element.getLocalName(),
                    element.getNamespaceURI());
        } else {
            schemaNode = jsonUtils.getDeviceRootSchemaNode(element.getLocalName(),
                    element.getNamespaceURI());
        }

        String jsonNode = JsonUtil.convertFromXmlToJson(Arrays.asList(element), schemaNode,
                schemaRegistry, modelNodeDsm);
        jsonNodes.add(jsonNode.substring(1, jsonNode.length() - 1));
        return "{" + String.join(",", jsonNodes) + "}";
    }
}
