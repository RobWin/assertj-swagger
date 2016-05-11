package io.github.robwin.swagger.test;

import io.swagger.models.Swagger;

public interface ContractValidator {
    void validateSwagger(Swagger expected, SchemaObjectResolver schemaObjectResolver);
}
