package com.coin.app.service;

import com.coin.app.model.User;
import com.coin.app.model.Wallet;

public interface WalletService
{
    Wallet createWallet(User user);
}
