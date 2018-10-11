package com.coin.app.repository;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>
{
    Account findByUser(User user);
}
