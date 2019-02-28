package org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AggregatorRpcMessageTest {
    private static String REQUEST =
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
                    "    <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
                    "        <deploy-adapter xmlns=\"urn:bbf:yang:obbaa:device-adapters\">\n" +
                    "            <deploy>\n" +
                    "                <adapter-archive>adapterExample.zip</adapter-archive>\n" +
                    "            </deploy>\n" +
                    "        </deploy-adapter>\n" +
                    "    </action>\n" +
                    "</rpc>";

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
        assertEquals("urn:bbf:yang:obbaa:device-adapters", aggregatorRpcMessage.getOnlyOneTopXmlns());
    }

    @Test
    public void getOnlyOneTopPayload() throws Exception {
        assertEquals("deploy-adapter",
                aggregatorRpcMessage.getOnlyOneTopPayload().getFirstChild().getLocalName());
    }

    @Test
    public void isNetworkManagerMessage() throws Exception {
        assertFalse(aggregatorRpcMessage.isNetworkManagerMessage());
    }

}