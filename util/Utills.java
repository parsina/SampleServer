package com.coin.app.util;

import java.text.DecimalFormat;

public class Utills
{
    public static String toFakePrice(String price, boolean bid)
    {
        int suffix = price.split("\\.")[1].length();
        double amount = Double.parseDouble(price);
        StringBuilder pattern = new StringBuilder("#,###.");

        for (int i = 0; i < suffix; i++)
        {
            amount = amount * 10;
            pattern.append("#");
        }

        if(bid)
            amount = amount - (0.01 * amount);
        else
            amount = amount + (0.01 * amount);
        amount = Math.round(amount);

        for (int i = 0; i < suffix; i++)
            amount = amount / 10;

        DecimalFormat formatter = new DecimalFormat(pattern.toString());

        return formatter.format(amount);
    }

    public static String commaSeparator(String value)
    {
        double amount = Double.parseDouble(value);
        DecimalFormat formatter = new DecimalFormat("#,###.#########");
        return formatter.format(amount);
    }
}
