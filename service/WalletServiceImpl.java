package com.coin.app.service;

import java.util.Date;

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
        Wallet wallet = new com.coin.app.model.Wallet();
        wallet.setCreatedDate(new Date());
        wallet.setName(user.getEmail());
        wallet.setBalance("0");
        wallet.setRealBalance("0");
        wallet.setAddress(bitcoinJService.getNewWalletAddress());
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet update(Wallet wallet)
    {
        return walletRepository.save(wallet);
    }
}
