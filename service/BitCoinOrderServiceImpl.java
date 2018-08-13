package com.coin.app.service;

import java.util.ArrayList;
import java.util.List;

import com.coin.app.dto.data.BitCoinOrderData;
import com.coin.app.model.BitCoinOrder;
import com.coin.app.model.enums.OrderType;
import com.coin.app.repository.BitCoinOrderRepository;
import com.coin.app.util.Utills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BitCoinOrderServiceImpl implements BitCoinOrderService
{
    @Autowired
    private BitCoinOrderRepository bitCoinOrderRepository;

    @Override
    public List<BitCoinOrderData> findOrders(OrderType orderType)
    {
        List<BitCoinOrderData> bitCoinOrderData = new ArrayList<>();
        List<BitCoinOrder> bitCoinOrders = bitCoinOrderRepository.findFirst5ByOrderType(orderType,  orderType.equals(OrderType.SELL) ? Sort.by("price").ascending() : Sort.by("price").descending());
        int i = 1;
        for(BitCoinOrder bitCoinOrder: bitCoinOrders)
        {
            BitCoinOrderData data = new BitCoinOrderData();
            data.setIndex(i);
            data.setOrderId(bitCoinOrder.getId());
            data.setPrice(Utills.commaSeparator(bitCoinOrder.getPrice().toString()));
            data.setAmount(bitCoinOrder.getAmount().toString());
            bitCoinOrderData.add(data);
            i++;
        }

        while (i < 6)
        {
            BitCoinOrderData data = new BitCoinOrderData();
            data.setIndex(i);
            bitCoinOrderData.add(data);
            i++;
        }

        return bitCoinOrderData;
    }
}
