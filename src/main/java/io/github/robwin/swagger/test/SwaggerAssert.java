package io.github.robwin.swagger.test;

import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

import java.util.Collection;

/**
 * Assertion methods for {@code Swagger}.
 * <p>
 * To create a new instance of this class, invoke <code>{@link io.github.robwin.swagger.test.SwaggerAssertions#assertThat(Swagger)}</code>.
 * </p>
 *
 * @author Robert Winkler
 */

public class SwaggerAssert extends AbstractAssert<SwaggerAssert, Swagger> {

    public SwaggerAssert(Swagger actual) {
        super(actual, SwaggerAssert.class);
    }

    /**
     * Verifies that the actual value is equal to the given one.
     *
     * @param expected the given value to compare the actual value to.
     * @return {@code this} assertion object.
     * @throws AssertionError if the actual value is not equal to the given one or if the actual value is {@code null}..
     */
    public SwaggerAssert isEqualTo(Swagger expected) {
        SoftAssertions softAssertions = new SoftAssertions();

        // Check Paths
        softAssertions.assertThat(actual.getPaths().keySet()).as("Checking Paths").hasSameElementsAs(expected.getPaths().keySet());

        Collection<Path> actualPaths = actual.getPaths().values();
        Collection<Path> expectedPaths = expected.getPaths().values();

        softAssertions.assertThat(actualPaths).as("Checking Paths").hasSameElementsAs(expectedPaths);


        // Check Definitions
        softAssertions.assertThat(actual.getDefinitions().keySet()).as("Checking Definitions").hasSameElementsAs(expected.getDefinitions().keySet());

        softAssertions.assertAll();

        return myself;
    }
}