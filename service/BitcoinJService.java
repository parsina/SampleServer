package com.coin.app.service;


import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.Wallet;

public interface BitcoinJService
{
    void initialize();

    Wallet initializeWallet(User user);

    void startCoinReceiveListener(Account account);
}
