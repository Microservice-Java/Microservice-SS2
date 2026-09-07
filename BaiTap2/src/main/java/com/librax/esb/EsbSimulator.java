package com.librax.esb;

import com.librax.service.ServiceInterface;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ESB (Enterprise Service Bus) Simulator - Tầng trung gian định tuyến thông điệp giữa các dịch vụ trong kiến trúc SOA.
 */
public class EsbSimulator {

    private static final Logger logger = Logger.getLogger(EsbSimulator.class.getName());

    // Registry quản lý các dịch vụ đã đăng ký với ESB Bus
    private final Map<String, ServiceInterface> serviceRegistry = new ConcurrentHashMap<>();

    /**
     * Đăng ký một dịch vụ với ESB Bus
     */
    public void registerService(ServiceInterface service) {
        if (service != null && service.getServiceName() != null) {
            serviceRegistry.put(service.getServiceName(), service);
            logger.info(String.format("[ESB BUS] Service registered successfully: %s", service.getServiceName()));
        }
    }

    /**
     * Định tuyến thông điệp từ Service phát đến Service nhận.
     * 
     * ĐÃ SỬA LỖI:
     * 1. So sánh chuỗi bằng Objects.equals() hoặc phương thức .equals() thay vì toán tử '=='.
     * 2. Bổ sung nhánh xử lý khi 'toService' không khớp bất kỳ dịch vụ nào (logging cảnh báo & ném ngoại lệ rõ ràng).
     */
    public void routeMessage(String toService, String operation, String payload) {
        logger.info(String.format("[ESB BUS] Intercepted message destined for Service: '%s', Operation: '%s'", toService, operation));

        // Phân tích & Sửa lỗi 1: Kiểm tra null an toàn và tìm dịch vụ trong registry bằng .equals()
        ServiceInterface targetService = null;

        if (toService != null) {
            // Duyệt registry hoặc lấy trực tiếp từ Map (sử dụng .equals() bên trong HashMap/ConcurrentHashMap)
            for (Map.Entry<String, ServiceInterface> entry : serviceRegistry.entrySet()) {
                if (Objects.equals(entry.getKey(), toService)) { // SỬA LỖI: dùng .equals() / Objects.equals() thay vì '=='
                    targetService = entry.getValue();
                    break;
                }
            }
        }

        // Phân tích & Sửa lỗi 2: Bổ sung nhánh xử lý khi không tìm thấy dịch vụ đăng ký
        if (targetService != null) {
            logger.info(String.format("[ESB BUS] Routing message successfully to '%s'...", toService));
            targetService.handle(operation, payload);
        } else {
            // Ghi log cảnh báo mức SEVERE/WARNING chi tiết
            String errorMessage = String.format(
                "[ESB ROUTING FAILURE] Cannot route message! Target service '%s' is not registered in ESB. Operation: '%s', Payload: %s",
                toService, operation, payload
            );
            logger.severe(errorMessage);

            // Ném ngoại lệ để bên gọi biết và xử lý (hoặc đẩy vào Dead Letter Queue), tránh lỗi Silent Failure
            throw new ServiceNotFoundException(errorMessage);
        }
    }
}
