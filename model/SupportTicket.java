package com.coin.app.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.coin.app.model.enums.SupportTicketStatus;
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
public class SupportTicket
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private @NotNull Date createdDate;

    private @NotNull Date updateDate;

    private @NotNull String subject;

    private @NotNull String description;

    private String response;

    @ManyToOne
    private @NotNull User sender;

    @Enumerated(EnumType.STRING)
    private @NotNull SupportTicketStatus status;
}
