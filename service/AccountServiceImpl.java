package com.coin.app.service;


import java.util.Date;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.enums.AccountStatus;
import com.coin.app.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService
{
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    WalletService walletService;

    @Override
    public Account createAccount(User user)
    {
        Account account = new Account();
        account.setCreatedDate(new Date());
        account.setStatus(AccountStatus.ACTIVE);
        account.setWallet(walletService.createWallet(user));
        return accountRepository.save(account);
    }
}
