package com.finflow.finflow.integration;

import com.finflow.finflow.transaction.entity.Transaction;
import com.finflow.finflow.transaction.entity.TransactionType;
import com.finflow.finflow.transaction.repository.TransactionRepository;
import com.finflow.finflow.transaction.service.TransactionService;
import com.finflow.finflow.user.entity.User;
import com.finflow.finflow.user.repository.UserRepository;
import com.finflow.finflow.user.service.UserService;
import com.finflow.finflow.wallet.entity.Wallet;
import com.finflow.finflow.wallet.repository.WalletRepository;
import com.finflow.finflow.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should transfer funds between two wallets successfully")
    void testTransferBetweenWallets() {
        // Create users
        User alice = userService.createUser("alice@test.com", "password");
        User bob = userService.createUser("bob@test.com", "password");

        // Create wallets
        walletService.createDefaultWallet(alice);
        walletService.createDefaultWallet(bob);

        // Refresh users to get wallet relationship
        alice = userRepository.findById(alice.getId()).orElseThrow();
        bob = userRepository.findById(bob.getId()).orElseThrow();

        Wallet aliceWallet = walletRepository.findByUser(alice).orElseThrow();
        Wallet bobWallet = walletRepository.findByUser(bob).orElseThrow();

        // Top up Alice's wallet
        walletService.adjustBalance(aliceWallet, BigDecimal.valueOf(1000));

        // Create transfer transaction
        Transaction tx = transactionService.createTransaction(
                alice.getEmail(),
                aliceWallet.getId(),
                bobWallet.getId(),
                BigDecimal.valueOf(200)
        );

        // Verify balances
        Wallet updatedAlice = walletService.getWalletEntityById(aliceWallet.getId());
        Wallet updatedBob = walletService.getWalletEntityById(bobWallet.getId());

        assertThat(updatedAlice.getBalance()).isEqualByComparingTo("800");
        assertThat(updatedBob.getBalance()).isEqualByComparingTo("200");
        assertThat(tx.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(tx.getAmount()).isEqualByComparingTo("200");
        assertThat(tx.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should create deposit transaction")
    void testDepositToWallet() {
        // Create user
        User alice = userService.createUser("alice@test.com", "password");
        walletService.createDefaultWallet(alice);

        alice = userRepository.findById(alice.getId()).orElseThrow();
        Wallet aliceWallet = walletRepository.findByUser(alice).orElseThrow();

        // Create deposit transaction
        Transaction tx = transactionService.createTransaction(
                alice.getEmail(),
                null,
                aliceWallet.getId(),
                BigDecimal.valueOf(500)
        );

        // Verify balance
        Wallet updatedWallet = walletService.getWalletEntityById(aliceWallet.getId());

        assertThat(updatedWallet.getBalance()).isEqualByComparingTo("500");
        assertThat(tx.getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    @DisplayName("Should create withdrawal transaction")
    void testWithdrawFromWallet() {
        // Create user
        User alice = userService.createUser("alice@test.com", "password");
        walletService.createDefaultWallet(alice);

        alice = userRepository.findById(alice.getId()).orElseThrow();
        Wallet aliceWallet = walletRepository.findByUser(alice).orElseThrow();

        // Top up wallet first
        walletService.adjustBalance(aliceWallet, BigDecimal.valueOf(1000));

        // Create withdrawal transaction
        Transaction tx = transactionService.createTransaction(
                alice.getEmail(),
                aliceWallet.getId(),
                null,  // No to wallet for withdrawal
                BigDecimal.valueOf(300)
        );

        // Verify balance
        Wallet updatedWallet = walletService.getWalletEntityById(aliceWallet.getId());

        assertThat(updatedWallet.getBalance()).isEqualByComparingTo("700");
        assertThat(tx.getType()).isEqualTo(TransactionType.WITHDRAW);
    }

    @Test
    @DisplayName("Should handle concurrent transactions with optimistic locking")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testConcurrentTransactions() throws InterruptedException {
        // Create users
        final User alice = userService.createUser("alice@test.com", "password");
        User bob = userService.createUser("bob@test.com", "password");

        walletService.createDefaultWallet(alice);
        walletService.createDefaultWallet(bob);

        Wallet aliceWallet = walletRepository.findByUser(alice).orElseThrow();
        Wallet bobWallet = walletRepository.findByUser(bob).orElseThrow();

        // Top up Alice's wallet
        walletService.adjustBalance(aliceWallet, BigDecimal.valueOf(1000));

        // Simulate concurrent transactions
        Thread t1 = new Thread(() -> transactionService.createTransaction(
                alice.getEmail(),
                aliceWallet.getId(),
                bobWallet.getId(),
                BigDecimal.valueOf(100)
        ));

        Thread t2 = new Thread(() -> transactionService.createTransaction(
                alice.getEmail(),
                aliceWallet.getId(),
                bobWallet.getId(),
                BigDecimal.valueOf(150)
        ));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Verify final balances
        Wallet finalAlice = walletService.getWalletEntityById(aliceWallet.getId());
        Wallet finalBob = walletService.getWalletEntityById(bobWallet.getId());

        assertThat(finalAlice.getBalance()).isEqualByComparingTo("750");
        assertThat(finalBob.getBalance()).isEqualByComparingTo("250");
    }
}