package com.finflow.finflow.exception;

public record ApiError(
        int status,
        String message,
        String path
) {}
