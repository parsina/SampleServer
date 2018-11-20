package com.coin.app.service;


import java.util.Map;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.Wallet;
import com.coin.app.model.enums.TransactionStatus;

public interface BitcoinJService
{
    void initialize();

    String getNewWalletAddress();

    TransactionStatus forwardCoins(Long amount, String address);
}
