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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.broadband_forum.obbaa.netconf.persistence.jpa.AbstractEntityDataStoreManager;

public class BaaOsgiEntityDataStoreManager extends AbstractEntityDataStoreManager {

    @PersistenceContext(unitName = "baa")
    private EntityManager m_entityManager;

    @Override
    public void beginTransaction() {
        //nothing to be done, container takes care of this
    }

    @Override
    public void commitTransaction() {
        //nothing to be done, container takes care of this

    }

    @Override
    public void rollbackTransaction() {
        //nothing to be done, container takes care of this

    }

    @Override
    public void close() {
        //nothing to be done, container takes care of this
    }

    @Override
    public EntityManager getEntityManager() {
        return m_entityManager;
    }

    protected void setEntityManager(EntityManager entityManager) {
        m_entityManager = entityManager;
    }
}
