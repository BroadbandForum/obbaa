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

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STANDARD;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.STANDARD_ADAPTER_OLDEST_VERSION;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.broadband_forum.obbaa.device.adapter.util.SystemProperty;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdapterUtils {

    private static final String LOG_FILE_FOLDER = File.separator + "baa" + File.separator + "stores" + File.separator
            + "deviceAdapter" + File.separator;
    private static final String LOG_FILE = "errorEvents";
    private static final String LOG_FILE_PATH = LOG_FILE_FOLDER + File.separator + LOG_FILE;
    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterUtils.class);

    private AdapterUtils() {

    }

    public static AdapterContext getAdapterContext(Device device, AdapterManager adapterManager) {
        return adapterManager.getAdapterContext(new DeviceAdapterId(
                device.getDeviceManagement().getDeviceType(), device.getDeviceManagement().getDeviceInterfaceVersion(),
                device.getDeviceManagement().getDeviceModel(), device.getDeviceManagement().getDeviceVendor()));
    }

    public static AdapterContext getStandardAdapterContext(AdapterManager adapterManager, DeviceAdapter deviceAdapter) {
        if (deviceAdapter != null) {
            String adapterType = deviceAdapter.getType();
            AdapterContext stdAdapterContext = null;
            if (deviceAdapter.getStdAdapterIntVersion() != null && !deviceAdapter.getModel().equals(STANDARD)) {
                stdAdapterContext = getStdAdapterContext(adapterType, adapterManager, deviceAdapter.getStdAdapterIntVersion());

            } else if (deviceAdapter.getStdAdapterIntVersion() == null && !deviceAdapter.getModel().equals(STANDARD)) {
                stdAdapterContext = getStdAdapterContext(adapterType, adapterManager, STANDARD_ADAPTER_OLDEST_VERSION);

            } else if (deviceAdapter.getModel().equalsIgnoreCase(STANDARD) && deviceAdapter.getStdAdapterIntVersion() == null) {
                stdAdapterContext = adapterManager.getAdapterContext(deviceAdapter.getDeviceAdapterId());

            }
            return stdAdapterContext;
        } else {
            throw new RuntimeException("Given device adapter is not installed");
        }
    }

    private static AdapterContext getStdAdapterContext(String adapterType, AdapterManager adapterManager, String stdAdapterVersion) {
        if (isStandardAdapterDeployed(adapterManager, adapterType, stdAdapterVersion)) {
            String stdAdapterKey = adapterType.concat("-").concat(stdAdapterVersion);
            AdapterContext adapterContext = adapterManager.getStdAdapterContextRegistry().get(stdAdapterKey);
            if (adapterContext == null) {
                throw new RuntimeException("no standard adapterContext deployed for : " + adapterType);
            }
            return adapterContext;
        } else {
            throw new RuntimeException("no standard adapter found for this type of device : " + adapterType);
        }

    }

    public static String updateAdapterLastUpdateTime(String key, DateTime now) {
        String result = now.toString();
        SystemProperty systemProperty = SystemProperty.getInstance();
        String persistResult = systemProperty.get(key);
        if (persistResult != null) {
            result = persistResult;
        } else {
            systemProperty.set(key, result);
        }

        return result;
    }

    public static void removeAdapterLastUpdateTime(String key) {
        SystemProperty systemProperty = SystemProperty.getInstance();
        String persistResult = systemProperty.get(key);
        if (persistResult != null) {
            systemProperty.remove(key);
        }
    }

    public static DeviceAdapter getAdapter(Device device, AdapterManager adapterManager) {
        return adapterManager.getDeviceAdapter(new DeviceAdapterId(
                device.getDeviceManagement().getDeviceType(), device.getDeviceManagement().getDeviceInterfaceVersion(),
                device.getDeviceManagement().getDeviceModel(), device.getDeviceManagement().getDeviceVendor()));
    }

    public static EditConfigRequest getDefaultEditReq(Device device, AdapterManager adapterManager) {
        return adapterManager.getEditRequestForAdapter(new DeviceAdapterId(
                device.getDeviceManagement().getDeviceType(), device.getDeviceManagement().getDeviceInterfaceVersion(),
                device.getDeviceManagement().getDeviceModel(), device.getDeviceManagement().getDeviceVendor()));
    }

    private static void createLogFile() throws IOException {
        File logFile = new File(LOG_FILE_PATH);
        if (!logFile.exists()) {
            Boolean result = logFile.createNewFile();
            LOGGER.info(String.format("creation of file:%s in path %s result %s", LOG_FILE, LOG_FILE_FOLDER, result));
        }
    }

    public static void logErrorToFile(String exception, DeviceAdapter adapter) throws IOException {
        createLogFile();
        DeviceAdapterId adapterId = new DeviceAdapterId(adapter.getType(), adapter.getInterfaceVersion(),
                adapter.getModel(), adapter.getVendor());
        final DateTime now = DateTime.now();
        String timeStamp = now.withZone(DateTimeZone.UTC).toString();
        String eventLog = timeStamp + ":" + exception + ":" + adapterId.toString();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            LOGGER.info(String.format("Logging Exception to file : %s ", LOG_FILE));
            bw.append(eventLog);
            bw.append(System.lineSeparator());
            bw.newLine();
        }
    }

    private static boolean isStandardAdapterDeployed(AdapterManager adapterManager, String givenVendorAdapterType,
                                                     String stdAdapterInterfaceVersion) {
        boolean stdAdapterInstalled = false;
        String stdAdapterKey = givenVendorAdapterType.concat("-").concat(stdAdapterInterfaceVersion);
        if (adapterManager.getStdAdapterContextRegistry().containsKey(stdAdapterKey)) {
            stdAdapterInstalled = true;
        }
        return stdAdapterInstalled;
    }

}
