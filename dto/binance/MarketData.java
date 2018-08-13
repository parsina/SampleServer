package com.coin.app.dto.binance;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode

public class MarketData
{
    private String symbol;
    private String askPrice;
    private String bidPrice;
    private String marketValue;
    private String marketVolume;
    private String changePercent;
}
