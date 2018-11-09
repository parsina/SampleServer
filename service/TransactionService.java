package com.coin.app.service;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.Transaction;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;

public interface TransactionService
{
    Long countUserAccountTransactions();

    ResultData getUserAccountTransactions(String filter, String sortOrder, String sortBy, int pageNumber,int pageSize);

    ResultData transfer(String userId, String address, String amount, String userSecurityCode);
}
