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
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerParser;
import org.assertj.core.api.AbstractAssert;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


/**
 * Assertion methods for {@code Swagger}.
 * <p>
 * To create a new instance of this class, invoke <code>{@link io.github.robwin.swagger.test.SwaggerAssertions#assertThat(Swagger)}</code>.
 * </p>
 *
 * @author Robert Winkler
 */

public class SwaggerAssert extends AbstractAssert<SwaggerAssert, Swagger> {

    private static final String ASSERTION_ENABLED_CONFIG_PATH = "/assertj-swagger.properties";
    private DocumentationDrivenValidator documentationDrivenValidator;
    private ConsumerDrivenValidator consumerDrivenValidator;


    public SwaggerAssert(Swagger actual) {
        super(actual, SwaggerAssert.class);
        documentationDrivenValidator = new DocumentationDrivenValidator(actual, loadSwaggerAssertionFlagsConfiguration(ASSERTION_ENABLED_CONFIG_PATH));
        consumerDrivenValidator = new ConsumerDrivenValidator(actual, loadSwaggerAssertionFlagsConfiguration(ASSERTION_ENABLED_CONFIG_PATH));
    }

    public SwaggerAssert(Swagger actual, SwaggerAssertionConfig assertionConfig) {
        super(actual, SwaggerAssert.class);
        documentationDrivenValidator = new DocumentationDrivenValidator(actual, assertionConfig);
        consumerDrivenValidator = new ConsumerDrivenValidator(actual, assertionConfig);
    }

    public SwaggerAssert(Swagger actual, String configurationResourceLocation) {
        super(actual, SwaggerAssert.class);
        documentationDrivenValidator = new DocumentationDrivenValidator(actual, loadSwaggerAssertionFlagsConfiguration(configurationResourceLocation));
        consumerDrivenValidator = new ConsumerDrivenValidator(actual, loadSwaggerAssertionFlagsConfiguration(configurationResourceLocation));
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expected the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert isEqualTo(Swagger expected) {
        SchemaObjectResolver schemaObjectResolver = new SchemaObjectResolver(expected, actual);
        documentationDrivenValidator.validateSwagger(expected, schemaObjectResolver);
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

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expectedLocation the location of the given value to compare the actual value to.
     * @param auths List of io.swagger.models.auth.AuthorizationValue for access to protected locations.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert isEqualTo(String expectedLocation, List<AuthorizationValue> auths) {
        return isEqualTo(new SwaggerParser().read(expectedLocation, auths, true));
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expected the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert satisfiesContract(Swagger expected) {
        SchemaObjectResolver schemaObjectResolver = new SchemaObjectResolver(expected, actual);
        consumerDrivenValidator.validateSwagger(expected, schemaObjectResolver);
        return myself;
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expectedLocation the location of the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert satisfiesContract(String expectedLocation) {
        return satisfiesContract(new SwaggerParser().read(expectedLocation));
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expectedLocation the location of the given value to compare the actual value to.
     * @param auths List of io.swagger.models.auth.AuthorizationValue for access to protected locations.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert satisfiesContract(String expectedLocation, List<AuthorizationValue> auths) {
        return satisfiesContract(new SwaggerParser().read(expectedLocation, auths, true));
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
}