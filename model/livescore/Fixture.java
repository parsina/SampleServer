package com.coin.app.model.livescore;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.FixtureStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Fixture
{
    @Id
    private Long id;

    private @NotNull Long leagueId;
    private String leagueName;

    private @NotNull Long localTeamCountryId;
    private @NotNull Long localTeamId;
    private String localTeamName;
    private String localCountryName;
    private String localCountryFIFAName;
    @Lob
    @Column
    private String localCountryFlag;
    private String localTeamShortName;
    private String localTeamLogo;
    private int localTeamFounded;
    private boolean nationalLocalTeam;
    private int localTeamScore;
    private int localTeamPenaltyScore;

    private @NotNull Long visitorTeamCountryId;
    private @NotNull Long visitorTeamId;
    private String visitorTeamName;
    private String visitorCountryName;
    private String visitorCountryFIFAName;
    @Lob
    @Column
    private String visitorCountryFlag;
    private String visitorTeamShortName;
    private String visitorTeamLogo;
    private int visitorTeamFounded;
    private boolean NationalVisitorTeam;
    private int visitorTeamScore;
    private int visitorTeamPenaltyScore;

    private String weatherReport;

    private String halfTimeScore;
    private String fullTimeScore;
    private String extraTimeScore;

    @Enumerated(EnumType.STRING)
    private @NotNull FixtureStatus status;

    private LocalDate localDate;
    private String date;
    private String time;
    private String minute;
    private String addedTime;
    private String extraTime;

    private @NotNull boolean used = false;

    @ManyToOne
    private FormTemplate formTemplate;
}
