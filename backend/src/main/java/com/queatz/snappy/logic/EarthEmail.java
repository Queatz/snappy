package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.google.common.html.HtmlEscapers;
import com.queatz.snappy.shared.Config;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by jacob on 5/29/16.
 */
public class EarthEmail {
    public void sendRawEmail(Entity fromPerson,
                             Entity toPerson,
                             String subject,
                             String message) {

        message = fromPerson.getString(EarthField.EMAIL) + " says " + message;

        sendRawEmail(
                toPerson.getString(EarthField.EMAIL),
                subject,
                message);
    }

    public void sendRawEmail(String toPerson,
                             String subject,
                             String message) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

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