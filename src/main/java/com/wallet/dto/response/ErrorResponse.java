package com.wallet.dto.response;

import java.time.Instant;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;

    public ErrorResponse(int status, String code, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}