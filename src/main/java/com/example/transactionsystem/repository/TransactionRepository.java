package com.example.transactionsystem.repository;

import com.example.transactionsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTxnId(String txnId);

    List<Transaction> findByRrn(String rrn);

    List<Transaction> findByPayerVpa(String payerVpa);

    List<Transaction> findByPayeeVpa(String payeeVpa);

    Optional<Transaction> findByTxnIdAndRrn(String txnId, String rrn);

}
