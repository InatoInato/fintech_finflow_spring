package com.finflow.finflow.transaction.repository;

import com.finflow.finflow.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
