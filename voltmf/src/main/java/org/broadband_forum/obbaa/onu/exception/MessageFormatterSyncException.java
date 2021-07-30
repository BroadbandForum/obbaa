/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu.exception;

/**
 * <p>
 * Used when the configuration is already synchronous
 * </p>
 * Created by Filipe Cláudio (Altice Labs) on 09/06/2021.
 */
public class MessageFormatterSyncException extends MessageFormatterException {

    public MessageFormatterSyncException(String message) {
        super(message);
    }

    public MessageFormatterSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
