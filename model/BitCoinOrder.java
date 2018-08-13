package com.coin.app.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.OrderType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Table(name = "BitCoinOrder")

public class BitCoinOrder
{
    @Id
    @GeneratedValue
    private @NotNull Long id;

    private @NotNull Date orderDate;

    @Enumerated(EnumType.STRING)
    private @NotNull OrderType orderType;

    @ManyToOne
    private @NotNull User user;

    private @NotNull Long price;

    private @NotNull Double amount;
}
