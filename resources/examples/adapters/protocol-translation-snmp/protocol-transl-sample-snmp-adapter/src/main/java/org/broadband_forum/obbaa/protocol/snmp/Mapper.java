/*
 * Copyright 2020 Broadband Forum
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
 *
 * This file contains Mapper class which includes functionalites
 * for mapping Netconf Requests to corresponding SNMP requests.
 *
 * Created by Balaji Venkatachalapathy (DZSI) on 01/10/2020.
 */

package org.broadband_forum.obbaa.protocol.snmp;

import java.io.IOException;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Mapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mapper.class);
    SnmpTransport m_snmpHdl = null;

    public Mapper(SnmpTransport snmpHdl) {
        m_snmpHdl = snmpHdl;
    }

/*
    This function receives EditConfig request and converts to SNMP requests
*/
    public void mapAndSendSnmp(EditConfigRequest request) {
        try {
            NodeList listOfNodes = request.getConfigElement().getXmlElement().getChildNodes();
            for (int i = 0; i < listOfNodes.getLength(); i++) {
                String keystring = listOfNodes.item(i).getLocalName();
                for (Element element : DocumentUtils.getChildElements(listOfNodes.item(i))) {
                    parseAndSend(element, keystring);
                }
            }
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("NetconfMessageBuilderException", e);
        }
    }

/*
    This function recursively generate the keypaths, retrieve OIDs from DataStore,
    form Varbinds and send SNMP request.
    The below parsing logic do not support leaf-lists.
*/
    public void parseAndSend(Element parent, String parentPath) {
        String keypath = parentPath + "/" + parent.getLocalName();
        parentPath = keypath;
        Attr operation = parent.getAttributeNode("xc:operation");
        if (null != operation) {
            if ((operation.getValue().contains("remove"))
                || (operation.getValue().contains("delete"))) {
                keypath = keypath + "/delete";
            }
        }
        List<MetaData> oids = DataStore.getOids(keypath);
        if (!oids.isEmpty()) {
            fillOidValues(oids, parent);
            customizeCfg(oids, parent, keypath);
            SnmpVarBindBuilder buildVar = new SnmpVarBindBuilder();
            try {
                String response = m_snmpHdl.snmpSetGroup(buildVar.buildVarbindArray(oids));
                if (response == null) {
                    // request timed out
                    LOGGER.warn("ProtTranslateSnmp:  request timed out!!");
                } else {
                    LOGGER.debug("ProtTranslateSnmp:  Received response");
                }
            } catch (IOException e) {
                throw new RuntimeException("Error during SnmpTarget test", e);
            }
        } else {
            for (Element element : DocumentUtils.getChildElements(parent)) {
                parseAndSend(element, parentPath);
            }
        }
        return;
    }

/*
    This function fills the OID values in the incoming MetaData List
*/
    public void fillOidValues(List<MetaData> mdata, Element element) {
        for (MetaData iter : mdata) {
            if (iter.m_leaf != null) {
                Element childElem = DocumentUtils.getChildElement(element, iter.m_leaf);
                if (null != childElem) {
                    iter.m_oidValue = childElem.getTextContent();
                } else if (iter.m_oidValue == null) {
                    LOGGER.debug("oid with empty value.Hence removing from the List");
                    mdata.remove(iter);
                }
            }
        }
    }

/*
    This function contains customer specific implementation.
    In this sample implementation, all the hardware component
    names are mapped to the entPhysicalIndex by extracting
    the digits comes after the '_' in the name string.
    So the digits should be of the format [a-zA-Z]*_[0-9]+ eg:fan_1, power_2
    OIDs retrieved are appended with theIndex no. extracted.
*/
    public void customizeCfg(List<MetaData> mdata, Element element, String keypath) {
        LOGGER.debug("In fn customizeCfg");

// Customer specific implementation below:

        if (keypath.equals("hardware/component")) {
            Element childElem = DocumentUtils.getChildElement(element, "name");
            String keyValue = "";
            if (childElem != null) {
                keyValue = childElem.getTextContent();
                keyValue = keyValue.substring(keyValue.lastIndexOf("_") + 1);
                for (MetaData iter : mdata) {
                    iter.m_oid = iter.m_oid + "." + keyValue;
                }
            }
        }
    }
}
