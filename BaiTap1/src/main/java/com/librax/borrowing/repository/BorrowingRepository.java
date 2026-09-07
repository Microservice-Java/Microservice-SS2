package com.librax.borrowing.repository;

import com.librax.borrowing.model.BorrowStatus;
import com.librax.borrowing.model.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByMemberId(Long memberId);
    long countByMemberIdAndStatus(Long memberId, BorrowStatus status);
    long countByStatus(BorrowStatus status);
}
