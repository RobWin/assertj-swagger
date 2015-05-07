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

import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion methods for {@code Swagger}.
 * <p>
 * To create a new instance of this class, invoke <code>{@link io.github.robwin.swagger.test.SwaggerAssertions#assertThat(Swagger)}</code>.
 * </p>
 *
 * @author Robert Winkler
 */

public class SwaggerAssert extends AbstractAssert<SwaggerAssert, Swagger> {

    private SoftAssertions softAssertions;

    public SwaggerAssert(Swagger actual) {
        super(actual, SwaggerAssert.class);
        softAssertions = new SoftAssertions();
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expected the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert isEqualTo(Swagger expected) {
        // Check Paths
        Map<String, Path> expectedPaths = expected.getPaths();
        if(MapUtils.isNotEmpty(expectedPaths)) {
            Map<String, Path> actualPaths = actual.getPaths();
            assertThat(actualPaths).isNotEmpty();

            softAssertions.assertThat(actualPaths.keySet()).as("Checking Paths").hasSameElementsAs(expected.getPaths().keySet());

            for (Map.Entry<String, Path> actualPathEntry : actualPaths.entrySet()) {
                Path expectedPath = expectedPaths.get(actualPathEntry.getKey());
                if (expectedPath != null) {
                    Path actualPath = actualPathEntry.getValue();
                    softAssertions.assertThat(actualPath.getOperations()).as("Checking number of operations of path '%s'", actualPathEntry.getKey()).hasSameSizeAs(actualPath.getOperations());
                    validateOperation(actualPath.getGet(), expectedPath.getGet(), String.format("Checking GET operation of path '%s'", actualPathEntry.getKey()));
                    validateOperation(actualPath.getDelete(), expectedPath.getDelete(), String.format("Checking DELETE operation of path '%s'", actualPathEntry.getKey()));
                    validateOperation(actualPath.getPost(), expectedPath.getPost(), String.format("Checking POST operation of path '%s'", actualPathEntry.getKey()));
                    validateOperation(actualPath.getPut(), expectedPath.getPut(), String.format("Checking PUT operation of path '%s'", actualPathEntry.getKey()));
                    validateOperation(actualPath.getPatch(), expectedPath.getPatch(), String.format("Checking PATCH operation of path '%s'", actualPathEntry.getKey()));
                    validateOperation(actualPath.getOptions(), expectedPath.getOptions(), String.format("Checking OPTIONS operation of path '%s'", actualPathEntry.getKey()));
                }
            }
        }


        Map<String, Model> expectedDefinitions = expected.getDefinitions();
        if(MapUtils.isNotEmpty(expectedDefinitions)) {
            Map<String, Model> actualDefinitions = actual.getDefinitions();
            assertThat(actualDefinitions).isNotEmpty();

            // Check Definitions
            softAssertions.assertThat(actualDefinitions.keySet()).as("Checking Definitions").hasSameElementsAs(expectedDefinitions.keySet());
        }
        softAssertions.assertAll();

        return myself;
    }

    private void validateOperation(Operation actualOperation, Operation expectedOperation, String message){
        if(expectedOperation != null){

            // TODO CHECK EMPTY

            softAssertions.assertThat(actualOperation.getConsumes()).as(message).isEqualTo(expectedOperation.getConsumes());


            softAssertions.assertThat(actualOperation.getProduces()).as(message).isEqualTo(expectedOperation.getProduces());


            // TODO VALIDATE PARAMETERS AND RESPONSES
        }else{
            softAssertions.assertThat(actualOperation).as(message).isNull();
        }
    }
}