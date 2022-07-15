package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
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

        if(throwable instanceof AuthenticationCredentialsNotFoundException){  // Unauthorized
            AuthenticationCredentialsNotFoundException ex = (AuthenticationCredentialsNotFoundException) throwable;
            errorAttributesMap.put("message", ex.getMessage());
            // set status as UNAUTHORIZED (instead of INTERNAL_SERVER_ERROR)
            errorAttributesMap.put("status", HttpStatus.UNAUTHORIZED.value());
            errorAttributesMap.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
        if(throwable instanceof ResponseStatusException){
            ResponseStatusException ex = (ResponseStatusException) throwable;

            // if SQL error - hide error from user & log
            if(ex.getReason() != null && ex.getReason().startsWith("executeMany; SQL")){
                log.error(String.format("SQL ERROR: %s", ex.getReason()));
                errorAttributesMap.put("message", "Error occurred. Please try again later.");
//                errorAttributesMap.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//                errorAttributesMap.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            }else{
                errorAttributesMap.put("message", ex.getReason());
            }
        }
        // default status code
        errorAttributesMap.putIfAbsent("status", HttpStatus.BAD_REQUEST.value());
        return errorAttributesMap;
    }
}
