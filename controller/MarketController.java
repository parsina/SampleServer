package com.coin.app.controller;

import java.util.List;

import com.coin.app.dto.binance.MarketData;
import com.coin.app.service.BinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketController
{
    @Autowired
    private BinanceService binanceService;

    @GetMapping("/marketData")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<MarketData> marketData()
    {
        return binanceService.getMarketData();
    }
}
