package com.theladders.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        Map<String, Object> body;
        if (cause instanceof MismatchedInputException mismatchedInputException) {
            body = Map.of(
                    "error", "Bad Request",
                    "message", "Request body is missing or has an invalid value for a required field",
                    "field", fieldPath(mismatchedInputException)
            );
        } else {
            body = Map.of(
                    "error", "Bad Request",
                    "message", cause.getMessage() != null ? cause.getMessage() : "Request body could not be parsed as valid JSON"
            );
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private static String fieldPath(MismatchedInputException ex) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference reference : ex.getPath()) {
            if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            } else {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(reference.getPropertyName());
            }
        }
        return path.toString();
    }
}
