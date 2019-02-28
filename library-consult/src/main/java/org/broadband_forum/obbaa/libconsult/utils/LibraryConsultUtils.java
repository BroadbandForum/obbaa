/*
 *   Copyright 2018 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.broadband_forum.obbaa.libconsult.utils;

import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;

public final class LibraryConsultUtils {

    public static final String NAMESPACE = "urn:bbf:yang:obbaa:module-library-check";
    public static final String MODULE_NAME = "bbf-obbaa-yang-library-consult";
    public static final String RELATED_DEVICES = "devices";
    public static final String RELATED_DEVICE_NUM = "device-count";
    public static final String DEVICE = "device";
    public static final String MODULE = "module";
    public static final String USED_YANG_MODULES = "in-use-library-modules";
    public static final String REVISION_TAG = "revision";
    public static final String REVISION = "2018-11-07";
    public static final String ASSOCIATE_ADAPTERS = "associated-adapters";
    public static final String DEVICE_USED_YANG_MODULES = "device-library-modules";
    public static final String RELATED_ADAPTER = "related-adapter";

    private LibraryConsultUtils() {

    }

    public static DeviceAdapterId getDevReferredAdapterId(Device device) {
        return new DeviceAdapterId(device.getDeviceManagement().getDeviceType(),
                device.getDeviceManagement().getDeviceInterfaceVersion(), device.getDeviceManagement().getDeviceModel(),
                device.getDeviceManagement().getDeviceVendor());
    }

}
