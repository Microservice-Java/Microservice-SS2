package com.librax.notification.controller;

import com.librax.notification.dto.OverdueNotificationRequest;
import com.librax.notification.dto.OverdueNotificationResponse;
import com.librax.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * REST Endpoint tương ứng với hợp đồng notifyOverdue (SOA/ESB) từ Bài 2,
     * chuyển đổi thành giao tiếp REST API trực tiếp / nhẹ nhàng theo chuẩn Microservices Architecture.
     * 
     * Endpoint: POST /api/v1/notifications/overdue
     */
    @PostMapping("/overdue")
    public ResponseEntity<OverdueNotificationResponse> notifyOverdue(
            @Valid @RequestBody OverdueNotificationRequest request) {
        
        OverdueNotificationResponse response = notificationService.processOverdueNotification(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
