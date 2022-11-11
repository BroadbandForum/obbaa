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

package org.broadband_forum.obbaa.msd.impl;

import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BAA_KUBERNETES_NAMESPACE;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBAA_VIRTUAL_NETWORK_FUNCTION;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBAA_VNF_INSTANCE_NAME;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBAA_VNF_NAME;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBAA_VNF_TYPE;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBAA_VNF_VENDOR;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBAA_VNF_VERSION;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.BBF_OBBBA_VNF_ADMIN_STATE;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.RUNNING;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.VOMCI;
import static org.broadband_forum.obbaa.msd.MicroserviceDiscoveryConstants.VPROXY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.connectors.sbi.netconf.impl.SystemPropertyUtils;
import org.broadband_forum.obbaa.msd.NetworkFunctionType;
import org.broadband_forum.obbaa.msd.OperState;
import org.broadband_forum.obbaa.msd.VirtualNetworkFunctionInstancesId;
import org.broadband_forum.obbaa.msd.VirtualNwFunctionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

@RestController
public class MicroServicesDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServicesDiscovery.class);
    String kubernatedConfigPath = "/kube/config";
    String certificationFilePath = "src/main/resources/ca_cert_file.pem";
    String namespace = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(BAA_KUBERNETES_NAMESPACE, "obbaa");

    public MicroServicesDiscovery() {
    }

    private V1PodList getAllPodDetails() {
        ApiClient client = null;
        try {
            client = Config.fromConfig(kubernatedConfigPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f = new File(certificationFilePath);
        if (f.exists()) {
            client.setVerifyingSsl(true);
            Path truststorePath = Paths.get(certificationFilePath);
            byte[] truststoreBytes = new byte[0];
            try {
                truststoreBytes = Files.readAllBytes(truststorePath);
            } catch (IOException e) {
                LOGGER.error("Failed to read certification file");
            }
            client.setSslCaCert(new ByteArrayInputStream(truststoreBytes));
            Configuration.setDefaultApiClient(client);
        } else {
            client.setVerifyingSsl(false);
            Configuration.setDefaultApiClient(client);
        }

        CoreV1Api api = new CoreV1Api();
        V1PodList list = null;
        try {
            list = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static List<V1Pod> getAllPodsList(V1PodList podList) {
        List<V1Pod> itemList = new ArrayList();
        for (V1Pod item : podList.getItems()) {
            if (!itemList.contains(item)) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    @GetMapping("/nbi/api/getNetworkFunction")
    public Set<VirtualNwFunctionId> getNetworkFunctionDetails() {
        Set<VirtualNwFunctionId> virtualNetworkFunctionInstancesIdSet = new HashSet();
        V1PodList podList = getAllPodDetails();
        List<V1Pod> itemList = getAllPodsList(podList);
        for (V1Pod item : itemList) {
            List<V1Container> podInstances = item.getSpec().getContainers();
            if (podInstances.toString().contains(BBF_OBBAA_VNF_NAME)) {
                for (V1Container podInstance : podInstances) {
                    List<V1EnvVar> envVariableList = podInstance.getEnv();
                    VirtualNwFunctionId virtualNwFunctionId = new VirtualNwFunctionId();
                    for (V1EnvVar envVar : envVariableList) {
                        if (envVar.getName() != null) {
                            if (envVar.getName().equals(BBF_OBBAA_VNF_NAME)) {
                                virtualNwFunctionId.setName(envVar.getValue());
                            } else if (envVar.getName().equals(BBF_OBBAA_VNF_TYPE)) {
                                if (envVar.getValue().contains(VPROXY)) {
                                    virtualNwFunctionId.setNetworkFunctionType(NetworkFunctionType.VPROXY);
                                } else if (envVar.getValue().contains(VOMCI)) {
                                    virtualNwFunctionId.setNetworkFunctionType(NetworkFunctionType.VOMCI);
                                } else {
                                    virtualNwFunctionId.setNetworkFunctionType(NetworkFunctionType.ACCESS_VNF);
                                }
                            } else if (envVar.getName().equals(BBF_OBBAA_VNF_VERSION)) {
                                virtualNwFunctionId.setSoftwareVersion(envVar.getValue());
                            } else if (envVar.getName().equals(BBF_OBBAA_VNF_VENDOR)) {
                                virtualNwFunctionId.setVendor(envVar.getValue());
                            }
                        }
                    }
                    virtualNetworkFunctionInstancesIdSet.add(virtualNwFunctionId);
                }
            }
        }
        return virtualNetworkFunctionInstancesIdSet;
    }

    @GetMapping("/nbi/api/getNetworkFunctionInstances")
    public Set<VirtualNetworkFunctionInstancesId> getNetworkFunctionInstancesDetails() {
        Set<VirtualNetworkFunctionInstancesId> virtualNetworkFunctionInstancesIdSet = new HashSet();
        V1PodList podList = getAllPodDetails();
        List<V1Pod> itemList = getAllPodsList(podList);
        for (V1Pod item : itemList) {
            List<V1Container> podInstances = item.getSpec().getContainers();
            if (podInstances.toString().contains(BBF_OBBAA_VNF_INSTANCE_NAME)) {
                for (V1Container podInstance : podInstances) {
                    VirtualNetworkFunctionInstancesId virtualNwFunctionInstancesId = new VirtualNetworkFunctionInstancesId();
                    List<V1EnvVar> envVariableList = podInstance.getEnv();
                    for (V1EnvVar envVar : envVariableList) {
                        if (envVar.getName() != null) {
                            if (envVar.getName().equals(BBF_OBBAA_VNF_INSTANCE_NAME)) {
                                virtualNwFunctionInstancesId.setVirtualNetworkFunctionInstanceName(envVar.getValue());
                            } else if (envVar.getName().equals(BBF_OBBAA_VNF_NAME)) {
                                virtualNwFunctionInstancesId.setVirtualNetworkFunction(envVar.getValue());
                            } else if (envVar.getName().equals(BBF_OBBBA_VNF_ADMIN_STATE)) {
                                virtualNwFunctionInstancesId.setAdminState(envVar.getValue());
                            }
                        }
                    }
                    if (item.getStatus().getPhase().equals(RUNNING)) {
                        virtualNwFunctionInstancesId.setOperState(OperState.UP);
                    } else {
                        virtualNwFunctionInstancesId.setOperState(OperState.DOWN);
                    }
                    virtualNetworkFunctionInstancesIdSet.add(virtualNwFunctionInstancesId);
                }
            }
        }
        return virtualNetworkFunctionInstancesIdSet;
    }
}
