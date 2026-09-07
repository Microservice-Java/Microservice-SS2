# LibraX System - SS2 Bài Tập 3: Chuyển Đổi Từ SOA Sang Microservice Architecture Bằng REST API

Tài liệu phân tích kỹ thuật, giải pháp sửa lỗi cho `BookClientService` (Service Discovery & Load Balancing, Fault Tolerance), thiết kế **REST API Endpoint** thay thế cho hợp đồng ESB Bài 2, và **Bài phân tích chuyên sâu so sánh SOA vs. Microservice Architecture (MSA)** trong bối cảnh hệ thống quản lý thư viện **LibraX**.

---

## 1. Phân Tích Kỹ Thuật & Giải Pháp Cho `BookClientService`

### 1.1 Vấn đề của việc gọi thẳng địa chỉ IP cố định `192.168.1.15:8082`

#### Đoạn code ban đầu (Có lỗi):
```java
@Service
public class BookClientService {
    private RestTemplate restTemplate = new RestTemplate();

    public String getBookTitle(Long bookId) {
        // Gọi thẳng một địa chỉ IP cố định của đúng 1 instance book-service
        String url = "http://192.168.1.15:8082/api/books/" + bookId;
        return restTemplate.getForObject(url, String.class);
    }
}
```

#### Phân tích nguyên nhân & Rủi ro:
1. **Vi phạm nguyên tắc Service Discovery trong Cloud Native**:
   - Trong môi trường Microservices (sử dụng Docker, Kubernetes, Netflix Eureka, Consul), các instance dịch vụ `book-service` được tự động khởi tạo, co giãn (Auto-scaling) và gán các địa chỉ IP động không cố định.
   - Việc hardcode IP `192.168.1.15:8082` sẽ khiến ứng dụng thất bại ngay lập tức khi instance này bị sập, khởi động lại hoặc đổi IP.
2. **Mất khả năng Cân bằng tải (Load Balancing)**:
   - Khi `book-service` được scale thành 5 hay 10 instances để gánh tải, `borrowing-service` vẫn luôn dồn 100% traffic vào duy nhất instance `192.168.1.15`, gây nghẽn cổ chai và lãng phí tài nguyên của các instance còn lại.
3. **Điểm sập đơn lẻ (Single Point of Failure - SPOF)**:
   - Phụ thuộc hoàn toàn vào 1 IP duy nhất khiến toàn bộ chức năng mượn/trả sách bị ngừng trệ nếu máy chủ chứa IP đó gặp sự cố.

#### Giải pháp sửa đổi:
- Đăng ký `book-service` với Service Registry (Eureka/Consul/Kubernetes DNS).
- Thay địa chỉ IP cứng bằng **Tên dịch vụ logic (Logical Service Name)**: `"http://book-service/api/books/" + bookId`.
- Sử dụng `@LoadBalanced RestTemplate` (Spring Cloud LoadBalancer) để tự động tra cứu IP động từ Service Registry và phân tải Round-Robin/Random giữa các instance.

---

### 1.2 Bổ sung cơ chế Khả năng chịu lỗi (Fault Tolerance)

#### Phân tích nguy cơ gây sập dây chuyền (Cascading Failure):
- Nếu `book-service` bị ngắt kết nối (Down), quá tải hoặc phản hồi chậm (Timeout), việc gọi `restTemplate.getForObject(...)` mà không có try-catch sẽ ném ra ngoại lệ `RestClientException` (`ResourceAccessException`, `HttpServerErrorException`).
- Ngoại lệ không được xử lý này sẽ lan truyền ngược lên `borrowing-service`, khiến luồng xử lý mượn sách bị crash hoàn toàn, mặc dù các dịch vụ khác vẫn hoạt động bình thường.

