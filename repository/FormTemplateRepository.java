package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.livescore.FormTemplate;
import com.coin.app.model.livescore.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long>
{
    List<FormTemplate> findAllByStatus(FormTemplateStatus status);
}
