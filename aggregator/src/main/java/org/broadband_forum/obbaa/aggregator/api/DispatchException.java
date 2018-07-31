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
 * The exception during the progress of message process by Aggregator.
 */
public class DispatchException extends Exception {
    public static final  String NOT_SUPPORT = "Do not support the operation";

    private final String m_errorMessage;

    public DispatchException(String message) {
        super(message);
        m_errorMessage = String.format("Aggregator error: %s", message);
    }

    public DispatchException(Throwable ex) {
        super(ex);
        m_errorMessage = String.format("Aggregator error: %s", ex.getMessage());
    }

    public static DispatchException buildNotSupport() {
        return new DispatchException(NOT_SUPPORT);
    }

    public static void assertNull(Object object, String error) throws DispatchException {
        if (object == null) {
            throw new DispatchException(error);
        }
    }

    public static void assertNull(Object object, Throwable error) throws DispatchException {
        if (object == null) {
            throw new DispatchException(error);
        }
    }

    public static void assertZero(int value, String error) throws DispatchException {
        if (value == 0) {
            throw new DispatchException(error);
        }
    }

    @Override
    public String getMessage() {
        return m_errorMessage;
    }
}
