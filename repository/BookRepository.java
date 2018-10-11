package com.coin.app.repository;

import com.coin.app.model.livescore.single.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long>
{
}
