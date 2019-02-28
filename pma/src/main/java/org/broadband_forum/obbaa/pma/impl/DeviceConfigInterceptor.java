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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.INTERFACE_VERSION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MODEL;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.REVISION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.TYPE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.VENDOR;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInterceptorChain;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DeviceConfigInterceptor implements ModelNodeInterceptor {

    private static final Logger LOGGER = Logger.getLogger(DeviceConfigInterceptor.class);

    private SchemaPath m_schemaPathNwMgr = new SchemaPathBuilder().withNamespace(NS)
            .withRevision(REVISION).appendLocalName("network-manager").appendLocalName("managed-devices")
                    .appendLocalName("device").appendLocalName("device-management").build();
    private AdapterManager m_adapterManager;

    public DeviceConfigInterceptor(AdapterManager adapterManager) {
        m_adapterManager = adapterManager;
        ModelNodeInterceptorChain.getInstance().registerInterceptor(m_schemaPathNwMgr, this);
    }

    @Override
    public void interceptEditConfig(HelperDrivenModelNode modelNode, EditContext editContext) throws EditConfigException {
        EditContainmentNode editNode = editContext.getEditNode();
        if (editNode.getName().equals("device-management") && editNode.getNamespace().equals(NS)
                && isCreateOrMergeOrReplace(editNode.getEditOperation())) {
            DeviceAdapterId adapterId = getAdapterId(editNode);

            if (adapterId.getType() == null || adapterId.getInterfaceVersion() == null
                    || adapterId.getModel() == null || adapterId.getVendor() == null) {
                // this is just the modification of some properties of the device, not a new device
                return;
            }

            // check if its a known device or conforms to a deployed adapter
            if (m_adapterManager.getDeviceAdapter(adapterId) == null) {
                NetconfRpcError error = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Invalid adapter details for device. " + adapterId + " not deployed");
                throw new EditConfigException(error);
            }
        }
    }

    private DeviceAdapterId getAdapterId(EditContainmentNode editNode) {
        String type = null;
        String interfaceVersion = null;
        String model = null;
        String vendor = null;
        for (EditChangeNode editChangeNode : editNode.getChangeNodes()) {
            if (editChangeNode.getName().equals(TYPE) && editChangeNode.getNamespace().equals(NS)) {
                type = editChangeNode.getValue();
            } else if (editChangeNode.getName().equals(INTERFACE_VERSION) && editChangeNode.getNamespace().equals(NS)) {
                interfaceVersion = editChangeNode.getValue();
            } else if (editChangeNode.getName().equals(MODEL) && editChangeNode.getNamespace().equals(NS)) {
                model = editChangeNode.getValue();
            } else if (editChangeNode.getName().equals(VENDOR) && editChangeNode.getNamespace().equals(NS)) {
                vendor = editChangeNode.getValue();
            }
        }
        return new DeviceAdapterId(type, interfaceVersion, model, vendor);
    }

    private boolean isCreateOrMergeOrReplace(String editOperation) {
        return EditConfigOperations.CREATE.equals(editOperation) || EditConfigOperations.MERGE.equals(editOperation)
                || EditConfigOperations.REPLACE.equals(editOperation);
    }

}
