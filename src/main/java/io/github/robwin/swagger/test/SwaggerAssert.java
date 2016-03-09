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

import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


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

    private static final String ASSERTION_ENABLED_CONFIG_PATH = "/assertj-swagger.properties";
    private SwaggerAssertionConfig assertionConfig;

    private AttributeResolver attributeResolver;   // provide means to fall back from local to global properties


    public SwaggerAssert(Swagger actual) {
        super(actual, SwaggerAssert.class);
        softAssertions = new SoftAssertions();
        assertionConfig = loadSwaggerAssertionFlagsConfiguration(ASSERTION_ENABLED_CONFIG_PATH);
    }

    public SwaggerAssert(Swagger actual, SwaggerAssertionConfig assertionConfig) {
        this(actual);
        this.assertionConfig = assertionConfig;
    }

    public SwaggerAssert(Swagger actual, String configurationResourceLocation) {
        this(actual);
        this.assertionConfig = loadSwaggerAssertionFlagsConfiguration(configurationResourceLocation);
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expected the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert isEqualTo(Swagger expected) {
        attributeResolver = new AttributeResolver(expected, actual);
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
        return isEqualTo(new SwaggerParser().read(expectedLocation));
    }

    private void validateSwagger(Swagger expected) {
        // Check Paths
        if (isAssertionEnabled(SwaggerAssertionType.PATHS)) {
            final Set<String> filter = assertionConfig.getPathsToIgnoreInExpected();
            final Map<String, Path> expectedPaths = adjustExpectedPathsWithPrefix(expected.getPaths(), assertionConfig.getPathsPrependExpected());
            validatePaths(actual.getPaths(), removeAllFromMap(expectedPaths, filter));
        }

        // Check Definitions
        if (isAssertionEnabled(SwaggerAssertionType.DEFINITIONS)) {
            final Set<String> filter = assertionConfig.getDefinitionsToIgnoreInExpected();
            validateDefinitions(actual.getDefinitions(), removeAllFromMap(expected.getDefinitions(), filter));
        }

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
            validateModel(actualDefinition, expectedDefinition, String.format("Checking model of definition '%s", definitionName));
            validateDefinitionProperties(actualDefinition.getProperties(), expectedDefinition.getProperties(), definitionName);
        }
    }

    private void validateModel(Model actualDefinition, Model expectedDefinition, String message) {
        if (isAssertionEnabled(SwaggerAssertionType.MODELS)) {
        if (expectedDefinition instanceof ModelImpl) {
            // TODO Validate ModelImpl
            softAssertions.assertThat(actualDefinition).as(message).isExactlyInstanceOf(ModelImpl.class);
        } else if (expectedDefinition instanceof RefModel) {
            // TODO Validate RefModel
            softAssertions.assertThat(actualDefinition).as(message).isExactlyInstanceOf(RefModel.class);
        } else if (expectedDefinition instanceof ArrayModel) {
            ArrayModel arrayModel = ((ArrayModel) expectedDefinition);
            // TODO Validate ArrayModel
            softAssertions.assertThat(actualDefinition).as(message).isExactlyInstanceOf(ArrayModel.class);
        }else{
            // TODO Validate all model types
            softAssertions.assertThat(actualDefinition).isExactlyInstanceOf(expectedDefinition.getClass());
        }
    }
    }

    private void validateDefinitionProperties(Map<String, Property> actualDefinitionProperties, Map<String, Property> expectedDefinitionProperties, String definitionName) {
        if(MapUtils.isNotEmpty(expectedDefinitionProperties)) {
            softAssertions.assertThat(actualDefinitionProperties).as("Checking properties of definition '%s", definitionName).isNotEmpty();
            if(MapUtils.isNotEmpty(actualDefinitionProperties)){
                final Set<String> filteredExpectedProperties = filterWhitelistedPropertyNames(definitionName, expectedDefinitionProperties.keySet());
                softAssertions.assertThat(actualDefinitionProperties.keySet()).as("Checking properties of definition '%s'", definitionName).hasSameElementsAs(filteredExpectedProperties);
                for (Map.Entry<String, Property> actualDefinitionPropertyEntry : actualDefinitionProperties.entrySet()) {
                    Property expectedDefinitionProperty = expectedDefinitionProperties.get(actualDefinitionPropertyEntry.getKey());
                    Property actualDefinitionProperty = actualDefinitionPropertyEntry.getValue();
                    String propertyName = actualDefinitionPropertyEntry.getKey();
                    validateProperty(actualDefinitionProperty, expectedDefinitionProperty, String.format("Checking property '%s' of definition '%s'", propertyName, definitionName));
                }
            }
        } else {
            softAssertions.assertThat(actualDefinitionProperties).as("Checking properties of definition '%s", definitionName).isNullOrEmpty();
        }
    }

    private void validateProperty(Property actualProperty, Property expectedProperty, String message) {
        // TODO Validate Property schema
        if (expectedProperty != null && isAssertionEnabled(SwaggerAssertionType.PROPERTIES)) {
            if (expectedProperty instanceof RefProperty) {
                if (isAssertionEnabled(SwaggerAssertionType.REF_PROPERTIES)) {
                RefProperty refProperty = (RefProperty) expectedProperty;
                softAssertions.assertThat(actualProperty).as(message).isExactlyInstanceOf(RefProperty.class);
                // TODO Validate RefProperty
                }
            } else if (expectedProperty instanceof ArrayProperty) {
                if (isAssertionEnabled(SwaggerAssertionType.ARRAY_PROPERTIES)) {
                ArrayProperty arrayProperty = (ArrayProperty) expectedProperty;
                softAssertions.assertThat(actualProperty).as(message).isExactlyInstanceOf(ArrayProperty.class);
                // TODO Validate ArrayProperty
                }
            } else if (expectedProperty instanceof StringProperty) {
                if (isAssertionEnabled(SwaggerAssertionType.STRING_PROPERTIES)) {
                StringProperty expectedStringProperty = (StringProperty) expectedProperty;
                softAssertions.assertThat(actualProperty).as(message).isExactlyInstanceOf(StringProperty.class);
                // TODO Validate StringProperty
                if(actualProperty instanceof StringProperty){
                    StringProperty actualStringProperty = (StringProperty) expectedProperty;
                    List<String> expectedEnums = expectedStringProperty.getEnum();
                    if (CollectionUtils.isNotEmpty(expectedEnums)) {
                        softAssertions.assertThat(actualStringProperty.getEnum()).hasSameElementsAs(expectedEnums);
                    }else{
                        softAssertions.assertThat(actualStringProperty.getEnum()).isNullOrEmpty();
                    }
                }
                }
            } else {
                // TODO Validate all other properties
                softAssertions.assertThat(actualProperty).isExactlyInstanceOf(expectedProperty.getClass());
            }
        }

    }

    private void validateOperation(Operation actualOperation, Operation expectedOperation, String path, String httpMethod){
        String message = String.format("Checking '%s' operation of path '%s'", httpMethod, path);
        if(expectedOperation != null){
            softAssertions.assertThat(actualOperation).as(message).isNotNull();
            if(actualOperation != null) {
                //Validate consumes
                validateList(attributeResolver.getActualConsumes(actualOperation),
                             attributeResolver.getExpectedConsumes((expectedOperation)),
                             String.format("Checking '%s' of '%s' operation of path '%s'", "consumes", httpMethod, path));
                //Validate produces
                validateList(attributeResolver.getActualProduces(actualOperation),
                             attributeResolver.getExpectedProduces((expectedOperation)),
                             String.format("Checking '%s' of '%s' operation of path '%s'", "produces", httpMethod, path));
                //Validate parameters
                validateParameters(actualOperation.getParameters(), expectedOperation.getParameters(), httpMethod, path);
                //Validate responses
                validateResponses(actualOperation.getResponses(), expectedOperation.getResponses(), httpMethod, path);
            }
        }else{
            softAssertions.assertThat(actualOperation).as(message).isNull();
        }
    }

    private void validateParameters(List<Parameter> actualOperationParameters,  List<Parameter> expectedOperationParameters, String httpMethod, String path) {
        String message = String.format("Checking parameters of '%s' operation of path '%s'", httpMethod, path);
        if(CollectionUtils.isNotEmpty(expectedOperationParameters)) {
            softAssertions.assertThat(actualOperationParameters).as(message).isNotEmpty();
            if(CollectionUtils.isNotEmpty(actualOperationParameters)) {
                softAssertions.assertThat(actualOperationParameters).as(message).hasSameSizeAs(expectedOperationParameters);
                //softAssertions.assertThat(actualOperationParameters).as(message).usingElementComparatorOnFields("in", "name", "required").hasSameElementsAs(expectedOperationParametersParameters);
                Map<String,Parameter> expectedParametersAsMap = new HashMap<>();
                for(Parameter expectedParameter : expectedOperationParameters){
                    expectedParametersAsMap.put(expectedParameter.getName(), expectedParameter);
                }
                for(Parameter actualParameter : actualOperationParameters){
                    String parameterName = actualParameter.getName();
                    Parameter expectedParameter = expectedParametersAsMap.get(parameterName);
                    validateParameter(actualParameter, expectedParameter, parameterName, httpMethod, path);
                }
            }
        }else{
            softAssertions.assertThat(actualOperationParameters).as(message).isNullOrEmpty();
        }
    }

    private void validateParameter(Parameter actualParameter, Parameter expectedParameter, String parameterName, String httpMethod, String path) {
        if(expectedParameter != null) {
            String message = String.format("Checking parameter '%s' of '%s' operation of path '%s'", parameterName, httpMethod, path);
            softAssertions.assertThat(actualParameter).as(message).isExactlyInstanceOf(expectedParameter.getClass());
            if (expectedParameter instanceof BodyParameter && actualParameter instanceof BodyParameter) {
                BodyParameter actualBodyParameter = (BodyParameter) expectedParameter;
                BodyParameter expectedBodyParameter = (BodyParameter) expectedParameter;
                validateModel(actualBodyParameter.getSchema(), expectedBodyParameter.getSchema(), String.format("Checking model of parameter '%s' of '%s' operation of path '%s'", parameterName, httpMethod, path));
            } else if (expectedParameter instanceof PathParameter && actualParameter instanceof PathParameter) {
                PathParameter actualPathParameter = (PathParameter) actualParameter;
                PathParameter expectedPathParameter = (PathParameter) expectedParameter;
                softAssertions.assertThat(actualPathParameter.getType()).as(message).isEqualTo(expectedPathParameter.getType());
                List<String> expectedEnums = expectedPathParameter.getEnum();
                if (CollectionUtils.isNotEmpty(expectedEnums)) {
                    softAssertions.assertThat(actualPathParameter.getEnum()).as(message).hasSameElementsAs(expectedEnums);
                } else {
                    softAssertions.assertThat(actualPathParameter.getEnum()).as(message).isNullOrEmpty();
                }
            } else if (expectedParameter instanceof QueryParameter && actualParameter instanceof QueryParameter) {
                QueryParameter actualQueryParameter = (QueryParameter) actualParameter;
                QueryParameter expectedQueryParameter = (QueryParameter) expectedParameter;
                softAssertions.assertThat(actualQueryParameter.getType()).as(message).isEqualTo(expectedQueryParameter.getType());
                List<String> expectedEnums = expectedQueryParameter.getEnum();
                if (CollectionUtils.isNotEmpty(expectedEnums)) {
                    softAssertions.assertThat(actualQueryParameter.getEnum()).as(message).hasSameElementsAs(expectedEnums);
                } else {
                    softAssertions.assertThat(actualQueryParameter.getEnum()).as(message).isNullOrEmpty();
                }
            } else if (expectedParameter instanceof HeaderParameter && actualParameter instanceof HeaderParameter) {
                HeaderParameter actualHeaderParameter = (HeaderParameter) actualParameter;
                HeaderParameter expectedHeaderParameter = (HeaderParameter) expectedParameter;
                softAssertions.assertThat(actualHeaderParameter.getType()).as(message).isEqualTo(expectedHeaderParameter.getType());
                List<String> expectedEnums = expectedHeaderParameter.getEnum();
                if (CollectionUtils.isNotEmpty(expectedEnums)) {
                    softAssertions.assertThat(actualHeaderParameter.getEnum()).as(message).hasSameElementsAs(expectedEnums);
                } else {
                    softAssertions.assertThat(actualHeaderParameter.getEnum()).as(message).isNullOrEmpty();
                }
            } else if (expectedParameter instanceof FormParameter && actualParameter instanceof FormParameter) {
                FormParameter actualFormParameter = (FormParameter) actualParameter;
                FormParameter expectedFormParameter = (FormParameter) expectedParameter;
                softAssertions.assertThat(actualFormParameter.getType()).as(message).isEqualTo(expectedFormParameter.getType());
                List<String> expectedEnums = expectedFormParameter.getEnum();
                if (CollectionUtils.isNotEmpty(expectedEnums)) {
                    softAssertions.assertThat(actualFormParameter.getEnum()).as(message).hasSameElementsAs(expectedEnums);
                } else {
                    softAssertions.assertThat(actualFormParameter.getEnum()).as(message).isNullOrEmpty();
                }
            } else if (expectedParameter instanceof CookieParameter && actualParameter instanceof CookieParameter) {
                CookieParameter actualCookieParameter = (CookieParameter) actualParameter;
                CookieParameter expectedCookieParameter = (CookieParameter) expectedParameter;
                softAssertions.assertThat(actualCookieParameter.getType()).as(message).isEqualTo(expectedCookieParameter.getType());
                List<String> expectedEnums = expectedCookieParameter.getEnum();
                if (CollectionUtils.isNotEmpty(expectedEnums)) {
                    softAssertions.assertThat(actualCookieParameter.getEnum()).as(message).hasSameElementsAs(expectedEnums);
                } else {
                    softAssertions.assertThat(actualCookieParameter.getEnum()).as(message).isNullOrEmpty();
                }
            } else if (expectedParameter instanceof RefParameter && actualParameter instanceof RefParameter) {
                RefParameter expectedRefParameter = (RefParameter) expectedParameter;
                RefParameter actualRefParameter = (RefParameter) actualParameter;
                softAssertions.assertThat(actualRefParameter.getSimpleRef()).as(message).isEqualTo(expectedRefParameter.getSimpleRef());
            }
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

    private SwaggerAssertionConfig loadSwaggerAssertionFlagsConfiguration(String configurationResourceLocation) {
        final Properties props = new Properties();
        try (final InputStream is = this.getClass().getResourceAsStream(configurationResourceLocation)) {
            if (is != null)
                props.load(is);
        } catch (final IOException ioe) {
            // eat it.
        }

        return new SwaggerAssertionConfig(props);
    }

    private boolean isAssertionEnabled(final SwaggerAssertionType assertionType) {
        return assertionConfig.swaggerAssertionEnabled(assertionType);
    }

    private Set<String> filterWhitelistedPropertyNames(String definitionName, Set<String> expectedPropertyNames) {
        Set<String> result = new HashSet<>(expectedPropertyNames.size());
        final Set<String> ignoredPropertyNames = assertionConfig.getPropertiesToIgnoreInExpected();
        for (String property : expectedPropertyNames) {
            if (!ignoredPropertyNames.contains(definitionName + '.' + property)) {
                result.add(property);
            }
        }
        return result;
    }

    private <K, V> Map<K, V> removeAllFromMap(Map<K, V> map, Set<K> keysToExclude) {
        final LinkedHashMap<K, V> result = new LinkedHashMap<>(map);
        result.keySet().removeAll(keysToExclude);
        return result;
    }

    private Map<String, Path> adjustExpectedPathsWithPrefix(Map<String, Path> paths, String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return paths;   // no path prefix configured, nothing to do
        }

        final Map<String, Path> adjustedPaths = new HashMap<>(paths.size());
        for (final Map.Entry<String, Path> entry : paths.entrySet()) {
            adjustedPaths.put(prefix + entry.getKey(), entry.getValue());
        }
        return adjustedPaths;
    }


    /**
     * Provide a means to retrieve values from various objects in the schema, providing a fallback to 'global'
     * settings if they're not defined locally.
     */
    private class AttributeResolver {

        private Swagger expected;
        private Swagger actual;

        public AttributeResolver(Swagger expected, Swagger actual) {
            this.expected = expected;
            this.actual = actual;
        }

        public List<String> getExpectedConsumes(Operation op) {
            return getListWithFallback(op.getConsumes(), expected.getConsumes());
        }

        public List<String> getActualConsumes(Operation op) {
            return getListWithFallback(op.getConsumes(), actual.getConsumes());
        }

        public List<String> getExpectedProduces(Operation op) {
            return getListWithFallback(op.getProduces(), expected.getProduces());
        }

        public List<String> getActualProduces(Operation op) {
            return getListWithFallback(op.getProduces(), actual.getProduces());
        }

        private <A> List<A> getListWithFallback(List<A> localDefn, List<A> globalDefn) {
            final List<A> result;
            if (localDefn != null && !localDefn.isEmpty()) {
                result = localDefn;
            } else if (globalDefn != null) {
                result = globalDefn;
            } else {
                result = Collections.emptyList();
            }
            return result;
        }

    }

}