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

/**
 * <p>
 * Enum to help convert the object type between proto data and NetworkWideTag object
 * </p>
 * Created by Filipe Cláudio (Altice Labs) on 19/05/2021.
 */
public enum ObjectType {
    ONU(0),
    VOMCI_FUNCTION(1),
    VOMCI_PROXY(2),
    VOLTMF(3);

    private int m_code;

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
}
