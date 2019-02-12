package com.coin.app.model;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.BitrixType;
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

public class Bitrix
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private @NotNull String username;

    private @NonNull String email;

    @JsonIgnore
    private String password;

    private Date createdDate;

    private @NotNull String reference;

    private @NotNull String referee;

    @Transient
    private String refereeURL;

    private @NotNull String token;

    private @NotNull Long parentId;

    private Long firstChildId;

    private Long secondChildId;

    private @NonNull Long balance;

    private @NonNull String address;

    private @NonNull int step;

    private @NonNull int nodes;

    private String description;

    @Enumerated(EnumType.STRING)
    private @NotNull UserStatus status;

    @Enumerated(EnumType.STRING)
    private @NonNull BitrixType plan;

    @Enumerated(EnumType.STRING)
    private @NonNull UserRole role;
}
