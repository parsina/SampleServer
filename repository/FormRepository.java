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

    long countByFormTemplateAndReal(FormTemplate formTemplate, boolean real);

    List<Form> findByFormTemplate(FormTemplate formTemplate, Pageable pageable);

    List<Form> findByFormTemplateAndReal(FormTemplate formTemplate, boolean real, Pageable pageable);

    long countByAccount(Account account);

    List<Form> findByAccountAndStatusIsIn(Account account, List<FormStatus> statuses, Pageable pageable);

    long countByAccountAndFormTemplateAndReal(Account account, FormTemplate formTemplate, boolean real);

    List<Form> findByStatus(FormStatus status);

    List<Form> findByFormTemplateAndScoreAndReal(FormTemplate formTemplate, int score, boolean real);

    List<Form> findByFormTemplate(FormTemplate formTemplate);

    List<Form> findByFormTemplateAndRealOrderByCreatedDateAscCreatedTimeAsc(FormTemplate formTemplate, boolean real);

    Long countByAccountAndStatusIn(Account account, List<FormStatus> statuses);

    List<Form> findByFormTemplateIdAndStatusIsIn(Long id, List<FormStatus> statuses, Pageable pageable);

    List<Form> findByFormTemplateIdAndStatusIsInAndReal(Long id, List<FormStatus> statuses, boolean real, Pageable pageable);

    Long countByFormTemplateIdAndStatusIsIn(Long id, List<FormStatus> status);

    Long countByFormTemplateIdAndStatusIsInAndReal(Long id, List<FormStatus> status, boolean real);
}
