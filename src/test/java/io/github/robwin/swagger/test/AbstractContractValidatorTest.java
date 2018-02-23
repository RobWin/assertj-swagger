package io.github.robwin.swagger.test;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests AbstractContractValidator.
 */
@RunWith(Enclosed.class)
public class AbstractContractValidatorTest {
    private static Swagger buildSwaggerFrom(String filename) {
        File swaggerFile = new File(AbstractContractValidatorTest.class.getResource(filename).getPath());
        return new SwaggerParser().read(swaggerFile.getAbsolutePath());
    }

    /**
     * Tests getPathsIncludingBasePath().
     */
    public static class GetPathsIncludingBasePath {
        @Test
        public void shouldReturnPlainPathsIfNoBasePathSet() {
            // given
            Swagger swagger = buildSwaggerFrom("/swagger_with_path_prefixes.json");
            // when
            Map<String, Path> paths = new DummyValidator().getPathsIncludingBasePath(swagger);
            // then
            paths.entrySet().forEach(e -> assertThat(e.getKey(), startsWith("/v2")));
        }

        @Test
        public void shouldReturnPathsPrefixedIfBasePathSet() {
            // given
            Swagger swagger = buildSwaggerFrom("/swagger.json");
            // when
            Map<String, Path> paths = new DummyValidator().getPathsIncludingBasePath(swagger);
            // then
            paths.entrySet().forEach(e -> assertThat(e.getKey(), startsWith(swagger.getBasePath())));
        }

        @Test
        public void shouldNotAddRootBasePathToPaths() {
            // given basePath: /
            Swagger swagger = buildSwaggerFrom("/swagger-with-path-prefixes-and-root-basepath.json");
            // when
            Map<String, Path> paths = new DummyValidator().getPathsIncludingBasePath(swagger);
            // then
            paths.entrySet().forEach(e -> assertThat(e.getKey(), startsWith("/v2")));
        }
    }

    /**
     * Tests findExpectedPaths().
     */
    public static class FindExpectedPaths {
        @Test
        public void shouldReturnPlainPathsIfNoBasePathSetAndNoPrefixConfigured() {
            // given
            Swagger swagger = buildSwaggerFrom("/swagger_with_path_prefixes.json");
            SwaggerAssertionConfig swaggerAssertionConfig = new SwaggerAssertionConfig(new Properties());
            // when
            Map<String, Path> paths = new DummyValidator().findExpectedPaths(swagger, swaggerAssertionConfig);
            // then
            paths.entrySet().forEach(e -> assertThat(e.getKey(), startsWith("/v2")));
        }

        @Test
        public void shouldReturnPathsPrefixedIfBasePathSet() {
            // given
            Swagger swagger = buildSwaggerFrom("/swagger.json");
            SwaggerAssertionConfig swaggerAssertionConfig = new SwaggerAssertionConfig(new Properties());
            // when
            Map<String, Path> paths = new DummyValidator().findExpectedPaths(swagger, swaggerAssertionConfig);
            // then
            paths.entrySet().forEach(e -> assertThat(e.getKey(), startsWith(swagger.getBasePath())));
        }

        @Test
        public void shouldReturnPathsPrefixedIfPrefixConfigured() {
            // given
            Swagger swagger = buildSwaggerFrom("/swagger.json");
            Properties properties = new Properties();
            String configuredPathPrefix = "/foo";
            properties.put("assertj.swagger.pathsPrependExpected", configuredPathPrefix);
            SwaggerAssertionConfig swaggerAssertionConfig = new SwaggerAssertionConfig(properties);
            // when
            Map<String, Path> paths = new DummyValidator().findExpectedPaths(swagger, swaggerAssertionConfig);
            // then configured prefix takes precedence over base path
            paths.entrySet().forEach(e -> {
                assertThat(e.getKey(), startsWith(configuredPathPrefix));
                assertThat(e.getKey(), not(containsString(swagger.getBasePath())));
            });
        }
    }

    private static class DummyValidator extends AbstractContractValidator {
        @Override
        public void validateSwagger(Swagger expected, SchemaObjectResolver schemaObjectResolver) {

        }
    }

}
