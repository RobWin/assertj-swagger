package io.github.robwin.swagger.test;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

import java.util.Collections;
import java.util.List;

/**
 * Provide a means to retrieve values from various objects in the schema.  Provides a means of falling back to 'global'
 * settings if they're not defined locally in a definition or path object.  Also permits resolving local
 * {@code $ref}-erences and types making use of {@code allOf}-style inheritance.
 */
class SchemaObjectResolver {

    private Swagger expected;
    private Swagger actual;

    SchemaObjectResolver(Swagger expected, Swagger actual) {
        this.expected = expected;
        this.actual = actual;
    }

    List<String> getExpectedConsumes(Operation op) {
        return getListWithFallback(op.getConsumes(), expected.getConsumes());
    }

    List<String> getActualConsumes(Operation op) {
        return getListWithFallback(op.getConsumes(), actual.getConsumes());
    }

    List<String> getExpectedProduces(Operation op) {
        return getListWithFallback(op.getProduces(), expected.getProduces());
    }

    List<String> getActualProduces(Operation op) {
        return getListWithFallback(op.getProduces(), actual.getProduces());
    }

    private <A> List<A> getListWithFallback(List<A> localDefn, List<A> globalDefn) {
        final List<A> result;
        if (localDefn != null && !localDefn.isEmpty()) {
            result = localDefn;
        } else if (globalDefn != null) {
            result = globalDefn;
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

}

