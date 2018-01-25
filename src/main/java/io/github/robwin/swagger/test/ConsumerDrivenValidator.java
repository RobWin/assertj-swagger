package io.github.robwin.swagger.test;

import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by raceconditions on 3/17/16.
 */
class ConsumerDrivenValidator implements ContractValidator {

    private SoftAssertions softAssertions;

    private SwaggerAssertionConfig assertionConfig;
    private Swagger actual;
    private SchemaObjectResolver schemaObjectResolver;   // provide means to fall back from local to global properties

    ConsumerDrivenValidator(Swagger actual, SwaggerAssertionConfig assertionConfig) {
        this.actual = actual;
        this.assertionConfig = assertionConfig;
        softAssertions = new SoftAssertions();
    }

    @Override
    public void validateSwagger(Swagger expected, SchemaObjectResolver schemaObjectResolver) {
        this.schemaObjectResolver = schemaObjectResolver;

        validateInfo(actual.getInfo(), expected.getInfo());

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


    private void validateInfo(Info actualInfo, Info expectedInfo) {

        // Version.  OFF by default.
        if (isAssertionEnabled(SwaggerAssertionType.VERSION)) {
            softAssertions.assertThat(actualInfo.getVersion()).as("Checking Version").isEqualTo(expectedInfo.getVersion());
        }

        // Everything (but potentially brittle, therefore OFF by default)
        if (isAssertionEnabled(SwaggerAssertionType.INFO)) {
            softAssertions.assertThat(actualInfo).as("Checking Info").isEqualToComparingFieldByField(expectedInfo);
        }
    }

    private void validatePaths(Map<String, Path> actualPaths, Map<String, Path> expectedPaths) {
        if(MapUtils.isNotEmpty(expectedPaths)) {
            softAssertions.assertThat(actualPaths).as("Checking Paths").isNotEmpty();
            if(MapUtils.isNotEmpty(actualPaths)){
                softAssertions.assertThat(actualPaths.keySet()).as("Checking Paths").containsAll(expectedPaths.keySet());
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
                softAssertions.assertThat(actualDefinitions.keySet()).as("Checking Definitions").containsAll(expectedDefinitions.keySet());
                for (Map.Entry<String, Model> expectedDefinitionEntry : expectedDefinitions.entrySet()) {
                    Model expectedDefinition = expectedDefinitionEntry.getValue();
                    Model actualDefinition = actualDefinitions.get(expectedDefinitionEntry.getKey());
                    String definitionName = expectedDefinitionEntry.getKey();
                    validateDefinition(definitionName, actualDefinition, expectedDefinition);
                }
            }
        }else{
            softAssertions.assertThat(actualDefinitions).as("Checking Definitions").isNullOrEmpty();
        }
    }

    private void validatePath(String pathName, Path actualPath, Path expectedPath) {
        if (expectedPath != null) {
            softAssertions.assertThat(actualPath.getOperations().size()).as("Checking number of operations of path '%s'", pathName).isGreaterThanOrEqualTo(expectedPath.getOperations().size());
            validateOperation(actualPath.getGet(), expectedPath.getGet(), pathName, "GET");
            validateOperation(actualPath.getDelete(), expectedPath.getDelete(), pathName, "DELETE");
            validateOperation(actualPath.getPost(), expectedPath.getPost(), pathName, "POST");
            validateOperation(actualPath.getPut(), expectedPath.getPut(), pathName, "PUT");
            validateOperation(actualPath.getPatch(), expectedPath.getPatch(), pathName, "PATCH");
            validateOperation(actualPath.getOptions(), expectedPath.getOptions(), pathName, "OPTIONS");
        }
    }

    private void validateDefinition(String definitionName, Model actualDefinition, Model expectedDefinition) {
        if (expectedDefinition != null && actualDefinition != null) {
            validateModel(actualDefinition, expectedDefinition, String.format("Checking model of definition '%s", definitionName));
            validateDefinitionProperties(schemaObjectResolver.resolvePropertiesFromActual(actualDefinition),
                                         schemaObjectResolver.resolvePropertiesFromExpected(expectedDefinition),
                                         definitionName);

            if (expectedDefinition instanceof ModelImpl) {
                validateDefinitionRequiredProperties(((ModelImpl)actualDefinition).getRequired(),
                                                     ((ModelImpl)expectedDefinition).getRequired(),
                                                       definitionName);
            }
        }
    }

    private void validateDefinitionRequiredProperties(List<String> actualRequiredProperties, List<String> expectedRequiredProperties, String definitionName) {
        if(CollectionUtils.isNotEmpty(expectedRequiredProperties)) {
            softAssertions.assertThat(actualRequiredProperties).as("Checking required properties of definition '%s'", definitionName).isNotEmpty();
            if(CollectionUtils.isNotEmpty(actualRequiredProperties)){
                final Set<String> filteredExpectedProperties = filterWhitelistedPropertyNames(definitionName, new HashSet<>(expectedRequiredProperties));
                softAssertions.assertThat(actualRequiredProperties).as("Checking required properties of definition '%s'", definitionName).hasSameElementsAs(filteredExpectedProperties);
            }
        } else {
            softAssertions.assertThat(actualRequiredProperties).as("Checking required properties of definition '%s'", definitionName).isNullOrEmpty();
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
                softAssertions.assertThat(actualDefinitionProperties.keySet()).as("Checking properties of definition '%s'", definitionName).containsAll(filteredExpectedProperties);
                for (Map.Entry<String, Property> expectedDefinitionPropertyEntry : expectedDefinitionProperties.entrySet()) {
                    Property expectedDefinitionProperty = expectedDefinitionPropertyEntry.getValue();
                    Property actualDefinitionProperty = actualDefinitionProperties.get(expectedDefinitionPropertyEntry.getKey());
                    String propertyName = expectedDefinitionPropertyEntry.getKey();
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
            if(actualOperation != null) {
                softAssertions.assertThat(actualOperation).as(message).isNotNull();
                //Validate consumes
                validateList(schemaObjectResolver.getActualConsumes(actualOperation),
                        schemaObjectResolver.getExpectedConsumes((expectedOperation)),
                        String.format("Checking '%s' of '%s' operation of path '%s'", "consumes", httpMethod, path));
                //Validate produces
                validateList(schemaObjectResolver.getActualProduces(actualOperation),
                        schemaObjectResolver.getExpectedProduces((expectedOperation)),
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

    private void validateParameters(final List<Parameter> actualOperationParameters,
                                    final List<Parameter> expectedOperationParameters,
                                    final String httpMethod,
                                    final String path) {
        verifyExpectedParameters(actualOperationParameters, expectedOperationParameters, httpMethod, path);
        verifyUnexpectedParameters(actualOperationParameters, expectedOperationParameters, httpMethod, path);
    }

    private void verifyUnexpectedParameters(List<Parameter> actualOperationParameters,
                                            List<Parameter> expectedOperationParameters,
                                            String httpMethod,
                                            String path) {
        String message = String.format("Checking parameters of '%s' operation of path '%s'", httpMethod, path);
        Predicate<Parameter> notExpectedParameter = ((Predicate<Parameter>) expectedOperationParameters::contains).negate();
        actualOperationParameters.stream().filter(notExpectedParameter).forEach(extraParameter ->
                softAssertions.assertThat(extraParameter.getRequired()).as(message).isFalse()
        );
    }

    private void verifyExpectedParameters(List<Parameter> actualOperationParameters,
                                          List<Parameter> expectedOperationParameters,
                                          String httpMethod,
                                          String path) {
        actualOperationParameters.forEach(actual -> {
            Optional<Parameter> matchedActualParameter = expectedOperationParameters.stream().filter(actual::equals).findFirst();
            matchedActualParameter.ifPresent(matched -> validateParameter(actual, matched, matched.getName(), httpMethod, path));
        });
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
                softAssertions.assertThat(actualResponseHeaders.keySet()).as(message).containsAll(expectedResponseHeaders.keySet());
                for (Map.Entry<String, Property> expectedResponseHeaderEntry : expectedResponseHeaders.entrySet()) {
                    Property expectedResponseHeader = expectedResponseHeaderEntry.getValue();
                    Property actualResponseHeader = actualResponseHeaders.get(expectedResponseHeaderEntry.getKey());
                    String responseHeaderName = expectedResponseHeaderEntry.getKey();
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
                softAssertions.assertThat(actualList).as(message).containsAll(expectedList);
            }
        }else{
            softAssertions.assertThat(actualList).as(message).isNullOrEmpty();
        }
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
}
