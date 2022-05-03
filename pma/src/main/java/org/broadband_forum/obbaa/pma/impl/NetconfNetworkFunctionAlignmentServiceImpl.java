/*
 * Copyright 2022 Broadband Forum
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
import static org.broadband_forum.obbaa.pma.impl.NetconfDeviceAlignmentServiceImpl.REQUEST_SENT;
import static org.broadband_forum.obbaa.pma.impl.NetconfDeviceAlignmentServiceImpl.REQUEST_TIMED_OUT;
import static org.broadband_forum.obbaa.pma.impl.NetconfDeviceAlignmentServiceImpl.RESPONSE_RECEIVED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.tx.TXTemplate;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionManager;
import org.broadband_forum.obbaa.pma.NetconfNetworkFunctionAlignmentService;


/**
 * <p>
 * Netconf Network Function Alignment Service
 * </p>
 * Created by J.V.Correia (Altice Labs) on 14/02/2022.
 */
//TODO obbaa-366 complete implementation of class
public class NetconfNetworkFunctionAlignmentServiceImpl implements NetconfNetworkFunctionAlignmentService {
    private static final Logger LOGGER = Logger.getLogger(NetconfNetworkFunctionAlignmentServiceImpl.class);
    private final NetworkFunctionManager m_nm;
    private TxService m_txService;
    private ConcurrentHashMap<String, List<EditConfigRequest>> m_queue = new ConcurrentHashMap<>();
    private final NetconfConnectionManager m_ncm;

    public NetconfNetworkFunctionAlignmentServiceImpl(NetworkFunctionManager nm, NetconfConnectionManager ncm) {
        m_nm = nm;
        m_ncm = ncm;
    }

    public void init() {
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - init");
    }

    public void destroy() {
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - destroy");
    }

    public TxService getTxService() {
        return m_txService;
    }

    public void setTxService(TxService txService) {
        m_txService = txService;
    }

