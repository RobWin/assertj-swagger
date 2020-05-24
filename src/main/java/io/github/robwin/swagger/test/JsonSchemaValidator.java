package io.github.robwin.swagger.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;

import java.io.Reader;
import java.util.Map;

public class JsonSchemaValidator {

    private JsonNode schemaObj;
    private Swagger actual;

    /**
     * @param schemaSpec - io.Reader of schema
     * @throws JsonSchemaValidatorException
     */
    public JsonSchemaValidator(Reader schemaSpec) throws JsonSchemaValidatorException {
        try {
            validateNotNull(schemaSpec);
            this.schemaObj = JsonLoader.fromReader(schemaSpec);
            this.actual = new SwaggerParser().read(schemaObj);
        }catch (NullPointerException nx){
            throw new JsonSchemaValidatorException("Schema Object cannot be null / empty");
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Invalid Schema Object, Unable to create schema object");
        }

    }

    /**
     * @param schemaSpec - String of schema
     * @throws JsonSchemaValidatorException
     */
    public JsonSchemaValidator(String schemaSpec) throws JsonSchemaValidatorException {
        try {
        validateNotNull(schemaSpec);
        this.schemaObj = JsonLoader.fromString(schemaSpec);
        this.actual = new SwaggerParser().read(schemaObj);
        }catch (NullPointerException nx){
            throw new JsonSchemaValidatorException("Schema Object cannot be null / empty");
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Invalid Schema Object, Unable to create schema object");
        }

    }

    /**
     * @param logLevel
     * @param exceptionThreshold
     *
     * @return - Javaschemafactory to validate schema
     */
    public JsonSchemaFactory getSchemaFactory(final LogLevel logLevel, final LogLevel exceptionThreshold) {
        return JsonSchemaFactory
            .newBuilder()
            .setValidationConfiguration(ValidationConfiguration.byDefault())
            .setReportProvider(new ListReportProvider(logLevel, exceptionThreshold))
            .freeze();
    }

    /**
     * @return - Javaschemafactory to validate schema
     */
    public JsonSchemaFactory getSchemaFactory() {
        return JsonSchemaFactory.byDefault();
    }

    /**
     * @param definition - Definition path in schema
     * @param jsonNode - Response json node to validate against schema
     *
     *                 This method can be used if validation needs to be done directly with Definition
     * @return
     * @throws JsonSchemaValidatorException
     */
    public Boolean validateSchemaWithDefinitionPath(String definition, JsonNode jsonNode) throws JsonSchemaValidatorException {
        try {
            JsonSchemaFactory schemaVal = getSchemaFactory(LogLevel.ERROR, LogLevel.FATAL);
            JsonSchema jsonSchemaVal = schemaVal.getJsonSchema(schemaObj, definition);
            ProcessingReport report =  jsonSchemaVal.validate(jsonNode);
            if(report.isSuccess()){
                return true;
            }else{
                System.err.println(report.toString());
                return false;
            }
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Unable to get parse schema object for given parameter");
        }
    }

    /**
     * @param jsonNode - Response json node to validate against schema
     *
     *             This method can be used if there is one to one schema mapping for each request
     *
     * @return
     * @throws JsonSchemaValidatorException
     */
    public Boolean validateSchema(JsonNode jsonNode) throws JsonSchemaValidatorException {
        try{
            JsonSchemaFactory schemaVal = getSchemaFactory(LogLevel.ERROR, LogLevel.FATAL);
            JsonSchema jsonSchemaVal = schemaVal.getJsonSchema(schemaObj);
            ProcessingReport report =  jsonSchemaVal.validate(jsonNode);
            if(report.isSuccess()){
                return true;
            }else{
                System.err.println(report);
                return false;
            }
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Unable to get parse schema object for given parameter");
        }
    }

    /**
     * @param endPoint - Request endpoint to validate schema
     * @param jsonNode - Response json node to validate against schema
     *
     * This method will extract Schema for given end point & request type [get] & response schema [200] and
     * validate jsonNode value against schema provided
     *
     * @return - True / false
     * @throws JsonSchemaValidatorException
     */
    public Boolean validateSchema(String endPoint, JsonNode jsonNode) throws JsonSchemaValidatorException {
        try{
            JsonSchemaFactory schemaVal = getSchemaFactory(LogLevel.ERROR, LogLevel.FATAL);
            schemaObj = JsonLoader.fromString(identifyResponseSchemaFromSwagger(endPoint, "get","200"));
            JsonSchema jsonSchemaVal = schemaVal.getJsonSchema(schemaObj);
            ProcessingReport report =  jsonSchemaVal.validate(jsonNode);
            if(report.isSuccess()){
                return true;
            }else{
                System.err.println(report);
                return false;
            }
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Unable to get parse schema object for given parameter");
        }

    }

