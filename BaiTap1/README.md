# LibraX Library Management System - SS2 Bài Tập 1

Dự án tái cấu trúc hệ thống quản lý thư viện **LibraX** từ kiến trúc Monolith ban đầu (viết chung trong 1 Controller đơn lẻ) sang kiến trúc **Modular Monolith** phân chia theo Domain-Driven Package (`book`, `member`, `borrowing`).

---

## 1. Phân Tích Lỗi Logic và Giải Pháp Khắc Phục

### 1.1 Lỗi điều kiện kiểm tra giới hạn mượn sách (`canBorrowBook`)

#### Code ban đầu (Có lỗi):
```java
public boolean canBorrowBook(int currentBorrowedByMember) {
    // Quy định: mỗi độc giả chỉ được mượn tối đa 5 cuốn cùng lúc
    if (currentBorrowedByMember > 5) {
        return false;
    }
    return true; // Sai: khi currentBorrowedByMember = 5 vẫn cho mượn thêm cuốn thứ 6
}
```

#### Phân tích nguyên nhân:
- Điều kiện `currentBorrowedByMember > 5` chỉ trả về `false` khi số sách mượn **lớn hơn hẳn 5** (tức là 6 trở lên).
- Khi độc giả đang mượn **đúng 5 cuốn** (`currentBorrowedByMember == 5`), biểu thức `5 > 5` trả về `false`. Phương thức tiếp tục thực thi và trả về `true`. Điều này cho phép độc giả mượn thêm cuốn thứ 6, vi phạm quy định tối đa 5 cuốn của thư viện.

#### Giải pháp sửa lại:
Sử dụng toán tử `>=` (lớn hơn hoặc bằng):
```java
public boolean canBorrowBook(int currentBorrowedByMember) {
    if (currentBorrowedByMember >= 5) {
        return false;
    }
    return true;
}
```
Hoặc kiểm tra điều kiện sau khi cộng thêm 1 cuốn sắp mượn: `(currentBorrowedByMember + 1 > 5)`.

---

### 1.2 Vấn đề biến static `totalBorrowedBooks` (Shared Mutable State)

#### Code ban đầu (Có lỗi):
```java
private static int totalBorrowedBooks = 0;
```

#### Phân tích nguyên nhân & Nguy cơ:
1. **Shared Mutable State trong môi trường Multi-threaded**: Trong Spring Boot (chạy trên Web Server như Tomcat), mỗi HTTP request được xử lý đồng thời trên một thread độc lập. Việc nhiều thread cùng thay đổi (mutate) một biến `static` chung mà không có cơ chế đồng bộ (synchronization) sẽ dẫn đến tình trạng **Race Condition** và sai lệch dữ liệu.
2. **Vi phạm đóng gói Domain**: Biến `static` đặt ở Controller là một trạng thái toàn cục, không phân biệt mượn sách nào, của độc giả nào, và không theo dõi được lịch sử giao dịch.
3. **Mất dữ liệu khi Restart / Không thể Scale-out**: Dữ liệu lưu hoàn toàn trong bộ nhớ RAM của JVM process. Khi server bị restart hoặc được triển khai nhiều instance (scale-out đằng sau Load Balancer), biến static không thể chia sẻ giữa các instance và dữ liệu mượn sách sẽ bị mất hoặc bất đồng bộ.

#### Giải pháp loại bỏ:
- **Loại bỏ hoàn toàn biến `static`**.
- Quản lý trạng thái mượn/trả sách thông qua bảng CSDL `borrowings` và Spring Data JPA `BorrowingRepository`.
- Đếm số sách đang mượn của độc giả hoặc trên toàn hệ thống thông qua truy vấn CSDL động:
  ```java
  long activeBorrowings = borrowingRepository.countByMemberIdAndStatus(memberId, BorrowStatus.BORROWED);
  ```

---

## 2. Cấu Trúc Package Theo Domain (Modular Monolith)

Dự án được phân chia thành 3 domain chính, mỗi domain bao gồm đủ 3 tầng `Controller` - `Service` - `Repository`:

