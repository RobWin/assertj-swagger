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
package io.github.robwin.swagger.test;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Offers functionality shared across concrete validator implementations.
 */
public abstract class AbstractContractValidator implements ContractValidator {

    /**
     * Finds the expected paths considering both {@code pathsPrependExpected} in the config and {@code basePath} in the Swagger model. The configured value
     * takes precedence regardless of whether it happens to be the same value as the base path or not. If no prefix is configured the Swagger base path is added
     * to each actual path.
     *
     * @param expected        Swagger model
     * @param assertionConfig assertion configuration
     * @return expected paths
     */
    protected Map<String, Path> findExpectedPaths(Swagger expected, SwaggerAssertionConfig assertionConfig) {
        String pathsPrependExpected = assertionConfig.getPathsPrependExpected();
        String basePath = expected.getBasePath();
        if (StringUtils.isBlank(pathsPrependExpected) && isBlankOrSlash(basePath)) {
            return expected.getPaths();   // no path prefix configured and no basePath set, nothing to do
        }

        String pathPrefix = null;
        if (StringUtils.isNotBlank(pathsPrependExpected)) {
            pathPrefix = pathsPrependExpected;
        } else if (!isBlankOrSlash(basePath)) {
            pathPrefix = basePath;
        }

        final String finalPathPrefix = pathPrefix;
        return finalPathPrefix == null ?
                expected.getPaths() :
                getPathsWithPrefix(expected, finalPathPrefix);
    }

    /**
     * Gets the paths from the actual Swagger model. Each path is prefixed with the base path configured in the model.
     *
     * @param actual Swagger model
     * @return paths including base path
     */
    protected Map<String, Path> getPathsIncludingBasePath(Swagger actual) {
        String basePath = actual.getBasePath();
        return isBlankOrSlash(basePath) ?
                actual.getPaths() :
                getPathsWithPrefix(actual, basePath);
    }

    private Map<String, Path> getPathsWithPrefix(Swagger swagger, String prefix) {
        return swagger.getPaths().entrySet().stream().collect(Collectors.toMap(
                e -> prefix + e.getKey(),
                e -> e.getValue()));
    }

    private boolean isBlankOrSlash(String basePath) {
        return StringUtils.isBlank(basePath) || basePath.equals("/");
    }
}
