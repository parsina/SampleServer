package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.Account;
import com.coin.app.model.enums.FormStatus;
import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository  extends JpaRepository<Form, Long>
{

    long countByFormTemplate(FormTemplate formTemplate);

    List<Form> findByFormTemplate(FormTemplate formTemplate, Pageable pageable);

    long countByAccount(Account account);

    List<Form> findByAccountAndStatusIsIn(Account account, List<FormStatus> statuses, Pageable pageable);

    List<Form> findByStatus(FormStatus status);

    List<Form> findByFormTemplateAndScore(FormTemplate formTemplate, int score);

    List<Form> findByFormTemplate(FormTemplate formTemplate);

    Long countByAccountAndStatusIn(Account account, List<FormStatus> statuses);
}
