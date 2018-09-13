package com.coin.app.service;


import com.coin.app.model.Account;
import com.coin.app.model.User;

public interface AccountService
{
    Account createAccount(User user);
}
