package com.coin.app.model;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@ToString
@EqualsAndHashCode

public class CoinAmount
{
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String fullName;

    private String amount;

    private String bid;

    private String ask;

    private String marketCapital;

    private String volume;

    private String change;

    private String description;
}
