package com.coin.app.repository;

import com.coin.app.model.livescore.League;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository  extends JpaRepository<League, Long>
{
}
