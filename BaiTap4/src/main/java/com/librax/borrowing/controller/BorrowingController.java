package com.librax.borrowing.controller;

import com.librax.borrowing.dto.BorrowingDetailResponse;
import com.librax.borrowing.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping("/{id}/detail")
    public ResponseEntity<BorrowingDetailResponse> getBorrowingDetail(@PathVariable Long id) {
        BorrowingDetailResponse response = borrowingService.getBorrowingDetail(id);
        return ResponseEntity.ok(response);
    }
}
