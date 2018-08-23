package com.coin.app.controller;

import java.util.List;

import com.coin.app.dto.binance.MarketData;
import com.coin.app.service.BinanceService;
import com.coin.app.service.LiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MarketController
{
    @Autowired
    private BinanceService binanceService;

    @Autowired
    private LiveScoreService liveScoreService;

    @GetMapping("/marketData")
    public List<MarketData> marketData()
    {
        liveScoreService.loadData();
        return binanceService.getMarketData();
    }
}
