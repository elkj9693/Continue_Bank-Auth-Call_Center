package com.entrusting.backend.user.repository;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(Account sender, Account receiver);

    @Query("SELECT t FROM Transaction t WHERE (t.sender = :account OR t.receiver = :account) AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactions(@Param("account") Account account, @Param("since") LocalDateTime since);
}
