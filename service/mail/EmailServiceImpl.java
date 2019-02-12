package com.coin.app.service.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.coin.app.dto.data.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
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
    private MailContentBuilderService mailContentBuilderService;

    @Override
    public void sendVerification(String email, String token)
    {
        String subject = "Email Verification";
        String header = "Please click on following link to confirm your verification: " ;
        String message = appUrl + "/#/confirmVerification?token=" + token;
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
    public ResultData sendWithdrawalCode(String email, String code)
    {
        ResultData resultData = new ResultData(true, "");
        String subject = "Security Code";
        String header = "Please use following security code: " ;

        try
        {
            this.sendEmailTo(email, subject, header, code, false);
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
        String subject = "Reset Password";
        String header = "Please use your email address with following password to login and after your first login change your password: ";
        String message = password.trim();
        try
        {
            this.sendEmailTo(email, subject, header, message, true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sendEmailTo(String email, String subject, String bodyHeader, String bodyMessage, boolean sendURL) throws Exception
    {
        String body = String.join(
                System.getProperty("line.separator"),
            "<body>",
                "<h1>" + bodyHeader + "</h1>",
                "<h2>" + bodyMessage + "<h2></br></br>", sendURL ? "<p><a href='" + appUrl + "'>" + appUrl + "</a></p>" : "</br>",
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

        msg.setFrom(new InternetAddress("noreply@bitrixo.com", ""));
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
