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
package io.github.robwin.swagger;

import java.io.File;

import io.github.robwin.swagger.test.SwaggerAssert;
import io.github.robwin.swagger.test.SwaggerAssertions;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.Validate;
import org.junit.Test;

public class SwaggerConsumerDrivenAssertTest {

    @Test
    public void shouldFindNoDifferences() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getFile());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getFile());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldtolerateOptionalParameter() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-added-optional-parameter.json").getFile());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getFile());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotTolerateRequiredParameter() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-added-required-parameter.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInImplementation() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/wrong_swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInInfo() {
        // Otherwise-good comparison will fail here, because 'info.title' is different
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-info.properties")
                .satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindMissingPropertyInPartialModel() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-singleresource-extraproperty.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindMissingMethodInPartialModel() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-singleresource-extramethod.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindMissingResourceInPartialModel() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-extraresource.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleConsumerContractSingleResource() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-singleresource.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleConsumerContractPartialModel() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-singleresource-partialmodel.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleExpectedPathsWithPrefix() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger_with_path_prefixes.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-path-prefix.properties")
                .satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleDefinitionsUsingAllOf() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-flat.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-inheritance.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-allOf.properties")
            .satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldHandleDefinitionsUsingAllOfIncludingCycles() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-flat.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-allOf-test-inheritance-cycles.json").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-allOf.properties")
            .satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferentRequiredFieldsForObjectDefinition() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-missing-required-field-object.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test
    public void shouldPassTestForIdenticalParametersSetContainsParameterWithDifferentTypes(){
        File swaggerFile = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-with-multi-types-parameters.json").getPath());
        SwaggerAssertions.assertThat(swaggerFile.getAbsolutePath()).satisfiesContract(swaggerFile.getAbsolutePath());

    }

    @Test
    public void shouldntFailWhenImplHasMoreOperationsOfSamePath() {
        File implFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerConsumerDrivenAssertTest.class.getResource("/swagger-path-without-some-operations.json").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath())
                .satisfiesContract(designFirstSwaggerLocation.getAbsolutePath());
    }

}
