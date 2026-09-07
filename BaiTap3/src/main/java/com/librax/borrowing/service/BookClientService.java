package com.librax.borrowing.service;

import com.librax.borrowing.dto.BookResponse;
import com.librax.common.exception.BookServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BookClientService {

    private static final Logger log = LoggerFactory.getLogger(BookClientService.class);

    // Tiêm Bean @LoadBalanced RestTemplate từ Spring Context thay vì tự 'new RestTemplate()'
    private final RestTemplate restTemplate;

    /**
     * Lấy tên đầu sách từ book-service.
     * 
     * ĐÃ SỬA LỖI:
     * 1. Sử dụng tên dịch vụ logic 'http://book-service' thay cho địa chỉ IP cứng 'http://192.168.1.15:8082'.
     *    Spring Cloud LoadBalancer sẽ tự động phân giải IP từ Service Discovery và cân bằng tải giữa các instance.
     * 2. Bổ sung cơ chế xử lý lỗi try-catch (Fault Tolerance), ngăn ngừa sập dây chuyền (Cascading Failure).
     */
    public String getBookTitle(Long bookId) {
        // Tên dịch vụ logic đăng ký với Service Registry (Eureka/K8s)
        String url = "http://book-service/api/books/" + bookId;

        try {
            log.info("Sending GET request to logical service URL: {} to fetch book title...", url);
            
            // Có thể nhận về DTO hoặc String
            BookResponse bookResponse = restTemplate.getForObject(url, BookResponse.class);
            if (bookResponse != null && bookResponse.getTitle() != null) {
                return bookResponse.getTitle();
            }
            return "Unknown Book Title";

        } catch (HttpClientErrorException.NotFound e) {
            // Xử lý khi book-service trả về lỗi 404 (Không tìm thấy sách)
            log.warn("Book not found in book-service for ID: {}. Response message: {}", bookId, e.getMessage());
            return "Không tìm thấy thông tin sách (ID: " + bookId + ")";

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Xử lý các lỗi 4xx/5xx HTTP Status Code
            log.error("HTTP error occurred when calling book-service for ID: {}. Status: {}, Response: {}",
                    bookId, e.getStatusCode(), e.getResponseBodyAsString());
            
            // Trả về Fallback Title để bảo vệ borrowing-service không bị sập
            return getFallbackBookTitle(bookId, "Dịch vụ book-service báo lỗi HTTP: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            // Xử lý khi book-service bị ngắt kết nối (Down), Network Failure hoặc Timeout
            log.error("ResourceAccessException: Connection to book-service failed or timed out for ID: {}. Error: {}",
                    bookId, e.getMessage());
            
            // Trả về Fallback Value hoặc ném Custom Exception có kiểm soát
            return getFallbackBookTitle(bookId, "Hệ thống book-service hiện không phản hồi (Timeout/Down)");

        } catch (RestClientException e) {
            // Bắt tất cả các lỗi RestClientException khác
            log.error("Unexpected RestClientException when calling book-service for ID: {}", bookId, e);
            throw new BookServiceUnavailableException("Không thể kết nối đến book-service: " + e.getMessage(), e);
        }
    }

    /**
     * Cơ chế Fallback giả lập (hoặc có thể kết hợp Resilience4j CircuitBreaker / Fallback method)
     */
    private String getFallbackBookTitle(Long bookId, String reason) {
        log.warn("Executing Fallback strategy for bookId: {}. Reason: {}", bookId, reason);
        return String.format("[Tạm thời không khả dụng - %s]", reason);
    }
}
