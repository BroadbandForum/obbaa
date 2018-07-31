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

package org.broadband_forum.obbaa.connectors.sbi.netconf.impl;

public final class SystemPropertyUtils {
    private static SystemPropertyUtils INSTANCE = new SystemPropertyUtils();

    private SystemPropertyUtils() {

    }

    public static SystemPropertyUtils getInstance() {
        return INSTANCE;
    }

    /**
     * For unit tests.
     * Caution: if this method used, one should make sure to reset the instance upon test teardown.
     * Or extend your UT from SystemPropertyUtilsUserTest
     *
     * @param instance - instance to be set
     */
    static void setInstance(SystemPropertyUtils instance) {
        INSTANCE = instance;
    }

    /**
     * Variant of {@link #getFromEnvOrSysProperty(String, String)} where the {@literal defaultValue} is {@literal NULL}.
     *
     * @param name Name of the Environment Variable / System Property
     */
    public String getFromEnvOrSysProperty(String name) {
        return getFromEnvOrSysProperty(name, null);
    }

    /**
     * Attempts to read the value of an Environment Variable and return. If the environment doesn't have that
     * variable, attempts to fall
     * back to System Property. Returns the {@literal defaultValue} if there is no such System property.
     *
     * @param name         Name of the Environment Variable / System Property
     * @param defaultValue Default Value to return upon unavailability of Env Variable / System Property.
     */
    public String getFromEnvOrSysProperty(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name, null);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Variant of {@link #getFromEnvOrSysProperty(String, String)} which throws {@link IllegalArgumentException}
     * instead of returning
     * {@literal defaultValue}.
     *
     * @param name Name of the Environment Variable / System Property
     */
    public String readFromEnvOrSysProperty(String name) {
        String value = getFromEnvOrSysProperty(name);
        if (value == null) {
            throw new IllegalArgumentException("name=" + name);
        }
        return value;
    }

}
