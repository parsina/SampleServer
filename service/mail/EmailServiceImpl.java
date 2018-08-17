package com.coin.app.service.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.coin.app.model.User;
import com.coin.app.service.AccountService;
import com.coin.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService
{
    @Autowired
    private JavaMailSender sender;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MailContentBuilderService mailContentBuilderService;

    @Async
    public void sendActivationLink(String email)
    {
        User user = userService.findByUsername(email);
        user.setAccount(accountService.createAccount(user));
        userService.saveUser(user);

        String message = "لطفا جهت تایید ایمیل و فعال سازی حساب خود بر روی لینک زیر کلیک نمایید: " ;
        String link = "http://localhost:4200/confirm?token=" + user.getConfirmationToken();

        MimeMessagePreparator messagePreparator = mimeMessage ->
        {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("تایید ثبت نام");
            String content = mailContentBuilderService.build(message, link, "activationLink");
            messageHelper.setText(content, true);
        };
        try
        {
            sender.send(messagePreparator);
        } catch (MailException e)
        {
            // runtime exception; compiler will not force you to handle it
        }

        System.out.println(">>>>> Invitation email send to : " + user.getEmail());
    }
}
