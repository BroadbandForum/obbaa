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

package org.broadband_forum.obbaa.store.dm;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DeviceInfoTest {

    @Test
    public void testIsCallHomeReturnsTrue_WhenDeviceIsCallHome() {
        DeviceInfo callHomeInfo = getCallHomeDevice();
        assertTrue(callHomeInfo.isCallHome());
    }

    @NotNull
    private DeviceInfo getCallHomeDevice() {
        return new DeviceInfo("Banglaore-North", new CallHomeInfo("1234-4562"));
    }

    @Test
    public void testIsCallHomeReturnsFalse_WhenDeviceIsConvertedDirect() {
        DeviceInfo callHomeInfo = getCallHomeDevice();
        callHomeInfo.setDeviceConnectionInfo(new SshConnectionInfo("127.0.0.1", 234, "user", "password"));
        assertFalse(callHomeInfo.isCallHome());
    }

    @Test
    public void testIsCallHomeReturnsFalse_WhenDeviceIsDirect() {
        DeviceInfo callHomeInfo = getDirectDevice();
        assertFalse(callHomeInfo.isCallHome());
    }

    @NotNull
    private DeviceInfo getDirectDevice() {
        return new DeviceInfo("Bangalore-North", new SshConnectionInfo("127.0.0.1", 234, "user", "password"));
    }

    @Test
    public void testIsCallHomeReturnsTrue_WhenDeviceIsConvertedCallHome() {
        DeviceInfo callHomeInfo = getDirectDevice();
        callHomeInfo.setDeviceCallHomeInfo(new CallHomeInfo("1234"));
        assertTrue(callHomeInfo.isCallHome());
    }
}
