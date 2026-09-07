package com.librax.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueNotificationRequest {

    @NotNull(message = "memberId không được để trống")
    private Long memberId;

    @NotNull(message = "bookId không được để trống")
    private Long bookId;

    @NotNull(message = "dueDate không được để trống")
    private LocalDate dueDate;
}
