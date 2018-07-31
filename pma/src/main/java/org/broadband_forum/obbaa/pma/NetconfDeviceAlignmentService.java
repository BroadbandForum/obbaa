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

package org.broadband_forum.obbaa.pma;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;

public interface NetconfDeviceAlignmentService extends DeviceAlignmentService {
    void queueEdit(String deviceName, EditConfigRequest request);

    List<EditConfigRequest> getEditQueue(String deviceName);

    void align(String deviceName);

    void forceAlign(String deviceName, CopyConfigRequest request);

}
