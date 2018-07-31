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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DeviceSubsystemResponseUtilTest {
    public static final String IFS_LEAF = "        <no-of-interfaces xmlns=\"urn:test\">5</no-of-interfaces>\n";
    public static final String IFS_IF_TEMPLATE = "        <interface xmlns=\"urn:test\">\n" +
            "            <name>%s</name>\n" +
            "            <admin-state>up_%s</admin-state>\n" +
            "        </interface>\n";
    public static final String IFS_STATE = "    <interfaces-state xmlns=\"urn:test\">\n" +
            IFS_LEAF +
            String.format(IFS_IF_TEMPLATE, "if1", "if1")+
            String.format(IFS_IF_TEMPLATE, "if2", "if2") +
            String.format(IFS_IF_TEMPLATE, "if3", "if3") +
            "    </interfaces-state>\n";
    public static final String IFS_STATE_FN = "        <stateContainer xmlns=\"urn:test\">\n" +
            "            <some-state>some-state</some-state>\n" +
            "        </stateContainer>\n";
    public static final String IF_LEAF_TEMPLATE = "            <oper-state xmlns=\"urn:test\">%s</oper-state>\n";
    public static final String IF_FN_TEMPLATE = "            <interfaceStateContainer xmlns=\"urn:test\">\n" +
            "                <some-state>somevalue_%s</some-state>\n" +
            "            </interfaceStateContainer>\n";
    public static final String RCL_LEAF_TEMPLATE = "        <rootConfigListState " +
            "xmlns=\"urn:test\">rootConfigListStateValue_%s</rootConfigListState>\n";
    public static final String RCL_FN_TEMPLATE = "        <stateContainer xmlns=\"urn:test\">\n" +
            "            <stateContainerLeaf>stateContainerLeafValue_%s</stateContainerLeaf>\n" +
            "        </stateContainer>\n";
    public static final String DATA = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            IFS_STATE +
            "    <interfaces xmlns=\"urn:test\">\n" +
            IFS_STATE_FN +
            "        <interface>\n" +
            "            <name>if1</name>\n" +
            String.format(IF_LEAF_TEMPLATE, "if1") +
            String.format(IF_FN_TEMPLATE, "if1") +
            "        </interface>\n" +
            "        <interface>\n" +
            "            <name>if2</name>\n" +
            String.format(IF_LEAF_TEMPLATE, "if2") +
            String.format(IF_FN_TEMPLATE, "if2") +
            "        </interface>\n" +
            "        <interface>\n" +
            "            <name>if3</name>\n" +
            String.format(IF_LEAF_TEMPLATE, "if3") +
            String.format(IF_FN_TEMPLATE, "if3") +
            "        </interface>\n" +
            "    </interfaces>\n" +
            "    <rootConfigList1 xmlns=\"urn:test\">\n" +
            "        <rootConfigList1Key>key1</rootConfigList1Key>\n" +
            String.format(RCL_LEAF_TEMPLATE, "key1")+
            String.format(RCL_FN_TEMPLATE, "key1")+
            "    </rootConfigList1 >\n" +
            "    <rootConfigList1 xmlns=\"urn:test\">\n" +
            "        <rootConfigList1Key>key2</rootConfigList1Key>\n" +
            String.format(RCL_LEAF_TEMPLATE, "key2")+
            String.format(RCL_FN_TEMPLATE, "key2")+
            "    </rootConfigList1>\n" +
            "</data>";
    private static final String NS = "urn:test";
    public static final String RCL_ID_TEMPLATE = "/container=rootConfigList1/rootConfigList1Key=%s";
    public static final String IF_ID_TEMPLATE = "/container=interfaces/container=interface/name=%s";
    DeviceSubsystemResponseUtil m_util ;
    private SchemaRegistry m_schemaRegistry;
    public static final ModelNodeId IFS_ID = new ModelNodeId("/container=interfaces-state", NS);
    public static final FilterNode IFS_SELECT_NODE = new FilterNode("interface", NS);

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getByteSources(Arrays.asList("/devicesubsystemresponseutiltest/test.yang")),
                new NoLockService());
        m_util = new DeviceSubsystemResponseUtil(m_schemaRegistry);
    }

    @Test
    public void testStateResponseIsPrepared() throws Exception {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = prepareAttributes();

        Element dataElement = DocumentUtils.stringToDocument(DATA).getDocumentElement();
        Map<ModelNodeId, List<Element>> stateResponse = m_util.getStateResponse(attributes, dataElement);
        assertResponse(stateResponse);
    }

    private void assertResponse(Map<ModelNodeId, List<Element>> stateResponse) throws Exception {
        assertIfsAttrs(stateResponse);
        assertIfAttrs("if1", stateResponse);
        assertIfAttrs("if2", stateResponse);
        assertIfAttrs("if3", stateResponse);
        assertRclAttrs("key1", stateResponse);
        assertRclAttrs("key2", stateResponse);
    }

    private void assertRclAttrs(String key, Map<ModelNodeId, List<Element>> stateResponse) throws NetconfMessageBuilderException,
            IOException, SAXException {
        List<Element> ifResponse =  stateResponse.get(new ModelNodeId(String.format(RCL_ID_TEMPLATE, key), NS));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(RCL_LEAF_TEMPLATE, key)).getDocumentElement(), ifResponse.get(0));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(RCL_FN_TEMPLATE, key)).getDocumentElement(), ifResponse.get(1));
    }

    private void assertIfAttrs(String name, Map<ModelNodeId, List<Element>> stateResponse) throws NetconfMessageBuilderException,
            IOException, SAXException {
        List<Element> ifResponse =  stateResponse.get(new ModelNodeId(String.format(IF_ID_TEMPLATE, name), NS));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(IF_LEAF_TEMPLATE, name)).getDocumentElement(), ifResponse.get(0));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(IF_FN_TEMPLATE, name, name)).getDocumentElement(), ifResponse.get(1));
    }

    private void assertIfsAttrs(Map<ModelNodeId, List<Element>> stateResponse) throws NetconfMessageBuilderException, IOException, SAXException {
        List<Element> ifResponse =  stateResponse.get(IFS_ID);
        assertXMLEquals(DocumentUtils.stringToDocument(IFS_LEAF).getDocumentElement(), ifResponse.get(0));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(IFS_IF_TEMPLATE, "if1", "if1")).getDocumentElement(), ifResponse.get(1));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(IFS_IF_TEMPLATE, "if2", "if2")).getDocumentElement(), ifResponse.get(2));
        assertXMLEquals(DocumentUtils.stringToDocument(String.format(IFS_IF_TEMPLATE, "if3", "if3")).getDocumentElement(), ifResponse.get(3));
    }

    @NotNull
    private HashMap<ModelNodeId, Pair<List<QName>, List<FilterNode>>> prepareAttributes() {
        HashMap<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributesToBeFetched = new HashMap<>();

        prepareIfsAttrs(attributesToBeFetched);

        prepareIfAttrs("if1", attributesToBeFetched);
        prepareIfAttrs("if2", attributesToBeFetched);
        prepareIfAttrs("if3", attributesToBeFetched);

        prepareRclAttrs("key1", attributesToBeFetched);
        prepareRclAttrs("key2", attributesToBeFetched);
        return attributesToBeFetched;
    }

    private void prepareIfsAttrs(HashMap<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributesToBeFetched) {
        attributesToBeFetched.put(IFS_ID, new Pair<>(Arrays.asList(QName.create("(urn:test?revision=2016-02-10)no-of-interfaces")),
                Arrays.asList(IFS_SELECT_NODE)));
    }

    private void prepareRclAttrs(String key, HashMap<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributesToBeFetched) {
        ModelNodeId iId = new ModelNodeId(String.format(RCL_ID_TEMPLATE, key), NS);
        FilterNode iSelectNode = new FilterNode("stateContainer", NS);
        attributesToBeFetched.put(iId, new Pair<>(Arrays.asList(QName.create("(urn:test?revision=2016-02-10)rootConfigListState")),
                Arrays.asList(iSelectNode)));
    }

    private void prepareIfAttrs(String ifName, HashMap<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributesToBeFetched) {
        ModelNodeId iId = new ModelNodeId(String.format(IF_ID_TEMPLATE,ifName), NS);
        FilterNode iSelectNode = new FilterNode("interfaceStateContainer", NS);
        attributesToBeFetched.put(iId, new Pair<>(Arrays.asList(QName.create("(urn:test?revision=2016-02-10)oper-state")),
                Arrays.asList(iSelectNode)));
    }

}
