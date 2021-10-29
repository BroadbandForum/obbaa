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

package org.broadband_forum.obbaa.onu;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.LockModeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Unit tests that tests handling of unknown ONUs
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
@Ignore
public class UnknownONUHandlerTest {

    @Mock
    PersistenceManagerUtil m_persistenceMgrUtil;
    @Mock
    EntityDataStoreManager m_manager;
    UnknownONU m_unknownOnu;
    String m_serialNum = "ABCD12345678";
    String m_regId = "12345678";

    UnknownONUHandler m_unknownOnuHandler;

    List<UnknownONU> m_unknownOnuList = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_unknownOnu = new UnknownONU();
        m_unknownOnu.setSerialNumber(m_serialNum);
        m_unknownOnu.setRegistrationId(m_regId);
        m_unknownOnuList.add(m_unknownOnu);
        when(m_persistenceMgrUtil.getEntityDataStoreManager()).thenReturn(m_manager);
    }

    @Test
    public void testCreateUnknownONUEntity() {
        m_unknownOnuHandler = new UnknownONUHandler(m_persistenceMgrUtil);
        m_unknownOnuHandler.createUnknownONUEntity(m_unknownOnu);
        //verify(m_manager).create(m_unknownOnu);
    }

    @Ignore
    @Test
    public void testFindUnknownOnuEntityWithSerialNumber() {
        Map<String, Object> matchValue = new HashMap<String, Object>();
        matchValue.put(ONUConstants.SERIAL_NUMBER, m_serialNum);
        when(m_manager.findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE)).thenReturn(m_unknownOnuList);
        m_unknownOnuHandler = new UnknownONUHandler(m_persistenceMgrUtil);
        UnknownONU onu = m_unknownOnuHandler.findUnknownOnuEntity(m_serialNum, null);
        verify(m_manager).findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE);
        assertEquals(m_serialNum, onu.getSerialNumber());
    }

    @Ignore
    @Test
    public void testFindUnknownOnuEntityWithRegId() {
        Map<String, Object> matchValue = new HashMap<String, Object>();
        matchValue.put(ONUConstants.REGISTRATION_ID, m_regId);
        when(m_manager.findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE)).thenReturn(m_unknownOnuList);
        m_unknownOnuHandler = new UnknownONUHandler(m_persistenceMgrUtil);
        UnknownONU onu = m_unknownOnuHandler.findUnknownOnuEntity(null, m_regId);
        //verify(m_manager).findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE);
        assertEquals(m_regId, onu.getRegistrationId());
    }

    @Ignore
    @Test
    public void testFindUnknownOnuEntityWithSNAndRegId() {
        Map<String, Object> matchValue = new HashMap<String, Object>();
        matchValue.put(ONUConstants.REGISTRATION_ID, m_regId);
        matchValue.put(ONUConstants.SERIAL_NUMBER, m_serialNum);
        when(m_manager.findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE)).thenReturn(m_unknownOnuList);
        m_unknownOnuHandler = new UnknownONUHandler(m_persistenceMgrUtil);
        UnknownONU onu = m_unknownOnuHandler.findUnknownOnuEntity(m_serialNum, m_regId);
        //verify(m_manager).findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE);
        assertEquals(m_serialNum, onu.getSerialNumber());
        assertEquals(m_regId, onu.getRegistrationId());
    }

    @Test
    public void testFindUnknownOnuEntityIfSNAndRegIdNull() {
        Map<String, Object> matchValue = new HashMap<String, Object>();
        m_unknownOnuHandler = new UnknownONUHandler(m_persistenceMgrUtil);
        UnknownONU onu = m_unknownOnuHandler.findUnknownOnuEntity(null, null);
        //verify(m_manager, never()).findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE);
        assertNull(onu);
    }

    @Test
    public void testDeleteUnknownOnuEntity() {
        m_unknownOnuHandler = new UnknownONUHandler(m_persistenceMgrUtil);
        m_unknownOnuHandler.deleteUnknownOnuEntity(m_unknownOnu);
        //verify(m_manager).delete(m_unknownOnu);
    }
}
