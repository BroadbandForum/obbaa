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
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.IN_ERROR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.tx.TXTemplate;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;

public class NetconfDeviceAlignmentServiceImpl implements NetconfDeviceAlignmentService {
    public static final String EDITS_PENDING = "%s Edit(s) Pending";
    public static final String REQUEST_TIMED_OUT = "request timed out";
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String RESPONSE_RECEIVED = "response received : ";
    public static final String REQUEST_SENT = "request sent : ";
    public static final String COMMA = ",";
    public static final String ALIGNMENT_STATE = "alignmentState";
    private static final Logger LOGGER = Logger.getLogger(NetconfDeviceAlignmentServiceImpl.class);
    private final NetconfConnectionManager m_ncm;
    private final DeviceManager m_dm;
    private TxService m_txService;
    private ConcurrentHashMap<String, List<EditConfigRequest>> m_queue = new ConcurrentHashMap<>();

    public NetconfDeviceAlignmentServiceImpl(DeviceManager dm, NetconfConnectionManager ncm) {
        m_dm = dm;
        m_ncm = ncm;
    }

    public void init() {
        m_dm.addDeviceStateProvider(this);
    }

    public void destroy() {
        m_dm.removeDeviceStateProvider(this);
    }

    public TxService getTxService() {
        return m_txService;
    }

    public void setTxService(TxService txService) {
        m_txService = txService;
    }

