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

package org.broadband_forum.obbaa.ipfix.collector.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.ipfix.collector.service.IEMappingCacheService;
import org.broadband_forum.obbaa.ipfix.collector.service.InformationElementService;
import org.broadband_forum.obbaa.ipfix.entities.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.entities.ie.InformationElementType;
import org.broadband_forum.obbaa.ipfix.entities.service.ie.InformationElementCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

public class InformationElementServiceImpl implements InformationElementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformationElementServiceImpl.class);

    private static final String HEADER_ELEMENTID = "ElementID";
    private static final String HEADER_NAME = "Name";
    private static final String HEADER_ABSTRACT_DATA_TYPE = "Abstract Data Type";
    private static final String HEADER_DATA_TYPE_SEMANTICS = "Data Type Semantics";
    private static final String HEADER_STATUS = "Status";
    private static final String HEADER_DESCRIPTION = "Description";
    private static final String HEADER_UNITS = "Units";
    private static final String HEADER_RANGE = "Range";
    private static final String HEADER_REFERENCES = "References";
    private static final String HEADER_REQUESTER = "Requester";
    private static final String HEADER_REVISION = "Revision";
    private static final String HEADER_DATE = "Date";
    private static final String[] IE_MAPPING_HEADERS = {HEADER_ELEMENTID, HEADER_NAME, HEADER_ABSTRACT_DATA_TYPE,
        HEADER_DATA_TYPE_SEMANTICS, HEADER_STATUS, HEADER_DESCRIPTION, HEADER_UNITS, HEADER_RANGE,
        HEADER_REFERENCES, HEADER_REQUESTER, HEADER_REVISION, HEADER_DATE};

    private InformationElementCache m_ieCache;
    private IEMappingCacheService m_ieMappingCacheService;

    public InformationElementServiceImpl(InformationElementCache ieCache) {
        m_ieCache = ieCache;
    }

    @Override
    public InformationElement getInformationElement(String family, int informationElementId) {
        if (isNeedToSync(family)) {
            synchronized (this) {
                if (isNeedToSync(family)) {
                    m_ieMappingCacheService.syncIEMappingCache(family);
                }
            }
        }

        return m_ieCache.getInformationElement(family, informationElementId);
    }

    @Override
    public InformationElement getInformationElement(long enterpriseNumber, String informationElementName) {
        return m_ieCache.getInformationElement(enterpriseNumber, informationElementName);
    }

    private boolean isNeedToSync(String family) {
        return !m_ieCache.isAvailable(family) && Objects.nonNull(m_ieMappingCacheService);
    }

    @Override
    public boolean isIECacheAvailable() {
        return m_ieCache.isAvailable();
    }

    private void loadInformationElementMappingInternal(String family, File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            CsvReader reader = new CsvReader(is, StandardCharsets.UTF_8);
            findHeaderHolder(reader);

            if (!Arrays.equals(IE_MAPPING_HEADERS, reader.getHeaders())) {
                LOGGER.error(String.format("IE mapping file for family %s has incorrect format,"
                        + " skipping file %s", family, file.getAbsolutePath()));
                return;
            }

            while (reader.readRecord()) {
                if (!StringUtils.isNumeric(reader.get(HEADER_ELEMENTID))) {
                    LOGGER.warn(String.format("IE mapping file has incorrect format Element ID: {}, skipping record",
                            reader.get(HEADER_ELEMENTID)));
                    continue;
                }
                InformationElementType dataType;
                try {
                    dataType = InformationElementType.getEnum(reader.get(HEADER_ABSTRACT_DATA_TYPE));
                } catch (MissingAbstractDataTypeException e) {
                    LOGGER.warn(String.format("IE mapping file has unsupported data type: %s, skipping record",
                            reader.get(HEADER_ABSTRACT_DATA_TYPE)));
                    continue;
                }

                InformationElement ie = new InformationElement();
                ie.setElementId(Integer.parseInt(reader.get(HEADER_ELEMENTID)));
                ie.setDataType(dataType);
                ie.setName(reader.get(HEADER_NAME));
                ie.setDataTypeSemantics(reader.get(HEADER_DATA_TYPE_SEMANTICS));
                ie.setStatus(reader.get(HEADER_STATUS));
                ie.setDescription(reader.get(HEADER_DESCRIPTION));
                ie.setUnits(reader.get(HEADER_UNITS));
                ie.setRange(reader.get(HEADER_RANGE));
                ie.setReferences(reader.get(HEADER_REFERENCES));
                ie.setRequester(reader.get(HEADER_REQUESTER));
                ie.setRevision(reader.get(HEADER_REVISION));
                ie.setDate(reader.get(HEADER_DATE));

                m_ieCache.putInformationElementId(family, ie);
                LOGGER.debug(null, "Finished caching file: {}. The IEId of device adapter {} is updated.", file.getName(), family);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read file {}, skipping IE map caching for device adapter {}", file.getName(), family);
        }
    }

    private void findHeaderHolder(CsvReader reader) throws IOException {
        boolean hasMoreData = true;
        while (hasMoreData && !Arrays.equals(IE_MAPPING_HEADERS, reader.getHeaders())) {
            hasMoreData = reader.readHeaders();
        }
    }

    @Override
    public void loadInformationElementMapping(String family, File file) {
        loadInformationElementMappingInternal(family, file);
    }

    @Override
    public boolean removeInformationElementMapping(String deviceFamily) {
        return m_ieCache.removeInformationElement(deviceFamily);
    }

    @Override
    public void setIeMappingCacheService(IEMappingCacheService ieMappingCacheService) {
        m_ieMappingCacheService = ieMappingCacheService;
    }
}
