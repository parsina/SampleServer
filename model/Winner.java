package com.coin.app.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.BankName;
import com.coin.app.model.enums.WinnerPlace;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormTemplate;
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

public class Winner
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private @NotNull Long prize;

    @Enumerated(EnumType.STRING)
    private @NotNull WinnerPlace winnerPlace;

    @ManyToOne
    private @NotNull Form form;
}
