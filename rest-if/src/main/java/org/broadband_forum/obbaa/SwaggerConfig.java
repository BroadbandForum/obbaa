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

package org.broadband_forum.obbaa;

import java.util.Collections;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
public final class SwaggerConfig {
    private SwaggerConfig(){

    }

    public static Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.broadband_forum.obbaa"))
                .paths(input -> input.matches("/baa/.*"))
                .build()
                .apiInfo(apiInfo());
    }

    private static ApiInfo apiInfo() {
        return new ApiInfo(
                "BBF OBBAA REST API",
                "REST API providing access to OBBAA core component",
                "1.0", "https://www.broadband-forum.org/",
                new Contact("Broadband Forum", "https://www.broadband-forum.org", "info@broadband-forum.org"),
                "Apache License, Version 2.0", "http://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
    }
}