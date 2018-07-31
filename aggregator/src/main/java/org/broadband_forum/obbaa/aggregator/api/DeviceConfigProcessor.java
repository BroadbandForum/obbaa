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

package org.broadband_forum.obbaa.aggregator.api;

/**
 * Implemented by PMA for device service configuration request process.
 * Device configuration is mainly used for service configuration like VLAN etc.
 */
public interface DeviceConfigProcessor {
    /**
     * Provide a unified API for netconfRequest processing from Aggregator.
     *
     * @param deviceName     Unique identifier of the device
     * @param netconfRequest Message of NetConf request etc
     * @return Result
     * @throws DispatchException exception
     */
    String processRequest(String deviceName, String netconfRequest) throws DispatchException;
}
