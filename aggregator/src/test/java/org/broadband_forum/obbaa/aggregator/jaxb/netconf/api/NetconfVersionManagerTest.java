package org.broadband_forum.obbaa.aggregator.jaxb.netconf.api;

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcV10;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcV11;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV10;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV11;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NetconfVersionManagerTest {
    @Test
    public void getRpcReplyClass() throws Exception {
        Class clazz = NetconfVersionManager.getRpcReplyClass(new RpcV10());
        assertEquals(RpcReplyV10.class, clazz);

        clazz = NetconfVersionManager.getRpcReplyClass(new RpcV11());
        assertEquals(RpcReplyV11.class, clazz);

        clazz = NetconfVersionManager.getRpcReplyClass(NetconfProtocol.VERSION_1_0);
        assertEquals(RpcReplyV10.class, clazz);

        clazz = NetconfVersionManager.getRpcReplyClass(NetconfProtocol.VERSION_1_1);
        assertEquals(RpcReplyV11.class, clazz);
    }
}