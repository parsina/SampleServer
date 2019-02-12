package com.coin.app.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.BitrixType;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.model.enums.UserRole;
import com.coin.app.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

public class BitrixTransaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private @NotNull LocalDate createdDate;

    private @NotNull LocalDate updateDate;

    private @NotNull LocalTime createdTime;

    private @NotNull LocalTime updateTime;

    private @NonNull String txId;

    private String fee;

    private @NotNull Long totalValue;

    @ManyToOne
    private @NotNull Bitrix user;

    private String description;

    @Enumerated(EnumType.STRING)
    private @NonNull
    TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
}
