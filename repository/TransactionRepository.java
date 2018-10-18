package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.Account;
import com.coin.app.model.Transaction;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long>
{
    Transaction findByTxId(String txId);

    List<Transaction> findByAccount(Account account);

    List<Transaction> findByStatusAndType(TransactionStatus status, TransactionType type);

    int countByType(TransactionType type);
}
