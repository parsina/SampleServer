package com.coin.app.service.mail;

public interface MailContentBuilderService
{
    String build(String message, String link, String template);
}
