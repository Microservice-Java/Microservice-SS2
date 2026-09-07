package com.librax.borrowing.service;

import com.librax.book.model.Book;
import com.librax.book.service.BookService;
import com.librax.borrowing.dto.BorrowRequest;
import com.librax.borrowing.dto.BorrowResponse;
import com.librax.borrowing.model.BorrowStatus;
import com.librax.borrowing.model.Borrowing;
import com.librax.borrowing.repository.BorrowingRepository;
import com.librax.common.exception.BorrowingDomainException;
import com.librax.common.exception.ResourceNotFoundException;
import com.librax.member.model.Member;
import com.librax.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final MemberService memberService;
    private final BookService bookService;

    /**
     * Nghiệp vụ mượn sách:
     * 1. Kiểm tra sự tồn tại của độc giả và sách.
     * 2. Đếm số lượng sách độc giả đang mượn từ CSDL (thay vì dùng biến static nguy hiểm).
     * 3. Kiểm tra điều kiện mượn (tối đa 5 cuốn).
     * 4. Kiểm tra số lượng sách khả dụng trong kho.
     * 5. Tạo giao dịch mượn sách và cập nhật trạng thái trong cùng 1 Transaction.
     */
    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {
        Member member = memberService.getMemberEntityById(request.getMemberId());
        Book book = bookService.getBookEntityById(request.getBookId());

        // Đếm số lượng phiếu mượn đang active của độc giả này từ CSDL
        long activeBorrowings = borrowingRepository.countByMemberIdAndStatus(member.getId(), BorrowStatus.BORROWED);

        if (!memberService.canBorrowBook((int) activeBorrowings)) {
            throw new BorrowingDomainException(
                String.format("Độc giả '%s' (ID: %d) đã mượn %d cuốn sách, đạt giới hạn tối đa (5 cuốn).",
                        member.getFullName(), member.getId(), activeBorrowings)
            );
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BorrowingDomainException(
                String.format("Đầu sách '%s' (ID: %d) hiện đã hết bản sao sẵn có.", book.getTitle(), book.getId())
            );
        }

        // Cập nhật số lượng
        book.decreaseAvailableCopies();
        member.incrementBorrowedCount();

        Borrowing borrowing = Borrowing.builder()
                .memberId(member.getId())
                .bookId(book.getId())
                .borrowDate(LocalDateTime.now())
                .status(BorrowStatus.BORROWED)
                .build();

        Borrowing saved = borrowingRepository.save(borrowing);
        return mapToResponse(saved);
    }

    /**
     * Nghiệp vụ trả sách:
     */
    @Transactional
    public BorrowResponse returnBook(Long borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn với ID: " + borrowingId));

        if (borrowing.getStatus() == BorrowStatus.RETURNED) {
            throw new BorrowingDomainException("Phiếu mượn này đã được hoàn trả trước đó.");
        }

        Member member = memberService.getMemberEntityById(borrowing.getMemberId());
        Book book = bookService.getBookEntityById(borrowing.getBookId());

        book.increaseAvailableCopies();
        member.decrementBorrowedCount();

        borrowing.setStatus(BorrowStatus.RETURNED);
        borrowing.setReturnDate(LocalDateTime.now());

        return mapToResponse(borrowingRepository.save(borrowing));
    }

    @Transactional(readOnly = true)
    public List<BorrowResponse> getBorrowingsByMember(Long memberId) {
        return borrowingRepository.findByMemberId(memberId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tổng số lượt mượn sách hiện tại trên toàn hệ thống từ CSDL (thay thế cho biến static totalBorrowedBooks)
     */
    @Transactional(readOnly = true)
    public long getTotalActiveBorrowedBooks() {
        return borrowingRepository.countByStatus(BorrowStatus.BORROWED);
    }

    private BorrowResponse mapToResponse(Borrowing borrowing) {
        return BorrowResponse.builder()
                .id(borrowing.getId())
                .memberId(borrowing.getMemberId())
                .bookId(borrowing.getBookId())
                .borrowDate(borrowing.getBorrowDate())
                .returnDate(borrowing.getReturnDate())
                .status(borrowing.getStatus())
                .build();
    }
}