    /**
     * @param endPoint - Request endpoint to validate schema
     * @param requestType - Request type [get,post,put,delete,patch]
     * @param jsonNode - Response json node to validate against schema
     *
     *  This method will extract Schema for given end point & requestType & response code [200] and
     *  validate jsonNode value against schema provided
     *
     * @return - True / False
     * @throws JsonSchemaValidatorException
     */
    public Boolean validateSchema(String endPoint, String requestType, JsonNode jsonNode) throws JsonSchemaValidatorException {
        try{
            JsonSchemaFactory schemaVal = getSchemaFactory(LogLevel.ERROR, LogLevel.FATAL);
            schemaObj = JsonLoader.fromString(identifyResponseSchemaFromSwagger(endPoint, requestType, "200"));
            JsonSchema jsonSchemaVal = schemaVal.getJsonSchema(schemaObj);
            ProcessingReport report =  jsonSchemaVal.validate(jsonNode);
            if(report.isSuccess()){
                return true;
            }else{
                System.err.println(report);
                return false;
            }
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Unable to get parse schema object for given parameter");
        }

    }

    /**
     * @param endPoint  - Request endpoint to validate schema
     * @param requestType  - Request type [get,post,put,delete,patch]
     * @param responseCode - Response code of schema [200,400 etc..]
     * @param jsonNode - Response json node to validate against schema
     *
     *      This method will extract Schema for given end point & requestType & responsecode and
     *      validate jsonNode value against schema provided
     * @return
     * @throws JsonSchemaValidatorException
     */
    public Boolean validateSchema(String endPoint, String requestType, String responseCode, JsonNode jsonNode) throws JsonSchemaValidatorException {
        try{
            JsonSchemaFactory schemaVal = getSchemaFactory(LogLevel.ERROR, LogLevel.FATAL);
            schemaObj = JsonLoader.fromString(identifyResponseSchemaFromSwagger(endPoint, requestType, responseCode));
            JsonSchema jsonSchemaVal = schemaVal.getJsonSchema(schemaObj);
            ProcessingReport report =  jsonSchemaVal.validate(jsonNode);
            if(report.isSuccess()){
                return true;
            }else{
                System.err.println(report);
                return false;
            }
        }catch (Exception e){
            throw new JsonSchemaValidatorException("Unable to get parse schema object for given parameter");
        }

    }

    /**
     * @param schema - validate schema object/value as not null
     */
    void validateNotNull(Object schema) {
        if (schema == null) {
            throw new NullPointerException("Schema Object cannot be null");
        }else if ((schema instanceof  String) && (((String) schema).isEmpty() || schema == null)){
            throw new NullPointerException("Schema Object cannot be null");
        }
    }

    /**
     * @param endPoint - Request End Point to validate schema
     * @param operation - Request Type of Schema [get,post,put,delete,patch]
     * @return
     * @throws JsonSchemaValidatorException
     */
    String identifyResponseSchemaFromSwagger(String endPoint, String operation, String responseCode) throws JsonSchemaValidatorException {
        try {
            Map<String, Response> sr;
            String jsonResponse;
            switch (operation.toLowerCase()) {
                case "get":
                    sr = actual.getPath(endPoint).getGet().getResponses();
                    break;

                case "post":
                    sr = actual.getPath(endPoint).getPost().getResponses();
                    break;

                case "put":
                    sr = actual.getPath(endPoint).getPut().getResponses();
                    break;

                case "delete":
                    sr = actual.getPath(endPoint).getDelete().getResponses();
                    break;

                case "patch":
                    sr = actual.getPath(endPoint).getPatch().getResponses();
                    break;

                default:
                    sr = null;
                    System.err.println("Invalid Request Type : "+operation);
                    throw new JsonSchemaValidatorException("Invalid Request Type ");

            }
            jsonResponse = Json.pretty(sr.get(responseCode).getResponseSchema());
            if (jsonResponse.contains("#")) {
                jsonResponse = getDefinition(jsonResponse);
            }
            return jsonResponse;
        }catch (Exception e){
            System.err.println("Unable to get response schema object with given parameter");
            throw new JsonSchemaValidatorException("Unable to get response schema object for given parameter");
        }

    }

    /**
     * @param jsonResponse
     * @return Schema add all definitions to solve reference issue
     * @throws JsonSchemaValidatorException
     */
    public String getDefinition(String jsonResponse) throws JsonSchemaValidatorException {
        try {
            String sr = "\"definitions\" : " + Json.pretty(actual.getDefinitions()) + ",";
            jsonResponse = jsonResponse.replace("{", "{\n" + sr);
            return jsonResponse;
        }catch (Exception e){
            System.err.println("Unable to get definition from schema");
            throw new JsonSchemaValidatorException("Unable to get definition from schema");
        }
    }
}
