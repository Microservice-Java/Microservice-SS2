package com.librax.borrowing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingDetailResponse {
    private Long borrowingId;
    
    // Dữ liệu lấy từ book-service
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    
    // Dữ liệu lấy từ member-service
    private Long memberId;
    private String memberName;
    private String memberEmail;
    
    // Dữ liệu từ borrowings_db
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private String status;
}
