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

/**
 * Netconf version management.
 */
public final class NetconfVersionManager {
    private NetconfVersionManager() {
        //Hide
    }

    /**
     * Get class correct version of rpc-reply JAXB.
     *
     * @param rpcObject Rpc message object
     * @return Class for rpc-reply building
     */
    public static Class getRpcReplyClass(Object rpcObject) {
        if (rpcObject.getClass() == RpcV10.class) {
            return RpcReplyV10.class;
        }

        if (rpcObject.getClass() == RpcV11.class) {
            return RpcReplyV11.class;
        }

        return null;
    }

    /**
     * Get class correct version of rpc-reply JAXB.
     *
     * @param rpcNamespace Namespace of the request
     * @return Class for rpc-reply building
     */
    public static Class getRpcReplyClass(String rpcNamespace) {
        switch (rpcNamespace) {
            case NetconfProtocol.VERSION_1_0:
                return RpcReplyV10.class;
            case NetconfProtocol.VERSION_1_1:
                return RpcReplyV11.class;

            default:
                return null;
        }
    }
}
