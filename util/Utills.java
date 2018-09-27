package com.coin.app.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Date;

import ir.huri.jcal.JalaliCalendar;

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

        if (bid)
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

    public static String nameDisplayForDate(String date, boolean includeYear)
    {
        String year = date.split("-")[0].substring(2, 4);
        int month = Integer.valueOf(date.split("-")[1]);
        String day = date.split("-")[2];
        return day + " " + JalaliMonths.values()[month - 1] + " " + (includeYear ? year : "");
    }

    public static String nameDisplayForDate(LocalDate localDate, boolean includeYear)
    {
        return nameDisplayForDate(new JalaliCalendar(localDate).toString(), includeYear);
    }

    public static String shortDisplayForTime(String time)
    {
        return time.split(":")[0] + ":" + time.split(":")[1];
    }

    public static String addLeadingZeros(int count, long num)
    {
        return String.format("%0" + (count + 1) + "d", num);
    }
}
