package com.coin.app.service.mail;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.repository.AccountRepository;
import com.coin.app.service.AccountService;
import com.coin.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService
{
    @Value("${client.url}")
    private String appUrl;

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
    @Override
    public void sendActivationLink(String email)
    {
        User user = userService.findByEmail(email);
        while(user.getAccount() == null)
            user.setAccount(accountService.createAccount(user));
        userService.saveUser(user);

        String subject = "تایید ثبت نام";
        String header = "لطفا جهت تایید ایمیل و فعال سازی حساب خود بر روی لینک زیر کلیک نمایید: " ;
        String message = appUrl + "/#/confirmRegistration?token=" + user.getConfirmationToken();
        try
        {
            this.sendEmailTo(email, subject, header, message, false);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendInvitationEmail(String email)
    {
        String subject = "دعوت";
        String header = "شما به شرکت در مسابقات پیش بینی فوتبال دعوت شده اید." ;
        String message = "لطفا جهت شرکت در مسابقات روی لینک زیر کلیک نمایید:";
        try
        {
            this.sendEmailTo(email, subject, header, message, true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public ResultData sendWithdrawalCode(String userId)
    {
        ResultData resultData = new ResultData(true, "");
        String subject = "کد امنیتی";
        String header = "لطفا کد امنیتی زیر را در محل مربوطه وارد نمایید: " ;
        Random rnd = new Random();
        String message = ( 100000 + rnd.nextInt(900000 )) + "";

        User user = userService.findById(Long.valueOf(userId));
        Account account = user.getAccount();
        account.setDescription(message);
        accountRepository.save(account);
        try
        {
            this.sendEmailTo(user.getEmail(), subject, header, message, false);
        } catch (Exception e)
        {
            e.printStackTrace();
            return new ResultData(false, "Email sending problem!");
        }
        return resultData;
    }

    @Async
    @Override
    public void sendNewPassword(String email, String password)
    {
        String subject = "بازیابی کلمه عبور";
        String header = "لطفا از کلمه عبور زیر برای ورود به سامانه استفاده نمایید و پس از ورود، در قسمت حساب کاربری آن را تغییر دهید.";
        String message = password.trim();
        try
        {
            this.sendEmailTo(email, subject, header, message, true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }





    // Replace sender@example.com with your "From" address.
    // This address must be verified.
//    static final String FROM = "info@bigbitbet.com";
//    static final String FROMNAME = "Admin Info";

    // Replace recipient@example.com with a "To" address. If your account
    // is still in the sandbox, this address must be verified.
//    static final String TO = "jfarzaneh@gmail.com";

    // Replace smtp_username with your Amazon SES SMTP user name.
//    static final String SMTP_USERNAME = "AKIAICPIFB4W5S57BIVQ";

    // Replace smtp_password with your Amazon SES SMTP password.
//    static final String SMTP_PASSWORD = "AhXbtX20GG+5gnQNm+/6ZARJieBeZs1UuYoCdfP8MOgT";

    // The name of the Configuration Set to use for this message.
    // If you comment out or remove this variable, you will also need to
    // comment out or remove the header below.
//    static final String CONFIGSET = "ConfigSet";

    // Amazon SES SMTP host name. This example uses the US West (Oregon) region.
    // See https://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html#region-endpoints
    // for more information.
//    static final String HOST = "email-smtp.us-east-1.amazonaws.com";

    // The port you will connect to on the Amazon SES SMTP endpoint.
//    static final int PORT = 587;

//    static final String SUBJECT = "Amazon SES test (SMTP interface accessed using Java)";

//    static final String BODY = String.join(
//            System.getProperty("line.separator"),
//            "<h1>Amazon SES SMTP Email Test</h1>",
//            "<p>This email was sent with Amazon SES using the ",
//            "<a href='https://github.com/javaee/javamail'>Javamail Package</a>",
//            " for <a href='https://www.java.com'>Java</a>."
//    );


    private void sendEmailTo(String email, String subject, String bodyHeader, String bodyMessage, boolean sendURL) throws Exception
    {



//        <!DOCTYPE html>
//<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
//<head></head>
//<body dir="rtl">
//<div dir="rtl" style="text-align: right; height:40px;border:1px #555555 solid;width:100%;background-color: rgba(73,171,20,0.65)">
//    <label style="font-size: large;">
//        <span th:text="${message}"></span>
//    </label>
//</div>
//<div style="border:1px #555555 solid; width:100%;padding: 10px; background-color: rgba(158,249,21,0.13);">
//    <div dir="rtl" style="width:80%;display:flex;text-align: center;">
//        <p>
//            <span><a th:href="${data}">فعال سازی حساب</a></span>
//        </p>
//    </div>
//</div>
//</body>
//</html>



        String body = String.join(
                System.getProperty("line.separator"),
            "<body style='text-align:center' dir='rtl'>",
                "<h1>" + bodyHeader + "</h1>",
                "<h2>" + bodyMessage + "<h2></br></br>",
                sendURL ? "<p><a href='" + appUrl + "'>" + appUrl + "</a></p>" : "</br>",
                "</body>"
        );



        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 587);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress("info@bigbitbet.com", ""));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        msg.setSubject(subject, "UTF-8");
        msg.setContent(body, "text/html; charset=UTF-8;");


        // Add a configuration set header. Comment or delete the
        // next line if you are not using a configuration set
//        msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try
        {
            System.out.println("Sending Email to : " + email);

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect("email-smtp.us-east-1.amazonaws.com", "AKIAICPIFB4W5S57BIVQ", "AhXbtX20GG+5gnQNm+/6ZARJieBeZs1UuYoCdfP8MOgT");

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        } catch (Exception ex)
        {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally
        {
            // Close and terminate the connection.
            transport.close();
        }
    }
}
