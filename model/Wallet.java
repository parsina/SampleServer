package com.coin.app.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode

public class Wallet
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull Date createdDate;

    private @NonNull String name;

    private @NonNull String balance;

    private @NotNull String realBalance;

    private @NonNull String address;

    private String description;

    @OneToMany
    @JoinColumn(name = "wallet_id")
    private List<CoinAmount> coinAmounts;
}
