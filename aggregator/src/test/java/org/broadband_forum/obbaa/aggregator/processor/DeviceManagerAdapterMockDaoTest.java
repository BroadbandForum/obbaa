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

package org.broadband_forum.obbaa.aggregator.processor;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NETWORK_MANAGER_SP;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.connectors.sbi.netconf.impl.NetconfConnectionManagerImpl;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManagementSubsystem;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.nm.devicemanager.impl.DeviceManagerImpl;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class DeviceManagerAdapterMockDaoTest extends AbstractValidationTestSetup {

    private static final String DIRECT_DEVICE = "directDevice";
    private static Aggregator m_aggregator;
    private static DeviceManagerAdapter m_deviceManagerAdapter;
    private static String DEVICE_NAME_A = "deviceA";
    private static String DEVICE_TYPE = "DPU";
    private static Date m_now = NetconfResources.parseDateTime("1970-01-01T00:00:00+00:00").toDate();
    @Mock
    private NetconfClientSession m_newDeviceSession;
    private Set<String> m_newDeviceCaps;
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("UT", 1);
    @Mock
    private NetconfConnectionManagerImpl m_connectionManager;
    private List<NewDeviceInfo> m_newDevices;
    @Mock
    private DeviceManager m_deviceManager;
    public static final String OBBAA_YANG = "/yangs/bbf-obbaa-network-manager.yang";
    private NetConfServerImpl m_server;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private RootModelNodeAggregatorImpl m_rootModelNodeAggregator;
    private DataStoreIntegrityServiceImpl m_integrityService;
    private DataStoreValidatorImpl m_datastoreValidator;
    private DeviceManagementSubsystem m_deviceSubsystem;
    private Device m_directDevice;
    private Device m_callhomeDevice;
    private static final String EXPECTED_DM_GET_REPLY_DIRECT = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
            "   <data>\n" +
            "      <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "         <baa-network-manager:managed-devices>\n" +
            "            <baa-network-manager:device>\n" +
            "               <baa-network-manager:device-management>\n" +
            "                  <baa-network-manager:device-connection>\n" +
            "                     <baa-network-manager:connection-model>direct</baa-network-manager:connection-model>\n" +
            "                     <baa-network-manager:password-auth>\n" +
            "                        <baa-network-manager:authentication>\n" +
            "                           <baa-network-manager:address>192.168.100.101</baa-network-manager:address>\n" +
            "                           <baa-network-manager:management-port>803</baa-network-manager:management-port>\n" +
            "                           <baa-network-manager:password>testPassword</baa-network-manager:password>\n" +
            "                           <baa-network-manager:user-name>testUser</baa-network-manager:user-name>\n" +
            "                        </baa-network-manager:authentication>\n" +
            "                     </baa-network-manager:password-auth>\n" +
            "                  </baa-network-manager:device-connection>\n" +
            "                  <device-state xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                     <configuration-alignment-state>Never Aligned</configuration-alignment-state>\n" +
            "                     <connection-state>\n" +
            "                        <connected>false</connected>\n" +
            "                        <connection-creation-time>1970-01-01T05:30:00+05:30</connection-creation-time>\n" +
            "                     </connection-state>\n" +
            "                  </device-state>\n" +
            "                  <baa-network-manager:interface-version>1.0.0</baa-network-manager:interface-version>\n" +
            "                  <baa-network-manager:push-pma-configuration-to-device>true</baa-network-manager:push-pma-configuration-to-device>\n" +
            "                  <baa-network-manager:type>DPU</baa-network-manager:type>\n" +
            "                  <baa-network-manager:vendor>testVendor</baa-network-manager:vendor>\n" +
            "               </baa-network-manager:device-management>\n" +
            "               <baa-network-manager:name>directDevice</baa-network-manager:name>\n" +
            "            </baa-network-manager:device>\n" +
            "         </baa-network-manager:managed-devices>\n" +
            "      </baa-network-manager:network-manager>\n" +
            "   </data>\n" +
            "</rpc-reply>";

    private static String REQUEST_DEVICE_ADD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
        "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <managed-devices>\n" +
            "        <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "          <name>" + DIRECT_DEVICE + "</name>\n" +
            "          <device-management>\n" +
            "            <type>DPU</type>\n" +
            "            <interface-version>1.0.0</interface-version>\n" +
            "            <vendor>testVendor</vendor>\n" +
            "            <device-connection>\n" +
            "              <connection-model>direct</connection-model>\n" +
            "              <password-auth>\n" +
            "                <authentication>\n" +
            "                  <address>192.168.100.101</address>\n" +
            "                  <management-port>803</management-port>\n" +
            "                  <user-name>testUser</user-name>\n" +
            "                  <password>testPassword</password>\n" +
            "                </authentication>\n" +
            "              </password-auth>\n" +
            "            </device-connection>\n" +
            "          </device-management>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
        "            </network-manager>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";
    private static final String CALLHOME_DEVICE = "callhomeDevice";
    private static String REQUEST_DEVICE_ADD_CALLHOME = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
        "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <managed-devices>\n" +
            "        <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "          <name>" + CALLHOME_DEVICE + "</name>\n" +
            "          <device-management>\n" +
            "            <type>DPU</type>\n" +
            "            <interface-version>1.0.0</interface-version>\n" +
            "            <vendor>testVendor</vendor>\n" +
            "            <device-connection>\n" +
            "              <connection-model>call-home</connection-model>\n" +
            "              <duid>" + CALLHOME_DEVICE + "-duid" + "</duid>\n" +
            "            </device-connection>\n" +
            "          </device-management>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
        "            </network-manager>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_DELETE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <delete-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
        "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <managed-devices>\n" +
            "        <device>\n" +
            "          <name>" + DIRECT_DEVICE + "</name>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
        "            </network-manager>\n" +
            "    </config>\n" +
            "  </delete-config>\n" +
            "</rpc>";
    private static String REQUEST_DEVICE_DELETE_CONFIG_CALLHOME = REQUEST_DEVICE_DELETE.replaceAll(DIRECT_DEVICE,
            CALLHOME_DEVICE);
    private static String REQUEST_DEVICE_DELETE_CALLHOME = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
        "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <managed-devices>\n" +
            "        <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\">\n" +
            "          <name>" + CALLHOME_DEVICE + "</name>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
        "            </network-manager>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_DELETE_DIRECT = REQUEST_DEVICE_DELETE_CALLHOME.replaceAll(CALLHOME_DEVICE, DIRECT_DEVICE);
    private static String REQUEST_DEVICE_GET =
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "<filter type=\"subtree\">\n" +
                "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                    "<managed-devices>\n" +
                    "  <device>\n" +
                    "    <name>" + DIRECT_DEVICE + "</name>\n" +
                    "  </device>\n" +
                    "</managed-devices>\n" +
                "            </network-manager>\n" +
                    "</filter>\n" +
                    "</get>\n" +
                    "</rpc>";

    private static String REQUEST_GET_NEWDEVICES =
            "<rpc message-id=\"1018\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "        <filter type=\"subtree\">\n" +
                "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                    "            <new-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                    "                <new-device/>\n" +
                    "            </new-devices>\n" +
                "            </network-manager>\n" +
                    "        </filter>\n" +
                    "    </get>\n" +
                    "</rpc>";

    private static String REQUEST_DEVICE_GET_CONFIG =
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "<get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <source>\n" +
                    "    <running/>\n" +
                    "  </source>\n" +
                    "<filter type=\"subtree\">\n" +
                "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                    "<managed-devices>\n" +
                    "  <device>\n" +
                    "    <name>" + DIRECT_DEVICE + "</name>\n" +
                    "  </device>\n" +
                    "</managed-devices>\n" +
                "            </network-manager>\n" +
                    "</filter>\n" +
                    "</get-config>\n" +
                    "</rpc>";

    private static String REQUEST_DEVICE_GET_CONFIG_CALLHOME = REQUEST_DEVICE_GET_CONFIG.replaceAll(DIRECT_DEVICE,
            CALLHOME_DEVICE);

    private static final String EXPECTED_DM_GET_CONFIG_REPLY_DIRECT = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
            "   <data>\n" +
            "      <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "         <baa-network-manager:managed-devices>\n" +
            "            <baa-network-manager:device>\n" +
            "               <baa-network-manager:device-management>\n" +
            "                  <baa-network-manager:device-connection>\n" +
            "                     <baa-network-manager:connection-model>direct</baa-network-manager:connection-model>\n" +
            "                     <baa-network-manager:password-auth>\n" +
            "                        <baa-network-manager:authentication>\n" +
            "                           <baa-network-manager:address>192.168.100.101</baa-network-manager:address>\n" +
            "                           <baa-network-manager:management-port>803</baa-network-manager:management-port>\n" +
            "                           <baa-network-manager:password>testPassword</baa-network-manager:password>\n" +
            "                           <baa-network-manager:user-name>testUser</baa-network-manager:user-name>\n" +
            "                        </baa-network-manager:authentication>\n" +
            "                     </baa-network-manager:password-auth>\n" +
            "                  </baa-network-manager:device-connection>\n" +
            "                  <baa-network-manager:interface-version>1.0.0</baa-network-manager:interface-version>\n" +
            "                  <baa-network-manager:push-pma-configuration-to-device>true</baa-network-manager:push-pma-configuration-to-device>\n" +
            "                  <baa-network-manager:type>DPU</baa-network-manager:type>\n" +
            "                  <baa-network-manager:vendor>testVendor</baa-network-manager:vendor>\n" +
            "               </baa-network-manager:device-management>\n" +
            "               <baa-network-manager:name>directDevice</baa-network-manager:name>\n" +
            "            </baa-network-manager:device>\n" +
            "         </baa-network-manager:managed-devices>\n" +
            "      </baa-network-manager:network-manager>\n" +
            "   </data>\n" +
            "</rpc-reply>";

    private static final String EXPECTED_DM_GET_REPLY_CALLHOME = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
            "   <data>\n" +
            "      <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "         <baa-network-manager:managed-devices>\n" +
            "            <baa-network-manager:device>\n" +
            "               <baa-network-manager:device-management>\n" +
            "                  <baa-network-manager:device-connection>\n" +
            "                     <baa-network-manager:connection-model>call-home</baa-network-manager:connection-model>\n" +
            "                     <baa-network-manager:duid>callhomeDevice-duid</baa-network-manager:duid>\n" +
            "                  </baa-network-manager:device-connection>\n" +
            "                  <device-state xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                     <configuration-alignment-state>Never Aligned</configuration-alignment-state>\n" +
            "                     <connection-state>\n" +
            "                        <connected>false</connected>\n" +
            "                        <connection-creation-time>1970-01-01T05:30:00+05:30</connection-creation-time>\n" +
            "                     </connection-state>\n" +
            "                  </device-state>\n" +
            "                  <baa-network-manager:interface-version>1.0.0</baa-network-manager:interface-version>\n" +
            "                  <baa-network-manager:push-pma-configuration-to-device>true</baa-network-manager:push-pma-configuration-to-device>\n" +
            "                  <baa-network-manager:type>DPU</baa-network-manager:type>\n" +
            "                  <baa-network-manager:vendor>testVendor</baa-network-manager:vendor>\n" +
            "               </baa-network-manager:device-management>\n" +
            "               <baa-network-manager:name>callhomeDevice</baa-network-manager:name>\n" +
            "            </baa-network-manager:device>\n" +
            "         </baa-network-manager:managed-devices>\n" +
            "      </baa-network-manager:network-manager>\n" +
            "   </data>\n" +
            "</rpc-reply>";

    private static final String EXPECTED_DM_GET_REPLY_WITH_NEWDEVICES = "<rpc-reply message-id=\"1018\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <data>\n" +
        "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "    <new-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <new-device>\n" +
            "        <duid>duid-1234</duid>\n" +
            "        <device-capability>caps1</device-capability>\n" +
            "        <device-capability>caps2</device-capability>\n" +
            "      </new-device>\n" +
            "    </new-devices>\n" +
        "            </network-manager>\n" +
            "  </data>\n" +
            "</rpc-reply>\n";

    private static final String EXPECTED_DM_GET_CONFIG_REPLY_CALLHOME = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
            "   <data>\n" +
            "      <baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "         <baa-network-manager:managed-devices>\n" +
            "            <baa-network-manager:device>\n" +
            "               <baa-network-manager:device-management>\n" +
            "                  <baa-network-manager:device-connection>\n" +
            "                     <baa-network-manager:connection-model>call-home</baa-network-manager:connection-model>\n" +
            "                     <baa-network-manager:duid>callhomeDevice-duid</baa-network-manager:duid>\n" +
            "                  </baa-network-manager:device-connection>\n" +
            "                  <baa-network-manager:interface-version>1.0.0</baa-network-manager:interface-version>\n" +
            "                  <baa-network-manager:push-pma-configuration-to-device>true</baa-network-manager:push-pma-configuration-to-device>\n" +
            "                  <baa-network-manager:type>DPU</baa-network-manager:type>\n" +
            "                  <baa-network-manager:vendor>testVendor</baa-network-manager:vendor>\n" +
            "               </baa-network-manager:device-management>\n" +
            "               <baa-network-manager:name>callhomeDevice</baa-network-manager:name>\n" +
            "            </baa-network-manager:device>\n" +
            "         </baa-network-manager:managed-devices>\n" +
            "      </baa-network-manager:network-manager>\n" +
            "   </data>\n" +
            "</rpc-reply>";
    private static String REQUEST_DEVICE_GET_CALLHOME = REQUEST_DEVICE_GET.replaceAll(DIRECT_DEVICE, CALLHOME_DEVICE);
    @Mock
    private AdapterManager m_adapterMgr;
    @Mock
    private DeviceInterface m_devInterface;
    @Mock
    private AdapterContext m_context;

    @Before
    public void setup() throws SchemaBuildException, ModelNodeFactoryException {
        MockitoAnnotations.initMocks(this);
        m_aggregator = new AggregatorImpl();
        m_deviceManagerAdapter = new DeviceManagerAdapter(m_aggregator);
        String yangFilePath = getClass().getResource(OBBAA_YANG).getPath();
        m_directDevice = new Device();
        m_callhomeDevice = new Device();

        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("network-manager", Arrays.asList(
                TestUtil.getByteSource("/yangs/ietf-yang-types.yang"),
                TestUtil.getByteSource("/yangs/ietf-inet-types.yang"),
                TestUtil.getByteSource("/yangs/ietf-netconf-acm.yang"),
                TestUtil.getByteSource("/yangs/ietf-crypto-types.yang"),
                TestUtil.getByteSource("/yangs/ietf-yang-library@2016-06-21.yang"),
                TestUtil.getByteSource("/yangs/ietf-yang-schema-mount.yang"),
                TestUtil.getByteSource("/yangs/ietf-tcp-common.yang"),
                TestUtil.getByteSource("/yangs/ietf-tcp-client.yang"),
                TestUtil.getByteSource("/yangs/ietf-tcp-server.yang"),
                TestUtil.getByteSource("/yangs/ietf-datastores@2017-08-17.yang"),
                TestUtil.getByteSource("/yangs/bbf-device-types.yang"),
                TestUtil.getByteSource("/yangs/bbf-network-function-types.yang"),
                TestUtil.getByteSource("/yangs/bbf-xpon-types.yang"),
                TestUtil.getByteSource("/yangs/bbf-yang-types.yang"),
                TestUtil.getByteSource("/yangs/bbf-vomci-types.yang"),
                TestUtil.getByteSource("/yangs/bbf-grpc-client.yang"),
                TestUtil.getByteSource("/yangs/bbf-kafka-agent.yang"),
                TestUtil.getByteSource("/yangs/bbf-obbaa-network-manager.yang"),
                TestUtil.getByteSource("/yangs/bbf-network-function-client.yang"),
                TestUtil.getByteSource("/yangs/bbf-network-function-server.yang")), Collections.emptySet(), Collections.emptyMap());
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        m_server = new NetConfServerImpl(m_schemaRegistry);

        m_deviceManager = mock(DeviceManagerImpl.class);
        m_deviceSubsystem = new DeviceManagementSubsystem(m_schemaRegistry, m_adapterMgr);
        m_deviceSubsystem.setDeviceManager(m_deviceManager);
        m_newDeviceCaps = new LinkedHashSet<>();
        m_newDeviceCaps.add("caps1");
        m_newDeviceCaps.add("caps2");
        when(m_newDeviceSession.getServerCapabilities()).thenReturn(m_newDeviceCaps);
        m_newDevices = new ArrayList<>();
        m_newDevices.add(new NewDeviceInfo("duid-1234", m_newDeviceSession));
        when(m_connectionManager.getNewDevices()).thenReturn(m_newDevices);
        m_deviceSubsystem.setConnectionManager(m_connectionManager);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry , m_subSystemRegistry);
        m_integrityService = new DataStoreIntegrityServiceImpl(m_server);
        m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService, m_expValidator);
        List<String> yangs = new ArrayList<>(Arrays.asList("/yangs/ietf/ietf-restconf.yang",
                "/yangs/ietf/ietf-inet-types.yang",
                "/yangs/ietf/ietf-yang-types.yang",
                "/yangs/ietf/ietf-yang-schema-mount.yang",
                "/yangs/ietf/ietf-yang-library@2016-06-21.yang",
                "/yangs/bbf-network-function-client.yang",
                "/yangs/bbf-network-function-server.yang",
                "/yangs/ietf-inet-types.yang",
                "/yangs/ietf-netconf-acm.yang",
                "/yangs/ietf-crypto-types.yang",
                "/yangs/ietf-yang-library@2016-06-21.yang",
                "/yangs/ietf-yang-schema-mount.yang",
                "/yangs/ietf-tcp-common.yang",
                "/yangs/ietf-tcp-client.yang",
                "/yangs/ietf-tcp-server.yang",
                "/yangs/ietf-datastores@2017-08-17.yang",
                "/yangs/bbf-device-types.yang",
                "/yangs/bbf-network-function-types.yang",
                "/yangs/bbf-xpon-types.yang",
                "/yangs/bbf-yang-types.yang",
                "/yangs/bbf-vomci-types.yang",
                "/yangs/bbf-grpc-client.yang",
                "/yangs/bbf-kafka-agent.yang"
        ));
        YangUtils.deployInMemoryHelpers(yangs, m_deviceSubsystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm, null, null, null);

        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(NETWORK_MANAGER_SP);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(NETWORK_MANAGER_SP, containerHelper);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_dataStore.setValidator(m_datastoreValidator);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(m_dataStore);
        m_deviceManagerAdapter.setDmNetconfServer(m_server);
        m_directDevice.setDeviceName(DIRECT_DEVICE);
        m_callhomeDevice.setDeviceName(CALLHOME_DEVICE);
        ConnectionState connectionState = new ConnectionState();
        when(m_adapterMgr.getAdapterContext(any())).thenReturn(m_context);
        when(m_context.getDeviceInterface()).thenReturn(m_devInterface);
        when(m_devInterface.getConnectionState(any())).thenReturn(connectionState);
    }

    @Test
    @Ignore
    public void processRequestDmGetNewDevices() throws Exception {
        String response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_ADD);
        NetConfResponse netconfResponse = getNetconfResponse(stringToDocument(response));
        assertTrue(netconfResponse.isOk());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_GET_NEWDEVICES);
        netconfResponse = getNetconfResponse(stringToDocument(response));
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_REPLY_WITH_NEWDEVICES).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_DELETE_DIRECT);
        assertFalse(response.isEmpty());
    }

    @Test
    @Ignore
    public void processRequestDmTest() throws Exception {
        String response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_ADD);
        NetConfResponse netconfResponse = getNetconfResponse(stringToDocument(response));
        assertTrue(netconfResponse.isOk());

        when(m_deviceManager.getDevice(DIRECT_DEVICE)).thenReturn(getDevice("directDevice", "DPU", "testVendor", "1.0.0"));

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET);
        netconfResponse = getNetconfResponse(stringToDocument(response));
        assertFalse(response.isEmpty());
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_REPLY_DIRECT).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET_CONFIG);
        assertFalse(response.isEmpty());
        netconfResponse = getNetconfResponse(stringToDocument(response));
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_CONFIG_REPLY_DIRECT).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_DELETE_DIRECT);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_DELETE);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET);
        assertFalse(response.isEmpty());
    }

    @Test
    @Ignore
    public void processRequestDmTest_WithCallHomeDevices() throws Exception {

        String response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_ADD_CALLHOME);
        NetConfResponse netconfResponse = getNetconfResponse(stringToDocument(response));
        Assert.assertTrue(netconfResponse.isOk());

        when(m_deviceManager.getDevice(CALLHOME_DEVICE)).thenReturn(getDevice("callhomeDevice", "DPU", "testVendor", "1.0.0"));

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET_CALLHOME);
        netconfResponse = getNetconfResponse(stringToDocument(response));
        assertFalse(response.isEmpty());
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_REPLY_CALLHOME).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET_CONFIG_CALLHOME);
        assertFalse(response.isEmpty());
        netconfResponse = getNetconfResponse(stringToDocument(response));
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_CONFIG_REPLY_CALLHOME).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_DELETE_CONFIG_CALLHOME);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_DELETE_CALLHOME);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET_CALLHOME);
        assertFalse(response.isEmpty());

    }
    @Test
    public void getDeviceTypeByDeviceName() throws Exception {
        DeviceAdapterInfo deviceAdapterInfo = new DeviceAdapterInfo(DEVICE_TYPE, null, null, null);
        m_deviceManagerAdapter.updateDeviceAdptInfo(DEVICE_NAME_A, deviceAdapterInfo);
        String response = m_deviceManagerAdapter.getDeviceTypeByDeviceName(DEVICE_NAME_A);
        assertEquals(DEVICE_TYPE, response);

        m_deviceManagerAdapter.removeDeviceAdptInfo(DEVICE_NAME_A);
    }


    @Test
    public void getDeviceFilterMsgTest() throws Exception {
        String response = m_deviceManagerAdapter.processRequest(m_clientInfo, REQUEST_DEVICE_GET);
        assertFalse(response.isEmpty());
    }

    private Device getDevice(String name, String type, String vendor, String interfaceVersion) {
        DeviceState deviceState = new DeviceState();
        deviceState.setDeviceNodeId("/container=network-manager/container=managed-devices/container=device/name=" + name + "\"");
        deviceState.setConfigAlignmentState("Never Aligned");
        DeviceMgmt deviceManagement = new DeviceMgmt();
        deviceManagement.setDeviceType(type);
        deviceManagement.setDeviceInterfaceVersion(interfaceVersion);
        deviceManagement.setDeviceVendor(vendor);
        deviceManagement.setParentId("/container=network-manager/container=managed-devices/container=device/name=" + name + "\"");
        deviceManagement.setSchemaPath("urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",network-manager,urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",managed-devices,urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",device," +
                "urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",device-management,");
        deviceManagement.setDeviceState(deviceState);
        Device device = new Device();
        device.setParentId("/container=network-manager/container=managed-devices");
        device.setDeviceName(name);
        device.setSchemaPath("urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",network-manager,urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",managed-devices,urn:bbf:yang:obbaa:network-manager,"+ DeviceManagerNSConstants.REVISION + ",device,");
        device.setDeviceManagement(deviceManagement);
        return device;
    }
}
