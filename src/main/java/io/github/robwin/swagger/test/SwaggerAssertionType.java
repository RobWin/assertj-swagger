package io.github.robwin.swagger.test;

/**
 * Flags to enable or disable various assertion types are either a coarse- or fine-grained level.
 */
public enum SwaggerAssertionType {

    DEFINITIONS("validateDefinitions"),
        PROPERTIES("validateProperties"),
            REF_PROPERTIES("validateRefProperties"),
            ARRAY_PROPERTIES("validateArrayProperties"),
            STRING_PROPERTIES("validateStringProperties"),
        MODELS("validateModels"),
    PATHS("validatePaths");

    private String suffix;

    SwaggerAssertionType(final String assertionType) {
        this.suffix = assertionType;
    }

    public String getBarePropertyName() {
        return suffix;
    }

}