#### Giải pháp khắc phục:
- Tiêm Bean `@LoadBalanced RestTemplate` từ Spring Context.
- Bọc cuộc gọi mạng trong khối `try-catch` với các ngoại lệ REST cụ thể (`HttpClientErrorException.NotFound`, `HttpServerErrorException`, `ResourceAccessException`).
- Áp dụng chiến lược **Fallback Strategy**: Khi `book-service` gặp sự cố, trả về thông tin mặc định (Fallback title) hoặc log cảnh báo để bảo vệ `borrowing-service` tiếp tục vận hành an toàn.

---

### 1.3 Mã nguồn `BookClientService.java` hoàn chỉnh

```java
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

    // Tiêm Bean @LoadBalanced RestTemplate từ Spring Context
    private final RestTemplate restTemplate;

    public String getBookTitle(Long bookId) {
        // Sử dụng Logical Service Name thay vì địa chỉ IP cứng
        String url = "http://book-service/api/books/" + bookId;

        try {
            log.info("Sending GET request to logical service URL: {}...", url);
            BookResponse bookResponse = restTemplate.getForObject(url, BookResponse.class);
            if (bookResponse != null && bookResponse.getTitle() != null) {
                return bookResponse.getTitle();
            }
            return "Unknown Book Title";

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Book not found in book-service for ID: {}. Message: {}", bookId, e.getMessage());
            return "Không tìm thấy thông tin sách (ID: " + bookId + ")";

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error from book-service for ID: {}. Status: {}", bookId, e.getStatusCode());
            return getFallbackBookTitle(bookId, "Dịch vụ book-service báo lỗi HTTP: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException: Connection to book-service timed out/down for ID: {}. Error: {}", bookId, e.getMessage());
            return getFallbackBookTitle(bookId, "Hệ thống book-service hiện không phản hồi (Timeout/Down)");

        } catch (RestClientException e) {
            log.error("Unexpected RestClientException for bookId: {}", bookId, e);
            throw new BookServiceUnavailableException("Không thể kết nối đến book-service: " + e.getMessage(), e);
        }
    }

    private String getFallbackBookTitle(Long bookId, String reason) {
        log.warn("Executing Fallback strategy for bookId: {}. Reason: {}", bookId, reason);
        return String.format("[Tạm thời không khả dụng - %s]", reason);
    }
}
```

---

## 2. REST API Endpoint Thay Thế Cho Hợp Đồng SOA (Bài 2)

Thay vì đi qua giao thức phức tạp của ESB (SOAP/WSDL), trong kiến trúc Microservices, giao tiếp giữa các dịch vụ chuyển sang giao thức **RESTful HTTP/JSON** nhẹ nhàng và trực tiếp.

### 2.1 Thông tin Endpoint REST

- **HTTP Method**: `POST`
- **URL Path**: `/api/v1/notifications/overdue`
- **Content-Type**: `application/json`

### 2.2 Request Payload JSON (`OverdueNotificationRequest`)
```json
{
  "memberId": 101,
  "bookId": 502,
  "dueDate": "2026-09-04"
}
```

### 2.3 Response Payload JSON (`OverdueNotificationResponse`)
```json
{
  "success": true,
  "message": "Đã gửi email nhắc nhở quá hạn thành công cho độc giả ID: 101 cho cuốn sách ID: 502 (Hạn trả: 2026-09-04).",
  "memberId": 101,
  "bookId": 502,
  "timestamp": "2026-09-07T22:30:00.123"
}
```

### 2.4 Mã nguồn `NotificationController.java`
```java
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

    @PostMapping("/overdue")
    public ResponseEntity<OverdueNotificationResponse> notifyOverdue(
            @Valid @RequestBody OverdueNotificationRequest request) {
        
        OverdueNotificationResponse response = notificationService.processOverdueNotification(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
```

---

## 3. Phân Tích So Sánh Ưu, Nhược Điểm: Chuyển Đổi Từ SOA Sang MSA Trong Bối Cảnh LibraX

*(Bài phân tích chuyên sâu ~380 từ áp dụng trực tiếp cho hệ thống quản lý thư viện LibraX)*

