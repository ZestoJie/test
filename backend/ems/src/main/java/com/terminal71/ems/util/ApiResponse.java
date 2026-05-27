package com.terminal71.ems.util;

public class ApiResponse<T> {

    final private T data;
    final private String message;

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