    @Override
    public void queueEdit(String networkFunctionName, EditConfigRequest request) {
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - queueEdit");
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                NetworkFunction networkFunction = m_nm.getNetworkFunction(networkFunctionName);
                if (networkFunction.isNeverAligned()) {
                    LOGGER.info(String.format("Network Function %s is never aligned, "
                            + "new edit requests wont be queued to be sent to the Network Function,"
                            + " please execute a full re-sync", networkFunctionName));
                    return null;
                }
                if (networkFunction.isInError()) {
                    LOGGER.info(String.format("Network Function %s is in error, "
                            + "new edit requests wont be queued to be sent to the Network Function,"
                            + " please execute a full re-sync", networkFunctionName));
                    return null;
                }
                LOGGER.debug(String.format("Queueing edit request %s for Network Function %s",
                        request.requestToString(), networkFunctionName));
                new WithSynchronousNetworkFunctionQueue<Void>() {
                    @Override
                    Void execute(String networkFunctionName, List<EditConfigRequest> networkFunctionQueue) {
                        networkFunctionQueue.add(request);
                        setAlignmentState(networkFunctionName,getAlignmentState(networkFunctionName));
                        return null;
                    }
                }.execute(networkFunctionName);
                return null;
            }
        });
    }

    @Override
    public List<EditConfigRequest> getEditQueue(String networkFunctionName) {
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - getEditQueue");
        return new WithSynchronousNetworkFunctionQueue<List<EditConfigRequest>>() {
            @Override
            List<EditConfigRequest> execute(String info, List<EditConfigRequest> networkFunctionQueue) {
                return Collections.unmodifiableList(networkFunctionQueue);
            }
        }.execute(networkFunctionName);
    }

    protected ConcurrentHashMap<String, List<EditConfigRequest>> getQueue() {
        return m_queue;
    }

    @Override
    public void align(NetworkFunction networkFunction, NetConfResponse getConfigResponse) {
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                String networkFunctionName = networkFunction.getNetworkFunctionName();
                LOGGER.debug("Trying to flush changes to the network function " + networkFunctionName);
                new WithSynchronousNetworkFunctionQueue<Void>() {
                    @Override
                    public Void execute(String networkFunctionName, List queueForNetworkFunction) {
                        Iterator<EditConfigRequest> iterator = queueForNetworkFunction.iterator();
                        while (iterator.hasNext()) {
                            EditConfigRequest request = iterator.next();
                            try {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug(String.format("Trying to send edit %s to network function %s",
                                            request.requestToString(), networkFunctionName));
                                }
                                Future<NetConfResponse> future = null;

                                //No adapters yet, devices use the adapter deviceInterface here
                                if (m_ncm.isConnected(networkFunction)) {
                                    future = m_ncm.executeNetconf(networkFunction, request);
                                } else {
                                    throw new IllegalStateException(String.format("NetworkFunction not connected %s", networkFunctionName));
                                }

                                NetConfResponse response = future.get();
                                if (response == null || !response.isOk()) {
                                    markNetworkFunctionInError(networkFunctionName, queueForNetworkFunction,
                                            request.requestToString(), response);
                                    break;
                                } else {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug(String.format("Successfully sent edit %s to device %s", request.requestToString(),
                                                networkFunctionName));
                                    }
                                    iterator.remove();
                                }
                            } catch (IllegalStateException e) {
                                LOGGER.info(String.format("Network Function %s is not connected,"
                                        + " alignment would be retried later", networkFunctionName));
                            } catch (Exception e) {
                                LOGGER.error(String.format("Error while aligning configuration with the network function %s",
                                        networkFunctionName), e);
                            }
                        }
                        if (queueForNetworkFunction.isEmpty() && !m_nm.getNetworkFunction(networkFunctionName).isInError()
                                && !m_nm.getNetworkFunction(networkFunctionName).isAlignmentUnknown()) {

                            LOGGER.info("Setting alignment state of nf " + networkFunctionName + " to "
                                    + DeviceManagerNSConstants.ALIGNED);
                            m_nm.updateConfigAlignmentState(networkFunctionName,
                                    DeviceManagerNSConstants.ALIGNED);
                        }

                        return null;
                    }
                }.execute(networkFunctionName);
                return null;
            }
        });
    }

    private void markNetworkFunctionInError(String networkFunctionInfo, List queueForDevice,
                                            String request, NetConfResponse response) {
        LOGGER.error(String.format("Edit failure/timeout on network function %s,"
                        + " edit request %s , response %s, remaining requests won't be sent to the device",
                networkFunctionInfo, request, response != null ? response.responseToString() : null));

        queueForDevice.clear();
        //TODO obbaa-366 missing error details
        m_nm.updateConfigAlignmentState(networkFunctionInfo,String.format(IN_ERROR + ", %s", getErrorDetails(request, response)));
    }

    private String getErrorDetails(String request, NetConfResponse errorResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_SENT);
        sb.append(request);
        sb.append(",").append(System.lineSeparator());
        sb.append(RESPONSE_RECEIVED);
        if (errorResponse == null) {
            sb.append(REQUEST_TIMED_OUT);
        } else {
            sb.append(errorResponse.responseToString());
        }
        return sb.toString();
    }

    private Pair<AbstractNetconfRequest, Future<NetConfResponse>> sendForceAlignToNF(NetworkFunction networkFunction,
                                                                                      NetConfResponse getConfigResponse)
                                                                throws NetconfMessageBuilderException, ExecutionException {
        if (m_ncm.isConnected(networkFunction)) {
            CopyConfigRequest ccRequest = new CopyConfigRequest();
            EditConfigElement config = new EditConfigElement();
            config.setConfigElementContents(getConfigResponse.getDataContent());
            ccRequest.setSourceConfigElement(config.getXmlElement());
            ccRequest.setTargetRunning();
            Future<NetConfResponse> responseFuture = m_ncm.executeNetconf(networkFunction,
                    ccRequest);
            return new Pair<>(ccRequest, responseFuture);
        } else {
            throw new IllegalStateException(String.format("Network Function not connected %s",
                    networkFunction.getNetworkFunctionName()));
        }
    }

    @Override
    public void forceAlign(NetworkFunction networkFunction, NetConfResponse getConfigResponse) {
        String networkFunctionName = networkFunction.getNetworkFunctionName();
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - forceAlign " + networkFunctionName);
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                new WithSynchronousNetworkFunctionQueue<Void>() {
                    @Override
                    Void execute(String info, List<EditConfigRequest> networkFunctionQueue) throws RuntimeException {
                        try {
                            networkFunctionQueue.clear();

                            Pair<AbstractNetconfRequest, Future<NetConfResponse>> copyConfigFuture =
                                    sendForceAlignToNF(networkFunction, getConfigResponse);
                            NetConfResponse response = copyConfigFuture.getSecond().get();

                            if (response == null || !response.isOk()) {
                                LOGGER.info("Marking Network Function " + networkFunctionName + " in error");
                                markNetworkFunctionInError(info,
                                        networkFunctionQueue,
                                        copyConfigFuture.getFirst().requestToString(),
                                        response);
                            } else {
                                LOGGER.info("Setting network function " + networkFunctionName + " to "
                                        + DeviceManagerNSConstants.ALIGNED);
                                m_nm.updateConfigAlignmentState(networkFunctionName,
                                        DeviceManagerNSConstants.ALIGNED);
                            }

                        } catch (IllegalStateException e) {
                            LOGGER.info(String.format("Network Function %s is not connected, "
                                            + "force-align would be retried", networkFunctionName));
                        } catch (ExecutionException | InterruptedException | NetconfMessageBuilderException e) {
                            throw new RuntimeException(e);
                        }

                        return null;
                    }
                }.execute(networkFunctionName);
                return null;
            }
        });
    }

    @Override
    public String getAlignmentState(String networkFunctionName) {
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - getAlignmentState");
        List<EditConfigRequest> queue = getEditQueue(networkFunctionName);
        int numOfEdits = queue.size();
        if (numOfEdits == 0) {
            return ALIGNED;
        }
        return String.format("%s Edits Pending", numOfEdits);
    }

    @Override
    public void networkFunctionAdded(String networkFunctionName) {
        LOGGER.info("NetconfNetworkFunctionAlignmentServiceImpl - networkFunctionAdded");
        m_queue.put(networkFunctionName,new ArrayList<>());
    }

    @Override
    public void networkFunctionRemoved(String networkFunctionName) {
        LOGGER.info(("NetconfNetworkFunctionAlignmentServiceImpl - networkFunctionRemoved"));
        m_queue.remove(networkFunctionName);
    }

    public void alignAllNetworkFunctions() {
        for (Map.Entry<String,List<EditConfigRequest>> entry : m_queue.entrySet()) {
            align(m_nm.getNetworkFunction(entry.getKey()),null);
        }
    }

    private List<EditConfigRequest> getQueueForNetworkFunction(String networkFunctionName) {
        List<EditConfigRequest> editConfigRequests = m_queue.get(networkFunctionName);
        if (editConfigRequests == null) {
            editConfigRequests = new ArrayList<>();
            m_queue.putIfAbsent(networkFunctionName, editConfigRequests);
        }
        return editConfigRequests;
    }

    private void setAlignmentState(String networkFunctionName, String verdict) {
        m_nm.updateConfigAlignmentState(networkFunctionName,verdict);
    }

    private abstract class WithSynchronousNetworkFunctionQueue<T> {
        abstract T execute(String networkFunctionName, List<EditConfigRequest> networkFunctionQueue);

        T execute(String networkFunctionName) {
            List<EditConfigRequest> queue = getQueueForNetworkFunction(networkFunctionName);
            T result;
            synchronized (queue) {
                result = execute(networkFunctionName,queue);
            }
            return result;
        }
    }
}
