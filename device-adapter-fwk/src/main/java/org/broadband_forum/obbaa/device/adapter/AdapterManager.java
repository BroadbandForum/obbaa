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


import java.util.Collection;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;

public interface AdapterManager {
    void deploy(DeviceAdapter adapter, SubSystem subSystem, Class klass, DeviceInterface deviceInterface);

    void undeploy(DeviceAdapter adapter);

    int getAdapterSize();

    DeviceAdapter getDeviceAdapter(DeviceAdapterId adapterId);

    AdapterContext getAdapterContext(DeviceAdapterId adapterId);

    Collection<DeviceAdapter> getAllDeviceAdapters();

    EditConfigRequest getEditRequestForAdapter(DeviceAdapterId adapterId);

    FactoryGarmentTag getFactoryGarmentTag(DeviceAdapter adapter);

    Map<String, AdapterContext> getStdAdapterContextRegistry();

    String EVENT_TOPIC = "com/bbf/obbaa/AdapterManager/Event";

    boolean isAdapterInUse(DeviceAdapterId adapterId);
}
