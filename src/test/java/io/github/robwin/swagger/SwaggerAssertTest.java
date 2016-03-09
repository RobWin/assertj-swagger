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

import io.github.robwin.swagger.test.SwaggerAssert;
import io.github.robwin.swagger.test.SwaggerAssertions;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.Validate;
import org.junit.Test;

import java.io.File;

public class SwaggerAssertTest {

    @Test
    public void shouldFindNoDifferences(){
        File implFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/swagger.json").getFile());
        File designFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/swagger.yaml").getFile());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test(expected = AssertionError.class)
    public void shouldFindDifferencesInImplementation(){
        File implFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/wrong_swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/swagger.yaml").getPath());
        SwaggerAssertions.assertThat(implFirstSwaggerLocation.getAbsolutePath()).isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test()
    public void shouldHandlePartiallyImplementedApi(){
        File implFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/partial_impl_swagger.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/swagger.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-partial-impl.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

    @Test()
    public void shouldHandleExpectedPathsWithPrefix(){
        File implFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/swagger_with_path_prefixes.json").getPath());
        File designFirstSwaggerLocation = new File(SwaggerAssertTest.class.getResource("/swagger.yaml").getPath());

        Validate.notNull(implFirstSwaggerLocation.getAbsolutePath(), "actualLocation must not be null!");
        new SwaggerAssert(new SwaggerParser().read(implFirstSwaggerLocation.getAbsolutePath()), "/assertj-swagger-path-prefix.properties")
                .isEqualTo(designFirstSwaggerLocation.getAbsolutePath());
    }

}
