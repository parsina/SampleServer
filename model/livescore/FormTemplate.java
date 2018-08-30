package com.coin.app.model.livescore;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FormTemplate
{
    @Id
    @GeneratedValue
    private Long id;

    private @NotNull String name;

    private @NotNull int numberOfMatches;

    private @NotNull String createdDate;

    private Long totalValue;

    @Enumerated(EnumType.STRING)
    private @NotNull FormTemplateStatus status;

    public FormTemplate()
    {
    }

    public FormTemplate(@NotNull String name, @NotNull int numberOfMatches)
    {
        this.name = name;
        this.numberOfMatches = numberOfMatches;
        this.status = FormTemplateStatus.OPEN;
        this.totalValue = 0L;
        this.createdDate = new Date().toString();
    }
}
