package com.coin.app.model.livescore;


import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

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
public class Match
{
    @Id
    private Long id;

    //    private @NotNull Long fixtureId;
    private @NotNull String date;
    private @NotNull String time;
    private @NotNull String round;
    private @NotNull String homeName;
    private @NotNull String awayName;
    private @NotNull int homeGoals = 0;
    private @NotNull int awayGoals = 0;
    private @NotNull boolean homeWin = false;
    private @NotNull boolean awayWin = false;
    private @NotNull boolean noWin = false;
    private @NotNull int score = 0;
    private @NotNull String location;

    @ManyToOne
    private @NotNull League league;

    @Enumerated(EnumType.STRING)
    private @NotNull MatchStatus status = MatchStatus.NOTSTARTED;

    @ManyToMany(mappedBy = "matches")
    private List<Form> forms;
}
