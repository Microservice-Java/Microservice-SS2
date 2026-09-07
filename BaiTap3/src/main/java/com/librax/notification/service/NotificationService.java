package com.librax.notification.service;

import com.librax.notification.dto.OverdueNotificationRequest;
import com.librax.notification.dto.OverdueNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationService {

    public OverdueNotificationResponse processOverdueNotification(OverdueNotificationRequest request) {
        log.info("[REST MSA] Processing overdue notification request for Member ID: {}, Book ID: {}, Due Date: {}",
                request.getMemberId(), request.getBookId(), request.getDueDate());

        // Giả lập logic gửi thông báo (Email / Push Notification / SMS)
        String detailMessage = String.format(
            "Đã gửi email nhắc nhở quá hạn thành công cho độc giả ID: %d cho cuốn sách ID: %d (Hạn trả: %s).",
            request.getMemberId(), request.getBookId(), request.getDueDate()
        );

        log.info("[REST MSA] SUCCESS: Notification dispatched.");

        return OverdueNotificationResponse.builder()
                .success(true)
                .message(detailMessage)
                .memberId(request.getMemberId())
                .bookId(request.getBookId())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
