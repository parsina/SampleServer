package com.coin.app.repository;

import java.time.LocalDate;
import java.util.List;

import com.coin.app.model.enums.FixtureStatus;
import com.coin.app.model.livescore.Fixture;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.enums.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FixtureRepository extends JpaRepository<Fixture, Long>
{
    List<Fixture> findTop50ByUsedAndStatusIsAndLocalDateGreaterThanOrderByDateAscTimeAsc(boolean used, FixtureStatus status, LocalDate localDate);

    List<Fixture> findByFormTemplateOrderByDateAscTimeAsc(FormTemplate formTemplate);

    List<Fixture> findByUsedAndFormTemplateStatusIsInOrderByDateAscTimeAsc(boolean used, List<FormTemplateStatus> formTemplateStatuses);

    List<Fixture> findByUsedAndLocalDateLessThanAndStatusIsNotInAndFormTemplateStatusIsInOrderByDateAscTimeAsc(boolean used, LocalDate localDate,  List<FixtureStatus> fixtureStatuses, List<FormTemplateStatus> formTemplateStatuses);

    List<Fixture> findByLocalDateGreaterThanOrderByDateAscTimeAsc(LocalDate localDate);

    @Query(value = "SELECT DISTINCT local_country_name FROM fixture WHERE local_country_name is not NULL", nativeQuery = true)
    List<String> findLocalCountries();

    @Query(value = "SELECT DISTINCT visitor_country_name FROM fixture WHERE visitor_country_name is not NULL", nativeQuery = true)
    List<String> findVisitorCountries();

    @Query(value = "SELECT DISTINCT league_name FROM fixture", nativeQuery = true)
    List<String> findLeages();

    @Query(value = "SELECT DISTINCT local_team_name FROM fixture", nativeQuery = true)
    List<String> findLocalTeams();

    @Query(value = "SELECT DISTINCT visitor_team_name FROM fixture", nativeQuery = true)
    List<String> findVisitorTeams();
}
