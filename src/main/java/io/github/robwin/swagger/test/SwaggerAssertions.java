package io.github.robwin.swagger.test;

import com.wordnik.swagger.models.Swagger;

/**
 * Entry point for assertion methods for different data types. Each method in this class is a static factory for the
 * type-specific assertion objects. The purpose of this class is to make test code more readable.
 *
 * @author Robert Winkler
 */
public class SwaggerAssertions {

    /**
     * Creates a new instance of <code>{@link SwaggerAssert}</code>.
     *
     * @param actual the the actual value.
     * @return the created assertion object.
     */
    public static SwaggerAssert assertThat(Swagger actual) {
        return new SwaggerAssert(actual);
    }
}
