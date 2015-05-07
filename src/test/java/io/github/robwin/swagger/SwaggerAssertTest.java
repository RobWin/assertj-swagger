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
