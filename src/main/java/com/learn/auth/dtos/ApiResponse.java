package com.learn.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Standard API response wrapper")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the request was processed successfully", example = "true")
    private boolean success;

    @Schema(description = "Human-readable status or error message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response payload data")
    private T data;

    @Schema(description = "Optional metadata (pagination, filtering, etc.)")
    private Object meta;

    @Schema(description = "Error details if the request failed")
    private Object errors;

    @Schema(description = "Server timestamp when the response was generated")
    private LocalDateTime timestamp;


    public static <T> ApiResponse<T> success(String message) {
        return success(message, null, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(message, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data, Object meta) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setMeta(meta);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrors(errors);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}