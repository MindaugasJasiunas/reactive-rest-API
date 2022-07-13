package com.example.demo;

import org.springframework.http.HttpStatus;

public record HttpErrorResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {}
