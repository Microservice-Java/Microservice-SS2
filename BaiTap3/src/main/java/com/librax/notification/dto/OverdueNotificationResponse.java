package com.librax.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueNotificationResponse {
    private boolean success;
    private String message;
    private Long memberId;
    private Long bookId;
    private LocalDateTime timestamp;
}
