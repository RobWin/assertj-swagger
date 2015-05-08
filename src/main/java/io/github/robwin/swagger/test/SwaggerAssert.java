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
import com.wordnik.swagger.models.properties.Property;
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

    private void validatePaths(Map<String, Path> actualPaths, Map<String, Path> expectedPaths) {
        if(MapUtils.isNotEmpty(expectedPaths)) {
            softAssertions.assertThat(actualPaths).as("Checking Paths").isNotEmpty();
            if(MapUtils.isNotEmpty(actualPaths)){
                softAssertions.assertThat(actualPaths.keySet()).as("Checking Paths").hasSameElementsAs(expectedPaths.keySet());
                for (Map.Entry<String, Path> actualPathEntry : actualPaths.entrySet()) {
                    Path expectedPath = expectedPaths.get(actualPathEntry.getKey());
                    Path actualPath = actualPathEntry.getValue();
                    String pathName = actualPathEntry.getKey();
                    validatePath(pathName, actualPath, expectedPath);
                }
            }
        }else{
            softAssertions.assertThat(actualPaths).as("Checking Paths").isNullOrEmpty();
        }
    }

    private void validateDefinitions(Map<String, Model> actualDefinitions, Map<String, Model> expectedDefinitions) {
        if(MapUtils.isNotEmpty(expectedDefinitions)) {
            softAssertions.assertThat(actualDefinitions).as("Checking Definitions").isNotEmpty();
            if(MapUtils.isNotEmpty(actualDefinitions)){
                softAssertions.assertThat(actualDefinitions.keySet()).as("Checking Definitions").hasSameElementsAs(expectedDefinitions.keySet());
                for (Map.Entry<String, Model> actualDefinitionEntry : actualDefinitions.entrySet()) {
                    Model expectedDefinition = expectedDefinitions.get(actualDefinitionEntry.getKey());
                    Model actualDefinition = actualDefinitionEntry.getValue();
                    String definitionName = actualDefinitionEntry.getKey();
                    validateDefinition(definitionName, actualDefinition, expectedDefinition);
                }
            }
        }else{
            softAssertions.assertThat(actualDefinitions).as("Checking Definitions").isNullOrEmpty();
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

    private void validateDefinition(String definitionName, Model actualDefinition, Model expectedDefinition) {
        if (expectedDefinition != null) {
            // TODO Validate Definition
            validateDefinitionProperties(actualDefinition.getProperties(), expectedDefinition.getProperties(), definitionName);
        }
    }

    private void validateDefinitionProperties(Map<String, Property> actualDefinitionProperties, Map<String, Property> expectedDefinitionProperties, String definitionName) {
        if(MapUtils.isNotEmpty(expectedDefinitionProperties)) {
            softAssertions.assertThat(actualDefinitionProperties).as("Checking properties of definition '%s", definitionName).isNotEmpty();
            if(MapUtils.isNotEmpty(actualDefinitionProperties)){
                softAssertions.assertThat(actualDefinitionProperties.keySet()).as("Checking number of properties of definition '%s'", definitionName).hasSameElementsAs(expectedDefinitionProperties.keySet());
                for (Map.Entry<String, Property> actualDefinitionPropertyEntry : actualDefinitionProperties.entrySet()) {
                    Property expectedDefinitionProperty = expectedDefinitionProperties.get(actualDefinitionPropertyEntry.getKey());
                    Property actualDefinitionProperty = actualDefinitionPropertyEntry.getValue();
                    String propertyName = actualDefinitionPropertyEntry.getKey();
                    validateProperty(actualDefinitionProperty, expectedDefinitionProperty, String.format("Checking property '%s' of definition '%s'", propertyName, definitionName));
                }
            }
        }else{
            softAssertions.assertThat(actualDefinitionProperties).as("Checking properties of definition '%s", definitionName).isNullOrEmpty();
        }
    }

    private void validateProperty(Property actualProperty, Property expectedProperty, String message) {
        // TODO Validate Property schema
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
                validateResponses(actualOperation.getResponses(), expectedOperation.getResponses(), httpMethod, path);
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
                // TODO Validate Parameter schema
                softAssertions.assertThat(actualOperationParameters).as(message).usingElementComparatorOnFields("in", "name", "required").hasSameElementsAs(expectedOperationParametersParameters);
            }
        }else{
            softAssertions.assertThat(actualOperationParameters).as(message).isNullOrEmpty();
        }
    }

    private void validateResponses(Map<String, Response> actualOperationResponses, Map<String, Response> expectedOperationResponses, String httpMethod, String path) {
        String message = String.format("Checking responses of '%s' operation of path '%s'", httpMethod, path);
        if(MapUtils.isNotEmpty(expectedOperationResponses)) {
            softAssertions.assertThat(actualOperationResponses).as(message).isNotEmpty();
            if(MapUtils.isNotEmpty(actualOperationResponses)) {
                softAssertions.assertThat(actualOperationResponses.keySet()).as(message).hasSameElementsAs(expectedOperationResponses.keySet());
                for (Map.Entry<String, Response> actualResponseEntry : actualOperationResponses.entrySet()) {
                    Response expectedResponse = expectedOperationResponses.get(actualResponseEntry.getKey());
                    Response actualResponse = actualResponseEntry.getValue();
                    String responseName = actualResponseEntry.getKey();
                    validateResponse( actualResponse, expectedResponse, responseName, httpMethod, path);
                }
            }
        }else{
            softAssertions.assertThat(actualOperationResponses).as(message).isNullOrEmpty();
        }
    }

    private void validateResponse(Response actualResponse, Response expectedResponse, String responseName, String httpMethod, String path) {
        if (expectedResponse != null) {
            validateProperty(actualResponse.getSchema(), expectedResponse.getSchema(), String.format("Checking response schema of response '%s' of '%s' operation of path '%s'", responseName, httpMethod, path));
            validateResponseHeaders(actualResponse.getHeaders(), expectedResponse.getHeaders(), responseName, httpMethod, path);
        }
    }

    private void validateResponseHeaders(Map<String, Property> actualResponseHeaders, Map<String, Property> expectedResponseHeaders, String responseName, String httpMethod, String path) {
        String message = String.format("Checking response headers of response '%s' of '%s' operation of path '%s'", responseName, httpMethod, path);
        if(MapUtils.isNotEmpty(expectedResponseHeaders)) {
            softAssertions.assertThat(actualResponseHeaders).as(message).isNotEmpty();
            if(MapUtils.isNotEmpty(actualResponseHeaders)){
                softAssertions.assertThat(actualResponseHeaders.keySet()).as(message).hasSameElementsAs(expectedResponseHeaders.keySet());

                for (Map.Entry<String, Property> actualResponseHeaderEntry : actualResponseHeaders.entrySet()) {
                    Property expectedResponseHeader = expectedResponseHeaders.get(actualResponseHeaderEntry.getKey());
                    Property actualResponseHeader = actualResponseHeaderEntry.getValue();
                    String responseHeaderName = actualResponseHeaderEntry.getKey();
                    validateProperty(actualResponseHeader, expectedResponseHeader, String.format("Checking response header '%s' of response '%s' of '%s' operation of path '%s'", responseHeaderName, responseName, httpMethod, path));
                }


                // TODO Validate Response header schema property
            }
        }else{
            softAssertions.assertThat(actualResponseHeaders).as(message).isNullOrEmpty();
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