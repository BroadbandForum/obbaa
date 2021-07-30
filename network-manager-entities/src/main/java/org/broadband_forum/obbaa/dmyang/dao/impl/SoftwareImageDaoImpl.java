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


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.dao.SoftwareImageDao;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImagePK;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.AbstractDao;

@Transactional(Transactional.TxType.MANDATORY)
public class SoftwareImageDaoImpl extends AbstractDao<SoftwareImage, SoftwareImagePK> implements SoftwareImageDao {

    public static final String PARENT_ID = "parentId";
    private static final Logger LOGGER = Logger.getLogger(SoftwareImageDaoImpl.class);

    public SoftwareImageDaoImpl(PersistenceManagerUtil persistenceManagerUtil) {
        super(persistenceManagerUtil, SoftwareImage.class);
    }

    @Override
    public List<SoftwareImage> getSoftwareImageEntityList(String parentId) {
        if (parentId != null) {
            EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
            Map<String, Object> matchedValues = new HashMap<String, Object>();
            matchedValues.put(JPAEntityDataStoreManager.buildQueryPath(PARENT_ID), parentId);
            return entityDataStoreManager.findByMatchValue(SoftwareImage.class, matchedValues);
        } else {
            LOGGER.debug(String.format("Parent ID received is %s ", parentId));
            return null;
        }

    }

    @Override
    public void updateSWImageInOnuStateInfo(DeviceState deviceState, Set<SoftwareImage> swImageSetFromVomci) {
        for (SoftwareImage vomciImage : swImageSetFromVomci) {
            vomciImage.setParentId(deviceState.getOnuStateInfo().getOnuStateInfoId());
        }
        if (deviceState.getOnuStateInfo().getSoftwareImages() == null) {
            SoftwareImages softwareImages = new SoftwareImages();
            softwareImages.setSoftwareImagesId(deviceState.getOnuStateInfo().getOnuStateInfoId());
            softwareImages.setSoftwareImage(swImageSetFromVomci);
            deviceState.getOnuStateInfo().setSoftwareImages(softwareImages);
            for (SoftwareImage vomciImage : swImageSetFromVomci) {
                getPersistenceManager().create(vomciImage);
            }
        } else {
            String parentId = deviceState.getDeviceNodeId();
            HashSet<SoftwareImage> swImageSetFromDB = new HashSet<>(getSoftwareImageEntityList(parentId));
            if (!verifyIfSwImageSetsAreEqual(swImageSetFromDB, swImageSetFromVomci)) {
                for (SoftwareImage vomciImage : swImageSetFromVomci) {
                    for (SoftwareImage dbImage : getSoftwareImageEntityList(parentId)) {
                        //remove images from db if its not equal to vomci image
                        if (!dbImage.equals(vomciImage)) {
                            getPersistenceManager().delete(dbImage);
                            deviceState.getOnuStateInfo().getSoftwareImages().getSoftwareImage().remove(dbImage);
                        }
                    }
                }

                //add vomci image to db if its not present in DB already
                for (SoftwareImage vomciImage : swImageSetFromVomci) {
                    if (!deviceState.getOnuStateInfo().getSoftwareImages().getSoftwareImage().contains(vomciImage)) {
                        getPersistenceManager().create(vomciImage);
                        deviceState.getOnuStateInfo().getSoftwareImages().getSoftwareImage().add(vomciImage);
                    }
                }
            }

        }
    }

    private boolean verifyIfSwImageSetsAreEqual(Set<SoftwareImage> swImageSetDB, Set<SoftwareImage> swImageVomci) {
        boolean isEqual = false;
        if (swImageSetDB.size() == swImageVomci.size()) {
            if (swImageSetDB.containsAll(swImageVomci)) {
                isEqual = true;
            }
        } else {
            isEqual = false;
        }
        return isEqual;
    }


}

