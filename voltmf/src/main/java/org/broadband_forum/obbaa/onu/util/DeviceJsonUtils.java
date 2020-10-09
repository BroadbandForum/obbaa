/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu.util;

import java.util.LinkedList;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

/**
 * <p>
 * Utility to convert JSON device nodes to XML
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class DeviceJsonUtils {
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeDataStoreManager m_modelNodeDSM;

    public DeviceJsonUtils(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeDSM = modelNodeDSM;
    }

    public DataSchemaNode getDeviceRootSchemaNode(String localName, String namespace) {
        SchemaPathBuilder builder = new SchemaPathBuilder();
        SchemaRegistry sr = m_schemaRegistry;
        builder.withNamespace(namespace)
                .withRevision(sr.getModuleByNamespace(namespace).getQNameModule().getRevision().orElse(null))
                .appendLocalName(localName);
        return sr.getDataSchemaNode(builder.build());
    }

    public Element convertFromJsonToXmlSBI(String jsonData) throws NetconfMessageBuilderException {
        return JsonUtil.convertFromJsonToXml(m_modelNodeDSM, new Pair<>(ONUConstants.ONU_GET_OPERATION, null), new LinkedList<>(),
                jsonData, m_schemaRegistry, null, null);
    }
}
