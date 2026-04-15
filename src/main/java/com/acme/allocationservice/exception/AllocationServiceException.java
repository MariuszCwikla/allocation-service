package com.acme.allocationservice.exception;

/**
 * Represents generic domain logic exception that shall be reported as HTTP 400 in REST API.
 */
public class AllocationServiceException extends RuntimeException {
    public AllocationServiceException(String message) {
        super(message);
    }
}