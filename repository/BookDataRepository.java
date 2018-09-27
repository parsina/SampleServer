package com.coin.app.repository;

import com.coin.app.model.single.BookData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookDataRepository extends JpaRepository<BookData, Long>
{
}