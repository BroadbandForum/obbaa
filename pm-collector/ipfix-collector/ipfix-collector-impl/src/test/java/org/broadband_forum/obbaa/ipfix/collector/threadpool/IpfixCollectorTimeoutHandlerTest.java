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

package org.broadband_forum.obbaa.ipfix.collector.threadpool;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class IpfixCollectorTimeoutHandlerTest {

    @Test
    public void testTimeoutHandler() {
        IpfixCollectorTimeoutHandler handler = new IpfixCollectorTimeoutHandler();
        assertEquals(60 * 60 * 1000, handler.getReaderIdleTimeInMillis());

        handler = new IpfixCollectorTimeoutHandler(10, TimeUnit.SECONDS);
        assertEquals(10 * 1000, handler.getReaderIdleTimeInMillis());
    }

}
