/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu;

import java.util.concurrent.CompletableFuture;

import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;

/**
 * <p>
 * CompletableFuture response along with timestamp
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class TimestampFutureResponse extends CompletableFuture<NetConfResponse> {
    private final long m_timeStamp;

    public TimestampFutureResponse() {
        m_timeStamp = System.currentTimeMillis();
    }

    // This constructor must only be used for testing timed out responses
    public TimestampFutureResponse(long timeStamp) {
        m_timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return m_timeStamp;
    }

    public boolean isResponseExpired() {
        if (Math.abs(System.currentTimeMillis() - m_timeStamp) >= AbstractNetconfClientSession.DEFAULT_MESSAGE_TIMEOUT) {
            return true;
        }
        return false;
    }
}
