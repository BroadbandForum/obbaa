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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ALIGNED;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEVER_ALIGNED;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.AlignmentOption;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.dmyang.tx.TXTemplate;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.w3c.dom.Element;

public class AlignmentTimer {
    private static final Logger LOGGER = Logger.getLogger(AlignmentTimer.class);
    private final PmaRegistry m_pmaRegistry;
    private final Timer m_timer;
    private TxService m_txService;
    private DeviceDao m_deviceDao;
    private AdapterManager m_adapterMgr;

    public AlignmentTimer(PmaRegistry pmaRegistry, AdapterManager adaperMgr,
                          DeviceDao deviceDao,TxService txService) {
        m_pmaRegistry = pmaRegistry;
        m_adapterMgr = adaperMgr;
        m_timer = new Timer();
        m_deviceDao = deviceDao;
        m_txService = txService;
    }

    public TxService getTxService() {
        return m_txService;
    }

    public void setTxService(TxService txService) {
        m_txService = txService;
    }

    public DeviceDao getDeviceDao() {
        return m_deviceDao;
    }

    public void setDeviceDao(DeviceDao deviceDao) {
        m_deviceDao = deviceDao;
    }

    public void init() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (m_deviceDao != null && m_txService != null) {
                    try {
                        runAlignment();
                    } catch (Exception e) {
                        LOGGER.error("something wrong during alignment:" + e.getMessage());
                    }
                }
            }
        }, 10000L, 10000L);
    }

    public void destroy() {
        m_timer.cancel();
    }

    public void runAlignment() {
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                List<Device> devices = m_deviceDao.findAllDevices();
                for (Device device : devices) {
                    PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.DEVICE,device.getDeviceName());
                    try {
                        m_pmaRegistry.executeWithPmaSession(resourceId, session -> {
                            if (device.isNeverAligned()) {
                                LOGGER.debug(String.format("Device %s is " + NEVER_ALIGNED, device));
                                AlignmentOption alignmentOption = device.getAlignmentOption();
                                if (AlignmentOption.PUSH.equals(alignmentOption)) {
                                    if (deviceHasConfigurations(device)) {
                                        session.forceAlign();
                                    }
                                } else if (AlignmentOption.PULL.equals(alignmentOption)) {
                                    invokeUploadConfig(device, session);
                                    LOGGER.debug("Upload config done");
                                    m_deviceDao.updateDeviceAlignmentState(device.getDeviceName(), ALIGNED);
                                }
                            } else if (!device.isAligned() && !device.isAlignmentUnknown()) {
                                session.align();
                            }
                            return null;
                        });
                    } catch (ExecutionException e) {
                        LOGGER.error(String.format("Could not align device %s", device), e);
                    }
                }
                return null;
            }
        });
    }

    private void invokeUploadConfig(Device device, PmaSession session) throws ExecutionException {
        GetConfigRequest getConfigRequest = new GetConfigRequest();
        try {
            AdapterContext context = AdapterUtils.getAdapterContext(device, m_adapterMgr);
            NetConfResponse response = context.getDeviceInterface().getConfig(device, getConfigRequest).get();
            if (response != null) {
                List<Element> configElements = response.getDataContent();

                if (!configElements.isEmpty()) {
                    EditConfigRequest request = new EditConfigRequest()
                            .setTargetRunning()
                            .setTestOption(EditConfigTestOptions.SET)
                            .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                            .setConfigElement(new EditConfigElement()
                                    .setConfigElementContents(configElements));
                    request.setMessageId("1");
                    request.setUploadToPmaRequest();

                    Map<NetConfResponse, List<Notification>> netConfResponseListMap = session.executeNC(request.requestToString());
                    Map.Entry<NetConfResponse, List<Notification>> entry = netConfResponseListMap.entrySet().iterator().next();
                    String editResponseStr = entry.getKey().responseToString();
                    NetConfResponse editresponse = getNetconfResponse(DocumentUtils.stringToDocument(editResponseStr));
                    if (!editresponse.isOk()) {
                        throw new ExecutionException("Upload config failed", new Throwable("bad upload config"));
                    }
                } else {
                    LOGGER.info("No config found in device");
                }
                updatePmaConfigLeaf(device);
            }
        } catch (InterruptedException e) {
            throw new ExecutionException("interrupted exception", e);
        } catch (NetconfMessageBuilderException e) {
            throw new ExecutionException("Message builder", e);
        }
    }

    private void updatePmaConfigLeaf(Device device) {
        LOGGER.debug("updating the upload-pma-config leaf to True");
        device.getDeviceManagement().setPushPmaConfigurationToDevice("true");
    }

    private boolean deviceHasConfigurations(Device device) throws ExecutionException {
        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.DEVICE,device.getDeviceName());
        return m_pmaRegistry.executeWithPmaSession(resourceId, session -> {
            Map<NetConfResponse, List<Notification>> netConfResponseListTreeMap = session.executeNC(new GetConfigRequest()
                    .setSourceRunning().setMessageId("xx")
                    .requestToString());
            Map.Entry<NetConfResponse, List<Notification>> entry = netConfResponseListTreeMap.entrySet().iterator().next();
            String responseStr = entry.getKey().responseToString();
            try {
                NetConfResponse response = getNetconfResponse(DocumentUtils.stringToDocument(responseStr));
                return !response.getDataContent().isEmpty();
            } catch (NetconfMessageBuilderException e) {
                throw new ExecutionException(e);
            }
        });
    }
}
