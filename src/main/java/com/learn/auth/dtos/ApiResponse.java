package com.learn.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object meta;
    private Object errors;
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

    public static <T> ApiResponse<List<T>> success(String message, Page<T> page) {
        ApiResponse<List<T>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(page.getContent()); // Slices out the raw list of DTOs/Entities
        response.setMeta(PaginationMeta.from(page)); // Maps pagination details to meta
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

}