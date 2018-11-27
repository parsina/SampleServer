package com.coin.app.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;

import com.coin.app.model.User;
import com.coin.app.model.enums.FixtureStatus;
import com.itextpdf.text.Document;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;

import ir.huri.jcal.JalaliCalendar;

public class Utills
{
    private static Properties countryProps = new Properties();
    private static Properties leagueProps = new Properties();
    private static Properties teamProps = new Properties();
    public static String propertiesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();

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

    public static String getFarsiName(String key)
    {
        if (key == null)
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

    public static String fixtureStatusFarsiDescription(FixtureStatus status)
    {
        switch (status)
        {
            case LIVE:
                return "زنده";
            case BREAK:
                return "وقفه در بازی";
            case CANCEL:
                return "بازی لغو شد";
            case DELAYED:
                return "بازی به تاخیر افتاد";
            case HT:
                return "پایان نیمه اول";
            case FT:
                return "پایان بازی";
            case DELETED:
                return "بازی حذف شد";
            default:
                return "بازی هنوز شروع نشده";
        }
    }

    public static String formFarsiName(String name)
    {
        String type = name.split("-")[0].trim().toUpperCase();
        if (type.equals("GLD"))
            return "طلایی " + name.split("-")[1] + "-" + name.split("-")[2];
        else if(type.equals("SLV"))
            return "نقره ای " + name.split("-")[1] + "-" + name.split("-")[2];
        return "برنزی " + name.split("-")[1] + "-" + name.split("-")[2];
    }
}
