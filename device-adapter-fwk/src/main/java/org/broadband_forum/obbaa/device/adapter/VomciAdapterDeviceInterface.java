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

package org.broadband_forum.obbaa.device.adapter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.impl.NcCompliantAdapterDeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class VomciAdapterDeviceInterface extends NcCompliantAdapterDeviceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(VomciAdapterDeviceInterface.class);
    private Queue<DeviceConfigBackup> m_backupDatastore;

    public VomciAdapterDeviceInterface(NetconfConnectionManager netconfConnManager) {
        super(netconfConnManager);
    }

    @Override
    public void veto(Device device, EditConfigRequest request, Document oldDataStore, Document updatedDataStore)
            throws SubSystemValidationException {
        if (device.isMediatedSession()) {
            //Maintain backup of datastore: oldDatastore and updatedDatastore
            String oldStore;
            try {
                if (oldDataStore == null) {
                    oldStore = "<data> </data>";
                } else {
                    oldStore = DocumentUtils.documentToPrettyString(oldDataStore.getDocumentElement());
                }
                if (m_backupDatastore == null) {
                    m_backupDatastore = new ConcurrentLinkedQueue<>();
                }
                m_backupDatastore.add(new DeviceConfigBackup(oldStore,
                        DocumentUtils.documentToPrettyString(updatedDataStore.getDocumentElement())));
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("error occurred duting veto", e);
                throw new SubSystemValidationException(new NetconfRpcError(
                        NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                        NetconfRpcErrorSeverity.Error, "Error occurred during veto"));
            }
        }
    }

    public DeviceConfigBackup getDatastoreBackup() {
        return m_backupDatastore.poll();
    }
}
