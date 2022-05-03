/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.aggregator.impl;

import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.w3c.dom.Document;

/**
 * <p>
 * Request of one Network Function mounted to the network-manager
 * </p>
 * Created by J.V.Correia (Altice Labs) on 26/01/2022.
 */

public class SingleNetworkFunctionRequest {
    private Document m_requestToBeProcessed;
    private String m_networkFunctionName;
    private String m_networkFunctionType;

    public SingleNetworkFunctionRequest(String networkFunctionName, String networkFunctionType, Document requestToBeProcessed) {
        m_networkFunctionName = networkFunctionName;
        m_networkFunctionType = networkFunctionType;
        m_requestToBeProcessed = requestToBeProcessed;
    }

    Document getDocument() {
        return m_requestToBeProcessed;
    }

    String getNetworkFunctionName() {
        return m_networkFunctionName;
    }

    public void setNetworkFunctionType(String networkFunctionType) {
        m_networkFunctionType = networkFunctionType;
    }

    public String getNetworkFunctionType() {
        return m_networkFunctionType;
    }

    String getNamespace() {
        String namespace = NetconfMessageUtil.getParentXmlns(m_requestToBeProcessed);
        return namespace;
    }

    String getMessage() {
        String message = PojoToDocumentTransformer.requestToString(m_requestToBeProcessed);
        return message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
