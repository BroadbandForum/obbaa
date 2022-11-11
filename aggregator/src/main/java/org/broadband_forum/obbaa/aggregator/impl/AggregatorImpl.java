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

package org.broadband_forum.obbaa.aggregator.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.api.NetworkFunctionConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.NotificationProcessor;
import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl.AggregatorRpcMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl.AggregatorUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.broadband_forum.obbaa.aggregator.processor.PmaAdapter;
import org.broadband_forum.obbaa.aggregator.registrant.api.NotificationProcessorManager;
import org.broadband_forum.obbaa.aggregator.registrant.api.RequestProcessorManager;
import org.broadband_forum.obbaa.aggregator.registrant.impl.NotificationProcessorManagerImpl;
import org.broadband_forum.obbaa.aggregator.registrant.impl.RequestProcessorManagerImpl;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class AggregatorImpl implements Aggregator {
    private RequestProcessorManager m_requestProcessorManager;
    private NotificationProcessorManager m_notificationProcessorManager;
    private DeviceManagementProcessor m_deviceManagerAdapter;

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorImpl.class);
    private static final String COMMON_REQUEST_TYPE = "System-global";
    private static final String OPERATION_NOT_SUPPORT = "Does not support the operation.";

    /**
     * Aggregator implementation.
     */
    public AggregatorImpl() {
        m_requestProcessorManager = new RequestProcessorManagerImpl();
        m_notificationProcessorManager = new NotificationProcessorManagerImpl();
    }

    @Override
    public String dispatchRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        LOGGER.debug("Aggregator dispatch request:\n {}", netconfRequest);
        AggregatorRpcMessage aggregatorRpcMessage = buildAggregatorRpcMessage(netconfRequest);

        String response;
        if (aggregatorRpcMessage.isNetworkManagerMessage() && !aggregatorRpcMessage.isNetworkFunctionStateMessage()) {
            response = dispatchNetworkManageRequest(clientInfo, aggregatorRpcMessage);
            LOGGER.debug("Network manager request response:\n {}", response);
        } else {
            response = dispatchGlobalRequest(clientInfo, aggregatorRpcMessage);
            LOGGER.info("Global request response:\n {}", response);
        }

        return response;
    }

    @Override
    public void publishNotification(Notification notification) throws DispatchException {
        LOGGER.debug("Publish global notification:\n {}", notification.notificationToString());
        Set<NotificationProcessor> notificationProcessors = getNotificationProcessorManager().getProcessors();

        for (NotificationProcessor notificationProcessor : notificationProcessors) {
            LOGGER.info("Processor: {}", notificationProcessor);
            notificationProcessor.publishNotification(notification);
        }
    }

    @Override
    public void publishNotification(String deviceName, Notification notification) throws DispatchException {
        LOGGER.debug("Publish device notification:\n {} \n {}", deviceName, notification.notificationToString());

        Notification packagedNotification = null;
        try {
            packagedNotification = AggregatorUtils.packageNotification(deviceName, notification);
            LOGGER.debug("Packaged message:\n {}", packagedNotification.notificationToString());
            publishNotification(packagedNotification);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Failed to build Notifcation", e);
        }
    }

    @Override
    public void addProcessor(Set<ModuleIdentifier> moduleIdentifiers, GlobalRequestProcessor globalRequestProcessor)
            throws DispatchException {
        getRequestProcessorManager().addProcessor(moduleIdentifiers, globalRequestProcessor);
    }

    @Override
    public void addProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                             DeviceConfigProcessor deviceConfigProcessor) throws DispatchException {
        getRequestProcessorManager().addProcessor(deviceType, moduleIdentifiers, deviceConfigProcessor);
    }

    @Override
    public void addProcessor(NotificationProcessor notificationProcessor) throws DispatchException {
        getNotificationProcessorManager().addProcessor(notificationProcessor);
    }

    @Override
    public void addNFCProcessor(Set<ModuleIdentifier> moduleIdentifiers, NetworkFunctionConfigProcessor networkFunctionConfigProcessor)
            throws DispatchException {
        getRequestProcessorManager().addNFCProcessor(moduleIdentifiers,networkFunctionConfigProcessor);
    }

    @Override
    public void removeProcessor(GlobalRequestProcessor globalRequestProcessor) throws DispatchException {
        getRequestProcessorManager().removeProcessor(globalRequestProcessor);
    }

    @Override
    public void removeProcessor(DeviceConfigProcessor deviceConfigProcessor) throws DispatchException {
        getRequestProcessorManager().removeProcessor(deviceConfigProcessor);
    }

    @Override
    public void removeProcessor(NotificationProcessor notificationProcessor) throws DispatchException {
        getNotificationProcessorManager().removeProcessor(notificationProcessor);
    }

    @Override
    public void removeProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                                DeviceConfigProcessor deviceConfigProcessor) throws DispatchException {
        getRequestProcessorManager().removeProcessor(deviceType, moduleIdentifiers, deviceConfigProcessor);
    }

    @Override
    public Set<String> getSystemCapabilities() {
        Set<String> capabilities = new HashSet<>();

        //Basic capability
        capabilities.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        capabilities.add(NetconfResources.NETCONF_BASE_CAP_1_1);

        //Processor capability
        capabilities.addAll(getRequestProcessorManager().getAllProcessorsCapability());
        return capabilities;
    }

    @Override
    public void registerDeviceManager(DeviceManagementProcessor deviceManagementProcessor) {
        m_deviceManagerAdapter = deviceManagementProcessor;
    }

    @Override
    public void unregisterDeviceManager() {
        m_deviceManagerAdapter = null;
    }

    @Override
    public Set<ProcessorCapability> getProcessorCapabilities() {
        //Common processor
        Set<ModuleIdentifier> moduleIdentifiers = NetworkManagerRpc.getNetworkManagerModuleIdentifiers();
        moduleIdentifiers.addAll(getRequestProcessorManager().getCommonModuleIdentifiers());

        //Device processor
        Set<ProcessorCapability> processorCapabilities = getRequestProcessorManager().getDeviceProcessorCapabilities();

        processorCapabilities.add(new ProcessorCapabilityImpl(COMMON_REQUEST_TYPE, moduleIdentifiers));

        return processorCapabilities;
    }

    @Override
    public Set<ModuleIdentifier> getModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = getRequestProcessorManager().getAllModuleIdentifiers();
        moduleIdentifiers.addAll(NetworkManagerRpc.getNetworkManagerModuleIdentifiers());
        return moduleIdentifiers;
    }

    /**
     * Dispatch request of network-manager YANG model.
     *
     * @param clientInfo           Client Info
     * @param aggregatorRpcMessage Request
     * @return Response
     * @throws DispatchException Exception
     */
    private String dispatchNetworkManageRequest(NetconfClientInfo clientInfo, AggregatorRpcMessage aggregatorRpcMessage)
            throws DispatchException {
        String originalMessage = aggregatorRpcMessage.getOriginalMessage();
        NetworkManagerRpc networkManagerRpc = new NetworkManagerRpc(originalMessage,
                aggregatorRpcMessage.getNetconfRpcMessage(), aggregatorRpcMessage.getOnlyOneTopPayload());

        // Device configuration is mainly used for service configuration like L2.
        if (networkManagerRpc.isDeviceConfigRequest()) {
            return dispatchDeviceConfigRequest(networkManagerRpc);
        }
        if (networkManagerRpc.isNetworkFunctionConfigRequest()) {
            return dispatchNetworkFunctionConfigRequest(networkManagerRpc);
        }

        // Device management is mainly used for adding and deleting devices and other actions.
        return getDeviceManagerAdapter().processRequest(clientInfo, aggregatorRpcMessage.getOriginalMessage());
    }

    /**
     * Get processor of network-manager YANG request.
     *
     * @param singleDeviceRequest Request of mounted to one device
     * @return Processor
     */
    private DeviceConfigProcessor getNetworkManagerProcessor(SingleDeviceRequest singleDeviceRequest) {
        String mountedXmlns = singleDeviceRequest.getNamespace();
        DeviceConfigProcessor deviceConfigProcessor = m_requestProcessorManager
                .getProcessor(singleDeviceRequest.getDeviceType(), mountedXmlns);
        if (deviceConfigProcessor == null) {
            return getRequestProcessorManager().getAllDeviceConfigProcessors().stream()
                    .filter(processor -> processor.getClass().getSimpleName()
                            .startsWith(PmaAdapter.class.getSimpleName())).findFirst().get();
        }
        return deviceConfigProcessor;
    }

    private NetworkFunctionConfigProcessor getNetworkManagerProcessor(SingleNetworkFunctionRequest singleNetworkFunctionRequest) {
        String mountedXmlns = singleNetworkFunctionRequest.getNamespace();
        NetworkFunctionConfigProcessor networkFunctionConfigProcessor = m_requestProcessorManager
                .getNFCProcessor(mountedXmlns);
        if (networkFunctionConfigProcessor == null) {
            return getRequestProcessorManager().getAllNFConfigProcessors().stream()
                    .filter(processor -> processor.getClass().getSimpleName()
                            .startsWith(PmaAdapter.class.getSimpleName())).findFirst().get();
        }
        return networkFunctionConfigProcessor;
    }

    /**
     * Get processor of common request like YANG library.
     *
     * @param xmlns YANG namespace
     * @return Processor
     */
    private GlobalRequestProcessor getCommonRequestProcessor(String xmlns) {
        return getRequestProcessorManager().getProcessor(xmlns);
    }

    /**
     * Dispatch common request.
     *
     * @param netconfClientInfo    Client Info
     * @param aggregatorRpcMessage Request
     * @return Response
     * @throws DispatchException Exception
     */
    private String dispatchGlobalRequest(NetconfClientInfo netconfClientInfo, AggregatorRpcMessage aggregatorRpcMessage)
            throws DispatchException {
        GlobalRequestProcessor globalRequestProcessor = getCommonRequestProcessor(
                aggregatorRpcMessage.getOnlyOneTopXmlns());
        DispatchException.assertNull(globalRequestProcessor, OPERATION_NOT_SUPPORT);

        return globalRequestProcessor.processRequest(netconfClientInfo, aggregatorRpcMessage.getOriginalMessage());
    }

    /**
     * Dispatch request of one device.
     *
     * @param singleDeviceRequest Request
     * @return Response
     * @throws DispatchException Exception
     */
    private String dispatchSingleDeviceRequest(SingleDeviceRequest singleDeviceRequest) throws DispatchException {
        DeviceConfigProcessor deviceConfigProcessor = getNetworkManagerProcessor(singleDeviceRequest);
        DispatchException.assertNull(deviceConfigProcessor, OPERATION_NOT_SUPPORT);

        String deviceName = singleDeviceRequest.getDeviceName();
        String request = singleDeviceRequest.getMessage();
        LOGGER.debug("Dispatch device config request:\n {} {} {}", deviceConfigProcessor, deviceName, request);

        //Dispatch (can thrown exception)
        String singleDeviceResponse = deviceConfigProcessor.processRequest(deviceName, request);
        DispatchException.assertNull(singleDeviceResponse, String.format("Process error of device %s", deviceName));

        return singleDeviceResponse;
    }

    private String dispatchSingleNetworkFunctionRequest(SingleNetworkFunctionRequest singleNetworkFunctionRequest)
            throws DispatchException {
        NetworkFunctionConfigProcessor networkFunctionConfigProcessor = getNetworkManagerProcessor(singleNetworkFunctionRequest);
        DispatchException.assertNull(networkFunctionConfigProcessor, OPERATION_NOT_SUPPORT);

        String networkFunctionName = singleNetworkFunctionRequest.getNetworkFunctionName();
        String request = singleNetworkFunctionRequest.getMessage();

        LOGGER.debug("Dispatch network function config request:\n {} {} {}", networkFunctionConfigProcessor, networkFunctionName, request);

        PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.NETWORK_FUNCTION, networkFunctionName);
        String singleNetworkFunctionResponse = networkFunctionConfigProcessor.processRequest(resourceId,request);
        DispatchException.assertNull(singleNetworkFunctionResponse,
                String.format("Process error of network function %s", networkFunctionName));
        return singleNetworkFunctionResponse;
    }

    /**
     * Build requests for every device. It will delete the information of network-manager YANG model.
     *
     * @param networkManagerRpc NetworkManagerRpc
     * @return Set of single device requests
     * @throws DispatchException Exception
     */
    private Set<SingleDeviceRequest> buildSingleDeviceRequests(NetworkManagerRpc networkManagerRpc)
            throws DispatchException {
        return networkManagerRpc
                .buildSingleDeviceRequests(getDeviceManagerAdapter(), networkManagerRpc.getOriginalMessage());
    }

    private Set<SingleNetworkFunctionRequest> buildSingleNetworkFunctionRequests(NetworkManagerRpc networkManagerRpc)
            throws DispatchException {
        return networkManagerRpc.buildSingleNetworkFunctionRequests(networkManagerRpc.getOriginalMessage());
    }

    /**
     * Dispatch request of device config.
     *
     * @param networkManagerRpc Request
     * @return Response
     * @throws DispatchException Exception
     */
    private String dispatchDeviceConfigRequest(NetworkManagerRpc networkManagerRpc) throws DispatchException {
        Map<Document, String> responses = new HashMap<>();
        Set<SingleDeviceRequest> singleDeviceRequests = buildSingleDeviceRequests(networkManagerRpc);

        for (SingleDeviceRequest singleDeviceRequest : singleDeviceRequests) {
            String responseOneDevice = dispatchSingleDeviceRequest(singleDeviceRequest);
            responses.put(AggregatorUtils.stringToDocument(responseOneDevice), singleDeviceRequest.getDeviceName());
        }

        //Response
        return networkManagerRpc.packageResponse(responses);
    }

    private String dispatchNetworkFunctionConfigRequest(NetworkManagerRpc networkManagerRpc) throws DispatchException {
        Map<Document, String> responses = new HashMap<>();
        Set<SingleNetworkFunctionRequest> singleNetworkFunctionRequests = buildSingleNetworkFunctionRequests(networkManagerRpc);

        for (SingleNetworkFunctionRequest singleNetworkFunctionRequest : singleNetworkFunctionRequests) {
            String responseOneNetFunc = dispatchSingleNetworkFunctionRequest(singleNetworkFunctionRequest);
            responses.put(AggregatorUtils.stringToDocument(responseOneNetFunc), singleNetworkFunctionRequest.getNetworkFunctionName());
        }

        return networkManagerRpc.packageResponse(responses);

    }

    /**
     * Get processor manager of request.
     *
     * @return Processor
     */
    private RequestProcessorManager getRequestProcessorManager() {
        return m_requestProcessorManager;
    }

    /**
     * Get processor manager of notification.
     *
     * @return Processor
     */
    private NotificationProcessorManager getNotificationProcessorManager() {
        return m_notificationProcessorManager;
    }

    /**
     * Get processor of device manage.
     *
     * @return Device manage processor
     */
    private DeviceManagementProcessor getDeviceManagerAdapter() {
        return m_deviceManagerAdapter;
    }

    /**
     * Build AggregatorRpcMessage from request.
     *
     * @param netconfRequest Netconf request
     * @return AggregatorRpcMessage
     * @throws DispatchException Exception
     */
    private static AggregatorRpcMessage buildAggregatorRpcMessage(String netconfRequest) throws DispatchException {
        try {
            return new AggregatorRpcMessage(netconfRequest);
        } catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }
}
