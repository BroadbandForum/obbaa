/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu.message;

import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;

/**
 * <p>
 * Provides APIs to format the messages
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 13/05/2021.
 */
public interface MessageFormatter<T> {

    T getFormattedRequest(AbstractNetconfRequest request, String operationType, Device onuDevice,
                               AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                               SchemaRegistry schemaRegistry, NetworkWideTag networkWideTag)
            throws NetconfMessageBuilderException, MessageFormatterException;

    T getFormattedRequestForNF(AbstractNetconfRequest request,
                                        String operationType,
                                        NetworkFunction networkFunction,
                                        ModelNodeDataStoreManager modelNodeDsm,
                                        AdapterManager adapterManager)
            throws NetconfMessageBuilderException, MessageFormatterException;

    T getFormattedHelloRequest(String msgId, String networkFunctionName,
                               ObjectType type, String localEndpointName) throws MessageFormatterException;

    boolean isHelloResponse(Object responseObject) throws MessageFormatterException;

    HelloResponseData getHelloResponseData(Object responseObject) throws MessageFormatterException;

    ResponseData getResponseData(Object responseObject) throws MessageFormatterException;
}
