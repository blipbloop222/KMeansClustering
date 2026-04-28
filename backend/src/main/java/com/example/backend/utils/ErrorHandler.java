package com.example.backend.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized error handling and logging utility for the application.
 * Ensures consistent error reporting and logging across different components.
 */
public final class ErrorHandler {
    private static final Logger GLOBAL_LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    // Private constructor to prevent instantiation
    private ErrorHandler() {}

    /**
     * Logs and throws an illegal argument exception with a standardized message.
     * 
     * @param message Detailed error message
     * @throws IllegalArgumentException Always thrown with the given message
     */
    public static void handleInvalidArgument(String message) {
        GLOBAL_LOGGER.log(Level.SEVERE, "Invalid Argument: {0}", message);
        throw new IllegalArgumentException(message);
    }

    /**
     * Logs an error message without throwing an exception.
     * 
     * @param source The class or component where the error occurred
     * @param message Detailed error message
     */
    public static void logError(Class<?> source, String message) {
        GLOBAL_LOGGER.log(Level.SEVERE, "[{0}] {1}", new Object[]{source.getSimpleName(), message});
    }

    /**
     * Logs a warning message.
     * 
     * @param source The class or component where the warning occurred
     * @param message Detailed warning message
     */
    public static void logWarning(Class<?> source, String message) {
        GLOBAL_LOGGER.log(Level.WARNING, "[{0}] {1}", new Object[]{source.getSimpleName(), message});
    }

    /**
     * Validates that an object is not null.
     * 
     * @param obj Object to validate
     * @param paramName Name of the parameter for error messaging
     * @throws IllegalArgumentException if the object is null
     */
    public static void validateNotNull(Object obj, String paramName) {
        if (obj == null) {
            handleInvalidArgument(paramName + " cannot be null");
        }
    }
}