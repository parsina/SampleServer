package com.coin.app.service.mail;

import java.util.Random;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.repository.AccountRepository;
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
    private AccountRepository accountRepository;

    @Autowired
    private MailContentBuilderService mailContentBuilderService;

    @Async
    public void sendActivationLink(String email)
    {
        User user = userService.findByEmail(email);
        while(user.getAccount() == null)
            user.setAccount(accountService.createAccount(user));
        userService.saveUser(user);

        String message = "لطفا جهت تایید ایمیل و فعال سازی حساب خود بر روی لینک زیر کلیک نمایید: " ;
        String link = "http://localhost:4200/confirmRegistration?token=" + user.getConfirmationToken();

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
            System.out.println(">>>>> Email sending problem!");
        }

        System.out.println(">>>>> Invitation email send to : " + user.getEmail());
    }

    @Async
    @Override
    public void sendInvitationEmail(String email)
    {
        String message = "شما به شرکت در مسابقات پیش بینی فوتبال دعوت شده اید. لطفا جهت شرکت در مسابقات روی لینک زیر کلیک نمایید. " ;
        String link = "http://localhost:4200/";

        MimeMessagePreparator messagePreparator = mimeMessage ->
        {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(email);
            messageHelper.setSubject("ثبت نام");
            String content = mailContentBuilderService.build(message, link, "invitation");
            messageHelper.setText(content, true);
        };
        try
        {
            sender.send(messagePreparator);
        } catch (MailException e)
        {
            // runtime exception; compiler will not force you to handle it
            System.out.println(">>>>> Email sending problem!");
        }

        System.out.println(">>>>> Invitation email send to : " + email);
    }

    @Async
    @Override
    public ResultData sendWithdrawalCode(String userId)
    {
        String message = "لطفا کد امنیتی زیر را در محل مربوطه وارد نمایید: " ;
        Random rnd = new Random();
        String code = ( 100000 + rnd.nextInt(900000 )) + " ";

        ResultData resultData = new ResultData(true, "");
        if(userService.getCurrentUser().getId().equals(Long.valueOf(userId)))
        {
            Account account = userService.getCurrentUser().getAccount();
            account.setDescription(code);
            accountRepository.save(account);

            MimeMessagePreparator messagePreparator = mimeMessage ->
            {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setTo(userService.getCurrentUser().getEmail());
                messageHelper.setSubject("کد امنیتی");
                String content = mailContentBuilderService.build(message, code, "securityCode");
                messageHelper.setText(content, true);
            };
            try
            {
                sender.send(messagePreparator);
                return resultData;
            } catch (MailException e)
            {
                // runtime exception; compiler will not force you to handle it
                return new ResultData(false, "Email sending problem!");
            }
        }
        else return new ResultData(false, "Authentication problem!");
    }
}
