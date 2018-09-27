package com.coin.app.repository;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>
{
    User findByEmail(String email);

    User findByUsername(String username);

    User findByAccount(Account account);

    User findByConfirmationToken(String confirmationToken);
}
