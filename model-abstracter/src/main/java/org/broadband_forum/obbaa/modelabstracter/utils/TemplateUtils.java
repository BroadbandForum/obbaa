/*
 *   Copyright 2022 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.broadband_forum.obbaa.modelabstracter.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Process Template with FreeMarker.
 */
@Slf4j
public final class TemplateUtils {
    private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_28);

    private TemplateUtils() {
        // hide constructor
    }

    static {
        init();
    }

    private static void init() {
        log.info("start to init template configuration");
        try {
            CFG.setClassLoaderForTemplateLoading(TemplateUtils.class.getClassLoader(), "template");
            CFG.setDefaultEncoding(StandardCharsets.UTF_8.name());
            CFG.setLocale(Locale.ROOT);
            CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        } catch (Exception e) {
            log.error("init template configuration error", e);
        }
    }

    /**
     * Filled the template with input values.
     *
     * @param values some values in the template
     * @param templateName template's name
     * @return template's String with values
     */
    public static String processTemplate(Map<String, Object> values, String templateName)
        throws IOException, TemplateException {
        Template template = CFG.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(values, writer);
        String msg = writer.toString();
        log.info("process template msg, values:{}, templateName:{}, msg:{}", values, templateName, msg);
        return msg;
    }
}
