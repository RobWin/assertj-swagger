package io.github.robwin.swagger.test;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.api.SoftAssertions;

public class PropertyValidator {

    /**
     * Basic properties that impacts swagger contract.
     */
    private static final String[] BASIC_PROPERTIES = {"type", "format", "allowEmptyValue", "name", "required",
        "readOnly", "access", "vendorExtensions"};

    private SwaggerAssertionConfig assertionConfig;
    private SoftAssertions softAssertions;

    public PropertyValidator(SwaggerAssertionConfig assertionConfig, SoftAssertions softAssertions) {
        this.assertionConfig = assertionConfig;
        this.softAssertions = softAssertions;
    }

    void validateProperty(Property actualProperty, Property expectedProperty, String message) {
        if (expectedProperty == null || !isAssertionEnabled(SwaggerAssertionType.PROPERTIES)) {
            return;
        }

        // TODO Validate Property schema
        if (shouldValidateRefProperty(expectedProperty)) {
            validateBasicPropertyFeatures(actualProperty, expectedProperty, message);
            // TODO improve validation by verifying property based on RefProperty type
        } else if (shouldValidateArrayProperty(expectedProperty)) {
            validateBasicPropertyFeatures(actualProperty, expectedProperty, message);
            // TODO improve validation by verifying property based on ArrayProperty type
        } else if (shouldValidateByteArrayProperty(expectedProperty)) {
            validateBasicPropertyFeatures(actualProperty, expectedProperty, message);
            // TODO improve validation by verifying property based on ByteArrayProperty type
        } else if (shouldValidateStringProperty(expectedProperty)) {
            StringProperty expectedStringProperty = (StringProperty) expectedProperty;
            validateBasicPropertyFeatures(actualProperty, expectedProperty, message);
            if (isPropertyOfEnumType(actualProperty)) {
                StringProperty actualStringProperty = (StringProperty) actualProperty;
                validateEnumPropertyFeatures(actualStringProperty, expectedStringProperty);
            }
        } else {
            validateBasicPropertyFeatures(actualProperty, expectedProperty, message);
        }
    }

    private boolean shouldValidateRefProperty(Property expectedProperty) {
        return RefProperty.class.isAssignableFrom(expectedProperty.getClass()) && isAssertionEnabled(
            SwaggerAssertionType.REF_PROPERTIES);
    }

    private boolean shouldValidateArrayProperty(Property expectedProperty) {
        return ArrayProperty.class.isAssignableFrom(expectedProperty.getClass()) && isAssertionEnabled(
            SwaggerAssertionType.ARRAY_PROPERTIES);
    }

    private boolean shouldValidateByteArrayProperty(Property expectedProperty) {
        return ByteArrayProperty.class.isAssignableFrom(expectedProperty.getClass()) && isAssertionEnabled(
            SwaggerAssertionType.BYTE_ARRAY_PROPERTIES);
    }

    private boolean shouldValidateStringProperty(Property expectedProperty) {
        return StringProperty.class.isAssignableFrom(expectedProperty.getClass()) && isAssertionEnabled(
            SwaggerAssertionType.STRING_PROPERTIES);
    }

    private boolean isAssertionEnabled(final SwaggerAssertionType assertionType) {
        return assertionConfig.swaggerAssertionEnabled(assertionType);
    }

    private void validateBasicPropertyFeatures(Property actualProperty, Property expectedProperty, String message) {
        softAssertions.assertThat(actualProperty).as(message).isExactlyInstanceOf(expectedProperty.getClass());
        softAssertions.assertThat(actualProperty).as(message)
            .isEqualToComparingOnlyGivenFields(expectedProperty, BASIC_PROPERTIES);
    }

    private boolean isPropertyOfEnumType(Property property) {
        return property != null && StringProperty.class.isAssignableFrom(property.getClass())
            && CollectionUtils.isNotEmpty(((StringProperty) property).getEnum());
    }

    private void validateEnumPropertyFeatures(StringProperty actualStringProperty,
        StringProperty expectedStringProperty) {
        List<String> expectedEnums = expectedStringProperty.getEnum();
        if (CollectionUtils.isNotEmpty(expectedEnums)) {
            softAssertions.assertThat(actualStringProperty.getEnum()).hasSameElementsAs(expectedEnums);
        } else {
            softAssertions.assertThat(actualStringProperty.getEnum()).isNullOrEmpty();
        }
    }

}
