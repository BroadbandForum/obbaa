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

package org.broadband_forum.obbaa.device.adapter;

public class DeviceConfigBackup {

    private String m_oldDataStore;
    private String m_updatedDatastore;


    public DeviceConfigBackup(String oldDataStore, String updatedDatastore) {
        m_oldDataStore = oldDataStore;
        m_updatedDatastore = updatedDatastore;
    }


    public String getOldDataStore() {
        return m_oldDataStore;
    }

    public void setOldDataStore(String oldDataStore) {
        m_oldDataStore = oldDataStore;
    }

    public String getUpdatedDatastore() {
        return m_updatedDatastore;
    }

    public void setUpdatedDatastore(String updatedDatastore) {
        m_updatedDatastore = updatedDatastore;
    }
}
