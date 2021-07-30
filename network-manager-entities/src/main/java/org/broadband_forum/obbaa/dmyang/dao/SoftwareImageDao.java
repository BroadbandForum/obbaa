package org.broadband_forum.obbaa.dmyang.dao;

import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImagePK;
import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.EntityDAO;

public interface SoftwareImageDao extends EntityDAO<SoftwareImage, SoftwareImagePK> {

    List<SoftwareImage> getSoftwareImageEntityList(String parentId);

    void updateSWImageInOnuStateInfo(DeviceState deviceState, Set<SoftwareImage> vomciReturnedSwImageSet);
}
