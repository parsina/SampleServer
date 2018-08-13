package com.coin.app.service;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;
import com.coin.app.dto.binance.MarketData;
import com.coin.app.util.Utills;
import org.springframework.stereotype.Service;

@Service
public class BinanceServiceImpl implements BinanceService
{
    private static String apiKey = "zg9qGIEnCYQ2MYZ1a8J2y1feUK6vJNyCuP2iee60jSK6TMgSFC3HCKLGlLsGf0F0";
    private static String secretKey = "yDQjg1vhfC27svv9LjU7FRugeA8j8GVdy0EYaN5qjH867HdDJjnEcfnI9emC8dl6";
    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
    BinanceApiRestClient client = factory.newRestClient();

    @Override
    public List<MarketData> getMarketData()
    {
        List<MarketData> marketDataList = new ArrayList<>();

        for (TickerStatistics statistic : client.getAll24HrPriceStatistics().subList(0,10))
            if (statistic.getSymbol().toLowerCase().contains("btc") && !statistic.getSymbol().toLowerCase().substring(0, 3).equals("btc"))
            {
                MarketData marketData = new MarketData();
                marketData.setSymbol(statistic.getSymbol().toLowerCase().replace("btc", ""));
                marketData.setAskPrice(Utills.toFakePrice(statistic.getAskPrice(), false));
                marketData.setBidPrice(Utills.toFakePrice(statistic.getBidPrice(), true));
                marketData.setMarketValue(Utills.commaSeparator(String.valueOf(statistic.getCount())));
                marketData.setMarketVolume(Utills.commaSeparator(statistic.getVolume()));
                marketData.setChangePercent(Utills.commaSeparator(statistic.getPriceChangePercent()));
                marketDataList.add(marketData);
            }
        return marketDataList;
    }
}
