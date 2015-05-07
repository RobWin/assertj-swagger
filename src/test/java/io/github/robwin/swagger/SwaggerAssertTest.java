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

import com.wordnik.swagger.models.Swagger;
import io.github.robwin.swagger.test.SwaggerAssertions;
import io.swagger.parser.SwaggerParser;
import org.junit.Test;

public class SwaggerAssertTest {

    @Test
    public void shouldFindNoDifferences(){

        String implFirstSwaggerLocation = SwaggerAssertTest.class.getResource("/swagger.json").getPath();
        String designFirstSwaggerLocation = SwaggerAssertTest.class.getResource("/swagger.yaml").getPath();

        Swagger designFirstSwagger = new SwaggerParser().read(designFirstSwaggerLocation);
        Swagger implFirstSwagger = new SwaggerParser().read(implFirstSwaggerLocation);

        SwaggerAssertions.assertThat(implFirstSwagger).isEqualTo(designFirstSwagger);

    }

    @Test
    public void shouldFindDifferencesInImplementation(){

        String implFirstSwaggerLocation = SwaggerAssertTest.class.getResource("/wrong_swagger.json").getPath();
        String designFirstSwaggerLocation = SwaggerAssertTest.class.getResource("/swagger.yaml").getPath();

        Swagger designFirstSwagger = new SwaggerParser().read(designFirstSwaggerLocation);
        Swagger implFirstSwagger = new SwaggerParser().read(implFirstSwaggerLocation);

        SwaggerAssertions.assertThat(implFirstSwagger).isEqualTo(designFirstSwagger);

    }
}
