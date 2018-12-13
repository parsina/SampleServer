package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.enums.FormTemplateType;
import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.enums.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long>
{
    long countByType(FormTemplateType type);

    List<FormTemplate> findAllByStatusIsInOrderByIdAsc(List<FormTemplateStatus> statuses);

    List<FormTemplate> findByStatusIsIn(List<FormTemplateStatus> statuses, Pageable pageable);

    List<FormTemplate> findByStatusIsInAndType(List<FormTemplateStatus> statuses, FormTemplateType type, Pageable pageable);

    Long countByStatusIn(List<FormTemplateStatus> statuses);

    Long countByStatusInAndType(List<FormTemplateStatus> statuses, FormTemplateType type);
}
