package com.librax.book.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    public void decreaseAvailableCopies() {
        if (this.availableCopies <= 0) {
            throw new IllegalStateException("Hết sách trong kho để mượn.");
        }
        this.availableCopies--;
    }

    public void increaseAvailableCopies() {
        if (this.availableCopies >= this.totalCopies) {
            throw new IllegalStateException("Số lượng sách có sẵn vượt quá tổng số bản sao.");
        }
        this.availableCopies++;
    }
}
