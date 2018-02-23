/*
 *
 *  Copyright 2018 Robert Winkler
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
package io.github.robwin.swagger.test;

/**
 * Flags to enable or disable various assertion types are either a coarse- or fine-grained level.
 */
public enum SwaggerAssertionType {

    INFO("validateInfo", false),
    VERSION("validateVersion", false),

    DEFINITIONS("validateDefinitions", true),
        PROPERTIES("validateProperties", true),
            REF_PROPERTIES("validateRefProperties", true),
            ARRAY_PROPERTIES("validateArrayProperties", true),
            STRING_PROPERTIES("validateStringProperties", true),
        MODELS("validateModels", true),
    PATHS("validatePaths", true);

    private String suffix;
    private boolean enabledByDefault;

    SwaggerAssertionType(final String assertionType, final boolean defaultValue) {
        this.suffix = assertionType;
        this.enabledByDefault = defaultValue;
    }

    public String getBarePropertyName() {
        return suffix;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }
}
