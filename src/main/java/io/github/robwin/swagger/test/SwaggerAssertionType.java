package io.github.robwin.swagger.test;

/**
 * Flags to enable or disable various assertion types are either a coarse- or fine-grained level.
 */
public enum SwaggerAssertionType {

    INFO("validateInfo", false),
    VERSION("validateVersion", false),

    DEFINITIONS("validateDefinitions", true),
        PROPERTIES("validateProperties", true),
            REF_PROPERTIES("validateRefProperties", true),
            ARRAY_PROPERTIES("validateArrayProperties", true),
            STRING_PROPERTIES("validateStringProperties", true),
        MODELS("validateModels", true),
    PATHS("validatePaths", true);

    private String suffix;
    private boolean enabledByDefault;

    SwaggerAssertionType(final String assertionType, final boolean defaultValue) {
        this.suffix = assertionType;
        this.enabledByDefault = defaultValue;
    }

    public String getBarePropertyName() {
        return suffix;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }
}
