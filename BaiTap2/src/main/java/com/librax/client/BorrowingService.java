package com.librax.client;

import com.librax.contract.NotifyOverdueRequest;
import com.librax.esb.EsbSimulator;

import java.time.LocalDate;
import java.util.logging.Logger;

/**
 * BorrowingService - Dịch vụ mượn trả sách phát hiện các trường hợp quá hạn và gửi thông điệp tới NotificationService qua ESB Bus.
 */
public class BorrowingService {

    private static final Logger logger = Logger.getLogger(BorrowingService.class.getName());
    private final EsbSimulator esbSimulator;

    public BorrowingService(EsbSimulator esbSimulator) {
        this.esbSimulator = esbSimulator;
    }

    /**
     * Phát hiện sách quá hạn và gửi thông điệp nhắc nhở qua ESB Bus
     */
    public void processOverdueCheck(Long memberId, Long bookId, LocalDate dueDate) {
        logger.info(String.format("[BorrowingService] Detected overdue book! MemberId: %d, BookId: %d, DueDate: %s", memberId, bookId, dueDate));

        // 1. Đóng gói payload theo Hợp đồng dịch vụ (Service Contract)
        NotifyOverdueRequest request = new NotifyOverdueRequest(memberId, bookId, dueDate);
        String payloadJson = String.format("{\"memberId\":%d,\"bookId\":%d,\"dueDate\":\"%s\"}",
                request.getMemberId(), request.getBookId(), request.getDueDate());

        // 2. Gửi thông điệp tới ESB Simulator
        logger.info("[BorrowingService] Sending 'notifyOverdue' message to ESB Bus...");
        esbSimulator.routeMessage("NotificationService", "notifyOverdue", payloadJson);
    }

    /**
     * Thử nghiệm gửi thông điệp tới service không tồn tại để kiểm tra khả năng xử lý lỗi của ESB Bus
     */
    public void sendInvalidServiceMessage() {
        logger.info("[BorrowingService] Sending message to an invalid service 'UnknownService'...");
        esbSimulator.routeMessage("UnknownService", "notifyOverdue", "{\"data\":\"test\"}");
    }
}
