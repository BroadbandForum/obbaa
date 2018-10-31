package org.broadband_forum.obbaa.aggregator.jaxb.netconf.api;

import org.junit.Test;
import org.w3c.dom.Document;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetconfRpcMessageTest {
    @Test
    public void getPayloadsTest() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
                "  <get>\n" +
                "    <source>\n" +
                "      <running />\n" +
                "    </source>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </yang-library>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>\n";

        NetconfRpcMessage netconfRpcMessage = new NetconfRpcMessage(request);

        List<Document> documents = netconfRpcMessage.getPayloadDocuments();
        assertFalse(documents.isEmpty());
        assertEquals("urn:ietf:params:xml:ns:yang:ietf-yang-library", documents.get(0).getFirstChild().getNamespaceURI());

        List<String> payloads = netconfRpcMessage.getPayloads();
        assertTrue(payloads.get(0).contains("yang-library"));
    }

    @Test
    public void getRpcTest() throws Exception {
        String request =
                "<rpc message-id=\"3\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <get>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <schema-mounts xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount\">\n" +
                "      <mount-point/>\n" +
                "    </schema-mounts>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>";

        NetconfRpcMessage netconfRpcMessage = new NetconfRpcMessage(request);
        assertTrue(netconfRpcMessage.getRpc().getGet() != null);
    }

    @Test
    public void getOriginalMessageTest() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc message-id=\"10101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "<get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <source>\n" +
                "    <running/>\n" +
                "  </source>\n" +
                "  <filter type=\"subtree\">\n" +
                "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "      <device/>\n" +
                "    </managed-devices>\n" +
                "  </filter>\n" +
                "</get-config>\n" +
                "</rpc>\n";

        NetconfRpcMessage netconfRpcMessage = new NetconfRpcMessage(request);
        assertEquals(request, netconfRpcMessage.getOriginalMessage());
    }

}