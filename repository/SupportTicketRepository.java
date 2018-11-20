package com.coin.app.repository;

import com.coin.app.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>
{
}
