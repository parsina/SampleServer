package com.coin.app.dto.data;

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

public class BitCoinOrderData
{
    private int index;
    private Long orderId;
    private String price;
    private String amount;
}
