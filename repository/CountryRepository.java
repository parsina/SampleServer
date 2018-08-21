package com.coin.app.repository;

import com.coin.app.model.livescore.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long>
{
}
