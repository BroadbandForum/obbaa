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

package org.broadband_forum.obbaa.store;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

/**
 * <p>
 * Abstract class that provides generic CRUD implementation based on mapDB.
 * </p>
 *
 * @param <KT> - type of key of the object being stored.
 * @param <VT> - type of key of the object being stored
 */
public abstract class AbstractStore<KT, VT extends Value<KT>> implements Store<KT, VT> {
    private final String m_mapDbFilePath;
    private final String m_dbName;
    private final String m_valueFileName;
    ConcurrentMap<KT, VT> m_db = null;
    private DB m_mapDb;

    public AbstractStore(String mapDbFilePath, String dbName, String valueFileName) {
        m_mapDbFilePath = mapDbFilePath;
        m_dbName = dbName;
        m_valueFileName = valueFileName;
    }

    public void init() {
        m_mapDb = DBMaker
                .fileDB(m_mapDbFilePath)
                .fileMmapEnable()
                .checksumHeaderBypass()
                .make();
        m_db = m_mapDb.hashMap(m_dbName, Serializer.STRING, Serializer.JAVA)
                .createOrOpen();
    }

    public void destroy() {
        m_mapDb.close();
    }

    public void create(VT value) {
        if (m_db.get(value.getKey()) != null) {
            throw new IllegalArgumentException(String.format("%s with key %s already exists", m_valueFileName, value.getKey()));
        }
        m_db.put(value.getKey(), value);
    }

    public VT get(KT key) {
        return m_db.get(key);
    }

    public void update(VT value) {
        if (m_db.get(value.getKey()) == null) {
            throw new IllegalArgumentException(String.format("%s with name %s does not exist", m_valueFileName, value.getKey()));
        }
        m_db.put(value.getKey(), value);
    }

    public void delete(KT key) {
        m_db.remove(key);
    }

    @Override
    public Set<VT> getAllEntries() {
        return new HashSet<>(m_db.values());
    }
}
