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

import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

public final class NetconfProtocol {
    private NetconfProtocol() {
        //Hide
    }

    public static final String RPC = "rpc";
    public static final String RPC_REPLY = "rpc-reply";

    public static final String VERSION_1_0 = "urn:ietf:params:xml:ns:netconf:base:1.0";
    public static final String VERSION_1_1 = "urn:ietf:params:xml:ns:netconf:base:1.1";

    /**
     * Build data of existed object.
     * @param object Object of payload
     * @param template Action
     */
    public static void buildNonEmpty(Object object, NetconfRootBuilder template) {
        if (object == null) {
            return;
        }

        template.execute();
    }

    /**
     * Build data even the object is null.
     *
     * @param subClass Sub class
     * @param object Parent object
     * @param template Template
     * @return New object
     */
    public static Object buildEvenEmpty(Class subClass, Object object, NetconfSubDataBuilder template) {
        try {
            if (object == null) {
                object = subClass.newInstance();
            }

            template.execute(object);
            return object;
        }
        catch (InstantiationException | IllegalAccessException ex) {
            return null;
        }
    }

    /**
     * Build module capability based one YANG model.
     *
     * @param moduleIdentifier YANG model identifier
     * @return Module capability
     */
    public static String buildCapability(ModuleIdentifier moduleIdentifier) {
        return String.format("%s?module=%s&revision=%s", moduleIdentifier.getNamespace(),
                moduleIdentifier.getName(), moduleIdentifier.getQNameModule().getFormattedRevision());
    }
}
