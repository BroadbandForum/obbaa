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

package org.broadband_forum.obbaa.device.adapter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

public final class PyangValidatorUtil {

    private PyangValidatorUtil() {
    }

    public static List<String> runPyang(String pyangCmd) {
        try {
            Process execProcess = Runtime.getRuntime().exec(new String[]{"bash", "-c", pyangCmd});
            execProcess.waitFor();
            InputStream errorStream = execProcess.getErrorStream();
            return IOUtils.readLines(errorStream, "UTF-8");
        } catch (IOException | InterruptedException e) {
            return Collections.singletonList(e.getMessage());
        }
    }

}
