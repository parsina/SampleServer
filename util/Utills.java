package com.coin.app.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;

import com.coin.app.model.User;
import com.itextpdf.text.Document;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;

import ir.huri.jcal.JalaliCalendar;

public class Utills
{
//    @Value("${app.photpCal.directory}")
//    private String propertiesPath;

    private static Properties countryProps = new Properties();
    private static Properties leagueProps = new Properties();
    private static Properties teamProps = new Properties();
    private static String propertiesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();


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

    public static String addLeadingZeros(int count, long num, boolean split)
    {
        String str = String.format("%0" + (count + 1) + "d", num);
        String part1 = str.substring(0, (count + 1) / 2);
        String part2 = str.substring((count + 1) / 2, str.length());
        return part2 + (split ? "-" : "") + part1;
    }

    public static String getFarsiName(String key)
    {
        if(key == null)
            return null;
        try
        {
            countryProps.load(new FileInputStream(propertiesPath + "country.properties"));
            leagueProps.load(new FileInputStream(propertiesPath + "league.properties"));
            teamProps.load(new FileInputStream(propertiesPath + "team.properties"));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return countryProps.get(key) != null ? countryProps.get(key).toString() :
                leagueProps.get(key) != null ? leagueProps.get(key).toString() :
                        teamProps.get(key) != null ? teamProps.get(key).toString() : key;
    }
}
