package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributesMap = super.getErrorAttributes(request, options);
        Throwable throwable = getError(request);

        log.error(String.format("ERROR OCCURRED: %s", throwable));

        if(throwable instanceof RuntimeException){
            RuntimeException ex = (RuntimeException) throwable;
            errorAttributesMap.put("message", ex.getMessage());
            errorAttributesMap.put("status", HttpStatus.BAD_REQUEST.value());
            errorAttributesMap.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        }
        if(throwable instanceof AuthenticationCredentialsNotFoundException){  // Unauthorized
            AuthenticationCredentialsNotFoundException ex = (AuthenticationCredentialsNotFoundException) throwable;
            errorAttributesMap.put("message", ex.getMessage());
            // set status as UNAUTHORIZED (instead of INTERNAL_SERVER_ERROR)
            errorAttributesMap.put("status", HttpStatus.UNAUTHORIZED.value());
            errorAttributesMap.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
        if(throwable instanceof AccessDeniedException){  // Forbidden (not enough authorities)
            AccessDeniedException ex = (AccessDeniedException) throwable;
            errorAttributesMap.put("message", ex.getMessage());
            errorAttributesMap.put("status", HttpStatus.FORBIDDEN.value());
            errorAttributesMap.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        }
        if(throwable instanceof IllegalStateException){
            IllegalStateException ex = (IllegalStateException) throwable;
            errorAttributesMap.put("message", ex.getMessage());
            errorAttributesMap.put("status", HttpStatus.BAD_REQUEST.value());
            errorAttributesMap.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
            if(ex.getMessage() != null && ex.getMessage().startsWith("Required property")) {
                log.error(String.format("DATA SORTING ERROR: %s", ex.getMessage()));
                errorAttributesMap.put("message", "Error occurred when trying to sort by non existant field.");
            }
        }
        if(throwable instanceof ResponseStatusException){
            ResponseStatusException ex = (ResponseStatusException) throwable;
            errorAttributesMap.put("message", ex.getReason());
        }

        // if SQL error - hide error from user & log
        if(throwable.getMessage() != null && throwable.getMessage().startsWith("executeMany; SQL")){
            log.error(String.format("SQL ERROR: %s", throwable.getMessage()));
            errorAttributesMap.put("message", "Error occurred. Please try again later.");
        }

        // default status code
        errorAttributesMap.putIfAbsent("status", HttpStatus.BAD_REQUEST.value());
        return errorAttributesMap;
    }
}
