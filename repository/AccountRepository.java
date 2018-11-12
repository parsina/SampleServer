package com.coin.app.repository;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>
{
    Account findByWallet(Wallet wallet);
}
