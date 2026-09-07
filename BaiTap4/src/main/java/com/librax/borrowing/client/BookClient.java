package com.librax.borrowing.client;

import com.librax.borrowing.dto.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class BookClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public BookResponse getBookById(Long bookId) {
        String url = "http://localhost:8081/api/books/" + bookId;
        try {
            log.info("Calling book-service REST API for bookId: {}", bookId);
            return restTemplate.getForObject(url, BookResponse.class);
        } catch (Exception e) {
            log.warn("Failed to fetch book from book-service (bookId: {}). Fallback applied. Error: {}", bookId, e.getMessage());
            return BookResponse.builder()
                    .id(bookId)
                    .title("[Tạm thời không khả dụng - Fallback Title]")
                    .author("Unknown Author")
                    .build();
        }
    }
}
