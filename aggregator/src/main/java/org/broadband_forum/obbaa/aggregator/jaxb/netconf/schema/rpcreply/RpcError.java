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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rpc-error")
public class RpcError {
    private String errorType;
    private String errorTag;
    private String errorSeverity;
    private String errorMessage;
    private ErrorInfo errorInfo;

    @XmlElement(name = "error-type")
    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    @XmlElement(name = "error-tag")
    public String getErrorTag() {
        return errorTag;
    }

    public void setErrorTag(String errorTag) {
        this.errorTag = errorTag;
    }

    @XmlElement(name = "error-severity")
    public String getErrorSeverity() {
        return errorSeverity;
    }

    public void setErrorSeverity(String errorSeverity) {
        this.errorSeverity = errorSeverity;
    }

    @XmlElement(name = "error-message")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @XmlElement(name = "error-info")
    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }
}
