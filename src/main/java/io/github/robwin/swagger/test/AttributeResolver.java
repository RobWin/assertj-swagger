package io.github.robwin.swagger.test;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

import java.util.Collections;
import java.util.List;

/**
 * Provide a means to retrieve values from various objects in the schema, providing a fallback to 'global'
 * settings if they're not defined locally.
 */
class AttributeResolver {

    private Swagger expected;
    private Swagger actual;

    public AttributeResolver(Swagger expected, Swagger actual) {
        this.expected = expected;
        this.actual = actual;
    }

    public List<String> getExpectedConsumes(Operation op) {
        return getListWithFallback(op.getConsumes(), expected.getConsumes());
    }

    public List<String> getActualConsumes(Operation op) {
        return getListWithFallback(op.getConsumes(), actual.getConsumes());
    }

    public List<String> getExpectedProduces(Operation op) {
        return getListWithFallback(op.getProduces(), expected.getProduces());
    }

    public List<String> getActualProduces(Operation op) {
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
