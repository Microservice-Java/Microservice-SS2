package com.librax;

import com.librax.client.BorrowingService;
import com.librax.esb.EsbSimulator;
import com.librax.esb.ServiceNotFoundException;
import com.librax.service.NotificationService;
import com.librax.service.PaymentService;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   DEMO BAI 2: SOA SERVICE CONTRACT & ESB SIMULATOR (LIBRAX SYSTEM)");
        System.out.println("================================================================================");

        // 1. Khởi tạo ESB Simulator Bus
        EsbSimulator esbBus = new EsbSimulator();

        // 2. Đăng ký các dịch vụ vào ESB Bus
        System.out.println("\n--- BƯỚC 1: ĐĂNG KÝ CÁC DỊCH VỤ VÀO ESB BUS ---");
        NotificationService notificationService = new NotificationService();
        PaymentService paymentService = new PaymentService();

        esbBus.registerService(notificationService);
        esbBus.registerService(paymentService);

        // 3. Khởi tạo BorrowingService (Bên gửi thông điệp)
        BorrowingService borrowingService = new BorrowingService(esbBus);

        // 4. Test Case 1: Gửi thông điệp hợp lệ (Định tuyến thành công qua NotificationService)
        System.out.println("\n--- BƯỚC 2: GỬI THÔNG ĐIỆP HỢP LỆ (notifyOverdue) ---");
        try {
            borrowingService.processOverdueCheck(101L, 502L, LocalDate.now().minusDays(3));
            System.out.println("-> TEST CASE 1 PASSED: Thông điệp đã được ESB định tuyến và NotificationService xử lý thành công!");
        } catch (Exception e) {
            System.err.println("-> TEST CASE 1 FAILED: " + e.getMessage());
        }

        // 5. Test Case 2: Gửi thông điệp tới tên dịch vụ không tồn tại (Kiểm tra xử lý lỗi & logging)
        System.out.println("\n--- BƯỚC 3: GỬI THÔNG ĐIỆP TỚI SERVICE KHÔNG TỒN TẠI (UnknownService) ---");
        try {
            borrowingService.sendInvalidServiceMessage();
        } catch (ServiceNotFoundException e) {
            System.out.println("-> TEST CASE 2 PASSED: ESB đã ghi log cảnh báo và ném ngoại lệ đúng như kỳ vọng: " + e.getMessage());
        }

        System.out.println("\n================================================================================");
        System.out.println("   HOÀN THÀNH KIỂM THỬ DEMO BAI 2!");
        System.out.println("================================================================================");
    }
}
