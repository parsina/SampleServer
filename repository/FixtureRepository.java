package com.coin.app.repository;

import java.time.LocalDate;
import java.util.List;

import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository extends JpaRepository<Fixture, Long>
{
    List<Fixture> findByUsedAndLocalDateGreaterThanOrderByDateAscTimeAsc(boolean used, LocalDate localDate);

    List<Fixture> findByFormTemplateOrderByDateAscTimeAsc(FormTemplate formTemplate);
}
