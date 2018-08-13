package com.coin.app.service;

import com.coin.app.model.User;
import com.coin.app.model.Wallet;
import com.coin.app.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService
{
    @Autowired
    WalletRepository walletRepository;

    @Autowired
    BitcoinJService bitcoinJService;

    @Override
    public Wallet createWallet(User user)
    {
        return walletRepository.save(bitcoinJService.initializeWallet(user));
    }

    @Override
    public Wallet update(Wallet wallet)
    {
        return walletRepository.save(wallet);
    }
}
