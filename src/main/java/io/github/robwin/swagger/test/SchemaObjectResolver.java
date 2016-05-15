package io.github.robwin.swagger.test;

import io.swagger.models.*;
import io.swagger.models.properties.Property;

import java.util.*;


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

    Map<String, Property> resolvePropertiesFromExpected(Model definition) {
        return resolveProperties(definition, expected, new HashSet<String>());
    }

    Map<String, Property> resolvePropertiesFromActual(Model definition) {
        return resolveProperties(definition, actual, new HashSet<String>());
    }

    private Map<String, Property> resolveProperties(Model definition, Swagger owningSchema, Set<String> seenRefs) {
        Map<String, Property> result;

        final Map<String, Property> definitionProperties = definition.getProperties();

        if (definition instanceof RefModel) {
            // Don't navigate ref-def cycles infinitely
            final RefModel refDef = (RefModel) definition;

            if (seenRefs.contains(refDef.getSimpleRef())) {
                return Collections.emptyMap();
            } else {
                seenRefs.add(refDef.getSimpleRef());
            }
            result = resolveProperties(findDefinition(owningSchema.getDefinitions(), refDef), owningSchema, seenRefs);
        } else if (definition instanceof ComposedModel) {
            Map<String, Property> allProperties = new HashMap<>();
            if (definitionProperties != null) {
                allProperties.putAll(definitionProperties);
            }
            for (final Model childDefinition : ((ComposedModel)definition).getAllOf()) {
                allProperties.putAll(resolveProperties(childDefinition, owningSchema, seenRefs));
            }
            result = allProperties;
        } else {
            result = definitionProperties;
        }

        return result;
    }

    private Model findDefinition(Map<String, Model> defs, RefModel refModel) {
        return defs.get(refModel.getSimpleRef());
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

