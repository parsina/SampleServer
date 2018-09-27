package com.coin.app.repository;

import java.time.LocalDate;
import java.util.List;

import com.coin.app.model.enums.FixtureStatus;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.enums.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository extends JpaRepository<Fixture, Long>
{
    List<Fixture> findByUsedAndLocalDateGreaterThanOrderByDateAscTimeAsc(boolean used, LocalDate localDate);

    List<Fixture> findByFormTemplateOrderByDateAscTimeAsc(FormTemplate formTemplate);

    List<Fixture> findByUsedAndFormTemplateStatusIsInOrderByDateAscTimeAsc(boolean used, List<FormTemplateStatus> formTemplateStatuses);

    List<Fixture> findByUsedAndLocalDateEqualsAndStatusIsNotInAndFormTemplateStatusIsInOrderByDateAscTimeAsc(boolean used, LocalDate localDate,  List<FixtureStatus> fixtureStatuses, List<FormTemplateStatus> formTemplateStatuses);

    List<Fixture> findByLocalDateGreaterThanOrderByDateAscTimeAsc(LocalDate localDate);
}