```text
d:\microservice\BaiTap\SS2\BaiTap1
├── build.gradle
├── settings.gradle
├── README.md
└── src
    └── main
        ├── java
        │   └── com
        │       └── librax
        │           ├── LibraXApplication.java
        │           │
        │           ├── book (Domain 1: Quản lý đầu sách)
        │           │   ├── controller
        │           │   │   └── BookController.java
        │           │   ├── service
        │           │   │   └── BookService.java
        │           │   ├── repository
        │           │   │   └── BookRepository.java
        │           │   ├── model
        │           │   │   └── Book.java
        │           │   └── dto
        │           │       ├── BookRequest.java
        │           │       └── BookResponse.java
        │           │
        │           ├── member (Domain 2: Quản lý độc giả)
        │           │   ├── controller
        │           │   │   └── MemberController.java
        │           │   ├── service
        │           │   │   └── MemberService.java
        │           │   ├── repository
        │           │   │   └── MemberRepository.java
        │           │   ├── model
        │           │   │   └── Member.java
        │           │   └── dto
        │           │       ├── MemberRequest.java
        │           │       └── MemberResponse.java
        │           │
        │           ├── borrowing (Domain 3: Quản lý mượn/trả sách)
        │           │   ├── controller
        │           │   │   └── BorrowingController.java
        │           │   ├── service
        │           │   │   └── BorrowingService.java
        │           │   ├── repository
        │           │   │   └── BorrowingRepository.java
        │           │   ├── model
        │           │   │   ├── Borrowing.java
        │           │   │   └── BorrowStatus.java
        │           │   └── dto
        │           │       ├── BorrowRequest.java
        │           │       └── BorrowResponse.java
        │           │
        │           └── common (Xử lý lỗi tập trung)
        │               └── exception
        │                   ├── ApiErrorResponse.java
        │                   ├── ResourceNotFoundException.java
        │                   ├── BorrowingDomainException.java
        │                   └── GlobalExceptionHandler.java
        │
        └── resources
            └── application.yml
```

---

## 3. Vì Sao Đây Vẫn Là Monolithic Architecture (Modular Monolith)?

Mặc dù mã nguồn được tổ chức rất sạch sẽ theo ranh giới domain độc lập (Domain-Driven Design), ứng dụng này **vẫn là một kiến trúc Monolith (Modular Monolith)** chứ chưa phải **Microservices** vì các lý do sau:

1. **Cùng 1 Đơn vị Triển khai (Single Deployment Unit)**:
   - Toàn bộ 3 domain (`book`, `member`, `borrowing`) được đóng gói chung vào **MỘT file `.jar` duy nhất** khi build Gradle.
2. **Cùng 1 Tiền trình Runtime (Single JVM Process)**:
   - Toàn bộ ứng dụng khởi chạy trong cùng 1 JVM Process. Tất cả các Controller và Service hoạt động chung trong một không gian bộ nhớ RAM duy nhất.
3. **Giao tiếp Nội bộ Trực tiếp (In-Memory Method Calls)**:
   - `BorrowingService` gọi đến `MemberService` và `BookService` thông qua **trực tiếp phương thức Java (Java method calls / Spring Bean Injection)**, không thông qua các giao thức mạng như HTTP REST API, gRPC hay Message Broker (RabbitMQ/Kafka).
4. **Chung Một Cơ Sở Dữ Liệu (Shared Database)**:
   - Cả 3 domain đều lưu trữ dữ liệu trong cùng một cơ sở dữ liệu (Database H2). Trong kiến trúc Microservices chuẩn, mỗi service phải quản lý cơ sở dữ liệu riêng biệt của nó (Database-per-service pattern).

### Ưu điểm của kiến trúc Modular Monolith:
- Giữ được sự đơn giản trong triển khai và vận hành của Monolith.
- Codebase được phân chia ranh giới domain rõ ràng, dễ bảo trì và mở rộng.
- Dễ dàng nâng cấp hoặc tách thành các Microservice độc lập trong tương lai khi ứng dụng phát triển lớn hơn.

---

## 4. Hướng Dẫn Khởi Chạy Và Test REST Endpoints

### 4.1 Biên dịch và Chạy ứng dụng

Trong thư mục `SS2/BaiTap1`:
```bash
# Biên dịch project
gradle build

# Khởi chạy ứng dụng Spring Boot
gradle bootRun
```
Ứng dụng sẽ chạy tại port `8080`. Bạn có thể truy cập H2 Console tại: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:libraxdb`, username: `sa`, password: `password`).

---

### 4.2 Danh sách REST Endpoints Mẫu

#### 1. Domain Book
- **Tạo sách mới (`POST /api/books`)**:
  ```json
  POST http://localhost:8080/api/books
  Content-Type: application/json

  {
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "totalCopies": 10
  }
  ```
- **Lấy thông tin sách theo ID (`GET /api/books/{id}`)**:
  ```http
  GET http://localhost:8080/api/books/1
  ```

#### 2. Domain Member
- **Đăng ký độc giả (`POST /api/members`)**:
  ```json
  POST http://localhost:8080/api/members
  Content-Type: application/json

  {
    "fullName": "Nguyen Van A",
    "email": "anguyen@example.com",
    "phone": "0912345678"
  }
  ```
- **Lấy thông tin độc giả theo ID (`GET /api/members/{id}`)**:
  ```http
  GET http://localhost:8080/api/members/1
  ```

#### 3. Domain Borrowing
- **Thực hiện mượn sách (`POST /api/borrowings`)**:
  ```json
  POST http://localhost:8080/api/borrowings
  Content-Type: application/json

  {
    "memberId": 1,
    "bookId": 1
  }
  ```
- **Trả sách (`POST /api/borrowings/{id}/return`)**:
  ```http
  POST http://localhost:8080/api/borrowings/1/return
  ```
- **Xem danh sách mượn theo độc giả (`GET /api/borrowings/member/{memberId}`)**:
  ```http
  GET http://localhost:8080/api/borrowings/member/1
  ```
