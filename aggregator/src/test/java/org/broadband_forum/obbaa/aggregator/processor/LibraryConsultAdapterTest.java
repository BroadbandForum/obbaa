package org.broadband_forum.obbaa.aggregator.processor;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.broadband_forum.obbaa.libconsult.LibConsultMgr;
import org.broadband_forum.obbaa.libconsult.impl.LibConsultMgrImpl;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LibraryConsultAdapterTest {

    private static String DEVICE_NAME_A = "deviceA";

    private static final String REQUEST_ALL_USED_YANG_MODULES =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1542612595090\">\n"
                    + "  <get>\n"
                    + "    <filter type=\"subtree\">\n"
                    + "      <in-use-library-modules xmlns=\"urn:bbf:yang:obbaa:module-library-check\" />\n"
                    + "    </filter>\n"
                    + "  </get>\n"
                    + "</rpc>";

    private static final String REQUEST_SPECIFIC_USED_YANG_MODULE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1543477791934\">\n"
                    + "  <get>\n"
                    + "    <filter type=\"subtree\">\n"
                    + "      <in-use-library-modules xmlns=\"urn:bbf:yang:obbaa:module-library-check\">\n"
                    + "        <module>\n"
                    + "           <name>bbf-fiber</name>\n"
                    + "           <revision>2016-09-08</revision>\n"
                    + "        </module>\n"
                    + "      </in-use-library-modules>\n"
                    + "    </filter>\n"
                    + "  </get>\n"
                    + "</rpc>";


    private static final String REQUEST_USED_YANG_MODULE_PER_DEV =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907169\">\n"
                    + "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
                    + "  <filter type=\"subtree\">\n"
                    + "    <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
                    + "      <managed-devices>\n"
                    + "        <device>\n"
                    + "          <name>deviceA</name>\n"
                    + "          <root>\n"
                    + "            <device-library-modules xmlns=\"urn:bbf:yang:obbaa:module-library-check\" />\n"
                    + "          </root>\n"
                    + "        </device>\n"
                    + "      </managed-devices>\n"
                    + "    </network-manager>\n"
                    + "  </filter>\n"
                    + "</get>\n"
                    + "</rpc>";

    private static final String INVALID_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1542612595090\">\n"
                    + "  <get>\n"
                    + "    <filter type=\"subtree\">\n"
                    + "      <invalid-tag xmlns=\"urn:bbf:yang:obbaa:module-library-check\" />\n"
                    + "    </filter>\n"
                    + "  </get>\n"
                    + "</rpc>";

    private static final String GET_NOTHING =
            "<rpc-reply message-id=\"1542612595090\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
            +"  <data/>\n"
            +"</rpc-reply>";

    private static LibraryConsultAdapter m_libraryConsultAdapter;

    @Mock
    private static LibConsultMgr m_libConsultMgr;

    @Mock
    private static Aggregator m_aggregator;

    private static NetconfClientInfo m_clientInfo;
    @BeforeClass
    public static void setUp(){
        m_libConsultMgr = mock(LibConsultMgrImpl.class);
        m_aggregator = mock(AggregatorImpl.class);
        m_libraryConsultAdapter = new LibraryConsultAdapter(m_aggregator, m_libConsultMgr);
        m_clientInfo = new NetconfClientInfo("UT", 1);

        m_libraryConsultAdapter.init();
    }

    @AfterClass
    public static void destroy() {
        m_libraryConsultAdapter.destroy();
    }

    @Test
    public void processWithInvalidRequest() throws Exception {

        Document emptyResponse = DocumentUtils.stringToDocument(GET_NOTHING);
        String result = m_libraryConsultAdapter.processRequest(m_clientInfo, INVALID_REQUEST);
        Document docResult = DocumentUtils.stringToDocument(result);
        TestUtil.assertXMLEquals(emptyResponse.getDocumentElement(), docResult.getDocumentElement());
    }

    @Test
    public void processRequestGetAllUsedYangModules() throws Exception {

        m_libraryConsultAdapter.processRequest(m_clientInfo, REQUEST_ALL_USED_YANG_MODULES);
        verify(m_libConsultMgr).getAllUsedYangModules(any());
    }

    @Test
    public void processRequestGetSpecificYangModules() throws Exception {
        m_libraryConsultAdapter.processRequest(m_clientInfo, REQUEST_SPECIFIC_USED_YANG_MODULE);
        verify(m_libConsultMgr).getSpecificUsedYangModules(anyString(), anyString(), any());
    }

    @Test
    public void processRequestGetYangModulesUsedPerDev() throws Exception {
        m_libraryConsultAdapter.processRequest(DEVICE_NAME_A, REQUEST_USED_YANG_MODULE_PER_DEV);
        verify(m_libConsultMgr).getUsedYangModulesPerDevice(any(), anyString());
    }

}