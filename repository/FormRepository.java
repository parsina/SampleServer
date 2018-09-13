package com.coin.app.repository;

import com.coin.app.model.livescore.Form;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository  extends JpaRepository<Form, Long>
{
}
