package com.coin.app.model.livescore;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.FormTemplateStatus;
import com.coin.app.model.enums.FormTemplateType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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

    private @NotNull int numberOfForms;

    private @NotNull String createdDate;

    private Long totalValue;

    @Enumerated(EnumType.STRING)
    private @NotNull FormTemplateStatus status;

    @Enumerated(EnumType.STRING)
    private @NotNull FormTemplateType type;

    public FormTemplate()
    {
    }

    public FormTemplate(@NotNull String name, @NotNull FormTemplateType type)
    {
        this.name = name;
        this.numberOfForms = 0;
        this.status = FormTemplateStatus.OPEN;
        this.totalValue = 0L;
        this.createdDate = new Date().toString();
        this.type = type;
    }
}
