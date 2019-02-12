package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.BitrixTransaction;
import com.coin.app.model.Bitrix;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BitrixTransactionRepository extends JpaRepository<BitrixTransaction, Long>
{
    Long countByUser(Bitrix user);

    List<BitrixTransaction> findByUser(Bitrix user, Pageable pageable);

    List<BitrixTransaction> findByStatus(TransactionStatus status);

    BitrixTransaction findByTxId(String txId);

    int countByType(TransactionType type);
}
