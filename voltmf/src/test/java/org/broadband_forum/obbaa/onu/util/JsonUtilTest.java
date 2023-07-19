/*
 * Copyright 2023 Broadband Forum
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

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(SchemaPath.class)
public class JsonUtilTest {

    @Mock
    private SchemaRegistry m_schemaRegistry;

    @Mock
    private LeafSchemaNode m_node;

    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;

    @Mock
    private Int8TypeDefinition m_Int8TypeDefinition;
    @Mock
    private Uint8TypeDefinition m_Uint8TypeDefinition;
    @Mock
    private Int16TypeDefinition m_Int16TypeDefinition;
    @Mock
    private Uint16TypeDefinition m_Uint16TypeDefinition;
    @Mock
    private Int32TypeDefinition m_Int32TypeDefinition;
    @Mock
    private Uint32TypeDefinition m_Uint32TypeDefinition;
    @Mock
    private Int64TypeDefinition m_Int64TypeDefinition;
    @Mock
    private Uint64TypeDefinition m_Uint64TypeDefinition;

    @Mock
    private IdentityrefTypeDefinition m_IdentityrefTypeDefinition;
    @Mock
    private BooleanTypeDefinition m_BooleanTypeDefinition;
    @Mock
    private UnionTypeDefinition m_UnionTypeDefinition;
    @Mock
    private StringTypeDefinition m_StringTypeDefinition;
    @Mock
    private InstanceIdentifierTypeDefinition m_InstanceIdentifierTypeDefinition;
    @Mock
    private EmptyTypeDefinition m_EmptyTypeDefinition;

    @Mock
    private SchemaPath m_schemaPath;

    @Mock
    private Module m_schemaModule;

    @Before
    public void setUp() {
        m_schemaRegistry = mock(SchemaRegistry.class);
        m_node = mock(LeafSchemaNode.class);
        m_modelNodeDSM = mock(ModelNodeDataStoreManager.class);

        m_Int8TypeDefinition = mock(Int8TypeDefinition.class);
        m_Uint8TypeDefinition = mock(Uint8TypeDefinition.class);
        m_Int16TypeDefinition  = mock(Int16TypeDefinition.class);
        m_Uint16TypeDefinition = mock(Uint16TypeDefinition.class);
        m_Int32TypeDefinition  = mock(Int32TypeDefinition.class);
        m_Uint32TypeDefinition = mock(Uint32TypeDefinition.class);
        m_Int64TypeDefinition  = mock(Int64TypeDefinition.class);
        m_Uint64TypeDefinition = mock(Uint64TypeDefinition.class);

        m_IdentityrefTypeDefinition = mock(IdentityrefTypeDefinition.class);
        m_BooleanTypeDefinition = mock(BooleanTypeDefinition.class);
        m_UnionTypeDefinition = mock(UnionTypeDefinition.class);
        m_StringTypeDefinition = mock(StringTypeDefinition.class);
        m_InstanceIdentifierTypeDefinition = mock(InstanceIdentifierTypeDefinition.class);
        m_EmptyTypeDefinition = mock(EmptyTypeDefinition.class);
        //mockito doesn't mock final methods
        PowerMockito.mockStatic(SchemaPath.class);
        m_schemaModule = mock(Module.class);
    }

    private String testConvertJsonSimpleTypes(String input, String moduleName, TypeDefinition<? extends TypeDefinition<?>> type)
            throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument(input);
        when(m_schemaRegistry.getModuleNameByNamespace(any())).thenReturn(moduleName);
        Mockito.doReturn(type).when(m_node).getType();
        ArrayList<Element> list = new ArrayList<Element>();
        list.add(doc.getDocumentElement());
        String jsonData = JsonUtil.convertFromXmlToJson(list, m_node, m_schemaRegistry, m_modelNodeDSM);
        return jsonData;
    }

    @Test
    public void testConvertJsonInt8() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>-3</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Int8TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":-3}"));
    }

    @Test
    public void testConvertJsonUint8() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>250</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Int8TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":250}"));
    }

    @Test
    public void testConvertJsonInt16() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>-32767</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Int16TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":-32767}"));
    }

    @Test
    public void testConvertJsonUInt16() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>65535</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Uint16TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":65535}"));
    }

    @Test
    public void testConvertJsonInt32() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>-2147483647</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Int32TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":-2147483647}"));
    }

    @Test
    public void testConvertJsonUInt32() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>4294967295</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Uint32TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":4294967295}"));
    }

    @Test
    public void testConvertJsonInt64() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>-922337203685477580</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Int64TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":-922337203685477580}"));
    }

    @Test
    public void testConvertJsonUInt64() throws NetconfMessageBuilderException {
        String xmlStr = new String("<a xmlns='test:namespace'>922337203685477580</a>");
        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_Uint64TypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":922337203685477580}"));
    }

    @Test
    public void testConvertIdentityrefTypeDefinition() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument(
                "<a xmlns='test:namespace' xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">" +
                        "ianaift:ethernetCsmacd" +
                     "</a>"
        );
        QName xpathQname = QName.create(
                "urn:ietf:params:xml:ns:yang:ietf-yang-types",
                "xpath1.0");

        when(m_schemaRegistry.getModuleNameByNamespace("test:namespace")).thenReturn("test");
        when(m_schemaRegistry.getModuleNameByNamespace("urn:ietf:params:xml:ns:yang:iana-if-type")).thenReturn("iana-if-type");
        Mockito.doReturn(m_IdentityrefTypeDefinition).when(m_node).getType();
        when(m_StringTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);
        ArrayList<Element> list = new ArrayList<Element>();
        list.add(doc.getDocumentElement());

        String jsonData = JsonUtil.convertFromXmlToJson(list, m_node, m_schemaRegistry, m_modelNodeDSM);
        assertTrue(jsonData.equals("{\"test:a\":\"iana-if-type:ethernetCsmacd\"}"));
    }

    @Test
    public void testConvertBoolean() throws NetconfMessageBuilderException {
        String xmlStr1 = new String("<a xmlns='test:namespace'>true</a>");
        String xmlStr2 = new String("<a xmlns='test:namespace'>false</a>");
        String jsonData1 = testConvertJsonSimpleTypes(xmlStr1, "test", m_BooleanTypeDefinition);
        String jsonData2 = testConvertJsonSimpleTypes(xmlStr2, "test", m_BooleanTypeDefinition);
        assertTrue(jsonData1.equals("{\"test:a\":true}"));
        assertTrue(jsonData2.equals("{\"test:a\":false}"));
    }

    @Test
    public void testConvertUnionType() throws NetconfMessageBuilderException {
        String xmlStr1 = new String("<a xmlns='test:namespace'>true</a>");
        String xmlStr2 = new String("<a xmlns='test:namespace'>10</a>");
        String xmlStr3 = new String("<a xmlns='test:namespace'>abcde</a>");

        QName xpathQname = QName.create(
                "urn:ietf:params:xml:ns:yang:ietf-yang-types",
                "xpath1.0");


        ArrayList<TypeDefinition<?>> typeList = new ArrayList<TypeDefinition<?>>();
        typeList.add(m_BooleanTypeDefinition);
        typeList.add(m_Int32TypeDefinition);
        typeList.add(m_StringTypeDefinition);
        Mockito.doReturn(typeList).when(m_UnionTypeDefinition).getTypes();

        when(m_StringTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);

        String jsonData1 = testConvertJsonSimpleTypes(xmlStr1, "test", m_UnionTypeDefinition);
        String jsonData2 = testConvertJsonSimpleTypes(xmlStr2, "test", m_UnionTypeDefinition);
        String jsonData3 = testConvertJsonSimpleTypes(xmlStr3, "test", m_UnionTypeDefinition);

        assertTrue(jsonData1.equals("{\"test:a\":true}"));
        assertTrue(jsonData2.equals("{\"test:a\":10}"));
        assertTrue(jsonData3.equals("{\"test:a\":\"abcde\"}"));
    }

    @Test
    public void testConvertXpathAbsolute() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument(
                "<a xmlns='test:namespace' xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">" +
                        "/if:interfaces-state/if:interface[if:name='eth0']/if:statistics/if:in-octets" +
                     "</a>"
        );
        QName xpathQname = QName.create(
                "urn:ietf:params:xml:ns:yang:ietf-yang-types",
                "xpath1.0");

        when(m_schemaRegistry.getModuleNameByNamespace(any())).thenReturn("test");
        Mockito.doReturn(m_StringTypeDefinition).when(m_node).getType();
        when(m_StringTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);
        ArrayList<Element> list = new ArrayList<Element>();
        list.add(doc.getDocumentElement());

        String jsonData = JsonUtil.convertFromXmlToJson(list, m_node, m_schemaRegistry, m_modelNodeDSM);
        assertTrue(jsonData.equals(
                "{\"test:a\":\"/ietf-interfaces:interfaces-state/interface[name='eth0']/statistics/in-octets\"}"));
    }

    @Test
    public void testConvertXpathRelative() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument(
                "<a xmlns='test:namespace' xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">"+
                        "if:interfaces-state" +
                     "</a>"
        );
        QName xpathQname = QName.create(
                "urn:ietf:params:xml:ns:yang:ietf-yang-types",
                "xpath1.0");

        when(m_schemaRegistry.getModuleNameByNamespace(any())).thenReturn("test");
        Mockito.doReturn(m_StringTypeDefinition).when(m_node).getType();
        when(m_StringTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);
        ArrayList<Element> list = new ArrayList<Element>();
        list.add(doc.getDocumentElement());

        String jsonData = JsonUtil.convertFromXmlToJson(list, m_node, m_schemaRegistry, m_modelNodeDSM);
        assertTrue(jsonData.equals(
                "{\"test:a\":\"ietf-interfaces:interfaces-state\"}"));
    }

    @Test
    public void testConvertString() throws NetconfMessageBuilderException {
        String xmlStr1 = new String("<a xmlns='test:namespace'>::</a>");
        String xmlStr2 = new String("<a xmlns='test:namespace'>a:b[a:b='dummy']</a>");
        QName xpathQname = QName.create(
                "dummy:ns",
                "dummy");

        when(m_StringTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);

        String jsonData1 = testConvertJsonSimpleTypes(xmlStr1, "test", m_StringTypeDefinition);
        String jsonData2 = testConvertJsonSimpleTypes(xmlStr2, "test", m_StringTypeDefinition);
        assertTrue(jsonData1.equals("{\"test:a\":\"::\"}"));
        assertTrue(jsonData2.equals("{\"test:a\":\"a:b[a:b='dummy']\"}"));
    }


    @Test
    public void testConvertInstanceIdentifier() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument(
                "<a xmlns='test:namespace' xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">" +
                        "/if:interfaces-state/if:interface[if:name='eth0']/if:statistics/if:in-octets" +
                     "</a>"
        );
        QName xpathQname = QName.create(
                "dummy:ns",
                "dummy");

        when(m_schemaRegistry.getModuleNameByNamespace(any())).thenReturn("test");
        Mockito.doReturn(m_InstanceIdentifierTypeDefinition).when(m_node).getType();
        when(m_StringTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);
        ArrayList<Element> list = new ArrayList<Element>();
        list.add(doc.getDocumentElement());

        String jsonData = JsonUtil.convertFromXmlToJson(list, m_node, m_schemaRegistry, m_modelNodeDSM);
        assertTrue(jsonData.equals(
                "{\"test:a\":\"/ietf-interfaces:interfaces-state/interface[name='eth0']/statistics/in-octets\"}"));
    }

    @Test
    public void testConvertEmpty() throws NetconfMessageBuilderException {
        String xmlStr = new String(
                "<a xmlns='test:namespace'></a>"
        );
        QName xpathQname = QName.create(
                "dummy:ns",
                "dummy");

        when(m_EmptyTypeDefinition.getPath()).thenReturn(m_schemaPath);
        PowerMockito.when(m_schemaPath.getLastComponent()).thenReturn(xpathQname);

        String jsonData = testConvertJsonSimpleTypes(xmlStr, "test", m_EmptyTypeDefinition);
        assertTrue(jsonData.equals("{\"test:a\":[null]}"));
    }

}
