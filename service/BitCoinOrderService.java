package com.coin.app.service;

import java.util.List;

import com.coin.app.dto.data.BitCoinOrderData;
import com.coin.app.model.enums.OrderType;

public interface BitCoinOrderService
{
    List<BitCoinOrderData> findOrders(OrderType orderType);
}
