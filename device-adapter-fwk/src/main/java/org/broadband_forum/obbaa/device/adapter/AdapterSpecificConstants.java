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

package org.broadband_forum.obbaa.device.adapter;

public interface AdapterSpecificConstants {

    String ADAPTER_XML_PATH = "/model/device-adapter.xml";
    String YANG_PATH = "/yang";

    String UNABLE_TO_GET_ADAPTER_DETAILS_TYPE = "Unable to get type, interface-version, model and vendor in specified "
            + "device adapter definition archive";
    String ADAPTER_DEFINITION_ARCHIVE_NOT_FOUND_ERROR = "Device adapter definition archive not found in the staging area";
    String DASH = "-";
    String YANG_EXTN = ".yang";
    String FINAL_FOLDER = "final";          // in target dir (target/generated-resources)
    String YANG_LIB_FILE = "model/yang-library.xml";
    String VARIANTS = "variants";
    String MODEL = "model";
    String YANG = "yang";
    String SUPPORTED_FEATURES_FILE = "supported-features.txt";
    String SUPPORTED_DEVIATIONS_FILE = "supported-deviations.txt";
    String SLASH = "/";
    String DEFAULT_FEATURES_PATH = "/model/supported-features.txt";
    String DEFAULT_DEVIATIONS_PATH = "/model/supported-deviations.txt";
    String DEFAULT_CONFIG_XML_PATH = "/model/default-config.xml";
    String DEFAULT_IPFIX_MAPPING_FILE_PATH = "model/IPFIX_IEId.csv";
    String DEFAULT_IPFIX_MAPPING_FILE = "IPFIX_IEId.csv";
    String COMMA = ",";
    String DPU = "DPU";
    String OLT = "OLT";
    String STANDARD = "standard";
    String BBF = "BBF";
    String STANDARD_ADAPTER_OLDEST_VERSION = "1.0";
    String PERCENTAGE = "%";
    String NOT_APPLICABLE = "Not Applicable";
}
