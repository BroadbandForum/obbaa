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

package org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.common;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Message basic information.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class MessageBase {
    private String messageId;
    private Object payloadObjectOwner;

    @XmlAttribute(name = "message-id")
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get owner of the payload.
     *
     * @return Parent of the payload
     */
    @XmlTransient
    private Object getPayloadObjectOwner() {
        return payloadObjectOwner;
    }

    /**
     * Set the owner of the payload. The caller must extend MessageBase.
     *
     * @param payloadObjectOwner Payload object owner
     */
    protected void setPayloadObjectOwner(Object payloadObjectOwner) {
        this.payloadObjectOwner = payloadObjectOwner;
    }

    /**
     * Get payload objects of the message.
     *
     * @return Objects
     * @throws JAXBException Exception
     */
    @XmlTransient
    public List<Object> getPayloadObjects() throws JAXBException {
        try {
            Method method = getPayloadObjectOwner().getClass().getMethod("getObjects", null);
            return (List<Object>) method.invoke(getPayloadObjectOwner(), null);
        }
        catch (NoSuchMethodException | InvocationTargetException | NullPointerException | IllegalAccessException ex) {
            throw new JAXBException("Error payload of the message.", ex);
        }
    }
}
