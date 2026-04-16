package com.example.transactionsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"txn_id", "rrn"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String txnId;
    private String rrn;
    private String payerVpa;
    private String payeeVpa;
    private LocalDateTime transactionDate;
    private String remitterDetails;
}
