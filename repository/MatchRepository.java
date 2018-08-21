package com.coin.app.repository;

import com.coin.app.model.livescore.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long>
{
}
