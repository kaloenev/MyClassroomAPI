package com.alibou.security.exceptionHandling;

import org.springframework.http.HttpStatus;

/**
 * @author Kaloyan Enev
 * @version 1.0
 * This class is used to throw custom exceptions in the Fallob system.
 */
public class CustomException extends Exception {

    private HttpStatus status;
    private String message;

    /**
     * Constructor that sets the HttpStatus and message
     *
     * @param status  the HttpStatus to be printed
     * @param message the message to be printed
     */
    public CustomException(HttpStatus status, String message) {
        this.setStatus(status);
        this.setMessage(message);
    }

    /**
     * Constructor that sets the HttpStatus, message and cause
     *
     * @param status  the HttpStatus to be printed
     * @param message the message to be printed
     * @param cause   the cause of the Exception
     */
    public CustomException(HttpStatus status, String message, Throwable cause) {
        this.setStatus(status);
        this.setMessage(message);
        this.initCause(cause);
    }

    /**
     * Getter
     *
     * @return the HttpStatus to be printed
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Setter
     *
     * @param status the HttpStatus to be printed
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * Getter
     *
     * @return the message to be printed
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Setter
     *
     * @param message the message to be printed
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Logs where the Exception was thrown in Fallob
     */
    @Override
    public void printStackTrace() {
        if (this.getCause() != null) {
            this.getCause().printStackTrace();
        } else {
            super.printStackTrace();
        }
    }
}
