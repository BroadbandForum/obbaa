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

package org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc;

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.common.MessageBase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Rpc extends MessageBase {
    private Action action;
    private Get get;
    private GetConfig getConfig;
    private EditConfig editConfig;
    private CopyConfig copyConfig;
    private DeleteConfig deleteConfig;

    private RpcOperationType rpcOperationType;

    @XmlElement
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;

        setPayloadObjectOwner(getAction());
        setRpcOperationType(RpcOperationType.ACTION);
    }

    @XmlElement(name = "get")
    public Get getGet() {
        return get;
    }

    public void setGet(Get get) {
        this.get = get;

        setPayloadObjectOwner(getGet().getFilter());
        setRpcOperationType(RpcOperationType.GET);
    }

    @XmlElement(name = "get-config")
    public GetConfig getGetConfig() {
        return getConfig;
    }

    public void setGetConfig(GetConfig getConfig) {
        this.getConfig = getConfig;

        setPayloadObjectOwner(getGetConfig().getFilter());
        setRpcOperationType(RpcOperationType.GET_CONFIG);
    }

    @XmlElement(name = "edit-config")
    public EditConfig getEditConfig() {
        return editConfig;
    }

    public void setEditConfig(EditConfig editConfig) {
        this.editConfig = editConfig;

        setPayloadObjectOwner(getEditConfig().getConfig());
        setRpcOperationType(RpcOperationType.EDIT_CONFIG);
    }

    @XmlElement(name = "copy-config")
    public CopyConfig getCopyConfig() {
        return copyConfig;
    }

    public void setCopyConfig(CopyConfig copyConfig) {
        this.copyConfig = copyConfig;

        setPayloadObjectOwner(getCopyConfig());
        setRpcOperationType(RpcOperationType.COPY_CONFIG);
    }

    @XmlElement(name = "delete-config")
    public DeleteConfig getDeleteConfig() {
        return deleteConfig;
    }

    public void setDeleteConfig(DeleteConfig deleteConfig) {
        this.deleteConfig = deleteConfig;

        setPayloadObjectOwner(getDeleteConfig());
        setRpcOperationType(RpcOperationType.DELETE_CONFIG);
    }

    @XmlTransient
    public RpcOperationType getRpcOperationType() {
        return rpcOperationType;
    }

    protected void setRpcOperationType(RpcOperationType rpcOperationType) {
        this.rpcOperationType = rpcOperationType;
    }
}
