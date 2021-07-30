/*
 * Copyright 2021 Broadband Forum
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
package org.broadband_forum.obbaa.nm.nwfunctionmgr;

import static org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants.NETWORK_FUNCTIONS_ID_TEMPLATE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NetworkFunctionSubsystemTest {

    @Mock
    NetworkFunctionManager m_networkFunctionManager;
    @Mock
    EditConfigChangeNotification m_editConfigChangeNotification;
    @Mock
    ModelNodeId m_modelNodeId;
    @Mock
    ModelNodeChange m_modelNodeChange;
    @Mock
    EditContainmentNode m_editContainmentNode;
    @Mock
    List<EditMatchNode> m_editMatchNodesList;
    @Mock
    EditMatchNode m_editMatchNode;

    NetworkFunctionSubsystem m_networkFunctionSubsystem;
    List<ChangeNotification> m_changeNotificationList;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_networkFunctionSubsystem = new NetworkFunctionSubsystem(m_networkFunctionManager);
        m_changeNotificationList = new ArrayList<ChangeNotification>();
        m_changeNotificationList.add(m_editConfigChangeNotification);
    }

    @Test
    public void testNotifYChangedCreateNwFunction() {
        when(m_editConfigChangeNotification.getModelNodeId()).thenReturn(NETWORK_FUNCTIONS_ID_TEMPLATE);
        when(m_editConfigChangeNotification.getChange()).thenReturn(m_modelNodeChange);
        when(m_modelNodeChange.getChangeType()).thenReturn(ModelNodeChangeType.create);
        when(m_modelNodeChange.getChangeData()).thenReturn(m_editContainmentNode);
        when(m_editContainmentNode.getName()).thenReturn("network-function");
        when(m_editContainmentNode.getMatchNodes()).thenReturn(m_editMatchNodesList);
        when(m_editMatchNodesList.get(0)).thenReturn(m_editMatchNode);
        when(m_editMatchNode.getValue()).thenReturn("vomci");
        m_networkFunctionSubsystem.notifyChanged(m_changeNotificationList);
        verify(m_editConfigChangeNotification, times(1)).getModelNodeId();
        verify(m_editConfigChangeNotification, times(3)).getChange();
        verify(m_modelNodeChange, times(2)).getChangeData();
        verify(m_modelNodeChange, times(1)).getChangeType();
        verify(m_editContainmentNode, times(1)).getName();
        verify(m_editContainmentNode, times(1)).getMatchNodes();
        verify(m_editMatchNodesList, times(1)).get(0);
        verify(m_editMatchNode, times(1)).getValue();
        verify(m_networkFunctionManager, times(1)).networkFunctionAdded("vomci");
        verify(m_networkFunctionManager, never()).networkFunctionRemoved("vomci");
    }

    @Test
    public void testNotifYChangedDeleteNwFunction() {
        when(m_editConfigChangeNotification.getModelNodeId()).thenReturn(NETWORK_FUNCTIONS_ID_TEMPLATE);
        when(m_editConfigChangeNotification.getChange()).thenReturn(m_modelNodeChange);
        when(m_modelNodeChange.getChangeType()).thenReturn(ModelNodeChangeType.delete);
        when(m_modelNodeChange.getChangeData()).thenReturn(m_editContainmentNode);
        when(m_editContainmentNode.getName()).thenReturn("network-function");
        when(m_editContainmentNode.getMatchNodes()).thenReturn(m_editMatchNodesList);
        when(m_editMatchNodesList.get(0)).thenReturn(m_editMatchNode);
        when(m_editMatchNode.getValue()).thenReturn("vomci");
        m_networkFunctionSubsystem.notifyChanged(m_changeNotificationList);
        verify(m_editConfigChangeNotification, times(1)).getModelNodeId();
        verify(m_editConfigChangeNotification, times(4)).getChange();
        verify(m_modelNodeChange, times(2)).getChangeData();
        verify(m_modelNodeChange, times(2)).getChangeType();
        verify(m_editContainmentNode, times(1)).getName();
        verify(m_editContainmentNode, times(1)).getMatchNodes();
        verify(m_editMatchNodesList, times(1)).get(0);
        verify(m_editMatchNode, times(1)).getValue();
        verify(m_networkFunctionManager, never()).networkFunctionAdded("vomci");
        verify(m_networkFunctionManager, times(1)).networkFunctionRemoved("vomci");
    }

    @Test
    public void testNotifYChangedWithWrongModelNodeId() {
        when(m_editConfigChangeNotification.getModelNodeId()).thenReturn(m_modelNodeId);
        m_networkFunctionSubsystem.notifyChanged(m_changeNotificationList);
        verify(m_editConfigChangeNotification, times(1)).getModelNodeId();
        verify(m_editConfigChangeNotification, never()).getChange();
        verify(m_networkFunctionManager, never()).networkFunctionAdded("vomci");
        verify(m_networkFunctionManager, never()).networkFunctionRemoved("vomci");
    }
}