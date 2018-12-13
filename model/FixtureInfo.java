package com.coin.app.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.FixtureInfoType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FixtureInfo
{
    public FixtureInfo()
    {
    }

    public FixtureInfo(@NotNull FixtureInfoType type, @NotNull String name, String farsiName)
    {
        this.type = type;
        this.name = name;
        this.farsiName = farsiName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private @NotNull FixtureInfoType type;

    private @NotNull String name;

    private String farsiName;

    public String getFarsiName()
    {
        if(farsiName == null || farsiName.trim().equals(""))
            return this.name;
        return farsiName;
    }
}
