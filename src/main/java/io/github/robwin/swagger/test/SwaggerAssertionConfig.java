package io.github.robwin.swagger.test;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SwaggerAssertionConfig {

    private static final String PREFIX = "assertj.swagger.";
    private static final String IGNORE_MISSING_PATHS = "pathsToIgnoreInExpected";
    private static final String IGNORE_MISSING_DEFINITIONS = "definitionsToIgnoreInExpected";
    private static final String IGNORE_MISSING_PROPERTIES = "propertiesToIgnoreInExpected";
    private static final String PATHS_PREPEND_EXPECTED = "pathsPrependExpected";

    private Map<SwaggerAssertionType, Boolean> swaggerAssertionFlags = new HashMap<>();

    private Set<String> pathsToIgnoreInExpected = Collections.emptySet();

    private Set<String> propertiesToIgnoreInExpected = Collections.emptySet();

    private Set<String> definitionsToIgnoreInExpected = Collections.emptySet();

    private String pathsPrependExpected;


    /**
     * Construct a {@link SwaggerAssertionConfig}.
     */
    public SwaggerAssertionConfig() {
        final SwaggerAssertionType[] assertionTypes = SwaggerAssertionType.values();
        for (final SwaggerAssertionType assertionType : assertionTypes) {
            swaggerAssertionFlags.put(assertionType, assertionType.isEnabledByDefault());
        }
    }

    /**
     * Construct a {@link SwaggerAssertionConfig}.  All checks are enabled by default, and overridden by the supplied
     * properties.
     * @param props properties.  Typically sourced from root of classpath
     */
    public SwaggerAssertionConfig(final Properties props) {
        this();
        final SwaggerAssertionType[] assertionTypes = SwaggerAssertionType.values();
        for (final SwaggerAssertionType assertionType : assertionTypes) {
            final String value = props.getProperty(PREFIX + assertionType.getBarePropertyName());
            if (value != null) {
                swaggerAssertionFlags.put(assertionType, Boolean.TRUE.toString().equals(value));
            } else {
                swaggerAssertionFlags.put(assertionType, assertionType.isEnabledByDefault());
            }
        }

        final String ignoreMissingPathsStr = props.getProperty(PREFIX + IGNORE_MISSING_PATHS);
        if (!StringUtils.isBlank(ignoreMissingPathsStr)) {
            pathsToIgnoreInExpected = splitCommaDelimStrIntoSet(ignoreMissingPathsStr);
        }

        final String ignoreMissingDefinitionsStr = props.getProperty(PREFIX + IGNORE_MISSING_DEFINITIONS);
        if (!StringUtils.isBlank(ignoreMissingDefinitionsStr)) {
            definitionsToIgnoreInExpected = splitCommaDelimStrIntoSet(ignoreMissingDefinitionsStr);
        }

        final String ignoreMissingPropertiesStr = props.getProperty(PREFIX + IGNORE_MISSING_PROPERTIES);
        if (!StringUtils.isBlank(ignoreMissingPropertiesStr)) {
            propertiesToIgnoreInExpected = splitCommaDelimStrIntoSet(ignoreMissingPropertiesStr);
        }

        pathsPrependExpected = props.getProperty(PREFIX + PATHS_PREPEND_EXPECTED);
    }

    public boolean swaggerAssertionEnabled(SwaggerAssertionType assertionType) {
        final Boolean flag = swaggerAssertionFlags.get(assertionType);
        return flag != null ? flag : assertionType.isEnabledByDefault();
    }

    public Set<String> getPathsToIgnoreInExpected() {
        return pathsToIgnoreInExpected;
    }

    public Set<String> getDefinitionsToIgnoreInExpected() {
        return definitionsToIgnoreInExpected;
    }

    public Set<String> getPropertiesToIgnoreInExpected() {
        return propertiesToIgnoreInExpected;
    }

    public String getPathsPrependExpected() {
        return pathsPrependExpected;
    }

    private Set<String> splitCommaDelimStrIntoSet(String str) {
        final String[] strs = str.split("\\s*,\\s*");
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(strs)));
    }
}
