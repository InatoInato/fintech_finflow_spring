package com.finflow.finflow.wallet.repository;

import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.wallet.entity.Wallet;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
    @Lock(LockModeType.PESSIMISTIC_WRITE) // Prevents other threads from reading/writing until transaction ends
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") Long id);
}
