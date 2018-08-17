package com.coin.app.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.BankName;
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

public class UserInfo
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull Date createdDate;

    private @NonNull String firstName;

    private @NonNull String lastName;

    private @NonNull Long phoneNumber;

    @Enumerated(EnumType.STRING)
    private @NotNull BankName bankName;

    private @NonNull Long accountNumber;

    private @NonNull Long cardNumber;

    private @NonNull Long sheba;
}
