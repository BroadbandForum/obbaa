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
