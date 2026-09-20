package com.djmart.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Standard API response envelope used across all REST JSON endpoints.
 * Conforms to both:
 * { "success": true, "message": "...", "data": {...} }
 * and
 * { "success": false, "message": "...", "errorCode": "...", "error": {...} }
 *
 * @param <T> Payload data type
 */
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private ApiError error;

    public ApiResponse() {
    }

    private ApiResponse(boolean success, String message, String errorCode, T data, ApiError error) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.error = error;
    }

    /**
     * Creates a success response with data and a default success message.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", null, data, null);
    }

    /**
     * Creates a success response with a custom message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data, null);
    }

    /**
     * Creates an error response with an error message and errorCode.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiError err = new ApiError(errorCode, message);
        return new ApiResponse<>(false, message, errorCode, null, err);
    }

    /**
     * Creates a validation error response with field errors.
     */
    public static <T> ApiResponse<T> validationError(String message, Map<String, String> fieldErrors) {
        ApiError err = new ApiError("VALIDATION_ERROR", message, fieldErrors);
        return new ApiResponse<>(false, message, "VALIDATION_ERROR", null, err);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", data=" + data +
                ", error=" + error +
                '}';
    }
}
