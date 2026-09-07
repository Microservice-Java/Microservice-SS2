package com.librax.borrowing.service;

import com.librax.borrowing.client.BookClient;
import com.librax.borrowing.client.MemberClient;
import com.librax.borrowing.dto.BookResponse;
import com.librax.borrowing.dto.BorrowingDetailResponse;
import com.librax.borrowing.dto.MemberResponse;
import com.librax.borrowing.model.Borrowing;
import com.librax.borrowing.repository.BorrowingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookClient bookClient;
    private final MemberClient memberClient;

    /**
     * TÁI THIẾT KẾ PHƯƠNG THỨC getBorrowingDetail THEO DATABASE-PER-SERVICE:
     * 
     * ĐÃ SỬA LỖI:
     * 1. Loại bỏ hoàn toàn câu lệnh SQL JOIN xuyên bảng sang 'books' và 'members' (vốn thuộc DB của service khác).
     * 2. Chỉ truy vấn CSDL 'borrowings_db' thuộc sở hữu độc quyền của borrowing-service để lấy record Borrowing.
     * 3. Áp dụng API Aggregation Pattern: Gọi song song (CompletableFuture) sang book-service và member-service 
     *    để lấy thông tin 'title' và 'name', sau đó tổng hợp thành BorrowingDetailResponse.
     */
    public BorrowingDetailResponse getBorrowingDetail(Long borrowingId) {
        log.info("Fetching borrowing record from borrowings_db for ID: {}", borrowingId);

        // 1. Chỉ truy vấn CSDL riêng của borrowing-service (bảng borrowings)
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với ID: " + borrowingId));

        Long bookId = borrowing.getBookId();
        Long memberId = borrowing.getMemberId();

        // 2. Thực hiện gọi API REST song song (Async API Aggregation) để tối ưu hiệu năng
        CompletableFuture<BookResponse> bookFuture = CompletableFuture.supplyAsync(
                () -> bookClient.getBookById(bookId)
        );

        CompletableFuture<MemberResponse> memberFuture = CompletableFuture.supplyAsync(
                () -> memberClient.getMemberById(memberId)
        );

        // Chờ cả 2 API trả về kết quả
        CompletableFuture.allOf(bookFuture, memberFuture).join();

        BookResponse bookResponse = bookFuture.join();
        MemberResponse memberResponse = memberFuture.join();

        // 3. Tổng hợp dữ liệu trả về cho client
        return BorrowingDetailResponse.builder()
                .borrowingId(borrowing.getId())
                .bookId(bookId)
                .bookTitle(bookResponse != null ? bookResponse.getTitle() : "Unknown Title")
                .bookAuthor(bookResponse != null ? bookResponse.getAuthor() : "Unknown Author")
                .memberId(memberId)
                .memberName(memberResponse != null ? memberResponse.getFullName() : "Unknown Member")
                .memberEmail(memberResponse != null ? memberResponse.getEmail() : "Unknown Email")
                .borrowDate(borrowing.getBorrowDate())
                .returnDate(borrowing.getReturnDate())
                .status(borrowing.getStatus())
                .build();
    }
}
