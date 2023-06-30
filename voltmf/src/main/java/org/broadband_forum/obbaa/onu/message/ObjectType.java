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

import org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants;

/**
 * <p>
 * Enum to help convert the object type between proto data and NetworkWideTag object
 * </p>
 * Created by Filipe Claudio (Altice Labs) on 19/05/2021.
 * Updated by Miguel Melo (Altice Labs) on 25/01/2022.
 */
public enum ObjectType {
    ONU(0),
    VOMCI_FUNCTION(1),
    VOMCI_PROXY(2),
    VOLTMF(3),
    D_OLT_VNF(4);

    private int m_code;
    private static final String YANG_ONU = NetworkFunctionNSConstants.ONU_FUNCTION;
    private static final String YANG_VOMCI_FUNCTION = NetworkFunctionNSConstants.VOMCI_FUNCTION;
    private static final String YANG_VOMCI_PROXY = NetworkFunctionNSConstants.PROXY_FUNCTION;
    private static final String YANG_VOLTMF = NetworkFunctionNSConstants.VOLTMF_FUNCTION;

    private static final String YANG_PPPOE_IA = NetworkFunctionNSConstants.PPPOE_IA;

    ObjectType(int code) {
        this.m_code = code;
    }

    public int getCode() {
        return m_code;
    }

    public static ObjectType getObjectTypeFromCode(Integer code) {
        return Arrays.stream(ObjectType.values())
                .filter(objectType -> objectType.getCode() == code)
                .findFirst()
                .orElse(null);
    }

    public static ObjectType getObjectTypeFromName(String name) {
        if (name == null) {
            return null;
        }
        return Arrays.stream(ObjectType.values())
                .filter(objectType -> objectType.name().equals(name.toUpperCase()))
                .findFirst()
                .orElse(null);
    }

    public static ObjectType getObjectTypeFromYangString(String yang) {
        /*
        * This function receive something like this: bbf-nf-types:vomci-function-type
        * The namespace "bbf-nf-types" does matter for the purpose of this function.
        * */
        if (yang == null) {
            return null;
        }
        String networkFunctionYang = yang.split(":")[1];
        switch (networkFunctionYang) {
            case YANG_ONU:
                return ObjectType.ONU;
            case YANG_VOMCI_FUNCTION:
                return ObjectType.VOMCI_FUNCTION;
            case YANG_VOMCI_PROXY:
                return ObjectType.VOMCI_PROXY;
            case YANG_VOLTMF:
                return ObjectType.VOLTMF;
            case YANG_PPPOE_IA:
                return ObjectType.D_OLT_VNF;
            default:
                return null;
        }
    }
}
