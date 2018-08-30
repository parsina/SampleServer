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
public class Form
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull String name;

    private @NotNull String cratedDate;

    private @NotNull String cratedTime;

    private @NotNull int score;

    private @NotNull long value;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @ManyToOne
    private @NotNull FormTemplate formTemplate;
}