    @Override
    public void queueEdit(String deviceName, EditConfigRequest request) {
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                Device device = m_dm.getDevice(deviceName);
                if (device.isNeverAligned()) {
                    LOGGER.info(String.format("Device %s is never aligned, new edit requests wont be queued to be sent to the device,"
                        + " please execute a full re-sync", device));
                    return null;
                }
                if (device.isInError()) {
                    LOGGER.info(String.format("Device %s is in error, new edit requests wont be queued to be sent to the device,"
                        + " please execute a full re-sync", device));
                    return null;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Queueing edit request %s for device %s", request.requestToString(), device));
                }
                new WithSynchronousDeviceQueue<Void>() {
                    @Override
                    Void execute(String deviceName, List<EditConfigRequest> deviceQueue) {
                        deviceQueue.add(request);
                        setDeviceAlignmentState(deviceName, getAlignmentState(deviceName));
                        return null;
                    }
                }.execute(deviceName);
                return null;
            }
        });
    }

    private List<EditConfigRequest> getQueueForDevice(String deviceName) {
        List<EditConfigRequest> editConfigRequests = m_queue.get(deviceName);
        if (editConfigRequests == null) {
            m_queue.putIfAbsent(deviceName, new ArrayList<>());
            editConfigRequests = m_queue.get(deviceName);
        }
        return editConfigRequests;
    }

    @Override
    public List<EditConfigRequest> getEditQueue(String deviceName) {
        return new WithSynchronousDeviceQueue<List<EditConfigRequest>>() {
            @Override
            List<EditConfigRequest> execute(String info, List<EditConfigRequest> deviceQueue) {
                return Collections.unmodifiableList(deviceQueue);
            }
        }.execute(deviceName);
    }

    public void alignAllDevices() {
        for (Map.Entry<String, List<EditConfigRequest>> queueEntry : m_queue.entrySet()) {
            align(queueEntry.getKey());
        }
    }

    @Override
    public String getAlignmentState(String deviceName) {
        List<EditConfigRequest> queue = getEditQueue(deviceName);
        int numOfEdits = queue.size();
        if (numOfEdits == 0) {
            return ALIGNED;
        }
        return String.format(EDITS_PENDING, numOfEdits);
    }

    @Override
    public LinkedHashMap<String, Object> getState(String deviceName) {
        return m_txService.executeWithTx(new TXTemplate<LinkedHashMap<String, Object>>() {
            @Override
            public LinkedHashMap<String, Object> execute() {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put(ALIGNMENT_STATE, m_dm.getDevice(deviceName).getDeviceManagement().getDeviceState().getConfigAlignmentState());
                return map;
            }
        });
    }

    protected ConcurrentHashMap<String, List<EditConfigRequest>> getQueue() {
        return m_queue;
    }

    @Override
    public void deviceAdded(String deviceName) {
        m_queue.put(deviceName, new ArrayList<>());
    }

    @Override
    public void deviceRemoved(String deviceName) {
        m_queue.remove(deviceName);
    }

    @Override
    public void forceAlign(String deviceName, CopyConfigRequest request) {
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                new WithSynchronousDeviceQueue<Void>() {
                    @Override
                    Void execute(String info, List<EditConfigRequest> deviceQueue) throws RuntimeException {
                        try {
                            deviceQueue.clear();
                            Future<NetConfResponse> responseFuture = m_ncm.executeNetconf(info, request);
                            NetConfResponse response = null;

                            response = responseFuture.get();
                            if (response == null || !response.isOk()) {
                                markDeviceInError(info, deviceQueue, request.requestToString(), response);
                            } else {
                                markDeviceAligned(info);
                            }

                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        return null;
                    }
                }.execute(deviceName);
                return null;
            }
        });
    }

    private void markDeviceAligned(String deviceName) {
        setDeviceAlignmentState(deviceName, ALIGNED);
    }

    private void setDeviceAlignmentState(String deviceName, String verdict) {
        m_dm.updateConfigAlignmentState(deviceName, verdict);
    }

    private String getErrorDetails(String request, NetConfResponse errorResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_SENT);
        sb.append(request);
        sb.append(COMMA).append(LINE_SEPARATOR);
        sb.append(RESPONSE_RECEIVED);
        if (errorResponse == null) {
            sb.append(REQUEST_TIMED_OUT);
        } else {
            sb.append(errorResponse.responseToString());
        }
        return sb.toString();
    }

    @Override
    public void align(String deviceName) {
        LOGGER.debug("Trying to flush changes to the device " + deviceName);
        WithSynchronousDeviceQueue alignTemplate = new WithSynchronousDeviceQueue<Void>() {
            @Override
            public Void execute(String deviceName, List queueForDevice) {
                Iterator<EditConfigRequest> iterator = queueForDevice.iterator();
                while (iterator.hasNext()) {
                    EditConfigRequest request = iterator.next();
                    try {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("Trying to send edit %s to device %s",
                                request.requestToString(), deviceName));
                        }
                        Future<NetConfResponse> future = m_ncm.executeNetconf(deviceName, request);
                        NetConfResponse response = future.get();
                        if (response == null || !response.isOk()) {
                            markDeviceInError(deviceName, queueForDevice, request.requestToString(), response);
                            break;
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(String.format("Successfully sent edit %s to device %s", request.requestToString(),
                                    deviceName));
                            }
                            iterator.remove();
                        }
                    } catch (IllegalStateException e) {
                        LOGGER.debug(String.format("Device %s is not connected, alignment would be retried later", deviceName), e);
                    } catch (Exception e) {
                        LOGGER.error(String.format("Error while aligning configuration with the device %s", deviceName), e);
                    }
                }
                if (queueForDevice.isEmpty() && !m_dm.getDevice(deviceName).isInError()) {
                    markDeviceAligned(deviceName);
                }

                return null;
            }
        };
        alignTemplate.execute(deviceName);

    }

    private void markDeviceInError(String deviceInfo, List queueForDevice, String request, NetConfResponse response) {
        LOGGER.error(String.format("Edit failure/timeout on device %s, edit request %s , response %s, remaining requests "
            + "won't be sent to the device", deviceInfo, request, getResponseString(response)));
        queueForDevice.clear();
        setDeviceAlignmentState(deviceInfo, String.format(IN_ERROR + ", %s", getErrorDetails(request, response)));
    }

    private String getResponseString(NetConfResponse response) {
        if (response != null) {
            return response.responseToString();
        }
        return null;
    }

    private abstract class WithSynchronousDeviceQueue<T> {
        abstract T execute(String deviceName, List<EditConfigRequest> deviceQueue);

        T execute(String deviceName) {
            List<EditConfigRequest> queue = getQueueForDevice(deviceName);
            T result;
            synchronized (queue) {
                result = execute(deviceName, queue);
            }
            return result;
        }
    }
}
