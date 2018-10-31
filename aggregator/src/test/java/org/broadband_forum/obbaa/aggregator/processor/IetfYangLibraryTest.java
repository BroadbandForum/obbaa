package org.broadband_forum.obbaa.aggregator.processor;

import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.junit.Before;
import org.junit.Test;

public class IetfYangLibraryTest {
    private Aggregator aggregator;
    private IetfYangLibrary ietfYangLibrary;
    private NetconfClientInfo m_clientInfo;

    @Before
    public void setUp() throws Exception {
        aggregator = new AggregatorImpl();
        ietfYangLibrary = new IetfYangLibrary(aggregator);
        m_clientInfo = new NetconfClientInfo("UT", 1);
        ietfYangLibrary.init();
    }

    @Test
    public void processRequest() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
                "  <get>\n" +
                "    <source>\n" +
                "      <running />\n" +
                "    </source>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </yang-library>\n" +
                "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </modules-state>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>\n";

        String response = ietfYangLibrary.processRequest(m_clientInfo, request);
        assertTrue(response.contains("yang-library"));
    }

    @Test
    public void processRequestModulesState() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
                        "  <get>\n" +
                        "    <source>\n" +
                        "      <running />\n" +
                        "    </source>\n" +
                        "    <filter type=\"subtree\">\n" +
                        "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "     </modules-state>\n" +
                        "    </filter>\n" +
                        "  </get>\n" +
                        "</rpc>\n";

        String response = ietfYangLibrary.processRequest(m_clientInfo, request);
        assertTrue(response.contains("modules-state"));
    }

    @Test
    public void processRequestModulesStateFilter() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
                        "  <get>\n" +
                        "    <source>\n" +
                        "      <running />\n" +
                        "    </source>\n" +
                        "    <filter type=\"subtree\">\n" +
                        "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "     <module-set-id>all</module-set-id>\n" +
                        "     </modules-state>\n" +
                        "    </filter>\n" +
                        "  </get>\n" +
                        "</rpc>\n";

        String response = ietfYangLibrary.processRequest(m_clientInfo, request);
        assertTrue(response.contains("modules-state"));
    }

    @Test
    public void processRequestFilter() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
                        "  <get>\n" +
                        "    <source>\n" +
                        "      <running />\n" +
                        "    </source>\n" +
                        "    <filter type=\"subtree\">\n" +
                        "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "            <modules>\n" +
                        "                <module>\n" +
                        "                    <id>urn:ietf:params:xml:ns:yang:ietf-yang-library?module=ietf-yang-library&amp;revision=2017-10-30</id>\n" +
                        "                </module>\n" +
                        "            </modules>\n" +
                        "            <module-sets>\n" +
                        "                <module-set>\n" +
                        "                    <id>System-global</id>\n" +
                        "                </module-set>\n" +
                        "            </module-sets>\n" +
                        "     </yang-library>\n" +
                        "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "     </modules-state>\n" +
                        "    </filter>\n" +
                        "  </get>\n" +
                        "</rpc>\n";

        String response = ietfYangLibrary.processRequest(m_clientInfo, request);
        assertTrue(response.contains("yang-library"));
    }
}