package com.librax.member.model;

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
@Table(name = "members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;
    private int currentBorrowedCount;

    public void incrementBorrowedCount() {
        this.currentBorrowedCount++;
    }

    public void decrementBorrowedCount() {
        if (this.currentBorrowedCount > 0) {
            this.currentBorrowedCount--;
        }
    }
}
