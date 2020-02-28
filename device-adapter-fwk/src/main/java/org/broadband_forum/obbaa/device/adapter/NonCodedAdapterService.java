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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;

public interface NonCodedAdapterService {
    void deployAdapter(String deviceAdapterId, SubSystem subSystem, Class klass, String stagingArea, String ipfixStagingArea,
                       DeviceInterface deviceInterface) throws Exception;

    void unDeployAdapter(String type, String interfaceVersion, String model, String vendor) throws Exception;

    DeviceAdapterId getNonCodedAdapterId(String adapterArchiveFileName, String deviceXmlpath) throws Exception;
}

