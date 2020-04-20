package io.github.robwin.swagger.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.IOException;
import java.io.Reader;

public class JsonSchemaValidator {

    private JsonNode schemaObj;

    public JsonSchemaValidator(Reader schemaSpec) throws IOException {
        this.schemaObj = JsonLoader.fromReader(schemaSpec);
    }

    public JsonSchemaFactory getSchemaFactory(final LogLevel logLevel, final LogLevel exceptionThreshold) {
        return JsonSchemaFactory
            .newBuilder()
            .setValidationConfiguration(ValidationConfiguration.byDefault())
            .setReportProvider(new ListReportProvider(logLevel, exceptionThreshold))
            .freeze();
    }

    public ProcessingReport validateSchemaWithParams(String definition,JsonNode jsonNode) throws ProcessingException {
        JsonSchemaFactory schemaVal = getSchemaFactory(LogLevel.INFO, LogLevel.FATAL);
        JsonSchema jsonSchemaVal = schemaVal.getJsonSchema(schemaObj,definition);
        return jsonSchemaVal.validate(jsonNode);
    }



}
