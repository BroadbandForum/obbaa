/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.nbiadapter.netconf;

public final class NbiConstants {

    private NbiConstants() {}

    public static final String DEVICE = "device";
    public static final String NAME = "name";
    public static final String ROOT = "root";
    public static final String RPC_ERROR = "rpc-error";
    public static final String ERROR_TYPE = "error-type";
    public static final String ERROR_TAG = "error-tag";
    public static final String ERROR_SEVERITY = "error-severity";
    public static final String ERROR_MESSAGE = "error-message";
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:netconf:base:1.0";
    public static final String APPLICATION = "application";
    public static final String OPERATION_FAILED = "operation-failed";
    public static final String ERROR = "error";
    public static final String TIMEOUT_MESSAGE = "Timed out while getting response from VOLT-MF";
}