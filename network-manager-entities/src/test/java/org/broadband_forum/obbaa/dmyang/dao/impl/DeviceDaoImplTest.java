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

package org.broadband_forum.obbaa.dmyang.dao.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoints;
import org.broadband_forum.obbaa.dmyang.entities.NetworkFunctionLink;
import org.broadband_forum.obbaa.dmyang.entities.NetworkFunctionLinks;
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.TerminationPointA;
import org.broadband_forum.obbaa.dmyang.entities.TerminationPointB;
import org.broadband_forum.obbaa.dmyang.entities.VomciOnuManagement;
import org.broadband_forum.obbaa.dmyang.util.AbstractUtilTxTest;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

public class DeviceDaoImplTest extends AbstractUtilTxTest {

    public static final String DEVICE_A = "DeviceA";
    public static final String DEVICE_B = "DeviceB";
    public static final String DEVICE_D = "DeviceD";
    public static final String DEVICE_E = "DeviceE";
    public static final String ONU_DEVICE = "ont1";
    public static final String CONTAINER_MANAGED_DEVICES = "/container=managed-devices";
    public static final String DEVICE_C = "DeviceC";
    public static final String HSQL = "testhsql";
    private DeviceDao m_deviceDao;
    private PersistenceManagerUtil m_persistenceMgrUtil;
    private JPAEntityDataStoreManager m_emSpy;

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
        m_deviceDao = new DeviceDaoImpl(m_persistenceMgrUtil);
    }

    @After
    public void tearDown() throws Exception {
        beginTx();
        m_deviceDao.deleteAll();
        m_deviceDao = null;
        closeTx();
    }

    private void createTwoDevices() {
        Device device = new Device();
        device.setDeviceName(DEVICE_A);
        device.setParentId(CONTAINER_MANAGED_DEVICES);

        m_deviceDao.create(device);

        device = new Device();
        device.setDeviceName(DEVICE_B);
        device.setParentId(CONTAINER_MANAGED_DEVICES);

        m_deviceDao.create(device);
    }

    private void createOnuDevice(String deviceName) {
        Device device = new Device();
        device.setDeviceName(deviceName);
        device.setParentId(CONTAINER_MANAGED_DEVICES);

        Set<OnuManagementChain> onuManagementChainSet = new LinkedHashSet<>();
        OnuManagementChain onuManagementChain = new OnuManagementChain();
        onuManagementChain.setParentId(deviceName);
        onuManagementChain.setNfName("vomci-vendor-1");
        onuManagementChain.setNfType("vomci-function");
        onuManagementChain.setInsertOrder(0);
        onuManagementChainSet.add(onuManagementChain);

        OnuManagementChain onuManagementChain1 = new OnuManagementChain();
        onuManagementChain1.setParentId(deviceName);
        onuManagementChain1.setNfName("vomci-proxy-1");
        onuManagementChain1.setNfType("onu-management-proxy");
        onuManagementChain1.setInsertOrder(1);
        onuManagementChainSet.add(onuManagementChain1);


        OnuManagementChain onuManagementChain2 = new OnuManagementChain();
        onuManagementChain2.setParentId(deviceName);
        onuManagementChain2.setNfName("OLT1");
        onuManagementChain2.setNfType("olt");
        onuManagementChain2.setInsertOrder(2);
        onuManagementChainSet.add(onuManagementChain2);

        VomciOnuManagement vomciOnuManagement = new VomciOnuManagement();
        vomciOnuManagement.setOnuManagementChains(onuManagementChainSet);
        vomciOnuManagement.setVomciFunction("vomci-vendor-1");
        vomciOnuManagement.setParentId(deviceName);

        NetworkFunctionLinks networkFunctionLinks = new NetworkFunctionLinks();
        networkFunctionLinks.setParentId("NW_FN_LINKS");
        Set<NetworkFunctionLink> networkFunctionLinkSet = new HashSet<>();
        NetworkFunctionLink networkFunctionLink1 = new NetworkFunctionLink();
        networkFunctionLink1.setParentId("LINK-1");
        networkFunctionLink1.setName("link-1");
        TerminationPointA terminationPointA = new TerminationPointA();
        terminationPointA.setParentId("A");
        terminationPointA.setFunctionName("vomci-vendor-1");
        terminationPointA.setLocalEndpointName("vomci-grpc-1");
        TerminationPointB terminationPointB = new TerminationPointB();
        terminationPointB.setParentId("B");
        terminationPointB.setFunctionName("vomci-proxy-1");
        terminationPointB.setLocalEndpointName("proxy-grpc-1");
        networkFunctionLink1.setTerminationPointA(terminationPointA);
        networkFunctionLink1.setTerminationPointB(terminationPointB);
        networkFunctionLinkSet.add(networkFunctionLink1);

        NetworkFunctionLink networkFunctionLink2 = new NetworkFunctionLink();
        networkFunctionLink2.setParentId("LINK-2");
        networkFunctionLink2.setName("link-2");
        TerminationPointA terminationPointA2 = new TerminationPointA();
        terminationPointA2.setParentId("A2");
        terminationPointA2.setFunctionName("vomci-proxy-1");
        terminationPointA2.setLocalEndpointName("proxy-grpc-2");
        TerminationPointB terminationPointB2 = new TerminationPointB();
        terminationPointB2.setParentId("B2");
        terminationPointB2.setFunctionName("OLT1");
        terminationPointB2.setLocalEndpointName("olt-grpc-1");
        networkFunctionLink2.setTerminationPointA(terminationPointA2);
        networkFunctionLink2.setTerminationPointB(terminationPointB2);
        networkFunctionLinkSet.add(networkFunctionLink2);

        networkFunctionLinks.setNetworkFunctionLink(networkFunctionLinkSet);
        vomciOnuManagement.setNetworkFunctionLinks(networkFunctionLinks);
        vomciOnuManagement.setOnuMgmtChainSelection("configured");

        OnuConfigInfo onuConfigInfo = new OnuConfigInfo();
        onuConfigInfo.setParentId(deviceName);
        onuConfigInfo.setVomciOnuManagement(vomciOnuManagement);
        onuConfigInfo.setExpectedAttachmentPoints(prepreExpectedAttachmentPoint(deviceName));
        onuConfigInfo.setPlannedOnuManagementMode("baa-xpon-onu-types:use-vomci");
        onuConfigInfo.setPlannedOnuManagementModeNs("urn:bbf:yang:obbaa:xpon-onu-types");

        DeviceMgmt deviceMgmt = new DeviceMgmt();
        deviceMgmt.setParentId(deviceName);
        deviceMgmt.setOnuConfigInfo(onuConfigInfo);
        deviceMgmt.setDeviceType(DeviceManagerNSConstants.DEVICE_TYPE_ONU);
        device.setDeviceManagement(deviceMgmt);
        m_deviceDao.create(device);
    }

    private ExpectedAttachmentPoints prepreExpectedAttachmentPoint(String deviceName) {
        ExpectedAttachmentPoints expectedAttachmentPoints = new ExpectedAttachmentPoints();
        expectedAttachmentPoints.setListType("allow-any");
        expectedAttachmentPoints.setSchemaPath("expected-attachment-points");
        expectedAttachmentPoints.setParentId(deviceName);

        Set<ExpectedAttachmentPoint> expectedAttachmentPointSet = new HashSet<>();
        ExpectedAttachmentPoint expectedAttachmentPoint1 = new ExpectedAttachmentPoint();
        expectedAttachmentPoint1.setName("test1");
        expectedAttachmentPoint1.setParentId("test1");
        expectedAttachmentPoint1.setSchemaPath("test-sp1");
        expectedAttachmentPoint1.setChannelPartitionName("CG_1.CPart_1");
        expectedAttachmentPoint1.setOltName("olt1");
        expectedAttachmentPoint1.setPlannedOnuManagementModeInThisOlt("baa-xpon-onu-types:use-vomci");
        expectedAttachmentPoint1.setPlannedOnuManagementModeInThisOltNs("urn:bbf:yang:obbaa:xpon-onu-types");

        ExpectedAttachmentPoint expectedAttachmentPoint2 = new ExpectedAttachmentPoint();
        expectedAttachmentPoint2.setName("test2");
        expectedAttachmentPoint2.setParentId("test2");
        expectedAttachmentPoint2.setSchemaPath("test2-sp");
        expectedAttachmentPoint2.setChannelPartitionName("CG_1.CPart_2");
        expectedAttachmentPoint2.setPlannedOnuManagementModeInThisOlt("baa-xpon-onu-types:use-eomci");
        expectedAttachmentPoint2.setPlannedOnuManagementModeInThisOltNs("urn:bbf:yang:obbaa:xpon-onu-types");

        expectedAttachmentPointSet.add(expectedAttachmentPoint2);
        expectedAttachmentPointSet.add(expectedAttachmentPoint1);
        expectedAttachmentPoints.setExpectedAttachmentPointSet(expectedAttachmentPointSet);
        return expectedAttachmentPoints;
    }

    @Test
    public void createTwoDeviceAndAssert() throws Exception {
        beginTx();
        createTwoDevices();

        List<Device> deviceList = m_deviceDao.findAll();
        assertEquals(2, deviceList.size());

        assertEquals(DEVICE_A, m_deviceDao.getDeviceByName(DEVICE_A).getDeviceName());
        assertEquals(DEVICE_B, m_deviceDao.getDeviceByName(DEVICE_B).getDeviceName());
        assertNull(m_deviceDao.getDeviceByName(DEVICE_C));
        closeTx();
    }

    @Test
    public void testOnuConfigInfo_VomciOnuMgmt() throws Exception {
        beginTx();
        createOnuDevice(DEVICE_D);
        List<Device> deviceList = m_deviceDao.findAll();
        assertEquals(1, deviceList.size());

        //test getVomciFunctionName method
        assertEquals("vomci-vendor-1", m_deviceDao.getVomciFunctionName(DEVICE_D));
        assertEquals("configured", m_deviceDao.getDeviceByName(DEVICE_D).getDeviceManagement().getOnuConfigInfo()
                .getVomciOnuManagement().getOnuMgmtChainSelection());

        //test getOnuManagementChains method
        OnuManagementChain[] onuManagementChains = m_deviceDao.getOnuManagementChains(DEVICE_D);
        assertEquals(3, onuManagementChains.length);
        assertEquals("vomci-vendor-1", onuManagementChains[0].getNfName());
        assertEquals("vomci-function", onuManagementChains[0].getNfType());

        assertEquals("vomci-proxy-1", onuManagementChains[1].getNfName());
        assertEquals("onu-management-proxy", onuManagementChains[1].getNfType());

        assertEquals("OLT1", onuManagementChains[2].getNfName());
        assertEquals("olt", onuManagementChains[2].getNfType());

        //test getRemoteEndpointName method
        assertEquals("vomci-grpc-1", m_deviceDao.getRemoteEndpointName(DEVICE_D, DeviceManagerNSConstants.TERMINATION_POINT_A, "vomci-vendor-1"));
        assertEquals("proxy-grpc-2", m_deviceDao.getRemoteEndpointName(DEVICE_D, DeviceManagerNSConstants.TERMINATION_POINT_A, "vomci-proxy-1"));
        assertEquals("proxy-grpc-1", m_deviceDao.getRemoteEndpointName(DEVICE_D, DeviceManagerNSConstants.TERMINATION_POINT_B, "vomci-proxy-1"));
        assertEquals("olt-grpc-1", m_deviceDao.getRemoteEndpointName(DEVICE_D, DeviceManagerNSConstants.TERMINATION_POINT_B, "OLT1"));
        assertNull(m_deviceDao.getRemoteEndpointName(DEVICE_D, DeviceManagerNSConstants.TERMINATION_POINT_A, "OLT1"));

        //test OnuConfigInfo values
        assertEquals("baa-xpon-onu-types:use-vomci", m_deviceDao.getDeviceByName(DEVICE_D).getDeviceManagement().getOnuConfigInfo()
                .getPlannedOnuManagementMode());
        assertNotNull(m_deviceDao.getDeviceByName(DEVICE_D).getDeviceManagement().getOnuConfigInfo().getExpectedAttachmentPoints());
        assertEquals(2, m_deviceDao.getDeviceByName(DEVICE_D).getDeviceManagement().getOnuConfigInfo()
                .getExpectedAttachmentPoints().getExpectedAttachmentPointSet().size());
        assertEquals("allow-any", m_deviceDao.getDeviceByName(DEVICE_D).getDeviceManagement().getOnuConfigInfo()
                .getExpectedAttachmentPoints().getListType());
        closeTx();
    }

    @Test
    public void deleteOneDeviceAndAssert() throws Exception {
        beginTx();
        createTwoDevices();

        List<Device> deviceList = m_deviceDao.findAll();
        assertEquals(2, deviceList.size());

        Device deviceA = m_deviceDao.getDeviceByName(DEVICE_A);
        Device deviceB = m_deviceDao.getDeviceByName(DEVICE_B);

        assertEquals(DEVICE_A, deviceA.getDeviceName());
        assertEquals(DEVICE_B, deviceB.getDeviceName());
        assertNull(m_deviceDao.getDeviceByName(DEVICE_C));

        m_deviceDao.delete(deviceA);

        deviceList = m_deviceDao.findAll();
        assertEquals(1, deviceList.size());
        deviceA = m_deviceDao.getDeviceByName(DEVICE_A);
        deviceB = m_deviceDao.getDeviceByName(DEVICE_B);
        assertNull(deviceA);
        assertEquals(DEVICE_B, deviceB.getDeviceName());
        closeTx();
    }

    @Override
    protected PersistenceManagerUtil getPersistenceManagerUtil() {
        return m_persistenceMgrUtil;
    }
}