package com.coin.app.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class Validator
{
    public static boolean isValidEmailAddress(String email)
    {
        boolean result = true;
        try
        {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex)
        {
            result = false;
        }
        return result;
    }

    public static boolean isValidPassword(String password)
    {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
        return password.matches(pattern);
    }
}
