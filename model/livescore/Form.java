package com.coin.app.model.livescore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Form
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull String name;

    private String number;

    private @NotNull int score;

    private @NotNull long value;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @Enumerated(EnumType.STRING)
    private @NotNull FormType type;

    @ManyToMany
    @JoinTable(name = "form_match", joinColumns = @JoinColumn(name = "form_id"), inverseJoinColumns = @JoinColumn(name = "match_id"))
    private List<Match> matches;

    public Form()
    {
    }

    public Form(@NotNull String name , FormType type)
    {
        this.name = name;
        this.score = 0;
        this.value = 0L;
        this.type = type;
        this.matches = new ArrayList<>();
    }
}
