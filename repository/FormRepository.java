package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FormRepository  extends JpaRepository<Form, Long>
{
    List<Form> findByAccountAndStatusIsIn(Account account, List<FormStatus> statuses, Pageable pageable);

    Long countByAccountAndStatusIn(Account account, List<FormStatus> statuses);
}
