package com.coin.app.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilderServiceImpl implements MailContentBuilderService
{
    @Autowired
    private TemplateEngine templateEngine;

    public String build(String message, String data, String templete)
    {
        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("data", data);
        return templateEngine.process(templete, context);
    }
}
