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

package org.broadband_forum.obbaa.dmyang.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "db_version")
public class DbVersion {
    @Id
    private String moduleId;

    @Column
    private Long version = 1L;

    public String getModuleId() {
        return moduleId;
    }

    public long getVersion() {
        return version;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
