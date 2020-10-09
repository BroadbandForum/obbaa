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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_MANAGEMENT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.INTERFACE_VERSION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MODEL;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.TYPE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.VENDOR;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity.Error;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag.INVALID_VALUE;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType.Application;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;

public class DeviceConfigInterceptorTest {

    public static final String ADAPTER_NOR_DEPLOYED_ERROR = "Invalid adapter details for device. DeviceAdapterId{m_type='TYPE2," +
            " m_interfaceVersion='2.0, m_model='MODEL2, m_vendor='VENDOR2} not deployed";
    DeviceConfigInterceptor m_deviceConfigInterceptor;
    @Mock
    AdapterManager m_adapterManager;
    @Mock
    HelperDrivenModelNode m_modelNode;
    @Mock
    DeviceAdapter m_deviceAdapter;

    private static final String ERROR_ON_ROLLBACK = "error-on-rollback";
    private static final QName TYPE_QNAME = QName.create(NS, DeviceManagerNSConstants.REVISION, TYPE);
    private static final QName INTERFACE_VERSION_QNAME = QName.create(NS, DeviceManagerNSConstants.REVISION, INTERFACE_VERSION);
    private static final QName VENDOR_QNAME = QName.create(NS, DeviceManagerNSConstants.REVISION, VENDOR);
    private static final QName MODEL_QNAME = QName.create(NS, DeviceManagerNSConstants.REVISION, MODEL);
    private static final QName DEVICE_MANAGEMENT_QNAME = QName.create(NS, DeviceManagerNSConstants.REVISION, DEVICE_MANAGEMENT);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_deviceConfigInterceptor = new DeviceConfigInterceptor(m_adapterManager);
        when(m_adapterManager.getDeviceAdapter(new DeviceAdapterId("TYPE1", "1.0", "MODEL1", "VENDOR1" )))
                .thenReturn(m_deviceAdapter);
    }

    @Test
    public void testCreationOfDeviceWithAvailableAdapter() {
        ArgumentCaptor<DeviceAdapterId> adapter = ArgumentCaptor.forClass(DeviceAdapterId.class);
        EditContainmentNode deviceEditNode = new EditContainmentNode(DEVICE_MANAGEMENT_QNAME, EditConfigOperations.MERGE);
        deviceEditNode.addLeafChangeNode(TYPE_QNAME, new GenericConfigAttribute(TYPE, NS, "TYPE1"));
        deviceEditNode.addLeafChangeNode(INTERFACE_VERSION_QNAME, new GenericConfigAttribute(INTERFACE_VERSION, NS, "1.0"));
        deviceEditNode.addLeafChangeNode(VENDOR_QNAME, new GenericConfigAttribute(VENDOR, NS, "VENDOR1"));
        deviceEditNode.addLeafChangeNode(MODEL_QNAME, new GenericConfigAttribute(MODEL, NS, "MODEL1"));
        EditContext editContext = new EditContext(deviceEditNode, null, ERROR_ON_ROLLBACK, null);
        m_deviceConfigInterceptor.interceptEditConfig(m_modelNode, editContext);
        verify(m_adapterManager).getDeviceAdapter(adapter.capture());
    }

    @Test
    public void testWhenJustModificationOfProperties() {
        EditContainmentNode deviceEditNode = new EditContainmentNode(DEVICE_MANAGEMENT_QNAME, EditConfigOperations.MERGE);
        deviceEditNode.addLeafChangeNode(TYPE_QNAME, new GenericConfigAttribute(TYPE, NS, "TYPE1"));
        deviceEditNode.addLeafChangeNode(INTERFACE_VERSION_QNAME, new GenericConfigAttribute(INTERFACE_VERSION, NS, "1.0"));
        EditContext editContext = new EditContext(deviceEditNode, null, ERROR_ON_ROLLBACK, null);
        m_deviceConfigInterceptor.interceptEditConfig(m_modelNode, editContext);
        verifyZeroInteractions(m_adapterManager);
    }

    @Test
    public void testCreationOfDeviceWithNoAvailableAdapter() {
        ArgumentCaptor<DeviceAdapterId> adapter = ArgumentCaptor.forClass(DeviceAdapterId.class);
        EditContainmentNode deviceEditNode = new EditContainmentNode(DEVICE_MANAGEMENT_QNAME, EditConfigOperations.MERGE);
        deviceEditNode.addLeafChangeNode(TYPE_QNAME, new GenericConfigAttribute(TYPE, NS, "TYPE2"));
        deviceEditNode.addLeafChangeNode(INTERFACE_VERSION_QNAME, new GenericConfigAttribute(INTERFACE_VERSION, NS, "2.0"));
        deviceEditNode.addLeafChangeNode(VENDOR_QNAME, new GenericConfigAttribute(VENDOR, NS, "VENDOR2"));
        deviceEditNode.addLeafChangeNode(MODEL_QNAME, new GenericConfigAttribute(MODEL, NS, "MODEL2"));
        EditContext editContext = new EditContext(deviceEditNode, null, ERROR_ON_ROLLBACK, null);
        try {
            m_deviceConfigInterceptor.interceptEditConfig(m_modelNode, editContext);
        }
        catch (EditConfigException e) {
            NetconfRpcError error = e.getRpcError();
            assertEquals(ADAPTER_NOR_DEPLOYED_ERROR, error.getErrorMessage());
            assertEquals(INVALID_VALUE, error.getErrorTag());
            assertEquals(Application, error.getErrorType());
            assertEquals(Error, error.getErrorSeverity());
            verify(m_adapterManager).getDeviceAdapter(adapter.capture());
        }
    }
}
