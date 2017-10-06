package com.queatz.snappy.logic;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Gateway;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by jacob on
 * 5/29/16.
 */
public class EarthEmail {
    public void sendRawEmail(EarthThing fromPerson,
                             EarthThing toPerson,
                             String subject,
                             String message) {

        message = fromPerson.getString(EarthField.EMAIL) + " " + message;

        sendRawEmail(
                toPerson.getString(EarthField.EMAIL),
                subject,
                message);
    }

    public void sendRawEmail(String toPerson,
                             String subject,
                             String message) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.VILLAGE_EMAIL_ADDRESS, Gateway.VILLAGE_EMAIL_PASSWORD);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Config.VILLAGE_EMAIL));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toPerson));
            msg.setSubject(subject, "utf-8");
            msg.setContent(message, "text/html");
            Transport.send(msg);
        } catch (MessagingException e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
