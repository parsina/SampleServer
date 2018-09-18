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

import com.coin.app.model.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Form
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull String name;

    private @NotNull LocalDate createdDate;

    private @NotNull LocalTime createdTime;

    private @NotNull int score;

    private @NotNull long value;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @ManyToOne
    private @NotNull FormTemplate formTemplate;

    @ManyToOne
    private @NotNull Account account;

    public Form()
    {
    }

    public Form(@NotNull String name, @NotNull LocalDate createdDate, @NotNull LocalTime createdTime, FormStatus status, @NotNull FormTemplate formTemplate)
    {
        this.name = name;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.status = status;
        this.formTemplate = formTemplate;
        this.score = 0;
        this.value = 0;
    }
}
