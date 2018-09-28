package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.Winner;
import com.coin.app.model.livescore.FormTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinnerRepository extends JpaRepository<Winner, Long>
{
    Long countByFormFormTemplate(FormTemplate formTemplate);

    List<Winner> findByFormFormTemplate(FormTemplate formTemplate, Pageable pageable);
}
