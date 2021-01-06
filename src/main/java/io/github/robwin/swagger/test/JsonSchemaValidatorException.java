package io.github.robwin.swagger.test;

public class JsonSchemaValidatorException extends Exception {
    @Override
    public String toString() {
        return "JsonSchemaValidatorException{" +
            "exceptionMessage='" + exceptionMessage + '\'' +
            '}';
    }

    String exceptionMessage;

    public JsonSchemaValidatorException(String message){
        exceptionMessage = message;
    }

}
