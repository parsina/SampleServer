package com.coin.app.service;


import java.util.Date;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.enums.AccountStatus;
import com.coin.app.repository.AccountRepository;
import com.coin.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService
{
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WalletService walletService;

    @Override
    public Account createAccount(User user)
    {
        Account account = new Account();
        account.setCreatedDate(new Date());
        account.setStatus(AccountStatus.INACTIVE);
        account.setWallet(walletService.createWallet(user));
        return accountRepository.save(account);
    }

    @Override
    public ResultData getUserAccount()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.isAuthenticated())
        {
            Account account = userRepository.findByEmail(authentication.getName()).getAccount();
            ResultData data = new ResultData(true, "");
            data.addProperty("accountId", account.getId());
            data.addProperty("accountStatus", account.getStatus());
            data.addProperty("walletAddress", account.getWallet().getAddress());
            data.addProperty("realBalance", account.getWallet().getRealBalance());
            data.addProperty("balance", account.getWallet().getBalance());
            return data;
        }

        return new ResultData(false, "No permission to get user account data!");
    }
}
