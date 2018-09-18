package com.coin.app.repository;

import java.time.LocalDate;
import java.util.List;

import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.livescore.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository extends JpaRepository<Fixture, Long>
{
    List<Fixture> findByUsedAndLocalDateGreaterThanOrderByDateAscTimeAsc(boolean used, LocalDate localDate);

    List<Fixture> findByFormTemplateOrderByDateAscTimeAsc(FormTemplate formTemplate);

    List<Fixture> findByUsedAndFormTemplateStatusOrderByDateAscTimeAsc(boolean used, FormTemplateStatus status);

    List<Fixture> findByUsedAndFormTemplateStatusAndLocalDateEqualsOrderByDateAscTimeAsc(boolean used, FormTemplateStatus status, LocalDate localDate);
}
