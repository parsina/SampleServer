package com.coin.app.model.livescore;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Access;
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
import lombok.NoArgsConstructor;
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

    private @NotNull LocalDate cratedDate;

    private @NotNull LocalTime cratedTime;

    private @NotNull int score;

    private @NotNull long value;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @ManyToOne
    private @NotNull FormTemplate formTemplate;

    @ManyToOne
    private @NotNull Account account;

    public Form(@NotNull String name, @NotNull LocalDate cratedDate, @NotNull LocalTime cratedTime, FormStatus status, @NotNull FormTemplate formTemplate)
    {
        this.name = name;
        this.cratedDate = cratedDate;
        this.cratedTime = cratedTime;
        this.status = status;
        this.formTemplate = formTemplate;
        this.score = 0;
        this.value = 0;
    }
}
