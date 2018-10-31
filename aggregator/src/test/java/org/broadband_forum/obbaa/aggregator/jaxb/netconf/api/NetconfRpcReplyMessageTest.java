package org.broadband_forum.obbaa.aggregator.jaxb.netconf.api;

import org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl.AggregatorUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.ErrorInfo;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcError;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class NetconfRpcReplyMessageTest {
    private static final String MESSAGE_ID = "101";
    private static final String V10 = "1.0";
    private static final String V11 = "1.1";

    @Test
    public void getRpcReplyPayloads() throws Exception {
        String reply =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520491011759\">\n" +
                "  <data>\n" +
                "    <boards xmlns=\"urn:huawei:params:xml:ns:yang:huawei-board\">\n" +
                "      <board>\n" +
                "        <name>0.9</name>\n" +
                "        <description>H901MPLB_0_9</description>\n" +
                "      </board>\n" +
                "      <board>\n" +
                "        <name>0.10</name>\n" +
                "        <description>H901PILA_0_10</description>\n" +
                "      </board>\n" +
                "    </boards>\n" +
                "  </data>\n" +
                "</rpc-reply>\n";

        NetconfRpcReplyMessage netconfRpcReplyMessage = new NetconfRpcReplyMessage(reply);
        String payload = netconfRpcReplyMessage.getPayloads().get(0);
        assertTrue(payload.contains("<name>0.10</name>"));
    }

    @Test
    public void buildRpcReplyOk() throws Exception {
        String response = NetconfRpcReplyMessage.buildRpcReplyOkV10(MESSAGE_ID);
        assertTrue(response.contains(MESSAGE_ID));
        assertTrue(response.contains(V10));

        response = NetconfRpcReplyMessage.buildRpcReplyOkV11(MESSAGE_ID);
        assertTrue(response.contains(MESSAGE_ID));
        assertTrue(response.contains(V11));
        assertTrue(response.contains("ok"));
    }

    @Test
    public void buildRpcReplyData() throws Exception {
        String data =
                "<boards xmlns=\"urn:huawei:params:xml:ns:yang:huawei-board\">\n" +
                "  <board>\n" +
                "    <name>0.9</name>\n" +
                "    <description>H901MPLB_0_9</description>\n" +
                "      </board>\n" +
                "  <board>\n" +
                "    <name>0.10</name>\n" +
                "    <description>H901PILA_0_10</description>\n" +
                "  </board>\n" +
                "</boards>";

        List<Node> nodes = new ArrayList<>();
        Document document = AggregatorUtils.stringToDocument(data);
        nodes.add(document.getFirstChild());

        String reply = NetconfRpcReplyMessage.buildRpcReplyDataV10(MESSAGE_ID, nodes);
        assertTrue(reply.contains("huawei-board"));
        assertTrue(reply.contains(V10));

        reply = NetconfRpcReplyMessage.buildRpcReplyDataV11(MESSAGE_ID, nodes);
        assertTrue(reply.contains("H901MPLB_0_9"));
        assertTrue(reply.contains(V11));
    }

    @Test
    public void buildRpcReplyError() throws Exception {
        RpcError rpcError = new RpcError();
        rpcError.setErrorTag("error-type");
        rpcError.setErrorInfo(new ErrorInfo());
        rpcError.setErrorMessage("error-message");
        rpcError.setErrorType("error-type");

        String reply = NetconfRpcReplyMessage.buildRpcReplyErrorV10(MESSAGE_ID, rpcError);
        assertTrue(reply.contains(V10));
        assertTrue(reply.contains("error"));

        reply = NetconfRpcReplyMessage.buildRpcReplyErrorV11(MESSAGE_ID, rpcError);
        assertTrue(reply.contains(V11));
        assertTrue(reply.contains("error"));
    }
}