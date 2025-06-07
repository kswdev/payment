package com.example.backend.adaptor.in.web.dto.response;

public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> with(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
}
