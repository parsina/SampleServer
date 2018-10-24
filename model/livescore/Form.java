package com.coin.app.model.livescore;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.Account;
import com.coin.app.model.enums.FormStatus;
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
public class Form
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull String name;

    private @NotNull LocalDate createdDate;

    private @NotNull LocalTime createdTime;

    private int score;

    private long value;

    @Column(name = "real_form")
    private boolean real;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @ManyToOne
    private @NotNull FormTemplate formTemplate;

    @ManyToOne
    private @NotNull Account account;
}
