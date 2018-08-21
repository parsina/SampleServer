package com.coin.app.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.AccountStatus;
import com.coin.app.model.livescore.Form;
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
public class Account
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull Date createdDate;

    @OneToOne
    private @NonNull Wallet wallet;

    private String description;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @ManyToMany(mappedBy = "accounts")
    private List<Form> forms;
}
