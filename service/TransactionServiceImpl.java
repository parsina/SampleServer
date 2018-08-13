package com.coin.app.service;

import java.util.Date;

import com.coin.app.model.Account;
import com.coin.app.model.Transaction;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService
{
    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public Transaction createOrUpdateTransaction(String txId, String fee, Long value, String coinValue, Account account, TransactionStatus status, TransactionType type)
    {
        Transaction transaction = transactionRepository.findByTxId(txId);
        if(transaction == null )
        {
            transaction = new Transaction();
            transaction.setCreatedDate(new Date());
            transaction.setTxId(txId);
        }
        transaction.setFee(fee);
        transaction.setTotalValue(value);
        transaction.setTotalValueCoin(coinValue);
        transaction.setAccount(account);
        transaction.setStatus(status);
        transaction.setType(type);
        return transactionRepository.save(transaction);
    }
}
