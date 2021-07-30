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


package org.broadband_forum.obbaa.dmyang.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.dmyang.dao.SoftwareImageDao;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.dmyang.util.AbstractUtilTxTest;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SoftwareImageDaoImplTest extends AbstractUtilTxTest {

    public static final String HSQL = "testhsql";
    private SoftwareImageDao m_softwareImageDao;
    private PersistenceManagerUtil m_persistenceMgrUtil;
    private JPAEntityDataStoreManager m_emSpy;

    private DeviceState m_deviceState = mock(DeviceState.class);
    private OnuStateInfo m_onuStateInfo = mock(OnuStateInfo.class);
    private SoftwareImages m_softwareImages = mock(SoftwareImages.class);


    @Before
    public void setup() {
        JPAEntityManagerFactory factory = new JPAEntityManagerFactory(HSQL);
        m_persistenceMgrUtil = new ThreadLocalPersistenceManagerUtil(factory) {
            protected EntityDataStoreManager createEntityDataStoreManager() {
                JPAEntityDataStoreManager jpaEntityDataStoreManager = new JPAEntityDataStoreManager(getEntityManagerFactory());
                m_emSpy = spy(jpaEntityDataStoreManager);
                return m_emSpy;
            }
        };
        m_softwareImageDao = new SoftwareImageDaoImpl(m_persistenceMgrUtil);
    }

    @After
    public void tearDown() throws Exception {
        beginTx();
        m_softwareImageDao.deleteAll();
        closeTx();
    }

    private void createSoftwareImageEntity() {
        SoftwareImage softwareImage = new SoftwareImage();
        softwareImage.setParentId("onu1");
        softwareImage.setHash("1234");
        softwareImage.setVersion("1.0");
        softwareImage.setProductCode("version1.0");
        softwareImage.setIsActive(true);
        softwareImage.setIsCommitted(true);
        softwareImage.setIsValid(true);
        softwareImage.setId(0);
        m_softwareImageDao.create(softwareImage);

        SoftwareImage softwareImage1 = new SoftwareImage();
        softwareImage1.setParentId("onu2");
        softwareImage1.setHash("4321");
        softwareImage1.setVersion("1.1");
        softwareImage1.setProductCode("version1.1");
        softwareImage1.setIsActive(false);
        softwareImage1.setIsCommitted(false);
        softwareImage1.setIsValid(true);
        softwareImage1.setId(1);
        m_softwareImageDao.create(softwareImage1);

        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setParentId("onu2");
        softwareImage0.setHash("1234");
        softwareImage0.setVersion("1.2");
        softwareImage0.setProductCode("version1.2");
        softwareImage0.setIsActive(true);
        softwareImage0.setIsCommitted(true);
        softwareImage0.setIsValid(true);
        softwareImage0.setId(0);
        m_softwareImageDao.create(softwareImage0);

    }

    @Test
    public void testCreateSwImageAndAssert() throws Exception {
        beginTx();
        createSoftwareImageEntity();
        assertEquals(3, m_softwareImageDao.findAll().size());
        assertEquals(1, m_softwareImageDao.getSoftwareImageEntityList("onu1").size());
        assertEquals(2, m_softwareImageDao.getSoftwareImageEntityList("onu2").size());
        closeTx();
    }

    @Test
    public void testDeleteSwImageAndAssert() throws Exception {
        beginTx();
        createSoftwareImageEntity();
        for (SoftwareImage swImage : m_softwareImageDao.getSoftwareImageEntityList("onu2")) {
            m_softwareImageDao.delete(swImage);
        }
        assertEquals(0, m_softwareImageDao.getSoftwareImageEntityList("onu2").size());
        assertEquals(1, m_softwareImageDao.findAll().size());
        assertEquals(1, m_softwareImageDao.getSoftwareImageEntityList("onu1").size());
        m_softwareImageDao.deleteAll();
        assertEquals(0, m_softwareImageDao.getSoftwareImageEntityList("onu1").size());
        closeTx();
    }

    @Test
    public void testGetSwEntityOnNonExistingEntity() throws Exception {
        beginTx();
        assertEquals(0, m_softwareImageDao.findAll().size());
        createSoftwareImageEntity();
        assertEquals(3, m_softwareImageDao.findAll().size());
        assertEquals(1, m_softwareImageDao.getSoftwareImageEntityList("onu1").size());
        assertEquals(2, m_softwareImageDao.getSoftwareImageEntityList("onu2").size());
        assertEquals(0, m_softwareImageDao.getSoftwareImageEntityList("onu3").size());
        closeTx();
    }

    @Test
    public void testGetSWEntityWhenParentIdIsNull() throws Exception {
        beginTx();
        assertEquals(0, m_softwareImageDao.findAll().size());
        createSoftwareImageEntity();
        assertNull(m_softwareImageDao.getSoftwareImageEntityList(null));
        assertEquals(3, m_softwareImageDao.findAll().size());
        closeTx();
    }

    @Test
    public void testAddNewSwEntity() {
        beginTx();
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("onu2");
        when(m_onuStateInfo.getOnuStateInfoId()).thenReturn("onu2");
        when(m_onuStateInfo.getSoftwareImages()).thenReturn(null);
        m_softwareImageDao.deleteAll();
        assertEquals(0, m_softwareImageDao.findAll().size());
        m_softwareImageDao.updateSWImageInOnuStateInfo(m_deviceState, prepareVomciSwImageSet(""));
        assertEquals(2, m_softwareImageDao.getSoftwareImageEntityList("onu2").size());
        closeTx();
    }

    @Test
    public void testUpdateExistingSwEntity() {
        beginTx();
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("onu1");
        when(m_onuStateInfo.getOnuStateInfoId()).thenReturn("onu1");
        when(m_deviceState.getOnuStateInfo().getSoftwareImages()).thenReturn(m_softwareImages);
        createSoftwareImageEntity();
        assertEquals(3, m_softwareImageDao.findAll().size());
        assertEquals(1, m_softwareImageDao.getSoftwareImageEntityList("onu1").size());
        Set<SoftwareImage> swImageSetVomci = prepareVomciSwImageSet("onu1");
        m_softwareImageDao.updateSWImageInOnuStateInfo(m_deviceState, swImageSetVomci);
        List<SoftwareImage> swList = m_softwareImageDao.getSoftwareImageEntityList("onu1");
        assertEquals(2, m_softwareImageDao.getSoftwareImageEntityList("onu1").size());
        HashSet<SoftwareImage> swImageSetFromDB = new HashSet<>(swList);
        assertEquals(swImageSetFromDB.size(), swImageSetVomci.size());
        assertTrue(swImageSetFromDB.containsAll(swImageSetVomci));
        closeTx();
    }

    @Test
    public void testUpdateExistingSwEntity1() {
        beginTx();
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("onu2");
        when(m_onuStateInfo.getOnuStateInfoId()).thenReturn("onu2");
        when(m_deviceState.getOnuStateInfo().getSoftwareImages()).thenReturn(m_softwareImages);
        createSoftwareImageEntity();
        assertEquals(3, m_softwareImageDao.findAll().size());
        assertEquals(2, m_softwareImageDao.getSoftwareImageEntityList("onu2").size());
        Set<SoftwareImage> swImageSetVomci = prepareVomciSwImage0Set("");
        m_softwareImageDao.updateSWImageInOnuStateInfo(m_deviceState, swImageSetVomci);
        List<SoftwareImage> swList = m_softwareImageDao.getSoftwareImageEntityList("onu2");
        assertEquals(1, m_softwareImageDao.getSoftwareImageEntityList("onu2").size());
        Set<SoftwareImage> swImageSetDB = new HashSet<>(swList);
        assertEquals(swImageSetDB.size(), swImageSetVomci.size());
        assertTrue(swImageSetDB.containsAll(swImageSetVomci));
        closeTx();
    }

    private Set<SoftwareImage> prepareVomciSwImageSet(String parentId) {
        Set<SoftwareImage> softwareImageSet = new HashSet<>();
        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setId(0);
        softwareImage0.setParentId(parentId);
        softwareImage0.setHash("00");
        softwareImage0.setProductCode("test");
        softwareImage0.setVersion("0000");
        softwareImage0.setIsValid(true);
        softwareImage0.setIsCommitted(true);
        softwareImage0.setIsActive(true);
        SoftwareImage softwareImage1 = new SoftwareImage();
        softwareImage1.setId(1);
        softwareImage1.setParentId(parentId);
        softwareImage1.setHash("11");
        softwareImage1.setProductCode("test1111");
        softwareImage1.setVersion("1111");
        softwareImage1.setIsValid(true);
        softwareImage1.setIsCommitted(false);
        softwareImage1.setIsActive(false);
        softwareImageSet.add(softwareImage0);
        softwareImageSet.add(softwareImage1);
        return softwareImageSet;
    }

    private Set<SoftwareImage> prepareVomciSwImage0Set(String parentId) {
        Set<SoftwareImage> swImage0SetVomci = new HashSet<>();
        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setId(0);
        softwareImage0.setParentId(parentId);
        softwareImage0.setHash("1001");
        softwareImage0.setProductCode("test");
        softwareImage0.setVersion("1.01");
        softwareImage0.setIsValid(false);
        softwareImage0.setIsCommitted(false);
        softwareImage0.setIsActive(false);
        swImage0SetVomci.add(softwareImage0);
        return swImage0SetVomci;
    }

    @Override
    protected PersistenceManagerUtil getPersistenceManagerUtil() {
        return m_persistenceMgrUtil;
    }


}
