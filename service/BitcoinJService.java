package com.coin.app.service;


import com.coin.app.model.enums.TransactionStatus;

public interface BitcoinJService
{
    Long getWalletBalance();

    void initialize();

    String getNewWalletAddress();

    TransactionStatus forwardCoins(Long amount, String address);

    void updateAllAcountBalances();
}
