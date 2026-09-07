package com.librax.contract;

import java.time.LocalDateTime;

public class NotifyOverdueResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public NotifyOverdueResponse() {
    }

    public NotifyOverdueResponse(boolean success, String message, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NotifyOverdueResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
