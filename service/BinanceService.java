package com.coin.app.service;

import java.util.List;

import com.coin.app.dto.binance.MarketData;

public interface BinanceService
{
    List<MarketData> getMarketData();
}
