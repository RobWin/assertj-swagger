/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.lang3.Validate;

import java.io.IOException;

public class Swagger20Parser {

    public static Swagger parse(String swagger) throws IOException {
        Validate.notEmpty(swagger, "swagger String must not be empty!");
        return convertToSwagger(swagger);
    }

    private static Swagger convertToSwagger(String data) throws IOException {
        ObjectMapper mapper;
        if (data.trim().startsWith("{")) {
            mapper = Json.mapper();
        } else {
            mapper = Yaml.mapper();
        }
        JsonNode rootNode = mapper.readTree(data);
        // must have swagger node set
        JsonNode swaggerNode = rootNode.get("swagger");
        if (swaggerNode == null) {
            throw new IllegalArgumentException("Swagger String has an invalid format.");
        } else {
            return mapper.convertValue(rootNode, Swagger.class);
        }
    }
}
