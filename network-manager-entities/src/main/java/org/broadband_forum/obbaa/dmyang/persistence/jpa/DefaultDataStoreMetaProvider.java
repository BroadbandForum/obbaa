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

package org.broadband_forum.obbaa.dmyang.persistence.jpa;

import javax.transaction.Transactional;

import org.broadband_forum.obbaa.dmyang.entities.DbVersion;
import org.broadband_forum.obbaa.netconf.persistence.DataStoreMetaProvider;

public class DefaultDataStoreMetaProvider implements DataStoreMetaProvider {
    private final DbVersionDao m_dao;

    public DefaultDataStoreMetaProvider(DbVersionDao dao) {
        m_dao = dao;
    }

    @Override
    public long getDataStoreVersion(String moduleId) {

        DbVersion version = m_dao.findByIdWithReadLock(moduleId);
        if (version == null) {
            return 0;
        } else {
            return version.getVersion();
        }
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public void updateDataStoreVersion(String moduleId, long newVersion) {
        DbVersion version = m_dao.findByIdWithWriteLock(moduleId);
        if (version == null) {
            version = new DbVersion();
            version.setModuleId(moduleId);
            version.setVersion(newVersion);
            m_dao.create(version);
        } else {
            version.setVersion(newVersion);
        }
    }

    @Override
    public String getAllDataStoreVersions() {
        return null;
    }
}
