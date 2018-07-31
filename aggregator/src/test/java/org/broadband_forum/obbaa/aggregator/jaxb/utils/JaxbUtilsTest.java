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

package org.broadband_forum.obbaa.aggregator.jaxb.utils;

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcV10;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV11;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JaxbUtilsTest {
    private static String MESSAGE_ID = "212";

    @Test
    public void marshalToString() throws Exception {
        RpcV10 rpcV10 = new RpcV10();
        rpcV10.setMessageId(MESSAGE_ID);

        String result = JaxbUtils.marshalToString(rpcV10);
        assertTrue(result.contains("rpc"));
        assertTrue(result.contains(MESSAGE_ID));

        RpcV10 rpcUnmarshal = JaxbUtils.unmarshal(result, RpcV10.class);
        assertEquals(rpcV10.getMessageId(), rpcUnmarshal.getMessageId());
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
