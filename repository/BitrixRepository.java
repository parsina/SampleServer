package com.coin.app.repository;

import com.coin.app.model.Bitrix;
import com.coin.app.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BitrixRepository extends JpaRepository<Bitrix, Long>
{
    Bitrix findByUsername(String username);

    Bitrix findByRole(UserRole role);

    Bitrix findByEmail(String email);

    Bitrix findByReferee(String referee);

    Bitrix findByToken(String token);

    Bitrix findByAddress(String address);
}