### **Bài Phân Tích: Chuyển Đổi Kiến Trúc Từ SOA Sang Microservices Tại LibraX**

Việc chuyển đổi kiến trúc từ **SOA (Service-Oriented Architecture với trục trung gian ESB)** sang **MSA (Microservices Architecture với giao tiếp REST API trực tiếp)** đánh dấu bước ngoặt quan trọng trong hành trình chuyển đổi số của hệ thống quản lý thư viện **LibraX**. Dưới đây là phân tích ưu và nhược điểm của sự chuyển dịch này trên 3 khía cạnh cốt lõi:

#### **1. Tốc độ phát triển và tính linh hoạt (Development Agility)**
Trong kiến trúc SOA cũ, mỗi khi cần thêm tính năng mới hoặc thay đổi hợp đồng giao tiếp (như thông điệp `notifyOverdue`), nhóm phát triển `borrowing-service` và `notification-service` buộc phải thông qua đội vận hành ESB để cấu hình lại các luồng định tuyến (routing rules) và cập nhật sơ đồ XML WSDL phức tạp. Điều này tạo ra nút cổ chai phân tán và làm chậm tiến độ phát hành. Chuyển sang MSA với REST API/JSON nhẹ nhàng giúp các đội nghiệp vụ độc lập (`book`, `member`, `borrowing`, `notification`) toàn quyền tự định nghĩa hợp đồng API, lập trình, kiểm thử và triển khai tính năng mới (CI/CD) một cách nhanh chóng mà không bị phụ thuộc vào tầng trung gian.

#### **2. Độ phức tạp vận hành (Operational Complexity)**
Mặc dù SOA có nhược điểm về sự cồng kềnh, trục ESB lại đóng vai trò điểm quản trị tập trung (Centralized Governance) cho việc định tuyến, logging và bảo mật. Khi chuyển sang MSA, LibraX đã loại bỏ hoàn toàn nút cổ chai ESB, nhưng đổi lại độ phức tạp vận hành hạ tầng tăng lên đáng kể. Để hệ thống Microservices hoạt động ổn định khi scale đa instance, LibraX phải đầu tư và quản lý các thành phần hạ tầng phân tán như **Service Registry (Eureka/Consul)** để tra cứu địa chỉ IP động, **Spring Cloud LoadBalancer** để phân tải, **API Gateway** để định tuyến ngõ vào, và **Distributed Tracing (Zipkin/Sleuth)** để truy vết yêu cầu qua nhiều dịch vụ.

#### **3. Khả năng chịu lỗi và tính độc lập (Fault Tolerance & Resilience)**
Trong SOA, trục trung gian ESB chính là điểm sập đơn lẻ (Single Point of Failure - SPOF); nếu ESB Bus gặp sự cố, toàn bộ kết nối giữa các dịch vụ mượn sách, độc giả và thông báo đều sập theo. Ngược lại, MSA áp dụng triết lý *"Smart endpoints and dumb pipes"* — các dịch vụ giao tiếp REST trực tiếp với nhau. Khi kết hợp với các cơ chế chịu lỗi tại client như `@LoadBalanced RestTemplate`, `try-catch` bắt lỗi mạng và **Circuit Breaker (Resilience4j)**, `borrowing-service` có thể tự kích hoạt chiến lược Fallback khi `book-service` bị timeout hoặc ngắt kết nối. Nhờ đó, giao dịch của độc giả không bị crash dây chuyền (Cascading Failure), nâng cao đáng kể tính sẵn sàng (Availability) và khả năng chịu tải của hệ thống LibraX.

---

## 4. Hướng Dẫn Khởi Chạy Dự Án

Trong thư mục `d:\microservice\BaiTap\SS2\BaiTap3`:
```bash
# Biên dịch dự án
gradle build

# Khởi chạy dự án Spring Boot Microservices Demo
gradle bootRun
```
Ứng dụng sẽ chạy tại port `8083`. Endpoint thử nghiệm: `POST http://localhost:8083/api/v1/notifications/overdue`.
