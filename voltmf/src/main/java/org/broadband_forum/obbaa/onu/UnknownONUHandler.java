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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;

/**
 * <p>
 * Handles persistence of unknown ONU to DB
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class UnknownONUHandler {
    private static final Logger LOGGER = Logger.getLogger(UnknownONUHandler.class);
    private final PersistenceManagerUtil m_persistenceMgrUtil;

    public UnknownONUHandler(PersistenceManagerUtil persistenceMgrUtil) {
        m_persistenceMgrUtil = persistenceMgrUtil;
    }

    public void createUnknownONUEntity(UnknownONU unknownONU) {
        EntityDataStoreManager manager = m_persistenceMgrUtil.getEntityDataStoreManager();
        try {
            manager.create(unknownONU);
        } catch (Exception e) {
            LOGGER.error("Problem in creating unknown ONU entity " + unknownONU);
            throw new RuntimeException("Unable to create unknown ONU entity", e);
        }
    }

    public UnknownONU findUnknownOnuEntity(String serialNumber, String registrationId) {
        final UnknownONU[] unknownONU = new UnknownONU[1];
        EntityDataStoreManager manager = m_persistenceMgrUtil.getEntityDataStoreManager();
        Map<String, Object> matchValue = new HashMap<String, Object>();
        if (serialNumber != null) {
            matchValue.put(ONUConstants.SERIAL_NUMBER, serialNumber);
        }
        if (registrationId != null) {
            matchValue.put(ONUConstants.REGISTRATION_ID, registrationId);
        }
        if (!matchValue.isEmpty()) {
            try {
                List<UnknownONU> unknownOnuList = manager.findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE);
                if (unknownOnuList.size() > 0) {
                    unknownONU[0] = unknownOnuList.get(0);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Problem in finding unknown ONU entity with serialNo:%s and RegistrationId:%s.",
                        serialNumber, registrationId), e);
            }
        }
        return unknownONU[0];
    }

    public UnknownONU findMatchingUnknownOnu(String oltName, String channelTermRef, String onuId) {
        UnknownONU unknownONU = new UnknownONU();
        EntityDataStoreManager manager = m_persistenceMgrUtil.getEntityDataStoreManager();
        Map<String, Object> matchValue = new HashMap<>();
        if (oltName != null) {
            matchValue.put(ONUConstants.OLT_DEVICE_NAME, oltName);
        }
        if (onuId != null) {
            matchValue.put(ONUConstants.ONU_ID, onuId);
        }

        if (channelTermRef != null) {
            matchValue.put(ONUConstants.CHANNEL_TERMINATION_REFERENCE, channelTermRef);
        }
        if (!matchValue.isEmpty()) {
            try {
                List<UnknownONU> unknownOnuList = manager.findByMatchValue(UnknownONU.class, matchValue, LockModeType.PESSIMISTIC_WRITE);
                if (!unknownOnuList.isEmpty()) {
                    unknownONU = unknownOnuList.get(0);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Problem in finding unknown ONU entity with oltName:%s, onuId:%s and channelTermRef:%s",
                        oltName, onuId, channelTermRef), e);
            }
        }
        return unknownONU;
    }

    public void deleteUnknownOnuEntity(UnknownONU unknownONU) {
        EntityDataStoreManager manager = m_persistenceMgrUtil.getEntityDataStoreManager();
        try {
            manager.delete(unknownONU);
        } catch (Exception e) {
            LOGGER.error("Problem in deleting unknown ONU entity " + unknownONU, e);
        }
    }

}
