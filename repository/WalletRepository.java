package com.coin.app.repository;

import com.coin.app.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long>
{
    Wallet findByName(String name);
}
