package com.coin.app.controller;

import java.util.List;

import com.coin.app.dto.data.BitCoinOrderData;
import com.coin.app.model.enums.OrderType;
import com.coin.app.service.BitCoinOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BitCoinOrderController
{
    @Autowired
    private BitCoinOrderService bitCoinOrderService;

    @GetMapping("/bitCoinBuyOrders")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<BitCoinOrderData> buyOrders()
    {
        return bitCoinOrderService.findOrders(OrderType.BUY);
    }

    @GetMapping("/bitCoinSellOrders")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<BitCoinOrderData> sellOrders()
    {
        return bitCoinOrderService.findOrders(OrderType.SELL);
    }

}
