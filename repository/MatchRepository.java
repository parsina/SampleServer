package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long>
{
    List<Match> findByForm(Form formId);
}
