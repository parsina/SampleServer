package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.livescore.Form;
import com.coin.app.model.livescore.FormType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository extends JpaRepository<Form, Long>
{
    List<Form> findAllByType(FormType type);
}
