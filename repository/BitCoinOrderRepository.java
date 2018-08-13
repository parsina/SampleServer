package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.BitCoinOrder;
import com.coin.app.model.enums.OrderType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BitCoinOrderRepository extends JpaRepository<BitCoinOrder, Long>
{
    List<BitCoinOrder> findFirst5ByOrderType(OrderType orderType, Sort sort);
}
