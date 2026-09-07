package com.librax.borrowing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity Borrowing thuộc sở hữu ĐỘC QUYỀN của CSDL borrowings_db.
 * Chú ý: Trong mẫu Database-per-service, Borrowing CHỈ lưu bookId và memberId dạng Long nguyên bản.
 * Không sử dụng @ManyToOne hay @JoinColumn trỏ sang Book/Member Entity của service khác!
 */
@Entity
@Table(name = "borrowings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private Long memberId;

    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;

    private String status; // BORROWED, RETURNED
}
