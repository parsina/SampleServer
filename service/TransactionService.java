package com.coin.app.service;

import com.coin.app.model.Account;
import com.coin.app.model.Transaction;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;

public interface TransactionService
{
    Transaction createOrUpdateTransaction(String txId, String fee, Long value, String coinValue, Account account, TransactionStatus status, TransactionType type);
}
