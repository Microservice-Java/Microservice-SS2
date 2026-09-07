package com.librax.contract;

import java.time.LocalDate;

/**
 * Service Contract DTO cho thao tác notifyOverdue.
 * Hợp đồng quy định các trường bắt buộc: memberId, bookId, dueDate.
 */
public class NotifyOverdueRequest {
    private Long memberId;
    private Long bookId;
    private LocalDate dueDate;

    public NotifyOverdueRequest() {
    }

    public NotifyOverdueRequest(Long memberId, Long bookId, LocalDate dueDate) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.dueDate = dueDate;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "NotifyOverdueRequest{" +
                "memberId=" + memberId +
                ", bookId=" + bookId +
                ", dueDate=" + dueDate +
                '}';
    }
}
