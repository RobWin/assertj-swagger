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

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Map;

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
        validateSwagger(expected);

        return myself;
    }


    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expectedLocation the location of the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert isEqualTo(String expectedLocation) {
        // Check Paths
        validateSwagger(new SwaggerParser().read(expectedLocation));
        return myself;
    }

    private void validateSwagger(Swagger expected) {
        // Check Paths
        validatePaths(actual.getPaths(), expected.getPaths());

        // Check Definitions
        validateDefinitions(actual.getDefinitions(), expected.getDefinitions());

        softAssertions.assertAll();
    }


    private void validateDefinitions(Map<String, Model> actualDefinitions, Map<String, Model> expectedDefinitions) {
        if(MapUtils.isNotEmpty(expectedDefinitions)) {
            softAssertions.assertThat(actualDefinitions).as("Checking Definitions").isNotEmpty();
            if(MapUtils.isNotEmpty(actualDefinitions)){
                softAssertions.assertThat(actualDefinitions.keySet()).as("Checking Definitions").hasSameElementsAs(expectedDefinitions.keySet());
            }
        }else{
            softAssertions.assertThat(actualDefinitions).as("Checking Definitions").isNullOrEmpty();
        }
    }

    private void validatePaths(Map<String, Path> actualPaths, Map<String, Path> expectedPaths) {
        if(MapUtils.isNotEmpty(expectedPaths)) {
            softAssertions.assertThat(actualPaths).as("Checking Paths").isNotEmpty();
            softAssertions.assertThat(actualPaths.keySet()).as("Checking Paths").hasSameElementsAs(expectedPaths.keySet());
            for (Map.Entry<String, Path> actualPathEntry : actualPaths.entrySet()) {
                Path expectedPath = expectedPaths.get(actualPathEntry.getKey());
                Path actualPath = actualPathEntry.getValue();
                String pathName = actualPathEntry.getKey();
                validatePath(pathName, actualPath, expectedPath);
            }
        }else{
            softAssertions.assertThat(actualPaths).as("Checking Paths").isNullOrEmpty();
        }
    }

    private void validatePath(String pathName, Path actualPath, Path expectedPath) {
        if (expectedPath != null) {
            softAssertions.assertThat(actualPath.getOperations()).as("Checking number of operations of path '%s'", pathName).hasSameSizeAs(actualPath.getOperations());
            validateOperation(actualPath.getGet(), expectedPath.getGet(), pathName, "GET");
            validateOperation(actualPath.getDelete(), expectedPath.getDelete(), pathName, "DELETE");
            validateOperation(actualPath.getPost(), expectedPath.getPost(), pathName, "POST");
            validateOperation(actualPath.getPut(), expectedPath.getPut(), pathName, "PUT");
            validateOperation(actualPath.getPatch(), expectedPath.getPatch(), pathName, "PATCH");
            validateOperation(actualPath.getOptions(), expectedPath.getOptions(), pathName, "OPTIONS");
        }
    }

    private void validateOperation(Operation actualOperation, Operation expectedOperation, String path, String httpMethod){
        String message = String.format("Checking '%s' operation of path '%s'", httpMethod, path);
        if(expectedOperation != null){
            softAssertions.assertThat(actualOperation).as(message).isNotNull();
            if(actualOperation != null) {
                //Validate consumes
                validateList(actualOperation.getConsumes(), expectedOperation.getConsumes(),  String.format("Checking '%s' of '%s' operation of path '%s'", "consumes", httpMethod, path));
                //Validate produces
                validateList(actualOperation.getProduces(), expectedOperation.getProduces(),  String.format("Checking '%s' of '%s' operation of path '%s'", "produces", httpMethod, path));
                //Validate parameters
                validateParameters(actualOperation.getParameters(), expectedOperation.getParameters(),  String.format("Checking '%s' of '%s' operation of path '%s'", "parameters", httpMethod, path));
                //Validate responses
                validateResponses(actualOperation.getResponses(), expectedOperation.getResponses(),  String.format("Checking '%s' of '%s' operation of path '%s'", "responses", httpMethod, path));
            }
        }else{
            softAssertions.assertThat(actualOperation).as(message).isNull();
        }
    }

    private void validateParameters(List<Parameter> actualOperationParameters,  List<Parameter> expectedOperationParametersParameters, String message) {
        if(CollectionUtils.isNotEmpty(expectedOperationParametersParameters)) {
            softAssertions.assertThat(actualOperationParameters).as(message).isNotEmpty();
            if(CollectionUtils.isNotEmpty(actualOperationParameters)) {
                softAssertions.assertThat(actualOperationParameters).as(message).hasSameSizeAs(expectedOperationParametersParameters);
                softAssertions.assertThat(actualOperationParameters).as(message).usingElementComparatorOnFields("in", "name", "required").hasSameElementsAs(expectedOperationParametersParameters);
            }
        }else{
            softAssertions.assertThat(actualOperationParameters).as(message).isNullOrEmpty();
        }
    }

    private void validateResponses(Map<String, Response> actualOperationResponses, Map<String, Response> expectedOperationResponses, String message) {
        if(MapUtils.isNotEmpty(expectedOperationResponses)) {
            softAssertions.assertThat(actualOperationResponses).as(message).isNotEmpty();
            if(MapUtils.isNotEmpty(actualOperationResponses)) {
                softAssertions.assertThat(actualOperationResponses).as(message).hasSameSizeAs(expectedOperationResponses);


            }
        }else{
            softAssertions.assertThat(actualOperationResponses).as(message).isNullOrEmpty();
        }

    }

    private void validateList(List<String> actualList, List<String> expectedList, String message){
        if(CollectionUtils.isNotEmpty(expectedList)) {
            softAssertions.assertThat(actualList).as(message).isNotEmpty();
            if(CollectionUtils.isNotEmpty(actualList)) {
                softAssertions.assertThat(actualList).as(message).hasSameElementsAs(expectedList);
            }
        }else{
            softAssertions.assertThat(actualList).as(message).isNullOrEmpty();
        }
    }
}