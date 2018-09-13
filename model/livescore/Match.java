package com.coin.app.model.livescore;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
@EqualsAndHashCode
@ToString
public class Match
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull Long fixtureId;

    private @NotNull boolean localWin;

    private @NotNull boolean noWin;

    private @NotNull boolean visitorWin;

    private @NotNull boolean score;

    @ManyToOne
    private @NotNull Form form;
}
