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

package org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply;

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.common.Data;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.common.MessageBase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.NONE)
public class RpcReply extends MessageBase {
    private Ok ok;
    private Data data;
    private RpcError rpcError;
    private RpcReplyType rpcReplyType;

    @XmlElement
    public Ok getOk() {
        return ok;
    }

    public void setOk(Ok ok) {
        this.ok = ok;

        setPayloadObjectOwner(getOk());
        setRpcReplyType(RpcReplyType.OK);
    }

    @XmlElement
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;

        setPayloadObjectOwner(getData());
        setRpcReplyType(RpcReplyType.RPC_REPLY_DATA);
    }

    @XmlElement
    public RpcError getRpcError() {
        return rpcError;
    }

    public void setRpcError(RpcError rpcError) {
        this.rpcError = rpcError;

        setPayloadObjectOwner(getRpcError());
        setRpcReplyType(RpcReplyType.RPC_ERROR);
    }

    @XmlTransient
    public RpcReplyType getRpcReplyType() {
        return rpcReplyType;
    }

    private void setRpcReplyType(RpcReplyType rpcReplyType) {
        this.rpcReplyType = rpcReplyType;
    }
}
