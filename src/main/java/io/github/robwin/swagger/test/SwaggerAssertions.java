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
package io.github.robwin.swagger.test;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.Validate;

/**
 * Entry point for assertion methods for different data types. Each method in this class is a static factory for the
 * type-specific assertion objects. The purpose of this class is to make test code more readable.
 *
 * @author Robert Winkler
 */
public class SwaggerAssertions {

    /**
     * Creates a new instance of <code>{@link SwaggerAssert}</code>.
     *
     * @param actual the the actual Swagger value.
     * @return the created assertion object.
     */
    public static SwaggerAssert assertThat(Swagger actual) {
        Validate.notNull(actual, "actual must not be null!");
        return new SwaggerAssert(actual);
    }


    /**
     * Creates a new instance of <code>{@link SwaggerAssert}</code>.
     *
     * @param actualLocation the location the actual Swagger value.
     * @return the created assertion object.
     */
    public static SwaggerAssert assertThat(String actualLocation) {
        Validate.notNull(actualLocation, "actualLocation must not be null!");
        return new SwaggerAssert(new SwaggerParser().read(actualLocation));
    }
}
