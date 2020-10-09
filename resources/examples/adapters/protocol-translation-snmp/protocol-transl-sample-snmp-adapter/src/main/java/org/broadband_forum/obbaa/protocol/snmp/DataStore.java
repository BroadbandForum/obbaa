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
 *
 * This file contains sample data store implmentation
 *
 * Created by Sujeesh Ramakrishnan (DZSI) on 01/10/2020.
 */

package org.broadband_forum.obbaa.protocol.snmp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/* Sample data store class */

public final class DataStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStore.class);

    private static ListMultimap<String, MetaData> m_dict;

    private DataStore() {
    }

    static {
        m_dict = ArrayListMultimap.create();
        m_dict.put("hardware/component", new MetaData("1.3.6.1.2.1.47.1.1.1.1.11", OIDType.OCTETSTRING, "serial-num"));
        m_dict.put("hardware/component", new MetaData("1.3.6.1.2.1.47.1.1.1.1.14", OIDType.OCTETSTRING, "alias"));
        m_dict.put("hardware/component", new MetaData("1.3.6.1.2.1.47.1.1.1.1.15", OIDType.OCTETSTRING, "asset-id"));
    }

    public static List<MetaData> getOids(String keypath) {
        List<MetaData> clonedOids = new ArrayList<MetaData>();
        try {
            //Deep copy the list elements to avoid overwriting.
            for (MetaData iter : m_dict.get(keypath)) {
                clonedOids.add((MetaData)((MetaData)iter).clone());
            }
        } catch (CloneNotSupportedException e) {
            LOGGER.error("CloneNotSupportedException", e);
        }
        return clonedOids;
    }
}
