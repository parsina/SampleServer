package com.coin.app.repository;

import com.coin.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>
{
    User findByEmail(String email);

    User findByConfirmationToken(String confirmationToken);
}
