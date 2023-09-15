package com.alibou.security.exceptionHandling;

import org.springframework.http.HttpStatus;

/**
 * @author Kaloyan Enev
 * @version 1.0
 * A class used to print API-Errors in case of unsupported requests (those caught by the RestExceptionHandling)
 */
public class ApiError {
    private final HttpStatus status;
    private final String message;

    /**
     * Constructor that sets the HttpStatus and message
     * @param status the HttpStatus to be printed
     * @param message the message to be printed
     */
    public ApiError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Getter
     * @return the HttpStatus to be printed
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Getter
     * @return the HttpStatus to be printed
     */
    public String getMessage() {
        return message;
    }
}