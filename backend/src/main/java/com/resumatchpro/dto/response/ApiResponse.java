package com.resumatchpro.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    private Object errors;
    private LocalDateTime timestamp;

    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true).message(message)
                .timestamp(LocalDateTime.now()).build();
    }

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true).message(message).data(data)
                .timestamp(LocalDateTime.now()).build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false).message(message)
                .timestamp(LocalDateTime.now()).build();
    }

    public static ApiResponse error(String message, Object errors) {
        return ApiResponse.builder()
                .success(false).message(message).errors(errors)
                .timestamp(LocalDateTime.now()).build();
    }
}
