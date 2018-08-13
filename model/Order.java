package com.coin.app.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.OrderStatus;
import com.coin.app.model.enums.OrderType;
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
@Table(name = "\"Order\"")

public class Order
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull Date createdDate;

    private @NotNull String totalValue;

    private @NotNull String totalValueCoin;

    @OneToOne
    private @NotNull  CoinAmount coinAmount;

    @ManyToOne
    private @NotNull Account account;

    private String description;

    @Enumerated(EnumType.STRING)
    private @NotNull OrderStatus status;

    @Enumerated(EnumType.STRING)
    private @NotNull OrderType type;
}
