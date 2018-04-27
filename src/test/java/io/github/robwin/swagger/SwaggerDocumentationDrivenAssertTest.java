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
package io.github.robwin.swagger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import io.github.robwin.swagger.test.SwaggerAssert;
import io.github.robwin.swagger.test.SwaggerAssertionConfig;
import io.github.robwin.swagger.test.SwaggerAssertions;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.Validate;
import org.junit.After;
import org.junit.Test;

public class SwaggerDocumentationDrivenAssertTest {
    private static final String SWAGGER_CONFIG_LOCATION = "assertj-swagger.properties";
    private static final File SWAGGER_CONFIG = new File(SWAGGER_CONFIG_LOCATION);

    @After
    public void tearDown() {
        SWAGGER_CONFIG.deleteOnExit();
    }

    @Test
    public void shouldFindNoDifferences() {
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.json").getFile());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getFile());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInImplementation() {
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/wrong_swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInParameterNaming() {
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger-name-changes.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInRequiredness() {
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger-requiredness-changes.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInInfo() {
        // Otherwise-good comparison will fail here, because 'info.title' is different
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-info.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandlePartiallyImplementedApi() {
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/partial_impl_swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-partial-impl.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleExpectedPathsWithPrefix() {
        File implFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger_with_path_prefixes.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerDocumentationDrivenAssertTest.class.getResource("/swagger.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-path-prefix.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleDefinitionsUsingAllOf() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-flat.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-inheritance.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-allOf.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleDefinitionsUsingAllOfIncludingCycles() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-flat.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-inheritance-cycles.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-allOf.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleDefinitionsUsingAllOfForComposition() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-composition-flat.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-composition.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()))
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferentRequiredFieldsForObjectDefinition() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-missing-required-field-object.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()))
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleEnumValues() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()))
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferentEnumValues() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum-wrong.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()))
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldAllowConfigurationToLooselyMatchResponse() throws IOException {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/designed-swagger-with-less-response-defined.yaml").getPath());
        configureSwaggerAssertion();

        SwaggerAssertionConfig config = getConfig();

        SwaggerAssert swaggerAssert = new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), config);
        swaggerAssert.isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    private SwaggerAssertionConfig getConfig() throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(SWAGGER_CONFIG));
        return new SwaggerAssertionConfig(props);
    }

    private void configureSwaggerAssertion() throws IOException {
        Files.write(Paths.get(SWAGGER_CONFIG_LOCATION), toConfiguration().getBytes());
    }

    private String toConfiguration() {
        return "assertj.swagger.validateResponseWithStrictlyMatch=false";
    }
        
    @Test
    public void shouldHandleRefEnumValues() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum-ref.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum-ref.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()))
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferentRefEnumValues() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum-ref-wrong.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-enum-ref.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()))
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }
}
