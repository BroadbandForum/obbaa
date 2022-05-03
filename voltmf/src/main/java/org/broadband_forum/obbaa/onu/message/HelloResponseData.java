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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Response information received from vOMCI Hello Response.
 * </p>
 * Created by Miguel Melo (Altice Labs) on 13/01/2022
 * */
public class HelloResponseData {

    public enum NfCapabilities {
        NO_CAPABILITY_REPORTED(0),
        ONU_STATE_ONLY_SUPPORT(1),
        ONU_CONFIG_REPLICA_SUPPORT(2);

        private int m_code;

        NfCapabilities(int code) {
            this.m_code = code;
        }

        public int getCode() {
            return m_code;
        }

        public static NfCapabilities getObjectTypeFromCode(Integer code) {
            return Arrays.stream(NfCapabilities.values())
                    .filter(capabilities -> capabilities.getCode() == code)
                    .findFirst()
                    .orElse(null);
        }

        public static NfCapabilities getObjectTypeFromName(String name) {
            if (name == null) {
                return null;
            }
            return Arrays.stream(NfCapabilities.values())
                    .filter(capabilities -> capabilities.name().equals(name.toUpperCase()))
                    .findFirst()
                    .orElse(null);
        }
    }

    private String identifier; // msg_id
    private String senderName; // sender_name
    private ObjectType objectType; // object_type
    private String objectName; // object_name
    private String serviceEndpointName; // HelloResp : service_endpoint_name
    private Map<String, String> nfTypes;
    private NfCapabilities nfCapabilities;

    public HelloResponseData(String senderName, ObjectType objectType, String objectName,
                             String identifier, String serviceEndpointName,
                             Map<String, String> nfTypes, NfCapabilities nfCapabilities) {
        this.identifier = identifier;
        this.senderName = senderName;
        this.objectType = objectType;
        this.objectName = objectName;
        this.serviceEndpointName = serviceEndpointName;
        this.nfTypes = nfTypes;
        if (nfCapabilities == null) {
            this.nfCapabilities = NfCapabilities.NO_CAPABILITY_REPORTED;
        } else {
            this.nfCapabilities = nfCapabilities;
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getSenderName() {
        return senderName;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public Map<String, String> getNfTypes() {
        return nfTypes;
    }

    public void setNfTypesypes(Map<String, String> nfTypes) {
        this.nfTypes = nfTypes;
    }

    public NfCapabilities getNfCapabilities() {
        return nfCapabilities;
    }

    public void setNfCapabilities(NfCapabilities nfCapabilities) {
        this.nfCapabilities = nfCapabilities;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("\nResponseData: \n");
        sb.append("header { \n");
        sb.append("  msg_id: ").append(identifier + "\n");
        sb.append("  sender_name: ").append(senderName + "\n");
        sb.append("  object_type: ").append(objectType + "\n");
        sb.append("  object_name: ").append(objectName + "\n");
        sb.append("} \n");
        sb.append("body { \n");
        sb.append("  response { \n");
        sb.append("    hello_resp { \n");
        sb.append("      service_endpoint_name: ").append(serviceEndpointName + "\n");
        sb.append("      network_function_info { " + "\n");
        sb.append("        nf_types {\n");
        sb.append("         ").append(nfTypes.toString() + "\n");
        sb.append("        } \n");
        sb.append("        capabilities: ").append(nfCapabilities + "\n");
        sb.append("      } \n");
        sb.append("    } \n");
        sb.append("  } \n");
        sb.append("} \n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (ob == null || getClass() != ob.getClass()) {
            return false;
        }
        HelloResponseData that = (HelloResponseData) ob;
        return Objects.equals(identifier, that.identifier) && Objects.equals(senderName, that.senderName)
                && objectType == that.objectType && Objects.equals(objectName, that.objectName)
                && Objects.equals(serviceEndpointName, that.serviceEndpointName)
                && Objects.equals(nfTypes, that.nfTypes) && nfCapabilities == that.nfCapabilities;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, senderName, objectType, objectName, serviceEndpointName, nfTypes, nfCapabilities);
    }
}
