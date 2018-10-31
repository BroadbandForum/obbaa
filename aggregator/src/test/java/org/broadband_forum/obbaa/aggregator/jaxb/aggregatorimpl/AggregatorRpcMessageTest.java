package org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AggregatorRpcMessageTest {
    private static String REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
            "  <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
            "    <reload-yang-library xmlns=\"urn:bbf:yang:obbaa:pma-yang-library\">\n" +
            "        <reload/>\n" +
            "    </reload-yang-library>\n" +
            "  </action>\n" +
            "</rpc>\n";

    private AggregatorRpcMessage aggregatorRpcMessage;

    @Before
    public void setUp() throws Exception {
        aggregatorRpcMessage = new AggregatorRpcMessage(REQUEST);
    }

    @Test
    public void getNetconfRpcMessage() throws Exception {
        assertEquals(REQUEST, aggregatorRpcMessage.getNetconfRpcMessage().getOriginalMessage());
    }

    @Test
    public void getOnlyOneTopXmlns() throws Exception {
        assertEquals("urn:bbf:yang:obbaa:pma-yang-library", aggregatorRpcMessage.getOnlyOneTopXmlns());
    }

    @Test
    public void getOnlyOneTopPayload() throws Exception {
        assertEquals("reload-yang-library",
                aggregatorRpcMessage.getOnlyOneTopPayload().getFirstChild().getLocalName());
    }

    @Test
    public void isNetworkManagerMessage() throws Exception {
        assertFalse(aggregatorRpcMessage.isNetworkManagerMessage());
    }

}