package org.broadband_forum.obbaa.aggregator.jaxb.utils;

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV10;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV11;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JaxbUtilsTest {
    private static String MESSAGE_ID = "212";

    @Test
    public void marshalToString() throws Exception {
        RpcReplyV10 rpcReplyV10 = new RpcReplyV10();
        rpcReplyV10.setMessageId(MESSAGE_ID);

        String result = JaxbUtils.marshalToString(rpcReplyV10);
        assertTrue(result.contains("rpc-reply"));
        assertTrue(result.contains(MESSAGE_ID));

        RpcReplyV10 rpcUnmarshal = JaxbUtils.unmarshal(result, RpcReplyV10.class);
        assertEquals(rpcReplyV10.getMessageId(), rpcUnmarshal.getMessageId());
    }

    @Test
    public void marshalToDocument() throws Exception {
        RpcReplyV11 rpcReplyV11 = new RpcReplyV11();
        rpcReplyV11.setMessageId(MESSAGE_ID);

        Document document = JaxbUtils.marshalToDocument(rpcReplyV11);
        assertEquals("rpc-reply", document.getFirstChild().getLocalName());

        RpcReplyV11 rpcReplyUnmarshal = JaxbUtils.unmarshal(document, RpcReplyV11.class);
        assertEquals(rpcReplyV11.getMessageId(), rpcReplyUnmarshal.getMessageId());
    }
}